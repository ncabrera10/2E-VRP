package core;

import util.Counter;

/**
 * Implements a stoping criterion that causes the execution of an algorithmt o stop whenever a maximum number
 * of iterations is reached. Client classes must make sure that the current number of iterations is updated by
 * regulary calling {@link #uptade()}.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public class MaxIterations implements StoppingCriterion {
	/*
	 * Maximum number of iterations. This is the stoping criterion.
	 */
	private final int maxIterations;
	/* A reference to the counter keeping track of the number of iterations */
	private final Counter itCounter;
	/**
	 * Construcs a new object of the class.
	 * 
	 * @param maxIterations the maximum number of iterations
	 * @param itCounter a reference to the counter keeping track of the number of iterations
	 */
	public MaxIterations(Counter itCounter,int maxIterations){
		this.maxIterations=maxIterations;
		this.itCounter=itCounter;
	}
	/**
	 * @return true when the current number of iterations is greater or equal than the <code>maxIterations</code> passed as parameter
	 * to the constructor
	 */
	@Override
	public boolean stop() {
		return itCounter.getCount()>=maxIterations;
	}

}
