package split;

import core.DistanceMatrix;
import core.JVRAEnv;
import core.Route;
import core.RouteAttribute;
import core.Solution;
import core.Split;
import core.TSPSolution;
import core.VRPSolution;

/**
 * Implements a basic split procedure. 
 * 
 */
public class FirstEchelonSplit implements Split{

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * This method creates a new instance of the HpMP split algorithm
	 * @param distances
	 * @param m
	 * @param M
	 */
	public FirstEchelonSplit(DistanceMatrix distances) {
		this.distances = distances;
	}
	
	@Override
	public Solution split(TSPSolution r){

		//Initialize labels
		int[] P=new int[r.size()-1];			//The predecesor labels
		double[] V=new double[r.size()-1];		//The shortest path labels
		for(int i=1;i<r.size()-1;i++){
			V[i]=Double.MAX_VALUE;
		}
		
		//Build the auxiliary graph and find the shortest path at the same time
		for(int i=1; i<r.size(); i++){
			
			//Initilize auxiliary variables
			double cost=0;	
			int j=i;
			
			//Explore all routes starting at node i
			while(j<r.size()-1){
				//Compute metrics for the route: load and cost
				if(i==j)
					cost=distances.getDistance(0, r.get(j))+distances.getDistance(r.get(j),0);
				else
					cost=cost-distances.getDistance(r.get(j-1),0)+distances.getDistance(r.get(j-1),r.get(j))+distances.getDistance(r.get(j),0);
				//Check the route's feasibility
					
				if(V[i-1]+cost<V[j]){
					V[j]=V[i-1]+cost;
					P[j]=i-1;
				}
				j++;
									
			}
		}

		return extractRoutes(P,V, r);
	}
	/**
	 * Extracts the routes from the labels, builds a solution, and evaluates the solution
	 * @param P the predecessors
	 * @param V the shortest path labels
	 * @param tsp the TSP tour
	 * @return a solution with the routes in the optimal partition of the TSP tour
	 */
	private VRPSolution extractRoutes(int[] P, double[] V, TSPSolution tsp){
		//EXO 2: try to write this algorithm
		//Initialize the solution
		VRPSolution s=new VRPSolution();
		double of=0;
		int head=P.length-1; //The head of the arc representing the last route
		int  nodesToRoute=P.length-1;
		while (nodesToRoute>0){
			int tail=P[head]+1; //The tail of the arc representing the route being currently built
			//Initialize a new route
			Route r=JVRAEnv.getRouteFactory().buildRoute();
			r.add(tsp.get(0));
			double load=0;
			for(int i=tail; i<=head;i++){ //Build the route
				int node=tsp.get(i);
				r.add(node);
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
		s.setOF(of);
		return s;
	}

}
