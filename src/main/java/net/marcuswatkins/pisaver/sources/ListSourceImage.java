package net.marcuswatkins.pisaver.sources;

import java.io.File;

public class ListSourceImage extends FileSourceImage {

	private String tags[];
	private float rating;
	public ListSourceImage( File file, float rating, String tags[] ) {
	    super( file );
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
}
