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
		
		VRPSolution s=new VRPSolution();
		double of = 0.0;
		
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

				// Adds the route to the pool:
				Route route = JVRAEnv.getRouteFactory().buildRoute();
				route.add(r.get(0));
				for(int k=i; k<=j; k++) {
					route.add(r.get(k));
				}
				route.add(r.get(0));
				route.setAttribute(RouteAttribute.COST,cost);
				route.setAttribute(RouteAttribute.LOAD,0);
				s.addRoute(route);
				of+=cost;
				
				if(V[i-1]+cost<V[j]){
					V[j]=V[i-1]+cost;
					P[j]=i-1;
				}
				j++;
									
			}
		}

		s.setOF(of);
		return s;
	}
	
	

}
