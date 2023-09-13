package core;

import java.util.List;


public class SequenceRouteHashCode implements RouteHashCode{

	@Override
	public int compute(Route r) {
		List<Integer> sequence=r.getRoute();
		StringBuilder sb=new StringBuilder(sequence.size()*2);
		for(Integer i:sequence){
			sb.append(i+",");
		}
		return sb.toString().hashCode();
	}
}
