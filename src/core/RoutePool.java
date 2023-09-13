package core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import globalParameters.GlobalParameters;

/**
 * Implements a route pool. Technically, a route pool is simply a set of routes with no particular correlation.
 * Route pools are usually used as long term memory in VRP heuristics.
 * Some examples can be found in <a href:"http://dx.doi.org/10.1016/j.trc.2015.09.009">Montoya et al. 2016</a>, 
 * <a href:"http://link.springer.com/article/10.1007/s10732-015-9281-6">Mendoza, Rousseau and Villegas 2016</a>,
 * <a href:"http://www.springerlink.com/content/g3j763126q0tr814/">Mendoza and Vilelgas 2013</a>.</br>
 * Routes are stored in the pool in two phases. First, client classes submit the routes through method {@link #add(Route)}. 
 * The submitted route joins a queue. In the second phase, the route pool dequeues the route and runs a series of
 * {@link PoolFilteringRule}s before effectively storing the route in the pool.</br>
 * 
 * To improve computational performance, the route pool runs on an independent thread.</br>
 * 
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 6, 2016
 *
 */
public final class RoutePool{
	
	/**
	 * Route pool
	 */
	private HashMap<Integer,Route> pool= new HashMap<>();
	
	/**
	 * A reference to an object responsible for computing the storage and lookup route keys
	 */
	private RouteHashCode hashCode;

	/**
	 * Satellite associated with the pool
	 */
	
	private int satellite;
	
	/**
	 * Constructs a new {@lnik RoutePool} without any route filtering rule and a default {@link RouteHashCode}. 
	 * 
	 * @see {@link PoolFilteringRule} 
	 * @see {@link JVRAEnv}
	 */
	public RoutePool(int sat){
		this.pool=new HashMap<>();
		this.hashCode=JVRAEnv.getRouteHashCodeFactory().build();
		this.satellite = sat;
	}

	/**
	 * 
	 * @return the number of routes currently stored in the pool
	 */
	public final int size(){
		return this.pool.size();
	}
	/**
	 * Clears the route pool (i.e., removes all routes from the pool). Right after a call to this method {@link #size()} should
	 * return 0.
	 */
	public void clear(){
		this.pool.clear();
		if(GlobalParameters.PRINT_IN_CONSOLE) {
			Logger.getLogger("EXECUTION").log(Level.INFO,Thread.currentThread().getName()+" cleared the route pool");
		}
	}
	/**
	 * 
	 * @return an iterator to the routes stored in the pool
	 */
	public Iterator<Route> iterator(){
		return this.pool.values().iterator();
	}

	/**
	 * Adds a route to the route pool
	 * @param r the route to add
	 */
	public void add(Route r){
		this.pool.put(this.hashCode.compute(r),r);
	}
	
	/**
	 * Checks if the pool contains a given route
	 * @param r the route
	 * @return true if the pool contains the route and false otherwise
	 */
	public boolean contains(Route r){
		//return this.pool.containsValue(this.hashCode.compute(r));
		return this.pool.containsKey(this.hashCode.compute(r));
	}
	
	/**
	 * Transforms the pool into an array
	 * @return
	 */
	public Route[] toArray(){
		
		Route[] array=new Route[this.pool.size()];
		Iterator<Route> it=this.pool.values().iterator();
		int r=0;
		while(it.hasNext()){
			array[r]=it.next();
			r++;
		}			
		return array;		
	}


	/**
	 * @return the satellite
	 */
	public int getSatellite() {
		return satellite;
	}

	/**
	 * @param satellite the satellite to set
	 */
	public void setSatellite(int satellite) {
		this.satellite = satellite;
	}
	
	
}
