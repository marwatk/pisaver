package net.marcuswatkins.pisaver.gl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2ES2;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.util.Util;




public class GLImagePreparer implements ImagePreparer<GLTextureData>{
	private int lastPrepareTime;
	
	public GLImagePreparer( GL2ES2 gl ) {
	}

	@Override
	public GLTextureData prepareImage(File f) throws Exception {
		return prepareImageBufferedImageGetRGB( f );
	}

	private GLTextureData prepareImageBufferedImageGetRGB( File f ) throws Exception {
		long start = System.currentTimeMillis();
		
    	try {
			System.err.println( "Preparing (int): " + f.getName() );
			BufferedImage src = ImageIO.read( f );
			Dimension dim = new Dimension( src.getWidth(), src.getHeight() );
			Dimension newDim = Util.calcResizeDimensions( dim, GLUtil.MAX_DIMENSION );
	
			byte array[] = scaleImageJava( src, newDim.width, newDim.height );
            //TextureData tex = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
            return new GLTextureData( array, newDim.width, newDim.height, GLTextureData.BYTE_ORDER_ARGB );
    	}
        finally {
        	lastPrepareTime = (int)(System.currentTimeMillis() - start);
        }
		
		
	}
	
	public static byte[] scaleImageJava( BufferedImage img, int newWidth, int newHeight ) {
		
		BufferedImage dst = new BufferedImage( newWidth, newHeight, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g = dst.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(), img.getHeight(), null);
	    g.dispose();
	    int tempArray[] = dst.getRGB( 0, 0, newWidth, newHeight, null, 0, newWidth ); //We use this directly by swizzling in the shader (GL doesn't support ARGB, only RGBA, so we need to move the alpha byte)
		/*
		for( int i = 0; i < newWidth * 2; i++ ) { //Attempt at helping aliasing artifacts
			array[i] |= 0x7F000000;
		}
		*/
		return Util.intArrayToByteArray( tempArray );
		
	}
	
	@Override
	public int getLastPrepareTime() {
		return lastPrepareTime;
	}
	
}
