package core;
/**
 * Defines the interface to distance parsers
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Dec 4, 2015
 *
 */
public interface DistanceParser {
	/**
	 * Reads the distance matrix from a file
	 * @param pathname the pathname of the file holding the file
	 * @param n the number of nodes in the instance (including the depot)
	 * @return a distance matrix
	 */
	public DistanceMatrix readDistances(String pathname, int n);

}
