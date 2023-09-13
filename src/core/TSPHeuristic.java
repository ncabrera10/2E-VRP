package core;

/**
 * Defines the interface for TSP heuristics
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 *
 */
public interface TSPHeuristic extends Algorithm{
	/**
	 * Sets the initial node in the TSP tour
	 * @param i the initial node of the tour
	 */
	public void setInitNode(int i);
	/**
	 * Sets an initial route that the heuristic must complete when method {@link #run()} is called. Implementing classes must
	 * ensure that that the initial and terminal node in route <code>r</code> are the same (i.e., <code>r.get(0)=r.get(r.size())</code>).
	 * For encaptulation purposes, implementing clases must ensure that they do not store a reference to the route pointed to by <code>r</code>
	 * but rather a hard copy of the route (see {@link Route#getCopy()}.
	 * 
	 * @param r the initial route
	 */
	public void setInitRoute(Route r);
	
	@Override
	public TSPSolution run();
	
}
