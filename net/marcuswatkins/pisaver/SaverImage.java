package net.marcuswatkins.pisaver;


/**
 * 
 */

public class SaverImage<R,T> {
	private NativeImage<R,?> nativeImage;
	private SaverAnim anims[];
	private float x;
	private float y;
	private int activeAnim = 0;
	
	public SaverImage( NativeImage<R,?> nativeImage, float x, float y, SaverAnim anim[] ) {
		this.nativeImage = nativeImage;
		nativeImage.setPosition( x, y );
		this.anims = anim;
	}
	
	public void animate() {
		anims[activeAnim].animate( this, nativeImage );
	}
	public void draw( R renderer ) {
		this.nativeImage.draw( renderer );
	}

	public void cleanup( R renderer ) {
		this.nativeImage.dispose( renderer );
	}
	public void setActiveAnim( int idx ) {
		this.activeAnim = idx;
	}
}