package net.marcuswatkins.pisaver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface CacheablePreparedImage<T> {

	public boolean isCacheable();
	public CacheablePreparedImage<T> readCached( InputStream is ) throws IOException;
	public void writeCache( OutputStream os ) throws IOException;
}
