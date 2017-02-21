package net.marcuswatkins.pisaver.sources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.marcuswatkins.pisaver.util.Util;

public class ListSourceImage extends SourceImage {

	private String tags[];
	private float rating;
	private File file;
	public ListSourceImage( File file, float rating, String tags[] ) {
		this.file = file;
		this.rating = rating;
		this.tags = tags == null ? new String[0] : tags;
	}
	
	String filename;

	@Override
	public float getRating() {
		return rating;
	}

	@Override
	public String[] getTags() {
		return tags;
	}

	@Override
	public String getUniqueReference() {
		try {
			return file.getCanonicalPath();
		} catch( Exception e ) { }
		return file.getPath();
	}

	@Override
	public boolean isSpecial() {
		return rating > 4.9;
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
