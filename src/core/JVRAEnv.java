package core;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO Write more thorought documentation
//TODO Implement initialization mechanisms
/**
 * Implements the execution environment of jVRA.
 * 
 * Default values:
 * 
 * Route Factory: an instance of {@link GenericArrayRouteFactory}.
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 22, 2016
 *
 */
public class JVRAEnv {

	/* Holds a reference to the route factory. The default value points to an instance of {@link ArrayRouteFactory} */
	private static RouteFactory routeFactory=new GenericArrayRouteFactory();
	private static OptimizationSense sense=OptimizationSense.MINIMIZATION;
	private static RouteHashCodeFactory routeHashCodeFactory=new DefaultRouteHashCodeFactory();

	/**
	 * Initializes the environment using the parameters passed as arguments
	 * @param params the parameters
	 * @return true if the environment was correctly initialized and false otherwise
	 */
	public static boolean init(Properties params){
		boolean flag=true;
		// Route factory (not required because there is a default value)
		if(params.contains("ROUTE_FACTORY"))
			try {
				setRouteFactory((RouteFactory)loadClass(params.getProperty("ROUTE_FACTORY")).newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				Logger.getLogger("CONFIG").log(Level.SEVERE,"The route factory could not be instantiated");
				return false;
			}

		// Route hash code factory (not required because there is a default value)
		if(params.contains("ROUTE_HASH_CODE_FACTORY"))
			try {
				setRouteFactory((RouteFactory)loadClass(params.getProperty("ROUTE_HASH_CODE_FACTORY")).newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				Logger.getLogger("CONFIG").log(Level.SEVERE,"The route hash code factory could not be instantiated");
				return false;
			}

		Logger.getLogger("CONFIG").log(Level.INFO,"The jVRP environment was successfully created");
		return flag;
	}

	/**
	 * Sets a new route factory. 
	 * @param routeFactory a reference to the new route factory
	 */
	public static void setRouteFactory(RouteFactory routeFactory){
		JVRAEnv.routeFactory=routeFactory;
	}
	/**
	 * 
	 * @return a reference to the active route factory
	 */
	public static RouteFactory getRouteFactory(){
		return routeFactory;
	}

	/**
	 * @return a reference to the active route hash code factory
	 */
	public static RouteHashCodeFactory getRouteHashCodeFactory(){
		return routeHashCodeFactory;
	}

	/**
	 * Dynamically loads the class corresponding to one component
	 * @param className the name of the class to load
	 * @return an instance of {@link Class} associated to the component
	 */
	private static Class<?> loadClass(String className){
		try {
			Class<?> myClass=Class.forName(className);
			return myClass;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	/**
	 * 
	 * @return the active optimization sense
	 */
	public static OptimizationSense getOptimizationSense(){
		return sense;
	}

}
