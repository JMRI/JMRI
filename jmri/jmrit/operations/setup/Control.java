package jmri.jmrit.operations.setup;

import java.awt.Toolkit;
import java.awt.Dimension;

/**
 * Controls for operations developers. Debug Property changes and instance
 * creation, maximum panel width, etc.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * 
 */
public class Control {
	
	// debug flags
	public static final boolean showProperty = false;
	public static final boolean showInstance = false;
	
	// Default panel width
	public static final int panelWidth = 1025;
	
	// Default panel height
	public static final int panelHeight = 500;
	
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
	// Maximum panel height
	public static final int panelMaxHeight = screenSize.height;
	
	// Default panel edit locations
	public static final int panelX = 0;
	public static final int panelY = 0;
	
	
	// Train build parameters
	public static boolean fullTrainOnly = false;
	
	// Car and Engine attribute maximum string length	
	public static final int MAX_LEN_STRING_ATTRIBUTE = 12;

	// Car and Engine number maximum string length	
	public static final int MAX_LEN_STRING_ROAD_NUMBER = 10; 

	// Location name maximum string length
	public static final int MAX_LEN_STRING_LOCATION_NAME = 25;
	
	// Track name maximum string length
	public static final int MAX_LEN_STRING_TRACK_NAME = 25;
	
	// Track length maximum string length
	public static final int MAX_LEN_STRING_TRACK_LENGTH_NAME = 5;
	
	// Car and Engine length maximum string length
	public static final int MAX_LEN_STRING_LENGTH_NAME = 4;
	
	// Car weight maximum string length
	public static final int MAX_LEN_STRING_WEIGHT_NAME = 4;
	
	// Car and Engine built date maximum string length
	public static final int MAX_LEN_STRING_BUILT_NAME = 4;
}

