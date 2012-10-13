package jmri.jmrit.operations.setup;

/**
 * Operations settings. 
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
//import java.awt.Dimension;
//import java.awt.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmris.AbstractOperationsServer;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.trains.TrainLogger;

import org.jdom.Element;


public class Setup {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");
		
	// scale ratios from NMRA
	private static final int Z_RATIO = 220;
	private static final int N_RATIO = 160;
	private static final int TT_RATIO = 120;
	private static final int OO_RATIO = 76;			//actual ratio 76.2
	private static final int HO_RATIO = 87;
	private static final int S_RATIO = 64;
	private static final int O_RATIO = 48;
	private static final int G_RATIO = 32;			// NMRA #1
	
	// initial weight in milli ounces from NMRA
	private static final int Z_INITIAL_WEIGHT = 364;		// not specified by NMRA
	private static final int N_INITIAL_WEIGHT = 500;
	private static final int TT_INITIAL_WEIGHT = 750;
	private static final int HOn3_INITIAL_WEIGHT = 750;
	private static final int OO_INITIAL_WEIGHT = 750;	// not specified by NMRA
	private static final int HO_INITIAL_WEIGHT = 1000;
	private static final int Sn3_INITIAL_WEIGHT = 1000;
	private static final int S_INITIAL_WEIGHT = 2000;
	private static final int On3_INITIAL_WEIGHT = 1500;
	private static final int O_INITIAL_WEIGHT = 5000;
	private static final int G_INITIAL_WEIGHT = 10000;		// not specified by NMRA
	
	// additional weight in milli ounces from NMRA
	private static final int Z_ADD_WEIGHT = 100;			// not specified by NMRA
	private static final int N_ADD_WEIGHT = 150;
	private static final int TT_ADD_WEIGHT = 375;
	private static final int HOn3_ADD_WEIGHT = 375;
	private static final int OO_ADD_WEIGHT = 500;		// not specified by NMRA
	private static final int HO_ADD_WEIGHT = 500;
	private static final int Sn3_ADD_WEIGHT = 500;
	private static final int S_ADD_WEIGHT = 500;
	private static final int On3_ADD_WEIGHT = 750;
	private static final int O_ADD_WEIGHT = 1000;
	private static final int G_ADD_WEIGHT = 2000;		// not specified by NMRA
	
	// actual weight to tons conversion ratios (based on 40' boxcar at ~80 tons)
	private static final int Z_RATIO_TONS = 130;
	private static final int N_RATIO_TONS = 80;
	private static final int TT_RATIO_TONS = 36;
	private static final int HOn3_RATIO_TONS = 20;
	private static final int OO_RATIO_TONS = 20;
	private static final int HO_RATIO_TONS = 20;		// 20 tons per ounce
	private static final int Sn3_RATIO_TONS = 16;
	private static final int S_RATIO_TONS = 14;
	private static final int On3_RATIO_TONS = 8;
	private static final int O_RATIO_TONS = 5;
	private static final int G_RATIO_TONS = 2;			
	
	public static final int Z_SCALE = 1;
	public static final int N_SCALE = 2;
	public static final int TT_SCALE = 3;
	public static final int HOn3_SCALE = 4;
	public static final int OO_SCALE = 5;			
	public static final int HO_SCALE = 6;
	public static final int Sn3_SCALE = 7;
	public static final int S_SCALE = 8;
	public static final int On3_SCALE = 9;
	public static final int O_SCALE = 10;
	public static final int G_SCALE = 11;			// NMRA #1
	
	public static final int EAST = 1;		// train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;
	
	public static final String EAST_DIR = rb.getString("East");
	public static final String WEST_DIR = rb.getString("West");
	public static final String NORTH_DIR = rb.getString("North");
	public static final String SOUTH_DIR = rb.getString("South");
	
	public static final String DESCRIPTIVE = "Descriptive"; // Car types
	public static final String AAR = "ARR Codes"; // Car types
	
	public static final String COURIER = "Courier"; // printer fonts
	public static final String GARAMOND = "Garamond"; // printer fonts
	public static final String MONOSPACED = "Monospaced"; // printer fonts
	public static final String SANSERIF = "SansSerif";
	public static final String SERIF = "Serif";
	
	public static final String PORTRAIT = rb.getString("Portrait");
	public static final String LANDSCAPE = rb.getString("Landscape");
	public static final String HANDHELD	= rb.getString("HandHeld");
	
	public static final String LENGTHABV =rb.getString("LengthSymbol");
	
	public static final String BUILD_REPORT_MINIMAL = "1";
	public static final String BUILD_REPORT_NORMAL = "3";
	public static final String BUILD_REPORT_DETAILED = "5";
	public static final String BUILD_REPORT_VERY_DETAILED = "7";
	
	public static final String ROAD = rb.getString("Road");		// the supported message format options
	public static final String NUMBER = rb.getString("Number");
	public static final String TYPE = rb.getString("Type");
	public static final String MODEL = rb.getString("Model");
	public static final String LENGTH = rb.getString("Length");
	public static final String LOAD = rb.getString("Load");
	public static final String COLOR = rb.getString("Color");
	public static final String DESTINATION = rb.getString("Destination");
	public static final String DEST_TRACK = rb.getString("DestAndTrack");
	public static final String LOCATION = rb.getString("Location");
	public static final String CONSIST = rb.getString("Consist");
	public static final String KERNEL = rb.getString("Kernel");
	public static final String OWNER = rb.getString("Owner");
	public static final String RWE = rb.getString("RWE");
	public static final String COMMENT = rb.getString("Comment");
	public static final String DROP_COMMENT = rb.getString("DropComment");
	public static final String PICKUP_COMMENT = rb.getString("PickupComment");
	public static final String HAZARDOUS = rb.getString("Hazardous");
	public static final String NONE = " ";				// none has be a character or a space
	public static final String BOX = " [ ] ";
	
	// these are for the utility printing when using tabs
	public static final String NO_ROAD = "NO_ROAD";
	public static final String NO_NUMBER = "NO_NUMBER";
	public static final String NO_COLOR = "NO_COLOR";
	
	// truncated manifests
	public static final String NO_DESTINATION = "NO_DESTINATION";
	public static final String NO_DEST_TRACK = "NO_DEST_TRACK";
	public static final String NO_LOCATION = "NO_LOCATION";
	
	public static final String BLACK = rb.getString("Black");	// the supported pick up and set out colors
	public static final String BLUE = rb.getString("Blue");
	public static final String GREEN = rb.getString("Green");
	public static final String RED = rb.getString("Red");
	
	// Unit of Length
	public static final String FEET = rb.getString("Feet");
	public static final String METER = rb.getString("Meter");
	
	private static int scale = HO_SCALE;	// Default scale	
	private static int ratio = HO_RATIO;
	private static int ratioTons = HO_RATIO_TONS;
	private static int initWeight = HO_INITIAL_WEIGHT;
	private static int addWeight = HO_ADD_WEIGHT;
	private static String railroadName ="";
	private static int traindir = EAST+WEST+NORTH+SOUTH;
	private static int trainLength = 1000;
	private static int engineSize = 6;
	private static int carMoves = 5;
	private static String carTypes = DESCRIPTIVE;
	private static String ownerName ="";
	private static String fontName = MONOSPACED;
	private static int fontSize = 10;
	private static String manifestOrientation = PORTRAIT;
	private static String switchListOrientation = PORTRAIT;
	private static String pickupColor = BLACK;
	private static String dropColor = BLACK;
	private static String localColor = BLACK;
	private static String[] pickupEngineMessageFormat = {ROAD, NUMBER, NONE, MODEL, NONE, NONE, LOCATION, COMMENT};
	private static String[] dropEngineMessageFormat = {ROAD, NUMBER, NONE, MODEL, NONE, NONE, DESTINATION, COMMENT};
	private static String[] pickupCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION, COMMENT, PICKUP_COMMENT};
	private static String[] dropCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, DESTINATION, COMMENT, DROP_COMMENT};
	private static String[] localMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION, DESTINATION, COMMENT};
	private static String[] switchListPickupCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION, COMMENT, PICKUP_COMMENT};
	private static String[] switchListDropCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, DESTINATION, COMMENT, DROP_COMMENT};
	private static String[] switchListLocalMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION, DESTINATION, COMMENT};
	private static String[] missingCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, COMMENT};
	private static String pickupEnginePrefix = BOX + rb.getString("PickUpPrefix");
	private static String dropEnginePrefix = BOX + rb.getString("SetOutPrefix");
	private static String pickupCarPrefix = BOX + rb.getString("PickUpPrefix");
	private static String dropCarPrefix = BOX + rb.getString("SetOutPrefix");
	private static String localPrefix = BOX + rb.getString("LocalCarPrefix");
	private static String switchListPickupCarPrefix = BOX + rb.getString("PickUpPrefix");
	private static String switchListDropCarPrefix = BOX + rb.getString("SetOutPrefix");
	private static String switchListLocalPrefix = BOX + rb.getString("LocalCarPrefix");	
	private static String miaComment = rb.getString("misplacedCars");
	private static String hazardousMsg = "("+rb.getString("Hazardous")+")";
	private static String logoURL ="";
	private static String panelName ="Panel";
	private static String buildReportLevel = BUILD_REPORT_NORMAL;	
	private static int carSwitchTime = 3;		// how long it take to move a car
	private static int travelTime = 4;// how long it take a train to move one location
	private static String yearModeled = ""; 	// year being modeled
	private static String lengthUnit = FEET;
	private static String iconNorthColor ="";
	private static String iconSouthColor ="";
	private static String iconEastColor ="";
	private static String iconWestColor ="";
	private static String iconLocalColor ="";
	private static String iconTerminateColor ="";
	
	private static boolean tab = false;
	private static boolean manifestEditorEnabled = false;	// when true use text editor to view build report
	private static boolean switchListSameManifest = true;	// when true switch list format is the same as the manifest
	private static boolean manifestTruncated = false;		// when true, manifest is truncated if switch list is available
	private static boolean manifestDepartureTime = false;	// when true, manifest shows train's departure time
	private static boolean switchListRealTime = true;		// when true switch list only show work for built trains
	private static boolean switchListAllTrains = true;		// when true show all trains that visit the location
	private static boolean switchListPage = false;			// when true each train has its own page
	private static boolean buildReportEditorEnabled = false;	// when true use text editor to view build report
	
	private static boolean enableTrainIconXY = true;
	private static boolean appendTrainIcon = false;		//when true, append engine number to train name
		
	private static boolean mainMenuEnabled = false;		//when true add operations menu to main menu bar
	private static boolean closeWindowOnSave = false;	//when true, close window when save button is activated
	private static boolean autoSave = true;				//when true, automatically save files if modified
	private static boolean autoBackup = true;			//when true, automatically backup files
	private static boolean enableValue = false;			//when true show value fields for rolling stock
	private static String labelValue = rb.getString("Value");
	private static boolean enableRfid = false;			//when true show RFID fields for rolling stock
	private static String labelRfid = rb.getString("RFID");
	private static boolean carRoutingEnabled = true;	//when true enable car routing
	private static boolean carRoutingStaging = false;	//when true staging tracks can be used for car routing
	private static boolean forwardToYardEnabled = true;	//when true forward car to yard if track is full
	private static boolean carLogger = false;			//when true car logger is enabled
	private static boolean engineLogger = false;		//when true engine logger is enabled
	private static boolean trainLogger = false;			//when true train logger is enabled
	
	private static boolean aggressiveBuild = false;		//when true subtract car length from track reserve length
	private static boolean allowLocalInterchangeMoves = false;	// when true local interchange to interchange moves are allowed
	private static boolean allowLocalYardMoves = false;		// when true local yard to yard moves are allowed
	private static boolean allowLocalSidingMoves = false;	// when true local spur to spur moves are allowed
	
	private static boolean trainIntoStagingCheck = true;	// when true staging track must accept train's rolling stock types and roads
	private static boolean trackImmediatelyAvail = false; 	// when true staging track is immediately available for arrivals after a train is built
	private static boolean allowCarsReturnStaging = false;	// when true allow cars on a turn to return to staging if necessary (prevent build failure)
	private static boolean promptFromStaging = false;		// when true prompt user to specify which departure staging track to use
	private static boolean promptToStaging = false;			// when true prompt user to specify which arrival staging track to use
	
	private static boolean generateCsvManifest = false;		// when true generate csv manifest
	private static boolean generateCsvSwitchList = false;	// when true generate csv switch list
	private static boolean enableVsdPhysicalLocations = false;
	
	private static boolean printLocationComments = false;	// when true print location comments on the manifest
	private static boolean printRouteComments = false;		// when true print route comments on the manifest
	private static boolean printLoadsAndEmpties	= false;	// when true print Loads and Empties on the manifest
	private static boolean printTimetableName	= false;	// when true print timetable name on manifests and switch lists
	private static boolean use12hrFormat		= false;	// when true use 12hr rather than 24hr format
	private static boolean printValid			= true;		// when true print out the valid time and date

	public static boolean isMainMenuEnabled(){
		OperationsSetupXml.instance(); // load file
		return mainMenuEnabled;
	}
	
	public static void setMainMenuEnabled(boolean enabled){
		mainMenuEnabled = enabled;
	}
	
	public static boolean isCloseWindowOnSaveEnabled(){
		return closeWindowOnSave;
	}
	
	public static void setCloseWindowOnSaveEnabled(boolean enabled){
		closeWindowOnSave = enabled;
	}
	
	public static boolean isAutoSaveEnabled(){
		return autoSave;
	}
	
	public static void setAutoSaveEnabled(boolean enabled){
		boolean old = autoSave;
		autoSave = enabled;
		if (!old && enabled)
			new AutoSave();
	}
	
	public static boolean isAutoBackupEnabled(){
		return autoBackup;
	}
	
	public static void setAutoBackupEnabled(boolean enabled){
		// Do an autoBackup only if we are changing the setting from false to
		// true.
		if (enabled && !autoBackup)
			try {
				new AutoBackup().autoBackup();
			} catch (Exception ex) {
				log.debug("Autobackup after setting AutoBackup flag true", ex);
			}

		autoBackup = enabled;
	}
	
	public static boolean isValueEnabled(){
		return enableValue;
	}
	
	public static void setValueEnabled(boolean enabled){
		enableValue = enabled;
	}
	
	public static String getValueLabel(){
		return labelValue;
	}
	
	public static void setValueLabel(String label){
		labelValue = label;
	}
	
	public static boolean isRfidEnabled(){
		return enableRfid;
	}
	
	public static void setRfidEnabled(boolean enabled){
		enableRfid = enabled;
	}
	
	public static String getRfidLabel(){
		return labelRfid;
	}
	
	public static void setRfidLabel(String label){
		labelRfid = label;
	}
	
	
	public static boolean isCarRoutingEnabled(){
		return carRoutingEnabled;
	}
	
	public static void setCarRoutingEnabled(boolean enabled){
		carRoutingEnabled = enabled;
	}
	
	public static boolean isCarRoutingViaStagingEnabled(){
		return carRoutingStaging;
	}
	
	public static void setCarRoutingViaStagingEnabled(boolean enabled){
		carRoutingStaging = enabled;
	}
	
	public static boolean isForwardToYardEnabled(){
		return forwardToYardEnabled;
	}
	
	public static void setForwardToYardEnabled(boolean enabled){
		forwardToYardEnabled = enabled;
	}
	
	public static boolean isBuildAggressive(){
		return aggressiveBuild;
	}
	
	public static void setBuildAggressive(boolean enabled){
		aggressiveBuild = enabled;
	}
	
	public static boolean isLocalInterchangeMovesEnabled(){
		return allowLocalInterchangeMoves;
	}
	
	public static void setLocalInterchangeMovesEnabled(boolean enabled){
		allowLocalInterchangeMoves = enabled;
	}
	
	public static boolean isLocalYardMovesEnabled(){
		return allowLocalYardMoves;
	}
	
	public static void setLocalYardMovesEnabled(boolean enabled){
		allowLocalYardMoves = enabled;
	}
	
	public static boolean isLocalSidingMovesEnabled(){
		return allowLocalSidingMoves;
	}
	
	public static void setLocalSidingMovesEnabled(boolean enabled){
		allowLocalSidingMoves = enabled;
	}
	
	public static boolean isTrainIntoStagingCheckEnabled(){
		return trainIntoStagingCheck;
	}
	
	public static void setTrainIntoStagingCheckEnabled(boolean enabled){
		trainIntoStagingCheck = enabled;
	}
	
	public static boolean isStagingTrackImmediatelyAvail(){
		return trackImmediatelyAvail;
	}
	
	public static void setStagingTrackImmediatelyAvail(boolean enabled){
		trackImmediatelyAvail = enabled;
	}
	
	public static boolean isAllowReturnToStagingEnabled(){
		return allowCarsReturnStaging;
	}
	
	public static void setAllowReturnToStagingEnabled(boolean enabled){
		allowCarsReturnStaging = enabled;
	}
	
	public static boolean isPromptFromStagingEnabled(){
		return promptFromStaging;
	}
	
	public static void setPromptFromStagingEnabled(boolean enabled){
		promptFromStaging = enabled;
	}
	
	public static boolean isPromptToStagingEnabled(){
		return promptToStaging;
	}
	
	public static void setPromptToStagingEnabled(boolean enabled){
		promptToStaging = enabled;
	}
	
	public static boolean isGenerateCsvManifestEnabled(){
		return generateCsvManifest;
	}
	
	public static void setGenerateCsvManifestEnabled(boolean enabled){
		generateCsvManifest = enabled;
	}
	
	public static boolean isGenerateCsvSwitchListEnabled(){
		return generateCsvSwitchList;
	}
	
	public static void setGenerateCsvSwitchListEnabled(boolean enabled){
		generateCsvSwitchList = enabled;
	}
	
	public static boolean isVsdPhysicalLocationEnabled(){
		return enableVsdPhysicalLocations;
	}
	
	public static void setVsdPhysicalLocationEnabled(boolean enabled){
		enableVsdPhysicalLocations = enabled;
	}
	
	/*
	public static boolean isCreateReportersEnabled(){
		return enableReporters;
	}
	
	public static void setCreateReportersEnabled(boolean enabled){
		enableReporters = enabled;
	}
	*/
	
	public static String getRailroadName(){
		return railroadName;
	}
	
	public static void setRailroadName(String name){
		railroadName = name;
	}
	
	public static String getHazardousMsg(){
		return hazardousMsg;
	}
	
	public static void setHazardousMsg(String message){
		hazardousMsg = message;
	}
	
	
	public static String getMiaComment(){
		return miaComment;
	}
	
	public static void setMiaComment(String comment){
		miaComment = comment;
	}
	
	public static void setTrainDirection(int direction){
		traindir = direction;
	}
	
	public static int getTrainDirection(){
		return traindir;
	}
	
	public static void setTrainLength(int length){
		trainLength = length;
	}
	
	public static int getTrainLength(){
		return trainLength;
	}
	
	public static void setEngineSize(int size){
		engineSize = size;
	}
	
	public static int getEngineSize(){
		return engineSize;
	}
	
	public static void setCarMoves(int moves){
		carMoves = moves;
	}
	
	public static int getCarMoves(){
		return carMoves;
	}
	
	public static String getPanelName(){
		return panelName;
	}
	
	public static void setPanelName(String name){
		panelName = name;
	}
	
	public static String getLengthUnit(){
		return lengthUnit;
	}
	
	public static void setLengthUnit(String unit){
		lengthUnit = unit;
	}
	
	public static String getYearModeled(){
		return yearModeled;
	}
	
	public static void setYearModeled(String year){
		yearModeled = year;
	}
	
	public static String getCarTypes(){
		return carTypes;
	}
	
	public static void setCarTypes(String types){
		carTypes = types;
	}

	public static void  setTrainIconCordEnabled(boolean enable){
		enableTrainIconXY = enable;
	}
	
	public static boolean isTrainIconCordEnabled(){
		return enableTrainIconXY;
	}
	
	public static void  setTrainIconAppendEnabled(boolean enable){
		appendTrainIcon = enable;
	}
	
	public static boolean isTrainIconAppendEnabled(){
		return appendTrainIcon;
	}
	
	public static void  setBuildReportLevel(String level){
		buildReportLevel = level;
	}
	
	public static String getBuildReportLevel(){
		return buildReportLevel;
	}
	
	public static void setManifestEditorEnabled(boolean enable){
		manifestEditorEnabled = enable;
	}
	
	public static boolean isManifestEditorEnabled(){
		return manifestEditorEnabled;
	}
	
	public static void setBuildReportEditorEnabled(boolean enable){
		buildReportEditorEnabled = enable;
	}
	
	public static boolean isBuildReportEditorEnabled(){
		return buildReportEditorEnabled;
	}
	
	public static void setSwitchListFormatSameAsManifest(boolean b){
		switchListSameManifest = b;
	}
	
	public static boolean isSwitchListFormatSameAsManifest(){
		return switchListSameManifest;
	}
	
	public static void setSwitchListRealTime(boolean b){
		switchListRealTime = b;
	}
	
	public static boolean isSwitchListRealTime(){
		return switchListRealTime;
	}

	public static void setSwitchListAllTrainsEnabled(boolean b){
		switchListAllTrains = b;
	}
	
	public static boolean isSwitchListAllTrainsEnabled(){
		return switchListAllTrains;
	}
	
	public static void setSwitchListPagePerTrainEnabled(boolean b){
		switchListPage = b;
	}
	
	public static boolean isSwitchListPagePerTrainEnabled(){
		return switchListPage;
	}

	public static void setTruncateManifestEnabled(boolean b){
		manifestTruncated = b;
	}
	
	public static boolean isTruncateManifestEnabled(){
		return manifestTruncated;
	}
	
	public static void setUseDepartureTimeEnabled(boolean b){
		manifestDepartureTime = b;
	}
	
	public static boolean isUseDepartureTimeEnabled(){
		return manifestDepartureTime;
	}
	
	public static void setPrintLocationCommentsEnabled(boolean enable){
		printLocationComments = enable;
	}
	
	public static boolean isPrintLocationCommentsEnabled(){
		return printLocationComments;
	}
	
	public static void setPrintRouteCommentsEnabled(boolean enable){
		printRouteComments = enable;
	}
	
	public static boolean isPrintRouteCommentsEnabled(){
		return printRouteComments;
	}
	
	public static void setPrintLoadsAndEmptiesEnabled(boolean enable){
		printLoadsAndEmpties = enable;
	}
	
	public static boolean isPrintLoadsAndEmptiesEnabled(){
		return printLoadsAndEmpties;
	}
	
	public static void setPrintTimetableNameEnabled(boolean enable){
		printTimetableName = enable;
	}
	
	public static boolean isPrintTimetableNameEnabled(){
		return printTimetableName;
	}
	
	public static void set12hrFormatEnabled(boolean enable){
		use12hrFormat = enable;
	}
	
	public static boolean is12hrFormatEnabled(){
		return use12hrFormat;
	}
	
	public static void setPrintValidEnabled(boolean enable){
		printValid = enable;
	}
	
	public static boolean isPrintValidEnabled(){
		return printValid;
	}
	
	public static void setSwitchTime(int minutes){
		carSwitchTime = minutes;
	}
	
	public static int getSwitchTime(){
		return carSwitchTime;
	}
	
	public static void setTravelTime(int minutes){
		travelTime = minutes;
	}
	
	public static int getTravelTime(){
		return travelTime;
	}
	
	public static void setTrainIconColorNorth (String color){
		iconNorthColor = color;
	}
	
	public static String getTrainIconColorNorth(){
		return iconNorthColor;
	}
	
	public static void setTrainIconColorSouth (String color){
		iconSouthColor = color;
	}
	
	public static String getTrainIconColorSouth(){
		return iconSouthColor;
	}
	
	public static void setTrainIconColorEast (String color){
		iconEastColor = color;
	}
	
	public static String getTrainIconColorEast(){
		return iconEastColor;
	}
	
	public static void setTrainIconColorWest (String color){
		iconWestColor = color;
	}
	
	public static String getTrainIconColorWest(){
		return iconWestColor;
	}
	
	public static void setTrainIconColorLocal (String color){
		iconLocalColor = color;
	}
	
	public static String getTrainIconColorLocal(){
		return iconLocalColor;
	}
	
	public static void setTrainIconColorTerminate (String color){
		iconTerminateColor = color;
	}
	
	public static String getTrainIconColorTerminate(){
		return iconTerminateColor;
	}
	
	public static String getFontName(){
		return fontName;
	}
	
	public static void setFontName(String name){
		fontName = name;
	}
	
	public static int getFontSize(){
		return fontSize;
	}
	
	public static void setFontSize(int size){
		fontSize = size;
	}
	
	public static String getManifestOrientation(){
		return manifestOrientation;
	}
	
	public static void setManifestOrientation(String orientation){
		manifestOrientation = orientation;
	}
	
	public static String getSwitchListOrientation(){
		if (isSwitchListFormatSameAsManifest())
			return manifestOrientation;
		else
			return switchListOrientation;
	}
	
	public static void setSwitchListOrientation(String orientation){
		switchListOrientation = orientation;
	}
	
	public static boolean isTabEnabled(){
		return tab;
	}
	
	public static void setTabEnabled(boolean enable){
		tab = enable;
	}
	
	public static boolean isCarLoggerEnabled(){
		return carLogger;
	}
	
	public static void setCarLoggerEnabled(boolean enable){
		carLogger = enable;
		RollingStockLogger.instance().enableCarLogging(enable);
	}
	
	public static boolean isEngineLoggerEnabled(){
		return engineLogger;
	}
	
	public static void setEngineLoggerEnabled(boolean enable){
		engineLogger = enable;
		RollingStockLogger.instance().enableEngineLogging(enable);
	}
	
	public static boolean isTrainLoggerEnabled(){
		return trainLogger;
	}
	
	public static void setTrainLoggerEnabled(boolean enable){
		trainLogger = enable;
		TrainLogger.instance().enableTrainLogging(enable);
	}
	
	public static String getPickupEnginePrefix(){
		return pickupEnginePrefix;
	}
	
	public static void setPickupEnginePrefix(String prefix){
		pickupEnginePrefix = prefix;
	}
	
	public static String getDropEnginePrefix(){
		return dropEnginePrefix;
	}
	
	public static void setDropEnginePrefix(String prefix){
		dropEnginePrefix = prefix;
	}
	
	public static String getPickupCarPrefix(){
		return pickupCarPrefix;
	}
	
	public static void setPickupCarPrefix(String prefix){
		pickupCarPrefix = prefix;
	}
	
	public static String getDropCarPrefix(){
		return dropCarPrefix;
	}
	
	public static void setDropCarPrefix(String prefix){
		dropCarPrefix = prefix;
	}
	
	public static String getLocalPrefix(){
		return localPrefix;
	}
	
	public static void setLocalPrefix(String prefix){
		localPrefix = prefix;
	}
	
	public static String getSwitchListPickupCarPrefix(){
		if (isSwitchListFormatSameAsManifest())
			return pickupCarPrefix;
		else
			return switchListPickupCarPrefix;
	}
	
	public static void setSwitchListPickupCarPrefix(String prefix){
		switchListPickupCarPrefix = prefix;
	}
	
	public static String getSwitchListDropCarPrefix(){
		if (isSwitchListFormatSameAsManifest())
			return dropCarPrefix;
		else
			return switchListDropCarPrefix;
	}
	
	public static void setSwitchListDropCarPrefix(String prefix){
		switchListDropCarPrefix = prefix;
	}
	
	public static String getSwitchListLocalPrefix(){
		if (isSwitchListFormatSameAsManifest())
			return localPrefix;
		else
			return switchListLocalPrefix;
	}
	
	public static void setSwitchListLocalPrefix(String prefix){
		switchListLocalPrefix = prefix;
	}
	
	public static String[] getPickupEngineMessageFormat(){
		return pickupEngineMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupEngineMessageFormat(String[] format){
		pickupEngineMessageFormat = format;
	}
	
	public static String[] getDropEngineMessageFormat(){
		return dropEngineMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropEngineMessageFormat(String[] format){
		dropEngineMessageFormat = format;
	}
	
	public static String[] getPickupCarMessageFormat(){
		return pickupCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupCarMessageFormat(String[] format){
		pickupCarMessageFormat = format;
	}
	
	public static String[] getDropCarMessageFormat(){
		return dropCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropCarMessageFormat(String[] format){
		dropCarMessageFormat = format;
	}
	
	public static String[] getLocalMessageFormat(){
		return localMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setLocalMessageFormat(String[] format){
		localMessageFormat = format;
	}
	
	public static String[] getMissingCarMessageFormat(){
		return missingCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setMissingCarMessageFormat(String[] format){
		missingCarMessageFormat = format;
	}
	
	public static String[] getSwitchListPickupCarMessageFormat(){
		if (isSwitchListFormatSameAsManifest())
			return pickupCarMessageFormat.clone();
		else
			return switchListPickupCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListPickupCarMessageFormat(String[] format){
		switchListPickupCarMessageFormat = format;
	}
	
	public static String[] getSwitchListDropCarMessageFormat(){
		if (isSwitchListFormatSameAsManifest())
			return dropCarMessageFormat.clone();
		else
			return switchListDropCarMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListDropCarMessageFormat(String[] format){
		switchListDropCarMessageFormat = format;
	}
	
	public static String[] getSwitchListLocalMessageFormat(){
		if (isSwitchListFormatSameAsManifest())
			return localMessageFormat.clone();
		else
			return switchListLocalMessageFormat.clone();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListLocalMessageFormat(String[] format){
		switchListLocalMessageFormat = format;
	}
	
	/**
	 * Gets the manifest format for utility cars.  The car's
	 * road, number, and color are not printed.
	 * @return Utility car format
	 */
	public static String[] getPickupUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getPickupCarMessageFormat());
	}
	
	public static String[] getSetoutUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getDropCarMessageFormat());
	}
	
	public static String[] getLocalUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getLocalMessageFormat());
	}
	
	public static String[] getSwitchListPickupUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getSwitchListPickupCarMessageFormat());
	}
	
	public static String[] getSwitchListSetoutUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getSwitchListDropCarMessageFormat());
	}
	
	public static String[] getSwitchListLocalUtilityCarMessageFormat(){
		return createUitlityCarMessageFormat(getSwitchListLocalMessageFormat());
	}
	
	private static String[] createUitlityCarMessageFormat(String[] format){
		// remove car's road, number, color
		for (int i=0; i<format.length; i++){
			if (format[i].equals(ROAD))
				format[i] = NO_ROAD;
			else if (format[i].equals(NUMBER))
				format[i] = NO_NUMBER;
			else if (format[i].equals(COLOR))
				format[i] = NO_COLOR;
		}
		return format;
	}
	
	public static String[] getTruncatedPickupManifestMessageFormat(){
		return createTruncatedManifestMessageFormat(getPickupCarMessageFormat());
	}
	
	public static String[] getTruncatedSetoutManifestMessageFormat(){
		return createTruncatedManifestMessageFormat(getDropCarMessageFormat());
	}
	
	private static String[] createTruncatedManifestMessageFormat(String[] format){
		// remove car's destination and location
		for (int i=0; i<format.length; i++){
			if (format[i].equals(DESTINATION))
				format[i] = NO_DESTINATION;
			else if (format[i].equals(DEST_TRACK))
				format[i] = NO_DEST_TRACK;
			else if (format[i].equals(LOCATION))
				format[i] = NO_LOCATION;
		}
		return format;
	}
	
	public static String getDropTextColor(){
		return dropColor;
	}
	
	public static void setDropTextColor(String color){
		dropColor = color;
	}
	
	public static String getPickupTextColor(){
		return pickupColor;
	}
	
	public static void setPickupTextColor(String color){
		pickupColor = color;
	}
	
	public static String getLocalTextColor(){
		return localColor;
	}
	
	public static void setLocalTextColor(String color){
		localColor = color;
	}
	
	public static Color getPickupColor(){
		if (pickupColor.equals(BLUE))
			return Color.blue;
		if (pickupColor.equals(GREEN))
			return Color.green;
		if (pickupColor.equals(RED))
			return Color.red;
		return Color.black;	// default
	}
	
	public static Color getDropColor(){
		if (dropColor.equals(BLUE))
			return Color.blue;
		if (dropColor.equals(GREEN))
			return Color.green;
		if (dropColor.equals(RED))
			return Color.red;
		return Color.black;	// default
	}
	
	public static Color getLocalColor(){
		if (localColor.equals(BLUE))
			return Color.blue;
		if (localColor.equals(GREEN))
			return Color.green;
		if (localColor.equals(RED))
			return Color.red;
		return Color.black;	// default
	}
	
	public static String getManifestLogoURL(){
		return logoURL;
	}
	
	public static void setManifestLogoURL(String pathName){
		logoURL = pathName;
	}
	
	public static String getOwnerName(){
		return ownerName;
	}
	
	public static void setOwnerName(String name){
		ownerName = name;
	}
	
	public static int getScaleRatio(){
		if (scale == 0)
			log.error("Scale not set");
		return ratio;
	}
	
	public static int getScaleTonRatio(){
		if (scale == 0)
			log.error("Scale not set");
		return ratioTons;
	}
	
	public static int getInitalWeight(){
		if (scale == 0)
			log.error("Scale not set");
		return initWeight;
	}
	
	public static int getAddWeight(){
		if (scale == 0)
			log.error("Scale not set");
		return addWeight;
	}
	
	public static int getScale(){
		return scale;
	}
	
	public static void setScale(int s){
		scale = s;
		switch (scale){
		case Z_SCALE:
			ratio = Z_RATIO;
			initWeight = Z_INITIAL_WEIGHT;
			addWeight = Z_ADD_WEIGHT;
			ratioTons = Z_RATIO_TONS;
			break;
		case N_SCALE:
			ratio = N_RATIO;
			initWeight = N_INITIAL_WEIGHT;
			addWeight = N_ADD_WEIGHT;
			ratioTons = N_RATIO_TONS;
			break;
		case TT_SCALE:
			ratio = TT_RATIO;
			initWeight = TT_INITIAL_WEIGHT;
			addWeight = TT_ADD_WEIGHT;
			ratioTons = TT_RATIO_TONS;
			break;
		case HOn3_SCALE:
			ratio = HO_RATIO;
			initWeight = HOn3_INITIAL_WEIGHT;
			addWeight = HOn3_ADD_WEIGHT;
			ratioTons = HOn3_RATIO_TONS;
			break;
		case OO_SCALE:
			ratio = OO_RATIO;
			initWeight = OO_INITIAL_WEIGHT;
			addWeight = OO_ADD_WEIGHT;
			ratioTons = OO_RATIO_TONS;
			break;
		case HO_SCALE:
			ratio = HO_RATIO;
			initWeight = HO_INITIAL_WEIGHT;
			addWeight = HO_ADD_WEIGHT;
			ratioTons = HO_RATIO_TONS;
			break;
		case Sn3_SCALE:
			ratio = S_RATIO;
			initWeight = Sn3_INITIAL_WEIGHT;
			addWeight = Sn3_ADD_WEIGHT;
			ratioTons = Sn3_RATIO_TONS;
			break;
		case S_SCALE:
			ratio = S_RATIO;
			initWeight = S_INITIAL_WEIGHT;
			addWeight = S_ADD_WEIGHT;
			ratioTons = S_RATIO_TONS;
			break;
		case On3_SCALE:
			ratio = O_RATIO;
			initWeight = On3_INITIAL_WEIGHT;
			addWeight = On3_ADD_WEIGHT;
			ratioTons = On3_RATIO_TONS;
			break;
		case O_SCALE:
			ratio = O_RATIO;
			initWeight = O_INITIAL_WEIGHT;
			addWeight = O_ADD_WEIGHT;
			ratioTons = O_RATIO_TONS;
			break;
		case G_SCALE:
			ratio = G_RATIO;
			initWeight = G_INITIAL_WEIGHT;
			addWeight = G_ADD_WEIGHT;
			ratioTons = G_RATIO_TONS;
			break;
		default:
			log.error ("Unknown scale");
		}
	}
	
	public static JComboBox getFontComboBox(){
		JComboBox box = new JComboBox();
		//java.awt.Font fonts[] = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		//for (int i=0; i<fonts.length; i++){
		//	box.addItem(fonts[i]);
		//}
		box.addItem(COURIER);
		box.addItem(GARAMOND);
		box.addItem(MONOSPACED);
		box.addItem(SANSERIF);
		box.addItem(SERIF);
		return box;
	}
	
	public static JComboBox getOrientationComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(PORTRAIT);
		box.addItem(LANDSCAPE);
		box.addItem(HANDHELD);
		return box;
	}
	
	/**
	 * 
	 * @return the available text colors used for printing
	 */
	public static JComboBox getPrintColorComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(BLACK);
		box.addItem(BLUE);
		box.addItem(GREEN);
		box.addItem(RED);
		return box;
	}
	
	public static JComboBox getEngineMessageComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		box.addItem(ROAD);
		box.addItem(NUMBER);
		box.addItem(TYPE);
		box.addItem(MODEL);
		box.addItem(LENGTH);
		box.addItem(CONSIST);
		box.addItem(OWNER);
		box.addItem(LOCATION);
		box.addItem(DESTINATION);		
		box.addItem(COMMENT);
		return box;
	}
	
	public static JComboBox getCarMessageComboBox(){
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		box.addItem(ROAD);
		box.addItem(NUMBER);
		box.addItem(TYPE);
		box.addItem(LENGTH);
		box.addItem(LOAD);
		box.addItem(HAZARDOUS);
		box.addItem(COLOR);
		box.addItem(KERNEL);
		box.addItem(OWNER);
		box.addItem(LOCATION);
		box.addItem(DESTINATION);
		box.addItem(DEST_TRACK);
		box.addItem(COMMENT);
		box.addItem(DROP_COMMENT);
		box.addItem(PICKUP_COMMENT);
		box.addItem(RWE);
		return box;
	}
	
	/**
	 * 
	 * @return JComboBox loaded with the strings (North, South, East,
	 *         West) showing the available train directions for this
	 *         railroad
	 */
    public static JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
    	if ((traindir & EAST)>0)
			box.addItem(EAST_DIR);
    	if ((traindir & WEST)>0)
			box.addItem(WEST_DIR);
    	if ((traindir & NORTH)>0)
			box.addItem(NORTH_DIR);
    	if ((traindir & SOUTH)>0)
			box.addItem(SOUTH_DIR);
    	return box;
    }
    
    /**
     * Get train directions
     * @return List of valid train directions
     */
    public static List<String> getList(){
    	List<String> directions = new ArrayList<String>();
    	if ((traindir & EAST)>0)
    		directions.add(EAST_DIR);
    	if ((traindir & WEST)>0)
    		directions.add(WEST_DIR);
    	if ((traindir & NORTH)>0)
    		directions.add(NORTH_DIR);
    	if ((traindir & SOUTH)>0)
    		directions.add(SOUTH_DIR);
    	return directions;
    }
    
    /**
     * Converts binary direction to String direction
     * @param direction EAST, WEST, NORTH, SOUTH 
     * @return String representation of a direction
     */
    public static String getDirectionString(int direction){
    	switch (direction){
    	case EAST: return EAST_DIR; 
    	case WEST: return WEST_DIR; 
    	case NORTH: return NORTH_DIR; 
    	case SOUTH: return SOUTH_DIR; 
    	default: return "unknown";
    	}
    }
    
    /**
     * Converts String direction to binary direction
     * @param direction EAST_DIR WEST_DIR NORTH_DIR SOUTH_DIR
     * @return integer representation of a direction
     */
    public static int getDirectionInt(String direction){
    	if (direction.equals(EAST_DIR))
    		return EAST;
    	else if (direction.equals(WEST_DIR))
    		return WEST;
    	else if (direction.equals(NORTH_DIR))
    		return NORTH;
    	else if (direction.equals(SOUTH_DIR))
    		return SOUTH;
    	else
    		return 0; // return unknown
    }
    
    // must synchronize changes with operation-config.dtd
    public static Element store(){
    	Element values;
    	Element e = new Element("operations");
    	e.addContent(values = new Element("railRoad"));
    	values.setAttribute("name", getRailroadName());
    	
    	e.addContent(values = new Element("settings"));
    	values.setAttribute("mainMenu", isMainMenuEnabled()?"true":"false");
    	values.setAttribute("closeOnSave", isCloseWindowOnSaveEnabled()?"true":"false");
    	values.setAttribute("autoSave", isAutoSaveEnabled()?"true":"false");
    	values.setAttribute("autoBackup", isAutoBackupEnabled()?"true":"false");
    	values.setAttribute("trainDirection", Integer.toString(getTrainDirection()));
    	values.setAttribute("trainLength", Integer.toString(getTrainLength()));
    	values.setAttribute("maxEngines", Integer.toString(getEngineSize()));
    	values.setAttribute("scale", Integer.toString(getScale()));
    	values.setAttribute("carTypes", getCarTypes());
    	values.setAttribute("switchTime", Integer.toString(getSwitchTime()));
    	values.setAttribute("travelTime", Integer.toString(getTravelTime()));
    	values.setAttribute("showValue", isValueEnabled()?"true":"false");
    	values.setAttribute("valueLabel", getValueLabel());
    	values.setAttribute("showRfid", isRfidEnabled()?"true":"false");
    	values.setAttribute("rfidLabel", getRfidLabel());
    	values.setAttribute("carRoutingEnabled", isCarRoutingEnabled()?"true":"false");
    	values.setAttribute("carRoutingViaStaging", isCarRoutingViaStagingEnabled()?"true":"false");
    	values.setAttribute("forwardToYard", isForwardToYardEnabled()?"true":"false");
    	values.setAttribute("carLogger", isCarLoggerEnabled()?"true":"false");    	
       	values.setAttribute("engineLogger", isEngineLoggerEnabled()?"true":"false");
       	values.setAttribute("trainLogger", isTrainLoggerEnabled()?"true":"false");
       	values.setAttribute("printLocComments", isPrintLocationCommentsEnabled()?"true":"false");
       	values.setAttribute("printRouteComments", isPrintRouteCommentsEnabled()?"true":"false");
       	values.setAttribute("printLoadsEmpties", isPrintLoadsAndEmptiesEnabled()?"true":"false");
       	values.setAttribute("printTimetable", isPrintTimetableNameEnabled()?"true":"false");
       	values.setAttribute("use12hrFormat", is12hrFormatEnabled()?"true":"false");
       	values.setAttribute("printValid", isPrintValidEnabled()?"true":"false");
       	values.setAttribute("lengthUnit", getLengthUnit());
       	values.setAttribute("yearModeled", getYearModeled());
       	
       	e.addContent(values = new Element("pickupEngFormat"));
       	values.setAttribute("prefix", getPickupEnginePrefix());
        StringBuffer buf = new StringBuffer();
       	for (int i=0; i<pickupEngineMessageFormat.length; i++){
       		buf.append(pickupEngineMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
      	e.addContent(values = new Element("dropEngFormat"));
      	values.setAttribute("prefix", getDropEnginePrefix());
        buf = new StringBuffer();
       	for (int i=0; i<dropEngineMessageFormat.length; i++){
       		buf.append(dropEngineMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
      	e.addContent(values = new Element("pickupCarFormat"));
      	values.setAttribute("prefix", getPickupCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<pickupCarMessageFormat.length; i++){
       		buf.append(pickupCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
      	e.addContent(values = new Element("dropCarFormat"));
      	values.setAttribute("prefix", getDropCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<dropCarMessageFormat.length; i++){
       		buf.append(dropCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
      	e.addContent(values = new Element("localFormat"));
      	values.setAttribute("prefix", getLocalPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<localMessageFormat.length; i++){
       		buf.append(localMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
     	e.addContent(values = new Element("missingCarFormat"));
        buf = new StringBuffer();
       	for (int i=0; i<missingCarMessageFormat.length; i++){
       		buf.append(missingCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
       	e.addContent(values = new Element("switchList"));
       	values.setAttribute("sameAsManifest", isSwitchListFormatSameAsManifest()?"true":"false");
       	values.setAttribute("realTime", isSwitchListRealTime()?"true":"false");
       	values.setAttribute("allTrains", isSwitchListAllTrainsEnabled()?"true":"false");
       	values.setAttribute("pageMode", isSwitchListPagePerTrainEnabled()?"true":"false");
       	
      	e.addContent(values = new Element("switchListPickupCarFormat"));
      	values.setAttribute("prefix", getSwitchListPickupCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<switchListPickupCarMessageFormat.length; i++){
       		buf.append(switchListPickupCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
      	e.addContent(values = new Element("switchListDropCarFormat"));
      	values.setAttribute("prefix", getSwitchListDropCarPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<switchListDropCarMessageFormat.length; i++){
       		buf.append(switchListDropCarMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
       	
      	e.addContent(values = new Element("switchListLocalFormat"));
      	values.setAttribute("prefix", getSwitchListLocalPrefix());
        buf = new StringBuffer();
       	for (int i=0; i<switchListLocalMessageFormat.length; i++){
       		buf.append(switchListLocalMessageFormat[i]+",");
       	}
       	values.setAttribute("setting", buf.toString());
    	
    	e.addContent(values = new Element("panel"));
    	values.setAttribute("name", getPanelName());
    	values.setAttribute("trainIconXY", isTrainIconCordEnabled()?"true":"false");
    	values.setAttribute("trainIconAppend", isTrainIconAppendEnabled()?"true":"false");
 
       	e.addContent(values = new Element("fontName"));
    	values.setAttribute("name", getFontName());
    	
       	e.addContent(values = new Element("fontSize"));
    	values.setAttribute("size", Integer.toString(getFontSize()));
    	
       	e.addContent(values = new Element("pageOrientation"));
    	values.setAttribute("manifest", getManifestOrientation());
    	values.setAttribute("switchList", getSwitchListOrientation());
    	
      	e.addContent(values = new Element("manifestColors"));
    	values.setAttribute("dropColor", getDropTextColor());
    	values.setAttribute("pickupColor", getPickupTextColor());
    	values.setAttribute("localColor", getLocalTextColor());
    	
    	e.addContent(values = new Element("tab"));
    	values.setAttribute("enabled", isTabEnabled()?"true":"false");
    	
    	e.addContent(values = new Element("manifest"));
    	values.setAttribute("truncate", isTruncateManifestEnabled()?"true":"false");
    	values.setAttribute("useDepartureTime", isUseDepartureTimeEnabled()?"true":"false");
    	values.setAttribute("useEditor", isManifestEditorEnabled()?"true":"false");
    	values.setAttribute("hazardousMsg", getHazardousMsg());
    	
        if (getManifestLogoURL() != ""){
        	values = new Element("manifestLogo");
        	values.setAttribute("name", getManifestLogoURL());
        	e.addContent(values);
        }       
    	
    	e.addContent(values = new Element("buildOptions"));
    	values.setAttribute("aggressive", isBuildAggressive()?"true":"false");
    	
    	values.setAttribute("allowLocalInterchange", isLocalInterchangeMovesEnabled()?"true":"false");
    	values.setAttribute("allowLocalSiding", isLocalSidingMovesEnabled()?"true":"false");
    	values.setAttribute("allowLocalYard", isLocalYardMovesEnabled()?"true":"false");
    	
    	values.setAttribute("stagingRestrictionEnabled", isTrainIntoStagingCheckEnabled()?"true":"false");
    	values.setAttribute("stagingTrackAvail", isStagingTrackImmediatelyAvail()?"true":"false");
    	values.setAttribute("allowReturnStaging", isAllowReturnToStagingEnabled()?"true":"false");
    	values.setAttribute("promptStagingEnabled", isPromptFromStagingEnabled()?"true":"false");
    	values.setAttribute("promptToStagingEnabled", isPromptToStagingEnabled()?"true":"false");
    	
    	values.setAttribute("generateCsvManifest", isGenerateCsvManifestEnabled()?"true":"false");
    	values.setAttribute("generateCsvSwitchList", isGenerateCsvSwitchListEnabled()?"true":"false");
    	
    	e.addContent(values = new Element("buildReport"));
    	values.setAttribute("level", getBuildReportLevel());
    	values.setAttribute("useEditor", isBuildReportEditorEnabled()?"true":"false");
    	
       	e.addContent(values = new Element("owner"));
    	values.setAttribute("name", getOwnerName());
     	
    	e.addContent(values = new Element("iconColor"));
    	values.setAttribute("north", getTrainIconColorNorth());
    	values.setAttribute("south", getTrainIconColorSouth());
    	values.setAttribute("east", getTrainIconColorEast());
    	values.setAttribute("west", getTrainIconColorWest());
    	values.setAttribute("local", getTrainIconColorLocal());
    	values.setAttribute("terminate", getTrainIconColorTerminate());
    	
      	e.addContent(values = new Element("comments"));
    	values.setAttribute("misplacedCars", getMiaComment());
    	
    	if (isVsdPhysicalLocationEnabled()){
    		e.addContent(values = new Element("vsd"));
    		values.setAttribute("enablePhysicalLocations", isVsdPhysicalLocationEnabled()?"true":"false");
    	}
    	
    	// Save CATS setting
    	e.addContent(values = new Element("CATS"));
    	values.setAttribute("exactLocationName", AbstractOperationsServer.isExactLoationNameEnabled()?"true":"false");
    	return e;
    }
    
    public static void load(Element e) {
        //if (log.isDebugEnabled()) jmri.jmrit.XmlFile.dumpElement(e);
        
        if (e.getChild("operations") == null){
        	log.debug("operation setup values missing");
        	return;
        }
        Element operations = e.getChild("operations");
        org.jdom.Attribute a;
        
        if ((operations.getChild("railRoad") != null) && 
        		(a = operations.getChild("railRoad").getAttribute("name"))!= null){
        	String name = a.getValue();
           	if (log.isDebugEnabled()) log.debug("railroadName: "+name);
           	setRailroadName(name);
        }
        if (operations.getChild("settings") != null){
        	if ((a = operations.getChild("settings").getAttribute("mainMenu"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("mainMenu: "+enabled);
        		setMainMenuEnabled(enabled.equals("true"));
        	}
           	if ((a = operations.getChild("settings").getAttribute("closeOnSave"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("closeOnSave: "+enabled);
        		setCloseWindowOnSaveEnabled(enabled.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("trainDirection"))!= null){
        		String dir = a.getValue();
        		if (log.isDebugEnabled()) log.debug("direction: "+dir);
        		setTrainDirection(Integer.parseInt(dir));
        	}
        	if ((a = operations.getChild("settings").getAttribute("trainLength"))!= null){
        		String length = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Max train length: "+length);
        		setTrainLength(Integer.parseInt(length));
        	}
        	if ((a = operations.getChild("settings").getAttribute("maxEngines"))!= null){
        		String size = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Max number of engines: "+size);
        		setEngineSize(Integer.parseInt(size));
        	}
        	if ((a = operations.getChild("settings").getAttribute("scale"))!= null){
        		String scale = a.getValue();
        		if (log.isDebugEnabled()) log.debug("scale: "+scale);
        		setScale(Integer.parseInt(scale));
        	}
        	if ((a = operations.getChild("settings").getAttribute("carTypes"))!= null){
        		String types = a.getValue();
        		if (log.isDebugEnabled()) log.debug("CarTypes: "+types);
        		setCarTypes(types);
        	}
        	if ((a = operations.getChild("settings").getAttribute("switchTime"))!= null){
        		String minutes = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchTime: "+minutes);
        		setSwitchTime(Integer.parseInt(minutes));
        	}
        	if ((a = operations.getChild("settings").getAttribute("travelTime"))!= null){
        		String minutes = a.getValue();
        		if (log.isDebugEnabled()) log.debug("travelTime: "+minutes);
        		setTravelTime(Integer.parseInt(minutes));
        	}
        	if ((a = operations.getChild("settings").getAttribute("showValue"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("showValue: "+enable);
        		setValueEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("valueLabel"))!= null){
        		String label = a.getValue();
        		if (log.isDebugEnabled()) log.debug("valueLabel: "+label);
        		setValueLabel(label);
        	}
           	if ((a = operations.getChild("settings").getAttribute("showRfid"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("showRfid: "+enable);
        		setRfidEnabled(enable.equals("true"));
        	}
           	if ((a = operations.getChild("settings").getAttribute("rfidLabel"))!= null){
        		String label = a.getValue();
        		if (log.isDebugEnabled()) log.debug("rfidLabel: "+label);
        		setRfidLabel(label);
        	}
           	if ((a = operations.getChild("settings").getAttribute("carRoutingEnabled"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carRoutingEnabled: "+enable);
        		setCarRoutingEnabled(enable.equals("true"));
        	}
         	if ((a = operations.getChild("settings").getAttribute("carRoutingViaStaging"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carRoutingViaStaging: "+enable);
        		setCarRoutingViaStagingEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("settings").getAttribute("forwardToYard"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("forwardToYard: "+enable);
        		setForwardToYardEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printLocComments"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printLocComments: "+enable);
        		setPrintLocationCommentsEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printRouteComments"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printRouteComments: "+enable);
        		setPrintRouteCommentsEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printLoadsEmpties"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printLoadsEmpties: "+enable);
        		setPrintLoadsAndEmptiesEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printTimetable"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printTimetable: "+enable);
        		setPrintTimetableNameEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("use12hrFormat"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("use12hrFormat: "+enable);
        		set12hrFormatEnabled(enable.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("printValid"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("printValid: "+enable);
        		setPrintValidEnabled(enable.equals("true"));
        	}
         	if ((a = operations.getChild("settings").getAttribute("lengthUnit"))!= null){
        		String unit = a.getValue();
        		if (log.isDebugEnabled()) log.debug("lengthUnit: "+unit);
        		setLengthUnit(unit);
        	}
         	if ((a = operations.getChild("settings").getAttribute("yearModeled"))!= null){
        		String year = a.getValue();
        		if (log.isDebugEnabled()) log.debug("yearModeled: "+year);
        		setYearModeled(year);
        	}
        }
        if (operations.getChild("pickupEngFormat") != null){
        	if ((a = operations.getChild("pickupEngFormat").getAttribute("prefix"))!= null)
        		setPickupEnginePrefix(a.getValue());
        	if ((a = operations.getChild("pickupEngFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupEngFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setPickupEngineMessageFormat(format);
        	}
        }
        if (operations.getChild("dropEngFormat") != null){
        	if ((a = operations.getChild("dropEngFormat").getAttribute("prefix"))!= null)
        		setDropEnginePrefix(a.getValue());
        	if ((a = operations.getChild("dropEngFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropEngFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setDropEngineMessageFormat(format);
        	}
        }
        if (operations.getChild("pickupCarFormat") != null){
        	if ((a = operations.getChild("pickupCarFormat").getAttribute("prefix"))!= null)
        		setPickupCarPrefix(a.getValue());
        	if ((a = operations.getChild("pickupCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupCarFormat: "+setting);
        		String[] format = setting.split(",");
        		replaceOldFormat(format);
        		fixLocaleBug(format);
        		setPickupCarMessageFormat(format);
        	}
        }
        if (operations.getChild("dropCarFormat") != null){
        	if ((a = operations.getChild("dropCarFormat").getAttribute("prefix"))!= null)
        		setDropCarPrefix(a.getValue());
        	if ((a = operations.getChild("dropCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropCarFormat: "+setting);
        		String[] format = setting.split(",");
        		replaceOldFormat(format);
        		fixLocaleBug(format);
        		setDropCarMessageFormat(format);
        	}
        }
        if (operations.getChild("localFormat") != null){
        	if ((a = operations.getChild("localFormat").getAttribute("prefix"))!= null)
        		setLocalPrefix(a.getValue());
        	if ((a = operations.getChild("localFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("localFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setLocalMessageFormat(format);
        	}
        }
        if (operations.getChild("missingCarFormat") != null){
        	if ((a = operations.getChild("missingCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("missingCarFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setMissingCarMessageFormat(format);
        	}
        }
        if (operations.getChild("switchList") != null){
        	if ((a = operations.getChild("switchList").getAttribute("sameAsManifest"))!= null){
        		String b = a.getValue();
        		if (log.isDebugEnabled()) log.debug("sameAsManifest: "+b);
        		setSwitchListFormatSameAsManifest(b.equals("true"));
        	}
        	if ((a = operations.getChild("switchList").getAttribute("realTime"))!= null){
        		String b = a.getValue();
        		if (log.isDebugEnabled()) log.debug("realTime: "+b);
        		setSwitchListRealTime(b.equals("true"));
        	}
           	if ((a = operations.getChild("switchList").getAttribute("allTrains"))!= null){
        		String b = a.getValue();
        		if (log.isDebugEnabled()) log.debug("allTrains: "+b);
        		setSwitchListAllTrainsEnabled(b.equals("true"));
        	}
          	if ((a = operations.getChild("switchList").getAttribute("pageMode"))!= null){
        		String b = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pageMode: "+b);
        		setSwitchListPagePerTrainEnabled(b.equals("true"));
        	}
        }
        if (operations.getChild("switchListPickupCarFormat") != null){
        	if ((a = operations.getChild("switchListPickupCarFormat").getAttribute("prefix"))!= null)
        		setSwitchListPickupCarPrefix(a.getValue());
        	if ((a = operations.getChild("switchListPickupCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchListpickupCarFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setSwitchListPickupCarMessageFormat(format);
        	}
        }
        if (operations.getChild("switchListDropCarFormat") != null){
        	if ((a = operations.getChild("switchListDropCarFormat").getAttribute("prefix"))!= null)
        		setSwitchListDropCarPrefix(a.getValue());
        	if ((a = operations.getChild("switchListDropCarFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchListDropCarFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setSwitchListDropCarMessageFormat(format);
        	}
        }
        if (operations.getChild("switchListLocalFormat") != null){
        	if ((a = operations.getChild("switchListLocalFormat").getAttribute("prefix"))!= null)
        		setSwitchListLocalPrefix(a.getValue());
        	if ((a = operations.getChild("switchListLocalFormat").getAttribute("setting"))!= null){
        		String setting = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchListLocalFormat: "+setting);
        		String[] format = setting.split(",");
        		fixLocaleBug(format);
        		setSwitchListLocalMessageFormat(format);
        	}
        }
        if (operations.getChild("panel") != null){
        	if ((a = operations.getChild("panel").getAttribute("name"))!= null){
        		String panel = a.getValue();
        		if (log.isDebugEnabled()) log.debug("panel: "+panel);
        		setPanelName(panel);
        	}
        	if ((a = operations.getChild("panel").getAttribute("trainIconXY"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("TrainIconXY: "+enable);
        		setTrainIconCordEnabled(enable.equals("true"));
        	}
        	if ((a = operations.getChild("panel").getAttribute("trainIconAppend"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("TrainIconAppend: "+enable);
        		setTrainIconAppendEnabled(enable.equals("true"));
        	}
        }
        if ((operations.getChild("fontName") != null) 
        		&& (a = operations.getChild("fontName").getAttribute("name"))!= null){
        	String font = a.getValue();
           	if (log.isDebugEnabled()) log.debug("fontName: "+font);
           	setFontName(font);
        }
        if ((operations.getChild("fontSize") != null) 
        		&& (a = operations.getChild("fontSize").getAttribute("size"))!= null){
        	String size = a.getValue();
           	if (log.isDebugEnabled()) log.debug("fontName: "+size);
           	setFontSize(Integer.parseInt(size));
        }
        
        if ((operations.getChild("pageOrientation") != null)){
        	if((a = operations.getChild("pageOrientation").getAttribute("manifest"))!= null){
        		String orientation = a.getValue();
        		if (log.isDebugEnabled()) log.debug("manifestOrientation: "+orientation);
        		setManifestOrientation(orientation);
        	}
        	if((a = operations.getChild("pageOrientation").getAttribute("switchList"))!= null){
        		String orientation = a.getValue();
        		if (log.isDebugEnabled()) log.debug("switchListOrientation: "+orientation);
        		setSwitchListOrientation(orientation);
        	}
        }
        if ((operations.getChild("manifestColors") != null)){ 
        	if((a = operations.getChild("manifestColors").getAttribute("dropColor"))!= null){
        		String dropColor = a.getValue();
        		if (log.isDebugEnabled()) log.debug("dropColor: "+dropColor);
        		setDropTextColor(dropColor);
        	}
        	if((a = operations.getChild("manifestColors").getAttribute("pickupColor"))!= null){
        		String pickupColor = a.getValue();
        		if (log.isDebugEnabled()) log.debug("pickupColor: "+pickupColor);
        		setPickupTextColor(pickupColor);
        	}
           	if((a = operations.getChild("manifestColors").getAttribute("localColor"))!= null){
        		String localColor = a.getValue();
        		if (log.isDebugEnabled()) log.debug("localColor: "+localColor);
        		setLocalTextColor(localColor);
        	}
        }
        if ((operations.getChild("tab") != null)){ 
        	if((a = operations.getChild("tab").getAttribute("enabled"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("tab: "+enable);
        		setTabEnabled(enable.equals("true"));
        	}
        }
        if ((operations.getChild("manifest") != null)){ 
        	if((a = operations.getChild("manifest").getAttribute("truncate"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("manifest truncate: "+enable);
        		setTruncateManifestEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("manifest").getAttribute("useDepartureTime"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("manifest use departure time: "+enable);
        		setUseDepartureTimeEnabled(enable.equals("true"));
        	}
        	if((a = operations.getChild("manifest").getAttribute("useEditor"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("manifest useEditor: "+enable);
        		setManifestEditorEnabled(enable.equals("true"));
        	}
          	if((a = operations.getChild("manifest").getAttribute("hazardousMsg"))!= null){
        		String message = a.getValue();
        		if (log.isDebugEnabled()) log.debug("manifest hazardousMsg: "+message);
        		setHazardousMsg(message);
        	}
        }     
       	// get manifest logo
        if ((operations.getChild("manifestLogo") != null)){ 
        	if((a = operations.getChild("manifestLogo").getAttribute("name"))!= null){
        		setManifestLogoURL(a.getValue());
        	}
    	}
        if ((operations.getChild("buildOptions") != null)){
        	if((a = operations.getChild("buildOptions").getAttribute("aggressive")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("aggressive: "+enable);
        		setBuildAggressive(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalInterchange")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalInterchange: "+enable);
        		setLocalInterchangeMovesEnabled(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalSiding")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalSiding: "+enable);
        		setLocalSidingMovesEnabled(enable.equals("true"));
        	}
        	if((a = operations.getChild("buildOptions").getAttribute("allowLocalYard")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("noLocalYard: "+enable);
        		setLocalYardMovesEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("stagingRestrictionEnabled")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("stagingRestrictionEnabled: "+enable);
        		setTrainIntoStagingCheckEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("stagingTrackAvail")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("stagingTrackAvail: "+enable);
        		setStagingTrackImmediatelyAvail(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("allowReturnStaging")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("allowReturnStaging: "+enable);
        		setAllowReturnToStagingEnabled(enable.equals("true"));
        	}
           	if((a = operations.getChild("buildOptions").getAttribute("promptStagingEnabled")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("promptStagingEnabled: "+enable);
        		setPromptFromStagingEnabled(enable.equals("true"));
        	}
          	if((a = operations.getChild("buildOptions").getAttribute("promptToStagingEnabled")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("promptToStagingEnabled: "+enable);
        		setPromptToStagingEnabled(enable.equals("true"));
        	}
          	if((a = operations.getChild("buildOptions").getAttribute("generateCsvManifest")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("generateCvsManifest: "+enable);
        		setGenerateCsvManifestEnabled(enable.equals("true"));
        	}
         	if((a = operations.getChild("buildOptions").getAttribute("generateCsvSwitchList")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("generateCvsSwitchList: "+enable);
        		setGenerateCsvSwitchListEnabled(enable.equals("true"));
        	}
        }
        if (operations.getChild("buildReport") != null){
        	if ((a = operations.getChild("buildReport").getAttribute("level")) != null) {
        		String level = a.getValue();
        		if (log.isDebugEnabled()) log.debug("buildReport: "+level);
        		setBuildReportLevel(level);
        	}
        	if ((a = operations.getChild("buildReport").getAttribute("useEditor")) != null) {
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("build report useEditor: "+enable);
        		setBuildReportEditorEnabled(enable.equals("true"));
        	}
        }
        if ((operations.getChild("owner") != null) 
        		&& (a = operations.getChild("owner").getAttribute("name"))!= null){
        	String owner = a.getValue();
           	if (log.isDebugEnabled()) log.debug("owner: "+owner);
           	setOwnerName(owner);
        }
        if (operations.getChild("iconColor") != null){
        	if ((a = operations.getChild("iconColor").getAttribute("north"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("north color: "+color);
        		setTrainIconColorNorth(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("south"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("south color: "+color);
        		setTrainIconColorSouth(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("east"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("east color: "+color);
        		setTrainIconColorEast(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("west"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("west color: "+color);
        		setTrainIconColorWest(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("local"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("local color: "+color);
        		setTrainIconColorLocal(color);
        	}
        	if ((a = operations.getChild("iconColor").getAttribute("terminate"))!= null){
        		String color = a.getValue();
        		if (log.isDebugEnabled()) log.debug("terminate color: "+color);
        		setTrainIconColorTerminate(color);
        	}
        }
        if (operations.getChild("comments") != null){
        	if ((a = operations.getChild("comments").getAttribute("misplacedCars"))!= null){
           		String comment = a.getValue();
        		if (log.isDebugEnabled()) log.debug("Misplaced comment: "+comment);
        		setMiaComment(comment);
        	}
        }
        
        if (operations.getChild("vsd") != null){
        	if ((a = operations.getChild("vsd").getAttribute("enablePhysicalLocations"))!= null){
        		String enable = a.getValue();
        		setVsdPhysicalLocationEnabled(enable.equals("true"));
        	}
        }
        if (operations.getChild("CATS") != null){
        	if ((a = operations.getChild("CATS").getAttribute("exactLocationName"))!= null){
        		String enable = a.getValue();
        		AbstractOperationsServer.setExactLocationName(enable.equals("true"));
        	}
        }
        
        // logging has to be last, causes cars and engines to load
        if (operations.getChild("settings") != null){
          	if ((a = operations.getChild("settings").getAttribute("autoSave"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("autoSave: "+enabled);
        		setAutoSaveEnabled(enabled.equals("true"));
        	}
          	if ((a = operations.getChild("settings").getAttribute("autoBackup"))!= null){
        		String enabled = a.getValue();
        		if (log.isDebugEnabled()) log.debug("autoBackup: "+enabled);
        		setAutoBackupEnabled(enabled.equals("true"));
        	}
         // fixed by only configuring the booleans
        	if ((a = operations.getChild("settings").getAttribute("carLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("carLogger: "+enable);
        		carLogger = enable.equals("true");
        	}
        	if ((a = operations.getChild("settings").getAttribute("engineLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("engineLogger: "+enable);
        		engineLogger = enable.equals("true");
        	}
           	if ((a = operations.getChild("settings").getAttribute("trainLogger"))!= null){
        		String enable = a.getValue();
        		if (log.isDebugEnabled()) log.debug("trainLogger: "+enable);
        		trainLogger = enable.equals("true");
        	}
        }
    }
    
    // replace old pickup and drop message format
    // Change happened from 2.11.3 to 2.11.4
    private static void replaceOldFormat(String[] format){
    	for (int i=0; i<format.length; i++){
    		if (format[i].equals("Pickup Msg"))
    			format[i] = PICKUP_COMMENT;
    		if (format[i].equals("Drop Msg"))
    			format[i] = DROP_COMMENT; 
    	}
    }
    
    // bogus fix for change in locale US to and from UK
    // TODO code needs to change, should be saving tag and not actual string
    private static void fixLocaleBug(String[] format){
    	for (int i=0; i<format.length; i++){
    		if (ROAD.equals("Road") && format[i].equals("Railway"))
    			format[i] = ROAD;
    		else if (ROAD.equals("Railway") && format[i].equals("Road"))
    			format[i] = ROAD;
    	}
    }
	
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Setup.class.getName());

}

