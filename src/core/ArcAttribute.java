package core;

/**
 * This class enumares the attributes of an arc
 * @author nicolas.cabrera-malik
 *
 */
public enum ArcAttribute {
	/**
	 * The distance linking the tail and head node. Default value=Double.NaN
	 */
	DISTANCE,
	/**
	 * The travel time of the arc. In general it is assumed that it equals the distance. Default value=Double.Nan
	 */
	TRAVEL_TIME,
}
