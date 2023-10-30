package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import globalParameters.GlobalParametersReader;
import model.Manager;

/**
 * This class runs the MSH procedure. 
 * The user can select the instance and the implementation modifying the main method.
 * 
 * Instances:
 * 	Set: Breunig et al. 
 * 
 * If you want to change more parameters, you can modify the "parametersCG.xml" file.
 * For example:
 * 	-Printing useful information on console
 * 	-Modifying the number of iterations for each TSP heuristic..
 * 	Among others..
 * 
 * CPLEX is used to solve the set partitioning model. (assembly phase)
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class Main_cplex {

	public static void main(String[] args) {
		
	// ----------------SELECT THE MAIN PARAMETERS-----------------------
	
			// Select the txt file, with the instance specifications: The txt files are located in the experiments folder.
			
				//Options: ExperimentsAllSets.txt
	
				String fileName = "ExperimentsAllSets.txt"; 
				
			// Select the instance you want to run, (i.e., the line of the txt file): 1-285
			
				int current_instance = 11; 
				
			// Configuration file name:
				
				String config_file = "default.xml";
			
		// ------------------------------------------------------------------	
			
		// Main logic:
			
			
			// Create a buffered reader:
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader("./experiments/"+fileName));
				int count = 0;
				String line = reader.readLine();
				count++;
				while(line != null && count < current_instance) {
					line = reader.readLine();
					count++;
				}
				
				args = line.split(";");
				reader.close();
				
			} catch (IOException e1) {
				System.out.println("The file does not exists");
				System.exit(0);
			}
			
		// Store the instance name file:
			
		String instance_identifier = args[1];
		
		// Runs the code:
		
			try {
				
				// Loads the global parameters: some paths, the precision..
				
					GlobalParametersReader.initialize("./config/"+config_file);
					
				// Creates a Manager:
					
					Manager manager = new Manager();
					
				// Runs the MSH:
					
					manager.runMSH_cplex(instance_identifier);
					
				// Closes the code:
					
					System.exit(0);
				
			}catch(Exception e) {
				
				System.out.println("A problem running the code");
				System.out.println("Suggestions:");
				System.out.println("1. Do you have a valid Cplex license?");
				System.out.println("2. Do you have a folder in the results folder with the name of the config file?");
				System.out.println("3. If the instance is large, can you increase the memory / turn-off some sampling functions?");
				e.printStackTrace();
			} 
	
	}	
	
}
