package core;

/**
 * Defines the interface for stopping criteria. A stopping criterion is a condition that causes the execution of a VRP
 * algorithm (or any algorithm in general) to stop its execution. Typical stopping criteria in vehicle routing algorithms
 * are the number of iterations or the running time.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public interface StoppingCriterion {
	
	/**
	 * @return true if the stoping criteiron is met, false otherwise
	 */
	public boolean stop();
	
}
