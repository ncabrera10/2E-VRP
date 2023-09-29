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
 * with the LKH before being added to the pool. Also, the split is solved considering different capacities. Q and Q/2.
 * 
 */
public class SecondEchelonSplitCapacities implements Split{

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * The vehicle's capacity
	 */
	private final double Q;
	
	/**
	 * Minimum vehicle's capacity
	 */
	private final double minQ;
	
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
	public SecondEchelonSplitCapacities(DistanceMatrix distances, ArrayList<Double> demands,double capacity,double minCap) {
		this.distances = distances;
		this.demands = demands;
		this.Q = capacity;
		this.minQ = minCap;
	}
	
	@Override
	public Solution split(TSPSolution tsp){

		VRPSolution s=new VRPSolution();
		double of = 0.0;
		
		for(int q = 1 ; q <= 2; q++) { //Try with different capacities
			
			double cap = Q/q;
			if(cap < minQ) {
				cap = minQ;
			}
			
			//Initialize labels
			int[] P=new int[tsp.size()-1];			//The predecesor labels
			double[] V=new double[tsp.size()-1];		//The shortest path labels
			for(int i=1;i<tsp.size()-1;i++){
				V[i]=Double.MAX_VALUE;
			}
			
			//Build the auxiliary graph and find the shortest path at the same time
			for(int i=1; i<tsp.size(); i++){
				
				//Initilize auxiliary variables
				double load=0;
				double cost=0;	
				int j=i;
				
				//Explore all routes starting at node i
				while(load<=cap&&j<tsp.size()-1){
					//Compute metrics for the route: load and cost
					load+=demands.get(tsp.get(j)-1);
					if(i==j)
						cost=distances.getDistance(0, tsp.get(j))+distances.getDistance(tsp.get(j),0);
					else
						cost=cost-distances.getDistance(tsp.get(j-1),0)+distances.getDistance(tsp.get(j-1),tsp.get(j))+distances.getDistance(tsp.get(j),0);
					//Check the route's feasibility
					if(load<=cap){
						if(V[i-1]+cost<V[j]){
							V[j]=V[i-1]+cost;
							P[j]=i-1;
						}
						j++;
					}					
				}
			}
			
			int head=P.length-1; //The head of the arc representing the last route
			int  nodesToRoute=P.length-1;
			while (nodesToRoute>0){
				int tail=P[head]+1; //The tail of the arc representing the route being currently built\
				
				//Try to improve the current route with the LKH:

				// Create an arraylist with the nodes in the route:
			
					ArrayList<Integer> tsp_array = new ArrayList<Integer>();
					tsp_array.add(tsp.get(0));
					for(int i=tail; i<=head;i++){
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
						nodesToRoute--;
					}
					for(int i=0; i<satellite_pos;i++){ //Build the route
						int node = lkh.tour[i];
						r.add(node);
						load += demands.get(node-1);
						nodesToRoute--;
					}
					r.add(tsp.get(0));
					double cost=newCost;
					r.setAttribute(RouteAttribute.COST,cost);
					of+=cost;
					r.setAttribute(RouteAttribute.LOAD, load);
					s.addRoute(r);
					head=P[head]; //The head of the arc representing the next route to build
				}
				else { //Otherwise we just keep the previous one
					//Initialize a new route
					Route r=JVRAEnv.getRouteFactory().buildRoute();
					r.add(tsp.get(0));
					double load=0;
					for(int i=tail; i<=head;i++){ //Build the route
						int node = tsp.get(i);
						r.add(node);
						load += demands.get(node-1);
						nodesToRoute--;
					}
					r.add(tsp.get(0));
					double cost=V[head]-V[P[head]];
					r.setAttribute(RouteAttribute.COST,cost);
					of+=cost;
					r.setAttribute(RouteAttribute.LOAD, load);
					s.addRoute(r);
					head=P[head]; //The head of the arc representing the next route to build
				}
			}
		}


		s.setOF(of);
		return s;
	}
	
}
