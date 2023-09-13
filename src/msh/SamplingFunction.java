package msh;

import java.util.concurrent.Callable;

import core.RoutePool;
import core.Solution;

public interface SamplingFunction extends Callable<Solution>{

	public void setRoutePool(RoutePool pool);
	
	public void setNumberOfSamples(int samples);
	
	public int getNuberOfDrawnSamples();
	
}
