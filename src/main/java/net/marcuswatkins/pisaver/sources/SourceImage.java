package net.marcuswatkins.pisaver.sources;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.marcuswatkins.pisaver.sources.SourceImage.Rotations;
import net.marcuswatkins.pisaver.util.Util;

public abstract class SourceImage {

	public static enum Rotations {
        NONE,
        LEFT,
        RIGHT,
        FULL,
    }
    public final BufferedImage getBufferedImage() throws IOException {
		InputStream is = getInputStream();
		BufferedImage img = ImageIO.read( getInputStream() );
		is.close();
		return img;
	}

	public abstract SourceImage.Rotations getRotation();
	public abstract float getRating();
	public abstract String[] getTags();
	public abstract String getUniqueReference();
	public abstract boolean isSpecial();
	public final byte[] getBytes() throws IOException {
		long start = System.currentTimeMillis();
		InputStream is = this.getInputStream();
		byte bytes[] = Util.readInputStream( is );
		is.close();
		System.err.println( "SourceImage.getBytes() completed in " + (System.currentTimeMillis() - start ) );
		return bytes;
	}
	public abstract InputStream getInputStream() throws IOException;
	public abstract boolean isJpeg();
}
