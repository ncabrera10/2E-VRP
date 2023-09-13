package core;

/**
 * Defines the interface for split algorithms
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 *
 */
public interface Split {
	
	/**
	 * Runs the split algorithm. The method takes a {@link IRoute} encoding a giant TSP tour starting from the depot and visiting all nodes in the graph.
	 * The method splits the TSP tour into a {@link IVRPSolution} encoding a VRP solution (i.e., a set of feasible routes). In general implementations should follow the idea in:</br>
	 * 
	 * Prins. C., A simple and effective evolutionary algorithm for the vehicle routing problem. Computers and Operations Research. 2004.</br>
	 * 
	 * That is providing an optimal partition of the TSP tours into routes.</br>
	 * 
	 * Implementing classes must enssure that the {@link IVRPSolution} that is returned is evaluated (that is, its objective function is computed).
	 * 
	 * @param r the route to split
	 * @return a VRP solution
	 */
	public Solution split(TSPSolution r);
	
}
