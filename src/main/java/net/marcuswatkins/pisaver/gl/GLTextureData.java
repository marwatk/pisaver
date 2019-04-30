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
	
	private static final byte TYPE_BYTE = 1;
	//private static final byte TYPE_INT = 2;
	private static final int SPECIAL = 0x8a78facd;
	private static final int BUFFER_SIZE = 4096;
	
	
	public TextureData tData;
	public byte[] bData;
	int width;
	int height;


	@Override
	public SourceImage getSource() {
		return sourceImage;
	}
}
