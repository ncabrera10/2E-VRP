package core;

import java.util.List;

/**
 * Models a solution to the traveling salesman problem (TSP).
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 19, 2016
 * @version Aug 22, 2016
 *
 */
public class TSPSolution implements Route, Solution {

	/**
	 * Stores the route
	 */
	private Route route;
	/**
	 * The objective function
	 */
	private double of=Double.NaN;

	public TSPSolution(){
		this.route=JVRAEnv.getRouteFactory().buildRoute();
	}
	
	/**
	 * Sets the route (sequence of nodes) making up the solution. For encapsulation purposes the method calls {@link #getCopy()} 
	 * on <code>route</code> and stores the obtained copy.
	 *  
	 * @param route the route to store
	 */
	public void setRoute(Route route){
		this.route=route.getCopy();
	}
	
	@Override
	public double getOF() {
		return of;
	}

	@Override
	public void setOF(double of) {
		this.of=of;
	}

	@Override
	public TSPSolution clone() {
		TSPSolution clone=new TSPSolution();
		clone.route=this.route.getCopy();
		clone.of=this.of;
		return clone;
	}

	@Override
	public int size() {
		return this.route.size();
	}

	@Override
	public boolean contains(int nodeID) {
		return this.route.contains(nodeID);
	}

	@Override
	public void reverse() {
		this.route.reverse();
	}

	@Override
	public int positionOf(int nodeID) {
		return this.route.positionOf(nodeID);
	}

	@Override
	public int get(int i) {
		return this.route.get(i);
	}

	@Override
	public void add(int nodeID) {
		this.route.add(nodeID);
	}

	@Override
	public void insert(int nodeID, int i) {
		this.route.insert(nodeID, i);

	}

	@Override
	public boolean removeID(int nodeID) {
		return this.route.removeID(nodeID);
	}

	@Override
	public int remove(int i) {
		return this.route.remove(i);
	}

	@Override
	public void swap(int i, int j) {
		this.route.swap(i, j);
	}

	@Override
	public void relocate(int i, int j) {
		this.route.relocate(i, j);
	}

	public String toString(){
		StringBuilder sb=new StringBuilder();
		sb.append(this.of+"|"+this.route.toString());
		return sb.toString();
	}

	@Override
	public List<Integer> getRoute() {
		return this.route.getRoute();
	}

	@Override
	public Route getCopy() {
		return this.route.getCopy();
	}

	@Override
	public Object setAttribute(RouteAttribute att, Object value) {
		return this.route.setAttribute(att, value);
	}

	@Override
	public Object getAttribute(RouteAttribute att) {
		return this.route.getAttribute(att);
	}

}
