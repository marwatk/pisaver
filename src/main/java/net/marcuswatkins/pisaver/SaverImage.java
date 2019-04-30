package net.marcuswatkins.pisaver;

import net.marcuswatkins.pisaver.sources.SourceImage.Rotations;

/**
 * 
 */

public class SaverImage<R,T> {
	private NativeImage<R,?> nativeImage;
	private SaverAnim animation;
	
	public SaverImage( NativeImage<R,?> nativeImage, float x, float y, SaverAnim anim ) {
		this.nativeImage = nativeImage;
		nativeImage.setPosition( x, y );
		this.animation = anim;
	}
	
	public boolean animate() {
		return animation.animate( this, nativeImage );
	}
	public void draw( R renderer ) {
		this.nativeImage.draw( renderer );
	}

	public void cleanup( R renderer ) {
		this.nativeImage.dispose( renderer );
	}
}