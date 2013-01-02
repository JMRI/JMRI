package jmri.jmrit.operations.rollingstock.cars;

/**
 * A convenient place to access operations xml element and attribute names.
 * 
 * @author Daniel Boudreau Copyright (C) 2012
 * @version $Revision: 22156 $
 * 
 */
public class Xml {

	// Common to operation xml files
	static final String NAME = "name"; // NOI18N
	static final String LOAD = "load"; // NOI18N
	
	static final String TRUE = "true"; // NOI18N
	static final String FALSE = "false"; // NOI18N
	
	// Car.java
	static final String CAR = "car"; // NOI18N
	static final String PASSENGER = "passenger"; // NOI18N
	static final String HAZARDOUS = "hazardous"; // NOI18N
	static final String CABOOSE = "caboose"; // NOI18N
	static final String FRED = "fred"; // NOI18N
	static final String UTILITY = "utility"; // NOI18N
	static final String KERNEL = "kernel"; // NOI18N
	static final String LEAD_KERNEL = "leadKernel"; // NOI18N

	static final String LOAD_FROM_STAGING = "loadFromStaging"; // NOI18N
	static final String WAIT = "wait"; // NOI18N
	static final String ORDER = "order"; // NOI18N
	static final String SCHEDULE_ID = "scheduleId"; // NOI18N
	
	static final String NEXT_LOAD = "nextLoad"; // NOI18N
	static final String NEXT_WAIT = "nextWait"; // NOI18N
	static final String NEXT_DEST_ID = "nextDestId"; // NOI18N
	static final String NEXT_DEST_TRACK_ID = "nextDestTrackId"; // NOI18N
	static final String PREVIOUS_NEXT_DEST_ID = "previousNextDestId"; // NOI18N
	static final String PREVIOUS_NEXT_DEST_TRACK_ID = "previousNextDestTrackId"; // NOI18N	
	static final String RWE_DEST_ID = "rweDestId"; // NOI18N
	static final String RWE_DEST_TRACK_ID = "rweDestTrackId"; // NOI18N
	
	// CarManager.java
	static final String CARS_OPTIONS = "carsOptions"; // NOI18N
	static final String COLUMN_WIDTHS = "columnWidths"; // NOI18N backwards compatible TODO remove in 2013 after production release

	// CarManagerXml.java
	static final String OPTIONS = "options"; // NOI18N
	static final String CARS = "cars"; // NOI18N
	static final String ROAD_NAMES = "roadNames"; // NOI18N
	static final String CAR_TYPES = "carTypes"; // NOI18N
	static final String CAR_COLORS = "carColors"; // NOI18N
	static final String CAR_LENGTHS = "carLengths"; // NOI18N
	static final String CAR_OWNERS = "carOwners"; // NOI18N
	static final String KERNELS = "kernels"; // NOI18N
	
	// CarLoads.java
	static final String LOADS = "loads"; // NOI18N
	static final String NAMES = "names"; // NOI18N old style had a list of names
	static final String DEFAULTS = "defaults"; // NOI18N
	static final String EMPTY = "empty"; // NOI18N	
	static final String TYPE = "type"; // NOI18N
	static final String CAR_LOAD = "carLoad"; // NOI18N
	static final String PRIORITY = "priority"; // NOI18N	
	static final String PICKUP_COMMENT = "pickupComment"; // NOI18N
	static final String DROP_COMMENT = "dropComment"; // NOI18N
	static final String LOAD_TYPE = "loadType"; // NOI18N

}
