package net.marcuswatkins.pisaver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.marcuswatkins.pisaver.sources.SourceImage;


public interface CacheablePreparedImage<T extends PreparedImage> extends PreparedImage {

	public boolean isCacheable();
	public CacheablePreparedImage<T> readCached( InputStream is, SourceImage source ) throws IOException;
	public void writeCache( OutputStream os ) throws IOException;
}
