package distanceMatrices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import core.ArrayDistanceMatrix;
import util.EuclideanCalculator;

/**
 * This class implements an instance of a distance matrix, for the 2EVRPinstances
 * 
 * @author nicolas.cabrera-malik
 */
public class DepotToSatellitesDistanceMatrix extends ArrayDistanceMatrix{

	/**
	 * Constructs the distance matrix
	 * @throws IOException 
	 */
	
	public DepotToSatellitesDistanceMatrix(String path) throws IOException {
		
		super();
		
		// Read the coordinates of the nodes:
		
			//0. Creates a buffered reader:
			
			BufferedReader buff = new BufferedReader(new FileReader(path));
		
			//1. Skips the first 8 lines:
			
				String line = buff.readLine();
				buff.readLine();
				buff.readLine();
				buff.readLine();
				buff.readLine();
				buff.readLine();
				buff.readLine();
				buff.readLine();
			
			// 2. Captures the line with the depot / satellites coordinates:
			
				line = buff.readLine();
				
			// 3. Removes white spaces and splits the line:
				
				line = line.replace("   ",";");
				line = line.replace("  ",";");
				
			// 4. Read the coordinates of each node:
				
				//4.1 Initializes all the lists:
				
					ArrayList<Double> xCoors = new ArrayList<Double>();
					ArrayList<Double> yCoors = new ArrayList<Double>();
					
				// 4.2 Stores x and y coordinates
			
					String[] attrs = line.split(";");
					
					for(int i = 0; i < attrs.length; i++) {
						
						String[] current_coors = attrs[i].split(",");
						xCoors.add(Double.parseDouble(current_coors[0]));
						yCoors.add(Double.parseDouble(current_coors[1]));
						
					}

			// 5. Closes the buffered reader:
					
			buff.close();
					
			
		// Number of nodes:
		
		int dimension = xCoors.size();

		// Initializes the distance matrix:
		
		double[][] distances = new double[dimension][dimension];
		
		// Fills the matrix:
		
			//Between customers:
		
			EuclideanCalculator euc = new EuclideanCalculator();
			for(int i = 0; i < dimension; i++) {
				
				for(int j = 0; j < dimension; j++) {
					
					distances[i][j] = euc.calc(xCoors.get(i),yCoors.get(i),xCoors.get(j),yCoors.get(j));
					
				}
				
			}
		
		// Sets the distance matrix:
		
		this.setDistances(distances);
	}
}
