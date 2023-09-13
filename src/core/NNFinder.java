package core;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Finds nearest neighbors
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 18, 2016
 *
 */
public class NNFinder{

	/**
	 * The sorted list of neighbors. Position <code>[i][j]</code> in this matrix contains the ID 
	 * of the <code>j</code>th closest neighbor of node <code>i</code>
	 */
	private final int[][] neighbors;

	/**
	 * Constructs a new NNFinder and initializes the <code>neighbors</code> matrix.
	 * @param distances the distance matrix for building the neighbor list
	 * @param n the number of nodes in the instance
	 */
	public NNFinder(final DistanceMatrix distances, final int n){
		this.neighbors=new int[n][n-1];
		//Compute and sort the list of neighbors for each node
		for(int i=0;i<n;i++){
			ArrayList<Neighbor> list=new ArrayList<>(n);
			for(int j=0;j<n;j++){
				if(i!=j)
					list.add(new Neighbor(i,j,distances.getDistance(i, j)));
			}
			//Sort the neighbors
			Collections.sort(list);
			//Store the sorted list of neighbors
			for(int j=0;j<n-1;j++)
				neighbors[i][j]=list.get(j).getNeighborID();
		}
	}
	/**
	 * Finds the <code>k</code>th nearest non-routed neighbor of node <code>i</code>
	 * @param i a node in the graph
	 * @param nodeStatus the status of the nodes. For each node <code>j</code> in the TSP instance, <code>nodeStatus[j]=true</code>
	 * if node <code>j</code> is already included in the TSP tour and <code>nodeStatus[j]=true</code> otherwise.
	 * @param k
	 * @return the <code>k</code>th nearest non-routed neighbor of node <code>i</code>
	 */
	public int findNN(int i, boolean[] nodeStatus, int k) {
		if(k>neighbors[i].length)
			throw new IllegalArgumentException("k="+k+" is larger than the number neighbors of node i="+i);
		int counter=0;
		int neighbor=-1;
		for(int j=0; j<neighbors[i].length;j++){
			if(!nodeStatus[neighbors[i][j]]){
				neighbor=neighbors[i][j];
				counter++;
				if(counter==k)
					return neighbor;					
			}
		}
		if(neighbor==-1)
			throw new IllegalArgumentException("k="+k+" is larger than the number of non-routed neighbors of node i="+i);
		return neighbor;
	}
	/**
	 * Finds the nearest not-routed neighbor of node <code>i</code>
	 * @param i a node in the graph
	 * @param nodeStatus the status of the nodes. For each node <code>j</code> in the TSP instance, <code>nodeStatus[j]=true</code>
	 * if node <code>j</code> is already included in the TSP tour and <code>nodeStatus[j]=true</code> otherwise.
	 * @return the nearest not-routed neighbor of node <code>i</code>. If all the neighbors of node <code>i</code> are already routed, the method
	 * returns -1;
	 */
	public int findNN(int i, boolean[] nodeStatus) {
		return this.findNN(i, nodeStatus, 1);
	}

	@Override
	public String toString(){
		StringBuilder sb= new StringBuilder();
		for(int i=0; i<neighbors.length; i++){
			sb.append(i+"|\t");
			for(int j=0; j<neighbors[i].length; j++){
				sb.append(neighbors[i][j]+"\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
