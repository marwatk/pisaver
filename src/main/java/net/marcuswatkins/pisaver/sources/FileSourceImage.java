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

	//https://github.com/drewnoakes/metadata-extractor/commit/5b07a49f7b3d90c43a36a79dc4f6474845e1ebc7#diff-be3deaf11f6bf04ea83658e2604b8ac5L76
	private static final int XMP_RATING_TAG = 0x1001;
	private static final SourceImage.Rotations[] ORIENTATION_TO_ROTATION = { 
	        SourceImage.Rotations.NONE, // 0 
	        SourceImage.Rotations.NONE, // 1
	        SourceImage.Rotations.LEFT, // 2
	        SourceImage.Rotations.FULL, // 3
	        SourceImage.Rotations.RIGHT,// 4
	        SourceImage.Rotations.NONE, // 5
	        SourceImage.Rotations.LEFT, // 6
	        SourceImage.Rotations.FULL, // 7
	        SourceImage.Rotations.RIGHT // 8 
	};
	
	
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

		Directory dir1 = metadata.getFirstDirectoryOfType( XmpDirectory.class );
		if( dir1 != null ) {
			String ratingString = dir1.getString( XMP_RATING_TAG );
			if( ratingString != null ) {
				try {
					return Float.parseFloat( ratingString );
				}
				catch( Exception e ) {
					
				}
			}
		}
		Directory dir2 = metadata.getFirstDirectoryOfType( ExifIFD0Directory.class );
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
	
	public static String[] getTags( Metadata metadata ) {
		if( metadata == null ) {
			return new String[0];
		}
		ExifIFD0Directory dir1 = metadata.getFirstDirectoryOfType( ExifIFD0Directory.class );
		if( dir1 != null ) {
			ExifIFD0Descriptor desc = new ExifIFD0Descriptor( dir1 );
			String tags = desc.getWindowsKeywordsDescription();
			if( tags != null && tags.trim().length() > 0 ) {
				return tags.toLowerCase().trim().split( ";" );
			}
		}
		return new String[0];
	}
	
	public static SourceImage.Rotations getRotation( Metadata metadata ) {
        if( metadata == null ) {
            return Rotations.NONE;
        }
        ExifIFD0Directory dir1 = metadata.getFirstDirectoryOfType( ExifIFD0Directory.class );
        if( dir1 != null ) {
            try {
                int orientation = dir1.getInt( ExifIFD0Directory.TAG_ORIENTATION );
                return ORIENTATION_TO_ROTATION[orientation];
            }
            catch( Exception e ) {
                System.err.println("Error retrieving orientation: " + e.getMessage() );
            }
        }
        return Rotations.NONE;
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
    @Override
    public SourceImage.Rotations getRotation() {
        return getRotation( getMetaData() );
    }

}
