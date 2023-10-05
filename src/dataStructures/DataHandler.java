package dataStructures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class stores the main parameters of the current instance, as the number of satellites, customers..
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class DataHandler {
	
	/**
	 * Dimension (total number of nodes)
	 */
	
	private int dim;
	
	/**
	 * Number of satellites
	 */
	
	private int nbSat;
	
	/**
	 * Number of customers
	 */
	
	private int nbCustomers;
	
	/**
	 * Capacity of first-echelon vehicles
	 */
	
	private int Q1;
	
	/**
	 * Capacity of second-echelon vehicles
	 */
	
	private int Q2;
	
	/**
	 * Fleet of first-echelon vehicles
	 */
	
	private int fleet1;
	
	/**
	 * Fleet of second-echelon vehicles
	 */
	
	private int fleet2;
	
	/**
	 * Max SE vehicle per satellite
	 */
	private int ms;
	
	/**
	 * Largest demand
	 */
	
	private double largest_demand;
	
	/**
	 * Demands
	 */
	private ArrayList<Double> demands;
	
	/**
	 * Demands
	 */
	private ArrayList<Double> handling_costs;
	
	/**
	 * Number of copies for single satellite FE routes
	 */
	
	private int number_of_copies;
	
	// METHODS:
	
	
	/**
	 * This method creates a new DataHandler.
	 * 
	 * @param path: path to the instance.dat file
	 * @throws IOException
	 */
	public DataHandler(String path) throws IOException{
		
		// Read the coordinates of the nodes:
		
			//0. Creates a buffered reader:
			
			BufferedReader buff = new BufferedReader(new FileReader(path));
		
			//1. Skips the first 2 lines:
			
				String line = buff.readLine();
				buff.readLine();
				
			// Captures trucks info: (total #, capacity, cost per distance, fixcost)	
				
				line = buff.readLine();
				String[] attrs = line.split(",");
				
				fleet1 = Integer.parseInt(attrs[0]);
				Q1 = Integer.parseInt(attrs[1]);
				
			// Captures freighters info: (max cf/sat, total #, cap, cost/dist, fixcost)	
				
				line = buff.readLine();
				line = buff.readLine();
				line = buff.readLine();
				attrs = line.split(",");
				
				ms = Integer.parseInt(attrs[0]);
				fleet2 = Integer.parseInt(attrs[1]);
				Q2 = Integer.parseInt(attrs[2]);	
				
				buff.readLine();
				buff.readLine();
				
			// 2. Captures the line with the depot / satellites coordinates:
			
				line = buff.readLine();
			
			// 3. Removes white spaces and splits the line:
				
				line = line.replace("   ",";");
				line = line.replace("  ",";");
				
			// 4. Read the coordinates of the satellite
	
					attrs = line.split(";");
					
					nbSat = attrs.length-1;
					handling_costs = new ArrayList<Double>();
					
					for(int i = 0; i < attrs.length; i++) {
						
						String[] current_coors = attrs[i].split(",");
						if(current_coors.length > 2) {
							handling_costs.add(Double.parseDouble(current_coors[2]));
						}else {
							handling_costs.add(0.0);
						}
					}
			// 5. Read the coordinates of the customers:
					
					line = buff.readLine();
					line = buff.readLine();
					line = buff.readLine();
					line = line.replace("   ",";");
					line = line.replace("  ",";");
					
					attrs = line.split(";");
					
					nbCustomers = attrs.length;
					if(path.contains("Set7")) {
						nbCustomers = attrs.length-2;	
					}
					
					demands = new ArrayList<Double>();
					largest_demand = 0;
					double total_demand = 0;
					for(int i = 0; i < nbCustomers; i++) {
						
						String[] current_coors = attrs[i].split(",");
						demands.add(Double.parseDouble(current_coors[2]));
						if(demands.get(demands.size()-1) > largest_demand) {
							largest_demand = demands.get(demands.size()-1);
						}
						total_demand += demands.get(demands.size()-1);
					}
					setNumber_of_copies((int) Math.ceil(total_demand / Q1));
					
					
			// 5. Closes the buffered reader:
					
			buff.close();
	}

	// Getters / setters:
	
	
	/**
	 * @return the dim
	 */
	public int getDim() {
		return dim;
	}


	/**
	 * @param dim the dim to set
	 */
	public void setDim(int dim) {
		this.dim = dim;
	}


	/**
	 * @return the nbSat
	 */
	public int getNbSat() {
		return nbSat;
	}


	/**
	 * @param nbSat the nbSat to set
	 */
	public void setNbSat(int nbSat) {
		this.nbSat = nbSat;
	}


	/**
	 * @return the nbCustomers
	 */
	public int getNbCustomers() {
		return nbCustomers;
	}


	/**
	 * @param nbCustomers the nbCustomers to set
	 */
	public void setNbCustomers(int nbCustomers) {
		this.nbCustomers = nbCustomers;
	}


	/**
	 * @return the q1
	 */
	public int getQ1() {
		return Q1;
	}


	/**
	 * @param q1 the q1 to set
	 */
	public void setQ1(int q1) {
		Q1 = q1;
	}


	/**
	 * @return the q2
	 */
	public int getQ2() {
		return Q2;
	}


	/**
	 * @param q2 the q2 to set
	 */
	public void setQ2(int q2) {
		Q2 = q2;
	}


	/**
	 * @return the fleet1
	 */
	public int getFleet1() {
		return fleet1;
	}


	/**
	 * @param fleet1 the fleet1 to set
	 */
	public void setFleet1(int fleet1) {
		this.fleet1 = fleet1;
	}


	/**
	 * @return the fleet2
	 */
	public int getFleet2() {
		return fleet2;
	}


	/**
	 * @param fleet2 the fleet2 to set
	 */
	public void setFleet2(int fleet2) {
		this.fleet2 = fleet2;
	}


	/**
	 * @return the ms
	 */
	public int getMs() {
		return ms;
	}


	/**
	 * @param ms the ms to set
	 */
	public void setMs(int ms) {
		this.ms = ms;
	}

	/**
	 * @return the demands
	 */
	public ArrayList<Double> getDemands() {
		return demands;
	}

	/**
	 * @param demands the demands to set
	 */
	public void setDemands(ArrayList<Double> demands) {
		this.demands = demands;
	}

	/**
	 * @return the handling_costs
	 */
	public ArrayList<Double> getHandling_costs() {
		return handling_costs;
	}

	/**
	 * @param handling_costs the handling_costs to set
	 */
	public void setHandling_costs(ArrayList<Double> handling_costs) {
		this.handling_costs = handling_costs;
	}

	/**
	 * @return the largest_demand
	 */
	public double getLargest_demand() {
		return largest_demand;
	}

	/**
	 * @param largest_demand the largest_demand to set
	 */
	public void setLargest_demand(double largest_demand) {
		this.largest_demand = largest_demand;
	}

	public int getNumber_of_copies() {
		return number_of_copies;
	}

	public void setNumber_of_copies(int number_of_copies) {
		this.number_of_copies = number_of_copies;
	}
	
	
	
	
}
