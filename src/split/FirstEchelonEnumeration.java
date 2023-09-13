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
 * Implements a basic split procedure. 
 * 
 */
public class FirstEchelonEnumeration implements Split{

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
	public FirstEchelonEnumeration(DistanceMatrix distances) {
		this.distances = distances;
	}
	
	@Override
	public Solution split(TSPSolution tsp){

		VRPSolution s=new VRPSolution();
		double of=0;

		int numSatellites = tsp.size()-2;
		int arr[] = new int[numSatellites];
		for (int i=1;i<numSatellites+1;i++) {
			arr[i-1] = i;
		}
		
		ArrayList<ArrayList<Integer>> listComplete = new ArrayList<ArrayList<Integer>>();
		
		int n = arr.length;
		int N = (int) Math.pow(2d, Double.valueOf(n));  
		for (int i = 1; i < N; i++) {
		    String code = Integer.toBinaryString(N | i).substring(1);
		    ArrayList<Integer> partialList = new ArrayList<Integer>();
		    for (int j = 0; j < n; j++) {
		        if (code.charAt(j) == '1') {
		            partialList.add(arr[j]);
		            
		        }
		    }
		    listComplete.add(partialList);
		}
		
		for(ArrayList<Integer> currentList : listComplete) {
			
			currentList.add(0);
			
			LKH lkh = new LKH(distances,currentList);

			if(currentList.size()>2) {
				lkh.runAlgorithm();
			}
			
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
			double cost=newCost;
			r.setAttribute(RouteAttribute.COST,cost);
			of+=cost;
			r.setAttribute(RouteAttribute.LOAD, load);
			s.addRoute(r);
		}
		
		s.setOF(of);
		return s;
	}
	
}
