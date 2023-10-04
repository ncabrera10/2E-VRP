package msh;

import java.util.ArrayList;

import core.Route;
import core.RoutePool;
import core.Solution;


public abstract class AssemblyFunction {
	
	protected abstract Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools);
	
	public double objectiveFunction;
	public ArrayList<Route> solution_fe;
	public ArrayList<Route> solution_se;
	public ArrayList<ArrayList<Double>> solution_fe_drops;
	public ArrayList<Integer> solution_se_satellites;
	public ArrayList<String> solution_se_identifiers;
}
