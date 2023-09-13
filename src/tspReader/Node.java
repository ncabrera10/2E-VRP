package tspReader;

import java.util.Arrays;

/**
 * Represents a node (i.e., city) or arbitrary dimension.
 * 
 * @author David Hadka
 */
public class Node {
	
	/**
	 * The identifier of this node.
	 */
	private final int id;
	
	/**
	 * The position of this node.
	 */
	private final double[] position;
	
	/**
	 * Constructs a new node with the specified identifier and position.
	 * 
	 * @param id the identifier of this node
	 * @param position the position of this node
	 */
	public Node(int id, double... position) {
		super();
		this.id = id;
		this.position = position;
	}

	/**
	 * Returns the identifier of this node.
	 * 
	 * @return the identifier of this node
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the position of this node.
	 * 
	 * @return the position of this node
	 */
	public double[] getPosition() {
		return position;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		
		for (int i = 0; i < position.length; i++) {
			sb.append(' ');
			sb.append(position[i]);
		}
		
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + Arrays.hashCode(position);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (obj == null) {
			return false;
		}
		
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Node other = (Node) obj;
		
		if (id != other.id) {
			return false;
		}
		
		if (!Arrays.equals(position, other.position)) {
			return false;
		}
		
		return true;
	}

}
