package net.marcuswatkins.pisaver.sources;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.filters.ImageFilter;



public class FileImageSource<K> extends ImageSource<K> {
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
		hadGoodImage = false;
		files = filesAr;
	}

	@Override
	protected File getNextSourceItem() {
		return files.remove( files.size() - 1 );
	}

	@Override
	protected boolean haveNextSourceItem() {
		return files.size() != 0;
	}
	
}
