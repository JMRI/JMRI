package jmri.jmrit.operations.setup;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;

import jmri.jmris.AbstractOperationsServer;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.trains.TrainLogger;
import jmri.web.server.WebServerManager;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operations settings.
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2012
 * @version $Revision$
 */
public class Setup {

	// scale ratios from NMRA
	private static final int Z_RATIO = 220;
	private static final int N_RATIO = 160;
	private static final int TT_RATIO = 120;
	private static final int OO_RATIO = 76; // actual ratio 76.2
	private static final int HO_RATIO = 87;
	private static final int S_RATIO = 64;
	private static final int O_RATIO = 48;
	private static final int G_RATIO = 32; // NMRA #1

	// initial weight in milli ounces from NMRA
	private static final int Z_INITIAL_WEIGHT = 364; // not specified by NMRA
	private static final int N_INITIAL_WEIGHT = 500;
	private static final int TT_INITIAL_WEIGHT = 750;
	private static final int HOn3_INITIAL_WEIGHT = 750;
	private static final int OO_INITIAL_WEIGHT = 750; // not specified by NMRA
	private static final int HO_INITIAL_WEIGHT = 1000;
	private static final int Sn3_INITIAL_WEIGHT = 1000;
	private static final int S_INITIAL_WEIGHT = 2000;
	private static final int On3_INITIAL_WEIGHT = 1500;
	private static final int O_INITIAL_WEIGHT = 5000;
	private static final int G_INITIAL_WEIGHT = 10000; // not specified by NMRA

	// additional weight in milli ounces from NMRA
	private static final int Z_ADD_WEIGHT = 100; // not specified by NMRA
	private static final int N_ADD_WEIGHT = 150;
	private static final int TT_ADD_WEIGHT = 375;
	private static final int HOn3_ADD_WEIGHT = 375;
	private static final int OO_ADD_WEIGHT = 500; // not specified by NMRA
	private static final int HO_ADD_WEIGHT = 500;
	private static final int Sn3_ADD_WEIGHT = 500;
	private static final int S_ADD_WEIGHT = 500;
	private static final int On3_ADD_WEIGHT = 750;
	private static final int O_ADD_WEIGHT = 1000;
	private static final int G_ADD_WEIGHT = 2000; // not specified by NMRA

	// actual weight to tons conversion ratios (based on 40' boxcar at ~80 tons)
	private static final int Z_RATIO_TONS = 130;
	private static final int N_RATIO_TONS = 80;
	private static final int TT_RATIO_TONS = 36;
	private static final int HOn3_RATIO_TONS = 20;
	private static final int OO_RATIO_TONS = 20;
	private static final int HO_RATIO_TONS = 20; // 20 tons per ounce
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
	public static final int G_SCALE = 11; // NMRA #1

	public static final int EAST = 1; // train direction serviced by this location
	public static final int WEST = 2;
	public static final int NORTH = 4;
	public static final int SOUTH = 8;

	public static final String EAST_DIR = Bundle.getMessage("East");
	public static final String WEST_DIR = Bundle.getMessage("West");
	public static final String NORTH_DIR = Bundle.getMessage("North");
	public static final String SOUTH_DIR = Bundle.getMessage("South");

	public static final String DESCRIPTIVE = Bundle.getMessage("Descriptive"); // Car types
	public static final String AAR = Bundle.getMessage("ArrCodes"); // Car types

	public static final String MONOSPACED = Bundle.getMessage("Monospaced"); // default printer font
	
	public static final int STANDARD = 0;
	public static final int TWO_COLUMN = 1;
	public static final int TWO_COLUMN_TRACK = 2;
	
	public static final String STANDARD_FORMAT = Bundle.getMessage("StandardFormat");
	public static final String TWO_COLUMN_FORMAT = Bundle.getMessage("TwoColumnFormat");
	public static final String TWO_COLUMN_TRACK_FORMAT = Bundle.getMessage("TwoColumnTrackFormat");

	public static final String PORTRAIT = Bundle.getMessage("Portrait");
	public static final String LANDSCAPE = Bundle.getMessage("Landscape");
	public static final String HANDHELD = Bundle.getMessage("HandHeld");

	public static final String LENGTHABV = Bundle.getMessage("LengthSymbol");

	public static final String BUILD_REPORT_MINIMAL = "1";
	public static final String BUILD_REPORT_NORMAL = "3";
	public static final String BUILD_REPORT_DETAILED = "5";
	public static final String BUILD_REPORT_VERY_DETAILED = "7";

	public static final String ROAD = Bundle.getMessage("Road"); // the supported message format options
	public static final String NUMBER = Bundle.getMessage("Number");
	public static final String TYPE = Bundle.getMessage("Type");
	public static final String MODEL = Bundle.getMessage("Model");
	public static final String LENGTH = Bundle.getMessage("Length");
	public static final String LOAD = Bundle.getMessage("Load");
	public static final String COLOR = Bundle.getMessage("Color");
	public static final String TRACK = Bundle.getMessage("Track");
	public static final String DESTINATION = Bundle.getMessage("Destination");
	public static final String DEST_TRACK = Bundle.getMessage("Dest&Track");
	public static final String FINAL_DEST = Bundle.getMessage("Final_Dest");
	public static final String FINAL_DEST_TRACK = Bundle.getMessage("FD&Track");
	public static final String LOCATION = Bundle.getMessage("Location");
	public static final String CONSIST = Bundle.getMessage("Consist");
	public static final String KERNEL = Bundle.getMessage("Kernel");
	public static final String OWNER = Bundle.getMessage("Owner");
	public static final String RWE = Bundle.getMessage("RWE");
	public static final String COMMENT = Bundle.getMessage("Comment");
	public static final String DROP_COMMENT = Bundle.getMessage("SetOut_Msg");
	public static final String PICKUP_COMMENT = Bundle.getMessage("PickUp_Msg");
	public static final String HAZARDOUS = Bundle.getMessage("Hazardous");
	public static final String NONE = " "; // none has be a character or a space
	public static final String TAB = Bundle.getMessage("Tab"); // used to tab out in tabular mode
	public static final String TAB2 = Bundle.getMessage("Tab2");
	public static final String TAB3 = Bundle.getMessage("Tab3");
	public static final String BOX = " [ ] "; // NOI18N

	// these are for the utility printing when using tabs
	public static final String NO_ROAD = "NO_ROAD"; // NOI18N
	public static final String NO_NUMBER = "NO_NUMBER"; // NOI18N
	public static final String NO_COLOR = "NO_COLOR"; // NOI18N

	// truncated manifests
	public static final String NO_DESTINATION = "NO_DESTINATION"; // NOI18N
	public static final String NO_DEST_TRACK = "NO_DEST_TRACK"; // NOI18N
	public static final String NO_LOCATION = "NO_LOCATION"; // NOI18N
	public static final String NO_TRACK = "NO_TRACK"; // NOI18N

	// the supported colors for printed text
	public static final String BLACK = Bundle.getMessage("Black");
	public static final String RED = Bundle.getMessage("Red");
	public static final String ORANGE = Bundle.getMessage("Orange");
	public static final String YELLOW = Bundle.getMessage("Yellow");
	public static final String GREEN = Bundle.getMessage("Green");
	public static final String BLUE = Bundle.getMessage("Blue");
	public static final String GRAY = Bundle.getMessage("Gray");

	// Unit of Length
	public static final String FEET = Bundle.getMessage("Feet");
	public static final String METER = Bundle.getMessage("Meter");

	private static final String[] carAttributes = { ROAD, NUMBER, TYPE, LENGTH, LOAD, HAZARDOUS, COLOR, KERNEL, OWNER,
			TRACK, LOCATION, DESTINATION, DEST_TRACK, FINAL_DEST, FINAL_DEST_TRACK, COMMENT, DROP_COMMENT,
			PICKUP_COMMENT, RWE };
	private static final String[] engineAttributes = { ROAD, NUMBER, TYPE, MODEL, LENGTH, CONSIST, OWNER, TRACK,
			LOCATION, DESTINATION, COMMENT };

	private static int scale = HO_SCALE; // Default scale
	private static int ratio = HO_RATIO;
	private static int ratioTons = HO_RATIO_TONS;
	private static int initWeight = HO_INITIAL_WEIGHT;
	private static int addWeight = HO_ADD_WEIGHT;
	private static String railroadName = "";
	private static int traindir = EAST + WEST + NORTH + SOUTH;
	private static int maxTrainLength = 1000; // maximum train length
	private static int maxEngineSize = 6; // maximum number of engines that can be assigned to a train
	private static int horsePowerPerTon = 1; // Horsepower per ton
	private static int carMoves = 5; // default number of moves when creating a route
	private static String carTypes = DESCRIPTIVE;
	private static String ownerName = "";
	private static String fontName = MONOSPACED;
	private static int manifestFontSize = 10;
	private static int buildReportFontSize = 10;
	private static String manifestOrientation = PORTRAIT;
	private static String switchListOrientation = PORTRAIT;
	private static String pickupColor = BLACK;
	private static String dropColor = BLACK;
	private static String localColor = BLACK;
	private static String[] pickupEngineMessageFormat = { ROAD, NUMBER, NONE, MODEL, NONE, NONE, LOCATION, COMMENT };
	private static String[] dropEngineMessageFormat = { ROAD, NUMBER, NONE, MODEL, NONE, NONE, DESTINATION, COMMENT };
	private static String[] pickupCarMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION,
			COMMENT, PICKUP_COMMENT };
	private static String[] dropCarMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, DESTINATION,
			COMMENT, DROP_COMMENT };
	private static String[] localMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION,
			DESTINATION, COMMENT };
	private static String[] switchListPickupCarMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
			LOCATION, COMMENT, PICKUP_COMMENT };
	private static String[] switchListDropCarMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
			DESTINATION, COMMENT, DROP_COMMENT };
	private static String[] switchListLocalMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
			LOCATION, DESTINATION, COMMENT };
	private static String[] missingCarMessageFormat = { ROAD, NUMBER, TYPE, LENGTH, COLOR, COMMENT };
	private static String pickupEnginePrefix = BOX + Bundle.getMessage("PickUpPrefix");
	private static String dropEnginePrefix = BOX + Bundle.getMessage("SetOutPrefix");
	private static String pickupCarPrefix = BOX + Bundle.getMessage("PickUpPrefix");
	private static String dropCarPrefix = BOX + Bundle.getMessage("SetOutPrefix");
	private static String localPrefix = BOX + Bundle.getMessage("LocalCarPrefix");
	private static String switchListPickupCarPrefix = BOX + Bundle.getMessage("PickUpPrefix");
	private static String switchListDropCarPrefix = BOX + Bundle.getMessage("SetOutPrefix");
	private static String switchListLocalPrefix = BOX + Bundle.getMessage("LocalCarPrefix");
	private static String miaComment = Bundle.getMessage("misplacedCars");
	private static String hazardousMsg = "(" + Bundle.getMessage("Hazardous") + ")";
	private static String logoURL = "";
	private static String panelName = "Panel"; // NOI18N
	private static String buildReportLevel = BUILD_REPORT_VERY_DETAILED;
	private static String routerBuildReportLevel = BUILD_REPORT_NORMAL;
	private static int carSwitchTime = 3; // how long it takes to move a car in minutes
	private static int travelTime = 4; // how long it takes a train to move from one location to another in minutes
	private static String yearModeled = ""; // year being modeled
	private static String lengthUnit = FEET;
	private static String iconNorthColor = "";
	private static String iconSouthColor = "";
	private static String iconEastColor = "";
	private static String iconWestColor = "";
	private static String iconLocalColor = "";
	private static String iconTerminateColor = "";

	private static boolean tab = false; // when true, tab out manifest and switch lists
	private static int tab1CharLength = Control.max_len_string_attibute;
	private static int tab2CharLength = 6; // arbitrary lengths
	private static int tab3CharLength = 8;
	private static boolean twoColumnFormat = false; // when true, use two columns for work at a location
	private static String manifestFormat = STANDARD_FORMAT;
	private static boolean manifestEditorEnabled = false; // when true use text editor to view build report
	private static boolean switchListSameManifest = true; // when true switch list format is the same as the manifest
	private static boolean manifestTruncated = false; // when true, manifest is truncated if switch list is available
	private static boolean manifestDepartureTime = false; // when true, manifest shows train's departure time
	private static boolean switchListRouteComment = true; // when true, switch list have route location comments
	private static boolean switchListRealTime = true; // when true switch list only show work for built trains
	private static boolean switchListAllTrains = true; // when true show all trains that visit the location
	private static boolean switchListPage = false; // when true each train has its own page
	private static boolean buildReportEditorEnabled = false; // when true use text editor to view build report
	private static boolean buildReportIndentEnabled = true; // when true use text editor to view build report
	private static boolean buildReportAlwaysPreviewEnabled = false; // when true use text editor to view build report

	private static boolean enableTrainIconXY = true;
	private static boolean appendTrainIcon = false; // when true, append engine number to train name

	private static boolean mainMenuEnabled = false; // when true add operations menu to main menu bar
	private static boolean closeWindowOnSave = false; // when true, close window when save button is activated
	private static boolean autoSave = true; // when true, automatically save files if modified
	private static boolean autoBackup = true; // when true, automatically backup files
	private static boolean enableValue = false; // when true show value fields for rolling stock
	private static String labelValue = Bundle.getMessage("Value");
	private static boolean enableRfid = false; // when true show RFID fields for rolling stock
	private static String labelRfid = Bundle.getMessage("RFID");

	private static boolean carRoutingEnabled = true; // when true enable car routing
	private static boolean carRoutingYards = true; // when true enable car routing via yard tracks
	private static boolean carRoutingStaging = false; // when true staging tracks can be used for car routing
	private static boolean forwardToYardEnabled = true; // when true forward car to yard if track is full
	private static boolean onlyActiveTrains = false; // when true only active trains are used for routing
	private static boolean checkCarDestination = false; // when true check car's track for valid destination

	private static boolean carLogger = false; // when true car logger is enabled
	private static boolean engineLogger = false; // when true engine logger is enabled
	private static boolean trainLogger = false; // when true train logger is enabled

	private static boolean aggressiveBuild = false; // when true subtract car length from track reserve length
	private static boolean allowLocalInterchangeMoves = false; // when true local interchange to interchange moves are
																// allowed
	private static boolean allowLocalYardMoves = false; // when true local yard to yard moves are allowed
	private static boolean allowLocalSpurMoves = false; // when true local spur to spur moves are allowed

	private static boolean trainIntoStagingCheck = true; // when true staging track must accept train's rolling stock
															// types and roads
	private static boolean trackImmediatelyAvail = false; // when true staging track is immediately available for
															// arrivals after a train is built
	private static boolean allowCarsReturnStaging = false; // when true allow cars on a turn to return to staging if
															// necessary (prevent build failure)
	private static boolean promptFromStaging = false; // when true prompt user to specify which departure staging track
														// to use
	private static boolean promptToStaging = false; // when true prompt user to specify which arrival staging track to
													// use

	private static boolean generateCsvManifest = false; // when true generate csv manifest
	private static boolean generateCsvSwitchList = false; // when true generate csv switch list
	private static boolean enableVsdPhysicalLocations = false;

	private static boolean printLocationComments = false; // when true print location comments on the manifest
	private static boolean printRouteComments = false; // when true print route comments on the manifest
	private static boolean printLoadsAndEmpties = false; // when true print Loads and Empties on the manifest
	private static boolean printTimetableName = false; // when true print timetable name on manifests and switch lists
	private static boolean use12hrFormat = false; // when true use 12hr rather than 24hr format
	private static boolean printValid = true; // when true print out the valid time and date
	private static boolean sortByTrack = false; // when true manifest work is sorted by track names
	private static boolean printHeaders = false; // when true add headers to manifest and switch lists

	public static boolean isMainMenuEnabled() {
		OperationsSetupXml.instance(); // load file
		return mainMenuEnabled;
	}

	public static void setMainMenuEnabled(boolean enabled) {
		mainMenuEnabled = enabled;
	}

	public static boolean isCloseWindowOnSaveEnabled() {
		return closeWindowOnSave;
	}

	public static void setCloseWindowOnSaveEnabled(boolean enabled) {
		closeWindowOnSave = enabled;
	}

	public static boolean isAutoSaveEnabled() {
		return autoSave;
	}

	public static void setAutoSaveEnabled(boolean enabled) {
		boolean old = autoSave;
		autoSave = enabled;
		if (!old && enabled)
			new AutoSave();
	}

	public static boolean isAutoBackupEnabled() {
		return autoBackup;
	}

	public static void setAutoBackupEnabled(boolean enabled) {
		// Do an autoBackup only if we are changing the setting from false to
		// true.
		if (enabled && !autoBackup)
			try {
				new AutoBackup().autoBackup();
			} catch (IOException ex) {
				log.debug("Autobackup after setting AutoBackup flag true", ex);
			}

		autoBackup = enabled;
	}

	public static boolean isValueEnabled() {
		return enableValue;
	}

	public static void setValueEnabled(boolean enabled) {
		enableValue = enabled;
	}

	public static String getValueLabel() {
		return labelValue;
	}

	public static void setValueLabel(String label) {
		labelValue = label;
	}

	public static boolean isRfidEnabled() {
		return enableRfid;
	}

	public static void setRfidEnabled(boolean enabled) {
		enableRfid = enabled;
	}

	public static String getRfidLabel() {
		return labelRfid;
	}

	public static void setRfidLabel(String label) {
		labelRfid = label;
	}

	public static boolean isCarRoutingEnabled() {
		return carRoutingEnabled;
	}

	public static void setCarRoutingEnabled(boolean enabled) {
		carRoutingEnabled = enabled;
	}

	public static boolean isCarRoutingViaYardsEnabled() {
		return carRoutingYards;
	}

	public static void setCarRoutingViaYardsEnabled(boolean enabled) {
		carRoutingYards = enabled;
	}

	public static boolean isCarRoutingViaStagingEnabled() {
		return carRoutingStaging;
	}

	public static void setCarRoutingViaStagingEnabled(boolean enabled) {
		carRoutingStaging = enabled;
	}

	public static boolean isForwardToYardEnabled() {
		return forwardToYardEnabled;
	}

	public static void setForwardToYardEnabled(boolean enabled) {
		forwardToYardEnabled = enabled;
	}

	public static boolean isOnlyActiveTrainsEnabled() {
		return onlyActiveTrains;
	}

	public static void setOnlyActiveTrainsEnabled(boolean enabled) {
		onlyActiveTrains = enabled;
	}

	public static boolean isCheckCarDestinationEnabled() {
		return checkCarDestination;
	}

	public static void setCheckCarDestinationEnabled(boolean enabled) {
		checkCarDestination = enabled;
	}

	public static boolean isBuildAggressive() {
		return aggressiveBuild;
	}

	public static void setBuildAggressive(boolean enabled) {
		aggressiveBuild = enabled;
	}

	public static boolean isLocalInterchangeMovesEnabled() {
		return allowLocalInterchangeMoves;
	}

	public static void setLocalInterchangeMovesEnabled(boolean enabled) {
		allowLocalInterchangeMoves = enabled;
	}

	public static boolean isLocalYardMovesEnabled() {
		return allowLocalYardMoves;
	}

	public static void setLocalYardMovesEnabled(boolean enabled) {
		allowLocalYardMoves = enabled;
	}

	public static boolean isLocalSpurMovesEnabled() {
		return allowLocalSpurMoves;
	}

	public static void setLocalSpurMovesEnabled(boolean enabled) {
		allowLocalSpurMoves = enabled;
	}

	public static boolean isTrainIntoStagingCheckEnabled() {
		return trainIntoStagingCheck;
	}

	public static void setTrainIntoStagingCheckEnabled(boolean enabled) {
		trainIntoStagingCheck = enabled;
	}

	public static boolean isStagingTrackImmediatelyAvail() {
		return trackImmediatelyAvail;
	}

	public static void setStagingTrackImmediatelyAvail(boolean enabled) {
		trackImmediatelyAvail = enabled;
	}

	public static boolean isAllowReturnToStagingEnabled() {
		return allowCarsReturnStaging;
	}

	public static void setAllowReturnToStagingEnabled(boolean enabled) {
		allowCarsReturnStaging = enabled;
	}

	public static boolean isPromptFromStagingEnabled() {
		return promptFromStaging;
	}

	public static void setPromptFromStagingEnabled(boolean enabled) {
		promptFromStaging = enabled;
	}

	public static boolean isPromptToStagingEnabled() {
		return promptToStaging;
	}

	public static void setPromptToStagingEnabled(boolean enabled) {
		promptToStaging = enabled;
	}

	public static boolean isGenerateCsvManifestEnabled() {
		return generateCsvManifest;
	}

	public static void setGenerateCsvManifestEnabled(boolean enabled) {
		generateCsvManifest = enabled;
	}

	public static boolean isGenerateCsvSwitchListEnabled() {
		return generateCsvSwitchList;
	}

	public static void setGenerateCsvSwitchListEnabled(boolean enabled) {
		generateCsvSwitchList = enabled;
	}

	public static boolean isVsdPhysicalLocationEnabled() {
		return enableVsdPhysicalLocations;
	}

	public static void setVsdPhysicalLocationEnabled(boolean enabled) {
		enableVsdPhysicalLocations = enabled;
	}

	public static String getRailroadName() {
		if (railroadName == null)
			return WebServerManager.getWebServerPreferences().getRailRoadName();
		return railroadName;
	}

	public static void setRailroadName(String name) {
		if (!railroadName.equals(name))
			OperationsSetupXml.instance().setDirty(true);
		railroadName = name;
	}

	public static String getHazardousMsg() {
		return hazardousMsg;
	}

	public static void setHazardousMsg(String message) {
		hazardousMsg = message;
	}

	public static String getMiaComment() {
		return miaComment;
	}

	public static void setMiaComment(String comment) {
		miaComment = comment;
	}

	public static void setTrainDirection(int direction) {
		traindir = direction;
	}

	public static int getTrainDirection() {
		return traindir;
	}

	public static void setMaxTrainLength(int length) {
		maxTrainLength = length;
	}

	public static int getMaxTrainLength() {
		return maxTrainLength;
	}

	public static void setMaxNumberEngines(int value) {
		maxEngineSize = value;
	}

	public static int getMaxNumberEngines() {
		return maxEngineSize;
	}

	public static void setHorsePowerPerTon(int value) {
		horsePowerPerTon = value;
	}

	public static int getHorsePowerPerTon() {
		return horsePowerPerTon;
	}

	public static void setCarMoves(int moves) {
		carMoves = moves;
	}

	public static int getCarMoves() {
		return carMoves;
	}

	public static String getPanelName() {
		return panelName;
	}

	public static void setPanelName(String name) {
		panelName = name;
	}

	public static String getLengthUnit() {
		return lengthUnit;
	}

	public static void setLengthUnit(String unit) {
		lengthUnit = unit;
	}

	public static String getYearModeled() {
		return yearModeled;
	}

	public static void setYearModeled(String year) {
		yearModeled = year;
	}

	public static String getCarTypes() {
		return carTypes;
	}

	public static void setCarTypes(String types) {
		carTypes = types;
	}

	public static void setTrainIconCordEnabled(boolean enable) {
		enableTrainIconXY = enable;
	}

	public static boolean isTrainIconCordEnabled() {
		return enableTrainIconXY;
	}

	public static void setTrainIconAppendEnabled(boolean enable) {
		appendTrainIcon = enable;
	}

	public static boolean isTrainIconAppendEnabled() {
		return appendTrainIcon;
	}

	public static void setBuildReportLevel(String level) {
		buildReportLevel = level;
	}

	public static String getBuildReportLevel() {
		return buildReportLevel;
	}

	public static void setRouterBuildReportLevel(String level) {
		routerBuildReportLevel = level;
	}

	public static String getRouterBuildReportLevel() {
		return routerBuildReportLevel;
	}

	public static void setManifestEditorEnabled(boolean enable) {
		manifestEditorEnabled = enable;
	}

	public static boolean isManifestEditorEnabled() {
		return manifestEditorEnabled;
	}

	public static void setBuildReportEditorEnabled(boolean enable) {
		buildReportEditorEnabled = enable;
	}

	public static boolean isBuildReportEditorEnabled() {
		return buildReportEditorEnabled;
	}

	public static void setBuildReportIndentEnabled(boolean enable) {
		buildReportIndentEnabled = enable;
	}

	public static boolean isBuildReportIndentEnabled() {
		return buildReportIndentEnabled;
	}

	public static void setBuildReportAlwaysPreviewEnabled(boolean enable) {
		buildReportAlwaysPreviewEnabled = enable;
	}

	public static boolean isBuildReportAlwaysPreviewEnabled() {
		return buildReportAlwaysPreviewEnabled;
	}

	public static void setSwitchListFormatSameAsManifest(boolean b) {
		switchListSameManifest = b;
	}

	public static boolean isSwitchListFormatSameAsManifest() {
		return switchListSameManifest;
	}
	
	public static void setSwitchListRouteLocationCommentEnabled(boolean b) {
		switchListRouteComment = b;
	}

	public static boolean isSwitchListRouteLocationCommentEnabled() {
		return switchListRouteComment;
	}

	public static void setSwitchListRealTime(boolean b) {
		switchListRealTime = b;
	}

	public static boolean isSwitchListRealTime() {
		return switchListRealTime;
	}

	public static void setSwitchListAllTrainsEnabled(boolean b) {
		switchListAllTrains = b;
	}

	public static boolean isSwitchListAllTrainsEnabled() {
		return switchListAllTrains;
	}

	public static void setSwitchListPagePerTrainEnabled(boolean b) {
		switchListPage = b;
	}

	public static boolean isSwitchListPagePerTrainEnabled() {
		return switchListPage;
	}

	public static void setTruncateManifestEnabled(boolean b) {
		manifestTruncated = b;
	}

	public static boolean isTruncateManifestEnabled() {
		return manifestTruncated;
	}

	public static void setUseDepartureTimeEnabled(boolean b) {
		manifestDepartureTime = b;
	}

	public static boolean isUseDepartureTimeEnabled() {
		return manifestDepartureTime;
	}

	public static void setPrintLocationCommentsEnabled(boolean enable) {
		printLocationComments = enable;
	}

	public static boolean isPrintLocationCommentsEnabled() {
		return printLocationComments;
	}

	public static void setPrintRouteCommentsEnabled(boolean enable) {
		printRouteComments = enable;
	}

	public static boolean isPrintRouteCommentsEnabled() {
		return printRouteComments;
	}

	public static void setPrintLoadsAndEmptiesEnabled(boolean enable) {
		printLoadsAndEmpties = enable;
	}

	public static boolean isPrintLoadsAndEmptiesEnabled() {
		return printLoadsAndEmpties;
	}

	public static void setPrintTimetableNameEnabled(boolean enable) {
		printTimetableName = enable;
	}

	public static boolean isPrintTimetableNameEnabled() {
		return printTimetableName;
	}

	public static void set12hrFormatEnabled(boolean enable) {
		use12hrFormat = enable;
	}

	public static boolean is12hrFormatEnabled() {
		return use12hrFormat;
	}

	public static void setPrintValidEnabled(boolean enable) {
		printValid = enable;
	}

	public static boolean isPrintValidEnabled() {
		return printValid;
	}

	public static void setSortByTrackEnabled(boolean enable) {
		sortByTrack = enable;
	}

	public static boolean isSortByTrackEnabled() {
		return sortByTrack;
	}

	public static void setPrintHeadersEnabled(boolean enable) {
		printHeaders = enable;
	}

	public static boolean isPrintHeadersEnabled() {
		return printHeaders;
	}

	public static void setSwitchTime(int minutes) {
		carSwitchTime = minutes;
	}

	public static int getSwitchTime() {
		return carSwitchTime;
	}

	public static void setTravelTime(int minutes) {
		travelTime = minutes;
	}

	public static int getTravelTime() {
		return travelTime;
	}

	public static void setTrainIconColorNorth(String color) {
		iconNorthColor = color;
	}

	public static String getTrainIconColorNorth() {
		return iconNorthColor;
	}

	public static void setTrainIconColorSouth(String color) {
		iconSouthColor = color;
	}

	public static String getTrainIconColorSouth() {
		return iconSouthColor;
	}

	public static void setTrainIconColorEast(String color) {
		iconEastColor = color;
	}

	public static String getTrainIconColorEast() {
		return iconEastColor;
	}

	public static void setTrainIconColorWest(String color) {
		iconWestColor = color;
	}

	public static String getTrainIconColorWest() {
		return iconWestColor;
	}

	public static void setTrainIconColorLocal(String color) {
		iconLocalColor = color;
	}

	public static String getTrainIconColorLocal() {
		return iconLocalColor;
	}

	public static void setTrainIconColorTerminate(String color) {
		iconTerminateColor = color;
	}

	public static String getTrainIconColorTerminate() {
		return iconTerminateColor;
	}

	public static String getFontName() {
		return fontName;
	}

	public static void setFontName(String name) {
		fontName = name;
	}

	public static int getManifestFontSize() {
		return manifestFontSize;
	}

	public static void setManifestFontSize(int size) {
		manifestFontSize = size;
	}

	public static int getBuildReportFontSize() {
		return buildReportFontSize;
	}

	public static void setBuildReportFontSize(int size) {
		buildReportFontSize = size;
	}

	public static String getManifestOrientation() {
		return manifestOrientation;
	}

	public static void setManifestOrientation(String orientation) {
		manifestOrientation = orientation;
	}

	public static String getSwitchListOrientation() {
		if (isSwitchListFormatSameAsManifest())
			return manifestOrientation;
		else
			return switchListOrientation;
	}

	public static void setSwitchListOrientation(String orientation) {
		switchListOrientation = orientation;
	}

	public static boolean isTabEnabled() {
		return tab;
	}

	public static void setTabEnabled(boolean enable) {
		tab = enable;
	}

	public static int getTab1Length() {
		return tab1CharLength;
	}

	public static void setTab1length(int length) {
		tab1CharLength = length;
	}

	public static int getTab2Length() {
		return tab2CharLength;
	}

	public static void setTab2length(int length) {
		tab2CharLength = length;
	}

	public static int getTab3Length() {
		return tab3CharLength;
	}

	public static void setTab3length(int length) {
		tab3CharLength = length;
	}

	public static boolean isTwoColumnFormatEnabled() {
		return twoColumnFormat;
	}

	public static void setTwoColumnFormatEnabled(boolean enable) {
		twoColumnFormat = enable;
	}
	
	public static String getManifestFormat() {
		return manifestFormat;
	}
	
	public static void setManifestFormat(String format) {
		manifestFormat = format;
	}

	public static boolean isCarLoggerEnabled() {
		return carLogger;
	}

	public static void setCarLoggerEnabled(boolean enable) {
		carLogger = enable;
		RollingStockLogger.instance().enableCarLogging(enable);
	}

	public static boolean isEngineLoggerEnabled() {
		return engineLogger;
	}

	public static void setEngineLoggerEnabled(boolean enable) {
		engineLogger = enable;
		RollingStockLogger.instance().enableEngineLogging(enable);
	}

	public static boolean isTrainLoggerEnabled() {
		return trainLogger;
	}

	public static void setTrainLoggerEnabled(boolean enable) {
		trainLogger = enable;
		TrainLogger.instance().enableTrainLogging(enable);
	}

	public static String getPickupEnginePrefix() {
		return pickupEnginePrefix;
	}

	public static void setPickupEnginePrefix(String prefix) {
		pickupEnginePrefix = prefix;
	}

	public static String getDropEnginePrefix() {
		return dropEnginePrefix;
	}

	public static void setDropEnginePrefix(String prefix) {
		dropEnginePrefix = prefix;
	}

	public static String getPickupCarPrefix() {
		return pickupCarPrefix;
	}

	public static void setPickupCarPrefix(String prefix) {
		pickupCarPrefix = prefix;
	}

	public static String getDropCarPrefix() {
		return dropCarPrefix;
	}

	public static void setDropCarPrefix(String prefix) {
		dropCarPrefix = prefix;
	}

	public static String getLocalPrefix() {
		return localPrefix;
	}

	public static void setLocalPrefix(String prefix) {
		localPrefix = prefix;
	}

	public static int getManifestPrefixLength() {
		int maxLength = getPickupEnginePrefix().length();
		if (getDropEnginePrefix().length() > maxLength)
			maxLength = getDropEnginePrefix().length();
		if (getPickupCarPrefix().length() > maxLength)
			maxLength = getPickupCarPrefix().length();
		if (getDropCarPrefix().length() > maxLength)
			maxLength = getDropCarPrefix().length();
		if (getLocalPrefix().length() > maxLength)
			maxLength = getLocalPrefix().length();
		return maxLength;
	}

	public static String getSwitchListPickupCarPrefix() {
		if (isSwitchListFormatSameAsManifest())
			return pickupCarPrefix;
		else
			return switchListPickupCarPrefix;
	}

	public static void setSwitchListPickupCarPrefix(String prefix) {
		switchListPickupCarPrefix = prefix;
	}

	public static String getSwitchListDropCarPrefix() {
		if (isSwitchListFormatSameAsManifest())
			return dropCarPrefix;
		else
			return switchListDropCarPrefix;
	}

	public static void setSwitchListDropCarPrefix(String prefix) {
		switchListDropCarPrefix = prefix;
	}

	public static String getSwitchListLocalPrefix() {
		if (isSwitchListFormatSameAsManifest())
			return localPrefix;
		else
			return switchListLocalPrefix;
	}

	public static void setSwitchListLocalPrefix(String prefix) {
		switchListLocalPrefix = prefix;
	}

	public static int getSwitchListPrefixLength() {
		int maxLength = getSwitchListPickupCarPrefix().length();
		if (getSwitchListDropCarPrefix().length() > maxLength)
			maxLength = getSwitchListDropCarPrefix().length();
		if (getSwitchListLocalPrefix().length() > maxLength)
			maxLength = getSwitchListLocalPrefix().length();
		return maxLength;
	}

	public static String[] getEngineAttributes() {
		return engineAttributes.clone();
	}

	public static String[] getPickupEngineMessageFormat() {
		return pickupEngineMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupEngineMessageFormat(String[] format) {
		pickupEngineMessageFormat = format;
	}

	public static String[] getDropEngineMessageFormat() {
		return dropEngineMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropEngineMessageFormat(String[] format) {
		dropEngineMessageFormat = format;
	}

	public static String[] getCarAttributes() {
		return carAttributes.clone();
	}

	public static String[] getPickupCarMessageFormat() {
		return pickupCarMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setPickupCarMessageFormat(String[] format) {
		pickupCarMessageFormat = format;
	}

	public static String[] getDropCarMessageFormat() {
		return dropCarMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setDropCarMessageFormat(String[] format) {
		dropCarMessageFormat = format;
	}

	public static String[] getLocalMessageFormat() {
		return localMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setLocalMessageFormat(String[] format) {
		localMessageFormat = format;
	}

	public static String[] getMissingCarMessageFormat() {
		return missingCarMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setMissingCarMessageFormat(String[] format) {
		missingCarMessageFormat = format;
	}

	public static String[] getSwitchListPickupCarMessageFormat() {
		if (isSwitchListFormatSameAsManifest())
			return pickupCarMessageFormat.clone();
		else
			return switchListPickupCarMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListPickupCarMessageFormat(String[] format) {
		switchListPickupCarMessageFormat = format;
	}

	public static String[] getSwitchListDropCarMessageFormat() {
		if (isSwitchListFormatSameAsManifest())
			return dropCarMessageFormat.clone();
		else
			return switchListDropCarMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListDropCarMessageFormat(String[] format) {
		switchListDropCarMessageFormat = format;
	}

	public static String[] getSwitchListLocalMessageFormat() {
		if (isSwitchListFormatSameAsManifest())
			return localMessageFormat.clone();
		else
			return switchListLocalMessageFormat.clone();
	}

	@edu.umd.cs.findbugs.annotations.SuppressWarnings("EI_EXPOSE_STATIC_REP2")
	public static void setSwitchListLocalMessageFormat(String[] format) {
		switchListLocalMessageFormat = format;
	}

	/**
	 * Gets the manifest format for utility cars. The car's road, number, and color are not printed.
	 * 
	 * @return Utility car format
	 */
	public static String[] getPickupUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getPickupCarMessageFormat());
	}

	public static String[] getSetoutUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getDropCarMessageFormat());
	}

	public static String[] getLocalUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getLocalMessageFormat());
	}

	public static String[] getSwitchListPickupUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getSwitchListPickupCarMessageFormat());
	}

	public static String[] getSwitchListSetoutUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getSwitchListDropCarMessageFormat());
	}

	public static String[] getSwitchListLocalUtilityCarMessageFormat() {
		return createUitlityCarMessageFormat(getSwitchListLocalMessageFormat());
	}

	private static String[] createUitlityCarMessageFormat(String[] format) {
		// remove car's road, number, color
		for (int i = 0; i < format.length; i++) {
			if (format[i].equals(ROAD))
				format[i] = NO_ROAD;
			else if (format[i].equals(NUMBER))
				format[i] = NO_NUMBER;
			else if (format[i].equals(COLOR))
				format[i] = NO_COLOR;
		}
		return format;
	}

	public static String[] getTruncatedPickupManifestMessageFormat() {
		return createTruncatedManifestMessageFormat(getPickupCarMessageFormat());
	}

	public static String[] getTruncatedSetoutManifestMessageFormat() {
		return createTruncatedManifestMessageFormat(getDropCarMessageFormat());
	}

	private static String[] createTruncatedManifestMessageFormat(String[] format) {
		// remove car's destination and location
		for (int i = 0; i < format.length; i++) {
			if (format[i].equals(DESTINATION))
				format[i] = NO_DESTINATION;
			else if (format[i].equals(DEST_TRACK))
				format[i] = NO_DEST_TRACK;
			else if (format[i].equals(LOCATION))
				format[i] = NO_LOCATION;
			else if (format[i].equals(TRACK))
				format[i] = NO_TRACK;
		}
		return format;
	}

	public static String getDropTextColor() {
		return dropColor;
	}

	public static void setDropTextColor(String color) {
		dropColor = color;
	}

	public static String getPickupTextColor() {
		return pickupColor;
	}

	public static void setPickupTextColor(String color) {
		pickupColor = color;
	}

	public static String getLocalTextColor() {
		return localColor;
	}

	public static void setLocalTextColor(String color) {
		localColor = color;
	}

	public static Color getPickupColor() {
		return getColor(pickupColor);
	}

	public static Color getDropColor() {
		return getColor(dropColor);
	}

	public static Color getLocalColor() {
		return getColor(localColor);
	}

	private static Color getColor(String color) {
		if (color.equals(BLUE))
			return Color.blue;
		if (color.equals(GREEN))
			return Color.green;
		if (color.equals(RED))
			return Color.red;
		if (color.equals(ORANGE))
			return Color.orange;
		if (color.equals(GRAY))
			return Color.gray;
		if (color.equals(YELLOW))
			return Color.yellow;
		return Color.black; // default
	}

	public static String getManifestLogoURL() {
		return logoURL;
	}

	public static void setManifestLogoURL(String pathName) {
		logoURL = pathName;
	}

	public static String getOwnerName() {
		return ownerName;
	}

	public static void setOwnerName(String name) {
		ownerName = name;
	}

	public static int getScaleRatio() {
		if (scale == 0)
			log.error("Scale not set");
		return ratio;
	}

	public static int getScaleTonRatio() {
		if (scale == 0)
			log.error("Scale not set");
		return ratioTons;
	}

	public static int getInitalWeight() {
		if (scale == 0)
			log.error("Scale not set");
		return initWeight;
	}

	public static int getAddWeight() {
		if (scale == 0)
			log.error("Scale not set");
		return addWeight;
	}

	public static int getScale() {
		return scale;
	}

	public static void setScale(int s) {
		scale = s;
		switch (scale) {
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
			log.error("Unknown scale");
		}
	}

	public static JComboBox getManifestFormatComboBox() {
		JComboBox box = new JComboBox();
		box.addItem(STANDARD_FORMAT);
		box.addItem(TWO_COLUMN_FORMAT);
		box.addItem(TWO_COLUMN_TRACK_FORMAT);
		return box;
	}

	public static JComboBox getOrientationComboBox() {
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
	public static JComboBox getPrintColorComboBox() {
		JComboBox box = new JComboBox();
		box.addItem(BLACK);
		box.addItem(RED);
		box.addItem(ORANGE);
		box.addItem(YELLOW);
		box.addItem(GREEN);
		box.addItem(BLUE);
		box.addItem(GRAY);
		return box;
	}

	public static JComboBox getEngineMessageComboBox() {
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		for (String attribute : getEngineAttributes()) {
			box.addItem(attribute);
		}
		if (isTabEnabled()) {
			box.addItem(TAB);
			box.addItem(TAB2);
			box.addItem(TAB3);
		}
		return box;
	}

	public static JComboBox getCarMessageComboBox() {
		JComboBox box = new JComboBox();
		box.addItem(NONE);
		for (String attribute : getCarAttributes()) {
			box.addItem(attribute);
		}
		if (isTabEnabled()) {
			box.addItem(TAB);
			box.addItem(TAB2);
			box.addItem(TAB3);
		}
		return box;
	}

	/**
	 * 
	 * @return JComboBox loaded with the strings (North, South, East, West) showing the available train directions for
	 *         this railroad
	 */
	public static JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		if ((traindir & EAST) > 0)
			box.addItem(EAST_DIR);
		if ((traindir & WEST) > 0)
			box.addItem(WEST_DIR);
		if ((traindir & NORTH) > 0)
			box.addItem(NORTH_DIR);
		if ((traindir & SOUTH) > 0)
			box.addItem(SOUTH_DIR);
		return box;
	}

	/**
	 * Get train directions
	 * 
	 * @return List of valid train directions
	 */
	public static List<String> getList() {
		List<String> directions = new ArrayList<String>();
		if ((traindir & EAST) > 0)
			directions.add(EAST_DIR);
		if ((traindir & WEST) > 0)
			directions.add(WEST_DIR);
		if ((traindir & NORTH) > 0)
			directions.add(NORTH_DIR);
		if ((traindir & SOUTH) > 0)
			directions.add(SOUTH_DIR);
		return directions;
	}

	/**
	 * Converts binary direction to String direction
	 * 
	 * @param direction
	 *            EAST, WEST, NORTH, SOUTH
	 * @return String representation of a direction
	 */
	public static String getDirectionString(int direction) {
		switch (direction) {
		case EAST:
			return EAST_DIR;
		case WEST:
			return WEST_DIR;
		case NORTH:
			return NORTH_DIR;
		case SOUTH:
			return SOUTH_DIR;
		default:
			return "unknown"; // NOI18N
		}
	}

	/**
	 * Converts binary direction to a set of String directions
	 * 
	 * @param directions
	 *            EAST, WEST, NORTH, SOUTH
	 * @return String[] representation of a set of directions
	 */
	public static String[] getDirectionStrings(int directions) {
		String[] dir = new String[4];
		int i = 0;
		if ((directions & EAST) > 0)
			dir[i++] = EAST_DIR;
		if ((directions & WEST) > 0)
			dir[i++] = WEST_DIR;
		if ((directions & NORTH) > 0)
			dir[i++] = NORTH_DIR;
		if ((directions & SOUTH) > 0)
			dir[i++] = SOUTH_DIR;
		return dir;
	}

	/**
	 * Converts String direction to binary direction
	 * 
	 * @param direction
	 *            EAST_DIR WEST_DIR NORTH_DIR SOUTH_DIR
	 * @return integer representation of a direction
	 */
	public static int getDirectionInt(String direction) {
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
	public static Element store() {
		Element values;
		Element e = new Element(Xml.OPERATIONS);
		e.addContent(values = new Element(Xml.RAIL_ROAD));
		values.setAttribute(Xml.NAME, getRailroadName());

		e.addContent(values = new Element(Xml.SETTINGS));
		values.setAttribute(Xml.MAIN_MENU, isMainMenuEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.CLOSE_ON_SAVE, isCloseWindowOnSaveEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.AUTO_SAVE, isAutoSaveEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.AUTO_BACKUP, isAutoBackupEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.TRAIN_DIRECTION, Integer.toString(getTrainDirection()));
		values.setAttribute(Xml.TRAIN_LENGTH, Integer.toString(getMaxTrainLength()));
		values.setAttribute(Xml.MAX_ENGINES, Integer.toString(getMaxNumberEngines()));
		values.setAttribute(Xml.HPT, Integer.toString(getHorsePowerPerTon()));
		values.setAttribute(Xml.SCALE, Integer.toString(getScale()));
		values.setAttribute(Xml.CAR_TYPES, getCarTypes());
		values.setAttribute(Xml.SWITCH_TIME, Integer.toString(getSwitchTime()));
		values.setAttribute(Xml.TRAVEL_TIME, Integer.toString(getTravelTime()));
		values.setAttribute(Xml.SHOW_VALUE, isValueEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.VALUE_LABEL, getValueLabel());
		values.setAttribute(Xml.SHOW_RFID, isRfidEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.RFID_LABEL, getRfidLabel());
		values.setAttribute(Xml.LENGTH_UNIT, getLengthUnit());
		values.setAttribute(Xml.YEAR_MODELED, getYearModeled());
		// next seven manifest attributes for backward compatibility TODO remove in future release 2014
		values.setAttribute(Xml.PRINT_LOC_COMMENTS, isPrintLocationCommentsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_ROUTE_COMMENTS, isPrintRouteCommentsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_LOADS_EMPTIES, isPrintLoadsAndEmptiesEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_TIMETABLE, isPrintTimetableNameEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.USE12HR_FORMAT, is12hrFormatEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_VALID, isPrintValidEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.SORT_BY_TRACK, isSortByTrackEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_HEADERS, isPrintHeadersEnabled() ? Xml.TRUE : Xml.FALSE);
		// next three logger attributes for backward compatibility TODO remove in future release 2014
		values.setAttribute(Xml.CAR_LOGGER, isCarLoggerEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ENGINE_LOGGER, isEngineLoggerEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.TRAIN_LOGGER, isTrainLoggerEnabled() ? Xml.TRUE : Xml.FALSE);

		e.addContent(values = new Element(Xml.PICKUP_ENG_FORMAT));
		storeXmlMessageFormat(values, getPickupEnginePrefix(), getPickupEngineMessageFormat());

		e.addContent(values = new Element(Xml.DROP_ENG_FORMAT));
		storeXmlMessageFormat(values, getDropEnginePrefix(), getDropEngineMessageFormat());

		e.addContent(values = new Element(Xml.PICKUP_CAR_FORMAT));
		storeXmlMessageFormat(values, getPickupCarPrefix(), getPickupCarMessageFormat());

		e.addContent(values = new Element(Xml.DROP_CAR_FORMAT));
		storeXmlMessageFormat(values, getDropCarPrefix(), getDropCarMessageFormat());

		e.addContent(values = new Element(Xml.LOCAL_FORMAT));
		storeXmlMessageFormat(values, getLocalPrefix(), getLocalMessageFormat());

		e.addContent(values = new Element(Xml.MISSING_CAR_FORMAT));
		storeXmlMessageFormat(values, "", getMissingCarMessageFormat());

		e.addContent(values = new Element(Xml.SWITCH_LIST));
		values.setAttribute(Xml.SAME_AS_MANIFEST, isSwitchListFormatSameAsManifest() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.REAL_TIME, isSwitchListRealTime() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ALL_TRAINS, isSwitchListAllTrainsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PAGE_MODE, isSwitchListPagePerTrainEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_ROUTE_LOCATION, isSwitchListRouteLocationCommentEnabled() ? Xml.TRUE : Xml.FALSE);

		e.addContent(values = new Element(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT));
		storeXmlMessageFormat(values, getSwitchListPickupCarPrefix(), getSwitchListPickupCarMessageFormat());

		e.addContent(values = new Element(Xml.SWITCH_LIST_DROP_CAR_FORMAT));
		storeXmlMessageFormat(values, getSwitchListDropCarPrefix(), getSwitchListDropCarMessageFormat());

		e.addContent(values = new Element(Xml.SWITCH_LIST_LOCAL_FORMAT));
		storeXmlMessageFormat(values, getSwitchListLocalPrefix(), getSwitchListLocalMessageFormat());

		e.addContent(values = new Element(Xml.PANEL));
		values.setAttribute(Xml.NAME, getPanelName());
		values.setAttribute(Xml.TRAIN_ICONXY, isTrainIconCordEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.TRAIN_ICON_APPEND, isTrainIconAppendEnabled() ? Xml.TRUE : Xml.FALSE);

		e.addContent(values = new Element(Xml.FONT_NAME));
		values.setAttribute(Xml.NAME, getFontName());

		e.addContent(values = new Element(Xml.FONT_SIZE));
		values.setAttribute(Xml.SIZE, Integer.toString(getManifestFontSize()));

		e.addContent(values = new Element(Xml.PAGE_ORIENTATION));
		values.setAttribute(Xml.MANIFEST, getManifestOrientation());
		values.setAttribute(Xml.SWITCH_LIST, getSwitchListOrientation());

		e.addContent(values = new Element(Xml.MANIFEST_COLORS));
		values.setAttribute(Xml.DROP_COLOR, getDropTextColor());
		values.setAttribute(Xml.PICKUP_COLOR, getPickupTextColor());
		values.setAttribute(Xml.LOCAL_COLOR, getLocalTextColor());

		e.addContent(values = new Element(Xml.TAB));
		values.setAttribute(Xml.ENABLED, isTabEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.LENGTH, Integer.toString(getTab1Length()));
		values.setAttribute(Xml.TAB2_LENGTH, Integer.toString(getTab2Length()));
		values.setAttribute(Xml.TAB3_LENGTH, Integer.toString(getTab3Length()));

		e.addContent(values = new Element(Xml.MANIFEST));
		values.setAttribute(Xml.PRINT_LOC_COMMENTS, isPrintLocationCommentsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_ROUTE_COMMENTS, isPrintRouteCommentsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_LOADS_EMPTIES, isPrintLoadsAndEmptiesEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_TIMETABLE, isPrintTimetableNameEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.USE12HR_FORMAT, is12hrFormatEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PRINT_VALID, isPrintValidEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.SORT_BY_TRACK, isSortByTrackEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.TRUNCATE, isTruncateManifestEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.USE_DEPARTURE_TIME, isUseDepartureTimeEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.USE_EDITOR, isManifestEditorEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.HAZARDOUS_MSG, getHazardousMsg());

		// backward compatible, remove in 2015
		e.addContent(values = new Element(Xml.COLUMN_FORMAT));
		values.setAttribute(Xml.TWO_COLUMNS, getManifestFormat() == TWO_COLUMN_FORMAT  ? Xml.TRUE : Xml.FALSE);
		// new format June 2014
		e.addContent(values = new Element(Xml.MANIFEST_FORMAT));
		int value = STANDARD;
		if (getManifestFormat().equals(TWO_COLUMN_FORMAT))
			value = TWO_COLUMN;
		else if (getManifestFormat().equals(TWO_COLUMN_TRACK_FORMAT))
			value = TWO_COLUMN_TRACK;
		values.setAttribute(Xml.VALUE, Integer.toString(value));

		if (!getManifestLogoURL().equals("")) {
			values = new Element(Xml.MANIFEST_LOGO);
			values.setAttribute(Xml.NAME, getManifestLogoURL());
			e.addContent(values);
		}

		e.addContent(values = new Element(Xml.BUILD_OPTIONS));
		values.setAttribute(Xml.AGGRESSIVE, isBuildAggressive() ? Xml.TRUE : Xml.FALSE);

		values.setAttribute(Xml.ALLOW_LOCAL_INTERCHANGE, isLocalInterchangeMovesEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ALLOW_LOCAL_SPUR, isLocalSpurMovesEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ALLOW_LOCAL_YARD, isLocalYardMovesEnabled() ? Xml.TRUE : Xml.FALSE);

		values.setAttribute(Xml.STAGING_RESTRICTION_ENABLED, isTrainIntoStagingCheckEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.STAGING_TRACK_AVAIL, isStagingTrackImmediatelyAvail() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ALLOW_RETURN_STAGING, isAllowReturnToStagingEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PROMPT_STAGING_ENABLED, isPromptFromStagingEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.PROMPT_TO_STAGING_ENABLED, isPromptToStagingEnabled() ? Xml.TRUE : Xml.FALSE);

		values.setAttribute(Xml.GENERATE_CSV_MANIFEST, isGenerateCsvManifestEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.GENERATE_CSV_SWITCH_LIST, isGenerateCsvSwitchListEnabled() ? Xml.TRUE : Xml.FALSE);

		e.addContent(values = new Element(Xml.BUILD_REPORT));
		values.setAttribute(Xml.LEVEL, getBuildReportLevel());
		values.setAttribute(Xml.ROUTER_LEVEL, getRouterBuildReportLevel());
		values.setAttribute(Xml.USE_EDITOR, isBuildReportEditorEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.INDENT, isBuildReportIndentEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ALWAYS_PREVIEW, isBuildReportAlwaysPreviewEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.FONT_SIZE, Integer.toString(getBuildReportFontSize()));

		// new format for router options
		e.addContent(values = new Element(Xml.ROUTER));
		values.setAttribute(Xml.CAR_ROUTING_ENABLED, isCarRoutingEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.CAR_ROUTING_VIA_YARDS, isCarRoutingViaYardsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.CAR_ROUTING_VIA_STAGING, isCarRoutingViaStagingEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.FORWARD_TO_YARD, isForwardToYardEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ONLY_ACTIVE_TRAINS, isOnlyActiveTrainsEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.CHECK_CAR_DESTINATION, isCheckCarDestinationEnabled() ? Xml.TRUE : Xml.FALSE);

		// new format for logger options
		e.addContent(values = new Element(Xml.LOGGER));
		values.setAttribute(Xml.CAR_LOGGER, isCarLoggerEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.ENGINE_LOGGER, isEngineLoggerEnabled() ? Xml.TRUE : Xml.FALSE);
		values.setAttribute(Xml.TRAIN_LOGGER, isTrainLoggerEnabled() ? Xml.TRUE : Xml.FALSE);

		e.addContent(values = new Element(Xml.OWNER));
		values.setAttribute(Xml.NAME, getOwnerName());

		e.addContent(values = new Element(Xml.ICON_COLOR));
		values.setAttribute(Xml.NORTH, getTrainIconColorNorth());
		values.setAttribute(Xml.SOUTH, getTrainIconColorSouth());
		values.setAttribute(Xml.EAST, getTrainIconColorEast());
		values.setAttribute(Xml.WEST, getTrainIconColorWest());
		values.setAttribute(Xml.LOCAL, getTrainIconColorLocal());
		values.setAttribute(Xml.TERMINATE, getTrainIconColorTerminate());

		e.addContent(values = new Element(Xml.COMMENTS));
		values.setAttribute(Xml.MISPLACED_CARS, getMiaComment());

		if (isVsdPhysicalLocationEnabled()) {
			e.addContent(values = new Element(Xml.VSD));
			values.setAttribute(Xml.ENABLE_PHYSICAL_LOCATIONS, isVsdPhysicalLocationEnabled() ? Xml.TRUE : Xml.FALSE);
		}

		// Save CATS setting
		e.addContent(values = new Element(Xml.CATS));
		values.setAttribute(Xml.EXACT_LOCATION_NAME, AbstractOperationsServer.isExactLoationNameEnabled() ? Xml.TRUE
				: Xml.FALSE);
		return e;
	}

	private static void storeXmlMessageFormat(Element values, String prefix, String[] messageFormat) {
		values.setAttribute(Xml.PREFIX, prefix);
		StringBuffer buf = new StringBuffer();
		stringToKeyConversion(messageFormat);
		for (String attibute : messageFormat) {
			buf.append(attibute + ",");
		}
		values.setAttribute(Xml.SETTING, buf.toString());
	}

	public static void load(Element e) {
		if (e.getChild(Xml.OPERATIONS) == null) {
			log.debug("operation setup values missing");
			return;
		}
		Element operations = e.getChild(Xml.OPERATIONS);
		org.jdom.Attribute a;

		if ((operations.getChild(Xml.RAIL_ROAD) != null)
				&& (a = operations.getChild(Xml.RAIL_ROAD).getAttribute(Xml.NAME)) != null) {
			String name = a.getValue();
			if (log.isDebugEnabled())
				log.debug("railroadName: " + name);
			railroadName = name; // don't set the dirty bit
		}
		if (operations.getChild(Xml.SETTINGS) != null) {
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.MAIN_MENU)) != null) {
				String enabled = a.getValue();
				if (log.isDebugEnabled())
					log.debug("mainMenu: " + enabled);
				setMainMenuEnabled(enabled.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CLOSE_ON_SAVE)) != null) {
				String enabled = a.getValue();
				if (log.isDebugEnabled())
					log.debug("closeOnSave: " + enabled);
				setCloseWindowOnSaveEnabled(enabled.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_DIRECTION)) != null) {
				String dir = a.getValue();
				if (log.isDebugEnabled())
					log.debug("direction: " + dir);
				setTrainDirection(Integer.parseInt(dir));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_LENGTH)) != null) {
				String length = a.getValue();
				if (log.isDebugEnabled())
					log.debug("Max train length: " + length);
				setMaxTrainLength(Integer.parseInt(length));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.MAX_ENGINES)) != null) {
				String size = a.getValue();
				if (log.isDebugEnabled())
					log.debug("Max number of engines: " + size);
				setMaxNumberEngines(Integer.parseInt(size));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.HPT)) != null) {
				String value = a.getValue();
				if (log.isDebugEnabled())
					log.debug("HPT: " + value);
				setHorsePowerPerTon(Integer.parseInt(value));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SCALE)) != null) {
				String scale = a.getValue();
				if (log.isDebugEnabled())
					log.debug("scale: " + scale);
				setScale(Integer.parseInt(scale));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_TYPES)) != null) {
				String types = a.getValue();
				if (log.isDebugEnabled())
					log.debug("CarTypes: " + types);
				setCarTypes(types);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SWITCH_TIME)) != null) {
				String minutes = a.getValue();
				if (log.isDebugEnabled())
					log.debug("switchTime: " + minutes);
				setSwitchTime(Integer.parseInt(minutes));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAVEL_TIME)) != null) {
				String minutes = a.getValue();
				if (log.isDebugEnabled())
					log.debug("travelTime: " + minutes);
				setTravelTime(Integer.parseInt(minutes));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SHOW_VALUE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("showValue: " + enable);
				setValueEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.VALUE_LABEL)) != null) {
				String label = a.getValue();
				if (log.isDebugEnabled())
					log.debug("valueLabel: " + label);
				setValueLabel(label);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SHOW_RFID)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("showRfid: " + enable);
				setRfidEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.RFID_LABEL)) != null) {
				String label = a.getValue();
				if (log.isDebugEnabled())
					log.debug("rfidLabel: " + label);
				setRfidLabel(label);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.LENGTH_UNIT)) != null) {
				String unit = a.getValue();
				if (log.isDebugEnabled())
					log.debug("lengthUnit: " + unit);
				setLengthUnit(unit);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.YEAR_MODELED)) != null) {
				String year = a.getValue();
				if (log.isDebugEnabled())
					log.debug("yearModeled: " + year);
				setYearModeled(year);
			}
			// next seven attributes are for backward compatibility
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_LOC_COMMENTS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printLocComments: " + enable);
				setPrintLocationCommentsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_ROUTE_COMMENTS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printRouteComments: " + enable);
				setPrintRouteCommentsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_LOADS_EMPTIES)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printLoadsEmpties: " + enable);
				setPrintLoadsAndEmptiesEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_TIMETABLE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printTimetable: " + enable);
				setPrintTimetableNameEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.USE12HR_FORMAT)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("use12hrFormat: " + enable);
				set12hrFormatEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_VALID)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printValid: " + enable);
				setPrintValidEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SORT_BY_TRACK)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("sortByTrack: " + enable);
				setSortByTrackEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_HEADERS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("printHeaders: " + enable);
				setPrintHeadersEnabled(enable.equals(Xml.TRUE));
			}
		}
		if (operations.getChild(Xml.PICKUP_ENG_FORMAT) != null) {
			if ((a = operations.getChild(Xml.PICKUP_ENG_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setPickupEnginePrefix(a.getValue());
			if ((a = operations.getChild(Xml.PICKUP_ENG_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("pickupEngFormat: " + setting);
				String[] keys = setting.split(",");
				keyToStringConversion(keys);
				setPickupEngineMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.DROP_ENG_FORMAT) != null) {
			if ((a = operations.getChild(Xml.DROP_ENG_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setDropEnginePrefix(a.getValue());
			if ((a = operations.getChild(Xml.DROP_ENG_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("dropEngFormat: " + setting);
				String[] keys = setting.split(",");
				keyToStringConversion(keys);
				setDropEngineMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.PICKUP_CAR_FORMAT) != null) {
			if ((a = operations.getChild(Xml.PICKUP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setPickupCarPrefix(a.getValue());
			if ((a = operations.getChild(Xml.PICKUP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("pickupCarFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setPickupCarMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.DROP_CAR_FORMAT) != null) {
			if ((a = operations.getChild(Xml.DROP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setDropCarPrefix(a.getValue());
			if ((a = operations.getChild(Xml.DROP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("dropCarFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setDropCarMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.LOCAL_FORMAT) != null) {
			if ((a = operations.getChild(Xml.LOCAL_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setLocalPrefix(a.getValue());
			if ((a = operations.getChild(Xml.LOCAL_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("localFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setLocalMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.MISSING_CAR_FORMAT) != null) {
			if ((a = operations.getChild(Xml.MISSING_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("missingCarFormat: " + setting);
				String[] keys = setting.split(",");
				keyToStringConversion(keys);
				setMissingCarMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.SWITCH_LIST) != null) {
			if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.SAME_AS_MANIFEST)) != null) {
				String b = a.getValue();
				if (log.isDebugEnabled())
					log.debug("sameAsManifest: " + b);
				setSwitchListFormatSameAsManifest(b.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.REAL_TIME)) != null) {
				String b = a.getValue();
				if (log.isDebugEnabled())
					log.debug("realTime: " + b);
				setSwitchListRealTime(b.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.ALL_TRAINS)) != null) {
				String b = a.getValue();
				if (log.isDebugEnabled())
					log.debug("allTrains: " + b);
				setSwitchListAllTrainsEnabled(b.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.PAGE_MODE)) != null) {
				String b = a.getValue();
				if (log.isDebugEnabled())
					log.debug("pageMode: " + b);
				setSwitchListPagePerTrainEnabled(b.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.PRINT_ROUTE_LOCATION)) != null) {
				String b = a.getValue();
				if (log.isDebugEnabled())
					log.debug("print route location comment: " + b);
				setSwitchListRouteLocationCommentEnabled(b.equals(Xml.TRUE));
			}
		}
		if (operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT) != null) {
			if ((a = operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setSwitchListPickupCarPrefix(a.getValue());
			if ((a = operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("switchListpickupCarFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setSwitchListPickupCarMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT) != null) {
			if ((a = operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setSwitchListDropCarPrefix(a.getValue());
			if ((a = operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("switchListDropCarFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setSwitchListDropCarMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT) != null) {
			if ((a = operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT).getAttribute(Xml.PREFIX)) != null)
				setSwitchListLocalPrefix(a.getValue());
			if ((a = operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT).getAttribute(Xml.SETTING)) != null) {
				String setting = a.getValue();
				if (log.isDebugEnabled())
					log.debug("switchListLocalFormat: " + setting);
				String[] keys = setting.split(",");
				replaceOldFormat(keys);
				keyToStringConversion(keys);
				setSwitchListLocalMessageFormat(keys);
			}
		}
		if (operations.getChild(Xml.PANEL) != null) {
			if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.NAME)) != null) {
				String panel = a.getValue();
				if (log.isDebugEnabled())
					log.debug("panel: " + panel);
				setPanelName(panel);
			}
			if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.TRAIN_ICONXY)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("TrainIconXY: " + enable);
				setTrainIconCordEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.TRAIN_ICON_APPEND)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("TrainIconAppend: " + enable);
				setTrainIconAppendEnabled(enable.equals(Xml.TRUE));
			}
		}
		if ((operations.getChild(Xml.FONT_NAME) != null)
				&& (a = operations.getChild(Xml.FONT_NAME).getAttribute(Xml.NAME)) != null) {
			String font = a.getValue();
			if (log.isDebugEnabled())
				log.debug("fontName: " + font);
			setFontName(font);
		}
		if ((operations.getChild(Xml.FONT_SIZE) != null)
				&& (a = operations.getChild(Xml.FONT_SIZE).getAttribute(Xml.SIZE)) != null) {
			String size = a.getValue();
			if (log.isDebugEnabled())
				log.debug("fontsize: " + size);
			setManifestFontSize(Integer.parseInt(size));
		}

		if ((operations.getChild(Xml.PAGE_ORIENTATION) != null)) {
			if ((a = operations.getChild(Xml.PAGE_ORIENTATION).getAttribute(Xml.MANIFEST)) != null) {
				String orientation = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifestOrientation: " + orientation);
				setManifestOrientation(orientation);
			}
			if ((a = operations.getChild(Xml.PAGE_ORIENTATION).getAttribute(Xml.SWITCH_LIST)) != null) {
				String orientation = a.getValue();
				if (log.isDebugEnabled())
					log.debug("switchListOrientation: " + orientation);
				setSwitchListOrientation(orientation);
			}
		}
		if ((operations.getChild(Xml.MANIFEST_COLORS) != null)) {
			if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.DROP_COLOR)) != null) {
				String dropColor = a.getValue();
				if (log.isDebugEnabled())
					log.debug("dropColor: " + dropColor);
				setDropTextColor(dropColor);
			}
			if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.PICKUP_COLOR)) != null) {
				String pickupColor = a.getValue();
				if (log.isDebugEnabled())
					log.debug("pickupColor: " + pickupColor);
				setPickupTextColor(pickupColor);
			}
			if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.LOCAL_COLOR)) != null) {
				String localColor = a.getValue();
				if (log.isDebugEnabled())
					log.debug("localColor: " + localColor);
				setLocalTextColor(localColor);
			}
		}
		if ((operations.getChild(Xml.TAB) != null)) {
			if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("tab: " + enable);
				setTabEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.LENGTH)) != null) {
				String length = a.getValue();
				if (log.isDebugEnabled())
					log.debug("tab 1 length: " + length);
				setTab1length(Integer.parseInt(length));
			}
			if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.TAB2_LENGTH)) != null) {
				String length = a.getValue();
				if (log.isDebugEnabled())
					log.debug("tab 2 length: " + length);
				setTab2length(Integer.parseInt(length));
			}
			if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.TAB3_LENGTH)) != null) {
				String length = a.getValue();
				if (log.isDebugEnabled())
					log.debug("tab 3 length: " + length);
				setTab3length(Integer.parseInt(length));
			}
		}
		if ((operations.getChild(Xml.MANIFEST) != null)) {
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_LOC_COMMENTS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest printLocComments: " + enable);
				setPrintLocationCommentsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_ROUTE_COMMENTS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest printRouteComments: " + enable);
				setPrintRouteCommentsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_LOADS_EMPTIES)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest printLoadsEmpties: " + enable);
				setPrintLoadsAndEmptiesEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_TIMETABLE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest printTimetable: " + enable);
				setPrintTimetableNameEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE12HR_FORMAT)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest use12hrFormat: " + enable);
				set12hrFormatEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_VALID)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest printValid: " + enable);
				setPrintValidEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.SORT_BY_TRACK)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest sortByTrack: " + enable);
				setSortByTrackEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.TRUNCATE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest truncate: " + enable);
				setTruncateManifestEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE_DEPARTURE_TIME)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest use departure time: " + enable);
				setUseDepartureTimeEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE_EDITOR)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest useEditor: " + enable);
				setManifestEditorEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.HAZARDOUS_MSG)) != null) {
				String message = a.getValue();
				if (log.isDebugEnabled())
					log.debug("manifest hazardousMsg: " + message);
				setHazardousMsg(message);
			}
		}
		if ((operations.getChild(Xml.MANIFEST_FORMAT) != null)) {
			if ((a = operations.getChild(Xml.MANIFEST_FORMAT).getAttribute(Xml.VALUE)) != null) {
				try {
					int value = Integer.parseInt(a.getValue());
					switch (value) {
					case STANDARD:
						manifestFormat = STANDARD_FORMAT;
						break;
					case TWO_COLUMN:
						manifestFormat = TWO_COLUMN_FORMAT;
						break;
					case TWO_COLUMN_TRACK:
						manifestFormat = TWO_COLUMN_TRACK_FORMAT;
						break;
					default:
						log.debug("Unknown manifest format");
					}
				} catch (NumberFormatException ee) {
					log.debug("Manifest format isn't an integer");
				}
			}
		} else if ((operations.getChild(Xml.COLUMN_FORMAT) != null)) {
			if ((a = operations.getChild(Xml.COLUMN_FORMAT).getAttribute(Xml.TWO_COLUMNS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("two columns: " + enable);
				if (enable.equals(Xml.TRUE))
					setManifestFormat(TWO_COLUMN_FORMAT);
			}
		}
		// get manifest logo
		if ((operations.getChild(Xml.MANIFEST_LOGO) != null)) {
			if ((a = operations.getChild(Xml.MANIFEST_LOGO).getAttribute(Xml.NAME)) != null) {
				setManifestLogoURL(a.getValue());
			}
		}
		if ((operations.getChild(Xml.BUILD_OPTIONS) != null)) {
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.AGGRESSIVE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("aggressive: " + enable);
				setBuildAggressive(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_INTERCHANGE)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("noLocalInterchange: " + enable);
				setLocalInterchangeMovesEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_SPUR)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("noLocalSpur: " + enable);
				setLocalSpurMovesEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_YARD)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("noLocalYard: " + enable);
				setLocalYardMovesEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.STAGING_RESTRICTION_ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("stagingRestrictionEnabled: " + enable);
				setTrainIntoStagingCheckEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.STAGING_TRACK_AVAIL)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("stagingTrackAvail: " + enable);
				setStagingTrackImmediatelyAvail(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_RETURN_STAGING)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("allowReturnStaging: " + enable);
				setAllowReturnToStagingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.PROMPT_STAGING_ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("promptStagingEnabled: " + enable);
				setPromptFromStagingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.PROMPT_TO_STAGING_ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("promptToStagingEnabled: " + enable);
				setPromptToStagingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.GENERATE_CSV_MANIFEST)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("generateCvsManifest: " + enable);
				setGenerateCsvManifestEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.GENERATE_CSV_SWITCH_LIST)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("generateCvsSwitchList: " + enable);
				setGenerateCsvSwitchListEnabled(enable.equals(Xml.TRUE));
			}
		}
		if (operations.getChild(Xml.BUILD_REPORT) != null) {
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.LEVEL)) != null) {
				String level = a.getValue();
				if (log.isDebugEnabled())
					log.debug("buildReportLevel: " + level);
				setBuildReportLevel(level);
			}
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.ROUTER_LEVEL)) != null) {
				String level = a.getValue();
				if (log.isDebugEnabled())
					log.debug("routerBuildReportLevel: " + level);
				setRouterBuildReportLevel(level);
			}
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.USE_EDITOR)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("build report useEditor: " + enable);
				setBuildReportEditorEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.INDENT)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("build report indent: " + enable);
				setBuildReportIndentEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.FONT_SIZE)) != null) {
				String size = a.getValue();
				if (log.isDebugEnabled())
					log.debug("build font size: " + size);
				setBuildReportFontSize(Integer.parseInt(size));
			}
			if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.ALWAYS_PREVIEW)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("build report always preview: " + enable);
				setBuildReportAlwaysPreviewEnabled(enable.equals(Xml.TRUE));
			}
		}

		if (operations.getChild(Xml.ROUTER) != null) {
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingEnabled: " + enable);
				setCarRoutingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_VIA_YARDS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingViaYards: " + enable);
				setCarRoutingViaYardsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_VIA_STAGING)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingViaStaging: " + enable);
				setCarRoutingViaStagingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.FORWARD_TO_YARD)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("forwardToYard: " + enable);
				setForwardToYardEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.ONLY_ACTIVE_TRAINS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("onlyActiveTrains: " + enable);
				setOnlyActiveTrainsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CHECK_CAR_DESTINATION)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("checkCarDestination: " + enable);
				setCheckCarDestinationEnabled(enable.equals(Xml.TRUE));
			}
		} else if (operations.getChild(Xml.SETTINGS) != null) {
			// the next four items are for backwards compatibility
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_ENABLED)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingEnabled: " + enable);
				setCarRoutingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_VIA_YARDS)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingViaYards: " + enable);
				setCarRoutingViaYardsEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_VIA_STAGING)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carRoutingViaStaging: " + enable);
				setCarRoutingViaStagingEnabled(enable.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.FORWARD_TO_YARD)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("forwardToYard: " + enable);
				setForwardToYardEnabled(enable.equals(Xml.TRUE));
			}
		}

		if ((operations.getChild(Xml.OWNER) != null)
				&& (a = operations.getChild(Xml.OWNER).getAttribute(Xml.NAME)) != null) {
			String owner = a.getValue();
			if (log.isDebugEnabled())
				log.debug("owner: " + owner);
			setOwnerName(owner);
		}
		if (operations.getChild(Xml.ICON_COLOR) != null) {
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.NORTH)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("north color: " + color);
				setTrainIconColorNorth(color);
			}
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.SOUTH)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("south color: " + color);
				setTrainIconColorSouth(color);
			}
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.EAST)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("east color: " + color);
				setTrainIconColorEast(color);
			}
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.WEST)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("west color: " + color);
				setTrainIconColorWest(color);
			}
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.LOCAL)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("local color: " + color);
				setTrainIconColorLocal(color);
			}
			if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.TERMINATE)) != null) {
				String color = a.getValue();
				if (log.isDebugEnabled())
					log.debug("terminate color: " + color);
				setTrainIconColorTerminate(color);
			}
		}
		if (operations.getChild(Xml.COMMENTS) != null) {
			if ((a = operations.getChild(Xml.COMMENTS).getAttribute(Xml.MISPLACED_CARS)) != null) {
				String comment = a.getValue();
				if (log.isDebugEnabled())
					log.debug("Misplaced comment: " + comment);
				setMiaComment(comment);
			}
		}

		if (operations.getChild(Xml.VSD) != null) {
			if ((a = operations.getChild(Xml.VSD).getAttribute(Xml.ENABLE_PHYSICAL_LOCATIONS)) != null) {
				String enable = a.getValue();
				setVsdPhysicalLocationEnabled(enable.equals(Xml.TRUE));
			}
		}
		if (operations.getChild(Xml.CATS) != null) {
			if ((a = operations.getChild(Xml.CATS).getAttribute(Xml.EXACT_LOCATION_NAME)) != null) {
				String enable = a.getValue();
				AbstractOperationsServer.setExactLocationName(enable.equals(Xml.TRUE));
			}
		}

		if (operations.getChild(Xml.SETTINGS) != null) {
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.AUTO_SAVE)) != null) {
				String enabled = a.getValue();
				if (log.isDebugEnabled())
					log.debug("autoSave: " + enabled);
				setAutoSaveEnabled(enabled.equals(Xml.TRUE));
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.AUTO_BACKUP)) != null) {
				String enabled = a.getValue();
				if (log.isDebugEnabled())
					log.debug("autoBackup: " + enabled);
				setAutoBackupEnabled(enabled.equals(Xml.TRUE));
			}
		}

		if (operations.getChild(Xml.LOGGER) != null) {
			if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.CAR_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carLogger: " + enable);
				carLogger = enable.equals(Xml.TRUE);
			}
			if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.ENGINE_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("engineLogger: " + enable);
				engineLogger = enable.equals(Xml.TRUE);
			}
			if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.TRAIN_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("trainLogger: " + enable);
				trainLogger = enable.equals(Xml.TRUE);
			}
		} else if (operations.getChild(Xml.SETTINGS) != null) {
			// for backward compatibility
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("carLogger: " + enable);
				carLogger = enable.equals(Xml.TRUE);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.ENGINE_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("engineLogger: " + enable);
				engineLogger = enable.equals(Xml.TRUE);
			}
			if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_LOGGER)) != null) {
				String enable = a.getValue();
				if (log.isDebugEnabled())
					log.debug("trainLogger: " + enable);
				trainLogger = enable.equals(Xml.TRUE);
			}
		}
	}

	// replace old pickup and drop message keys
	// Change happened from 2.11.3 to 2.11.4
	// 4/16/2014
	// replace three keys that have spaces in the text
	private static void replaceOldFormat(String[] format) {
		for (int i = 0; i < format.length; i++) {
			if (format[i].equals("Pickup Msg")) // NOI18N
				format[i] = PICKUP_COMMENT;
			else if (format[i].equals("Drop Msg")) // NOI18N
				format[i] = DROP_COMMENT;
			// three keys with spaces that need conversion
			if (format[i].equals("PickUp Msg")) // NOI18N
				format[i] = "PickUp_Msg"; // NOI18N
			else if (format[i].equals("SetOut Msg")) // NOI18N
				format[i] = "SetOut_Msg"; // NOI18N
			else if (format[i].equals("Final Dest")) // NOI18N
				format[i] = "Final_Dest"; // NOI18N
		}
	}

	/**
	 * Converts the xml key to the proper locale text
	 * 
	 * @param keys
	 */
	private static void keyToStringConversion(String[] keys) {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals(NONE))
				continue;
			try {
				keys[i] = Bundle.getMessage(keys[i]);
			} catch (Exception e) {
				log.debug("Key {}: ({}) not found", i, keys[i]);
			}
		}
	}

	private static final String[] attributtes = { "Road", "Number", "Type", "Model", "Length", "Load", "Color",
			"Track", "Destination", "Dest&Track", "Final_Dest", "FD&Track", "Location", "Consist", "Kernel", "Owner",
			"RWE", "Comment", "SetOut_Msg", "PickUp_Msg", "Hazardous", "Tab" };

	/**
	 * Converts the strings into English tags for xml storage
	 * 
	 * @param strings
	 */
	private static void stringToKeyConversion(String[] strings) {
		Locale locale = Locale.ROOT;
		for (int i = 0; i < strings.length; i++) {
			String old = strings[i];
			if (old.equals(NONE))
				continue;
			for (String attribute : attributtes) {
				if (strings[i].equals(Bundle.getMessage(attribute))) {
					strings[i] = Bundle.getMessage(locale, attribute);
					break;
				}
			}
			// log.debug("Converted {} to {}", old, strings[i]);
		}
	}

	static Logger log = LoggerFactory.getLogger(Setup.class.getName());

}
