package core;

/**
 * Defines the interface to route factories. A route factory is the object responsible for building concrete instances
 * of the {@link Route} Interface. 
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 22, 2016
 *
 */
public interface RouteFactory {
	
	/**
	 * Build a route 
	 * @param parameters
	 * @return a new instance of a concrete {@link Route}
	 */
	public Route buildRoute();

}
