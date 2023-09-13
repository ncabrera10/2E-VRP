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

/**
 * Implements a basic split procedure. When this split algorithm is used, all the routes in the split graph
 * are added to the pool. 
 * 
 */
public class SecondEchelonSplitAll implements Split{

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
	public SecondEchelonSplitAll(DistanceMatrix distances, ArrayList<Double> demands,double capacity) {
		this.distances = distances;
		this.demands = demands;
		this.Q = capacity;
	}
	
	@Override
	public Solution split(TSPSolution r){

		//Initialize labels
		int[] P=new int[r.size()-1];			//The predecesor labels
		double[] V=new double[r.size()-1];		//The shortest path labels
		for(int i=1;i<r.size()-1;i++){
			V[i]=Double.MAX_VALUE;
		}
		

		VRPSolution s=new VRPSolution();
		double of = 0.0;
		
		//Build the auxiliary graph and find the shortest path at the same time
		for(int i=1; i<r.size(); i++){
			
			//Initilize auxiliary variables
			double load=0;
			double cost=0;	
			int j=i;
			
			//Explore all routes starting at node i
			while(load<=Q&&j<r.size()-1){
				//Compute metrics for the route: load and cost
				load+=demands.get(r.get(j)-1);
				if(i==j)
					cost=distances.getDistance(0, r.get(j))+distances.getDistance(r.get(j),0);
				else
					cost=cost-distances.getDistance(r.get(j-1),0)+distances.getDistance(r.get(j-1),r.get(j))+distances.getDistance(r.get(j),0);
				//Check the route's feasibility
				if(load<=Q){
					
					// Adds the route to the pool:
					Route route = JVRAEnv.getRouteFactory().buildRoute();
					route.add(r.get(0));
					for(int k=i; k<=j; k++) {
						route.add(r.get(k));
					}
					route.add(r.get(0));
					route.setAttribute(RouteAttribute.COST,cost);
					route.setAttribute(RouteAttribute.LOAD,load);
					s.addRoute(route);
					of+=cost;
					
					if(V[i-1]+cost<V[j]){
						V[j]=V[i-1]+cost;
						P[j]=i-1;
					}
					j++;
				}					
			}
		}

		s.setOF(of);
		return s;
	}
	

}
