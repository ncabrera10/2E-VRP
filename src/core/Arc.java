package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class represents an arc of the graph. 
 * @author nicolas.cabrera-malik
 *
 */
public class Arc {

	/**
	 * Holds a reference to the tail node of the arc
	 */
	private int tailID;
	
	/**
	 * Holds a reference to the head node of the arc
	 */
	private int headID;
	
	/**
	 * Holds the possible insertions between the tail and head nodes
	 */
	private ArrayList<Insertion> insertions = null;
	
	/**
	 * Holds the atts of the arc
	 */
	private HashMap<ArcAttribute,Object> atts = null;
	
	/**
	 * 
	 * @param tailNode a reference to the tail node of the arc
	 * @param headNode a reference to the head node of the arc
	 */
	public Arc(int tailID,int headID) {
		this.tailID = tailID;
		this.headID = headID;
	}
	
	/**
	 * Adds a new element to the list of insertions of the arc
	 * @param insertion the insertion to add
	 */
	public void setInsertion(Insertion insertion){
		if(this.insertions==null){
			this.insertions=new ArrayList<Insertion>();
		}
		this.insertions.add(insertion);
	}
	/**
	 * Sorts the insertions according to their cost
	 */
	public void sortInsertions(){
		Collections.sort(this.insertions);
	}
	/**
	 * @param i 
	 * @return the Insertion currently at the ith position of the insertion list
	 */
	public Insertion getInsertion(int i) {
		return this.insertions.get(i);
	}
	/**
	 * Sets the default value of the parameters
	 * @since 2010.11.24
	 */
	public void setParamDefaultValues() {
		this.atts = new HashMap<ArcAttribute,Object>();
		this.atts.put(ArcAttribute.DISTANCE,Double.NaN);
		this.atts.put(ArcAttribute.TRAVEL_TIME,Double.NaN);
	}
	
	/**
	 * Sets an attribute for the arc
	 * @param att the attribute to set
	 * @param value the new value of the attribute (can be a complex object describing the attribute)
	 */
	public void setAttribute(ArcAttribute att,Object value){
		this.atts.put(att, value);
	}
	/**
	 * 
	 * @param att the attribute to return
	 * @return an attribute of the arc
	 */
	public Object getAttribute(ArcAttribute att){
		return this.atts.get(att);
	}

	/**
	 * @return the tailID
	 */
	public int getTailID() {
		return tailID;
	}

	/**
	 * @param tailID the tailID to set
	 */
	public void setTailID(int tailID) {
		this.tailID = tailID;
	}

	/**
	 * @return the headID
	 */
	public int getHeadID() {
		return headID;
	}

	/**
	 * @param headID the headID to set
	 */
	public void setHeadID(int headID) {
		this.headID = headID;
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return tailID+","+headID;
	}
	
	public String getKey() {
		return tailID+","+headID;
	}
	
}
