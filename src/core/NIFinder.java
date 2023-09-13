package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;


public class NIFinder extends InsertionFinder {

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * Constructs a new NI finder
	 * @param distances
	 * @param n
	 */
	public NIFinder(final DistanceMatrix distances) {
		this.distances = distances;
	}
	
	
	/**
	 * The initial search bound is Double.MAX_VALUE
	 */
	@Override
	protected double initializeBound() {
		return Double.MAX_VALUE;
	}
	
	/**
	 * If the distance to the node being considered is shorter than the current search bound
	 * the bound should be updated (we are looking for the nearest neighbor to an already routed node).
	 */
	@Override
	protected double updateBound(double distanceToNeighbor, double bound) {
		if(distanceToNeighbor<bound){
			return distanceToNeighbor;
		}
		return bound;
	}
	
	/**
	 * Sort the list of candidate nodes in non-decreasing order and select the kth candidate
	 */
	@Override
	protected int selectNode(ArrayList<Neighbor> neighbors, int k) {
		Collections.sort(neighbors);
		return neighbors.get(k-1).getId();
	}

	/**
	 * This method finds the best insertion in the route for the node
	 */
	@Override
	protected Insertion findBestInsertion(Route r, int nodeID) {
		
		double bestCost = Double.MAX_VALUE;
		Insertion bestInsertion = null;
		for(int i=1; i<r.size(); i++){
			int tailNode = r.get(i-1);
			int headNode = r.get(i);
			double cost = distances.getDistance(tailNode, nodeID);
			cost = cost + distances.getDistance(nodeID, headNode);
			cost = cost - distances.getDistance(tailNode, headNode);
			if(cost < bestCost){
				bestInsertion = new Insertion(nodeID,cost);
				bestInsertion.setPosition(i);
				bestCost = cost;
			}
		}
		return bestInsertion;
	}
	
	/**
	 * @param r the route where the insertion is to be made
	 * @param routedNodes list of already routed nodes
	 * @param k 
	 * @return the kth available insertion into 'r'
	 * It is assumed that route r has the following structure r={0,i-1,i,i+1,0}. In other words it is assumed
	 * that route r starts and ends at the depot. 
	 */
	public Insertion findInsertion(Route r, Hashtable<Integer, Integer> routedNodes, Hashtable<Integer, Integer> nodesToRoute,int k) {
		
		Insertion insertion = null;
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();
				
		//Part 1: Find the not-routed node with the kth shortest/longest distance to a routed node
		
		Iterator<Integer> it = nodesToRoute.values().iterator();
		while(it.hasNext()){
			double bound = this.initializeBound();
			int notRoutedNode = it.next();
			Iterator<Integer> it2 = routedNodes.values().iterator();
			while(it2.hasNext()){
				int routedNode = it2.next();
				bound = this.updateBound(distances.getDistance(notRoutedNode, routedNode), bound);
			}
			neighbors.add(new Neighbor(notRoutedNode,-1,bound));
		}
		int node = this.selectNode(neighbors,k);
		
		//Part II: Find the best inserting position (in route r) for the node
		insertion = this.findBestInsertion(r, node);
		
		return insertion;
	}
	
	
}
