package net.marcuswatkins.pisaver.gl;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_REPEAT;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.GL2ES2;

import net.marcuswatkins.pisaver.NativeImage;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


public class GLImage implements NativeImage<GL2ES2,GLTextureData> {

	private static short[] indicesData = {0, 1, 2, 0, 2, 3};
	private static ShortBuffer indices = Buffers.newDirectShortBuffer(indicesData);

	private static int shader;
	private static int positionIdx;
	private static int texCoordIdx;
	private static int rotationIdx;
	private static int sampIdx;
	private static int adjustmentsIdx;

	private float[] verticesData;
	private FloatBuffer vertices;
	
	private Texture tex;
	private int textureId = -1;
	
	private float alpha = 0.1f;
	private float scale = 0.1f;
	private float xshift = 0.0f;
	private float yshift = 0.0f;
	
	private float rotation = 0.0f;
	
	public static void init(GL2ES2 gl) throws IOException {

		shader = GLUtil.createProgram(gl, GLUtil.readResource( "vShader2.vs" ), GLUtil.readResource( "fShader.fs" ) );

		positionIdx = gl.glGetAttribLocation(shader, "a_position");
		texCoordIdx = gl.glGetAttribLocation(shader, "a_texCoord");
		rotationIdx = gl.glGetAttribLocation( shader, "rad_angle" );
		adjustmentsIdx = gl.glGetAttribLocation(shader, "adjustments" );
		
		sampIdx = gl.glGetUniformLocation(shader, "s_texture");

	}

	//These help eliminate weird scaling artifacts on the bottom and right of the image (probably has to do with scaling at not exact aspect ratio (int approximation))
	//These cut off the edges of the image ever so slightly (2 tenths of a percent, so ~2 pixels on a 1024 texture)
	private static final float TEXTURE_MIN = 0.002f;
	private static final float TEXTURE_MAX = 0.998f;
	
	public GLImage( GL2ES2 gl, GLTextureData td ) {
        long start = System.currentTimeMillis();
		int width = td.width;
		int height = td.height;
		//Create coords with proper aspect for texture
		if( width > height ) {
			float hVal = ( (float)height / (float)width );
			verticesData = new float[] {
					-1.0f, hVal, 0.0f, // Top left
					TEXTURE_MIN, TEXTURE_MIN,        // TexCoord 0
					-1.0f, -hVal, 0.0f, // Bottom Left
					TEXTURE_MIN, TEXTURE_MAX,         // TexCoord 1
					1.0f, -hVal, 0.0f, // Bottom Right
					TEXTURE_MAX, TEXTURE_MAX,        // TexCoord 2
					1.0f, hVal, 0.0f, // Top Right
					TEXTURE_MAX, TEXTURE_MIN        // TexCoord 3
				};
			
		}
		else {
			float wVal = ( (float)width / (float)height );
			verticesData = new float[] {
					-wVal, 1.0f, 0.0f, // Top left
					TEXTURE_MIN, TEXTURE_MIN,        // TexCoord 0
					-wVal, -1.0f, 0.0f, // Bottom Left
					TEXTURE_MIN, TEXTURE_MAX,         // TexCoord 1
					wVal, -1.0f, 0.0f, // Bottom Right
					TEXTURE_MAX, TEXTURE_MAX,        // TexCoord 2
					wVal, 1.0f, 0.0f, // Top Right
					TEXTURE_MAX, TEXTURE_MIN         // TexCoord 3
				};			
		}
		vertices = Buffers.newDirectFloatBuffer(verticesData);	
		if( td.bData != null ) {
			int glPixelType = 0;
			switch( td.byteOrder ) {
				case GLTextureData.BYTE_ORDER_ARGB:
					glPixelType = GL2ES2.GL_UNSIGNED_BYTE;
					break;
				default:
					System.err.println( "Need to update shader to handle normal pixel byte orders again!" );
					System.exit( 0 );
					break;
					
			}
			loadTextureFromBytes( gl, td.bData, width, height, glPixelType );
		}
		else if( td.tData != null ) {
			loadTextureFromData( gl, td.tData );
		}
		
		System.err.println( "Finished creating GLImage (byte[]) (" + (System.currentTimeMillis() - start ) + ")" );
		
	}
	
	private void loadTextureFromInts( GL2ES2 gl, int[] data, int width, int height ) {
		int[] texId = new int[1];
		texId[0] = -1;
		IntBuffer pixelBuffer = Buffers.newDirectIntBuffer( data );
		gl.glPixelStorei( GL2ES2.GL_UNPACK_ALIGNMENT, 2 );
		gl.glGenTextures( 1, texId, 0 );
		gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, texId[0]);
		gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, GL2ES2.GL_RGBA, width, height, 0, GL2ES2.GL_RGBA, GL2ES2.GL_UNSIGNED_BYTE, pixelBuffer);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MIN_FILTER, GL2ES2.GL_LINEAR);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MAG_FILTER, GL2ES2.GL_LINEAR);
		textureId = texId[0];
	}
	
	private void loadTextureFromBytes( GL2ES2 gl, byte[] data, int width, int height, int glPixelType ) {
		int[] texId = new int[1];
		texId[0] = -1;
		ByteBuffer pixelBuffer = Buffers.newDirectByteBuffer( data );
		gl.glPixelStorei( GL2ES2.GL_UNPACK_ALIGNMENT, 2 );
		gl.glGenTextures( 1, texId, 0 );
		gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, texId[0]);
		gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, GL2ES2.GL_RGBA, width, height, 0, GL2ES2.GL_RGBA, GL2ES2.GL_UNSIGNED_BYTE, pixelBuffer);
		
		//RGB
		//gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, GL2ES2.GL_RGB, width, height, 0, GL2ES2.GL_RGB, GL2ES2.GL_UNSIGNED_BYTE, pixelBuffer);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MIN_FILTER, GL2ES2.GL_LINEAR);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MAG_FILTER, GL2ES2.GL_LINEAR);
		textureId = texId[0];
	}
	
	private void loadTextureFromData(GL2ES2 gl, TextureData texData) {
		System.err.println( "Creatign GLImage" );
		this.tex = TextureIO.newTexture( texData );
		tex.setTexParameterf(gl,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        tex.setTexParameterf(gl,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        tex.setTexParameterf(gl,GL_TEXTURE_WRAP_S,GL_REPEAT);
        tex.setTexParameterf(gl,GL_TEXTURE_WRAP_T,GL_REPEAT);
	}

	public void setAlpha( float alpha ) {
		this.alpha = alpha;
	}
	public void setScale( float scale ) {
		this.scale = scale;
	}
	public void setShift( float x, float y ) {
		this.xshift = x;
		this.yshift = y;
	}
	public void setRotation( float radians ) {
		this.rotation = radians;
	}
	public void setPosition(float x, float y) {
		this.setShift( x, y );
		
	}
	
	public void draw(GL2ES2 gl) {
		
		gl.glUseProgram(shader);
		
		vertices.position(0);
		gl.glVertexAttribPointer(positionIdx, 3, GL2ES2.GL_FLOAT, false, verticesData.length, vertices);
		vertices.position(3);
		gl.glVertexAttribPointer(texCoordIdx, 2, GL2ES2.GL_FLOAT, false, verticesData.length, vertices);
		gl.glEnableVertexAttribArray(positionIdx);
		gl.glEnableVertexAttribArray(texCoordIdx);
		gl.glVertexAttrib1f( rotationIdx, rotation);
		//System.err.println( "Alpha: " + alpha + ", Scale: " + scale + ", Coords: " + xshift + ", " + yshift );
		gl.glActiveTexture(GL2ES2.GL_TEXTURE0);
		if( tex != null ) {
			tex.bind( gl );
		}
		else if( textureId != -1 ) {
			gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, textureId );
		}
		gl.glUniform1i(sampIdx, 0);

		//For help antialiasing:
		//gl.glVertexAttrib4f( adjustmentsIdx, alpha * 0.3f, scale * 1.007f, xshift, yshift);
		//gl.glDrawElements(GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, indices);
		
		
		gl.glVertexAttrib4f( adjustmentsIdx, alpha, scale, xshift, yshift);
		gl.glDrawElements(GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, indices);

	}
	
	@Override
	public void dispose( GL2ES2 gl ) {
		if( textureId != -1 ) {
			gl.glDeleteTextures( 1, new int[] { textureId }, 0 );
		}
		else if( tex != null ) {
			tex.destroy( gl );
		}
		
	}

}