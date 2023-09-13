package core;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Defines the interface to objects in charge for finding insertions into a route
 * @author nicolas.cabrera-malik
 *
 */
public abstract class InsertionFinder {

	/**
	 * Updates the search bound of the specific node search strategy
	 * @param distanceToNeighbor the distance from the not-routed node being evaluated and the current
	 * routed node being considered 
	 * @param bound the current search bound
	 * @return an updated search bound
	 */
	protected abstract double updateBound(double distanceToNeighbor, double bound);
	
	/**
	* Initializes the search bound depending on the specific node selection strategy
	*/
	protected abstract double initializeBound();
	
	/**
	 * Selects the node to be inserted in the route according to the specific node selection strategy (i.e., 
	 * Nearest Insertion, Farthest Insertion 1, Farthest Insertion 2, Farthest Insertion 3, etc.)
	 * @param neighbors the list of selectable nodes
	 * @return the id of the node to be inserted
	 */
	protected abstract int selectNode(ArrayList<Neighbor> neighbors,int k);
	
	
	/**
	 * Finds the best insertion of a node into a route taking into account only the geometry of the instance
	 * @param r
	 * @param nodeID
	 * @return
	 */
	protected abstract Insertion findBestInsertion(Route r,int nodeID);
	
	/**
	 * @param r the route where the insertion is to be made
	 * @param routedNodes list of already routed nodes
	 * @param k 
	 * @return the kth available insertion into 'r'
	 * It is assumed that route r has the following structure r={0,i-1,i,i+1,0}. In other words it is assumed
	 * that route r starts and ends at the depot. 
	 */
	public abstract Insertion findInsertion(Route r, Hashtable<Integer, Integer> routedNodes, Hashtable<Integer, Integer> nodesToRoute,int k);
}
