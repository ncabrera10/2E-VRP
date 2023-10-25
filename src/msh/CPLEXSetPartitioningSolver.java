package msh;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import clusters.KMeans;
import clusters.PartitionClustering;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import core.ArrayDistanceMatrix;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Solution;
import dataStructures.Cluster;
import dataStructures.DataHandler;
import globalParameters.GlobalParameters;


/**
 * This class solves the set partitioning model to assembly the final solution. 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class CPLEXSetPartitioningSolver extends AssemblyFunction{

	protected int nRequests;
	protected int nSatellites;
	protected boolean hasTerminals;
	protected IloCplex cplex;
	protected IloNumVar[] x;
	protected IloNumVar[] y;
	protected IloNumVar[] f;
	protected DataHandler data;

	public CPLEXSetPartitioningSolver(int nRequests, int nSatellites,boolean hasTerminals,DataHandler data){
		this.nRequests=nRequests;
		this.nSatellites=nSatellites;
		this.hasTerminals=hasTerminals;
		this.data=data;
	}

	@Override
	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools,ArrayDistanceMatrix distances_customers, ArrayList<ArrayDistanceMatrix> distances_satellite_customers) {
		
		// PROCEDURE TO REMOVE DUPLICATES:
		
		// Create a pool for satellite:
		
			ArrayList<RoutePool> newPools = new ArrayList<RoutePool>();
			
			for(int i = 1; i <= nSatellites; i++) {
				
				newPools.add(new RoutePool(i));
				newPools.get(newPools.size()-1).setIdentifier("Unknown");
			}
			
		 //Total number of routes:
			
			int size_fe = 0;
			int size_se = 0;
			
		// Populate these new pools:
		
			for(RoutePool pool : pools) {
				
				if(pool.getSatellite() > 0) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						newPools.get(pool.getSatellite()-1).add(r);
					}
					
				}else {
					size_fe += pool.size();	
				}
				
			}
		
		for(RoutePool pool : newPools) {
				size_se += pool.size();
		}
		
		
		if(GlobalParameters.PRINT_POOLS_TO_FILE) {
			String path = "./output/"+ "Pool"+ ".txt";
			try {
				PrintWriter pw = new PrintWriter(new File(path));
				for(RoutePool pool : pools) {
					if(pool.getSatellite() == 0) {
						pw.println("-----------------");
						Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							Route r = iterator.next();
							pw.println(r.toString()+" - "+r.getAttribute(RouteAttribute.COST)+" - "+r.getAttribute(RouteAttribute.LOAD));
						}
					}
				}
				for(RoutePool pool : newPools) {
					pw.println("-----------------");
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
						
						Route r = iterator.next();
						pw.println(r.toString()+" - "+r.getAttribute(RouteAttribute.COST)+" - "+r.getAttribute(RouteAttribute.LOAD));
					}
				}
				pw.close();
			}catch(Exception e) {
				System.out.println("Error printing the pools");
			}
			
		}
		
		
		// Cplex model:
		
		 try {
			 
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Building the set partitioning model...");
			 }
			
			 //Build CPLEX environment
			 
			 	cplex = new IloCplex();
			
			 //Create decision variables
			 	
			 	x = cplex.boolVarArray(size_fe); //FE routes
			 	y = cplex.boolVarArray(size_se); //SE routes
			 	f = cplex.numVarArray(size_fe * nSatellites,0,data.getQ1()); //Quantity delivered by each first echelon route to each satellite

			 //Create covering/partitioning constraints and objective function

				IloLinearNumExpr[] partitioning_constraints = new IloLinearNumExpr[nRequests];
				IloLinearNumExpr[] capFE_constraints = new IloLinearNumExpr[size_fe];
				IloLinearNumExpr[] satFlow_constraints = new IloLinearNumExpr[nSatellites];
				IloLinearNumExpr fleetFE_constraints = cplex.linearNumExpr();
				IloLinearNumExpr fleetSE_constraints = cplex.linearNumExpr();
				IloLinearNumExpr[] maxSE_constraints = new IloLinearNumExpr[nSatellites];
				
				IloLinearNumExpr of = cplex.linearNumExpr();
				
			// Initializes the constraints:
				
				for(int i = 0;i<nRequests; i++) {
					partitioning_constraints[i] = cplex.linearNumExpr();
				}
				
				for(int i = 0;i<size_fe; i++) {
					capFE_constraints[i] = cplex.linearNumExpr();
				}
				
				for(int i = 0;i<nSatellites; i++) {
					satFlow_constraints[i] = cplex.linearNumExpr();
				}
				
				for(int i = 0;i<nSatellites; i++) {
					maxSE_constraints[i] = cplex.linearNumExpr();
				}
				
			//Add terms to the covering/partitioning constraints and objective function
				
			int start,end;
			int counter_fe = 0;
			int counter_se = 0;
			for(RoutePool pool : pools) {
				
				// Check if the route pool belongs to the FE or the SE:
				
				if(pool.getSatellite() == 0) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						// Update the objective function:
						
							of.addTerm((double)r.getAttribute(RouteAttribute.COST),x[counter_fe]);
							
						// Capture the route:
						
							ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
						
						// Update the start and end:
						
							if(hasTerminals){
								start=1;
								end=route.size()-1;
							}else{
								start=0;
								end=route.size();
							}
						
						// Update FE capacity constraints constraints:
						
							for(int i=start;i<end;i++){
								
								capFE_constraints[counter_fe].addTerm(1,f[route.get(i) + (counter_fe * nSatellites) - 1]);
								
								satFlow_constraints[route.get(i)-1].addTerm(1,f[route.get(i) + (counter_fe * nSatellites) - 1]);
								
								of.addTerm(data.getHandling_costs().get(route.get(i)), f[route.get(i) + (counter_fe * nSatellites) - 1]);
								
							}
						
							capFE_constraints[counter_fe].addTerm(-data.getQ1(),x[counter_fe]);
							fleetFE_constraints.addTerm(1, x[counter_fe]);
					
							counter_fe++;
					}
				}
			}
			// SE:
			
			for(RoutePool pool : newPools) {
					Iterator<Route> iterator = pool.iterator();
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						// Update the objective function:
						
							of.addTerm((double)r.getAttribute(RouteAttribute.COST),y[counter_se]);
						
						// Capture the route:
						
							ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
						
						// Update the start and end:
						
							if(hasTerminals){
								start=1;
								end=route.size()-1;
							}else{
								start=0;
								end=route.size();
							}
						
						// Update set partitioning constraints:
						
							for(int i=start;i<end;i++){
								partitioning_constraints[route.get(i)-1].addTerm(1,y[counter_se]);
							}
						
							satFlow_constraints[pool.getSatellite()-1].addTerm(-(double)r.getAttribute(RouteAttribute.LOAD),y[counter_se]);
							
							maxSE_constraints[pool.getSatellite()-1].addTerm(1,y[counter_se]);
							
							fleetSE_constraints.addTerm(1, y[counter_se]);
					
							counter_se++;
						
					}	

				}
				
		 	
			
			//Add constraints to the model
			
			for(int i = 0;i<nRequests; i++) {
				cplex.addEq(1,partitioning_constraints[i],"ServeCustomer_"+i);
			}
			
			for(int i = 0;i<size_fe; i++) {
				cplex.addGe(0,capFE_constraints[i],"CapacityFE_"+i);
			}
			
			for(int i = 0;i<nSatellites; i++) {
				cplex.addEq(0,satFlow_constraints[i],"FlowAtSatellite_"+i);
			}
			
			for(int i = 0;i<nSatellites; i++) {
				cplex.addGe(data.getMs(),maxSE_constraints[i],"MaxSERoutesSatellite_"+i);
			}
			
			cplex.addGe(data.getFleet1(), fleetFE_constraints,"MaxFERoutes");
			cplex.addGe(data.getFleet2(), fleetSE_constraints,"MaxSERoutes");
			
			//Add objective function
			
			 cplex.addMinimize(of);
			 
			//Hide the output:
			 
			 cplex.setParam(IloCplex.Param.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
			 cplex.setParam(IloCplex.Param.Threads, GlobalParameters.THREADS);
			 
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Printing in the output folder the set partitioning model...");
				 cplex.exportModel("./output/SetPartitioningModel"+".lp");	
			 }else {
				 cplex.setOut(null);
			 }
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Finished building the set partitioning model...");
				 System.out.println("About to start solving the set partitioning model...");
			 }
			 
			 if(GlobalParameters.EMPHASIZE_FEASIBILITY) {
				 cplex.setParam(IloCplex.Param.Emphasis.MIP, 1);
			 }
			 
			//Solve model:
			 
			 cplex.solve();
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Finished building the set partitioning model...");
			 }
			//Store the solution:
			 
			 // Objective function value:
			 
			 objectiveFunction = cplex.getObjValue();
			 
			 // Routes and loads:
			 
				 solution_fe = new ArrayList<Route>();
				 solution_se = new ArrayList<Route>();
				 
				 counter_fe = 0;
				 counter_se = 0;
				 solution_fe_drops = new ArrayList<ArrayList<Double>>();
				 solution_se_satellites = new ArrayList<Integer>();
				 solution_se_identifiers = new ArrayList<String>();
				 
			// Iterate overall the routes:
				 
				 for(RoutePool pool : pools) {
					 
					 if(pool.getSatellite() == 0) {
						    
							Iterator<Route> iterator = pool.iterator();
							
							while(iterator.hasNext()) {
							
								Route r = iterator.next();
								if(cplex.getValue(x[counter_fe]) > 0.5){
									solution_fe.add(r);
									if(hasTerminals){
										start=1;
										end=r.size()-1;
									}else{
										start=0;
										end=r.size();
									}
									solution_fe_drops.add(new ArrayList<Double>());
									for(int i=start;i<end;i++){
										solution_fe_drops.get(solution_fe_drops.size()-1).add(cplex.getValue(f[r.get(i) + (counter_fe * nSatellites) - 1]));
									}
								}
								counter_fe++;
							}
					 }
				 }
				 for(RoutePool pool : newPools) {
						 Iterator<Route> iterator = pool.iterator();
							
							while(iterator.hasNext()) {
							
								Route r = iterator.next();
								if(cplex.getValue(y[counter_se]) > 0.5){
									solution_se.add(r);
									solution_se_satellites.add(pool.getSatellite());
									solution_se_identifiers.add(pool.getIdentifier());
								}
								counter_se++;
							}
					 }
				 	 
 
		} catch (IloException e) {
			e.printStackTrace();
		}
		 
		 
		// Should we run the MIP improvement procedure ?
		 
		 if(GlobalParameters.USE_IMPROVEMENT_MIP) {
		
			 boolean keepGoing = true;
			 
			 while(keepGoing) {
				 
				//Step 1: Build the pools we will use:
				 
			 		ArrayList<RoutePool> fe_pools = new ArrayList<RoutePool>();
			 		ArrayList<RoutePool> se_pools = new ArrayList<RoutePool>();
			 		
			 		for(int i = 1 ; i <= this.nSatellites; i++) {
			 			se_pools.add(new RoutePool(i));
			 		}

			 		
			 	//Step 2: Populate the pools with the solution we found:
			 		
			 		// First echelon routes:
			 		
				 		int current = 0;
				 		fe_pools.add(new RoutePool(0));
				 		for(Route fe_r : this.solution_fe) {
				 			
				 			if(fe_pools.get(current).contains(fe_r)) {
				 				current++;
				 				fe_pools.add(new RoutePool(0));
				 			}
				 			fe_pools.get(current).add(fe_r);
				 		}
			 		
				 		
				 	// Second echelon routes:
				 		
				 		// The ones that were selected:
				 		
				 			this.solution_se_hashcodes = new ArrayList<Integer>();
					 		for(int i = 0; i < this.solution_se.size(); i++) {
					 			
					 			se_pools.get(this.solution_se_satellites.get(i)-1).add(this.solution_se.get(i));
					 			this.solution_se_hashcodes.add(this.solution_se.get(i).hashCode());
					 		}
			 		
					 	// Add the top X routes which are more "similar" to the routes we have in the solution:
					 		
					 		int X = (int)GlobalParameters.MAX_TO_ADD_SIMILARITY/this.solution_se.size(); 
					 		
					 		for(int j = 0; j < this.solution_se.size(); j++) {
					 			
					 			Route r_in = this.solution_se.get(j);
					 			ArrayList<Route> routes = new ArrayList<Route>();
					 			
					 			for(RoutePool pool : newPools) {
						 			
						 			if(this.solution_se_satellites.get(j) == pool.getSatellite()) {
						 				Iterator<Route> iterator = pool.iterator();
							 			
							 			// Compute the similarity for each route in this pool:
							 			
								 			while(iterator.hasNext()) {
								 				
								 				Route r = iterator.next();
								 				r.setAttribute(RouteAttribute.SATELLITE, pool.getSatellite());
								 				r.setAttribute(RouteAttribute.SIMILARITY, 0.0);
								 				
								 				double in_common = 0.0;
							 					for(int i = 1;i < r_in.size()-1; i++) {
							 						if(r.contains(r_in.get(i))) {
							 							in_common++;
							 						}
							 					}
							 					double size_to = (r.size()-2);
							 					if((r_in.size()-2) > size_to) {
							 						size_to = (r_in.size()-2);
							 					}
							 					double sim = (in_common / size_to);
							 					if(sim > (Double)r.getAttribute(RouteAttribute.SIMILARITY)) {
							 						r.setAttribute(RouteAttribute.SIMILARITY, sim);
							 					}
							 					if(sim < GlobalParameters.MAX_PERCENTAGE_SIMILARITY) {
							 						routes.add(r);
							 					}
								 			}
								 			
						 			}
					 			}
					 			// Sort the list based on the similarity:
					 			
						 			routes.sort(new Comparator<Route>() {
						 				public int compare(Route o1, Route o2) {
						 					if ((Double)o1.getAttribute(RouteAttribute.SIMILARITY) < (Double)o2.getAttribute(RouteAttribute.SIMILARITY))
						 						return 1;
						 					else if ((Double)o1.getAttribute(RouteAttribute.SIMILARITY) > (Double)o2.getAttribute(RouteAttribute.SIMILARITY))
						 						return -1;
						 					else
						 						return 0;
						 				}
						 			});
					 			
					 			// Add the top X to the pool we will use in the improvement MIP:
				 		
						 			for(int i = 0; i < X; i++) {
						 				se_pools.get((int)routes.get(i).getAttribute(RouteAttribute.SATELLITE)-1).add(routes.get(i));
						 			}
					 			
					 		}
			 		
			 		
			 	// Step 3: Run the improvement procedure: //se_pools
			 		
			 		try {
			 			// Recover the current objective function:
			 			
			 				double currentOBJ = this.objectiveFunction;
			 				
			 			// Run the improvement MIP:
			 				
						runImprovementMIP(fe_pools, se_pools,distances_customers, distances_satellite_customers);
						
						// Check if we did an improvement:
						
							if(this.objectiveFunction < currentOBJ) {
								keepGoing = true; //We can try to improve it even more, using the new routes
							}else {
								keepGoing = false; //We should stop
							}
					} catch (IloException e) {
						System.out.println("Problem with the improvement MIP");
						e.printStackTrace();
					}
				 
			 }
			 
			
		 		
		 }
		 	
		
		return null;
	}
	
	
	
	/**
	 * This method runs the MIP improvement procedure (after solving the set partitioning
	 * @throws IloException 
	 * 
	 */
	public Solution runImprovementMIP( ArrayList<RoutePool> fe_pools, ArrayList<RoutePool> se_pools, ArrayDistanceMatrix distances_customers, ArrayList<ArrayDistanceMatrix> distances_satellite_customers) throws IloException {
		
		// Step 1: Build the clusters with size 2 to 3:
		
			Hashtable<Integer,Cluster> clusters = buildClusters(distances_customers);
			
		// Step 2: For each route have a list of the clusters that could be inserted:
			
			Hashtable<Integer,ArrayList<Integer>> clusters_of_route = assignClustersToRoutes(se_pools,clusters);
			
		// Step 3: Calculate the cost of the insertion of each cluster into a route:
			
			Hashtable<Integer,ArrayList<String>> insertionCostsAndPositions_to_route = computeInsertionCosts(se_pools,clusters,clusters_of_route,distances_satellite_customers);
			
		// Step 4: Compute the cost of removing W customers starting from position p of route r:
			
			Hashtable<Integer,Hashtable<Integer,ArrayList<Double>>> removalCosts_to_route = computeRemovalCosts(se_pools,distances_satellite_customers);
			
		// Step 5: Build the MILP improvement model:
			
			buildImprovementMILP(fe_pools,se_pools,clusters,clusters_of_route,insertionCostsAndPositions_to_route,removalCosts_to_route);
			
		return null;
		
	}
	
	/**
	 * This method builds the improvement MILP to try to modify the final solution
	 * @param clusters
	 * @param clusters_of_route
	 * @param insertionCostsAndPositions_to_route
	 * @param removalCosts_to_route
	 * @return
	 * @throws IloException 
	 */
	public void buildImprovementMILP( ArrayList<RoutePool> fePools, ArrayList<RoutePool> sePools, Hashtable<Integer,Cluster> clusters,Hashtable<Integer,ArrayList<Integer>> clusters_of_route,Hashtable<Integer,ArrayList<String>> insertionCostsAndPositions_to_route,Hashtable<Integer,Hashtable<Integer,ArrayList<Double>>> removalCosts_to_route) throws IloException {
		
		// Size of the pools: How many routes do we have for each echelon
		
			int size_se = 0;
			for(RoutePool pool : sePools) {
				size_se += pool.size();
			}
			
			int size_fe = 0;
			for(RoutePool pool : fePools) {
				size_fe += pool.size();
			}
		
		// Create the model
		
			IloCplex model = new IloCplex();
		
		// Create the variables:

			// Initialize hashtable to store the variables:
			
				Hashtable<Integer,IloNumVar>  x_r = new Hashtable<Integer,IloNumVar>(); //Do we use a FE route?
				Hashtable<Integer,IloNumVar> y_r = new Hashtable<Integer,IloNumVar>(); //Do we use a SE route? 
				Hashtable<String,IloNumVar> f_rs = new Hashtable<String,IloNumVar>(); //Quantity delivered by each first echelon route to each satellite
				
				Hashtable<String,IloNumVar> z_rc = new Hashtable<String,IloNumVar>(); // Do we insert cluster c in route r?
				Hashtable<String,IloNumVar> v_rp = new Hashtable<String,IloNumVar>(); // Do we remove node in position p from route r?
				Hashtable<String,IloNumVar> w_rpt = new Hashtable<String,IloNumVar>(); //Do we remove the nodes starting in position p of route r with a sequence of t nodes?
				
			//Populate the hashtables:
			
				// FE variables: x and f
				
					int start,end;
					int counter = 0;
					for(RoutePool pool : fePools) {
						
						Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							Route r = iterator.next();
							
							// Capture the route:
							
							ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
						
							x_r.put(counter, model.boolVar("x_"+counter));
							
							if(hasTerminals){
								start=1;
								end=route.size()-1;
							}else{
								start=0;
								end=route.size();
							}
							for(int i=start;i<end;i++){
								f_rs.put(counter+" - "+route.get(i), model.numVar(0,data.getQ1(),"f_"+counter+"_"+route.get(i)));
							}
							counter++;
						}
						
					}
			
			// SE variables: y, v, w, and z
			
				counter = 0;
				ArrayList<Integer> positionsWarmStart = new ArrayList<Integer>();
				for(RoutePool pool : sePools) {
					
					 Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							Route r = iterator.next();
							
							y_r.put(counter, model.boolVar("y_"+counter));
							
							if(this.solution_se_hashcodes.contains(r.hashCode())) {
								positionsWarmStart.add(counter);
							}
							
							for(Integer cluster_id : clusters_of_route.get(counter)) {
								
								z_rc.put(counter+" - "+cluster_id, model.boolVar("z_"+counter+"_"+cluster_id));
								
							}
							
							for(int p = 0; p < r.size()-1; p++){
								
								v_rp.put(counter+" - "+p, model.boolVar("v_"+counter+"_"+p));
								
							}
							
							for(int j = 1; j < 4; j++) {
								if(j < r.size() - 2) {
									
									for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
										w_rpt.put(counter+" - "+p+" - "+j, model.boolVar("w_"+counter+"_"+p+"_"+j));
									}
								}
							}
							
							counter++;
						}
				}
			
		 // Initialize constraints and objective function

			// Original set of constraints:
				
				IloLinearNumExpr[] partitioning_constraints = new IloLinearNumExpr[nRequests];
				IloLinearNumExpr[] capFE_constraints = new IloLinearNumExpr[size_fe];
				IloLinearNumExpr[] satFlow_constraints = new IloLinearNumExpr[nSatellites];
				IloLinearNumExpr fleetFE_constraints = cplex.linearNumExpr();
				IloLinearNumExpr fleetSE_constraints = cplex.linearNumExpr();
				IloLinearNumExpr[] maxSE_constraints = new IloLinearNumExpr[nSatellites];
				
			// New constraints:
			
				Hashtable<String, IloLinearNumExpr> relationWandV = new Hashtable<String,IloLinearNumExpr>();
				Hashtable<String, IloLinearNumExpr> onlyInsertOneClusterInPos = new Hashtable<String,IloLinearNumExpr>();
				Hashtable<String, IloLinearNumExpr> nodesBeforeAndAfterRemain = new Hashtable<String,IloLinearNumExpr>();
				Hashtable<String, IloLinearNumExpr> removalConstraints = new Hashtable<String,IloLinearNumExpr>();
				IloLinearNumExpr[] capSE_constraints = new IloLinearNumExpr[size_se];
			
			// Objective function:
				
				IloLinearNumExpr of = cplex.linearNumExpr();
			
		// Initializes the constraints:
			
			// Set partitioning constraints:
				
				for(int i = 0;i<nRequests; i++) {
					partitioning_constraints[i] = cplex.linearNumExpr();
				}
				
			// FE capacity:
			
				for(int i = 0;i<size_fe; i++) {
					capFE_constraints[i] = cplex.linearNumExpr();
				}
				
			// SE capacity:
			
				for(int i = 0;i<size_se; i++) {
					capSE_constraints[i] = cplex.linearNumExpr();
				}
				
			// Flow through a satellite:
			
				for(int i = 0;i<nSatellites; i++) {
					satFlow_constraints[i] = cplex.linearNumExpr();
				}
			
			// Maximum number of routes departing from a satellite:
				
				for(int i = 0;i<nSatellites; i++) {
					maxSE_constraints[i] = cplex.linearNumExpr();
				}
			
			// Insertion and removal constraints for SE routes:
				
				counter = 0;
				for(RoutePool pool : sePools) {
					
					 Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							Route r = iterator.next();
							for(int p = 0; p < r.size()-1; p++){
								
								if(p >= 1) {
									relationWandV.put(counter+" - "+p, cplex.linearNumExpr());
									onlyInsertOneClusterInPos.put(counter+" - "+p, cplex.linearNumExpr());
									
								}else {
									onlyInsertOneClusterInPos.put(counter+" - "+p, cplex.linearNumExpr());
									
								}
								
								
							}
							for(int j = 1; j < 4; j++) {
								if(j < r.size() - 2) {
									
									for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
										removalConstraints.put(counter+" - "+p+" - "+j, cplex.linearNumExpr());
										of.addTerm(removalCosts_to_route.get(counter).get(j).get(p-1),w_rpt.get(counter+" - "+p+" - "+j));
									}
								}
							}
							for(Integer cluster_id : clusters_of_route.get(counter)) {
								
								nodesBeforeAndAfterRemain.put(counter+" - "+cluster_id, cplex.linearNumExpr());
								
							}
							
							
							
							counter++;
						}
						
				}
			
		// Fill the constraints and update the obj function:
		
			// Start some counters for each echelon:
				
				int counter_fe = 0;
				int counter_se = 0;

			// FE related constraints:
				
				for(RoutePool pool : fePools) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						// Update the objective function:
						
							of.addTerm((double)r.getAttribute(RouteAttribute.COST),x_r.get(counter_fe));
							
						// Capture the route:
						
							ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
						
						// Update the start and end:
						
							if(hasTerminals){
								start=1;
								end=route.size()-1;
							}else{
								start=0;
								end=route.size();
							}
						
						// Iterate over the satellites in the route:
						
							for(int i=start;i<end;i++){
								
								// Update left side capacity constraints: how much do we drop at each satellite:
								
									capFE_constraints[counter_fe].addTerm(1,f_rs.get(counter_fe+" - "+route.get(i)));
								
								// Update left side flow constraints:
									
									satFlow_constraints[route.get(i)-1].addTerm(1,f_rs.get(counter_fe+" - "+route.get(i)));
								
								// Update the objective function: handling costs:
									
									of.addTerm(data.getHandling_costs().get(route.get(i)), f_rs.get(counter_fe+" - "+route.get(i)));
								
							}
						
							// Right side of capacity constraints:
							
							capFE_constraints[counter_fe].addTerm(-data.getQ1(),x_r.get(counter_fe));
							
							// Left side of fleet size constraints:
							
							fleetFE_constraints.addTerm(1, x_r.get(counter_fe));
					
							counter_fe++;
					}
					
				}
			
			// SE related constraints:
			
				for(RoutePool pool : sePools) {
					
					// Iterator of routes:
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						// Capture the route:
						
							Route r = iterator.next();
						
						// Update the objective function: y_r variables
						
							of.addTerm((double)r.getAttribute(RouteAttribute.COST),y_r.get(counter_se));
						
						// Capture the route:
						
							ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
						
						// Update the start and end:
						
							if(hasTerminals){
								start=1;
								end=route.size()-1;
							}else{
								start=0;
								end=route.size();
							}
						
						// Iterate over the customers in the route:
						
							for(int i=start;i<end;i++){
								
								// Update partitioning constraints:
								
									partitioning_constraints[route.get(i)-1].addTerm(1,y_r.get(counter_se));
									partitioning_constraints[route.get(i)-1].addTerm(-1,v_rp.get(counter_se+" - "+i));
								
								// Update capacity constraints:
									
									capSE_constraints[counter_se].addTerm(data.getDemands().get(route.get(i)-1),y_r.get(counter_se));
									capSE_constraints[counter_se].addTerm(-data.getDemands().get(route.get(i)-1),v_rp.get(counter_se+" - "+i));
								
								// Update flow constraints:
									
									satFlow_constraints[pool.getSatellite()-1].addTerm(data.getDemands().get(route.get(i)-1),v_rp.get(counter_se+" - "+i));
								
								// Update the relation between w and v constraints:
									
									for(int j = 1; j < 4; j++) {
										
										for(int p = i-j+1; p <= i; p++){
											
											if(w_rpt.containsKey(counter_se+" - "+p+" - "+j)) {
												
												relationWandV.get(counter_se+" - "+i).addTerm(1,w_rpt.get(counter_se+" - "+p+" - "+j));
												
											}
											
										}
										
									}
								
									relationWandV.get(counter_se+" - "+i).addTerm(-1,v_rp.get(counter_se+" - "+i));
								
								
							}
							
							// Iterate over the clusters associated with this route:
							
							for(Integer cluster_id : clusters_of_route.get(counter_se)) {
								
								// Update partitioning constraints considering the clusters of the route:
								
									for(Integer customer : clusters.get(cluster_id).getNodes_array()) {
										partitioning_constraints[customer-1].addTerm(1,z_rc.get(counter_se+" - "+cluster_id));
									}
								
								// Update the capacity constraints:
								
									capSE_constraints[counter_se].addTerm(clusters.get(cluster_id).getDemand(),z_rc.get(counter_se+" - "+cluster_id));
								
								// Update the flow constraints:
								
									satFlow_constraints[pool.getSatellite()-1].addTerm(-clusters.get(cluster_id).getDemand(),z_rc.get(counter_se+" - "+cluster_id));
								
								// Right side of constraints that ensure that nodes before and after the insertion remain:
									
									nodesBeforeAndAfterRemain.get(counter_se+" - "+cluster_id).addTerm(-2, y_r.get(counter_se));
									nodesBeforeAndAfterRemain.get(counter_se+" - "+cluster_id).addTerm(2, z_rc.get(counter_se+" - "+cluster_id));
									
							}
							
							// Iterate over the clusters in the route: (but in order)
							
							for(int j = 0; j < clusters_of_route.get(counter_se).size(); j++) {
								
								String act = insertionCostsAndPositions_to_route.get(counter_se).get(j);
								String[] parts = act.split(";");
								int best_pos = Integer.parseInt(parts[0]);
								double best_cost = Double.parseDouble(parts[1]);
								int cluster_id = clusters_of_route.get(counter_se).get(j);
								
								// Only insert in the best position:
								
									onlyInsertOneClusterInPos.get(counter_se+" - "+best_pos).addTerm(1,z_rc.get(counter_se+" - "+cluster_id));
								
								// Update the objective funtion, to reflect the cost of insertion:
								
									of.addTerm(best_cost,z_rc.get(counter_se+" - "+cluster_id));
								
								// Left side of constraints that ensure that the tail and head node where the insertion is done remain:
								
									if(v_rp.get(counter_se+" - "+(best_pos-1)) != null) {
										nodesBeforeAndAfterRemain.get(counter_se+" - "+cluster_id).addTerm(1, v_rp.get(counter_se+" - "+(best_pos)));
										
									}
									if(v_rp.get(counter_se+" - "+(best_pos+1)) != null) {
										nodesBeforeAndAfterRemain.get(counter_se+" - "+cluster_id).addTerm(1, v_rp.get(counter_se+" - "+(best_pos+1)));
										
									}
								
							}
							
							// Guarantee that at each position of the route we only do one insertion:
							
							for(int p = 0; p < r.size() - 1;p++) {
								
								onlyInsertOneClusterInPos.get(counter_se+" - "+p).addTerm(-1,y_r.get(counter_se));
								
							}
							
							// Update removal constraints:
							
								for(int j = 1; j < 4; j++) {
									if(j < r.size() - 2) {
										
										for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
											removalConstraints.get(counter_se+" - "+p+" - "+j).addTerm(-2,y_r.get(counter_se));
											removalConstraints.get(counter_se+" - "+p+" - "+j).addTerm(2,w_rpt.get(counter_se+" - "+p+" - "+j));
											if(v_rp.get(counter_se+" - "+(p-1)) != null) {
												removalConstraints.get(counter_se+" - "+p+" - "+j).addTerm(1,v_rp.get(counter_se+" - "+(p-1)));
											}
											if(v_rp.get(counter_se+" - "+(p+j)) != null) {
												removalConstraints.get(counter_se+" - "+p+" - "+j).addTerm(1,v_rp.get(counter_se+" - "+(p+j)));
											}
											
											
										}
									}
								}
								
							// Update flow constraints:
								
							satFlow_constraints[pool.getSatellite()-1].addTerm(-(double)r.getAttribute(RouteAttribute.LOAD),y_r.get(counter_se));
							
							// Update maximum number of routes:
							
							maxSE_constraints[pool.getSatellite()-1].addTerm(1,y_r.get(counter_se));
							
							// Update fleet constraints:
							
							fleetSE_constraints.addTerm(1, y_r.get(counter_se));
					
							// Update the counter:
							
							counter_se++;
						
					}	
	
				}

			//Add constraints to the model
			
				// Original constraints + SE capacity
				
					for(int i = 0;i<nRequests; i++) {
						model.addEq(1,partitioning_constraints[i],"ServeCustomer_"+i);
					}
					
					for(int i = 0;i<size_fe; i++) {
						model.addGe(0,capFE_constraints[i],"CapacityFE_"+i);
					}
					
					for(int i = 0;i<size_se; i++) {
						model.addGe(data.getQ2(),capSE_constraints[i],"CapacitySE_"+i);
					}
		
					for(int i = 0;i<nSatellites; i++) {
						model.addEq(0,satFlow_constraints[i],"FlowAtSatellite_"+i);
					}
					
					for(int i = 0;i<nSatellites; i++) {
						model.addGe(data.getMs(),maxSE_constraints[i],"MaxSERoutesSatellite_"+i);
					}
				
				// Removal and insertion constraints:
				
					counter = 0;
					for(RoutePool pool : sePools) {
						
						 Iterator<Route> iterator = pool.iterator();
							
							while(iterator.hasNext()) {
							
								Route r = iterator.next();
								for(int p = 0; p < r.size()-1; p++){
									
									if(p >= 1) {
										model.addEq(0,relationWandV.get(counter+" - "+p),"RelWandV_"+counter+";"+p);
										model.addGe(0,onlyInsertOneClusterInPos.get(counter+" - "+p),"OnlyInserOneClusterInPos_"+counter+"_"+p);
									}else {
										model.addGe(0,onlyInsertOneClusterInPos.get(counter+" - "+p),"OnlyInserOneClusterInPos_"+counter+"_"+p);
									}
									
									
								}
								for(int j = 1; j < 4; j++) {
									if(j < r.size() - 2) {
										
										for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
											model.addGe(0,removalConstraints.get(counter+" - "+p+" - "+j),"Remove_"+counter+"_"+p+"_"+j);
										}
									}
								}
								for(Integer cluster_id : clusters_of_route.get(counter)) {
		
									model.addGe(0, nodesBeforeAndAfterRemain.get(counter+" - "+cluster_id),"NodeBefAndAfterRemain_"+counter+"_"+cluster_id);
								
								}
								
								
								
								counter++;
							}
							
					}
				
				// Fleet size constraints:
				
					model.addGe(data.getFleet1(), fleetFE_constraints,"MaxFERoutes");
					model.addGe(data.getFleet2(), fleetSE_constraints,"MaxSERoutes");
				
			//Add objective function
			
					model.addMinimize(of);
		
			//Hide the output:
			 
				model.setParam(IloCplex.Param.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
				model.setParam(IloCplex.Param.Threads, GlobalParameters.THREADS);
			 
				 if(GlobalParameters.PRINT_IN_CONSOLE) {
					 System.out.println("Printing in the output folder the set partitioning improvement model...");
					 model.exportModel("./output/ImprovementModel"+".lp");	
				 }else {
					 model.setOut(null);
				 }
				
				 if(GlobalParameters.PRINT_IN_CONSOLE) {
					 System.out.println("Finished building the set partitioning improvement model...");
					 System.out.println("About to start solving the set partitioning improvement model...");
				 }
			 
			// Do a warm-start:
				 
				 IloNumVar[] mipStart_a = cplex.boolVarArray(size_fe + size_se); //SE and FE routes
				 double[] values = new double[size_fe + size_se];
				 for(int i = 0; i < size_fe; i++) {
					 mipStart_a[i] = x_r.get(i);
					 values[i] = 1.0;
				 }
				 for(int i = 0; i < size_se; i++) {
					 mipStart_a[i+size_fe] = y_r.get(i);
					 
				 }
				 for(Integer pos : positionsWarmStart) {
					 values[pos + size_fe] = 1.0;
				 }
				 
				
				model.addMIPStart(mipStart_a, values);

			//Solve model:
			 
			 model.solve();
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Finished building the set partitioning improvement model...");
			 }
			 
			//Store the solution:
			 
			 // Objective function value:
			 
			 	this.objectiveFunction = model.getObjValue();
			 
			 // FE routes: (This ones do not have any structural changes):
		
			 	this.solution_fe = new ArrayList<Route>();
			 	this.solution_fe_drops = new ArrayList<ArrayList<Double>>();
			 	counter = 0;
			 	
			 	for(RoutePool pool : fePools) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						// If the route was selected:
						
							if(model.getValue(x_r.get(counter)) > 0) {
								
								this.solution_fe.add(r);
								if(hasTerminals){
									start=1;
									end=r.size()-1;
								}else{
									start=0;
									end=r.size();
								}
								solution_fe_drops.add(new ArrayList<Double>());
								for(int i=start;i<end;i++){
									solution_fe_drops.get(solution_fe_drops.size()-1).add(model.getValue(f_rs.get(counter+" - "+r.get(i))));
								}
						}
						
						// Update the counter:
							
							counter++;
					}
			 	}
			 	
			 	// SE Routes: These routes may have structural changes insertions/removals:

			 	counter = 0;
			 	this.solution_se = new ArrayList<Route>();
			 	this.solution_se_identifiers = new ArrayList<String>();
				this.solution_se_satellites = new ArrayList<Integer>();
				
				for(RoutePool pool : sePools) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						// If the route was selected:
						
						if(model.getValue(y_r.get(counter)) > 0) {
							
							// Create a copy of the route:
							
								Route r_updated = r.getCopy();
							
							// Insert the clusters selected by the model:
								
								// Iterate over the clusters:
								
									for(int j = 0; j < clusters_of_route.get(counter).size(); j++) {
									
										// Capture the cluster_id:
										
											int cluster_id = clusters_of_route.get(counter).get(j);
									
										// If the cluster was inserted:
										
											if(model.getValue(z_rc.get(counter+" - "+cluster_id)) > 0) {
												
												// Retrieve the best insertion position and the insertion cost:
												
													String act = insertionCostsAndPositions_to_route.get(counter).get(j);
													String[] parts = act.split(";");
													int best_pos = Integer.parseInt(parts[0]);
													int node_in_best_pos = r.get(best_pos);
													int position_to_insert = r_updated.positionOf(node_in_best_pos);
													double best_cost = Double.parseDouble(parts[1]);
													String chain = parts[2];
													String[] nodes = chain.split("_");

												// Do the insertion:
													
													for(int i = 0; i < nodes.length; i++) {
														int node_to_insert = Integer.parseInt(nodes[i]);
														r_updated.insert(node_to_insert, position_to_insert+i+1);
													}
													double currentCost = (double) r_updated.getAttribute(RouteAttribute.COST);
													r_updated.setAttribute(RouteAttribute.COST, currentCost + best_cost);
													double currentLoad = (double) r_updated.getAttribute(RouteAttribute.LOAD);
													r_updated.setAttribute(RouteAttribute.LOAD, currentLoad + clusters.get(cluster_id).getDemand());
												}
										
									}
							
									
							// Removals:
								
								for(int j = 1; j < 4; j++) {
									if(j < r.size() - 2) {
										
										for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
											if(model.getValue(w_rpt.get(counter+" - "+p+" - "+j)) > 0) {
												
												int node_in_remove_pos = r.get(p);
												int position_to_start_rem = r_updated.positionOf(node_in_remove_pos);
												
												for(int jj = 0; jj < j; jj++) {
													int node_in_remove_pos_act = r_updated.get(position_to_start_rem);
													double demand = data.getDemands().get(node_in_remove_pos_act-1);
													r_updated.remove(position_to_start_rem);
													double currentLoad = (double) r_updated.getAttribute(RouteAttribute.LOAD);
													r_updated.setAttribute(RouteAttribute.LOAD, currentLoad - demand);
												}
						
												double currentCost = (double) r_updated.getAttribute(RouteAttribute.COST);
												r_updated.setAttribute(RouteAttribute.COST, currentCost + removalCosts_to_route.get(counter).get(j).get(p-1));
											}
											
										}
									}
								}
						
							// Add the route to the solution:
								
								this.solution_se.add(r_updated);
								this.solution_se_identifiers.add("Unknown");
								this.solution_se_satellites.add(pool.getSatellite());
								
						}
						
						counter++;
					}
					
				}
			 	
		// Close the model:
				
		model.close();
				
	}
	
	
	
	/**
	 * This method builds the clusters (Of customers) with size 2 to 3
	 * We use a k-means algorithm with k = floor(nRequests / 2)
	 */
	public Hashtable<Integer,Cluster> buildClusters(ArrayDistanceMatrix distances_customers) {
		
		// Computes the number of clusters:
		
			int n_clusters = (int) Math.floor(nRequests / 2);
		
		// Runs the k-means algorithm:
			
			var results_kmeans = PartitionClustering.run(1, () -> KMeans.fit(distances_customers.getDistanceMatrixCopy(), n_clusters));

		// Stores the clusters:
			
			Hashtable<Integer,Cluster> clusters = new Hashtable<Integer,Cluster>();
			for(int i = 0;i<nRequests; i++) {
				int cluster_id = results_kmeans.y[i];
				if(clusters.containsKey(cluster_id)) {
					clusters.get(cluster_id).insertNode(i+1);
					clusters.get(cluster_id).updateDemand(data.getDemands().get(i));
				}else {
					clusters.put(cluster_id, new Cluster(cluster_id));
					clusters.get(cluster_id).insertNode(i+1);
					clusters.get(cluster_id).updateDemand(data.getDemands().get(i));
				}
				
			}
			
		// Filter clusters: We only keep clusters of size 2 and 3. We also check if the total demand of the cluster is less than the capacity.
			
			int counter = 0;
			Hashtable<Integer,Cluster> filtered_clusters = new Hashtable<Integer,Cluster>();
			for(int i=0;i < n_clusters;i++) {
				if(clusters.get(i).getDemand() <= data.getQ2() && clusters.get(i).getNumber_of_nodes() >= 2 && clusters.get(i).getNumber_of_nodes() <= 3) {
					filtered_clusters.put(counter, clusters.get(i));
					clusters.get(i).setID(counter);
					counter++;
				}
			}
		
		// Final clusters: We add every node to each cluster
			
			
			for(int c=0 ; c < nRequests; c++) { //Iterate over all the customers
				
				Cluster new_cluster = new Cluster(counter);
				new_cluster.insertNode(c+1);
				new_cluster.updateDemand(data.getDemands().get(c));
				filtered_clusters.put(counter, new_cluster);
				counter++;
				
			}
			
			// Print the clusters:
			
			/**
			if(GlobalParameters.PRINT_IN_CONSOLE) {
				
				Set<Integer> set_of_keys = filtered_clusters.keySet();
				for(Integer key : set_of_keys) {
					System.out.println(key+" ; "+filtered_clusters.get(key).toString()+" ; "+filtered_clusters.get(key).getDemand());
				}
				
			}
			*/
				
			// Return the hashtable of clusters:
				
				return filtered_clusters;
				
	}
	
	/**
	 * This method checks which clusters can be assigned to each route of the solution
	 * @param clusters
	 * @return
	 */
	public Hashtable<Integer,ArrayList<Integer>> assignClustersToRoutes(ArrayList<RoutePool> sePools,Hashtable<Integer,Cluster> clusters){
		
		// Create the hashtable to store the results:
		
		Hashtable<Integer,ArrayList<Integer>> clusters_of_route = new Hashtable<Integer,ArrayList<Integer>>();
		int start = 0;
		int end = 0;
		int counter = 0;
		
		// Iterate over all the routes:
		
		for(RoutePool pool : sePools) {
			
			 Iterator<Route> iterator = pool.iterator();
				
				while(iterator.hasNext()) {
				
					Route r = iterator.next();
				
				// Initializes the list of clusters that can be inserted in the route:
				
					clusters_of_route.put(counter, new ArrayList<Integer>());
				
				// Iterates over all the clusters:
					
					Set<Integer> set_of_keys = clusters.keySet();
					
					for(Integer key : set_of_keys) {
						
						// Check if the route contains a node inside the cluster:
						
						if(hasTerminals){
							start=1;
							end=r.size()-1;
						}else{
							start=0;
							end=r.size();
						}
						boolean check = true;
						for(int c=start;c<end && check;c++){
							if(clusters.get(key).checkIfNodeIsInCluster(r.get(c))) {
								check = false;
							}
						}
						if(check) {
							clusters_of_route.get(counter).add(key);
						}
						
					}
					
					counter++;
				
			}
		
		}
		
		// Print them:
		
		/**
		if(GlobalParameters.PRINT_IN_CONSOLE) {
			counter = 0;
			for(RoutePool pool : sePools) {
				
				Iterator<Route> iterator = pool.iterator();
				
				while(iterator.hasNext()) {
				
					Route r = iterator.next();
					
					System.out.println(counter+" - "+clusters_of_route.get(counter).toString()+" - "+r.toString());
					
					counter++;
				}
				
			}
		}
		*/
		
		
		// Return the clusters that can be assigned to each route:
		
		return clusters_of_route;
		
	}
	
	/**
	 * This method computes the cost of inserting a cluster in a route
	 * @param clusters
	 * @param clusters_of_route
	 * @param distances_satellite_customers
	 * @return
	 */
	public Hashtable<Integer,ArrayList<String>> computeInsertionCosts(ArrayList<RoutePool> sePools,Hashtable<Integer,Cluster> clusters,Hashtable<Integer,ArrayList<Integer>>clusters_of_route,ArrayList<ArrayDistanceMatrix>distances_satellite_customers){
		
		// Initialize the resulting hashtable:
		
			Hashtable<Integer,ArrayList<String>> costsAndPositions = new Hashtable<Integer,ArrayList<String>>();
		
			int counter = 0;
			
		// Iterate over all the routes:
			
			for(RoutePool pool : sePools) {
				
				 Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						
						ArrayList<String> current_list = new ArrayList<String>();
						
						// Iterate over the subset of clusters that can be inserted
						
						for(int j = 0; j < clusters_of_route.get(counter).size(); j++) {
							
							// Initialize the cost:
							
							double cost = Double.POSITIVE_INFINITY;
							int best_pos = -1;
							String chain = "";
							
							// Iterate over all the positions in which the cluster can be inserted. We will keep the best cost. 
							
							for(int p = 0; p < r.size()-1; p++){
								
								// Calculate the cost of the insertion 
								
								String valueAndChain = computeIndividualInsertionCost(clusters.get(clusters_of_route.get(counter).get(j)), r, p, distances_satellite_customers, pool.getSatellite());
								String[] parts = valueAndChain.split(";");
								
								double new_cost = Double.parseDouble(parts[0]);
								
								// If the cost is better, replace it:
								
								if(new_cost < cost) {
									
									cost = new_cost;
									best_pos = p;
									chain = parts[1];
									
								}
								
							}
						
						
							current_list.add(best_pos+";"+cost+";"+chain);
						
						}
	
					costsAndPositions.put(counter, current_list);
					counter++;
				}
			
			}
			
			/**
			Set<Integer> set_of_keys = costsAndPositions.keySet();
			for(Integer key : set_of_keys) {
				
				System.out.println(key + " - " + costsAndPositions.get(key).toString());
				
			}
			*/
			
		// Return the costs hashtable
			
			return costsAndPositions;
	}
	
	
	/**
	 * Computes the cost of inserting the cluster inside the route between positions pos and pos+1 considering that the satellite is sat.
	 * @param cluster
	 * @param r
	 * @param pos
	 * @param distances_satellite_customers
	 * @param sat
	 * @return
	 */
	public String computeIndividualInsertionCost(Cluster cluster, Route r, int pos, ArrayList<ArrayDistanceMatrix> distances_satellite_customers, int sat) {
		
		// Initializes the cost:
		
		String chain = "";
		double cost = Double.POSITIVE_INFINITY;
		
		// If the cluster size is equal to 1: pos to cluster + cluster to pos+1 - pos to pos+1
		
			if(cluster.getNumber_of_nodes() == 1) {
				
				cost = distances_satellite_customers.get(sat-1).getDistance(r.get(pos), cluster.getNodes_array().get(0)) + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(0),r.get(pos+1)) - distances_satellite_customers.get(sat-1).getDistance(r.get(pos), r.get(pos+1));	
				chain = cost+";"+cluster.getNodes_array().get(0);
			}
		
		// If the cluster size is equal to 2:
		
			if(cluster.getNumber_of_nodes() == 2) {
				
				// Order 1: 0 - 1
				
				cost = distances_satellite_customers.get(sat-1).getDistance(r.get(pos), cluster.getNodes_array().get(0)) + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(0), cluster.getNodes_array().get(1))  + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(1),r.get(pos+1)) - distances_satellite_customers.get(sat-1).getDistance(r.get(pos), r.get(pos+1));	
				chain = cost+";"+cluster.getNodes_array().get(0)+"_"+cluster.getNodes_array().get(1);
				
				// Order 2: 1 - 0:
				
				double alt_cost = distances_satellite_customers.get(sat-1).getDistance(r.get(pos), cluster.getNodes_array().get(1)) + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(1), cluster.getNodes_array().get(0))  + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(0),r.get(pos+1)) - distances_satellite_customers.get(sat-1).getDistance(r.get(pos), r.get(pos+1));	
				
				if(alt_cost < cost) {
					cost = alt_cost;
					chain = cost+";"+cluster.getNodes_array().get(1)+"_"+cluster.getNodes_array().get(0);
					
				}
				
				
			}
		
		// If the cluster size is equal to 3:
		
			if(cluster.getNumber_of_nodes() == 3) {
					
				for(int i = 0; i < 3 ; i++) {
					
					for(int j = 0; j < 3 ; j++) {
						
						if(j != i) {
							
							for(int k = 0; k < 3 ; k++) {
								
								if(j != k && k != i) {
									
									double new_cost = distances_satellite_customers.get(sat-1).getDistance(r.get(pos), cluster.getNodes_array().get(i)) + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(i), cluster.getNodes_array().get(j)) + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(j), cluster.getNodes_array().get(k))  + distances_satellite_customers.get(sat-1).getDistance(cluster.getNodes_array().get(k),r.get(pos+1)) - distances_satellite_customers.get(sat-1).getDistance(r.get(pos), r.get(pos+1));	
									
									if(new_cost < cost) {
										
										cost = new_cost;
										chain = cost+";"+cluster.getNodes_array().get(i)+"_"+cluster.getNodes_array().get(j)+"_"+cluster.getNodes_array().get(k);
										
									}
								}
							}
						}
						
					}
				}
				
			
				
			}
		
		
		// Returns the cost:
		
		return chain;
	}
	
	/**
	 * This method computes the removal cost of the node in position p and the next t nodes from route r
	 * @param fePool
	 * @param sePools
	 * @param distances_satellite_customers
	 * @return
	 */
	public Hashtable<Integer,Hashtable<Integer,ArrayList<Double>>> computeRemovalCosts(ArrayList<RoutePool> sePools,ArrayList<ArrayDistanceMatrix>distances_satellite_customers){
		
		// Initialize the costs hashtable:
		
			Hashtable<Integer,Hashtable<Integer,ArrayList<Double>>> removal_costs = new Hashtable<Integer,Hashtable<Integer,ArrayList<Double>>>();
		
			int counter = 0;
			
			// Iterate over all the routes:
				
				for(RoutePool pool : sePools) {
					
					 Iterator<Route> iterator = pool.iterator();
						
						while(iterator.hasNext()) {
						
							Route r = iterator.next();
							
							int customers_in_route = r.size() - 2;
							removal_costs.put(counter, new Hashtable<Integer,ArrayList<Double>>());
							
							// Iterate overall the possible # of consecutive customers to remove: 1, 2 or 3
							
							for(int j = 1; j < 4; j++) {
								
								removal_costs.get(counter).put(j, new ArrayList<Double>());
								
								// Iterate overall the positions in which these removals are possible:
								
									if(j < customers_in_route) {
										
										for(int p = 1; p + j < r.size(); p++) { //Iterate over the feasible positions in which j customers can be removed 
											
											// Compute the cost of the removal:
											
											double cost = 0;
											
											for(int k = p-1; k <= p+j-1; k++) {
												
												cost -= distances_satellite_customers.get(pool.getSatellite()-1).getDistance(r.get(k), r.get(k+1));
												
											}
											
											cost += distances_satellite_customers.get(pool.getSatellite()-1).getDistance(r.get(p-1), r.get(p+j));
											
											// Store the cost of the removal:
											
											removal_costs.get(counter).get(j).add(cost);
											
										}
										
										
									}
									
								
							}
						counter++;
					}
				
			}
		
		// Print
		
			/**
			for(int i = 0; i < this.solution_se.size(); i++) {
				
				for(int j = 1; j < 4; j++) {
					
					if(removal_costs.get(i).containsKey(j)) {
						
						System.out.println(i+" - "+j+" - "+removal_costs.get(i).get(j).toString());
						
					}
				}
				
			}
			*/
			
		// Return the hashtable:
			
			return removal_costs;
		
	}
}
