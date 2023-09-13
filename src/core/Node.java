package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class models a node: (It could be a customer, a depot, among others...)
 * @author nicolas.cabrera-malik
 *
 */
public class Node {

	/**
	 * Holds the attributes of the node
	 */
	private HashMap<NodeAttribute,Object> atts = null;
	
	/**
	 * Holds the node id.
	 */
	private int id;
	
	/**
	 * Node type
	 */
	private NodeType type;
	
	/**
	 * Holds an ordered list of neighbors (isNearest first).
	 */
	private ArrayList<Neighbor> neighbors = null;
	
	/**
	 * Holds the node's x coordinate
	 */
	private double cx;
	
	/**
	 * Holds the node's y coordinate
	 */
	private double cy;
	
	// Constructor methods
	
	/**
	 * Class constructor
	 * @param id the node id
	 * @param nodeType the node type
	 */
	public Node(int id,NodeType nodeType) {
		this.id = id;
		this.type = nodeType;
		this.setParamDefaultValues();
	}
	
	/**
	 * Class constructor
	 * @param id the node id
	 * @param nodeType the node type
	 * @param cx the node's x coordinate
	 * @param cy the node's y coordinate
	 */
	public Node(int id,NodeType nodeType,double cx, double cy) {
		this.id = id;
		this.type = nodeType;
		this.cx = cx;
		this.cy = cy;
		this.setParamDefaultValues();
	}
	
	// Getters and setters
	
	/**
	 * Adds a new neighbor to the list of neighbors of the node 
	 * @param neighbor to be added
	 */
	public void setNeighbor(Neighbor neighbor) {
		if(this.neighbors==null){
			this.neighbors=new ArrayList<Neighbor>();
		}
		this.neighbors.add(neighbor);
		Collections.sort(this.neighbors);
	}
	
	/**
	 * 
	 * @param index the position of the neighbor to return in the list of neighbors
	 * @return a neighbor
	 */
	public Neighbor getNeighbor(int index) {
		return this.neighbors.get(index);
	}
	
	/**
	 * Sets the default values for the node parameters
	 */
	public void setParamDefaultValues(){
		this.atts = new HashMap<NodeAttribute,Object>();
		//TODO: Initialize other attributes for a node. Example: this.atts.put(NodeAttribute.SERVICE_TIME,new Double(0));
	}
	
	/**
	 * 
	 * @return the id of the node
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * @return true if the node is a depot and false otherwise
	 */
	public boolean isDepot() {
		return (this.type == NodeType.DEPOT);
	}
	
	/**
	 * 
	 * @param att the attribute to return
	 * @return an attribute of the node
	 */
	public Object getAttribute(NodeAttribute att) {
		return this.atts.get(att);
	}
	
	/**
	 * Sets an attribute for the node
	 * @param att the attribute to set
	 * @param value the new value of the attribute (can be a complex object describing the attribute)
	 */
	public void setAttribute(NodeAttribute att, Object value) {
		this.atts.put(att, value);
	}
	
	/**
	 * @return the cx
	 */
	public double getCx() {
		return cx;
	}

	/**
	 * @param cx the cx to set
	 */
	public void setCx(double cx) {
		this.cx = cx;
	}
	
	// Identification and auxiliary methods

	/**
	 * @return the cy
	 */
	public double getCy() {
		return cy;
	}

	/**
	 * @param cy the cy to set
	 */
	public void setCy(double cy) {
		this.cy = cy;
	}

	/**
	 * 
	 * @return the hashing key of the object
	 */
	public Integer getKey() {
		return this.id;
	}
	
	public String toString(){
		return String.valueOf(id);
	}
}
