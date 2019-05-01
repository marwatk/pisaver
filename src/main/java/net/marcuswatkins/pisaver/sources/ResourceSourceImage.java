package net.marcuswatkins.pisaver.sources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.marcuswatkins.pisaver.PiSaver;

public class ResourceSourceImage extends SourceImage {

	private String resource;
	public ResourceSourceImage( String resource ) {
		this.resource = resource;
	}
	
	@Override
	public float getRating() {
		return 0;
	}

	@Override
	public String[] getTags() {
		return new String[0];
	}

	@Override
	public String getUniqueReference() {
		return resource;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URL url = PiSaver.class.getClassLoader().getResource(resource);
		return url.openStream();
	}

	@Override
	public boolean isJpeg() {
		return resource.toLowerCase().endsWith( ".jpg" ) || resource.toLowerCase().endsWith( ".jpeg" );
	}

    @Override
    public Rotations getRotation() {
        return Rotations.NONE;
    }

	

}
