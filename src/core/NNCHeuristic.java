package core;

import java.util.ArrayList;
import java.util.Random;

/**
 * Implements a nearest neighbor clustering heuristic.</br>
 * </br>
 * --- DEFAULT BEHAVIOR ---
 * <ol>
 * 	<li>{@link #run} method: if the heuristic is running in randomized mode and the random number generator has not been defined (by calling method {@link #setRandomGen(Random)}),
 * the random number generator is initialized with an instance of {@link Random} built using the default constructor.
 * <li> The default randomization factor is K=1.
 * </ol>
 * --- ASSUMPTIONS ---
 * <ol>
 * 	<li>The {@link #run} method assumes that the default start node is node 0.
 * </ol>
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 17, 2016
 *
 */
public class NNCHeuristic implements TSPHeuristic, RandomizedHeuristic {

	/**
	 * The nearest neighbor finder
	 */
	private final NNCFinder finder;
	/**
	 * The number of nodes in the instance
	 */
	private final int n;
	/**
	 * The random number generator
	 */
	private Random rnd=null;
	/**
	 * The starting node
	 */
	private int initNode=0;
	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	/**
	 * True if the heuristic runs in randomized mode and false otherwise
	 */
	private boolean randomized=false;
	/**
	 * Randomization factor
	 */
	private int K=1;
	/**
	 * The initial route that should be completed by the heuristic
	 */
	private Route initRoute=null;
	/**
	 * True if the route has been initialized (by calls to {@link #setInitNode(int) or #setInitRoute(Route)) and false otherwise
	 */
	private boolean initialized=false;
	
	private double Q;
	
	private ArrayList<Double> demands;

	/**
	 * Constructs a new nearest neighbor heuristic
	 * @param distances
	 */
	public NNCHeuristic(DistanceMatrix distances, ArrayList<Double> demands, double Q){
		this.Q = Q;
		this.demands = demands;
		this.finder=new NNCFinder(distances, distances.size(), demands);
		this.n=distances.size();
		this.distances=distances;
	}

	@Override
	public synchronized TSPSolution run() {

		//Initialize OF
		double of=0;

		//Initialized random number generator (if needed)
		if(randomized&&rnd==null)
			rnd=new Random();

		//Initialize the tour
		final TSPSolution tour = this.initTour();
		final int init=tour.get(0);

		//Initialize node status
		int toRoute=n;
		boolean[] nodeStatus=new boolean[toRoute];
		for(int i=0;i<tour.size();i++){
			nodeStatus[tour.get(i)]=true;
			toRoute--;
			//sumar el fo //TODO
			if(i < tour.size()-1) {
				of += distances.getDistance(tour.get(i), tour.get(i+1));
			}
		}
	
		
		//complete the tour
		int k=1;
		double remQ = Q;
		boolean feasible = true; //Is it possible to add another node?
		
		while(toRoute>0 && remQ > 0 && feasible){
			//Find the nearest neighbor
			int i=tour.get(tour.size()-1);
			if(remQ == Q && toRoute+1 == n) {
				k=1+rnd.nextInt(toRoute); //The first node is picked randomly!
			}else {
				k=1+rnd.nextInt(Math.min(K,toRoute));
			}
			
			int nn=finder.findNN(i, nodeStatus, k,remQ);
			if(nn != -1) {
				remQ = remQ - demands.get(nn-1);
				tour.add(nn);
				of+=distances.getDistance(tour.get(tour.size()-2),tour.get(tour.size()-1));
				nodeStatus[nn]=true;
				toRoute--;	
			}else {
				feasible = false;
				//remQ = Q;
			}
			
					
		}
		tour.add(init);
		of+=distances.getDistance(tour.get(tour.size()-2),tour.get(tour.size()-1));
		tour.setOF(of);
		//System.out.println(tour);
		//System.exit(0);
		return tour;
	}

	@Override
	public synchronized void setRandomized(boolean flag) {
		this.randomized=flag;
	}

	@Override
	public synchronized void setRandomGen(Random rnd) {
		this.rnd=rnd;
	}

	@Override
	public synchronized boolean isRandomized() {
		return this.randomized;
	}

	@Override
	public synchronized void setRandomizationFactor(int K) {
		this.K=K;
	}

	@Override
	public synchronized void setInitNode(int i) {
		if(this.initialized)
			throw new IllegalStateException("The heuristic has been already initialized by a call to setInitRoute(Route)");
		this.initNode=i;
		this.initialized=true;
	}

	@Override
	public synchronized void setInitRoute(Route r) {
		if(this.initialized)
			throw new IllegalStateException("The heuristic has been already initialized by a call to setInitNote(int)");
		if(r.get(0)!=r.get(r.size()-1))
			throw new IllegalArgumentException("The route must start and end at the same node. Starting and ending nodes are "+r.get(0)+" and  "+r.get(r.size()-1));
		this.initRoute=r.getCopy();
		this.initialized=true;
	}

	/**
	 * Initializes the solution (i.e., an incomplete TSP tour).
	 * @return an initialized TSP solution
	 */
	private TSPSolution initTour(){
		final TSPSolution tour = new TSPSolution();
		//Case 1: initialized with a route
		if(this.initRoute!=null){
			initRoute.remove(initRoute.size()-1); //the tour is still open
			tour.setRoute(initRoute);			
		}
		//Case 2: initialized with a node or not initialized
		else{
			final int init;
			if(randomized&&!initialized){
				if(rnd==null)	//Initialize the random number generator if needed. This will never happen if the method remains private, but I'm thinking about pushing it up to a superclass.
					rnd=new Random();
				init=rnd.nextInt(n); //Randomly initialized
			}
			else
				init=this.initNode; //Initialized with a given node or with the default node (i.e., node 0)
			tour.add(init);
		}
		return tour;
	}

}
