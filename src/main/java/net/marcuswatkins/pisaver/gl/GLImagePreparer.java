package net.marcuswatkins.pisaver.gl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import net.marcuswatkins.pisaver.ImagePreparer;
import net.marcuswatkins.pisaver.sources.SourceImage;
import net.marcuswatkins.pisaver.util.Util;

import com.jogamp.opengl.GL2ES2;



public class GLImagePreparer implements ImagePreparer<GLTextureData>{

	public static final int NORMAL_MAX_DIM = 640;
	public static final int SPECIAL_MAX_DIM = 2048;

	public static final int MAX_TEXTURE_DIM = 2048;
	
	private boolean useTurboJpeg = false;
	
	private int lastPrepareTime;
	public GLImagePreparer( GL2ES2 gl ) {
		try {
			Class.forName( "org.libjpegturbo.turbojpeg.TJDecompressor" );
			useTurboJpeg = true;
			System.err.println( "Using turbojpeg" );
		}
		catch( Throwable e ) {
			System.err.println( "Turbojpeg not available" );
		}
		
	}

	@Override
	public GLTextureData prepareImage(SourceImage f) throws Exception {
		return prepareImageBufferedImageGetRGB( f );
	}
	private GLTextureData prepareImageBufferedImageGetRGB( SourceImage f ) throws Exception {
		if( useTurboJpeg && f.isJpeg() ) {
			return prepareImageBufferedImageGetRGB_Turbo( f ); 
		}
		else {
			return prepareImageBufferedImageGetRGB_JDK( f );
		}
	}
	private GLTextureData prepareImageBufferedImageGetRGB_Turbo( SourceImage f ) throws Exception {
		return prepareImageBufferedImageGetRGB_JDK( f );
		/*
		long start = System.currentTimeMillis();
		TJDecompressor dec = null;
    	try {
			System.err.println( "Preparing (int): " + f.getUniqueReference() );
    		dec = new TJDecompressor( f.getBytes() );
		
			
			Dimension dim = new Dimension( dec.getWidth(), dec.getHeight() );
			Dimension newDim = Util.calcResizeDimensions( dim, f.isSpecial() ? SPECIAL_MAX_DIM : NORMAL_MAX_DIM );
			Dimension scaledDim = getScaledDims( dec, newDim );
			
			byte data[] = null;
			
			System.err.println( "Scaling during decode from " + dim.width + " to " + scaledDim.width );
			if( scaledDim.width > MAX_TEXTURE_DIM || scaledDim.height > MAX_TEXTURE_DIM ) {
				System.err.println( "Image needs further resizing" );
				BufferedImage img = dec.decompress( scaledDim.width, 0, BufferedImage.TYPE_INT_ARGB, 0 );
				data = scaleImageJava( img, newDim.width, newDim.height );
			}
			else {
				newDim = scaledDim;
				data = dec.decompress( scaledDim.width, 0, 0, TJ.PF_ARGB, 0 );
			}
			System.err.println( "Finished scaling in " + ( System.currentTimeMillis() - start ) + " milis, building texture...");
            //TextureData tex = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
            return new GLTextureData( data, newDim.width, newDim.height, GLTextureData.BYTE_ORDER_ARGB, f );
    	}
        finally {
        	lastPrepareTime = (int)(System.currentTimeMillis() - start);
        	if( dec != null ) dec.close();
        }
		*/
		
	}
	/*
	private Dimension getScaledDims( TJDecompressor dec, Dimension target ) throws Exception {
		
		int max = dec.getWidth();
		for( int i = target.width; i < max; i += 256 ) {
			try {
				int scaled = dec.getScaledWidth( i, 0 );
				if( scaled >= target.width ) {
					return new Dimension( scaled, Util.calcOtherDimension( dec.getWidth(), dec.getHeight(), scaled ) );
				}
			}
			catch( Exception e ) {
			}
		}
		return new Dimension( max, dec.getHeight() );
	}
	*/
	
	private GLTextureData prepareImageBufferedImageGetRGB_JDK( SourceImage f ) throws Exception {
		long start = System.currentTimeMillis();
		
    	try {
			System.err.println( "Preparing (int): " + f.getUniqueReference() );
			BufferedImage src = f.getBufferedImage();
			Dimension dim = new Dimension( src.getWidth(), src.getHeight() );
			Dimension newDim = Util.calcResizeDimensions( dim, f.isSpecial() ? SPECIAL_MAX_DIM : NORMAL_MAX_DIM );
	
			byte array[] = scaleImageJava( src, newDim.width, newDim.height );
			System.err.println( "Finished scaling in " + ( System.currentTimeMillis() - start ) + " milis, building texture...");
            //TextureData tex = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
            return new GLTextureData( array, newDim.width, newDim.height, GLTextureData.BYTE_ORDER_ARGB, f );
    	}
        finally {
        	lastPrepareTime = (int)(System.currentTimeMillis() - start);
        }
		
		
	}
	
	public static byte[] scaleImageJava( BufferedImage img, int newWidth, int newHeight ) {
		
		int tempArray[];
		if( newWidth == img.getWidth() && newHeight == img.getHeight() ) {
			System.err.println( "Using image in actual size" );
		    tempArray = img.getRGB( 0, 0, newWidth, newHeight, null, 0, newWidth ); //We use this directly by swizzling in the shader (GL doesn't support ARGB, only RGBA, so we need to move the alpha byte)
		}
		else {
			System.err.println( "Resizing image to fit texture" );
			BufferedImage dst = new BufferedImage( newWidth, newHeight, BufferedImage.TYPE_INT_ARGB );
			Graphics2D g = dst.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(), img.getHeight(), null);
		    g.dispose();
		    tempArray = dst.getRGB( 0, 0, newWidth, newHeight, null, 0, newWidth ); //We use this directly by swizzling in the shader (GL doesn't support ARGB, only RGBA, so we need to move the alpha byte)
		}
		
		return Util.intArrayToByteArray( tempArray );
	}

	
	@Override
	public int getLastPrepareTime() {
		return lastPrepareTime;
	}
	
}
