package core;

import java.util.Random;

/**
 * Iplements a sequence-first, cluster-second heuristic. The heuristic generates a giant TSP visition all customers in the instance
 * and then splits the tour into feasible routes for the VRP in hand.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Jan 21, 2016
 *
 */
public class OrderFirstSplitSecondHeuristic implements Algorithm, RandomizedHeuristic{
	/**
	 * The split procedure
	 */
	private final Split s;
	/**
	 * The TSP heuristic
	 */
	private final TSPHeuristic h;	

	public OrderFirstSplitSecondHeuristic(final TSPHeuristic h, final Split s){
		this.h=h;
		this.s=s;
	}

	@Override
	public Solution run() {
		return s.split(h.run());
	}

	@Override
	public void setRandomized(boolean flag) {
		if(h instanceof RandomizedHeuristic)
			((RandomizedHeuristic) h).setRandomized(flag);
	}

	@Override
	public void setRandomGen(Random rnd) {
		if(h instanceof RandomizedHeuristic)
			((RandomizedHeuristic) h).setRandomGen(rnd);
	}

	@Override
	public boolean isRandomized() {
		if(h instanceof RandomizedHeuristic)
			return ((RandomizedHeuristic) h).isRandomized();
		else
			return false;
	}

	@Override
	public void setRandomizationFactor(int K) {
		if(h instanceof RandomizedHeuristic)
			((RandomizedHeuristic) h).setRandomizationFactor(K);
	}

}
