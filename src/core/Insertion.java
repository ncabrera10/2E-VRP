package core;

public class Insertion implements Comparable<Insertion>{
	
	/**
	 * Holds the position in the route where the insertion is to be performed 
	 */
	private int position;
	
	/**
	 * Holds a reference to the node that is to be inserted
	 */
	private int node;
	
	/**
	 * Holds the insertion cost
	 */
	private double cost;

	/**
	 * Constructor
	 * @param nodeId identifier of the node to be inserted
	 * @param cost cost of the insertion
	 */
	public Insertion(int node,double cost){
		this.node=node;
		this.cost=cost;
	}
	/**
	 * @return the id of the node to be inserted
	 */
	public int getNode(){
		return this.node;
	}
	/**
	 * @return the insertion cost
	 */
	public double getCost(){
		return this.cost;
	}
	/**
	 * Compares to another insertion
	 */
	@Override
	public int compareTo(Insertion otherInsertion) {
		return (this.cost < otherInsertion.getCost() ? -1 : (this.cost > otherInsertion.getCost() ? 1 : 0));
	}
	/**
	 * Sets the position in the route where the insertion is to be made
	 * @param position
	 */
	public void setPosition(int position){
		this.position=position;
	}
	/**
	 * @return the position in which the insertion is to be made
	 */
	public int getPosition(){
		return this.position;
	}
	@Override
	public String toString() {
		return node+" in "+position+" - "+cost;
	}
	
	
}
