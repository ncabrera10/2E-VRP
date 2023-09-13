package core;

/**
 * Defines the interface to demand parses
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 17, 2016
 *
 */
public interface DemandParser {

	/**
	 * Reads the demands from a file
	 * @param pathname the pathname of the file holding the demands
	 * @param n the number of nodes in the instance (including the depot)
	 * @return the demands
	 */
	public Demands readDemands(String pathname, int n);
	
}
