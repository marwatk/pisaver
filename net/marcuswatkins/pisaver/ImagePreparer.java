package net.marcuswatkins.pisaver;
import java.io.File;

/**
 * 
 */

public interface ImagePreparer<K> {
	public K prepareImage( File f ) throws Exception;
	public int getLastPrepareTime();
}