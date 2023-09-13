package msh;

import core.JVRAEnv;
import core.OptimizationSense;
import core.OrderFirstSplitSecondHeuristic;
import core.Route;
import core.RoutePool;
import core.Solution;
import core.VRPSolution;
import globalParameters.GlobalParameters;

/**
 * This class implements a sampling function. Each sampling function has its own pool of routes and is associated with a TSP heuristic.
 * 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class OrderFirstSplitSecondSampling implements SamplingFunction{

	private final OrderFirstSplitSecondHeuristic h;
	private int nSamples=1;	//Default value = 1
	private int samplesDrawn=0;
	private RoutePool pool=null;
	private OptimizationSense sense=JVRAEnv.getOptimizationSense();
	
	public OrderFirstSplitSecondSampling(OrderFirstSplitSecondHeuristic h,int nSamples){
		this.h=h;
		this.nSamples=nSamples;
	}

	@Override
	public Solution call() throws Exception {
		if(pool==null)
			throw new IllegalStateException("The route pool has not been set up");
		//Set up variables
		VRPSolution s, best=null;
		int i;
		//Sampling
		Double IniTime = (double) System.nanoTime();
		boolean stop = false;
		int routes = 0;
		for(i=1;i<=this.nSamples && !stop;i++){
			s=(VRPSolution)h.run();
			routes+=s.getRoutes().size();
			for(Route r:s.getRoutes()){
				pool.add(r);
			}
			this.samplesDrawn++;
			//Update the best bound
			if(best==null)
				best=s;
			else if(this.sense==OptimizationSense.MINIMIZATION&&s.getOF()<best.getOF()
					||this.sense==OptimizationSense.MAXIMIZATION&&s.getOF()>best.getOF())
				best=s;
			
			//Check stopping conditions: This is something I added to allow for a time limit on the sampling phase and to control the pool size.
			
			if((System.nanoTime()-IniTime)/1000000000 > GlobalParameters.MSH_SAMPLING_TIME_LIMIT) {
				stop = true;
			}
			if(routes > GlobalParameters.MSH_MAX_POOL_SIZE) {
				stop = true;
			}
		}
		
		return best;
	}

	@Override
	public void setNumberOfSamples(int samples) {
		this.nSamples=samples;		
	}

	@Override
	public int getNuberOfDrawnSamples() {
		return this.samplesDrawn;
	}

	@Override
	public void setRoutePool(RoutePool pool) {
		this.pool=pool;
	}

}
