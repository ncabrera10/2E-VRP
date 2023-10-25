package msh;

import java.util.ArrayList;

import core.ArrayDistanceMatrix;
import core.RoutePool;
import core.Solution;

public class DummyAssemblyFunction extends AssemblyFunction{

	@Override
	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pool, ArrayDistanceMatrix distances_customers, ArrayList<ArrayDistanceMatrix> distances_satellite_customers) {
		return bound;
	}

	

}
