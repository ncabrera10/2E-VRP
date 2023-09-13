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
	
	public Solver runMSH(String instance_identifier)throws IOException, InterruptedException {
		
		// Creates a solver instance:
		
			Solver solver = new Solver();
		
		// Runs the MSH:
		
			solver.MSH(instance_identifier);
		
		// Returns the solver instance:
		
			return solver;
	}
}
