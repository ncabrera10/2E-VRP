package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * This class implements the Best-insertion finder
 * @author nicolas.cabrera-malik
 *
 */
public class BIFinder extends InsertionFinder {

	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	
	/**
	 * List of arcs
	 */
	private HashMap<String,Arc> arcs;
	
	/**
	 * Constructs a new NI finder
	 * @param distances
	 * @param n
	 */
	public BIFinder(final DistanceMatrix distances) {
		this.distances = distances;
		this.arcs = new HashMap<String,Arc>();
		this.initializeArcInsertions();
	}
	
	
	/**
	 * The initial search bound is Double.MAX_VALUE
	 */
	@Override
	protected double initializeBound() {
		return 0;
	}
	
	/**
	 * If the distance to the node being considered is shorter than the current search bound
	 * the bound should be updated (we are looking for the nearest neighbor to an already routed node).
	 */
	@Override
	protected double updateBound(double distanceToNeighbor, double bound) {
		return 0;
	}
	
	/**
	 * Sort the list of candidate nodes in non-decreasing order and select the kth candidate
	 */
	@Override
	protected int selectNode(ArrayList<Neighbor> neighbors, int k) {
		return -1;
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
		

		ArrayList<Insertion>insertions = new ArrayList<Insertion>(); 
		double bestBound = Double.MAX_VALUE;
		int counter = 0;
		
		
		//Part 1: Find the best insertions for each arc of the route
		
		for(int i=1; i<r.size(); i++){
			int tailNode = r.get(i-1);
			int headNode = r.get(i);
			int insertionIndex = 1;
			Insertion bestInsertion = arcs.get(tailNode+","+headNode).getInsertion(insertionIndex-1);
			bestInsertion.setPosition(i);
			boolean flag = true;
			while(flag){
				if(routedNodes.contains(bestInsertion.getNode())){
					insertionIndex++;
					bestInsertion = arcs.get(tailNode+","+headNode).getInsertion(insertionIndex-1);
					bestInsertion.setPosition(i);
				}else{
					if(counter>=k){
						if(bestInsertion.getCost() < bestBound){
							insertions.remove(insertions.size()-1);
							bestBound = bestInsertion.getCost();
						}
						flag = false;
					}else{
						counter++;
					}
					insertions.add(bestInsertion);
					Collections.sort(insertions);
				}

			}
		}
		return insertions.get(k-1);
		
		
	}
	
	/**
	 * This method initializes all the arc insertions
	 */
	private void initializeArcInsertions() {
		
		// Create the arcs:
		
		for(int i = 0; i < distances.size(); i++) {
			for(int j = 0; j < distances.size(); j++) {
				
				Arc arc = new Arc(i,j);
				arcs.put(arc.getKey(), arc);
			}
		}
		
		// Initialize the insertions for each arc
		
		Collection<Arc> arcs = (Collection<Arc>) this.arcs.values();
		Iterator<Arc> it = arcs.iterator();
		while (it.hasNext()){
			Arc currentArc = (Arc)it.next();
			int tailNode = currentArc.getTailID();
			int headNode = currentArc.getHeadID();
			
			for(int i=0; i<distances.size(); i++){
				int nodeToInsert = i;
				if(nodeToInsert != tailNode && nodeToInsert != headNode){
					double cost = this.distances.getDistance(tailNode, nodeToInsert);//From the tail node to the inserting node
					cost = cost + this.distances.getDistance(nodeToInsert,headNode);   //From the inserting to the head node
					cost = cost - this.distances.getDistance(tailNode,headNode); //From the tail to the head node
					currentArc.setInsertion(new Insertion(nodeToInsert,cost));
				}				
			}
			currentArc.sortInsertions();
		}
	}
	
	
}
