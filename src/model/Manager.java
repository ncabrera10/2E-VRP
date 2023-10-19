package model;

import java.io.IOException;

/**
 * Class to manage the different algorithms
 */

public class Manager {

	public Manager() throws IOException, InterruptedException{

	}
	
	/**
	 * Runs the MSH algorithm
	 */
	
	public Solver_cplex runMSH_cplex(String instance_identifier)throws IOException, InterruptedException {
		
		// Creates a solver instance:
		
			Solver_cplex solver = new Solver_cplex();
		
		// Runs the MSH:
		
			solver.MSH(instance_identifier);
		
		// Returns the solver instance:
		
			return solver;
	}
	
	/**
	 * Runs the MSH algorithm
	 */
	
	public Solver_gurobi runMSH_gurobi(String instance_identifier)throws IOException, InterruptedException {
		
		// Creates a solver instance:
		
			Solver_gurobi solver = new Solver_gurobi();
		
		// Runs the MSH:
		
			solver.MSH(instance_identifier);
		
		// Returns the solver instance:
		
			return solver;
	}
	
	
}
