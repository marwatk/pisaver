package net.marcuswatkins.pisaver.gl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.media.opengl.GL2ES2;

import net.marcuswatkins.pisaver.PiSaver;
import net.marcuswatkins.pisaver.util.Util;

import com.jogamp.common.nio.Buffers;


public class GLUtil {

	public static int compileShader(GL2ES2 gl, String shader, int type) {
		// Compile the vertexShader String into a program.
		int shaderNum = gl.glCreateShader(type);
	
		String[] flines = new String[]{shader};
		int[] flengths = new int[]{flines[0].length()};
		gl.glShaderSource(shaderNum, flines.length, flines, flengths, 0);
		gl.glCompileShader(shaderNum);
	
		// Check compile status.
		int[] compiled = new int[1];
		gl.glGetShaderiv(shaderNum, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] != 0) {
			System.out.println("Horray! shader compiled");
		} else {
			int[] logLength = new int[1];
			gl
					.glGetShaderiv(shaderNum, GL2ES2.GL_INFO_LOG_LENGTH,
							logLength, 0);
	
			byte[] log = new byte[logLength[0]];
			gl.glGetShaderInfoLog(shaderNum, logLength[0], (int[]) null, 0,
					log, 0);
	
			System.err.println("Error compiling the vertex shader: "
					+ new String(log));
			System.exit(1);
		}
		return shaderNum;
	
	}

	public static int createProgram(GL2ES2 gl, String vShaderStr,
			String fShaderStr) {
		System.err.println( "Vertex Shader:\n" + vShaderStr + "\n\n\n" );
		System.err.println( "Fragment Shader:\n" + fShaderStr + "\n\n\n" );
		
		int vShader = compileShader(gl, vShaderStr, GL2ES2.GL_VERTEX_SHADER);
		int fShader = compileShader(gl, fShaderStr, GL2ES2.GL_FRAGMENT_SHADER);
	
		int shader = gl.glCreateProgram();
		gl.glAttachShader(shader, vShader);
		gl.glAttachShader(shader, fShader);
	
		gl.glLinkProgram(shader);
	
		return shader;
	}

	public static String readResource(String name) throws IOException {
		URL url = PiSaver.class.getResource(name);
		InputStream is = url.openStream();
		Scanner s = new Scanner(is, "UTF-8");
		String rval = s.useDelimiter("\\A").next();
		s.close();
		return rval;
	}

	public static int createSimpleTexture2D(GL2ES2 gl) {
		int[] textureId = new int[1];
		byte[] pixels = {(byte) 0xff, 0, 0, // Red
				0, (byte) 0xff, 0, // Green
				0, 0, (byte) 0xff, // Blue
				(byte) 0xff, (byte) 0xff, 0 // Yellow
		};
		ByteBuffer pixelBuffer = Buffers.newDirectByteBuffer(pixels);
	
		gl.glPixelStorei(GL2ES2.GL_UNPACK_ALIGNMENT, 1);
		gl.glGenTextures(1, textureId, 0);
		gl.glBindTexture(GL2ES2.GL_TEXTURE_2D, textureId[0]);
		gl.glTexImage2D(GL2ES2.GL_TEXTURE_2D, 0, GL2ES2.GL_RGB, 2, 2, 0, GL2ES2.GL_RGB, GL2ES2.GL_UNSIGNED_BYTE, pixelBuffer);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MIN_FILTER, GL2ES2.GL_NEAREST);
		gl.glTexParameteri(GL2ES2.GL_TEXTURE_2D, GL2ES2.GL_TEXTURE_MAG_FILTER, GL2ES2.GL_NEAREST);
	
		return textureId[0];
	}

	public static int create565Texture( File f ) throws Exception {
		Dimension dims = Util.getImageDimensions( f );
		Dimension targetDim = Util.calcResizeDimensions( dims, GLUtil.MAX_DIMENSION );
		int[] textureId = new int[1];
		byte[] pixels565 = Util.createRGB888Bytes( f, targetDim.width, targetDim.height );
		return 0;
	}

	public static final boolean isBufferedImageTypeAllowed( int type ) { //from (line 302): https://projectsforge.org/projects/bundles/browser/trunk/jogl-2.0-rc3/jogl/src/main/java/com/jogamp/opengl/util/texture/awt/AWTTextureData.java?rev=18
		switch( type ) {
			case BufferedImage.TYPE_INT_RGB:
			case BufferedImage.TYPE_USHORT_565_RGB:
			case BufferedImage.TYPE_USHORT_555_RGB:
			case BufferedImage.TYPE_BYTE_GRAY:
			case BufferedImage.TYPE_USHORT_GRAY:
			case BufferedImage.TYPE_INT_ARGB:
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_BYTE_BINARY:
			case BufferedImage.TYPE_BYTE_INDEXED:
			case BufferedImage.TYPE_CUSTOM:
				return true;
		}
		return false;
	}

	public static final int PREFERRED_TYPE = BufferedImage.TYPE_USHORT_565_RGB;
	public static final int MAX_DIMENSION = 1024;

}
