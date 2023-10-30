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
import lkh.TspSolver;

/**
 * Implements the enumeration procedure for the FE routes
 * 
 */
public class FirstEchelonEnumerationMTZ implements Split{

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
	public FirstEchelonEnumerationMTZ(DistanceMatrix distances) {
		this.distances = distances;
	}
	
	@Override
	public Solution split(TSPSolution tsp){

		// Initializes an empty solution:
		
			VRPSolution s=new VRPSolution();
			double of=0;

		// Initializes an array with the current satellites:
			
			int numSatellites = tsp.size()-2;
			int arr[] = new int[numSatellites];
			for (int i=1;i<numSatellites+1;i++) {
				arr[i-1] = i;
			}
		
		// Builds a list with the possible subsets of satellites:
			
			ArrayList<ArrayList<Integer>> listComplete = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> partialList = new ArrayList<Integer>();
	
			int n = arr.length;
			int N = (int) Math.pow(2d, Double.valueOf(n));  
			for (int i = 1; i < N; i++) {
			    String code = Integer.toBinaryString(N | i).substring(1);
			    partialList = new ArrayList<Integer>();
			    for (int j = 0; j < n; j++) {
			        if (code.charAt(j) == '1') {
			            partialList.add(arr[j]);
			            
			        }
			    }
			    listComplete.add(partialList);
			}
		
		// Iterates over the subsets of satellites:
			
			for(ArrayList<Integer> currentList : listComplete) {
				
				// We check if the list contains less than 11 satellites:
				
				if(currentList.size() <= 10) {
					
					// We add the depot to the list:
					
						currentList.add(0);
	
					// We create a tsp solver object that will try to improve the current tsp:
						
						TspSolver lkh = new TspSolver(distances,currentList);
					
					// If there's more than 2 satellites:
						
						if(currentList.size()>3) {
							lkh.improveTour();
						}
					
					// We store the cost of the tsp:
						
						double newCost = lkh.getDistance();
					
					//Initialize a new route
					
						Route r=JVRAEnv.getRouteFactory().buildRoute();
						r.add(tsp.get(0));
						double load=0;
						int satellite_pos = lkh.getIndex(tsp.get(0));
						for(int i=satellite_pos+1; i<lkh.tour.length;i++){ //Build the route
							int node = lkh.tour[i];
							r.add(node);
						}
						for(int i=0; i<satellite_pos;i++){ //Build the route
							int node = lkh.tour[i];
							r.add(node);
						}
						r.add(tsp.get(0));
						
					// Store the route attributes:
						
						r.setAttribute(RouteAttribute.COST,newCost);
						r.setAttribute(RouteAttribute.LOAD, load);
					
					// Update the OF:
						
						of+=newCost;
						
					// Add the route to the pool:
					
						s.addRoute(r);
					
					
				}
			}
		
		// Sets the OF:
			
			s.setOF(of);
			
		// Returns the solution: Pool of FE routes
			
			return s;
	}
	
}
