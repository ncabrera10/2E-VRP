package dataStructures;

import java.util.ArrayList;
import java.util.Hashtable;

public class Cluster {
	
	/**
	 * ID of the cluster
	 */
	private int ID;
	
	/**
	 * Demand of the cluster
	 */
	private double demand;
	
	/**
	 * A hashtable with a key for each node included in the cluster. 
	 * It allows for checking if a node belongs to the cluster easily
	 */
	private Hashtable<Integer,Integer> nodes;
	
	/**
	 * A string that contains the nodes included in the cluster
	 */
	private String chain;
	
	/**
	 * Number of nodes in the cluster
	 */
	private int number_of_nodes;
	
	/**
	 * Array of nodes: in the order we entered them
	 */
	
	private ArrayList<Integer> nodes_array;
	
	/**
	 * This method creates a new cluster
	 * @param id
	 */
	public Cluster(int id) {
		
		this.ID = id;
		this.demand = 0;
		this.nodes = new Hashtable<Integer,Integer>();
		this.number_of_nodes = 0;
		this.chain = "";
		nodes_array = new ArrayList<Integer>();
		
	}
	
	/**
	 * This method udpdates the demand of the cluster
	 * @param toAdd
	 */
	public void updateDemand(double toAdd) {
		this.demand += toAdd;
	}
	
	/**
	 * This method inserts a node in the cluster
	 * @param node_id
	 */
	public void insertNode(int node_id) {
		nodes.put(node_id,1);
		nodes_array.add(node_id);
		this.number_of_nodes+=1;
		this.chain += node_id+" - ";
	}
	
	/**
	 * This method checks if a node is in the cluster
	 * @param node_id
	 * @return
	 */
	public boolean checkIfNodeIsInCluster(int node_id) {
		return nodes.containsKey(node_id);
	}

	public String toString() {
		return this.chain;
	}
	
	//---------Getters and setters
	
	/**
	 * @return the iD
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(int iD) {
		ID = iD;
	}

	/**
	 * @return the demand
	 */
	public double getDemand() {
		return demand;
	}

	/**
	 * @param demand the demand to set
	 */
	public void setDemand(double demand) {
		this.demand = demand;
	}

	/**
	 * @return the nodes
	 */
	public Hashtable<Integer, Integer> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(Hashtable<Integer, Integer> nodes) {
		this.nodes = nodes;
	}

	/**
	 * @return the chain
	 */
	public String getChain() {
		return chain;
	}

	/**
	 * @param chain the chain to set
	 */
	public void setChain(String chain) {
		this.chain = chain;
	}

	/**
	 * @return the number_of_nodes
	 */
	public int getNumber_of_nodes() {
		return number_of_nodes;
	}

	/**
	 * @param number_of_nodes the number_of_nodes to set
	 */
	public void setNumber_of_nodes(int number_of_nodes) {
		this.number_of_nodes = number_of_nodes;
	}

	/**
	 * @return the nodes_array
	 */
	public ArrayList<Integer> getNodes_array() {
		return nodes_array;
	}

	/**
	 * @param nodes_array the nodes_array to set
	 */
	public void setNodes_array(ArrayList<Integer> nodes_array) {
		this.nodes_array = nodes_array;
	}
	
	
	
	
}
