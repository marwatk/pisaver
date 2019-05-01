package net.marcuswatkins.pisaver.util;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.marcuswatkins.pisaver.PiSaver;
import net.marcuswatkins.pisaver.gl.GLUtil;


public class Util {

	public static byte[] readFile( File f ) throws IOException {
		byte b[] = new byte[(int)f.length()];
		DataInputStream is = new DataInputStream( new FileInputStream( f ) );
		is.readFully( b );
		is.close();
		return b;
	}
	public static byte[] readInputStream( InputStream is ) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
        for (int len; (len = is.read(buffer)) != -1;) {
            os.write(buffer, 0, len);
        }
        os.flush();
        return os.toByteArray();
	}

	public static String join( String s[], String sep ) {
		if( s == null ) {
			return "null";
		}
		String rval = "";
		for( int i = 0; i < s.length; i++ ) {
			if( i != 0 ) {
				rval += sep;
			}
			rval += s[i];
		}
		return rval;
	}
	
	public static void execAndWait( String cmd[] ) throws IOException {
		System.err.println( "execing: " + join( cmd, " " ) );
		Process process = Runtime.getRuntime().exec( cmd );
		BufferedReader in = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
		String line;
		while( ( line = in.readLine() ) != null ) {
			System.out.println( line );
		}
		in.close();
	}

	public static File scaleImageFileToJpg( File src, int maxDim ) throws IOException {
		File f = File.createTempFile( "SAVER", ".jpg" );
		execAndWait( new String[] {
			"/usr/bin/convert", src.getAbsolutePath(), "-filter", "Box", "-resize", maxDim + "x" + maxDim, f.getAbsolutePath()
		} );
		if( f.exists() ) {
			return f;
		}
		return null;
	}

	public static BufferedImage scaleImageInMem( BufferedImage src, int type, int newWidth, int newHeight ) {
		BufferedImage dest = new BufferedImage( newWidth, newHeight, GLUtil.PREFERRED_TYPE );
		Graphics2D g = dest.createGraphics();
		//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(src, 0, 0, newWidth, newHeight, 0, 0, src.getWidth(), src.getHeight(), null);
	    g.dispose();
		return dest;
	}

	public static BufferedImage scaleImageInMemAffine( BufferedImage src, int type, int newWidth, int newHeight ) {
		BufferedImage dest = new BufferedImage( newWidth, newHeight, type );
		AffineTransform at = new AffineTransform();
		float scale = (float)newWidth / (float)src.getWidth();
		at.scale( scale, scale );
		AffineTransformOp scaleOp = new AffineTransformOp( at, AffineTransformOp.TYPE_BILINEAR );
		dest = scaleOp.filter( src, dest );
		return dest;
	}

	public static Dimension getImageDimensions( File f ) throws Exception {
		ImageInputStream in = ImageIO.createImageInputStream( f );
		final Iterator<ImageReader> readers = ImageIO.getImageReaders( in );
		if( readers.hasNext() ) {
			ImageReader reader = readers.next();
			try {
				reader.setInput( in );
				return new Dimension( reader.getWidth( 0 ), reader.getHeight( 0 ) );
			}
			finally {
				reader.dispose();
			}
		}
		in.close();
		return null;
	}

	public static BufferedImage convertBufferedImage( BufferedImage src, int type ) {
		BufferedImage newImg = new BufferedImage( src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB );
		newImg.getGraphics().drawImage( src, 0, 0, null );
		return newImg;
	}

	public static float calcResizeScale( int width, int height, int maxDimension ) {
		if( maxDimension <= height && maxDimension >= width ) {
			return 1.0f;
		}
		if( height > width ) {
			return ( (float)maxDimension / (float)height );
		}
		else {
			return ( (float)maxDimension / (float)width );
		}
	}

	public static byte[] createRGB888Bytes( File f, int width, int height ) throws IOException {
		File tf = null;
		try {
			tf = File.createTempFile( "SAVER", ".raw" );
			execAndWait( new String[] {
				"/usr/bin/convert", f.getAbsolutePath(), "-depth", "8", "-filter", "Box", "-resize", width + "x" + height + "!", "rgb:" + tf.getAbsolutePath()
			} );
			if( tf.exists() ) {
				System.err.println( "Resuling file is " + tf.length() + " bytes" );
				byte b[] = readFile( tf );
				return b;
			}
			return null;
		}
		finally {
			tf.delete();
		}
	}
	
	public static int calcOtherDimension( int source1, int source2, int target1 ) {
		return ( source2 * target1) / source1;
	}

	public static Dimension calcResizeDimensions( Dimension src, int maxDim ) {
		if( src.width <= maxDim && src.height <= maxDim ) {
			return src;
		}
		if( src.width > src.height ) {
			return new Dimension( maxDim, ( maxDim * src.height ) / src.width );
		}
		return new Dimension( ( maxDim * src.width ) / src.height, maxDim );
	}
	
	public static float safeParseFloat( String s, float def ) {
		try {
			return Float.parseFloat( s );
		}
		catch( Exception e ) {
			return def;
		}
	}
	public static int safeParseInt( String s, int def ) {
		try {
			return Integer.parseInt( s );
		}
		catch( Exception e ) {
			return def;
		}
	}
	public static boolean safeParseBool( String s, boolean def ) {
		s = s.toLowerCase().trim();
		if( "true".equals( s ) || "1".equals( s ) || "yes".equals( s ) ) {
			return true;
		}
		if( "false".equals( s ) || "0".equals( s ) || "no".equals( s ) ) {
			return false;
		}
		return def;
	}
	public static String md5sum( String input ) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		digest.update( input.getBytes( "UTF-8" ) );
		byte[] md5sum = digest.digest();
		BigInteger bigInt = new BigInteger(1, md5sum);
		return bigInt.toString(16);
	}

	public static void safeClose(InputStream is) {
		if( is != null ) {
			try { is.close(); } catch( Exception e ) {}
		}
		
	}
	public static void safeClose(OutputStream is) {
		if( is != null ) {
			try { is.close(); } catch( Exception e ) {}
		}
		
	}

	public static byte[] intArrayToByteArray( int intArray[] ) {
		int numBytes = intArray.length * 4;
		byte bytes[] = new byte[numBytes];
		ByteBuffer buf = ByteBuffer.wrap( bytes );
		buf.asIntBuffer().put( intArray );
		buf = null;
		return bytes;
	}

	public static String readResource(String name) throws IOException {
		URL resource = PiSaver.class.getClassLoader().getResource(name);
		if( resource == null ) {
		    throw new RuntimeException( "Unable to find '" + name + "'" );
		}
		InputStream is = resource.openStream();
		Scanner s = new Scanner(is, "UTF-8");
		String rval = s.useDelimiter("\\A").next();
		s.close();
		//System.err.println( name + ":\n" + rval );
		return rval;
	}

	public static boolean isJpeg( File file ) {
		String lName = file.getName().toLowerCase();
		if( lName.endsWith( ".jpg" ) || lName.endsWith( ".jpeg" ) ) {
			return true;
		}
		return false;
	}
	
	public static boolean isImage( File file ) {
		if( !isJpeg( file ) ) {
			return false;
		}
		if( file.isDirectory() ) {
			return false;
		}
		if( !file.canRead() ) {
			return false;
		}
		return true;
	}
}
