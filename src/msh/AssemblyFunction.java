package msh;

import java.util.ArrayList;

import core.ArrayDistanceMatrix;
import core.Route;
import core.RoutePool;
import core.Solution;


public abstract class AssemblyFunction {
	
	protected abstract Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools, ArrayDistanceMatrix distances_customers, ArrayList<ArrayDistanceMatrix> distances_satellite_customers);
	
	public double objectiveFunction;
	public double objectiveFunctionPreMIP;
	
	public ArrayList<Route> solution_fe;
	public ArrayList<Route> solution_se;
	public ArrayList<ArrayList<Double>> solution_fe_drops;
	public ArrayList<Integer> solution_se_satellites;
	public ArrayList<String> solution_se_identifiers;
	public ArrayList<Integer> solution_se_hashcodes;
}
