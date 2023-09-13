package msh;

import java.util.ArrayList;

import core.RoutePool;
import core.Solution;


public interface AssemblyFunction {
	
	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools);
	
}
