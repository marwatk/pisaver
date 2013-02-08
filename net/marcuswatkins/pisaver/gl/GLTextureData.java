package net.marcuswatkins.pisaver.gl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.marcuswatkins.pisaver.CacheablePreparedImage;
import net.marcuswatkins.pisaver.util.Util;

import com.jogamp.opengl.util.texture.TextureData;

public class GLTextureData implements CacheablePreparedImage<GLTextureData> {
	public static final int BYTE_ORDER_ARGB = 1;
	public static final int BYTE_ORDER_RGBA = 2;
	public static final int BYTE_ORDER_RGB_565 = 3;
	public static final int BYTE_ORDER_RGB = 4;
	public static final int BYTE_ORDER_NA = 5;
	
	int byteOrder;
	
	public GLTextureData(TextureData data) {
		this.tData = data;
		height = data.getHeight();
		width = data.getWidth();
		byteOrder = BYTE_ORDER_NA;
	}
	public GLTextureData(byte[] data, int width, int height, int byteOrder ) {
		this.bData = data;
		this.width = width;
		this.height = height;
		this.byteOrder = byteOrder;
	}
	public GLTextureData(int[] data, int width, int height, int byteOrder) {
		this( Util.intArrayToByteArray( data ), width, height, byteOrder );
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
	public boolean isCacheable() {
		return bData != null; //Only byte arrays and int arrays are cacheable right now
	}
	@Override
	public void writeCache(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream( new BufferedOutputStream( os, BUFFER_SIZE ) );
		dos.writeInt( SPECIAL );
		if( bData != null ) {
			dos.writeByte( TYPE_BYTE );
			dos.writeInt( width );
			dos.writeInt( height );
			dos.writeInt( bData.length );
			dos.writeInt( byteOrder );
			dos.write( bData );
			dos.flush();
		}
		else {
			throw new RuntimeException( "TextureData images don't support serialization (yet)" );
		}
	}
	@Override
	public GLTextureData readCached(InputStream is) throws IOException {
		DataInputStream dis = new DataInputStream( new BufferedInputStream( is, BUFFER_SIZE ) );
		if( dis.readInt() != SPECIAL ) {
			throw new IOException( "Texture cache doesn't have proper header" );
		}
		byte type = dis.readByte();
		int width = dis.readInt();
		int height = dis.readInt();
		int length = dis.readInt();
		int byteOrder = dis.readInt();
		if( type == TYPE_BYTE ) {
			byte bytes[] = new byte[length];
			dis.readFully( bytes );
			return new GLTextureData( bytes, width, height, byteOrder );
		}
		throw new IOException( "Invalid type: " + type );
	}
}
