package net.marcuswatkins.pisaver.filters;
import net.marcuswatkins.pisaver.sources.SourceImage;

/**
 * 
 */

public interface ImageFilter {
	public boolean passesFilter( SourceImage image );
}