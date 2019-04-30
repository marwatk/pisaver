package net.marcuswatkins.pisaver.filters;

import java.io.File;

import net.marcuswatkins.pisaver.sources.FileSourceImage;
import net.marcuswatkins.pisaver.sources.SourceImage;
import net.marcuswatkins.pisaver.util.Util;


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
	public boolean passesFilter(SourceImage image) {
		try {
			/*
			for (Directory directory : metadata.getDirectories()) {
			    for (Tag tag : directory.getTags()) {
			    	System.out.println( tag );
			    	System.out.println( tag.getTagName() + "/" + tag.getTagType() + " => " + directory.getString( tag.getTagType() ) );
			    }
			}
			*/
			float rating = image.getRating( );
			if( minRating >= 0 && rating < minRating ) {
				System.err.println( "File " + image + " doesn't fit minRating ( " + rating + " < " + minRating + ")" );
				return false;
			}
			if( maxRating >= 0 && rating > maxRating ) {
				System.err.println( "File " + image + " doesn't fit maxRating ( " + rating + " > " + maxRating + ")" );
				return false;
			}
			String tags[] = image.getTags( );
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
	
	public static void main( String args[] ) {
		File f = new File( "D:\\Personal\\eyefi\\11-19-2011\\IMG_4250.JPG");
		ImageMetaDataFilter filter = new ImageMetaDataFilter( 1.0f, -1.0f, new String[] { "antmissa" }, new String[] { "nfow" } );
		System.err.println( filter.passesFilter( new FileSourceImage( f ) ) );
	}

}
