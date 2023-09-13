package core;

/**
 * Defines the interface to route hash code factories.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 12, 2016
 *
 */
public interface RouteHashCodeFactory {
	
	/**
	 * Builds a new {@lnik RouteHashCode}
	 * @return a new instance of {@lnik RouteHashCode}
	 */
	public RouteHashCode build();

}
