package net.marcuswatkins.pisaver;

/**
 * 
 */

public interface AnimFinishedListener<R,T> {
	void animationFinished( SaverImage<R,T> img );
}