package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class implements the Farthest insetion finder
 * @author nicolas.cabrera-malik
 *
 */
public class FIFinder extends InsertionFinder {

	public enum Strategy{
		/**
		 * Insert the node whose minimal distance to a routed node is maximal
		 */
		MIN_MAX,
		/**
		 * Insert the node that has the farthest distance to a routed node
		 */
		MAX,
		/**
		 * Insert the node whose maximal distance to a routed node is minimal
		 */
		MAX_MIN		
	}
	
	/**
	 * Node selection strategy. By default the insertion finder is configured with strategy {@link Strategy.MAX}.
	 */
	private Strategy strategy=Strategy.MAX;
	
	
	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * Constructs a new NI finder
	 * @param distances
	 * @param n
	 */
	public FIFinder(final DistanceMatrix distances) {
		this.distances = distances;
		this.strategy = null;
	}
	
	/**
	 * Sort the list of candidate nodes in non-decreasing order and select the kth candidate
	 */
	@Override
	protected int selectNode(ArrayList<Neighbor> neighbors, int k) {
		if(this.strategy==Strategy.MAX_MIN) return this.selectNodeForward(neighbors, k); //We are looking for the node whose maximal distance to a routed node is minimal (i.e., the top of the list)
		else return this.selectNodeBackwards(neighbors, k); //We are looking either for the node whose minimal distance to a routed node is maximal or the farthest node to a routed node. In both cases this node would be at the bottom of the list
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
	
	
	/**
	 * 
	 * @param strategy the node selection strategy to set
	 */
	public void setStrategy(Strategy strategy){
		if(strategy!=null)
			this.strategy=strategy;
	}
	/**
	 * 
	 * @return the current node selection strategy
	 */
	public Strategy getStrategy(){
		return this.strategy;
	}
	
	@Override
	protected double initializeBound() {
		if(this.strategy==Strategy.MIN_MAX) return this.initializeToMax(); //We are looking for the minimal distance between each not-routed node and each routed node
		else return this.initializeToZero(); //We are looking for the maximal distance between each not-routed node and each routed node
	}
	
	@Override
	protected double updateBound(double distanceToNeighbor, double bound) {
		if(this.strategy==Strategy.MIN_MAX) return this.updateIfLower(distanceToNeighbor, bound); //We are looking for the minimal distance between each not-routed node and each routed node
		else return this.updateIfGreater(distanceToNeighbor, bound); //We are looking for the maximal distance between each not-routed node and each routed node
	}
	
	//Specific behavior depending on the selection strategy
	
		private double initializeToMax(){
			return Double.MAX_VALUE;
		}
		private double initializeToZero(){
			return 0;
		}
		
		private double updateIfLower(double distance,double bound){
			if(distance<bound){
				return distance;
			}
			return bound;
		}
		
		private double updateIfGreater(double distance,double bound){
			if(distance>bound){
				return distance;
			}
			return bound;
		}
		
		private int selectNodeBackwards(ArrayList<Neighbor> neighbors, int k) {
			Collections.sort(neighbors);
			Collections.reverse(neighbors);
			return neighbors.get(k-1).getId();
		}
		
		private int selectNodeForward(ArrayList<Neighbor> neighbors, int k) {
			Collections.sort(neighbors);
			return neighbors.get(k-1).getId();
		}
	
}
