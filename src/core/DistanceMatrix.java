package core;

/**
 * Defines the interface to distance matrices. A distance matrix is an object holding (or able to compute) the distance
 * between two nodes in a VRP graph. Implementing classes should make sure that instance of the class are inmutable objects.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Dec 4, 2015
 * @version Aug 22, 2016
 *
 */
public interface DistanceMatrix {
	/**
	 * 
	 * @param i
	 * @param j
	 * @return the distance between nodes <code>i</code> and <code>j</code>
	 */
	public double getDistance(int i, int j);
	/**
	 * Sets the distance between nodes <code>i</code> and <code>j</code>
	 * @param i
	 * @param j
	 * @param distances the distance between nodes <code>i</code> and <code>j</code>
	 */
	public void setDistances(double[][] distances);
	/**
	 * 
	 * @return the size of the distance matrix. Implementing classes are responsible for defining exactly
	 * what the <code>size</code> means to them.
	 */
	public int size();
		
}
