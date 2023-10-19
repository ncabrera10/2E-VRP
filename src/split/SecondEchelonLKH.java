package split;

import java.util.ArrayList;

import core.DistanceMatrix;
import core.JVRAEnv;
import core.Route;
import core.RouteAttribute;
import core.Solution;
import core.Split;
import core.TSPSolution;
import core.VRPSolution;
import lkh.LKH;

/**
 * Implements a basic split procedure. When this split algorithm is used, routes are improved
 * with the LKH before being added to the pool.
 * 
 */
public class SecondEchelonLKH implements Split{

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * The vehicle's capacity
	 */
	private final double Q;
	
	/**
	 * The customer demands
	 */
	private final ArrayList<Double> demands;
	
	
	/**
	 * This method creates a new instance of the HpMP split algorithm
	 * @param distances
	 * @param m
	 * @param M
	 */
	public SecondEchelonLKH(DistanceMatrix distances, ArrayList<Double> demands,double capacity) {
		this.distances = distances;
		this.demands = demands;
		this.Q = capacity;
	}
	
	@Override
	public Solution split(TSPSolution tsp){

		VRPSolution s=new VRPSolution();
		double of=0;
		
		// Create an arraylist with the nodes in the route:
		
		ArrayList<Integer> tsp_array = new ArrayList<Integer>();
		tsp_array.add(tsp.get(0));
		for(int i=1; i<=tsp.size()-2;i++){
			tsp_array.add(tsp.get(i));
		}
		
		
		// Create an LKH object:
			
			LKH lkh = new LKH(distances,tsp_array);
			
		// Initialze the total distance:
			
			double iniCost = lkh.getDistance();

		// Run the algorithm:
			
			lkh.runAlgorithm();
			
		// Get the new total distance:
			
			double newCost = lkh.getDistance();
			
		// If the distance is better, then we can use this tour instead:
			
		if(newCost < iniCost) {
			//Initialize a new route
			Route r=JVRAEnv.getRouteFactory().buildRoute();
			r.add(tsp.get(0));
			double load=0;
			int satellite_pos = lkh.getIndex(tsp.get(0));
			for(int i=satellite_pos+1; i<lkh.tour.length;i++){ //Build the route
				int node = lkh.tour[i];
				r.add(node);
				load += demands.get(node-1);
			}
			for(int i=0; i<satellite_pos;i++){ //Build the route
				int node = lkh.tour[i];
				r.add(node);
				load += demands.get(node-1);
			}
			r.add(tsp.get(0));
			double cost=newCost;
			r.setAttribute(RouteAttribute.COST,cost);
			of+=cost;
			r.setAttribute(RouteAttribute.LOAD, load);
			s.addRoute(r);
		}
	
		return s;
	}
}
