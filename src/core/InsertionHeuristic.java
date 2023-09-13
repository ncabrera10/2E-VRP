package core;

import java.util.Hashtable;
import java.util.Random;

public class InsertionHeuristic implements TSPHeuristic, RandomizedHeuristic {

	/**
	 * The insertion finder
	 */
	private final InsertionFinder finder;
	
	/**
	 * The number of nodes in the instance
	 */
	private final int n;
	
	/**
	 * The random number generator
	 */
	private Random rnd = null;
	/**
	 * The starting node
	 */
	private int initNode = 0;
	/**
	 * The distance matrix
	 */
	private final DistanceMatrix distances;
	/**
	 * True if the heuristic runs in randomized mode and false otherwise
	 */
	private boolean randomized = false;
	/**
	 * Randomization factor
	 */
	private int K = 1;
	/**
	 * The initial route that should be completed by the heuristic
	 */
	private Route initRoute = null;
	/**
	 * True if the route has been initialized (by calls to {@link #setInitNode(int) or #setInitRoute(Route)) and false otherwise
	 */
	private boolean initialized = false;
	
	private String type;

	public InsertionHeuristic(DistanceMatrix distances, String type) {
		this.type = type;
		this.distances = distances;
		this.n = distances.size();
		if(this.type.equals("NEAREST_INSERTION")) {
			this.finder = new NIFinder(this.distances);
		}else if(this.type.equals("FARTHEST_INSERTION")){
			this.finder = new FIFinder(this.distances);
		}else {
			this.finder = new BIFinder(this.distances);
		}
		
	}
	
	@Override
	public void setRandomized(boolean flag) {
		this.randomized=flag;		
	}

	@Override
	public void setRandomGen(Random rnd) {
		this.rnd=rnd;	
	}

	@Override
	public boolean isRandomized() {
		return randomized;
	}

	@Override
	public void setRandomizationFactor(int K) {
		this.K=K;		
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
		
		//Create a hashtable with the routed nodes and the nodes to be routed
		Hashtable<Integer,Integer> nodesToRoute = new Hashtable<Integer,Integer>();
		Hashtable<Integer,Integer> routedNodes = new Hashtable<Integer,Integer>();
		
		//Initialize the routed and still not routed nodes
		for(int i=0;i<tour.size();i++){
			routedNodes.put(tour.get(i), tour.get(i));
			if(i < tour.size()-1) {
				of += distances.getDistance(tour.get(i), tour.get(i+1));
			}
		}
		if(this.initRoute!=null) {
			of += distances.getDistance(tour.get(tour.size()-1), init);
		}
		
		for(int i = 0; i < n;i++) {
			if(!routedNodes.containsKey(i)) {
				nodesToRoute.put(i,i);
			}
		}
		
		//Complete the tour
		
		while(nodesToRoute.size()>0){
			
			// Calculate the k
			
			int k = 1;
			if(randomized) {
				k = 1+rnd.nextInt(Math.min(nodesToRoute.size(),K));
			}
			
			//Add the initial node to the end of the tour
			tour.add(init);

			//Search for the best insertion
			Insertion nextInsertion = this.finder.findInsertion(tour, routedNodes, nodesToRoute, k);
			int pos = nextInsertion.getPosition();
			
			//Insert the node in the tour
			tour.insert(nextInsertion.getNode(),pos);
			
			//Update the routed nodes
			
			routedNodes.put(nextInsertion.getNode(),nodesToRoute.remove(nextInsertion.getNode()));
		
			//Update the total cost of the tour:
			
			of+=distances.getDistance(tour.get(pos-1),tour.get(pos));
			of+=distances.getDistance(tour.get(pos),tour.get(pos+1));
			of-=distances.getDistance(tour.get(pos-1),tour.get(pos+1));
			
			// Update the tour
			
			tour.remove(tour.size()-1);
		}
		
		tour.add(init);
		tour.setOF(of);
		return tour;
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
			if(randomized&&!initialized)
				init=rnd.nextInt(n); //Randomly initialized
			else
				init=this.initNode; //Initialized with a given node or with the default node (i.e., node 0)
			tour.add(init);
		}
		return tour;
	}

}
