package net.marcuswatkins.pisaver.filters;

import java.io.File;

import net.marcuswatkins.pisaver.util.Util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Descriptor;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.xmp.XmpDirectory;


public class ImageMetaDataFilter implements ImageFilter {

	private String includedTags[];
	private String excludedTags[];
	private float maxRating;
	private float minRating;
	
	public ImageMetaDataFilter(  float minRating, float maxRating, String includedTags[], String excludedTags[] ) {
		this.includedTags = lowerCaseArray( includedTags );
		this.excludedTags = lowerCaseArray( excludedTags );
		
		this.maxRating = maxRating;
		this.minRating = minRating;
	}
	
	@Override
	public boolean passesFilter(File image) {
		try {
			Metadata metadata = ImageMetadataReader.readMetadata( image );
			/*
			for (Directory directory : metadata.getDirectories()) {
			    for (Tag tag : directory.getTags()) {
			    	System.out.println( tag );
			    	System.out.println( tag.getTagName() + "/" + tag.getTagType() + " => " + directory.getString( tag.getTagType() ) );
			    }
			}
			*/
			float rating = getRating( metadata );
			if( minRating >= 0 && rating < minRating ) {
				System.err.println( "File " + image + " doesn't fit minRating ( " + rating + " < " + minRating + ")" );
				return false;
			}
			if( maxRating >= 0 && rating > maxRating ) {
				System.err.println( "File " + image + " doesn't fit maxRating ( " + rating + " > " + maxRating + ")" );
				return false;
			}
			String tags[] = getTags( metadata );
			if( hasUnion( tags, excludedTags ) ) { //Exclude trumps include
				System.err.println( "File " + image + " matches excluded tags (tags: " + Util.join( tags, ";" ) + ")" );
				return false;
			}
			if( includedTags != null && includedTags.length > 0 && !hasUnion( includedTags, tags ) ) {
				System.err.println( "File " + image + " doesn't have included tag (tags: " + Util.join( tags, ";" ) + ")" );
				return false;
			}
			return true;
			
			/*
			*/
		}
		catch( Exception e ) {
			System.err.println( "Error reading image meta data for " + image );
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	private static String[] lowerCaseArray( String a[] ) {
		if( a == null ) {
			return null;
		}
		String b[] = new String[a.length];
		for( int i = 0; i < a.length; i++ ) {
			b[i] = a[i].toLowerCase();
		}
		return b;
	}
	
	public static boolean hasUnion( String a[], String b[] ) {
		if( a == null || b == null ) {
			return false;
		}
		for( int i = 0; i < a.length; i++ ) {
			for( int j = 0; j < b.length; j++ ) {
				if( a[i] != null && a[i].equals( b[j] ) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public float getRating( Metadata metadata ) {
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
	
	
	
	public static void main( String args[] ) {
		File f = new File( "D:\\Personal\\eyefi\\11-19-2011\\IMG_4250.JPG");
		ImageMetaDataFilter filter = new ImageMetaDataFilter( 1.0f, -1.0f, new String[] { "antmissa" }, new String[] { "nfow" } );
		System.err.println( filter.passesFilter( f ) );
	}

}
