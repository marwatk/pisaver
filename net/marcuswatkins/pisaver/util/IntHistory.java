package net.marcuswatkins.pisaver.util;

public class IntHistory {

	private int values[];
	private int idx;
	private int count;
	private int toKeep;
	
	public IntHistory( int historyToKeep ) { 
		this.toKeep = historyToKeep;
		this.values = new int[historyToKeep];
	}
	
	public void add( int value ) {
		values[idx++] = value;
		if( idx >= values.length ) {
			idx = 0;
		}
		if( count < toKeep ) {
			count++;
		}
	}
	public int getMax() {
		int max = Integer.MIN_VALUE;
		for( int i = 0; i < count; i++ ) {
			if( values[i] > max ) {
				max = values[i];
			}
		}
		return max;
	}
	
	public int getAverage() {
		int tot = 0;
		for( int i = 0; i < count; i++ ) {
			tot += values[i];
		}
		return tot / count;
	}
}
