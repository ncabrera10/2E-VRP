package core;

import java.util.concurrent.TimeUnit;

import util.StopWatch;

/**
 * Implements a maximum CPU time stopping criterion. This criterion stops the execution of an algorithm when a maximum execution
 * time is reached.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public class MaxCPU implements StoppingCriterion{
	
	/* Holds a reference to the timer usted to track execution time */
	private final StopWatch timer;
	/* The maximum CPU time */
	private long  maxCPU;
	
	/**
	 * Constructor
	 * @param timer a reference to the stop watch used to track the execution time
	 * @param maxCPU the maximum CPU time
	 * @param timeUnit the time unit in which <code>maxCPU</code> is given
	 */
	public MaxCPU(StopWatch timer, long maxCPU, TimeUnit timeUnit){
		this.maxCPU=TimeUnit.NANOSECONDS.convert(maxCPU, timeUnit);
		this.timer=timer;
	}
	/**
	 * Constructor.</br>
	 * If the object is built using this constructor, the stop watch used to track the execution time is a new 
	 * instance of {@link StopWatch} constructed using the {@link StopWatch#createStarted()} factory method.
	 * 
	 * @param maxCPU the maximum CPU time
	 * @param timeUnit the time unit in which <code>maxCPU</code> is given
	 */
	public MaxCPU(long maxCPU, TimeUnit timeUnit){
		this(StopWatch.createStarted(),maxCPU,timeUnit);
	}
	
	@Override
	public boolean stop() {
		return timer.getNanoTime()>this.maxCPU;
	}

}
