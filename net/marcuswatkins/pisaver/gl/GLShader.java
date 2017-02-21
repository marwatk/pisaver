package net.marcuswatkins.pisaver.gl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;

public class GLShader {

	private int shader;
	private int positionIdx;
	private int texCoordIdx;
	private int rotationIdx;
	private int sampIdx;
	private int adjustmentsIdx;
	private int scaleyIdx;
	private GL2ES2 gl;

	private static IntBuffer vertexBuffers = null;

	public GLShader( GL2ES2 gl, String vertexShader, String fragmentShader ) {
		if( vertexBuffers == null ) {
			vertexBuffers = IntBuffer.allocate( 2 );
			gl.glGenBuffers(2, vertexBuffers);
		}
		shader = GLUtil.createProgram(gl, vertexShader, fragmentShader );
		this.gl = gl;
		//Not all shaders have all attributes, the try catches to to ingore them when using DebugGL2 wrapper
		try {
			positionIdx = gl.glGetAttribLocation(shader, "a_position");
		}
		catch( Exception e ) { }
		try {
			texCoordIdx = gl.glGetAttribLocation(shader, "a_texCoord");
		}
		catch( Exception e ) {
		}
		try {
			rotationIdx = gl.glGetAttribLocation( shader, "rad_angle" );
		}
		catch( Exception e ) {
			
		}
		try {
			scaleyIdx = gl.glGetAttribLocation( shader, "scale_y" );
		}
		catch( Exception e ) { }
		try { 
			adjustmentsIdx = gl.glGetAttribLocation(shader, "adjustments" );
		}
		catch( Exception e ) {
			
		}
		
		try {
			sampIdx = gl.glGetUniformLocation(shader, "s_texture");
		}
		catch( Exception e ) {
			
		}
		
	}
	
	
	public void setTexCoords( int stride, FloatBuffer vertices ) {
		glVertexAttribPointer(gl, vertexBuffers.get(0), texCoordIdx, 2, GL2ES2.GL_FLOAT, false, stride, vertices);
		gl.glEnableVertexAttribArray(texCoordIdx);
	}
	
	public void setPosCoords( int stride, FloatBuffer vertices ) {
		glVertexAttribPointer(gl, vertexBuffers.get(1), positionIdx, 3, GL2ES2.GL_FLOAT, false, stride, vertices);
		gl.glEnableVertexAttribArray(positionIdx);
	}
	private void glVertexAttribPointer( GL2ES2 gl, int bufferName, int indx, int size, int type, boolean normalized, int stride, FloatBuffer vertices) {
		
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, bufferName );
		gl.glBufferData( GL.GL_ARRAY_BUFFER, vertices.remaining() * (Float.SIZE / 8), vertices, GL.GL_STATIC_DRAW);
		gl.glVertexAttribPointer(indx, 2, GL2ES2.GL_FLOAT, false, stride, 0);
		gl.glBindBuffer( GL.GL_ARRAY_BUFFER, 0 );
	}
	
	public void setRotation( float rotation ) {
		if( rotationIdx >= 0 ) {
			gl.glVertexAttrib1f( rotationIdx, rotation);
		}
	}
	public void activate() {
		gl.glUseProgram( shader );
	}
	
	public void setTexIdx( int idx ) {
		gl.glUniform1i(sampIdx, idx);
	}
	
	public void setAdjustments( float alpha, float scaleX, float scaleY, float xshift, float yshift ) {
		gl.glVertexAttrib4f( adjustmentsIdx, alpha, scaleX, xshift, yshift);
		if( scaleyIdx >= 0 ) {
			gl.glVertexAttrib1f( scaleyIdx, scaleY );
		}
	}
	

	public void dispose() {
	}
}
