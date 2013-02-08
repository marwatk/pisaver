package net.marcuswatkins.pisaver;


/**
 * 
 */

public interface NativeScreen<R,T> {
	public R getRenderer();
	public float getScreenLeft();
	public float getScreenRight();
	public float getScreenTop();
	public float getScreenBottom();
	public float getAspect();
	public NativeImage<R,T> buildImage( T t );

}