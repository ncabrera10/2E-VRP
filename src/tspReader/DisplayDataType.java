package tspReader;

/**
 * Enumeration of ways a graphical display can be generated from the data.
 * 
 * @author David Hadka
 */
public enum DisplayDataType {
	
	/**
	 * The display is generated from the node coordinates.
	 */
	COORD_DISPLAY,
	
	/**
	 * Explicit coordinates in 2-D are given.
	 */
	TWOD_DISPLAY,
	
	/**
	 * No graphical display is available.
	 */
	NO_DISPLAY

}
