package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import core.ArrayDistanceMatrix;
import core.InsertionHeuristic;
import core.NNHeuristic;
import core.OrderFirstSplitSecondHeuristic;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Split;
import dataStructures.DataHandler;
import distanceMatrices.CenterToCustomersDistanceMatrix;
import distanceMatrices.DepotToCustomersDistanceMatrix;
import distanceMatrices.DepotToSatellitesDistanceMatrix;
import distanceMatrices.SatelliteToCustomersDistanceMatrix;
import globalParameters.GlobalParameters;
import msh.AssemblyFunction;
import msh.CPLEXSetPartitioningSolver;
import msh.GurobiSetPartitioningSolver;
import msh.MSH;
import msh.OrderFirstSplitSecondSampling;
import split.FirstEchelonEnumeration;
import split.FirstEchelonEnumerationSingle;
import split.FirstEchelonSplit;
import split.SecondEchelonSplit;
import split.SecondEchelonSplitAll;
import split.SecondEchelonSplitCapacities;
import split.SecondEchelonSplitLKH;

/**
 * This class contains the main logic of the MSH. 
 * It contains the information of the instance and the method MSH.
 * 
 * @author nicolas.cabrera-malik
 *
 */

public class Solver {

	// Main attributes:
	
		/**
		 * Instance name, for example "Set2a_E-n22-k4-s10-14"
		 */
	
		private String instance_name;
		
		/**
		 * Instance identifier, for example "Set2a_E-n22-k4-s10-14.dat"
		 */
		
		private String instance_identifier;
	
	// Statistics 
	
		/**
		 * CPU time used to initialize the instance
		 */
		private double cpu_initialization;
		
		/**
		 * CPU time used in the sampling phase of MSH
		 */
		private double cpu_msh_sampling;

		/**
		 * CPU time used in the assembly phase of MSH
		 */
		private double cpu_msh_assembly;
	
		
	// Methods:
		
		//------------------------------------------MAIN LOGIC-----------------------------------
		
		/**
		 * This method runs the MSH
		 * @throws IOException
		 */
		public void MSH(String instance_identif)throws IOException {
			
			//0. Store main attributes: 
			
				this.instance_identifier = instance_identif;
				this.instance_name = instance_identif.replace(".dat", "");
			
			//1. Starts the counter for the initialization step:
			
				Double IniTime = (double) System.nanoTime();
				
				if(GlobalParameters.PRINT_IN_CONSOLE) {
					System.out.println("Starting the initialization step...");
				}
				
			//2. Reads the instance 
				
				// Trucks and freighters capacities, and customers demands
				
					DataHandler data = new DataHandler(GlobalParameters.INSTANCE_FOLDER+instance_identifier);
				
				// Depot to satellites matrix
				
					// Matrix:
				
						ArrayDistanceMatrix distances_depot_satellites = null;
						distances_depot_satellites = new DepotToSatellitesDistanceMatrix(GlobalParameters.INSTANCE_FOLDER+instance_identifier);

				// Depot to customers matrix
						
					// Matrix
						ArrayDistanceMatrix distances_depot_customers = null;
						
						if(GlobalParameters.TSP_DEPOT_STARTING_POINT) {
							distances_depot_customers = new DepotToCustomersDistanceMatrix(GlobalParameters.INSTANCE_FOLDER+instance_identifier);
						}else if(GlobalParameters.TSP_CENTROID_STARTING_POINT) {
							distances_depot_customers = new CenterToCustomersDistanceMatrix(GlobalParameters.INSTANCE_FOLDER+instance_identifier);
						}
						
				// Satellites to customers matrix (one per satellite)
				
					// Create an arrayList to store all the matrices:
					
						ArrayList<ArrayDistanceMatrix> distances_satellite_customers = new ArrayList<ArrayDistanceMatrix>();
					
					// Create each distance matrix:
					
						for(int i = 1; i <= data.getNbSat(); i++) {
							
							distances_satellite_customers.add( new SatelliteToCustomersDistanceMatrix(GlobalParameters.INSTANCE_FOLDER+instance_identifier,i));
						
						}
				
			// 3. Initializes all the pool of routes. We will have one pool for each satellite/tspHeuristic
						
				ArrayList<RoutePool> pools = new ArrayList<RoutePool>();			
					
			// 4. Creates an assembler:
				
				AssemblyFunction assembler = null;
				//assembler = new GurobiSetPartitioningSolver(data.getNbCustomers(),data.getNbSat(),true,data);//GUROBI
				
				// If you want to use cplex use this line instead:
				
				assembler = new CPLEXSetPartitioningSolver(data.getNbCustomers(),data.getNbSat(),true,data);//GUROBI
				
				
			// 5. Initializes the MSH:
				
				MSH msh = new MSH(assembler,GlobalParameters.THREADS);	
				
			// 4. Initializes the split algorithm for each satellite: 
				
				ArrayList<Split> splits = new ArrayList<Split>();	
				
				for(int i = 1; i <= data.getNbSat(); i++) {
					
					// Should we add all the routes in the split graphs?
					
					if (GlobalParameters.SPLIT_ADD_ALL) {
						
						splits.add(new SecondEchelonSplitAll(distances_satellite_customers.get(i-1),data.getDemands(),data.getQ2()));
						
						
					}else {
						
						// Should we use the LKH to improve the petals?
						
						if(GlobalParameters.SPLIT_IMPROVE_PETAL_LKH) {
							
							if(GlobalParameters.SPLIT_TRY_CAPACITIES) {
								
								splits.add(new SecondEchelonSplitCapacities(distances_satellite_customers.get(i-1),data.getDemands(),data.getQ2(),data.getLargest_demand()));
								
							}else {
								
								splits.add(new SecondEchelonSplitLKH(distances_satellite_customers.get(i-1),data.getDemands(),data.getQ2()));
								
							}
							
						}else {
							
							if(GlobalParameters.SPLIT_TRY_CAPACITIES) {
								
								splits.add(new SecondEchelonSplitCapacities(distances_satellite_customers.get(i-1),data.getDemands(),data.getQ2(),data.getLargest_demand()));
								
							}else {
								
								splits.add(new SecondEchelonSplit(distances_satellite_customers.get(i-1),data.getDemands(),data.getQ2()));
								
							}
							
						}
						
					}
					
					
				}
				
				Split split_fe = null;
				
				if(data.getNbSat() <= 15) {
				
					split_fe = new FirstEchelonEnumeration(distances_depot_satellites);
					
				}else {
					
					split_fe = new FirstEchelonSplit(distances_depot_satellites);
					
				}
				
			// 5. Calculates the number of iterations:	
				
				int num_iterations = (int)GlobalParameters.MSH_NUM_ITERATIONS/(data.getNbSat()*8);
				if(GlobalParameters.TSP_CENTROID_STARTING_POINT || GlobalParameters.TSP_DEPOT_STARTING_POINT) {
					num_iterations = (int)GlobalParameters.MSH_NUM_ITERATIONS/(data.getNbSat()*12);
				}
				if(data.getNbCustomers() > 200) {
					num_iterations = (int)GlobalParameters.MSH_NUM_ITERATIONS/(data.getNbSat()*4);
				}
				
				int num_iterations_fe = (int)GlobalParameters.MSH_NUM_ITERATIONS/(data.getNbSat()*8);
				if(data.getNbSat() <= 15) {
					num_iterations_fe = 1;
				}
				
			// 6. Set-up of the sampling functions for the first echelon:
				
				if(data.getNbSat() <= 15) {
					
					// Sets the seed for the generation of random numbers:
					
						Random random_nn_fe = new Random(GlobalParameters.SEED);
				
					// RNN:
					
						NNHeuristic nn_fe = new NNHeuristic(distances_depot_satellites);
						nn_fe.setRandomized(true);
						nn_fe.setRandomGen(random_nn_fe);
						nn_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
						nn_fe.setInitNode(0);
							
					// Set up heuristics:
					
						OrderFirstSplitSecondHeuristic nn_h_fe = new OrderFirstSplitSecondHeuristic(nn_fe, split_fe);	
					
					// Creates sampling functions:
					
					
						OrderFirstSplitSecondSampling f_nn_fe = new OrderFirstSplitSecondSampling(nn_h_fe,num_iterations_fe,"Enumerator");
					
					// Creates the route pools:
					
						RoutePool pool_nn_fe = new RoutePool(0);
					
					// Adds the pools:
					
						pools.add(pool_nn_fe);
					
					// Sets the route pools for each heuristic:
					
						f_nn_fe.setRoutePool(pool_nn_fe);
					
						
					// Adds the sampling function:
						
						msh.addSamplingFunction(f_nn_fe);
						
					for(int i = 1 ; i < data.getNumber_of_copies(); i++) {
					
						 Split split_fe_duplicates = new FirstEchelonEnumerationSingle(distances_depot_satellites);
						
						// Sets the seed for the generation of random numbers:
							
							Random random_nn_fe_duplicates = new Random(GlobalParameters.SEED);
						
						// RNN:
						
							NNHeuristic nn_fe_duplicates = new NNHeuristic(distances_depot_satellites);
							nn_fe_duplicates.setRandomized(true);
							nn_fe_duplicates.setRandomGen(random_nn_fe_duplicates);
							nn_fe_duplicates.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
							nn_fe_duplicates.setInitNode(0);
							
						// Set up heuristics:
						
							OrderFirstSplitSecondHeuristic nn_h_fe_duplicates = new OrderFirstSplitSecondHeuristic(nn_fe_duplicates, split_fe_duplicates);	
						
						// Creates sampling functions:
						
						
							OrderFirstSplitSecondSampling f_nn_fe_duplicates = new OrderFirstSplitSecondSampling(nn_h_fe_duplicates,num_iterations_fe,"Enumerator_single");
						
						// Creates the route pools:
						
							RoutePool pool_nn_fe_duplicates = new RoutePool(0);
						
						// Adds the pools:
						
							pools.add(pool_nn_fe_duplicates);
						
						// Sets the route pools for each heuristic:
						
							f_nn_fe_duplicates.setRoutePool(pool_nn_fe_duplicates);
						
							
						// Adds the sampling function:
							
							msh.addSamplingFunction(f_nn_fe_duplicates);
			
					}	
				
			}else {
					
					// Sets the seed for the generation of random numbers:
					
					Random random_nn_fe = new Random(GlobalParameters.SEED+10);
					Random random_ni_fe = new Random(GlobalParameters.SEED+20);
					Random random_fi_fe = new Random(GlobalParameters.SEED+30);
					Random random_bi_fe = new Random(GlobalParameters.SEED+40);
					
					Random random_nn_2_fe = new Random(GlobalParameters.SEED+50);
					Random random_ni_2_fe = new Random(GlobalParameters.SEED+60);
					Random random_fi_2_fe = new Random(GlobalParameters.SEED+70);
					Random random_bi_2_fe = new Random(GlobalParameters.SEED+80);
					
				// Initializes the tsp heuristics:
				
					// RNN:
					
						NNHeuristic nn_fe = new NNHeuristic(distances_depot_satellites);
						nn_fe.setRandomized(true);
						nn_fe.setRandomGen(random_nn_fe);
						nn_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
						nn_fe.setInitNode(0);
						
						NNHeuristic nn_2_fe = new NNHeuristic(distances_depot_satellites);
						nn_2_fe.setRandomized(true);
						nn_2_fe.setRandomGen(random_nn_2_fe);
						nn_2_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
						nn_2_fe.setInitNode(0);
			
					// RNI:
				
						InsertionHeuristic ni_fe = new InsertionHeuristic(distances_depot_satellites,"NEAREST_INSERTION");
						ni_fe.setRandomized(true);
						ni_fe.setRandomGen(random_ni_fe);
						ni_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
						ni_fe.setInitNode(0);
						
						InsertionHeuristic ni_2_fe = new InsertionHeuristic(distances_depot_satellites,"NEAREST_INSERTION");
						ni_2_fe.setRandomized(true);
						ni_2_fe.setRandomGen(random_ni_2_fe);
						ni_2_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
						ni_2_fe.setInitNode(0);
				
					// RNI:
				
						InsertionHeuristic fi_fe = new InsertionHeuristic(distances_depot_satellites,"FARTHEST_INSERTION");
						fi_fe.setRandomized(true);
						fi_fe.setRandomGen(random_fi_fe);
						fi_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
						fi_fe.setInitNode(0);
				
						InsertionHeuristic fi_2_fe = new InsertionHeuristic(distances_depot_satellites,"FARTHEST_INSERTION");
						fi_2_fe.setRandomized(true);
						fi_2_fe.setRandomGen(random_fi_2_fe);
						fi_2_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
						fi_2_fe.setInitNode(0);
				
					// BI:
				
						InsertionHeuristic bi_fe = new InsertionHeuristic(distances_depot_satellites,"BEST_INSERTION");
						bi_fe.setRandomized(true);
						bi_fe.setRandomGen(random_bi_fe);
						bi_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
						bi_fe.setInitNode(0);
						
						InsertionHeuristic bi_2_fe = new InsertionHeuristic(distances_depot_satellites,"BEST_INSERTION");
						bi_2_fe.setRandomized(true);
						bi_2_fe.setRandomGen(random_bi_2_fe);
						bi_2_fe.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
						bi_2_fe.setInitNode(0);
							
				
				// Set up heuristics:
				
					OrderFirstSplitSecondHeuristic nn_h_fe = new OrderFirstSplitSecondHeuristic(nn_fe, split_fe);	
					OrderFirstSplitSecondHeuristic ni_h_fe = new OrderFirstSplitSecondHeuristic(ni_fe, split_fe);
					OrderFirstSplitSecondHeuristic fi_h_fe = new OrderFirstSplitSecondHeuristic(fi_fe, split_fe);
					OrderFirstSplitSecondHeuristic bi_h_fe = new OrderFirstSplitSecondHeuristic(bi_fe, split_fe);
					
					OrderFirstSplitSecondHeuristic nn_2h_fe = new OrderFirstSplitSecondHeuristic(nn_2_fe, split_fe);
					OrderFirstSplitSecondHeuristic ni_2h_fe = new OrderFirstSplitSecondHeuristic(ni_2_fe, split_fe);
					OrderFirstSplitSecondHeuristic fi_2h_fe = new OrderFirstSplitSecondHeuristic(fi_2_fe, split_fe);
					OrderFirstSplitSecondHeuristic bi_2h_fe = new OrderFirstSplitSecondHeuristic(bi_2_fe, split_fe);
					
				// Creates sampling functions:
						
						
					OrderFirstSplitSecondSampling f_nn_fe = new OrderFirstSplitSecondSampling(nn_h_fe,num_iterations_fe,("rnn_fe_high"));
					OrderFirstSplitSecondSampling f_ni_fe = new OrderFirstSplitSecondSampling(ni_h_fe,num_iterations_fe,("rni_fe_high"));
					OrderFirstSplitSecondSampling f_fi_fe = new OrderFirstSplitSecondSampling(fi_h_fe,num_iterations_fe,("rfi_fe_high"));
					OrderFirstSplitSecondSampling f_bi_fe = new OrderFirstSplitSecondSampling(bi_h_fe,num_iterations_fe,("rbi_fe_high"));
						
					OrderFirstSplitSecondSampling f_nn_2_fe = new OrderFirstSplitSecondSampling(nn_2h_fe,num_iterations_fe,("rnn_fe_low"));
					OrderFirstSplitSecondSampling f_ni_2_fe = new OrderFirstSplitSecondSampling(ni_2h_fe,num_iterations_fe,("rni_fe_low"));
					OrderFirstSplitSecondSampling f_fi_2_fe = new OrderFirstSplitSecondSampling(fi_2h_fe,num_iterations_fe,("rfi_fe_low"));
					OrderFirstSplitSecondSampling f_bi_2_fe = new OrderFirstSplitSecondSampling(bi_2h_fe,num_iterations_fe,("rbi_fe_low"));
						
				// Creates the route pools:
						
					RoutePool pool_nn_fe = new RoutePool(0);
					RoutePool pool_ni_fe = new RoutePool(0);
					RoutePool pool_fi_fe = new RoutePool(0);
					RoutePool pool_bi_fe = new RoutePool(0);
						
					RoutePool pool_nn_2_fe = new RoutePool(0);
					RoutePool pool_ni_2_fe = new RoutePool(0);
					RoutePool pool_fi_2_fe = new RoutePool(0);
					RoutePool pool_bi_2_fe = new RoutePool(0);
				
				// Adds the pools:
					
					pools.add(pool_nn_fe);
					pools.add(pool_ni_fe);
					pools.add(pool_fi_fe);
					pools.add(pool_bi_fe);
						
					pools.add(pool_nn_2_fe);
					pools.add(pool_ni_2_fe);
					pools.add(pool_fi_2_fe);
					pools.add(pool_bi_2_fe);
			
				// Sets the route pools for each heuristic:
					
					f_nn_fe.setRoutePool(pool_nn_fe);
					f_ni_fe.setRoutePool(pool_ni_fe);
					f_fi_fe.setRoutePool(pool_fi_fe);
					f_bi_fe.setRoutePool(pool_bi_fe);
						
					f_nn_2_fe.setRoutePool(pool_nn_2_fe);
					f_ni_2_fe.setRoutePool(pool_ni_2_fe);
					f_fi_2_fe.setRoutePool(pool_fi_2_fe);
					f_bi_2_fe.setRoutePool(pool_bi_2_fe);
						
				// Adds the sampling function:
						
					msh.addSamplingFunction(f_nn_fe);
					msh.addSamplingFunction(f_ni_fe);
					msh.addSamplingFunction(f_fi_fe);
					msh.addSamplingFunction(f_bi_fe);
					
					msh.addSamplingFunction(f_nn_2_fe);
					msh.addSamplingFunction(f_ni_2_fe);
					msh.addSamplingFunction(f_bi_2_fe);
					msh.addSamplingFunction(f_fi_2_fe);
			
					
			}
				
		// END FIRST ECHELON ! -------
			
		// START SECOND ECHELON ! -----------
				
			// 7. Set-up of the sampling functions for the second echelon:
				
				// For each satellite add the sampling functions with a high level of randomization:
				
					for(int i = 1; i <= data.getNbSat(); i++) {
						
						// Sets the seed for the generation of random numbers:
						
							Random random_nn = new Random(GlobalParameters.SEED+90+1000*i);
							Random random_ni = new Random(GlobalParameters.SEED+100+1000*i);
							Random random_fi = new Random(GlobalParameters.SEED+110+1000*i);
							Random random_bi = new Random(GlobalParameters.SEED+120+1000*i);
							
						// Initializes the tsp heuristics:
						
								// RNN:
								
								NNHeuristic nn = new NNHeuristic(distances_satellite_customers.get(i-1));
								nn.setRandomized(true);
								nn.setRandomGen(random_nn);
								nn.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
								nn.setInitNode(0);
								
								
							// RNI:
								
								InsertionHeuristic ni = new InsertionHeuristic(distances_satellite_customers.get(i-1),"NEAREST_INSERTION");
								ni.setRandomized(true);
								ni.setRandomGen(random_ni);
								ni.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
								ni.setInitNode(0);
								
								
							// RNI:
								
								InsertionHeuristic fi = new InsertionHeuristic(distances_satellite_customers.get(i-1),"FARTHEST_INSERTION");
								fi.setRandomized(true);
								fi.setRandomGen(random_fi);
								fi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
								fi.setInitNode(0);
							
								
							// BI:
								
								InsertionHeuristic bi = new InsertionHeuristic(distances_satellite_customers.get(i-1),"BEST_INSERTION");
								bi.setRandomized(true);
								bi.setRandomGen(random_bi);
								bi.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_HIGH);
								bi.setInitNode(0);
								
								
						// Set up heuristics:
								
							OrderFirstSplitSecondHeuristic nn_h = new OrderFirstSplitSecondHeuristic(nn, splits.get(i-1));	
							OrderFirstSplitSecondHeuristic ni_h = new OrderFirstSplitSecondHeuristic(ni, splits.get(i-1));
							OrderFirstSplitSecondHeuristic fi_h = new OrderFirstSplitSecondHeuristic(fi, splits.get(i-1));
							OrderFirstSplitSecondHeuristic bi_h = new OrderFirstSplitSecondHeuristic(bi, splits.get(i-1));
							
							
						// Creates sampling functions:
								
								
							OrderFirstSplitSecondSampling f_nn = new OrderFirstSplitSecondSampling(nn_h,num_iterations,("rnn_se_high_"+i));
							OrderFirstSplitSecondSampling f_ni = new OrderFirstSplitSecondSampling(ni_h,num_iterations,("rni_se_high_"+i));
							OrderFirstSplitSecondSampling f_fi = new OrderFirstSplitSecondSampling(fi_h,num_iterations,("rfi_se_high_"+i));
							OrderFirstSplitSecondSampling f_bi = new OrderFirstSplitSecondSampling(bi_h,num_iterations,("rbi_se_high_"+i));
							
						// Creates the route pools:
								
							RoutePool pool_nn = new RoutePool(i);
							RoutePool pool_ni = new RoutePool(i);
							RoutePool pool_fi = new RoutePool(i);
							RoutePool pool_bi = new RoutePool(i);
								
						// Adds the pools:
							
							pools.add(pool_nn);
							pools.add(pool_ni);
							pools.add(pool_fi);
							pools.add(pool_bi);
							
						// Sets the route pools for each heuristic:
							
							f_nn.setRoutePool(pool_nn);
							f_ni.setRoutePool(pool_ni);
							f_fi.setRoutePool(pool_fi);
							f_bi.setRoutePool(pool_bi);
								
							
						// Adds the sampling function:
								
							msh.addSamplingFunction(f_nn);
							msh.addSamplingFunction(f_ni);
							msh.addSamplingFunction(f_fi);
							msh.addSamplingFunction(f_bi);
							
					}
					
					if(data.getNbCustomers() <= 200) {
						
						// For each satellite add the sampling functions with a low level of randomization:
						
						for(int i = 1; i <= data.getNbSat(); i++) {
							
							// Sets the seed for the generation of random numbers:
							
								Random random_nn_2 = new Random(GlobalParameters.SEED+130+1000*i);
								Random random_ni_2 = new Random(GlobalParameters.SEED+140+1000*i);
								Random random_fi_2 = new Random(GlobalParameters.SEED+150+1000*i);
								Random random_bi_2 = new Random(GlobalParameters.SEED+160+1000*i);
								
							// Initializes the tsp heuristics:
							
									// RNN:
									
									NNHeuristic nn_2 = new NNHeuristic(distances_satellite_customers.get(i-1));
									nn_2.setRandomized(true);
									nn_2.setRandomGen(random_nn_2);
									nn_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									nn_2.setInitNode(0);
								
								// RNI:
		
									InsertionHeuristic ni_2 = new InsertionHeuristic(distances_satellite_customers.get(i-1),"NEAREST_INSERTION");
									ni_2.setRandomized(true);
									ni_2.setRandomGen(random_ni_2);
									ni_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									ni_2.setInitNode(0);	
									
								// RNI:
									
									InsertionHeuristic fi_2 = new InsertionHeuristic(distances_satellite_customers.get(i-1),"FARTHEST_INSERTION");
									fi_2.setRandomized(true);
									fi_2.setRandomGen(random_fi_2);
									fi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									fi_2.setInitNode(0);
									
								// BI:
									
									InsertionHeuristic bi_2 = new InsertionHeuristic(distances_satellite_customers.get(i-1),"BEST_INSERTION");
									bi_2.setRandomized(true);
									bi_2.setRandomGen(random_bi_2);
									bi_2.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									bi_2.setInitNode(0);
									
							// Set up heuristics:
								
								OrderFirstSplitSecondHeuristic nn_2h = new OrderFirstSplitSecondHeuristic(nn_2, splits.get(i-1));
								OrderFirstSplitSecondHeuristic ni_2h = new OrderFirstSplitSecondHeuristic(ni_2, splits.get(i-1));
								OrderFirstSplitSecondHeuristic fi_2h = new OrderFirstSplitSecondHeuristic(fi_2, splits.get(i-1));
								OrderFirstSplitSecondHeuristic bi_2h = new OrderFirstSplitSecondHeuristic(bi_2, splits.get(i-1));
								
							// Creates sampling functions:
									
								OrderFirstSplitSecondSampling f_nn_2 = new OrderFirstSplitSecondSampling(nn_2h,num_iterations,("rnn_se_low_"+i));
								OrderFirstSplitSecondSampling f_ni_2 = new OrderFirstSplitSecondSampling(ni_2h,num_iterations,("rni_se_low_"+i));
								OrderFirstSplitSecondSampling f_fi_2 = new OrderFirstSplitSecondSampling(fi_2h,num_iterations,("rfi_se_low_"+i));
								OrderFirstSplitSecondSampling f_bi_2 = new OrderFirstSplitSecondSampling(bi_2h,num_iterations,("rbi_se_low_"+i));
								
							// Creates the route pools:
									
								RoutePool pool_nn_2 = new RoutePool(i);
								RoutePool pool_ni_2 = new RoutePool(i);
								RoutePool pool_fi_2 = new RoutePool(i);
								RoutePool pool_bi_2 = new RoutePool(i);
									
							// Adds the pools:
									
								pools.add(pool_nn_2);
								pools.add(pool_ni_2);
								pools.add(pool_fi_2);
								pools.add(pool_bi_2);
								
							// Sets the route pools for each heuristic:
									
								f_nn_2.setRoutePool(pool_nn_2);
								f_ni_2.setRoutePool(pool_ni_2);
								f_fi_2.setRoutePool(pool_fi_2);
								f_bi_2.setRoutePool(pool_bi_2);
									
							// Adds the sampling function:
									
								msh.addSamplingFunction(f_nn_2);
								msh.addSamplingFunction(f_ni_2);
								msh.addSamplingFunction(f_bi_2);
								msh.addSamplingFunction(f_fi_2);

						}
						
					}
					
					
					//Check if we should add the additional sampling functions in which the distance matrix
					// used by the tsp is not the same that the one used by the split.
					
					if((GlobalParameters.TSP_CENTROID_STARTING_POINT || GlobalParameters.TSP_DEPOT_STARTING_POINT) && data.getNbCustomers() <= 200) {
						
						// For each satellite add the sampling functions with a low level of randomization:
						
						for(int i = 1; i <= data.getNbSat(); i++) {
							
							// Sets the seed for the generation of random numbers:

								Random random_nn_3 = new Random(GlobalParameters.SEED+170+1000*i);
								Random random_ni_3 = new Random(GlobalParameters.SEED+180+1000*i);
								Random random_fi_3 = new Random(GlobalParameters.SEED+190+1000*i);
								Random random_bi_3 = new Random(GlobalParameters.SEED+200+1000*i);
								
							// Initializes the tsp heuristics:
							
									// RNN:

									NNHeuristic nn_3 = new NNHeuristic(distances_depot_customers); //Here we use the depot to customers matrix
									nn_3.setRandomized(true);
									nn_3.setRandomGen(random_nn_3);
									nn_3.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									nn_3.setInitNode(0);
									
								// RNI:

									InsertionHeuristic ni_3 = new InsertionHeuristic(distances_depot_customers,"NEAREST_INSERTION");
									ni_3.setRandomized(true);
									ni_3.setRandomGen(random_ni_3);
									ni_3.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									ni_3.setInitNode(0);
									
								// RNI:
								
									InsertionHeuristic fi_3 = new InsertionHeuristic(distances_depot_customers,"FARTHEST_INSERTION");
									fi_3.setRandomized(true);
									fi_3.setRandomGen(random_fi_3);
									fi_3.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									fi_3.setInitNode(0);
									
								// BI:
									
									InsertionHeuristic bi_3 = new InsertionHeuristic(distances_depot_customers,"BEST_INSERTION");
									bi_3.setRandomized(true);
									bi_3.setRandomGen(random_bi_3);
									bi_3.setRandomizationFactor(GlobalParameters.MSH_RANDOM_FACTOR_LOW);
									bi_3.setInitNode(0);
										
									
							// Set up heuristics:
									
								OrderFirstSplitSecondHeuristic nn_3h = new OrderFirstSplitSecondHeuristic(nn_3, splits.get(i-1));
								OrderFirstSplitSecondHeuristic ni_3h = new OrderFirstSplitSecondHeuristic(ni_3, splits.get(i-1));
								OrderFirstSplitSecondHeuristic fi_3h = new OrderFirstSplitSecondHeuristic(fi_3, splits.get(i-1));
								OrderFirstSplitSecondHeuristic bi_3h = new OrderFirstSplitSecondHeuristic(bi_3, splits.get(i-1));
								
							// Creates sampling functions:
									
								OrderFirstSplitSecondSampling f_nn_3 = new OrderFirstSplitSecondSampling(nn_3h,num_iterations,("rnn_sedep_low_"+i));
								OrderFirstSplitSecondSampling f_ni_3 = new OrderFirstSplitSecondSampling(ni_3h,num_iterations,("rni_sedep_low_"+i));
								OrderFirstSplitSecondSampling f_fi_3 = new OrderFirstSplitSecondSampling(fi_3h,num_iterations,("rfi_sedep_low_"+i));
								OrderFirstSplitSecondSampling f_bi_3 = new OrderFirstSplitSecondSampling(bi_3h,num_iterations,("rbi_sedep_low_"+i));
								
							// Creates the route pools:

								RoutePool pool_nn_3 = new RoutePool(i);
								RoutePool pool_ni_3 = new RoutePool(i);
								RoutePool pool_fi_3 = new RoutePool(i);
								RoutePool pool_bi_3 = new RoutePool(i);
									
							// Adds the pools:

								pools.add(pool_nn_3);
								pools.add(pool_ni_3);
								pools.add(pool_fi_3);
								pools.add(pool_bi_3);
								
							// Sets the route pools for each heuristic:

								f_nn_3.setRoutePool(pool_nn_3);
								f_ni_3.setRoutePool(pool_ni_3);
								f_fi_3.setRoutePool(pool_fi_3);
								f_bi_3.setRoutePool(pool_bi_3);
									
							// Adds the sampling function:

								msh.addSamplingFunction(f_nn_3);
								msh.addSamplingFunction(f_ni_3);
								msh.addSamplingFunction(f_bi_3);
								msh.addSamplingFunction(f_fi_3);
								
						}
					
					}
		//8. Stops the clock for the initialization time:
					
			Double FinTime = (double) System.nanoTime();
			cpu_initialization = (FinTime-IniTime)/1000000000;
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				System.out.println("End of the initialization step...");
			}
			
		//9. Sets the pools:
			
			msh.setPools(pools);
			
		//10. Runs the assembly phase of MSH:
			
			Double IniTime_msh = (double) System.nanoTime();
			
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				System.out.println("Start of the sampling step...");
			}
			
			msh.run_sampling();

			
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				System.out.println("End of the sampling step...");
			}
			
			Double FinTime_msh = (double) System.nanoTime();
			
			cpu_msh_sampling = (FinTime_msh-IniTime_msh)/1000000000;
			
		//10.5 Creates copies of every second echelon route with different satellites. 
			
			if(GlobalParameters.SPLIT_DUPLICATE_SATELLITE_ROUTES) {
				
				// Step 1: Create a new pool for each satellite
				
				ArrayList<RoutePool> pools_for_each_satellite = new ArrayList<RoutePool>();			
				for(int i = 1; i <= data.getNbSat(); i++) {
					
					RoutePool pool = new RoutePool(i);
					pools_for_each_satellite.add(pool);
					
				}
				
			// Step 2: Iterate over the existing pools:
				
				for(RoutePool pool : pools) {
					
					// Step 3: Verify that the pool corresponds to a second echelon pool:
					
					if(pool.getSatellite() > 0) {
						
						// Step 4: Iterate over the routes of the pool:
						
						Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							// Step 5: Recover the current route:
							
								Route r = iterator.next();
								
							// Step 6: Store the current cost:
								
								double cost = (double) r.getAttribute(RouteAttribute.COST);
								
							// Step 7: Calculate the decrease in cost:
								
								double decrease = distances_satellite_customers.get(pool.getSatellite()-1).getDistance(0,r.get(1)) + distances_satellite_customers.get(pool.getSatellite()-1).getDistance(r.get(r.size()-2),0);
								
							// For the other satellites:
								
								for(int i = 1; i <= data.getNbSat(); i++) {
									
									if(i != pool.getSatellite()) {
										
										// Step 8: Calculate the increase in cost:
										
										double increase = distances_satellite_customers.get(i-1).getDistance(0,r.get(1)) + distances_satellite_customers.get(i-1).getDistance(r.get(r.size()-2),0);
									
										// Step 9: Compute the new cost:
										
										double new_cost = cost + increase - decrease;
										
										// Step 10: Creates a new route:
										
											Route r_copy = r.getCopy();
											r_copy.setAttribute(RouteAttribute.COST, new_cost);
											
										// Adds the route to the pool of the satellite:
											
											pools_for_each_satellite.get(i-1).add(r_copy);
											
									}
								
								}
								
								
						}
						
						
					}
					
				}
				
				// Final step: Adds every pool to the pools that is used by msh:
				
				for(int i = 1; i <= data.getNbSat(); i++) {
					
					pools.add(pools_for_each_satellite.get(i-1));

				}
			}
			
			
		//11. Runs the assembly phase of MSH:
			
			IniTime_msh = (double) System.nanoTime();
			
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				System.out.println("Start of the assembly step...");
			}
			
			msh.run_assembly();
			
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				System.out.println("End of the assembly step...");
			}
			
			FinTime_msh = (double) System.nanoTime();
			
			cpu_msh_assembly = (FinTime_msh-IniTime_msh)/1000000000;
			
		// 12. Print summary
			
			printSummary(msh,assembler,data);
						
		// 13. Print solution
			
			printSolution(msh,assembler,data);
			
		
		}
		
		
	/**
	 * This method prints the solution in console and in a txt file
	 * @param msh
	 * @param assembler
	 */
	public void printSolution(MSH msh, AssemblyFunction assembler,DataHandler data) {
		
		// 1. Defines the path for the txt file:
		
			String path = globalParameters.GlobalParameters.RESULT_FOLDER+ "Solution_" + instance_name +"_"+GlobalParameters.SEED+".txt";
			String path_arcs = globalParameters.GlobalParameters.RESULT_FOLDER+ "Arcs_" + instance_name +"_"+GlobalParameters.SEED+ ".txt";
			
		// 2. Prints the txt file:
			
			try {
							
				// Creates the print writer:
							
					PrintWriter pw = new PrintWriter(new File(path));
					PrintWriter pw_arcs = new PrintWriter(new File(path_arcs));
							
					System.out.println("-----------------------------------------------");
					System.out.println("Total cost: "+assembler.objectiveFunction);
					System.out.println("Routes:");	
					
				// Prints each of the selected first echelon routes:
					
					int counter = 0;	
					
					System.out.println("\t First echelon:");	
					
					for(Route r : assembler.solution_fe) {
						
						// Build the "Route":
						
						String route = "";
						route += r.getRoute().get(0)+" -> ";
						pw_arcs.println(r.getRoute().get(0)+";"+r.getRoute().get(1)+";"+1+";"+counter);

						for(int pos=1;pos < r.getRoute().size()-1;pos++) {
							
							route += r.getRoute().get(pos)+"("+assembler.solution_fe_drops.get(counter).get(pos-1)+")"+" -> ";
							pw_arcs.println(r.getRoute().get(pos)+";"+r.getRoute().get(pos+1)+";"+1+";"+counter);

						}
						
						route += r.getRoute().get(0);
						
					// Print the ring:
					
						pw.println(counter+" - "+route+" - "+r.getAttribute(RouteAttribute.COST));
						
						System.out.println("\t\t Route "+counter+": "+route+" - Cost: "+r.getAttribute(RouteAttribute.COST));
						
					// Update the counter:
						
						counter++;
					}
					
				// Prints each of the selected first echelon routes:
						
					System.out.println("\t Second echelon:");	
					
					int counter_se = 0;
					
					for(Route r : assembler.solution_se) {
						
						// Build the "Route":
						
						String route = "";
						route += assembler.solution_se_satellites.get(counter_se)+" -> ";
						pw_arcs.println(assembler.solution_se_satellites.get(counter_se)+";"+(r.getRoute().get(1)+data.getNbSat())+";"+2+";"+counter);
						
						for(int pos=1;pos < r.getRoute().size()-1;pos++) {
							
							if(pos<r.getRoute().size()-2) {
								pw_arcs.println((r.getRoute().get(pos)+data.getNbSat())+";"+(r.getRoute().get(pos+1)+data.getNbSat())+";"+2+";"+counter);
								route += (r.getRoute().get(pos)+data.getNbSat())+"("+data.getDemands().get(r.getRoute().get(pos)-1)+")"+" -> ";
								
							}else {
								pw_arcs.println((r.getRoute().get(pos)+data.getNbSat())+";"+assembler.solution_se_satellites.get(counter_se)+";"+2+";"+counter);
								route += (r.getRoute().get(pos)+data.getNbSat())+"("+data.getDemands().get(r.getRoute().get(pos)-1)+")"+" -> ";
								
							}
							
						}
						
						route += assembler.solution_se_satellites.get(counter_se);
						
					// Print the ring:
					
						pw.println(counter+" - "+route+" - "+r.getAttribute(RouteAttribute.COST));
						
						System.out.println("\t\t Route "+counter_se+" created by: "+assembler.solution_se_identifiers.get(counter_se)+" is: "+route+" - Cost: "+r.getAttribute(RouteAttribute.COST)+" - Load: "+r.getAttribute(RouteAttribute.LOAD));
						
							
					// Update the counter:
						
						counter++;
						counter_se++;
						
					}
					System.out.println("-----------------------------------------------");
					
			// Close the print writers:
					
				pw.close();
				pw_arcs.close();
					
			}catch(Exception e) {
				System.out.println("Error while printing the final solution");
			}
	}
	
	
	/**
	 * This method prints the summary in console and in a txt file
	 * @param msh
	 * @param assembler
	 */
	public void printSummary(MSH msh, AssemblyFunction assembler,DataHandler data) {
	
		// 1. Defines the path for the txt file:
		
			String path = globalParameters.GlobalParameters.RESULT_FOLDER+ "Summary_" + instance_name +"_"+GlobalParameters.SEED+ ".txt";
		
		// 2. Prints the txt file:
			
			try {
				
				// Creates the print writer:
				
					PrintWriter pw = new PrintWriter(new File(path));
					
				// Prints relevant information:
					
					pw.println("Instance;"+instance_name);
					pw.println("Instance_s;"+data.getNbSat());
					pw.println("Instance_n;"+data.getNbCustomers());
					pw.println("Instance_Q1;"+data.getQ1());
					pw.println("Instance_Q2;"+data.getQ2());
					pw.println("Instance_K1;"+data.getFleet1());
					pw.println("Instance_K2;"+data.getFleet2());
					pw.println("Seed;"+GlobalParameters.SEED);
					pw.println("AddingAllRoutesInTheSplit;"+GlobalParameters.SPLIT_ADD_ALL);
					pw.println("UsingLKHToImproveRoutes;"+GlobalParameters.SPLIT_IMPROVE_PETAL_LKH);
					pw.println("InitializationTime(s);"+cpu_initialization);
					pw.println("TotalTime(s);"+(cpu_msh_sampling+cpu_msh_assembly));
					pw.println("TotalDistance;"+assembler.objectiveFunction);
					pw.println("NumberOfFERoutes;"+assembler.solution_fe.size());
					pw.println("NumberOfSERoutes;"+assembler.solution_se.size());
					pw.println("Iterations;"+msh.getNumberOfIterations());
					pw.println("SizeOfPool;"+msh.getPoolSize());
					pw.println("SamplingTime(s);"+cpu_msh_sampling);
					pw.println("AssemblyTime(s);"+cpu_msh_assembly);
				
					System.out.println("-----------------------------------------------");
					
					System.out.println("Instance: "+instance_name);
					System.out.println("Instance_s: "+data.getNbSat());
					System.out.println("Instance_n: "+data.getNbCustomers());
					System.out.println("Instance_Q1: "+data.getQ1());
					System.out.println("Instance_Q2: "+data.getQ2());
					System.out.println("Instance_K1: "+data.getFleet1());
					System.out.println("Instance_K2: "+data.getFleet2());
					System.out.println("Seed: "+GlobalParameters.SEED);
					System.out.println("AddingAllRoutesInTheSplit: "+GlobalParameters.SPLIT_ADD_ALL);
					System.out.println("UsingLKHToImproveRoutes: "+GlobalParameters.SPLIT_IMPROVE_PETAL_LKH);
					System.out.println("CreatingCopiesOfRoutes: "+GlobalParameters.SPLIT_DUPLICATE_SATELLITE_ROUTES);
					System.out.println("TSPDiversificationStrategy: "+(GlobalParameters.TSP_CENTROID_STARTING_POINT || GlobalParameters.TSP_DEPOT_STARTING_POINT));
					System.out.println("InitializationTime(s): "+cpu_initialization);
					System.out.println("TotalTime(s): "+(cpu_msh_sampling+cpu_msh_assembly));
					System.out.println("TotalDistance: "+assembler.objectiveFunction);
					System.out.println("NumberOfFERoutes: "+assembler.solution_fe.size());
					System.out.println("NumberOfSERoutes: "+assembler.solution_se.size());
					System.out.println("Iterations: "+msh.getNumberOfIterations());
					System.out.println("SizeOfPool: "+msh.getPoolSize());
					System.out.println("SamplingTime(s): "+cpu_msh_sampling);
					System.out.println("AssemblyTime(s): "+cpu_msh_assembly);
						
					
				// Closes the print writer:
				
					pw.close();
					
			}catch(Exception e) {
				System.out.println("Mistake printing the summary");
				e.printStackTrace();
			}	
	}
}
