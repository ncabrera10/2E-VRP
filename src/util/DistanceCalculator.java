package util;

/**
 * Defines the interface to distance calculators
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 22, 2016
 *
 */
public interface DistanceCalculator {

	/**
	 * Computes the distance between two points
	 * @param cx1 the coordinate on the x axis of point 1
	 * @param cy1 the coordinate on the y axis of point 1
	 * @param cx2 the coordinate on the x axis of point 2
	 * @param cy2 the coordinate on the y axis of point 2
	 * @return the distance between the two points
	 */
	public double calc(double cx1, double cy1, double cx2, double cy2);

	/**
	 * Computes a distance matrix from a coordinates matrix
	 * @param coordinates the coordinates 
	 * @return the distance matrix
	 */
	public double[][] calc(double[][] coordinates);
	
	
	
}
