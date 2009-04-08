package jmri.jmrit.operations.setup;

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
	
	// Maximum panel width
	public static final int panelWidth = 1025;
	
	// Default panel height
	public static final int panelHeight = 500;
	
	// Default panel edit locations
	public static final int panelX = 0;
	public static final int panelY = 0;
	
	
	// Train build parameters
	public static final boolean fullTrainOnly = false;
	
	// Car and Engine attribute maximum string length
	
	public static final int MAX_LEN_STRING_ATTRIBUTE = 12; // the maximum length of any attribute string


}

