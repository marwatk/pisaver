package net.marcuswatkins.pisaver.sources;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.PreparedImage;
import net.marcuswatkins.pisaver.filters.ImageFilter;
import net.marcuswatkins.pisaver.util.Util;



public class FileImageSource<K extends PreparedImage> extends ImageSource<K> {
	protected File folders[];
	protected ArrayList<File> files = new ArrayList<File>();
	public FileImageSource( File folder, ImageFilter filter, ImagePreparer<K> preparer ) {
		this( new File[] { folder }, filter, preparer );
	}
	
	public FileImageSource( File folders[], ImageFilter filter, ImagePreparer<K> preparer ) {
		super( filter, preparer );
		this.folders = folders;
		startPrepare();
	}

	protected synchronized void refreshSource() {
		System.err.println( "ImageFinder.refreshList" );
		HashSet<File> newFiles = new HashSet<File>();
		for( int i = 0; i < folders.length; i++ ) {
			File folder = folders[i];
			scanFolder( folder, newFiles );
		}
		ArrayList<File> filesAr = new ArrayList<File>( newFiles.size() );
		copyCollectionInto( newFiles, filesAr );
		Collections.shuffle( filesAr );
		files = filesAr;
	}

	@Override
	protected SourceImage getNextSourceItem() {
		return new FileSourceImage( files.remove( files.size() - 1 ) );
	}

	@Override
	protected boolean haveNextSourceItem() {
		return files.size() != 0;
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
		else if( Util.isImage( folder ) ) {
			System.err.println( "Adding " + folder );
			files.add( folder );
		}
	}
	
}
