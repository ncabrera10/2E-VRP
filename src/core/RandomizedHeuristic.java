package core;

import java.util.Random;

/**
 * Defines the interface of randomized heuristics
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 18, 2016
 *
 */
public interface RandomizedHeuristic extends Algorithm{
	/**
	 * Activates the randomized mode
	 * @param flag true if the randomized mode is to be turned on and false otherwise
	 */
	public void setRandomized(boolean flag);
	/**
	 * Sets a new random number generator
	 * @param rnd the random number generator to set
	 */
	public void setRandomGen(Random rnd);
	/**
	 * 
	 * @return true if the randomized mode is turned on and false otherwise
	 */
	public boolean isRandomized();
	/**
	 * Sets the randomization factor of the heuristic
	 * @param K the new randomization factor
	 */
	public void setRandomizationFactor(int K);

}
