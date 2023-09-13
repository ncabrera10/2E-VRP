package core;


/**
 * Defines the interface for algorithms in the jVRP framework.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 * @version Aug 20, 2016
 *
 */
public interface Algorithm {
	/**
	 * Runs the algorithm and returns a solution
	 * @return a solution to the vehicle routing problem in hand
	 */
	public Solution run();
	
}
