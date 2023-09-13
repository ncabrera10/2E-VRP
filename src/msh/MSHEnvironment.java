package msh;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import core.RoutePool;
import core.Solution;
import core.StoppingCriterion;
import core.MaxCPU;
import core.MaxIterations;
import core.OptimizationSense;
import util.Counter;
import util.StopWatch;

public class MSHEnvironment {
	/**
	 * The optimization criterion 
	 */
	private static OptimizationSense criterion=OptimizationSense.MINIMIZATION;
	/**
	 * The global timer
	 */
	private static StopWatch sampplingTimer=new StopWatch();
	private static StopWatch assemblyTimer=new StopWatch();
	private static Counter samples=new Counter();
	private static Solution bound;
	private static RoutePool pool;
	private static ArrayList<StoppingCriterion> stoppingCriteria=null;
	
	public static void init(Properties parameters){
		/* Set optimization criterion */
		setOptimizationCriterion(OptimizationSense.valueOf(parameters.getProperty("OPTIMIZATION_CRITERION")));
		/* Set stoping criteria */
		if(parameters.contains("MAX_ITERATIONS"))
			addStoppingCriterion(new MaxIterations(samples,Integer.valueOf(parameters.getProperty("MAX_ITERATION"))));
		if(parameters.contains("MAX_CPU")&&parameters.contains("TIME_UNIT"));
			addStoppingCriterion(new MaxCPU(sampplingTimer,Integer.valueOf(parameters.getProperty("MAX_ITERATION")),TimeUnit.valueOf(parameters.getProperty("TIME_UNIT"))));
		if(parameters.contains("MAX_POOL_SIZE"))
			addStoppingCriterion(new MaxPoolSize(pool,Integer.valueOf(parameters.getProperty("MAX_POOL_SIZE"))));
			
	}
	
	public static void setOptimizationCriterion(OptimizationSense criterion){
		MSHEnvironment.criterion=criterion;
	}
	
	public static void addStoppingCriterion(StoppingCriterion criterion){
		if(stoppingCriteria==null)
			stoppingCriteria=new ArrayList<>();
		stoppingCriteria.add(criterion);
	}
	
	/**
	 * Checks one by one the defined optimization criteria.
	 * 
	 * @return true if the execution of the MSH heuristic is to stop and false otherwise
	 * @throws {@link IllegalStateException} if there there is not at least one stopping criterion.
	 */
	public static boolean stop(){
		if(stoppingCriteria==null)
			throw new IllegalStateException("There are no stoping criteria defined");
		for(StoppingCriterion sc:stoppingCriteria)
			if(sc.stop())
				return true;
		return false;
	}
	
	public static RoutePool routePool(){
		return pool;
	}
	
	
}
