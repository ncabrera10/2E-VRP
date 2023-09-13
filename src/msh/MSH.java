package msh;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import core.Algorithm;
import core.JVRAEnv;
import core.OptimizationSense;
import core.RoutePool;
import core.Solution;

/**
 * Implements the multispace sampling heuristic
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 16, 2016
 *
 */
public class MSH implements Algorithm{
	/**
	 * The set of sampling functions
	 */
	private List<SamplingFunction> samplingFunctions;
	/**
	 * The assembly function
	 */
	private AssemblyFunction assemblyFunction;
	/**
	 * The optimization sense
	 */
	private final OptimizationSense sense;
	/**
	 * The pool where sampled routes are stored
	 */
	private ArrayList<RoutePool> pools;
	
	/**
	 * Bound on the final solution
	 */
	private Solution bound=null;
	
	/**
	 * Total number of iterations of MSH actually carried out
	 */
	private int numberOfIterations;
	
	/**
	 * The number of threads running the sampling phase
	 */
	private final int nSamplingThreads;
	/**
	 * Constructs a new multi-space sampling heuristic
	 * @param samplingFunction a sampling function
	 * @param assemblyFuncion the assembly function
	 * @param pool the route pool
	 * @param nSamplingThreads the number of threads running the sampling phase
	 */
	public MSH(AssemblyFunction assemblyFuncion, int nSamplingThreads){
		this.samplingFunctions=new ArrayList<>();
		this.assemblyFunction=assemblyFuncion;
		this.sense=JVRAEnv.getOptimizationSense();
		this.nSamplingThreads=nSamplingThreads;
	}
	/**
	 * Adds an additional sampling function
	 * @param function the sampling function to add
	 */
	public void addSamplingFunction(SamplingFunction function){
		this.samplingFunctions.add(function);
	}
	
	/**
	 * Sets the pools
	 * @param pools
	 */
	public void setPools(ArrayList<RoutePool> pools) {
		this.pools=pools;
	}

	@Override
	public Solution run() {
		//Set up an executor to run the sampling phase
		final int threads=Math.min(nSamplingThreads, samplingFunctions.size());
		ExecutorService se=Executors.newFixedThreadPool(threads);
		//Set up futures
		List<Future<Solution>> bounds=new ArrayList<>();
		try {
			bounds=se.invokeAll(samplingFunctions);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Get the best bound
		Solution bound=null;
		
		// Total number of iterations:
		
		for(SamplingFunction sampling : samplingFunctions) {
			numberOfIterations += sampling.getNuberOfDrawnSamples();
		}
				
		for(Future<Solution> f:bounds){
			if(f!=null){
				try {
					if(bound==null) {
							bound=f.get();
					}
					else if(sense==OptimizationSense.MINIMIZATION&&f.get().getOF()<bound.getOF()
							||sense==OptimizationSense.MAXIMIZATION&&f.get().getOF()>bound.getOF()) {
							bound=f.get();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
		//Assemble the final solution	
		
		return assemblyFunction.assembleSolution(bound,pools);
	}
	
	/**
	 * This method runs the sampling phase. It does not assembly the final solution.
	 */
	public void run_sampling() {
		//Set up an executor to run the sampling phase
		final int threads=Math.min(nSamplingThreads, samplingFunctions.size());
		
		ExecutorService se=Executors.newFixedThreadPool(threads);
		//Set up futures
		List<Future<Solution>> bounds=new ArrayList<>();
		try {
			bounds=se.invokeAll(samplingFunctions);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Total number of iterations:
		
		for(SamplingFunction sampling : samplingFunctions) {
			numberOfIterations += sampling.getNuberOfDrawnSamples();
		}
		
		//Get the best bound

		for(Future<Solution> f:bounds){
			if(f!=null){
				try {
					if(bound==null) {
							bound=f.get();
					}
					else if(sense==OptimizationSense.MINIMIZATION&&f.get().getOF()<bound.getOF()
							||sense==OptimizationSense.MAXIMIZATION&&f.get().getOF()>bound.getOF()) {
							bound=f.get();
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * This method runs the assembly phase. It calls the CPLEX set partitioning solver.
	 * @return
	 */
	public Solution run_assembly() {

		//Assemble the final solution	
		
		return assemblyFunction.assembleSolution(bound,pools);
	}
	
	/**
	 * This method returns the current size of the pool, summing the size of the individual pools.
	 * @return
	 */
	public int getPoolSize() {
		int size = 0;
		for(RoutePool pool : pools) {
			size += pool.size();
		}
		return size;
	}
	/**
	 * @return the numberOfIterations
	 */
	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	/**
	 * @param numberOfIterations the numberOfIterations to set
	 */
	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}
	
	
}
