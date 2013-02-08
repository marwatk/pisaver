package net.marcuswatkins.pisaver.gl;
import java.io.IOException;

import javax.media.opengl.GL2ES2;

import net.marcuswatkins.pisaver.NativeScreen;


public class GLScreen implements NativeScreen<GL2ES2,GLTextureData> {

	private GL2ES2 gl;
	private float left;
	private float top;
	private float bottom;
	private float right;
	public GLScreen( GL2ES2 gl ) throws IOException {
		this.gl = gl;
		GLImage.init( gl );
	}
	
	public GL2ES2 getRenderer() {
		return gl;
	}

	@Override
	public float getScreenBottom() {
		return bottom;
	}

	@Override
	public float getScreenLeft() {
		return left;
	}

	@Override
	public float getScreenRight() {
		return right;
	}

	@Override
	public float getScreenTop() {
		return top;
	}
	public float getAspect() {
		return ( right - left ) / ( top - bottom );
	}

	public void reshape( int width, int height ) {
		if( width > height ) {
			gl.glViewport( 0, (width - height) / -2, width, width);
			left = -1.0f;
			right = 1.0f;
			top = ( (float)height / (float)width );
			bottom = -top;
		}
		else {
			gl.glViewport( (height - width) / -2, 0, height, height );
			top = 1.0f;
			bottom = 1.0f;
			right = ( (float)width / (float)height );
			left = -right;
		}

	}
	
	public GLImage buildImage( GLTextureData t ) {
		return new GLImage( gl, t );
	}

}
