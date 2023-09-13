package msh;

import java.util.ArrayList;

import core.RoutePool;
import core.Solution;

public class DummyAssemblyFunction implements AssemblyFunction{

	@Override
	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pool) {
		return bound;
	}

}
