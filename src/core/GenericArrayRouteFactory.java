package core;

/**
 * Implements an {@link ArrayRoute} factory
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Aug 22, 2016
 *
 */
public class GenericArrayRouteFactory implements RouteFactory{

	@Override
	public final Route buildRoute() {
		return new GenericArrayRoute();
	}

}
