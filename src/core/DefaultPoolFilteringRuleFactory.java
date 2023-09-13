package core;

public class DefaultPoolFilteringRuleFactory implements PoolFilteringRuleFactory{

	@Override
	public synchronized PoolFilteringRule buildRule(String rule) throws IllegalStateException {
		if(!rule.equals("Default"))
			throw new IllegalStateException("This factory cannot build object of type "+rule);
		return new DefaultPoolFilteringRule();	
	}

}
