package net.marcuswatkins.pisaver.gl;

import com.jogamp.opengl.util.texture.TextureData;

import net.marcuswatkins.pisaver.PreparedImage;
import net.marcuswatkins.pisaver.sources.SourceImage;
import net.marcuswatkins.pisaver.util.Util;

public class GLTextureData implements PreparedImage {
	
	public static final int BYTE_ORDER_ARGB = 1;
	/*
	public static final int BYTE_ORDER_RGBA = 2;
	public static final int BYTE_ORDER_RGB_565 = 3;
	public static final int BYTE_ORDER_RGB = 4;
	public static final int BYTE_ORDER_NA = 5;
	*/
	
	int byteOrder;
	private SourceImage sourceImage;
	
	public GLTextureData(TextureData data, SourceImage source ) {
		this.tData = data;
		height = data.getHeight();
		width = data.getWidth();
		byteOrder = data.getPixelFormat();
		sourceImage = source;
	}
	public GLTextureData(byte[] data, int width, int height, int byteOrder, SourceImage source ) {
		this.bData = data;
		this.width = width;
		this.height = height;
		this.byteOrder = byteOrder;
		sourceImage = source;
	}
	public GLTextureData(int[] data, int width, int height, int byteOrder, SourceImage source) {
		this( Util.intArrayToByteArray( data ), width, height, byteOrder, source );
	}
	
	public GLTextureData() {
		//Empty constructor needed for loading
	}
	
	public TextureData tData;
	public byte[] bData;
	int width;
	int height;


	@Override
	public SourceImage getSource() {
		return sourceImage;
	}
}
