package core;

/**
 * Implements the demands using an array as the subjacent data structure.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 *
 */
public class ArrayDemands implements Demands {
	
	/**
	 * Stores the demands
	 */
	private final double[] demands;
	
	/**
	 * Constructs a new object of the class
	 * @param demands the demands
	 */
	public ArrayDemands(double[] demands){
		this.demands=demands.clone();
	}
	
	@Override
	public double getDemand(int i) {
		return this.demands[i];
	}

}
