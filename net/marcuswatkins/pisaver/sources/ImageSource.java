package net.marcuswatkins.pisaver.sources;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.filters.ImageFilter;

/**
 * 
 */

public abstract class ImageSource<K> {
	private K next;
	protected boolean hadGoodImage = true;
	private ImageFilter filter;
	private ImagePreparer<K> preparer;
	private boolean prepareRunning = false;
	
	public static final int MAX_IMAGES = Integer.MAX_VALUE;
	
	public ImageSource( ImageFilter filter, ImagePreparer<K> preparer ) {
		this.filter = filter;
		this.preparer = preparer;
		startPrepare();
	}
	
	public int getLastPrepareTime() {
		return preparer.getLastPrepareTime();
	}
	
	private synchronized void prepareNext() {
		prepareRunning = true;
		while( next == null ) {
			while( !haveNextSourceItem() ) {
				if( hadGoodImage ) { //We're at the end of the list, load a refreshed one
					refreshList( false );
				}
				else {
					System.err.println( "No images to show! (prepareNext(1))" );
					System.exit( 0 );
				}
			}
			File nextAttempt = getNextSourceItem();
			if( filter.passesFilter( nextAttempt ) ) {
				try {
					next = preparer.prepareImage( nextAttempt );
					hadGoodImage = true;
				}
				catch( Exception e ) {
					System.err.println( "Error preparing image for " + nextAttempt );
					e.printStackTrace();
				}
				catch( OutOfMemoryError e ) {
					System.err.println( "OutOfMemory error preparing image for " + nextAttempt + ", attempting to continue, no idea if this will work" );
					e.printStackTrace();
				}
			}
		}
		prepareRunning = false;
		this.notifyAll();
	}
	protected synchronized void startPrepare() {
		prepareRunning = true;
		new Thread() {
			public void run() {
				prepareNext();
			}
		}.start();
	}
	private final void refreshList( boolean startPrepare ) {
		refreshSource();
		if( startPrepare ) {
			startPrepare();
		}

	}
	protected abstract void refreshSource();
	protected abstract File getNextSourceItem(); //This will eventually need to be something other than 'File' if more sources are allowed
	protected abstract boolean haveNextSourceItem();
	
	public boolean nextReady() {
		return next != null;
	}
	
	public synchronized K getNextImage() {
		if( next == null && prepareRunning ) { //Need this to catch race condition for gap between thread starting and getting into prepareNext()
			System.err.println( "Waiting for prepare..." );
			try { 
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		if( next == null ) {
			System.err.println( "No images to show! (getNext())" );
			System.exit( 1 );
		}
		K rval = next;
		next = null;
		startPrepare();
		return rval;
		
	}
	
	private boolean isImage( File file ) {
		if( file.isDirectory() ) {
			return false;
		}
		if( !file.canRead() ) {
			return false;
		}
		String lName = file.getName().toLowerCase();
		if( lName.endsWith( ".jpg" ) || lName.endsWith( ".jpeg" ) ) {
			return true;
		}
		return false;
	}
	
	protected void scanFolder( File folder, Collection<File> files ) {
		if( files.contains( folder ) ) { //Don't scan same folder twice (should probably do canonical here, but this is quick and dirty
			return;
		}
		if( files.size() >= MAX_IMAGES ) {
			return;
		}
		//System.err.println( "Scanning: " + folder );
		if( folder.isDirectory() ) {
			File subFiles[] = folder.listFiles();
			for( int i = 0; i < subFiles.length; i++ ) {
				scanFolder( subFiles[i], files );
			}
		}
		else if( isImage( folder ) ) {
			System.err.println( "Adding " + folder );
			files.add( folder );
		}
	}
	 
	public static <T> void copyCollectionInto( Collection<? extends T> src, Collection<? super T> dst ) {
		Iterator<? extends T> iter = src.iterator();
		while( iter.hasNext() ) {
			dst.add( iter.next() );
		}
	}
	
}