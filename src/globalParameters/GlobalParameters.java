package globalParameters;

/**
 * This class contains the main parameters for the MSH algorithm
 * 
 * @author nicolas.cabrera-malik
 *
 */
public class GlobalParameters {

	// Relative paths:
	
		/**
		 * Instance folder where the instances are stored
		 */
		public static final String INSTANCE_FOLDER = GlobalParametersReader.<String>get("INSTANCE_FOLDER", String.class);
		
		/**
		 * Folder in which all the results will be printed
		 */
		public static final String RESULT_FOLDER = GlobalParametersReader.<String>get("RESULT_FOLDER", String.class);
		
	// Precision:
		
		public static final int PRECISION = GlobalParametersReader.<Integer>get("PRECISION", Integer.class);
		public static final double DECIMAL_PRECISION = Math.pow(10, -PRECISION);

	// Experiment parameters:

		/**
		 * Seed for the current run (to allow for replication)
		 */
		public static int SEED = GlobalParametersReader.<Integer>get("SEED", Integer.class);
		
		/**
		 * Number of threads for the sampling phase. How many sampling functions will work in parallel.
		 */
		public static int THREADS = GlobalParametersReader.<Integer>get("THREADS", Integer.class);
		
		/**
		 * Should we print in console some information while we run the algorithm?
		 */
		public static final boolean PRINT_IN_CONSOLE = GlobalParametersReader.<String>get("PRINT_IN_CONSOLE", String.class).equals("false") ? false:true;
		
		/**
		 * Should we print in a txt file the pools?
		 */
		public static final boolean PRINT_POOLS_TO_FILE = GlobalParametersReader.<String>get("PRINT_POOLS_TO_FILE", String.class).equals("false") ? false:true;
		
	// MSH  parameters:
		
		/**
		 * Should CPLEX focus just on finding the best solution it can?
		 */
		public static final boolean GUROBI_EMPHASIZE_FEASIBILITY = GlobalParametersReader.<String>get("GUROBI_EMPHASIZE_FEASIBILITY", String.class).equals("false") ? false:true;
		
		/**
		 * Should we add diversity, by solving the TSP with a different matrix than the one used in the split?
		 */
		public static final boolean TSP_CENTROID_STARTING_POINT = GlobalParametersReader.<String>get("TSP_CENTROID_STARTING_POINT", String.class).equals("false") ? false:true;
		
		public static final boolean TSP_DEPOT_STARTING_POINT = GlobalParametersReader.<String>get("TSP_DEPOT_STARTING_POINT", String.class).equals("false") ? false:true;
		
		/**
		 * Should we use LKH to improve the petals found by the split algorithm?
		 */
		public static final boolean SPLIT_IMPROVE_PETAL_LKH = GlobalParametersReader.<String>get("SPLIT_IMPROVE_PETAL_LKH", String.class).equals("false") ? false:true;
		
		/**
		 * Should we add the routes associated with all the routes in the split graph?
		 */
		public static final boolean SPLIT_ADD_ALL = GlobalParametersReader.<String>get("SPLIT_ADD_ALL", String.class).equals("false") ? false:true;
		
		/**
		 * Should we create copies of the routes created for one satellite, to the others?
		 */
		public static final boolean SPLIT_DUPLICATE_SATELLITE_ROUTES = GlobalParametersReader.<String>get("SPLIT_DUPLICATE_SATELLITE_ROUTES", String.class).equals("false") ? false:true;
		
		/**
		 * Should we try to solve the split using different capacities?
		 */
		public static final boolean SPLIT_TRY_CAPACITIES = GlobalParametersReader.<String>get("SPLIT_TRY_CAPACITIES", String.class).equals("false") ? false:true;
		
		/**
		 * Maximum number of routes allowed in each pool
		 */
		public static final int MSH_MAX_POOL_SIZE = GlobalParametersReader.<Integer>get("MSH_MAX_POOL_SIZE", Integer.class);
		
		/**
		 * Maximum number of iterations for the MSH
		 */
		public static final int MSH_NUM_ITERATIONS = GlobalParametersReader.<Integer>get("MSH_NUM_ITERATIONS", Integer.class);
		
		/**
		 * Time limit for the sampling phase in seconds
		 */
		public static final int MSH_SAMPLING_TIME_LIMIT = GlobalParametersReader.<Integer>get("MSH_SAMPLING_TIME_LIMIT", Integer.class);
		
		/**
		 * Randomization factor (large value) for the tsp heuristics
		 */
		public static final int MSH_RANDOM_FACTOR_HIGH = GlobalParametersReader.<Integer>get("MSH_RANDOM_FACTOR_HIGH", Integer.class);
		
		/**
		 * Randomization factor (small value) for the tsp heuristics
		 */
		public static final int MSH_RANDOM_FACTOR_LOW = GlobalParametersReader.<Integer>get("MSH_RANDOM_FACTOR_LOW", Integer.class);
		
		/**
		 * Time limit for the assembly phase in seconds
		 */
		public static final int MSH_ASSEMBLY_TIME_LIMIT = GlobalParametersReader.<Integer>get("MSH_ASSEMBLY_TIME_LIMIT", Integer.class);
		
}
