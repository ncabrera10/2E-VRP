package core;

/**
 * Defines the interface for route pool filtering rule factories
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @version %I%, %G%
 * @since Oct 12, 2016
 *
 */
public interface PoolFilteringRuleFactory {
	/**
	 * Builds a new instance of a pool filtering rule factory. Implementing classes must ensure that
	 * calls to this method are thread safe.
	 * 
	 * @param rule the name of the rule to build
	 * @throws IllegalArgumentException if the factory cannot build rules of type <code>rule</code>
	 */
	public PoolFilteringRule buildRule(String rule) throws IllegalStateException;
	
}
