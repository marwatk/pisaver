package net.marcuswatkins.pisaver;
import net.marcuswatkins.pisaver.sources.SourceImage;

/**
 * 
 */

public interface ImagePreparer<K extends PreparedImage> {
	public K prepareImage( SourceImage f ) throws Exception;
	public int getLastPrepareTime();
}