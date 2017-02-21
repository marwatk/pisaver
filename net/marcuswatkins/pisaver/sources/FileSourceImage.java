package net.marcuswatkins.pisaver.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.marcuswatkins.pisaver.util.Util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.xmp.XmpDirectory;

public class FileSourceImage extends SourceImage {

	private File file;
	public FileSourceImage( File f ) {
		file = f;
	}
	@Override
	public float getRating() {
		return getRating( getMetaData() );
	}
	@Override
	public String[] getTags() {
		return getTags( getMetaData() );
	}
	@Override
	public String getUniqueReference() {
		try { return file.getCanonicalPath(); } catch( Exception e ) { }
		return file.getPath();
	}
	
	
	Metadata metaData;
	
	private Metadata getMetaData() {
		if( metaData == null ) {
			try {
				metaData = ImageMetadataReader.readMetadata( file );
			}
			catch( Exception e ) {
				
			}
		}
		return metaData;
		
	}

	public static float getRating( Metadata metadata ) {
		if( metadata == null ) {
			return 0;
		}
		Directory dir1 = metadata.getDirectory( XmpDirectory.class );
		if( dir1 != null ) {
			String ratingString = dir1.getString( XmpDirectory.TAG_RATING );
			if( ratingString != null ) {
				try {
					return Float.parseFloat( ratingString );
				}
				catch( Exception e ) {
					
				}
			}
		}
		Directory dir2 = metadata.getDirectory( ExifIFD0Directory.class );
		if( dir2 != null ) {
			String ratingString = dir2.getString( 0x4746 ); //Tag windows uses for ratings
			if( ratingString != null ) {
				try {
					return Float.parseFloat( ratingString );
				}
				catch( Exception e ) {
					
				}
			}
		}
		return 0;
		
	}
	
	public String[] getTags( Metadata metadata ) {
		if( metadata == null ) {
			return new String[0];
		}
		ExifIFD0Directory dir1 = metadata.getDirectory( ExifIFD0Directory.class );
		if( dir1 != null ) {
			ExifIFD0Descriptor desc = new ExifIFD0Descriptor( dir1 );
			String tags = desc.getWindowsKeywordsDescription();
			if( tags != null && tags.trim().length() > 0 ) {
				return tags.toLowerCase().trim().split( ";" );
			}
		}
		return new String[0];
	}
	@Override
	public boolean isSpecial() {
		return getRating() >= 4.9f;
	}
	public String toString() {
		return getUniqueReference();
	}
	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream( file );
	}
	@Override
	public boolean isJpeg() {
		return Util.isJpeg( file );
	}

}
