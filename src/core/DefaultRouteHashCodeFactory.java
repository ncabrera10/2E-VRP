package core;

/**
 * Implements the defult {@link RouteHashCodeFactory}. The default factory builds new objects of class {@link SequenceRouteHashCode}.
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 12, 2016
 *
 */
public class DefaultRouteHashCodeFactory implements RouteHashCodeFactory{

	@Override
	public synchronized RouteHashCode build() {
		return new SequenceRouteHashCode();
	}

}
