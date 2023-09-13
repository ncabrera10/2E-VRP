package tspReader;

/**
 * The Euclidean distance function.
 * 
 * @author David Hadka
 */
public class EuclideanDistance extends DistanceFunction {
	
	/**
	 * Constructs a new Euclidean distance function.
	 */
	public EuclideanDistance() {
		super();
	}
	
	@Override
	public double distance(int length, double[] position1, double[] position2) {
		double result = 0.0;

		for (int i = 0; i < length; i++) {
			result += Math.pow(position1[i] - position2[i], 2.0);
		}

		return Math.sqrt(result);
	}

}
