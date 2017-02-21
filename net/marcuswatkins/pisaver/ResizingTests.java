package net.marcuswatkins.pisaver;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import net.marcuswatkins.pisaver.gl.GLTextureData;
import net.marcuswatkins.pisaver.gl.GLUtil;
import net.marcuswatkins.pisaver.util.Util;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/*  Here were all of my attempts at speeding up the scaling of source images to fit in the pi's 2048 texture size. 
None were fast enough to create a smooth experience (even using imagemagick's fastest resize would take ~30-50 seconds)
so instead I implemented a texture cache so each image is only resized the first time it's displayed. (Requires lots of storage on the SD card)

*/

public class ResizingTests {

	static byte[] scaleImage565( BufferedImage bmp, int newWidth, int newHeight, int orig[], byte newVals[] ) {
			int curHeight = bmp.getHeight();
			int curWidth = bmp.getWidth();
			if( orig == null || orig.length < curHeight * curWidth ) {
				orig = new int[curHeight*curWidth];
			}
			bmp.getRGB( 0, 0, curWidth, curHeight, orig, 0, curWidth);
			
			if( newVals == null || newVals.length < newHeight * newWidth ) {
				newVals = new byte[2*newHeight*newWidth];
			}
			
			int xPerPix = curWidth / newWidth;
			int yPerPix = curHeight / newHeight;
			
			for( int y = 0; y < newHeight; y++ ) {
				int destRowOffset = ( y * newWidth * 2 );
				int startY = ( y * curHeight ) / newHeight;
				int endY = startY + yPerPix;
		
				for( int x = 0; x < newWidth; x++ ) {
					int destIdx = destRowOffset + ( x * 2 );
					int startX = ( x * curWidth ) / newWidth;
					int endX = startX + xPerPix;
					int sourceCount = 0;
					
					int Atot = 0;
					int Rtot = 0;
					int Gtot = 0;
					int Btot = 0;
					
					for( int sY = startY; sY < endY; sY++ ) {
						int sourceRowOffset = sY * curWidth;
						for( int sX = startX; sX < endX; sX++ ) {
							int dPixel = orig[sourceRowOffset + sX];
							sourceCount++;
							Atot += dPixel >>> 24;
							Rtot += ( dPixel & 0x00FF0000 ) >>> 16;
							Gtot += ( dPixel & 0x0000FF00 ) >>> 8;
							Btot += ( dPixel & 0x000000FF );	
						}
					}
					if( sourceCount > 0 ) {
						Atot = Atot / sourceCount;
						Rtot = Rtot / sourceCount;
						Gtot = Gtot / sourceCount;
						Btot = Btot / sourceCount;
					}
	/*				
					                                1  1  1  1  1  1  0  0
					  R   R   R   R   R  G   G   G  G  G  G  B  B  B  B  B
					[ 16, 15, 14, 13, 12 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
		*/			
					//newVals[destIdx] = (byte)0xF8;
					//newVals[destIdx] = (byte)0xFF;
					//newVals[destIdx+1] = (byte)0xFF;
					newVals[destIdx] =	(byte)((( Rtot & 0xF8 )) | (( Gtot & 0xFC ) >>> 5 ));
					 newVals[destIdx+1] = (byte)(((Gtot & 0xFC) << 3) | ((Btot & 0xFC) >>> 3 ));
						
					
				}
			}
			return newVals;
		}

	private static byte[] scaleImageHighMem( BufferedImage bmp, int newWidth, int newHeight, int orig[], byte newVals[] ) {
		int curHeight = bmp.getHeight();
		int curWidth = bmp.getWidth();
		if( orig == null || orig.length < curHeight * curWidth ) {
			orig = new int[curHeight*curWidth];
		}
		bmp.getRGB( 0, 0, curWidth, curHeight, orig, 0, curWidth);
		
		if( newVals == null || newVals.length < newHeight * newWidth ) {
			newVals = new byte[newHeight*newWidth*3];
		}
		
		int xPerPix = curWidth / newWidth;
		int yPerPix = curHeight / newHeight;
		
		for( int y = 0; y < newHeight; y++ ) {
			int destRowOffset = ( y * newWidth * 3 );
			int startY = ( y * curHeight ) / newHeight;
			int endY = startY + yPerPix;
	
			for( int x = 0; x < newWidth; x++ ) {
				int destIdx = destRowOffset + (x*3);
				int startX = ( x * curWidth ) / newWidth;
				int endX = startX + xPerPix;
				int sourceCount = 0;
				
				int Atot = 0;
				int Rtot = 0;
				int Gtot = 0;
				int Btot = 0;
				
				for( int sY = startY; sY < endY; sY++ ) {
					int sourceRowOffset = sY * curWidth;
					for( int sX = startX; sX < endX; sX++ ) {
						int dPixel = orig[sourceRowOffset + sX];
						sourceCount++;
						Atot += dPixel >>> 24;
						Rtot += ( dPixel & 0x00FF0000 ) >>> 16;
						Gtot += ( dPixel & 0x0000FF00 ) >>> 8;
						Btot += ( dPixel & 0x000000FF );	
					}
				}
				Atot = Atot / sourceCount;
				Rtot = Rtot / sourceCount;
				Gtot = Gtot / sourceCount;
				Btot = Btot / sourceCount;
				
				newVals[destIdx++] = (byte)Rtot;
				newVals[destIdx++] = (byte)Gtot;
				newVals[destIdx++] = (byte)Btot;
				
			}
		}
		return newVals;
	}

	static GLTextureData prepareImageExternal( File f ) throws Exception {
		long start = System.currentTimeMillis();
		try {
			System.err.println( "Preparing (ext): " + f.getName() );
			Dimension dim = Util.getImageDimensions( f );
			dim = Util.calcResizeDimensions( dim, GLUtil.MAX_DIMENSION );
			byte b[] = Util.createRGB888Bytes( f, dim.width, dim.height );
			System.err.println( "buffer length: " + b.length + " dims: " + dim );
			return new GLTextureData( b, dim.width, dim.height, GL2ES2.GL_RGB, null );
		}
	    finally {
	    	System.err.println( "Finished preparing (" + (System.currentTimeMillis() - start) + ")" );
	    }
		
	}

	static int[] buffer;
	static int[] getBuffer( int height, int width ) {
		if( buffer == null || buffer.length < ( height * width ) ) {
			buffer = new int[height*width];
		}
		return buffer;
	}

	private static GLTextureData prepareImageInternal888( File f ) throws Exception {
		long start = System.currentTimeMillis();
		
		try {
			System.err.println( "Preparing (int): " + f.getName() );
			BufferedImage src = ImageIO.read( f );
			Dimension dim = new Dimension( src.getWidth(), src.getHeight() );
			Dimension newDim = Util.calcResizeDimensions( dim, GLUtil.MAX_DIMENSION );
	
			byte array[] = scaleImage565( src, newDim.width, newDim.height, getBuffer( dim.height, dim.width ), null );
	        //TextureData tex = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
	        return new GLTextureData( array, newDim.width, newDim.height, GL2ES2.GL_RGB565, null );
		}
	    finally {
	    	System.err.println( "Finished preparing (" + (System.currentTimeMillis() - start) + ")" );
	    }
		
	}

	private static GLTextureData prepareImageInternalTextureData( File f ) throws Exception {
		long start = System.currentTimeMillis();
		System.err.println( "Preparing (int): " + f.getName() );
		Dimension dim = Util.getImageDimensions( f );
		if( dim.width > GLUtil.MAX_DIMENSION || dim.height > GLUtil.MAX_DIMENSION ) {
			f = Util.scaleImageFileToJpg( f, GLUtil.MAX_DIMENSION );
		}
		BufferedImage img = ImageIO.read( f );
		if( !GLUtil.isBufferedImageTypeAllowed( img.getType() ) ) {
			img = Util.convertBufferedImage( img, GLUtil.PREFERRED_TYPE );
		}
		try {
			TextureData tex = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
	        return new GLTextureData( tex, null );
		}
	    finally {
	    	System.err.println( "Finished preparing (" + (System.currentTimeMillis() - start) + ")" );
	    }
		
	}

}
