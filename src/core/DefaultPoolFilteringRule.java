package core;

import java.util.Map;

/**
 * Implements the default route pool filtering rule. According to this rule, no filter is applied to a route
 * before storing it in a {@link RoutePool}.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 12, 2016
 *
 */
public class DefaultPoolFilteringRule implements PoolFilteringRule{

	@Override
	public Route filter(Route r, Map<Integer, Route> pool) {
		return r;
	}

}