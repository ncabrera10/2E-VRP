package core;

/**
 * Defines the interface to route hash codea. Route hash codes are responsible for computing the hash code of a route.
 * The hash code can be computed based on route attributes such as the sequence of customers or the set visited customers.
 * Route hash codes are used mainly in long term memory mechanism (e.g., a route pool) for computing the key
 * for storage and lookup.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 12, 2016
 * @see {@link  RoutePool}
 */
public interface RouteHashCode {
	
	/**
	 * Computes the hash code of a route.
	 * @param r the route
	 * @return the hash code of route <code>r</code>
	 */
	public int compute(Route r);
	
}
