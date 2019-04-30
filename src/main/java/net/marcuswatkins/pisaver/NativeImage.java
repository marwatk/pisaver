package net.marcuswatkins.pisaver;

import net.marcuswatkins.pisaver.sources.SourceImage;


public interface NativeImage<R,T> {
	public void setAlpha( float alpha );
	public void setScale( float scale );
	public void setShift( float x, float y );
	public void setRotation( float radians );
	public void draw(R gl);
	public void setPosition(float x, float y);
	public void dispose(R gl);
	public SourceImage getSource();
	public int getWidth();
	public int getHeight();
}
