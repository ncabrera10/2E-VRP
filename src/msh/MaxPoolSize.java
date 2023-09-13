package msh;

import core.RoutePool;
import core.StoppingCriterion;
/**
 * Implements a stoping criterion that is proper to multispace sampling heuristics. According to this criterion, the 
 * algorithm's execution ends whenever the size of the route pools is equal or greated than a pre-fixed threshold.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public class MaxPoolSize implements StoppingCriterion {
	/* a reference to the route pool */
	private final RoutePool pool;
	/* the maximum size of the route pool */
	private final int maxPoolSize;
	/**
	 * Constructor
	 * @param pool a reference to the route pool
	 * @param maxPoolSize the maximum size of the pool
	 */
	public MaxPoolSize(RoutePool pool,int maxPoolSize){
		this.pool=pool;
		this.maxPoolSize=maxPoolSize;
	}
	
	@Override
	public boolean stop() {
		return pool.size()>=maxPoolSize;
	}

}
