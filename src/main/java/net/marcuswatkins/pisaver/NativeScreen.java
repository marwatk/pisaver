package net.marcuswatkins.pisaver;

import net.marcuswatkins.pisaver.gl.GLImage.ImageType;

/**
 * 
 */

public interface NativeScreen<R,T extends PreparedImage> {
	public R getRenderer();
	public float getScreenLeft();
	public float getScreenRight();
	public float getScreenTop();
	public float getScreenBottom();
	public float getAspect();
	public NativeImage<R,T> buildImage( T t, ImageType type );
	public NativeImage<R,T> captureScreen();

}