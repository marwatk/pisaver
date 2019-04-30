package net.marcuswatkins.pisaver.gl;
import java.io.IOException;
import java.nio.ByteBuffer;

import net.marcuswatkins.pisaver.NativeImage;
import net.marcuswatkins.pisaver.NativeScreen;

import com.jogamp.opengl.GL2ES2;


public class GLScreen implements NativeScreen<GL2ES2,GLTextureData> {

	private GL2ES2 gl;
	private float left;
	private float top;
	private float bottom;
	private float right;
	private int width;
	private int height;
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

	public void reshape( int w, int h ) {
		this.width = w;
		this.height = h;
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

	@Override
	public NativeImage<GL2ES2, GLTextureData> captureScreen() {
		byte[] buffer = captureBuffer( );
		GLTextureData texture = new GLTextureData( buffer, width, height, GL2ES2.GL_RGB, null );
		GLImage image = new GLImage( gl, texture, true );
		image.setAlpha( 1.0f );
		image.setRotation( 0.0f );
		image.setScale( 1.0f, -1.0f );
		return image;
	}
	private byte[] captureBuffer( ) {
		byte[] buffer = new byte[width*height*3];
		ByteBuffer b = ByteBuffer.wrap( buffer );
		gl.glReadPixels( 0, 0, width, height, GL2ES2.GL_RGB, GL2ES2.GL_UNSIGNED_BYTE, b );
		return buffer;
	}
}
