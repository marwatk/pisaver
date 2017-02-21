package net.marcuswatkins.pisaver;
import java.awt.geom.Point2D;
import java.io.File;

import net.marcuswatkins.pisaver.sources.ImageSource;
import net.marcuswatkins.pisaver.sources.SourceImage;
import net.marcuswatkins.pisaver.util.IntHistory;


public class Saver<R,T extends PreparedImage,S extends NativeScreen<R,T>> implements AnimFinishedListener<R,T> {
	
	private static final boolean DEBUG = false;
	
	ImageSource<T> finder;
	File folders[];
	SaverImage<R,T> currentImage;
	NativeImage<R,T> background;
	S nativeScreen;
	long lastImageTime = 0;
	private Point2D.Float positions[];
	private int position = 0;
	private float scale = 1.0f;
	
	private IntHistory prepareTimeHistory = new IntHistory( 5 );
	
	private static final float SCALE_RANGE = 0.1f;
	
		private static final float[] ALPHA = new float[] { 0.0f, 1.0f };
	private static final float ROTATION_AMT = 0.05f;
	private static final float ROTATION_SHIFT_MAX = 0.03f;
	private static final int ANIMATION_DURATION = DEBUG ? 1000 : 8000;
	
	public static final float SPECIAL_SCALE = 1.2f;
	public static final int SPECIAL_DURATION = DEBUG ? 1000 : 30000;
	public static final int ALPHA_DURATION = Math.min( 2000, ANIMATION_DURATION / 4 );
	
	public Saver( ) {
	}
	
	public void init( ImageSource<T> source, S nativeScreen ) {
		System.err.println( "Saver.init()" );
		this.nativeScreen = nativeScreen;
		finder = source;
		screenChanged();
		
	}
	
	public void screenChanged() {
		float xs[];
		float ys[] = getPositions( 2, nativeScreen.getScreenTop(), nativeScreen.getScreenBottom() );
		float aspect = nativeScreen.getAspect();
		if( aspect > 1.5f ) { //widescreen
			xs = getPositions( 3, nativeScreen.getScreenLeft(), nativeScreen.getScreenRight() );
			positions = new Point2D.Float[] {
				new Point2D.Float( xs[0], ys[0] ),
				new Point2D.Float( xs[1], ys[1] ),
				new Point2D.Float( xs[2], ys[0] ),
				new Point2D.Float( xs[0], ys[1] ),
				new Point2D.Float( xs[1], ys[0] ),
				new Point2D.Float( xs[2], ys[1] ),
			};
			scale = 0.41f;
		}
		else { //Squarish screen
			xs = getPositions( 2, nativeScreen.getScreenLeft(), nativeScreen.getScreenRight() );
			positions = new Point2D.Float[] {
					new Point2D.Float( xs[0], ys[0] ),
					new Point2D.Float( xs[1], ys[1] ),
					new Point2D.Float( xs[1], ys[0] ),
					new Point2D.Float( xs[0], ys[1] ),
				};
			scale = 0.5f;
		}
	}
	
	private static float[] getPositions( int positionCount, float min, float max ) {
		float positions[] = new float[positionCount];
		float spacing = ( max - min ) / (float)positionCount;
		float first = min + ( spacing * 0.5f );
		positions[0] = first;
		for( int i = 1; i < positionCount; i++ ) {
			positions[i] = positions[i-1] + spacing;
		}
		return positions;
	}
	
	public void draw( R renderer ) {
		
		long now = System.currentTimeMillis();
		boolean animationDone = false;
		if( currentImage != null ) {
			animationDone = currentImage.animate();
		}
		if( background != null ) {
			background.draw( renderer );
		}
		
		if( currentImage != null ) {
			currentImage.draw( renderer );
		}
		
		if( animationDone ) {
			if( background != null ) {
				background.dispose( nativeScreen.getRenderer() );
			}
			background = nativeScreen.captureScreen();
			currentImage.cleanup( nativeScreen.getRenderer() );
			currentImage = null;
		}
		if( currentImage == null && finder.nextReady() ) {
			System.err.println( "Requesting next image" );
			try {
				T texture = finder.getNextImage();
				System.err.println( "Last prepare time: " + finder.getLastPrepareTime() );
				prepareTimeHistory.add( finder.getLastPrepareTime() );
				NativeImage<R,T> nativeImage = nativeScreen.buildImage( texture );
				SourceImage source = texture.getSource();
				boolean isSpecial = source != null && texture.getSource().isSpecial();
				Point2D.Float pos = getNextPosition( isSpecial );
				
				SaverImage<R,T> image = new SaverImage<R,T>( nativeImage, pos.x, pos.y, generateSingleAnim( isSpecial ? SPECIAL_SCALE : scale, isSpecial ? SPECIAL_DURATION : ANIMATION_DURATION ) );
				currentImage = image;
				lastImageTime = now;
				System.err.println( "Next image created" );
			}
			catch( Exception e ) {
				e.printStackTrace();
			}
		}
		else if( currentImage == null ) {
			//We're waiting on a new image to become ready, sleep to not waste cpu in this loop
			try { Thread.sleep( 500 ); } catch( Exception e ) { }
		}
	}

	private Point2D.Float getNextPosition( boolean isSpecial ) {
		if( isSpecial ) {
			return new Point2D.Float( 0.0f, 0.0f );
		}
		Point2D.Float pos = positions[position++];
		if( position >= positions.length ) {
			position = 0;
		}
		return pos;
	}
	private SaverAnim generateSingleAnim( float scale, int duration ) {
		float rotShift = (float)(Math.random() * ( ROTATION_SHIFT_MAX * 2 )) - ROTATION_SHIFT_MAX;
		float neg = Math.random() < 0.5 ? 1 : -1;
		return new SaverAnim( new float[] { scale - SCALE_RANGE, scale }, new float[] { neg * (-ROTATION_AMT + rotShift), neg * (ROTATION_AMT + rotShift) }, ALPHA, duration, null );
	}
	
	private SaverAnim[] generateAnim() {
		SaverAnim anim[] = new SaverAnim[2];
		float rotShift = (float)(Math.random() * ( ROTATION_SHIFT_MAX * 2 )) - ROTATION_SHIFT_MAX;
		float neg = Math.random() < 0.5 ? 1 : -1;
		anim[0] = new SaverAnim( new float[] { scale - SCALE_RANGE, scale }, new float[] { neg * (-ROTATION_AMT + rotShift), neg * (ROTATION_AMT + rotShift) }, ALPHA, ANIMATION_DURATION, null );
		anim[1] = new SaverAnim( new float[] { scale, 0.0f }, new float[] {neg * (ROTATION_AMT + rotShift) }, new float[] { 1.0f, 0.0f }, 2000, this );
		return anim;
	}
	
	
	public void dispose(R renderer) {
		
	}

	@Override
	public void animationFinished(SaverImage<R,T> img) {
		img.cleanup( nativeScreen.getRenderer() );
	}
	
}
