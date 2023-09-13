package core;

/**
 * This class models a neighbor
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 17, 2016
 *
 */
public class Neighbor implements Comparable<Neighbor>{

	/**
	 * The id of the node
	 */
	private final int id;
	/**
	 * The id of the neighbor node
	 */
	private final int neighborID;
	/**
	 * The distance between <code>id</code> and <code>neighborID</code>
	 */
	private final double distance;

	/**
	 * Constructs a new neighbor
	 * @param id
	 * @param neighborID
	 * @param distance
	 */
	public Neighbor(int id, int neighborID, double distance){
		this.id=id;
		this.neighborID=neighborID;
		this.distance=distance;
	}

	/**
	 * 
	 * @return the id of the node
	 */
	public int getId() {
		return id;
	}
	/**
	 * 
	 * @return the id of the neighbor node
	 */
	public int getNeighborID() {
		return neighborID;
	}
	/**
	 * 
	 * @return the distance to the neighbor
	 */
	public double getDistance() {
		return distance;

	}

	@Override
	public int compareTo(Neighbor o) {
		if(distance<o.distance)
			return -1;
		return this.distance==o.distance?0:1;
	}
	
	@Override
	public String toString(){
		return "("+this.id+","+this.neighborID+")\t"+this.distance;
	}
	
}

