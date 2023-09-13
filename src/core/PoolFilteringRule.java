package core;

import java.util.Map;

public interface PoolFilteringRule {

	public Route filter(Route r, Map<Integer,Route> pool);
	
}
