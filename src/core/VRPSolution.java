package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Models a VRP solution
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 21, 2016
 *
 */
public class VRPSolution implements Solution {
	
	/* Stores the routes making up the solution */
	private List<Route> routes;
	/* Stores the objective function */
	private double of=Double.NaN;
	
	/**
	 * Constructs a new VRPSolution
	 */
	public VRPSolution(){
		this.routes=new ArrayList<Route>();
	}
	
	@Override
	public double getOF() {
		return this.of;
	}

	@Override
	public void setOF(double of) {
		this.of=of;
	}

	@Override
	public Solution clone() {
		VRPSolution clone=new VRPSolution();
		clone.of=this.of;
		clone.routes=this.cloneRoutes();
		return clone;
	}
	/**
	 * 
	 * @return the number of routes in the solution
	 */
	public int size(){
		return this.routes.size();
	}
	/**
	 * 
	 * @param r the route index
	 * @return the size of route <code>r</code> (i.e., the number of nodes visited by the route)
	 */
	public int size(int r){
		return this.routes.get(r).size();
	}
	/**
	 * 
	 * @return a hard copy of the routes making up the solution
	 */
	public List<Route> getRoutes(){
		return this.cloneRoutes();
	}
	/**
	 * Returns a hard copy of the route in position <code>r</code> in the solution
	 * @param r the position of the route
	 * @return a hard copy of the route in position <code>r</code> in the solution
	 */
	public Route getRoute(int r){
		return routes.get(r).getCopy();
	}
	/**
	 * Sets the routes making up the solution. For encapsulation purposes the method calls {@link #getCopy()} on each 
	 * route in <code>routes</code> and stores only the obtained copies. 
	 * @param routes the routes to store
	 */
	public void setRoutes(List<Route> routes){
		this.routes=new ArrayList<>();
		for(Route r:routes)
			this.routes.add(r.getCopy());
	}
	/**
	 * Adds a route to the solution. For encapsulation purposes the method first calls {@link #getCopy()} on <code>r</code> 
	 * and then adds to the solution the obtained copy.
	 * 
	 * @param r the route to add
	 */
	public void addRoute(final Route r){
		this.routes.add(r.getCopy());
	}
	/**
	 * Inserts a route into a specific position of the solution. For encapsulation purposes the method 
	 * first calls {@link #getCopy()} on <code>r</code> and then inserts into the solution the obtained copy.
	 * 
	 * @param r the route to insert
	 * @param i the possition in which the route must be inserted
	 */
	public void insertRoute(final Route r, int i){
		this.routes.add(i, r.getCopy());
	}
	/**
	 * 
	 * @param r the index of the route
	 * @param i the position in the route
	 * @return Returns the node in position <code>i</code> of route <code>r</code>
	 */
	public int getNode(int r, int i){
		return this.routes.get(r).get(i);
	}	
	/**
	 * Removes route <code>r</code> from the solution.</br>
	 * 
	 * <strong>Examples:</strong><br>
	 * 
	 * Assume the current solution <code>s</code> has two routes <code>{0,3,4,5,0}</code> and <code>{0,2,1,0}</code>.</br>
	 * 
	 * <code>remove(1)</code> leads to <code>s={{0,3,4,5,0}}</code>.</br>
	 * 
	 * Any call to the method with <code>r>=s.sise()</code> or <code>r<0</code> results on an {@link IndexOutOfBoundsException}.</br>
	 * 
	 * 
	 * @param r the index of the route to remove
	 * @return the removed route
	 */
	public Route remove(int r){
		return this.routes.remove(r); //need no cloning since the route no longer defines the state of this route solution object
	}
	/**
	 * Removes the node in position <code>i</code> of route <code>r</code>. After the removal of the node all subsequent nodes
	 * shift one position to the left.
	 * 
	 * <strong>Examples:</strong><br>
	 * 
	 * Assume the current solution <code>s</code> has two routes <code>{0,3,4,5,0}</code> and <code>{0,2,1,0}</code>.</br>
	 * 
	 * <code>remove(1,0)</code> leads to <code>s={{0,3,4,5,0},{2,1,0}}</code></br>
	 * <code>remove(1,1)</code> leads to <code>s={{0,3,4,5,0},{0,1,0}}</code></br>
	 * <code>remove(1,3)</code> leads to <code>s={{0,3,4,5,0},{0,2,1}}</code></br>
	 * 
	 * Any call to the method with <code>r>=s.sise()</code> or <code>r<0</code> results on an {@link IndexOutOfBoundsException}.</br>.
	 * Simmilarly, any call to the method with <code>i>=r.sise()</code> or <code>i<0</code> results on an {@link IndexOutOfBoundsException}.</br>
	 * 
	 * Note that for the sake of flexibility this method does not check semantic constraints such as: the first and last node in the route should be the same, or
	 * a node cannot be visited more than once by a route. Client classes are responsible for controling these constraints depending on the VRP in hand.
	 * 
	 * @param r the index of the route
	 * @param i the position in route <code>r</code> 
	 * @return the ID of the removed node
	 */
	public int remove(int r, int i){
		return this.routes.get(r).remove(i);
	}
	/**
	 * Inserts <code>nodeID</code> in position <code>i</code> of route <code>r</code></br>
	 * 
	 * <strong>Examples:</strong><br>
	 * 
	 * Assume the current solution <code>s</code> has two routes <code>{0,3,4,5,0}</code> and <code>{0,2,1,0}</code>.</br>
	 * 
	 * <code>insert(6,1,0)</code> leads to <code>s={{0,3,4,5,0},{6,0,2,1,0}}</code></br>
	 * <code>insert(6,1,1)</code> leads to <code>s={{0,3,4,5,0},{0,6,2,1,0}}</code></br>
	 * <code>insert(6,1,3)</code> leads to <code>s={{0,3,4,5,0},{0,2,1,6,0}}</code></br> 
	 * <code>insert(6,1,4)</code> leads to <code>s={{0,3,4,5,0},{0,2,1,0,6}}</code></br>
	 * 
	 * Any call to the method with <code>r>=s.sise()</code> or <code>r<0</code> results on an {@link IndexOutOfBoundsException}.
	 * Simmilarly, any call to the method with <code>i>r.sise()</code> or <code>i<0</code> results on an {@link IndexOutOfBoundsException}.</br>
	 * 
	 * Note that for the sake of flexibility this method does not check semantic constraints such as: the first and last node in the route should be the same, or
	 * a node cannot be visited more than once by a route. Client classes are responsible for controling these constraints depending on the VRP in hand.
	 * 
	 * @param nodeID the ID of the node to insert
	 * @param r the index of the route
	 * @param i the position in route <code>r</code>
	 */
	public void insert(int nodeID, int r, int i){
		if(this.routes.get(r).size()==i)
			this.routes.get(r).add(nodeID);
		else
			this.routes.get(r).insert(nodeID, i);
	}
	/**
	 * Removes from the solution every route <code>r</code> with <code>r.size()==size</code>
	 * @param size the size of the routes to remove
	 */
	public void removeRoutesBysize(int size){
		for(int r=0;r<this.routes.size();r++){
			if(routes.get(r).size()==size){
				this.routes.remove(r);
				r--;
			}
		}
	}
	
	/**
	 * Sets a new value for an attribute of a given route
	 * @param r the index of the route
	 * @param att the attribute to set
	 * @param value the new value for the attribute
	 * @return the previous value associated to attribute <code>att</code> or null if there was not value associated to <code>att</code>.
	 * Note that null may also mean that the previous value associated to <code>att</code> was null.
	 */
	public Object setRouteAttribute(int r, RouteAttribute att, Object value){
		return this.routes.get(r).setAttribute(att, value);		
	}
	/**
	 * Returns the current value for an attribute of a given route
	 * @param r the index of the route
	 * @param att the attribute to set
	 * @return the current value associated to attribute <code>att</code> or null if there is not value associated to <code>att</code>.
	 * Note that null may also mean that the value associated to <code>att</code> is null.
	 */
	public Object getRouteAttribute(int r, RouteAttribute att){
		return this.routes.get(r).getAttribute(att);
	}
	
	@Override
	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(this.of+"\n");
		for(Route r:routes){
			sb.append(r.toString()+"\n");
		}
		return sb.toString();
	}
	
	/**
	 * Internal method for cloning the list of routes
	 * @return a hard copy of the list of routes
	 */
	private List<Route> cloneRoutes(){
		List<Route> clone=new ArrayList<>();
		for(Route r:routes)
			clone.add(r.getCopy());
		return clone;
	}
	
}
