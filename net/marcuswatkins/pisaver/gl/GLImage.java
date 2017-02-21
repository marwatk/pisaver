package net.marcuswatkins.pisaver.gl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import net.marcuswatkins.pisaver.NativeImage;
import net.marcuswatkins.pisaver.sources.SourceImage;
import net.marcuswatkins.pisaver.util.Util;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


public class GLImage implements NativeImage<GL2ES2,GLTextureData> {

	private static final short[] indicesData = {0, 1, 2, 0, 2, 3};
	private static final ShortBuffer indices = Buffers.newDirectShortBuffer(indicesData);
	private static int indicesBufferIdx = 0;
	
	private float[] verticesData;
	private FloatBuffer vertices;
	
	private Texture tex;
	private int textureId = -1;
	
	private float alpha = 0.1f;
	private float scaleX = 0.1f;
	private float scaleY = 0.1f;
	private float xshift = 0.0f;
	private float yshift = 0.0f;
	
	private float rotation = 0.0f;
	
	private static GLShader ARGB_SHADER;
	private static GLShader RGB_SHADER;
	//private static GLShader BACKGROUND_SHADER;
	
	private GLShader shader;
	private boolean antiAlias = true;
	
	private SourceImage sourceImage;
	
	private static GLImage shadow;
	
	private int width;
	private int height;
	
	
	public static void setShadow( GLImage image ) {
		shadow = image;
	}
	
	public static void init(GL2ES2 gl) throws IOException {

		ARGB_SHADER = new GLShader( gl, Util.readResource( "vShader2.vs" ), Util.readResource( "fShader_argb.fs" ) );
		RGB_SHADER = new GLShader( gl, Util.readResource( "vShader2.vs" ), Util.readResource( "fShader_rgb.fs" ) );
		//BACKGROUND_SHADER = new GLShader( gl, Util.readResource( "vShader_bg.vs" ), Util.readResource( "fShader_rgb.fs" ) );
		IntBuffer intBuf = IntBuffer.allocate( 1 );
		gl.glGenBuffers(1, intBuf);
		indicesBufferIdx = intBuf.get( 0 );
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, indicesBufferIdx );
		gl.glBufferData( GL.GL_ELEMENT_ARRAY_BUFFER, indices.remaining() * 2, indices, GL.GL_STATIC_DRAW);
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
	}

	//These help eliminate weird scaling artifacts on the bottom and right of the image (probably has to do with scaling at not exact aspect ratio (int approximation))
	//These cut off the edges of the image ever so slightly (2 tenths of a percent, so ~2 pixels on a 1024 texture)
	private static final float TEXTURE_MIN = 0.002f;
	private static final float TEXTURE_MAX = 0.998f;
	public GLImage( GL2ES2 gl, GLTextureData td ) {
		this( gl, td, false );
	}
	
	public GLImage( GL2ES2 gl, GLTextureData td, boolean useBackgroundShader ) {
		
		sourceImage = td.getSource();
		antiAlias = !useBackgroundShader;
        long start = System.currentTimeMillis();
		width = td.width;
		height = td.height;
		float texMin = useBackgroundShader ? 0.0f : TEXTURE_MIN;
		float texMax = useBackgroundShader ? 1.0f : TEXTURE_MAX;
		//Create coords with proper aspect for texture
		if( width > height ) {
			float hVal = ( (float)height / (float)width );
			verticesData = new float[] {
					-1.0f, hVal, 0.0f, // Top left
					texMin, texMin,        // TexCoord 0
					-1.0f, -hVal, 0.0f, // Bottom Left
					texMin, texMax,         // TexCoord 1
					1.0f, -hVal, 0.0f, // Bottom Right
					texMax, texMax,        // TexCoord 2
					1.0f, hVal, 0.0f, // Top Right
					texMax, texMin        // TexCoord 3
				};
			
		}
		else {
			float wVal = ( (float)width / (float)height );
			verticesData = new float[] {
					-wVal, 1.0f, 0.0f, // Top left
					texMin, texMin,        // TexCoord 0
					-wVal, -1.0f, 0.0f, // Bottom Left
					texMin, texMax,         // TexCoord 1
					wVal, -1.0f, 0.0f, // Bottom Right
					texMax, texMax,        // TexCoord 2
					wVal, 1.0f, 0.0f, // Top Right
					texMax, texMin         // TexCoord 3
				};			
		}
		vertices = Buffers.newDirectFloatBuffer(verticesData);	
		if( td.bData != null ) {
			int glPixelType = 0;
			switch( td.byteOrder ) {
				case GLTextureData.BYTE_ORDER_ARGB:
					shader = ARGB_SHADER;
					glPixelType = GL2ES2.GL_UNSIGNED_BYTE;
					break;
				case GL2ES2.GL_RGB:
					shader = /* useBackgroundShader ? BACKGROUND_SHADER : */ RGB_SHADER;
					glPixelType = GL2ES2.GL_UNSIGNED_BYTE;
					break;
				default:
					System.err.println( "Need to update shader to handle normal pixel byte orders again!" );
					System.exit( 0 );
					break;
					
			}
			loadTextureFromBytes( gl, td.bData, width, height, glPixelType, td.byteOrder );
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
	
	private void loadTextureFromBytes( GL2ES2 gl, byte[] data, int width, int height, int glPixelType, int byteOrder ) {
		int[] texId = new int[1];
		texId[0] = -1;
		ByteBuffer pixelBuffer = Buffers.newDirectByteBuffer( data );
		gl.glPixelStorei( GL2ES2.GL_UNPACK_ALIGNMENT, 2 );
		gl.glGenTextures( 1, texId, 0 );
		gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, texId[0]);
		
		if( byteOrder == GLTextureData.BYTE_ORDER_ARGB ) {
			byteOrder = GL2ES2.GL_RGBA; //This will get flipped to ARGB in the shader for speed
		}
		
		//gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, GL2ES2.GL_RGBA, width, height, 0, byteOrder, glPixelType, pixelBuffer);
		gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, byteOrder, width, height, 0, byteOrder, glPixelType, pixelBuffer);
		
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MIN_FILTER, GL2ES2.GL_LINEAR);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MAG_FILTER, GL2ES2.GL_LINEAR);
		textureId = texId[0];
	}
	
	private void loadTextureFromData(GL2ES2 gl, TextureData texData) {
		System.err.println( "Creatign GLImage" );
		this.tex = TextureIO.newTexture( texData );
		tex.setTexParameterf(gl,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
        tex.setTexParameterf(gl,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
        tex.setTexParameterf(gl,GL.GL_TEXTURE_WRAP_S,GL.GL_REPEAT);
        tex.setTexParameterf(gl,GL.GL_TEXTURE_WRAP_T,GL.GL_REPEAT);
	}

	public void setAlpha( float alpha ) {
		this.alpha = alpha;
	}
	public void setScale( float scale ) {
		this.scaleX = this.scaleY = scale;
	}
	public void setScale( float scaleX, float scaleY ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
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
	
	private static final float ANTI_ALIAS_ARRAY[] = {-0.0008f,0.0008f}; 
	private static final float ANTI_ALIAS_ALPHA_ADJ = 0.3f;
	public void draw(GL2ES2 gl) {
		
		if( shadow != null && shadow != this ) {
			shadow.antiAlias = false;
			float shadowScaleX = 0.0f;
			float shadowScaleY = 0.0f;
			if( width > height ) {
				shadowScaleX = scaleX;
				shadowScaleY = ((float)height / (float)width) * shadowScaleX;
			}
			else {
				shadowScaleY = scaleY;
				shadowScaleX = ((float)width / (float)height) * shadowScaleY;
			}
			shadow.setAlpha( alpha * 0.7f );
			shadow.setShift( xshift + 0.03f, yshift - 0.03f);
			
			shadow.setScale( shadowScaleX, shadowScaleY );
			shadow.setRotation( rotation );
			shadow.draw( gl );
		}
		
		
		shader.activate();
		
		vertices.position(0);
		shader.setPosCoords( verticesData.length, vertices );
		vertices.position(3);
		shader.setTexCoords( verticesData.length, vertices );
		
		shader.setRotation( rotation );
		
		//System.err.println( "Alpha: " + alpha + ", Scale: " + scale + ", Coords: " + xshift + ", " + yshift );
		gl.glActiveTexture(GL2ES2.GL_TEXTURE0);
		if( tex != null ) {
			tex.bind( gl );
		}
		else if( textureId != -1 ) {
			gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, textureId );
		}
		shader.setTexIdx( 0 );

		//For help antialiasing:
		//gl.glVertexAttrib4f( adjustmentsIdx, alpha * 0.3f, scale * 1.007f, xshift, yshift);
		//gl.glDrawElements(GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, indices);
		shader.setAdjustments( alpha, scaleX, scaleY, xshift, yshift);
		
		gl.glBindBuffer( GL.GL_ELEMENT_ARRAY_BUFFER, indicesBufferIdx );
		gl.glDrawElements( GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, 0 );
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0);
		//GLUtil.glDrawElements( gl, GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, indices);
		
		if( false /*antiAlias*/ ) { //Totally hackish AA
			float newScaleX = scaleX * 1.001f;
			float newScaleY = scaleX * 1.001f;
			int size = ANTI_ALIAS_ARRAY.length;
			float newAlpha = alpha * ANTI_ALIAS_ALPHA_ADJ;
			for( int i = 0; i < size; i++ ) {
				for( int j = 0; j < size; j++ ) {
					shader.setAdjustments( newAlpha, newScaleX, newScaleY, xshift + ANTI_ALIAS_ARRAY[i], yshift + ANTI_ALIAS_ARRAY[j] );
					gl.glBindBuffer( GL.GL_ARRAY_BUFFER, indicesBufferIdx );
					gl.glDrawElements( GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, 0 );
					//GLUtil.glDrawElements( gl, GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_SHORT, indices);
				}
			}
		}

	}
	
	
	@Override
	public void dispose( GL2ES2 gl ) {
		if( textureId != -1 ) {
			gl.glDeleteTextures( 1, new int[] { textureId }, 0 );
		}
		else if( tex != null ) {
			tex.destroy( gl );
		}
		shader.dispose();		
	}

	@Override
	public SourceImage getSource() {
		return sourceImage;
	}

}