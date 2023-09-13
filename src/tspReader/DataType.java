package tspReader;

/**
 * Enumeration of the supported data types.
 * 
 * @author David Hadka
 */
public enum DataType {
	
	/**
	 * Data for a symmetric traveling salesman problem.
	 */
	TSP,
	
	/**
	 * Data for an asymmetric traveling salesman problem.
	 */
	ATSP,
	
	/**
	 * Data for a sequential ordering problem.
	 */
	SOP,
	
	/**
	 * Hamiltonian cycle problem data.
	 */
	HCP,
	
	/**
	 * Capacitated vehicle routing problem data.
	 */
	CVRP,
	
	/**
	 * A collection of tours.
	 */
	TOUR

}
