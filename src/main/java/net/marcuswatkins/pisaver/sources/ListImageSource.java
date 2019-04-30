package net.marcuswatkins.pisaver.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.PreparedImage;
import net.marcuswatkins.pisaver.filters.ImageFilter;
import net.marcuswatkins.pisaver.util.Util;

public class ListImageSource<K extends PreparedImage> extends ImageSource<K> {

	String cacheFile;
	ArrayList<ListSourceImage> files = new ArrayList<ListSourceImage>();
	
	public ListImageSource( String cacheFile, ImageFilter filter, ImagePreparer<K> preparer ) {
		super( filter, preparer );
		this.cacheFile = cacheFile;
	}
	
	
	@Override
	protected SourceImage getNextSourceItem() {
		return files.remove( files.size() - 1 );
	}

	@Override
	protected boolean haveNextSourceItem() {
		return files.size() > 0;
	}

	@Override
	protected void refreshSource() {
		System.err.println( "Refreshing list source" );
		try {
			ArrayList<ListSourceImage> newFiles = new ArrayList<ListSourceImage>();
			File cf = new File( cacheFile );
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( cf ) ) );
			String line;
			while( (line = reader.readLine()) != null ) {
				String parts[] = line.split( "\t" );
				if( parts.length == 3 ) {
					float rating = Util.safeParseFloat( parts[0], 0 );
					String tags[] = parts[1].split( "," );
					String filename = parts[2];
					File f = new File( filename );
					if( Util.isImage( f ) ) {
						newFiles.add( new ListSourceImage( f, rating, tags ) );
						//System.err.println( "added " + f.getPath() );
					}
				}
			}
			reader.close();
			Collections.shuffle( newFiles );
			files = newFiles;
		}
		catch( Exception e ) {
			System.err.println( "Error reading from cache file!" );
			e.printStackTrace();
		}
		System.err.println( "Finished refresh" );

	}

}
