package net.marcuswatkins.pisaver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.marcuswatkins.pisaver.util.Util;



public class CachingImagePreparer<T extends CacheablePreparedImage<T>> implements ImagePreparer<T> {

	
	//TODO: Make this clean up the cache folder occasionally
	
	public ImagePreparer<T> realPrep;
	File cacheDir;
	
	int lastTime = 0;
	private CacheablePreparedImage<T> reader;
	
	public CachingImagePreparer( File cacheDir, ImagePreparer<T> prep, T reader ) {
		realPrep = prep;
		this.cacheDir = cacheDir;
		this.reader = reader;
	}
	
	@Override
	public int getLastPrepareTime() {
		return lastTime;
	}

	@Override
	public T prepareImage(File f) throws Exception {
		long start = System.currentTimeMillis();
		String cacheFileName = Util.md5sum( f.getCanonicalPath() ) + ".cache";
		File cacheFile = new File( cacheDir.getCanonicalPath() + "/" + cacheFileName ); 
		FileInputStream is = null;;
		try {
			try {
				if( cacheFile.exists() ) {
					is = new FileInputStream( cacheFile );
					T result = (T)reader.readCached( is ); //There's probably a trick to making this generic work, but I gave up
					System.err.println( "Cache hit!" );
					return result;
				}
			}
			catch( Exception e ) {
				System.err.println( "Error reading cacheFile: " );
				e.printStackTrace();
			}
			finally {
				Util.safeClose( is );
			}
			T result = realPrep.prepareImage( f );
			if( result.isCacheable() ) {
				FileOutputStream os = null;
				boolean written = false;
				try {
					os = new FileOutputStream( cacheFile );
					result.writeCache( os );
					os.close();
					written = true;
				}
				catch( Exception e ) {
					System.err.println( "Error storing cache: " );
					e.printStackTrace();
				}
				finally {
					Util.safeClose( os );
					if( !written ) {
						try { cacheFile.delete(); } catch( Exception e ) { }
					}
				}
			}
			return result;
		}
		finally {
			lastTime = (int)(System.currentTimeMillis() - start );
		}
	}

}
