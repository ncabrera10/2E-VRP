package util;

/**
 * Implements a simple counter.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public class Counter {
	/* Holds the counter */
	private int c=0;
	/**
	 * Increments the counter
	 * @return the new count
	 */
	public int increment(){
		return this.c++;
	}
	/**
	 * Decrements the counter
	 * @return the new count
	 */
	public int decrement(){
		return this.c--;
	}
	/**
	 * 
	 * @return the current count
	 */
	public int getCount(){
		return this.c;
	}
	
}
