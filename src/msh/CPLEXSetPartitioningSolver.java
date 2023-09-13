package msh;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import core.Route;
import core.RouteAttribute;
import core.RoutePool;
import core.Solution;
import dataStructures.DataHandler;
import globalParameters.GlobalParameters;

/**
 * This class solves the set partitioning model to assembly the final solution. 
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class CPLEXSetPartitioningSolver implements AssemblyFunction{

	protected int nRequests;
	protected int nSatellites;
	protected boolean hasTerminals;
	protected IloCplex cplex;
	protected IloNumVar[] x;
	protected IloNumVar[] y;
	protected IloNumVar[] f;
	protected DataHandler data;

	public double objectiveFunction;
	public ArrayList<Route> solution_fe;
	public ArrayList<Route> solution_se;
	public ArrayList<ArrayList<Double>> solution_fe_drops;
	public ArrayList<Integer> solution_se_satellites;
	
	public CPLEXSetPartitioningSolver(int nRequests, int nSatellites,boolean hasTerminals,DataHandler data){
		this.nRequests=nRequests;
		this.nSatellites=nSatellites;
		this.hasTerminals=hasTerminals;
		this.data=data;
	}

	@Override
	public Solution assembleSolution(Solution bound, ArrayList<RoutePool> pools) {
		
		 //Total number of routes:
		
		int size_fe = 0;
		int size_se = 0;
		
		for(RoutePool pool : pools) {
			if(pool.getSatellite() == 0) {
				size_fe += pool.size();
			}else {
				size_se += pool.size();
			}
			
		}
		
		if(GlobalParameters.PRINT_POOLS_TO_FILE) {
			String path = "./output/"+ "Pool"+ ".txt";
			try {
				PrintWriter pw = new PrintWriter(new File(path));
				for(RoutePool pool : pools) {
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
								
								of.addTerm(data.getHandling_costs().get(route.get(i)-1), f[route.get(i) + (counter_fe * nSatellites) - 1]);
								
							}
						
							capFE_constraints[counter_fe].addTerm(-data.getQ1(),x[counter_fe]);
							fleetFE_constraints.addTerm(1, x[counter_fe]);
					
							counter_fe++;
					}
				}else {
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
					 }else {
						 
						 Iterator<Route> iterator = pool.iterator();
							
							while(iterator.hasNext()) {
							
								Route r = iterator.next();
								if(cplex.getValue(y[counter_se]) > 0.5){
									solution_se.add(r);
									solution_se_satellites.add(pool.getSatellite());
								}
								counter_se++;
							}
					 }
				 }	 
 
		} catch (IloException e) {
			e.printStackTrace();
		}
		 
		
		return null;
	}
	
}
