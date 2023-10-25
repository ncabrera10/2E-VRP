package msh;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import core.ArrayDistanceMatrix;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Solution;
import dataStructures.DataHandler;
import globalParameters.GlobalParameters;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;

/**
 * This class solves the set partitioning model to assembly the final solution. 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class GurobiSetPartitioningSolver extends AssemblyFunction{

	/**
	 * Gurobi environment
	 */
	protected GRBEnv env;
	
	protected int nRequests;
	protected int nSatellites;
	protected boolean hasTerminals;
	protected DataHandler data;
	
	public GurobiSetPartitioningSolver(int nRequests, int nSatellites,boolean hasTerminals,DataHandler data){
		this.nRequests=nRequests;
		this.nSatellites=nSatellites;
		this.hasTerminals=hasTerminals;
		this.data=data;
	}

	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools, ArrayDistanceMatrix distances_customers, ArrayList<ArrayDistanceMatrix> distances_satellite_customers) {
		
		// PROCEDURE TO REMOVE DUPLICATES:
		
		// Create a pool for satellite:
		
			ArrayList<RoutePool> newPools = new ArrayList<RoutePool>();
			
			for(int i = 1; i <= nSatellites; i++) {
				
				newPools.add(new RoutePool(i));
				newPools.get(newPools.size()-1).setIdentifier("Unknown");
			}
			
		
		// Populate these new pools:
		
			for(RoutePool pool : pools) {
				
				if(pool.getSatellite() > 0) {
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						Route r = iterator.next();
						newPools.get(pool.getSatellite()-1).add(r);
					}
					
				}
				
			}
		
		// Print the pool of paths (if selected by the user)
			
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
		
		
		// Gurobi model:
		
		 try {
 
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Building the set partitioning model...");
			 }
			 
			// Create the model:
				
				// Creates the environment:
					
					env = new GRBEnv();
				
				// Creates the model:
					
					GRBModel model = new GRBModel(env);

			 //Create covering/partitioning constraints and objective function

			 	GRBLinExpr[] partitioning_ctr = new GRBLinExpr[nRequests];
				GRBLinExpr[] satFlow_ctr = new GRBLinExpr[nSatellites];
				GRBLinExpr[] maxSE_ctr = new GRBLinExpr[nSatellites];
				GRBLinExpr fleetFE_ctr = new GRBLinExpr();
				GRBLinExpr fleetSE_ctr = new GRBLinExpr();
				GRBLinExpr objectiveExpr = new GRBLinExpr();
				
			// Initializes the constraints:
				
				for(int i = 0;i<nRequests; i++) {
					partitioning_ctr[i] = new GRBLinExpr();
				}
				
				for(int i = 0;i<nSatellites; i++) {
					satFlow_ctr[i] = new GRBLinExpr();
					maxSE_ctr[i] = new GRBLinExpr();
				}

			// Create variables:
				
			int start,end;
			int counter_fe = 0;
			int counter_se = 0;
			for(RoutePool pool : pools) {	
				if(pool.getSatellite() == 0) { // FE
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
						
						// Recover the route:
						
						Route r = iterator.next();
					
						// Capture the route:
						
						ArrayList<Integer> route = (ArrayList<Integer>) r.getRoute();
					
						// Creates the variable x: 
						
						model.addVar(0, 1, (double)r.getAttribute(RouteAttribute.COST),GRB.BINARY, "x_"+counter_fe);
						
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
							
							// Creates the f variable associated with the satellite:
							
							model.addVar(0,data.getQ1(),data.getHandling_costs().get(route.get(i)),GRB.CONTINUOUS,"f_"+counter_fe+"-"+route.get(i));
							
						}
						
						// Updates the counter:
						
						counter_fe++;
					}
				}
			}
			
			// SE routes:
			
			for(RoutePool pool : newPools) {	
				Iterator<Route> iterator = pool.iterator();
				while(iterator.hasNext()) {
				
					// Recover the route:
					
					Route r = iterator.next();
				
					// Creates the variable x: 
					
					model.addVar(0, 1, (double)r.getAttribute(RouteAttribute.COST),GRB.BINARY, "y_"+counter_se);
					
					// Updates the counter:
					
					counter_se++;
				}
			}
			
			model.update();
				
			//Add terms to the covering/partitioning constraints and objective function
				
			counter_fe = 0;
			counter_se = 0;
			for(RoutePool pool : pools) {
				
				// Check if the route pool belongs to the FE or the SE:
				
				if(pool.getSatellite() == 0) { // FE
					
					Iterator<Route> iterator = pool.iterator();
					
					while(iterator.hasNext()) {
					
						// Recover the route:
						
						Route r = iterator.next();

						// Update the objective function:
						
						objectiveExpr.addTerm((double)r.getAttribute(RouteAttribute.COST), model.getVarByName("x_"+counter_fe));
							
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
							
						// Creates the capacity constraint associated with this route:
							
							GRBLinExpr capFE_ctr = new GRBLinExpr();	
						
						// Update FE capacity constraints constraints:
						
							for(int i=start;i<end;i++){
								
								objectiveExpr.addTerm(data.getHandling_costs().get(route.get(i)), model.getVarByName("f_"+counter_fe+"-"+route.get(i)));
								
								// Updates the capacity constraints:
								
								capFE_ctr.addTerm(1, model.getVarByName("f_"+counter_fe+"-"+route.get(i)));
								
								// Updates the flow constraints: 
								
								satFlow_ctr[route.get(i)-1].addTerm(1,model.getVarByName("f_"+counter_fe+"-"+route.get(i)));
								
								
							}
						
							// Updates the constraint associated with the capacity of the route:
							
								capFE_ctr.addTerm(-data.getQ1(),model.getVarByName("x_"+counter_fe));
							
							// Updates the constraint associated with the size of the FE fleet:
								
								fleetFE_ctr.addTerm(1, model.getVarByName("x_"+counter_fe));
					
							// Adds the capacity constraint to the model:
								
								model.addConstr(capFE_ctr,GRB.LESS_EQUAL,0,"FECapacity_"+counter_fe);
								
							// Updates the counter:
							
								counter_fe++;
					}	

				}
				
		 	}
			
			// SE routes:
			
			for(RoutePool pool : newPools) {
				Iterator<Route> iterator = pool.iterator();
				while(iterator.hasNext()) {
				
					// Recover the route:
					
						Route r = iterator.next();
					
					// Updates the objective function:
						
						objectiveExpr.addTerm((double)r.getAttribute(RouteAttribute.COST), model.getVarByName("y_"+counter_se));
						
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
							partitioning_ctr[route.get(i)-1].addTerm(1,model.getVarByName("y_"+counter_se));
						}
					
						satFlow_ctr[pool.getSatellite()-1].addTerm(-(double)r.getAttribute(RouteAttribute.LOAD),model.getVarByName("y_"+counter_se));
						
						maxSE_ctr[pool.getSatellite()-1].addTerm(1,model.getVarByName("y_"+counter_se));
						
						fleetSE_ctr.addTerm(1, model.getVarByName("y_"+counter_se));
				
						counter_se++;
					
				}
			}
			
			//Add remaining constraints to the model
			
			for(int i = 0;i<nRequests; i++) {
				model.addConstr(partitioning_ctr[i],GRB.EQUAL,1,"ServeCustomer_"+i);
			}

			for(int i = 0;i<nSatellites; i++) {
				model.addConstr(satFlow_ctr[i],GRB.EQUAL,0,"FlowAtSatellite_"+i);
				model.addConstr(maxSE_ctr[i], GRB.LESS_EQUAL, data.getMs(), "MaxSERoutesSatellite_"+i);
			}

			// Fleet constraints:
			
			model.addConstr(fleetFE_ctr, GRB.LESS_EQUAL, data.getFleet1(), "MaxFERoutes");
			model.addConstr(fleetSE_ctr, GRB.LESS_EQUAL, data.getFleet2(), "MaxSERoutes");

			// Establish the sign:
			
			model.setObjective(objectiveExpr, GRB.MINIMIZE);
			
			// Update the model:
			
			model.update();
			 
			//Hide the output 
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Printing in the output folder the set partitioning model...");
				 model.write("./output/SetPartitioningModel"+".lp");	
			 }else {
				 model.set(GRB.IntParam.OutputFlag,0);
			 }
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Finished building the set partitioning model...");
				 System.out.println("About to start solving the set partitioning model...");
			 }
			 
			 if(GlobalParameters.EMPHASIZE_FEASIBILITY) {
				 model.set(GRB.IntParam.MIPFocus,1);
			 }
			 
			// Set time limit and # of threads:
				 
				 model.set(GRB.DoubleParam.TimeLimit,GlobalParameters.MSH_ASSEMBLY_TIME_LIMIT);
				 model.set(GRB.IntParam.Threads, GlobalParameters.THREADS);
				 
			 
			//Solve model:
			 
			model.optimize();
			
			 if(GlobalParameters.PRINT_IN_CONSOLE) {
				 System.out.println("Finished building the set partitioning model...");
			 }
			 
			//Store the solution:
			 
			 // Objective function value:
			 
			 objectiveFunction = model.get(GRB.DoubleAttr.ObjVal);
			 
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
								if(model.getVarByName("x_"+counter_fe).get(GRB.DoubleAttr.X) > 0.5){
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
										solution_fe_drops.get(solution_fe_drops.size()-1).add(model.getVarByName("f_"+counter_fe+"-"+r.get(i)).get(GRB.DoubleAttr.X));
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
							if(model.getVarByName("y_"+counter_se).get(GRB.DoubleAttr.X) > 0.5){
								solution_se.add(r);
								solution_se_satellites.add(pool.getSatellite());
								solution_se_identifiers.add(pool.getIdentifier());
							}
							counter_se++;
						}
				 }
 
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		return null;
	}

	
}
