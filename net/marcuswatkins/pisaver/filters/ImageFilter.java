package net.marcuswatkins.pisaver.filters;
import java.io.File;

/**
 * 
 */

public interface ImageFilter {
	public boolean passesFilter( File image );
}