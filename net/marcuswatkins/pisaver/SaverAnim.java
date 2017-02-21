package net.marcuswatkins.pisaver;

/**
 * 
 */

public class SaverAnim {
	private long start = 0;
	
	private float[] scale;
	private float[] rotation;
	private float[] alpha;
	private int duration;

	private AnimFinishedListener listener;
	
	public SaverAnim( float scale[], float rotation[], float alpha[], int duration, AnimFinishedListener listener ) {
		this.scale = scale;
		this.rotation = rotation;
		this.alpha = alpha;
		this.duration = duration;
		this.listener = listener;
	}
	
	boolean animate( SaverImage<?,?> img, NativeImage<?,?> nativeImage ) {
		boolean animationFinished = false;
		long now = System.currentTimeMillis();
		if( start == 0 ) {
			start = now; 
		}
		int elapsed = (int)(now - start);
		if( elapsed >= duration ) {
			animationFinished = true;
			if( listener != null ) {
				listener.animationFinished( img );
			}
		}
		nativeImage.setAlpha( interpolate( alpha, elapsed, Saver.ALPHA_DURATION ) );
		nativeImage.setScale( interpolate( scale, elapsed, duration ) );
		nativeImage.setRotation( interpolate( rotation, elapsed, duration ) );
		return animationFinished;
	}
	private static float interpolate( float array[], int elapsed, int duration ) {
		int intervals = array.length - 1;
		if( elapsed >= duration ) {
			return array[intervals];
		}
		if( intervals == 0 || elapsed == 0 ) {
			return array[0];
		}
		float percentComplete = (float)elapsed / (float)duration;
		int interval = (int)( intervals * percentComplete );
		float intervalValue = array[interval+1] - array[interval];
		//Original:
		//float intervalComplete = ( percentComplete - ( ( 1.0f / (float)intervals ) * interval ) ) / ( 1.0f / (float)intervals );
		//Optimized:
		float intervalComplete = ((float)intervals * percentComplete) - (float)interval;
		float partialDiff = intervalValue * intervalComplete;
		float rval = array[interval] + partialDiff;
		return rval;
	}
	
}