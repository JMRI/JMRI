package jmri.jmrit.operations.setup;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Disposable;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmris.AbstractOperationsServer;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.trains.TrainLogger;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.ColorUtil;
import jmri.util.swing.JmriColorChooser;
import jmri.web.server.WebServerPreferences;

/**
 * Operations settings.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010, 2012, 2014
 */
public class Setup implements InstanceManagerAutoDefault, Disposable {

    public static final String NONE = "";

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

    public static final String STANDARD_FORMAT = Bundle.getMessage("StandardFormat");
    public static final String TWO_COLUMN_FORMAT = Bundle.getMessage("TwoColumnFormat");
    public static final String TWO_COLUMN_TRACK_FORMAT = Bundle.getMessage("TwoColumnTrackFormat");

    public static final String PORTRAIT = Bundle.getMessage("Portrait");
    public static final String LANDSCAPE = Bundle.getMessage("Landscape");
    public static final String HALFPAGE = Bundle.getMessage("HalfPage");
    public static final String HANDHELD = Bundle.getMessage("HandHeld");

    public static final String PAGE_NORMAL = Bundle.getMessage("PageNormal");
    public static final String PAGE_PER_TRAIN = Bundle.getMessage("PagePerTrain");
    public static final String PAGE_PER_VISIT = Bundle.getMessage("PagePerVisit");

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
    public static final String WEIGHT = Bundle.getMessage("Weight");
    public static final String LOAD = Bundle.getMessage("Load");
    public static final String LOAD_TYPE = Bundle.getMessage("Load_Type");
    public static final String COLOR = Bundle.getMessage("Color");
    public static final String TRACK = Bundle.getMessage("Track");
    public static final String DESTINATION = Bundle.getMessage("Destination");
    public static final String DEST_TRACK = Bundle.getMessage("Dest&Track");
    public static final String FINAL_DEST = Bundle.getMessage("Final_Dest");
    public static final String FINAL_DEST_TRACK = Bundle.getMessage("FD&Track");
    public static final String LOCATION = Bundle.getMessage("Location");
    public static final String CONSIST = Bundle.getMessage("Consist");
    public static final String KERNEL = Bundle.getMessage("Kernel");
    public static final String KERNEL_SIZE = Bundle.getMessage("Kernel_Size");
    public static final String OWNER = Bundle.getMessage("Owner");
    public static final String RWE = Bundle.getMessage("RWE");
    public static final String COMMENT = Bundle.getMessage("Comment");
    public static final String DROP_COMMENT = Bundle.getMessage("SetOut_Msg");
    public static final String PICKUP_COMMENT = Bundle.getMessage("PickUp_Msg");
    public static final String HAZARDOUS = Bundle.getMessage("Hazardous");
    public static final String BLANK = " "; // blank has be a character or a space
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

    // Unit of Length
    public static final String FEET = Bundle.getMessage("Feet");
    public static final String METER = Bundle.getMessage("Meter");

    private static final String[] CAR_ATTRIBUTES
            = {ROAD, NUMBER, TYPE, LENGTH, WEIGHT, LOAD, LOAD_TYPE, HAZARDOUS, COLOR, KERNEL, KERNEL_SIZE, OWNER,
                TRACK, LOCATION, DESTINATION, DEST_TRACK, FINAL_DEST, FINAL_DEST_TRACK, COMMENT, DROP_COMMENT,
                PICKUP_COMMENT, RWE};
    private static final String[] ENGINE_ATTRIBUTES = {ROAD, NUMBER, TYPE, MODEL, LENGTH, WEIGHT, CONSIST, OWNER, TRACK,
        LOCATION, DESTINATION, COMMENT};
    /*
     * The print Manifest and switch list user selectable options are stored in the xml file using the English translation.
     */
    private static final String[] KEYS = {"Road", "Number", "Type", "Model", "Length", "Weight", "Load", "Load_Type", "Color", // NOI18N
        "Track", "Destination", "Dest&Track", "Final_Dest", "FD&Track", "Location", "Consist", "Kernel", // NOI18N
        "Kernel_Size", "Owner", "RWE", "Comment", "SetOut_Msg", "PickUp_Msg", "Hazardous", "Tab", "Tab2", "Tab3"}; // NOI18N

    private int scale = HO_SCALE; // Default scale
    private int ratio = HO_RATIO;
    private int ratioTons = HO_RATIO_TONS;
    private int initWeight = HO_INITIAL_WEIGHT;
    private int addWeight = HO_ADD_WEIGHT;
    private String railroadName = NONE;
    private int traindir = EAST + WEST + NORTH + SOUTH;
    private int maxTrainLength = 1000; // maximum train length
    private int maxEngineSize = 6; // maximum number of engines that can be assigned to a train
    private int horsePowerPerTon = 1; // Horsepower per ton
    private int carMoves = 5; // default number of moves when creating a route
    private String carTypes = DESCRIPTIVE;
    private String ownerName = NONE;
    private String fontName = MONOSPACED;
    private int manifestFontSize = 10;
    private int buildReportFontSize = 10;
    private String manifestOrientation = PORTRAIT;
    private String switchListOrientation = PORTRAIT;
    private Color pickupColor = Color.black;
    private Color dropColor = Color.black;
    private Color localColor = Color.black;
    private String[] pickupEngineMessageFormat = {ROAD, NUMBER, BLANK, MODEL, BLANK, BLANK, LOCATION, COMMENT};
    private String[] dropEngineMessageFormat = {ROAD, NUMBER, BLANK, MODEL, BLANK, BLANK, DESTINATION, COMMENT};
    private String[] pickupManifestMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION,
        COMMENT, PICKUP_COMMENT};
    private String[] dropManifestMessageFormat
            = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, DESTINATION,
                COMMENT, DROP_COMMENT};
    private String[] localManifestMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS, LOCATION,
        DESTINATION, COMMENT};
    private String[] pickupSwitchListMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
        LOCATION, COMMENT, PICKUP_COMMENT};
    private String[] dropSwitchListMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
        DESTINATION, COMMENT, DROP_COMMENT};
    private String[] localSwitchListMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, LOAD, HAZARDOUS,
        LOCATION, DESTINATION, COMMENT};
    private String[] missingCarMessageFormat = {ROAD, NUMBER, TYPE, LENGTH, COLOR, COMMENT};
    private String pickupEnginePrefix = BOX + Bundle.getMessage("PickUpPrefix");
    private String dropEnginePrefix = BOX + Bundle.getMessage("SetOutPrefix");
    private String pickupCarPrefix = BOX + Bundle.getMessage("PickUpPrefix");
    private String dropCarPrefix = BOX + Bundle.getMessage("SetOutPrefix");
    private String localPrefix = BOX + Bundle.getMessage("LocalCarPrefix");
    private String switchListPickupCarPrefix = BOX + Bundle.getMessage("PickUpPrefix");
    private String switchListDropCarPrefix = BOX + Bundle.getMessage("SetOutPrefix");
    private String switchListLocalPrefix = BOX + Bundle.getMessage("LocalCarPrefix");
    private String miaComment = Bundle.getMessage("misplacedCars");
    private String hazardousMsg = "(" + Bundle.getMessage("Hazardous") + ")";
    private String logoURL = NONE;
    private String panelName = "Panel"; // NOI18N
    private String buildReportLevel = BUILD_REPORT_VERY_DETAILED;
    private String routerBuildReportLevel = BUILD_REPORT_NORMAL;
    private int carSwitchTime = 3; // how long it takes to move a car in minutes
    private int travelTime = 4; // how long it takes a train to move from one location to another in minutes
    private String yearModeled = NONE; // year being modeled
    private String lengthUnit = FEET;
    private String iconNorthColor = NONE;
    private String iconSouthColor = NONE;
    private String iconEastColor = NONE;
    private String iconWestColor = NONE;
    private String iconLocalColor = NONE;
    private String iconTerminateColor = NONE;

    private boolean tab = false; // when true, tab out manifest and switch lists
    private int tab1CharLength = Control.max_len_string_attibute;
    private int tab2CharLength = 6; // arbitrary lengths
    private int tab3CharLength = 8;

    private String manifestFormat = STANDARD_FORMAT;
    private boolean manifestEditorEnabled = false; // when true use text editor to view build report
    private boolean switchListSameManifest = true; // when true switch list format is the same as the manifest
    private boolean manifestTruncated = false; // when true, manifest is truncated if switch list is available
    private boolean manifestDepartureTime = false; // when true, manifest shows train's departure time
    private boolean switchListRouteComment = true; // when true, switch list have route location comments
    private boolean trackSummary = true; // when true, print switch list track summary

    private boolean switchListRealTime = true; // when true switch list only show work for built trains
    private boolean switchListAllTrains = true; // when true show all trains that visit the location
    private String switchListPageFormat = PAGE_NORMAL; // how switch lists pages are printed

    private boolean buildReportEditorEnabled = false; // when true use text editor to view build report
    private boolean buildReportIndentEnabled = true; // when true use text editor to view build report
    private boolean buildReportAlwaysPreviewEnabled = false; // when true use text editor to view build report

    private boolean enableTrainIconXY = true;
    private boolean appendTrainIcon = false; // when true, append engine number to train name
    private String setupComment = NONE;

    private boolean mainMenuEnabled = false; // when true add operations menu to main menu bar
    private boolean closeWindowOnSave = false; // when true, close window when save button is activated
    private boolean autoSave = true; // when true, automatically save files if modified
    private boolean autoBackup = true; // when true, automatically backup files
    private boolean enableValue = false; // when true show value fields for rolling stock
    private String labelValue = Bundle.getMessage("Value");
    private boolean enableRfid = false; // when true show RFID fields for rolling stock
    private String labelRfid = Bundle.getMessage("RFID");

    private boolean carRoutingEnabled = true; // when true enable car routing
    private boolean carRoutingYards = true; // when true enable car routing via yard tracks
    private boolean carRoutingStaging = false; // when true staging tracks can be used for car routing
    private boolean forwardToYardEnabled = true; // when true forward car to yard if track is full
    private boolean onlyActiveTrains = false; // when true only active trains are used for routing
    private boolean checkCarDestination = false; // when true check car's track for valid destination

    private boolean carLogger = false; // when true car logger is enabled
    private boolean engineLogger = false; // when true engine logger is enabled
    private boolean trainLogger = false; // when true train logger is enabled
    private boolean saveTrainManifests = false; // when true save previous train manifest

    private boolean aggressiveBuild = false; // when true subtract car length from track reserve length
    private int numberPasses = 2; // the number of passes in train builder
    private boolean allowLocalInterchangeMoves = false; // when true local C/I to C/I moves are allowed
    private boolean allowLocalYardMoves = false; // when true local yard to yard moves are allowed
    private boolean allowLocalSpurMoves = false; // when true local spur to spur moves are allowed

    private boolean trainIntoStagingCheck = true; // staging track must accept train's rolling stock types and roads
    private boolean trackImmediatelyAvail = false; // when true staging track is available for other trains
    private boolean allowCarsReturnStaging = false; // allow cars on a turn to return to staging if necessary (prevent build failure)
    private boolean promptFromStaging = false; // prompt user to specify which departure staging track to use
    private boolean promptToStaging = false; // prompt user to specify which arrival staging track to use

    private boolean generateCsvManifest = false; // when true generate csv manifest
    private boolean generateCsvSwitchList = false; // when true generate csv switch list
    private boolean enableVsdPhysicalLocations = false;

    private boolean printLocationComments = false; // when true print location comments on the manifest
    private boolean printRouteComments = false; // when true print route comments on the manifest
    private boolean printLoadsAndEmpties = false; // when true print Loads and Empties on the manifest
    private boolean printTrainScheduleName = false; // when true print train schedule name on manifests and switch lists
    private boolean use12hrFormat = false; // when true use 12hr rather than 24hr format
    private boolean printValid = true; // when true print out the valid time and date
    private boolean sortByTrack = false; // when true manifest work is sorted by track names
    private boolean printHeaders = false; // when true add headers to manifest and switch lists

    private boolean printCabooseLoad = false; // when true print caboose load
    private boolean printPassengerLoad = false; // when true print passenger car load
    private boolean showTrackMoves = false; // when true show track moves in table

    // property changes
    public static final String SWITCH_LIST_CSV_PROPERTY_CHANGE = "setupSwitchListCSVChange"; //  NOI18N
    public static final String MANIFEST_CSV_PROPERTY_CHANGE = "setupManifestCSVChange"; //  NOI18N
    public static final String REAL_TIME_PROPERTY_CHANGE = "setupSwitchListRealTime"; //  NOI18N
    public static final String SHOW_TRACK_MOVES_PROPERTY_CHANGE = "setupShowTrackMoves"; //  NOI18N
    public static final String SAVE_TRAIN_MANIFEST_PROPERTY_CHANGE = "saveTrainManifestChange"; //  NOI18N
    public static final String ALLOW_CARS_TO_RETURN_PROPERTY_CHANGE = "allowCarsToReturnChange"; //  NOI18N

    public static boolean isMainMenuEnabled() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load file
        return getDefault().mainMenuEnabled;
    }

    public static void setMainMenuEnabled(boolean enabled) {
        getDefault().mainMenuEnabled = enabled;
    }

    public static boolean isCloseWindowOnSaveEnabled() {
        return getDefault().closeWindowOnSave;
    }

    public static void setCloseWindowOnSaveEnabled(boolean enabled) {
        getDefault().closeWindowOnSave = enabled;
    }

    public static boolean isAutoSaveEnabled() {
        return getDefault().autoSave;
    }

    public static void setAutoSaveEnabled(boolean enabled) {
        getDefault().autoSave = enabled;
        if (enabled) {
            new AutoSave().start();
        } else {
            new AutoSave().stop();
        }
    }

    public static boolean isAutoBackupEnabled() {
        return getDefault().autoBackup;
    }

    public static void setAutoBackupEnabled(boolean enabled) {
        // Do an autoBackup only if we are changing the setting from false to
        // true.
        if (enabled && !getDefault().autoBackup) {
            try {
                new AutoBackup().autoBackup();
            } catch (IOException ex) {
                log.debug("Autobackup after setting AutoBackup flag true", ex);
            }
        }

        getDefault().autoBackup = enabled;
    }

    public static boolean isValueEnabled() {
        return getDefault().enableValue;
    }

    public static void setValueEnabled(boolean enabled) {
        getDefault().enableValue = enabled;
    }

    public static String getValueLabel() {
        return getDefault().labelValue;
    }

    public static void setValueLabel(String label) {
        getDefault().labelValue = label;
    }

    public static boolean isRfidEnabled() {
        return getDefault().enableRfid;
    }

    public static void setRfidEnabled(boolean enabled) {
        getDefault().enableRfid = enabled;
    }

    public static String getRfidLabel() {
        return getDefault().labelRfid;
    }

    public static void setRfidLabel(String label) {
        getDefault().labelRfid = label;
    }

    public static boolean isCarRoutingEnabled() {
        return getDefault().carRoutingEnabled;
    }

    public static void setCarRoutingEnabled(boolean enabled) {
        getDefault().carRoutingEnabled = enabled;
    }

    public static boolean isCarRoutingViaYardsEnabled() {
        return getDefault().carRoutingYards;
    }

    public static void setCarRoutingViaYardsEnabled(boolean enabled) {
        getDefault().carRoutingYards = enabled;
    }

    public static boolean isCarRoutingViaStagingEnabled() {
        return getDefault().carRoutingStaging;
    }

    public static void setCarRoutingViaStagingEnabled(boolean enabled) {
        getDefault().carRoutingStaging = enabled;
    }

    public static boolean isForwardToYardEnabled() {
        return getDefault().forwardToYardEnabled;
    }

    public static void setForwardToYardEnabled(boolean enabled) {
        getDefault().forwardToYardEnabled = enabled;
    }

    public static boolean isOnlyActiveTrainsEnabled() {
        return getDefault().onlyActiveTrains;
    }

    public static void setOnlyActiveTrainsEnabled(boolean enabled) {
        getDefault().onlyActiveTrains = enabled;
    }

    /**
     * When true, router checks that the car's destination is serviced by departure track.
     * Very restrictive, not recommended.
     * @return true if enabled.
     */
    public static boolean isCheckCarDestinationEnabled() {
        return getDefault().checkCarDestination;
    }

    public static void setCheckCarDestinationEnabled(boolean enabled) {
        getDefault().checkCarDestination = enabled;
    }

    public static boolean isBuildAggressive() {
        return getDefault().aggressiveBuild;
    }

    public static void setBuildAggressive(boolean enabled) {
        getDefault().aggressiveBuild = enabled;
    }

    public static int getNumberPasses() {
        return getDefault().numberPasses;
    }

    public static void setNumberPasses(int number) {
        getDefault().numberPasses = number;
    }

    public static boolean isLocalInterchangeMovesEnabled() {
        return getDefault().allowLocalInterchangeMoves;
    }

    public static void setLocalInterchangeMovesEnabled(boolean enabled) {
        getDefault().allowLocalInterchangeMoves = enabled;
    }

    public static boolean isLocalYardMovesEnabled() {
        return getDefault().allowLocalYardMoves;
    }

    public static void setLocalYardMovesEnabled(boolean enabled) {
        getDefault().allowLocalYardMoves = enabled;
    }

    public static boolean isLocalSpurMovesEnabled() {
        return getDefault().allowLocalSpurMoves;
    }

    public static void setLocalSpurMovesEnabled(boolean enabled) {
        getDefault().allowLocalSpurMoves = enabled;
    }

    public static boolean isTrainIntoStagingCheckEnabled() {
        return getDefault().trainIntoStagingCheck;
    }

    /**
     * Controls staging track selection, when true, the terminus staging track
     * has to have the same characteristics as the train.
     *
     * @param enabled when true, the terminal staging track must service the
     *            same car types, loads, etc. as the train
     */
    public static void setTrainIntoStagingCheckEnabled(boolean enabled) {
        getDefault().trainIntoStagingCheck = enabled;
    }

    public static boolean isStagingTrackImmediatelyAvail() {
        return getDefault().trackImmediatelyAvail;
    }

    public static void setStagingTrackImmediatelyAvail(boolean enabled) {
        getDefault().trackImmediatelyAvail = enabled;
    }

    /**
     * allow cars to return to the same staging location if no other options (tracks) are available.
     * Also available on a per train basis.
     * @return true if cars are allowed to depart and return to same staging location
     */
    public static boolean isAllowReturnToStagingEnabled() {
        return getDefault().allowCarsReturnStaging;
    }

    public static void setAllowReturnToStagingEnabled(boolean enabled) {
        boolean old = getDefault().allowCarsReturnStaging;
        getDefault().allowCarsReturnStaging = enabled;
        setDirtyAndFirePropertyChange(ALLOW_CARS_TO_RETURN_PROPERTY_CHANGE, old, enabled);
    }

    public static boolean isPromptFromStagingEnabled() {
        return getDefault().promptFromStaging;
    }

    public static void setPromptFromStagingEnabled(boolean enabled) {
        getDefault().promptFromStaging = enabled;
    }

    public static boolean isPromptToStagingEnabled() {
        return getDefault().promptToStaging;
    }

    public static void setPromptToStagingEnabled(boolean enabled) {
        getDefault().promptToStaging = enabled;
    }

    public static boolean isGenerateCsvManifestEnabled() {
        return getDefault().generateCsvManifest;
    }

    public static void setGenerateCsvManifestEnabled(boolean enabled) {
        boolean old = getDefault().generateCsvManifest;
        getDefault().generateCsvManifest = enabled;
        if (enabled && !old) {
            InstanceManager.getDefault(TrainManagerXml.class).createDefaultCsvManifestDirectory();
        }
        setDirtyAndFirePropertyChange(MANIFEST_CSV_PROPERTY_CHANGE, old, enabled);
    }

    public static boolean isGenerateCsvSwitchListEnabled() {
        return getDefault().generateCsvSwitchList;
    }

    public static void setGenerateCsvSwitchListEnabled(boolean enabled) {
        boolean old = getDefault().generateCsvSwitchList;
        getDefault().generateCsvSwitchList = enabled;
        if (enabled && !old) {
            InstanceManager.getDefault(TrainManagerXml.class).createDefaultCsvSwitchListDirectory();
        }
        setDirtyAndFirePropertyChange(SWITCH_LIST_CSV_PROPERTY_CHANGE, old, enabled);
    }

    public static boolean isVsdPhysicalLocationEnabled() {
        return getDefault().enableVsdPhysicalLocations;
    }

    public static void setVsdPhysicalLocationEnabled(boolean enabled) {
        getDefault().enableVsdPhysicalLocations = enabled;
    }

    public static String getRailroadName() {
        if (getDefault().railroadName.isEmpty()) {
            return InstanceManager.getDefault(WebServerPreferences.class).getRailroadName();
        }
        return getDefault().railroadName;
    }

    public static void setRailroadName(String name) {
        String old = getDefault().railroadName;
        getDefault().railroadName = name;
        if (old == null || !old.equals(name)) {
            setDirtyAndFirePropertyChange("Railroad Name Change", old, name); // NOI18N
        }
    }

    public static String getHazardousMsg() {
        return getDefault().hazardousMsg;
    }

    public static void setHazardousMsg(String message) {
        getDefault().hazardousMsg = message;
    }

    public static String getMiaComment() {
        return getDefault().miaComment;
    }

    public static void setMiaComment(String comment) {
        getDefault().miaComment = comment;
    }

    public static void setTrainDirection(int direction) {
        getDefault().traindir = direction;
    }

    public static int getTrainDirection() {
        return getDefault().traindir;
    }

    public static void setMaxTrainLength(int length) {
        getDefault().maxTrainLength = length;
    }

    public static int getMaxTrainLength() {
        return getDefault().maxTrainLength;
    }

    public static void setMaxNumberEngines(int value) {
        getDefault().maxEngineSize = value;
    }

    public static int getMaxNumberEngines() {
        return getDefault().maxEngineSize;
    }

    public static void setHorsePowerPerTon(int value) {
        getDefault().horsePowerPerTon = value;
    }

    public static int getHorsePowerPerTon() {
        return getDefault().horsePowerPerTon;
    }

    public static void setCarMoves(int moves) {
        getDefault().carMoves = moves;
    }

    public static int getCarMoves() {
        return getDefault().carMoves;
    }

    public static String getPanelName() {
        return getDefault().panelName;
    }

    public static void setPanelName(String name) {
        getDefault().panelName = name;
    }

    public static String getLengthUnit() {
        return getDefault().lengthUnit;
    }

    public static void setLengthUnit(String unit) {
        getDefault().lengthUnit = unit;
    }

    public static String getYearModeled() {
        return getDefault().yearModeled;
    }

    public static void setYearModeled(String year) {
        getDefault().yearModeled = year;
    }

    public static String getCarTypes() {
        return getDefault().carTypes;
    }

    public static void setCarTypes(String types) {
        getDefault().carTypes = types;
    }

    public static void setTrainIconCordEnabled(boolean enable) {
        getDefault().enableTrainIconXY = enable;
    }

    public static boolean isTrainIconCordEnabled() {
        return getDefault().enableTrainIconXY;
    }

    public static void setTrainIconAppendEnabled(boolean enable) {
        getDefault().appendTrainIcon = enable;
    }

    public static boolean isTrainIconAppendEnabled() {
        return getDefault().appendTrainIcon;
    }

    public static void setComment(String comment) {
        getDefault().setupComment = comment;
    }

    public static String getComment() {
        return getDefault().setupComment;
    }

    public static void setBuildReportLevel(String level) {
        getDefault().buildReportLevel = level;
    }

    public static String getBuildReportLevel() {
        return getDefault().buildReportLevel;
    }

    /**
     * Sets the report level for the car router.
     * @param level BUILD_REPORT_NORMAL, BUILD_REPORT_DETAILED, BUILD_REPORT_VERY_DETAILED
     */
    public static void setRouterBuildReportLevel(String level) {
        getDefault().routerBuildReportLevel = level;
    }

    public static String getRouterBuildReportLevel() {
        return getDefault().routerBuildReportLevel;
    }

    public static void setManifestEditorEnabled(boolean enable) {
        getDefault().manifestEditorEnabled = enable;
    }

    public static boolean isManifestEditorEnabled() {
        return getDefault().manifestEditorEnabled;
    }

    public static void setBuildReportEditorEnabled(boolean enable) {
        getDefault().buildReportEditorEnabled = enable;
    }

    public static boolean isBuildReportEditorEnabled() {
        return getDefault().buildReportEditorEnabled;
    }

    public static void setBuildReportIndentEnabled(boolean enable) {
        getDefault().buildReportIndentEnabled = enable;
    }

    public static boolean isBuildReportIndentEnabled() {
        return getDefault().buildReportIndentEnabled;
    }

    public static void setBuildReportAlwaysPreviewEnabled(boolean enable) {
        getDefault().buildReportAlwaysPreviewEnabled = enable;
    }

    public static boolean isBuildReportAlwaysPreviewEnabled() {
        return getDefault().buildReportAlwaysPreviewEnabled;
    }

    public static void setSwitchListFormatSameAsManifest(boolean b) {
        getDefault().switchListSameManifest = b;
    }

    public static boolean isSwitchListFormatSameAsManifest() {
        return getDefault().switchListSameManifest;
    }

    public static void setTrackSummaryEnabled(boolean b) {
        getDefault().trackSummary = b;
    }

    public static boolean isTrackSummaryEnabled() {
        return getDefault().trackSummary;
    }

    public static void setSwitchListRouteLocationCommentEnabled(boolean b) {
        getDefault().switchListRouteComment = b;
    }

    public static boolean isSwitchListRouteLocationCommentEnabled() {
        return getDefault().switchListRouteComment;
    }

    public static void setSwitchListRealTime(boolean b) {
        boolean old = getDefault().switchListRealTime;
        getDefault().switchListRealTime = b;
        setDirtyAndFirePropertyChange(REAL_TIME_PROPERTY_CHANGE, old, b);
    }

    public static boolean isSwitchListRealTime() {
        return getDefault().switchListRealTime;
    }

    public static void setSwitchListAllTrainsEnabled(boolean b) {
        boolean old = getDefault().switchListAllTrains;
        getDefault().switchListAllTrains = b;
        setDirtyAndFirePropertyChange("Switch List All Trains", old, b); // NOI18N
    }

    /**
     * When true switch list shows all trains visiting a location, even if the
     * train doesn't have any work at that location. When false, switch lists
     * only report a train if it has work at the location.
     *
     * @return When true show all trains visiting a location.
     */
    public static boolean isSwitchListAllTrainsEnabled() {
        return getDefault().switchListAllTrains;
    }

    /**
     * Used to determine if there's spaces or form feed between trains and
     * locations when printing switch lists. see
     * getSwitchListPageFormatComboBox()
     *
     * @param format PAGE_NORMAL, PAGE_PER_TRAIN, or PAGE_PER_VISIT
     */
    public static void setSwitchListPageFormat(String format) {
        getDefault().switchListPageFormat = format;
    }

    public static String getSwitchListPageFormat() {
        return getDefault().switchListPageFormat;
    }

    public static void setTruncateManifestEnabled(boolean b) {
        getDefault().manifestTruncated = b;
    }

    public static boolean isTruncateManifestEnabled() {
        return getDefault().manifestTruncated;
    }

    public static void setUseDepartureTimeEnabled(boolean b) {
        getDefault().manifestDepartureTime = b;
    }

    public static boolean isUseDepartureTimeEnabled() {
        return getDefault().manifestDepartureTime;
    }

    public static void setPrintLocationCommentsEnabled(boolean enable) {
        getDefault().printLocationComments = enable;
    }

    public static boolean isPrintLocationCommentsEnabled() {
        return getDefault().printLocationComments;
    }

    public static void setPrintRouteCommentsEnabled(boolean enable) {
        getDefault().printRouteComments = enable;
    }

    public static boolean isPrintRouteCommentsEnabled() {
        return getDefault().printRouteComments;
    }

    public static void setPrintLoadsAndEmptiesEnabled(boolean enable) {
        getDefault().printLoadsAndEmpties = enable;
    }

    public static boolean isPrintLoadsAndEmptiesEnabled() {
        return getDefault().printLoadsAndEmpties;
    }

    public static void setPrintTrainScheduleNameEnabled(boolean enable) {
        getDefault().printTrainScheduleName = enable;
    }

    public static boolean isPrintTrainScheduleNameEnabled() {
        return getDefault().printTrainScheduleName;
    }

    public static void set12hrFormatEnabled(boolean enable) {
        getDefault().use12hrFormat = enable;
    }

    public static boolean is12hrFormatEnabled() {
        return getDefault().use12hrFormat;
    }

    public static void setPrintValidEnabled(boolean enable) {
        getDefault().printValid = enable;
    }

    public static boolean isPrintValidEnabled() {
        return getDefault().printValid;
    }

    public static void setSortByTrackNameEnabled(boolean enable) {
        getDefault().sortByTrack = enable;
    }

    /**
     * when true manifest work is sorted by track names.
     * @return true if work at a location is to be sorted by track names.
     */
    public static boolean isSortByTrackNameEnabled() {
        return getDefault().sortByTrack;
    }

    public static void setPrintHeadersEnabled(boolean enable) {
        getDefault().printHeaders = enable;
    }

    public static boolean isPrintHeadersEnabled() {
        return getDefault().printHeaders;
    }

    public static void setPrintCabooseLoadEnabled(boolean enable) {
        getDefault().printCabooseLoad = enable;
    }

    public static boolean isPrintCabooseLoadEnabled() {
        return getDefault().printCabooseLoad;
    }

    public static void setPrintPassengerLoadEnabled(boolean enable) {
        getDefault().printPassengerLoad = enable;
    }

    public static boolean isPrintPassengerLoadEnabled() {
        return getDefault().printPassengerLoad;
    }

    public static void setShowTrackMovesEnabled(boolean enable) {
        boolean old = getDefault().showTrackMoves;
        getDefault().showTrackMoves = enable;
        setDirtyAndFirePropertyChange(SHOW_TRACK_MOVES_PROPERTY_CHANGE, old, enable);
    }

    public static boolean isShowTrackMovesEnabled() {
        return getDefault().showTrackMoves;
    }

    public static void setSwitchTime(int minutes) {
        getDefault().carSwitchTime = minutes;
    }

    public static int getSwitchTime() {
        return getDefault().carSwitchTime;
    }

    public static void setTravelTime(int minutes) {
        getDefault().travelTime = minutes;
    }

    public static int getTravelTime() {
        return getDefault().travelTime;
    }

    public static void setTrainIconColorNorth(String color) {
        getDefault().iconNorthColor = color;
    }

    public static String getTrainIconColorNorth() {
        return getDefault().iconNorthColor;
    }

    public static void setTrainIconColorSouth(String color) {
        getDefault().iconSouthColor = color;
    }

    public static String getTrainIconColorSouth() {
        return getDefault().iconSouthColor;
    }

    public static void setTrainIconColorEast(String color) {
        getDefault().iconEastColor = color;
    }

    public static String getTrainIconColorEast() {
        return getDefault().iconEastColor;
    }

    public static void setTrainIconColorWest(String color) {
        getDefault().iconWestColor = color;
    }

    public static String getTrainIconColorWest() {
        return getDefault().iconWestColor;
    }

    public static void setTrainIconColorLocal(String color) {
        getDefault().iconLocalColor = color;
    }

    public static String getTrainIconColorLocal() {
        return getDefault().iconLocalColor;
    }

    public static void setTrainIconColorTerminate(String color) {
        getDefault().iconTerminateColor = color;
    }

    public static String getTrainIconColorTerminate() {
        return getDefault().iconTerminateColor;
    }

    public static String getFontName() {
        return getDefault().fontName;
    }

    public static void setFontName(String name) {
        getDefault().fontName = name;
    }

    public static int getManifestFontSize() {
        return getDefault().manifestFontSize;
    }

    public static void setManifestFontSize(int size) {
        getDefault().manifestFontSize = size;
    }

    public static int getBuildReportFontSize() {
        return getDefault().buildReportFontSize;
    }

    public static void setBuildReportFontSize(int size) {
        getDefault().buildReportFontSize = size;
    }

    public static String getManifestOrientation() {
        return getDefault().manifestOrientation;
    }

    public static void setManifestOrientation(String orientation) {
        getDefault().manifestOrientation = orientation;
    }

    public static String getSwitchListOrientation() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().manifestOrientation;
        } else {
            return getDefault().switchListOrientation;
        }
    }

    public static void setSwitchListOrientation(String orientation) {
        getDefault().switchListOrientation = orientation;
    }

    public static boolean isTabEnabled() {
        return getDefault().tab;
    }

    public static void setTabEnabled(boolean enable) {
        getDefault().tab = enable;
    }

    public static int getTab1Length() {
        return getDefault().tab1CharLength;
    }

    public static void setTab1length(int length) {
        getDefault().tab1CharLength = length;
    }

    public static int getTab2Length() {
        return getDefault().tab2CharLength;
    }

    public static void setTab2length(int length) {
        getDefault().tab2CharLength = length;
    }

    public static int getTab3Length() {
        return getDefault().tab3CharLength;
    }

    public static void setTab3length(int length) {
        getDefault().tab3CharLength = length;
    }

    public static String getManifestFormat() {
        return getDefault().manifestFormat;
    }

    /**
     * Sets the format for manifests
     * @param format STANDARD_FORMAT, TWO_COLUMN_FORMAT, or TWO_COLUMN_TRACK_FORMAT
     */
    public static void setManifestFormat(String format) {
        getDefault().manifestFormat = format;
    }

    public static boolean isCarLoggerEnabled() {
        return getDefault().carLogger;
    }

    public static void setCarLoggerEnabled(boolean enable) {
        getDefault().carLogger = enable;
        InstanceManager.getDefault(RollingStockLogger.class).enableCarLogging(enable);
    }

    public static boolean isEngineLoggerEnabled() {
        return getDefault().engineLogger;
    }

    public static void setEngineLoggerEnabled(boolean enable) {
        getDefault().engineLogger = enable;
        InstanceManager.getDefault(RollingStockLogger.class).enableEngineLogging(enable);
    }

    public static boolean isTrainLoggerEnabled() {
        return getDefault().trainLogger;
    }

    public static void setTrainLoggerEnabled(boolean enable) {
        getDefault().trainLogger = enable;
        InstanceManager.getDefault(TrainLogger.class).enableTrainLogging(enable);
    }

    public static boolean isSaveTrainManifestsEnabled() {
        return getDefault().saveTrainManifests;
    }

    public static void setSaveTrainManifestsEnabled(boolean enable) {
        boolean old = getDefault().saveTrainManifests;
        getDefault().saveTrainManifests = enable;
        setDirtyAndFirePropertyChange(SAVE_TRAIN_MANIFEST_PROPERTY_CHANGE, old, enable);
    }

    public static String getPickupEnginePrefix() {
        return getDefault().pickupEnginePrefix;
    }

    public static void setPickupEnginePrefix(String prefix) {
        getDefault().pickupEnginePrefix = prefix;
    }

    public static String getDropEnginePrefix() {
        return getDefault().dropEnginePrefix;
    }

    public static void setDropEnginePrefix(String prefix) {
        getDefault().dropEnginePrefix = prefix;
    }

    public static String getPickupCarPrefix() {
        return getDefault().pickupCarPrefix;
    }

    public static void setPickupCarPrefix(String prefix) {
        getDefault().pickupCarPrefix = prefix;
    }

    public static String getDropCarPrefix() {
        return getDefault().dropCarPrefix;
    }

    public static void setDropCarPrefix(String prefix) {
        getDefault().dropCarPrefix = prefix;
    }

    public static String getLocalPrefix() {
        return getDefault().localPrefix;
    }

    public static void setLocalPrefix(String prefix) {
        getDefault().localPrefix = prefix;
    }

    public static int getManifestPrefixLength() {
        int maxLength = getPickupEnginePrefix().length();
        if (getDropEnginePrefix().length() > maxLength) {
            maxLength = getDropEnginePrefix().length();
        }
        if (getPickupCarPrefix().length() > maxLength) {
            maxLength = getPickupCarPrefix().length();
        }
        if (getDropCarPrefix().length() > maxLength) {
            maxLength = getDropCarPrefix().length();
        }
        if (getLocalPrefix().length() > maxLength) {
            maxLength = getLocalPrefix().length();
        }
        return maxLength;
    }

    public static String getSwitchListPickupCarPrefix() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().pickupCarPrefix;
        } else {
            return getDefault().switchListPickupCarPrefix;
        }
    }

    public static void setSwitchListPickupCarPrefix(String prefix) {
        getDefault().switchListPickupCarPrefix = prefix;
    }

    public static String getSwitchListDropCarPrefix() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().dropCarPrefix;
        } else {
            return getDefault().switchListDropCarPrefix;
        }
    }

    public static void setSwitchListDropCarPrefix(String prefix) {
        getDefault().switchListDropCarPrefix = prefix;
    }

    public static String getSwitchListLocalPrefix() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().localPrefix;
        } else {
            return getDefault().switchListLocalPrefix;
        }
    }

    public static void setSwitchListLocalPrefix(String prefix) {
        getDefault().switchListLocalPrefix = prefix;
    }

    public static int getSwitchListPrefixLength() {
        int maxLength = getPickupEnginePrefix().length();
        if (getDropEnginePrefix().length() > maxLength) {
            maxLength = getDropEnginePrefix().length();
        }
        if (getSwitchListPickupCarPrefix().length() > maxLength) {
            maxLength = getSwitchListPickupCarPrefix().length();
        }
        if (getSwitchListDropCarPrefix().length() > maxLength) {
            maxLength = getSwitchListDropCarPrefix().length();
        }
        if (getSwitchListLocalPrefix().length() > maxLength) {
            maxLength = getSwitchListLocalPrefix().length();
        }
        return maxLength;
    }

    public static String[] getEngineAttributes() {
        return ENGINE_ATTRIBUTES.clone();
    }

    public static String[] getPickupEngineMessageFormat() {
        return getDefault().pickupEngineMessageFormat.clone();
    }

    public static void setPickupEngineMessageFormat(String[] format) {
        getDefault().pickupEngineMessageFormat = format;
    }

    public static String[] getDropEngineMessageFormat() {
        return getDefault().dropEngineMessageFormat.clone();
    }

    public static void setDropEngineMessageFormat(String[] format) {
        getDefault().dropEngineMessageFormat = format;
    }

    public static String[] getCarAttributes() {
        return CAR_ATTRIBUTES.clone();
    }

    public static String[] getPickupManifestMessageFormat() {
        return getDefault().pickupManifestMessageFormat.clone();
    }

    public static void setPickupManifestMessageFormat(String[] format) {
        getDefault().pickupManifestMessageFormat = format;
    }

    public static String[] getDropManifestMessageFormat() {
        return getDefault().dropManifestMessageFormat.clone();
    }

    public static void setDropManifestMessageFormat(String[] format) {
        getDefault().dropManifestMessageFormat = format;
    }

    public static String[] getLocalManifestMessageFormat() {
        return getDefault().localManifestMessageFormat.clone();
    }

    public static void setLocalManifestMessageFormat(String[] format) {
        getDefault().localManifestMessageFormat = format;
    }

    public static String[] getMissingCarMessageFormat() {
        return getDefault().missingCarMessageFormat.clone();
    }

    public static void setMissingCarMessageFormat(String[] format) {
        getDefault().missingCarMessageFormat = format;
    }

    public static String[] getPickupSwitchListMessageFormat() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().pickupManifestMessageFormat.clone();
        } else {
            return getDefault().pickupSwitchListMessageFormat.clone();
        }
    }

    public static void setPickupSwitchListMessageFormat(String[] format) {
        getDefault().pickupSwitchListMessageFormat = format;
    }

    public static String[] getDropSwitchListMessageFormat() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().dropManifestMessageFormat.clone();
        } else {
            return getDefault().dropSwitchListMessageFormat.clone();
        }
    }

    public static void setDropSwitchListMessageFormat(String[] format) {
        getDefault().dropSwitchListMessageFormat = format;
    }

    public static String[] getLocalSwitchListMessageFormat() {
        if (isSwitchListFormatSameAsManifest()) {
            return getDefault().localManifestMessageFormat.clone();
        } else {
            return getDefault().localSwitchListMessageFormat.clone();
        }
    }

    public static void setLocalSwitchListMessageFormat(String[] format) {
        getDefault().localSwitchListMessageFormat = format;
    }

    /**
     * Gets the manifest format for utility cars. The car's road, number, and
     * color are not printed.
     *
     * @return Utility car format
     */
    public static String[] getPickupUtilityManifestMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getPickupManifestMessageFormat());
    }

    public static String[] getDropUtilityManifestMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getDropManifestMessageFormat());
    }

    public static String[] getLocalUtilityManifestMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getLocalManifestMessageFormat());
    }

    public static String[] getPickupUtilitySwitchListMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getPickupSwitchListMessageFormat());
    }

    public static String[] getDropUtilitySwitchListMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getDropSwitchListMessageFormat());
    }

    public static String[] getLocalUtilitySwitchListMessageFormat() {
        return getDefault().createUitlityCarMessageFormat(getLocalSwitchListMessageFormat());
    }

    private String[] createUitlityCarMessageFormat(String[] format) {
        // remove car's road, number, color
        for (int i = 0; i < format.length; i++) {
            if (format[i].equals(ROAD)) {
                format[i] = NO_ROAD;
            } else if (format[i].equals(NUMBER)) {
                format[i] = NO_NUMBER;
            } else if (format[i].equals(COLOR)) {
                format[i] = NO_COLOR;
            }
        }
        return format;
    }

    public static String[] getPickupTruncatedManifestMessageFormat() {
        return getDefault().createTruncatedManifestMessageFormat(getPickupManifestMessageFormat());
    }

    public static String[] getDropTruncatedManifestMessageFormat() {
        return getDefault().createTruncatedManifestMessageFormat(getDropManifestMessageFormat());
    }

    private String[] createTruncatedManifestMessageFormat(String[] format) {
        // remove car's destination and location
        for (int i = 0; i < format.length; i++) {
            if (format[i].equals(DESTINATION)) {
                format[i] = NO_DESTINATION;
            } else if (format[i].equals(DEST_TRACK)) {
                format[i] = NO_DEST_TRACK;
            } else if (format[i].equals(LOCATION)) {
                format[i] = NO_LOCATION;
            } else if (format[i].equals(TRACK)) {
                format[i] = NO_TRACK;
            }
        }
        return format;
    }

    public static String[] getPickupTwoColumnByTrackManifestMessageFormat() {
        return getDefault().createTwoColumnByTrackPickupMessageFormat(getPickupManifestMessageFormat());
    }

    public static String[] getPickupTwoColumnByTrackSwitchListMessageFormat() {
        return getDefault().createTwoColumnByTrackPickupMessageFormat(getPickupSwitchListMessageFormat());
    }

    public static String[] getPickupTwoColumnByTrackUtilityManifestMessageFormat() {
        return getDefault().createTwoColumnByTrackPickupMessageFormat(getPickupUtilityManifestMessageFormat());
    }

    public static String[] getPickupTwoColumnByTrackUtilitySwitchListMessageFormat() {
        return getDefault().createTwoColumnByTrackPickupMessageFormat(getPickupUtilitySwitchListMessageFormat());
    }

    private String[] createTwoColumnByTrackPickupMessageFormat(String[] format) {
        for (int i = 0; i < format.length; i++) {
            if (format[i].equals(LOCATION)) {
                format[i] = BLANK;
            } else if (format[i].equals(TRACK)) {
                format[i] = BLANK;
            }
        }
        return format;
    }

    public static String[] getDropTwoColumnByTrackManifestMessageFormat() {
        return getDefault().createTwoColumnByTrackDropMessageFormat(getDropManifestMessageFormat());
    }

    public static String[] getDropTwoColumnByTrackSwitchListMessageFormat() {
        return getDefault().createTwoColumnByTrackDropMessageFormat(getDropSwitchListMessageFormat());
    }

    public static String[] getDropTwoColumnByTrackUtilityManifestMessageFormat() {
        return getDefault().createTwoColumnByTrackDropMessageFormat(getDropUtilityManifestMessageFormat());
    }

    public static String[] getDropTwoColumnByTrackUtilitySwitchListMessageFormat() {
        return getDefault().createTwoColumnByTrackDropMessageFormat(getDropUtilitySwitchListMessageFormat());
    }

    private String[] createTwoColumnByTrackDropMessageFormat(String[] format) {
        for (int i = 0; i < format.length; i++) {
            if (format[i].equals(DESTINATION)) {
                format[i] = BLANK;
            } else if (format[i].equals(TRACK)) {
                format[i] = BLANK;
            }
        }
        return format;
    }

    public static String getDropTextColor() {
        return ColorUtil.colorToColorName(getDefault().dropColor);
    }

    public static void setDropTextColor(String color) {
        setDropColor(ColorUtil.stringToColor(color));
    }

    public static void setDropColor(Color c) {
        getDefault().dropColor = c;
        JmriColorChooser.addRecentColor(c);
    }

    public static String getPickupTextColor() {
        return ColorUtil.colorToColorName(getDefault().pickupColor);
    }

    public static void setPickupTextColor(String color) {
        setPickupColor(ColorUtil.stringToColor(color));
    }

    public static void setPickupColor(Color c) {
        getDefault().pickupColor = c;
        JmriColorChooser.addRecentColor(c);
    }

    public static String getLocalTextColor() {
        return ColorUtil.colorToColorName(getDefault().localColor);
    }

    public static void setLocalTextColor(String color) {
        setLocalColor(ColorUtil.stringToColor(color));
    }

    public static void setLocalColor(Color c) {
        getDefault().localColor = c;
        JmriColorChooser.addRecentColor(c);
    }

    public static Color getPickupColor() {
        return getDefault().pickupColor;
    }

    public static Color getDropColor() {
        return getDefault().dropColor;
    }

    public static Color getLocalColor() {
        return getDefault().localColor;
    }

    public static Color getColor(String colorName) {
        return ColorUtil.stringToColor(colorName);
    }

    public static String getManifestLogoURL() {
        return getDefault().logoURL;
    }

    public static void setManifestLogoURL(String pathName) {
        getDefault().logoURL = pathName;
    }

    public static String getOwnerName() {
        return getDefault().ownerName;
    }

    public static void setOwnerName(String name) {
        getDefault().ownerName = name;
    }

    public static int getScaleRatio() {
        if (getDefault().scale == 0) {
            log.error("Scale not set");
        }
        return getDefault().ratio;
    }

    public static int getScaleTonRatio() {
        if (getDefault().scale == 0) {
            log.error("Scale not set");
        }
        return getDefault().ratioTons;
    }

    public static int getInitalWeight() {
        if (getDefault().scale == 0) {
            log.error("Scale not set");
        }
        return getDefault().initWeight;
    }

    public static int getAddWeight() {
        if (getDefault().scale == 0) {
            log.error("Scale not set");
        }
        return getDefault().addWeight;
    }

    public static int getScale() {
        return getDefault().scale;
    }

    public static void setScale(int s) {
        getDefault().scale = s;
        switch (getDefault().scale) {
            case Z_SCALE:
                getDefault().ratio = Z_RATIO;
                getDefault().initWeight = Z_INITIAL_WEIGHT;
                getDefault().addWeight = Z_ADD_WEIGHT;
                getDefault().ratioTons = Z_RATIO_TONS;
                break;
            case N_SCALE:
                getDefault().ratio = N_RATIO;
                getDefault().initWeight = N_INITIAL_WEIGHT;
                getDefault().addWeight = N_ADD_WEIGHT;
                getDefault().ratioTons = N_RATIO_TONS;
                break;
            case TT_SCALE:
                getDefault().ratio = TT_RATIO;
                getDefault().initWeight = TT_INITIAL_WEIGHT;
                getDefault().addWeight = TT_ADD_WEIGHT;
                getDefault().ratioTons = TT_RATIO_TONS;
                break;
            case HOn3_SCALE:
                getDefault().ratio = HO_RATIO;
                getDefault().initWeight = HOn3_INITIAL_WEIGHT;
                getDefault().addWeight = HOn3_ADD_WEIGHT;
                getDefault().ratioTons = HOn3_RATIO_TONS;
                break;
            case OO_SCALE:
                getDefault().ratio = OO_RATIO;
                getDefault().initWeight = OO_INITIAL_WEIGHT;
                getDefault().addWeight = OO_ADD_WEIGHT;
                getDefault().ratioTons = OO_RATIO_TONS;
                break;
            case HO_SCALE:
                getDefault().ratio = HO_RATIO;
                getDefault().initWeight = HO_INITIAL_WEIGHT;
                getDefault().addWeight = HO_ADD_WEIGHT;
                getDefault().ratioTons = HO_RATIO_TONS;
                break;
            case Sn3_SCALE:
                getDefault().ratio = S_RATIO;
                getDefault().initWeight = Sn3_INITIAL_WEIGHT;
                getDefault().addWeight = Sn3_ADD_WEIGHT;
                getDefault().ratioTons = Sn3_RATIO_TONS;
                break;
            case S_SCALE:
                getDefault().ratio = S_RATIO;
                getDefault().initWeight = S_INITIAL_WEIGHT;
                getDefault().addWeight = S_ADD_WEIGHT;
                getDefault().ratioTons = S_RATIO_TONS;
                break;
            case On3_SCALE:
                getDefault().ratio = O_RATIO;
                getDefault().initWeight = On3_INITIAL_WEIGHT;
                getDefault().addWeight = On3_ADD_WEIGHT;
                getDefault().ratioTons = On3_RATIO_TONS;
                break;
            case O_SCALE:
                getDefault().ratio = O_RATIO;
                getDefault().initWeight = O_INITIAL_WEIGHT;
                getDefault().addWeight = O_ADD_WEIGHT;
                getDefault().ratioTons = O_RATIO_TONS;
                break;
            case G_SCALE:
                getDefault().ratio = G_RATIO;
                getDefault().initWeight = G_INITIAL_WEIGHT;
                getDefault().addWeight = G_ADD_WEIGHT;
                getDefault().ratioTons = G_RATIO_TONS;
                break;
            default:
                log.error("Unknown scale");
        }
    }

    public static JComboBox<String> getManifestFormatComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(STANDARD_FORMAT);
        box.addItem(TWO_COLUMN_FORMAT);
        box.addItem(TWO_COLUMN_TRACK_FORMAT);
        return box;
    }

    public static JComboBox<String> getOrientationComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(PORTRAIT);
        box.addItem(LANDSCAPE);
        box.addItem(HALFPAGE);
        box.addItem(HANDHELD);
        return box;
    }

    public static JComboBox<String> getSwitchListPageFormatComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(PAGE_NORMAL);
        box.addItem(PAGE_PER_TRAIN);
        box.addItem(PAGE_PER_VISIT);
        return box;
    }

    public static JComboBox<String> getEngineMessageComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(BLANK);
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

    public static JComboBox<String> getCarMessageComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(BLANK);
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
     * @return JComboBox loaded with the strings (North, South, East, West)
     *         showing the available train directions for this railroad
     */
    public static JComboBox<String> getTrainDirectionComboBox() {
        JComboBox<String> box = new JComboBox<>();
        for (String direction : getTrainDirectionList()) {
            box.addItem(direction);
        }
        return box;
    }

    /**
     * Get train directions String format
     *
     * @return List of valid train directions
     */
    public static List<String> getTrainDirectionList() {
        List<String> directions = new ArrayList<>();
        if ((getDefault().traindir & EAST) == EAST) {
            directions.add(EAST_DIR);
        }
        if ((getDefault().traindir & WEST) == WEST) {
            directions.add(WEST_DIR);
        }
        if ((getDefault().traindir & NORTH) == NORTH) {
            directions.add(NORTH_DIR);
        }
        if ((getDefault().traindir & SOUTH) == SOUTH) {
            directions.add(SOUTH_DIR);
        }
        return directions;
    }

    /**
     * Converts binary direction to String direction
     *
     * @param direction EAST, WEST, NORTH, SOUTH
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
     * @param directions EAST, WEST, NORTH, SOUTH
     * @return String[] representation of a set of directions
     */
    public static String[] getDirectionStrings(int directions) {
        String[] dir = new String[4];
        int i = 0;
        if ((directions & EAST) == EAST) {
            dir[i++] = EAST_DIR;
        }
        if ((directions & WEST) == WEST) {
            dir[i++] = WEST_DIR;
        }
        if ((directions & NORTH) == NORTH) {
            dir[i++] = NORTH_DIR;
        }
        if ((directions & SOUTH) == SOUTH) {
            dir[i++] = SOUTH_DIR;
        }
        return dir;
    }

    /**
     * Converts String direction to binary direction
     *
     * @param direction EAST_DIR WEST_DIR NORTH_DIR SOUTH_DIR
     * @return integer representation of a direction
     */
    public static int getDirectionInt(String direction) {
        if (direction.equals(EAST_DIR)) {
            return EAST;
        } else if (direction.equals(WEST_DIR)) {
            return WEST;
        } else if (direction.equals(NORTH_DIR)) {
            return NORTH;
        } else if (direction.equals(SOUTH_DIR)) {
            return SOUTH;
        } else {
            return 0; // return unknown
        }
    }

    // must synchronize changes with operation-config.dtd
    public static Element store() {
        Element values;
        Element e = new Element(Xml.OPERATIONS);

        // only store railroad name if it doesn't match the preferences railroad name
        if (!InstanceManager.getDefault(WebServerPreferences.class).getRailroadName().equals(getRailroadName())) {
            e.addContent(values = new Element(Xml.RAIL_ROAD));
            values.setAttribute(Xml.NAME, getRailroadName());
        }

        e.addContent(values = new Element(Xml.SETUP));
        values.setAttribute(Xml.COMMENT, getComment());

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

        e.addContent(values = new Element(Xml.PICKUP_ENG_FORMAT));
        storeXmlMessageFormat(values, getPickupEnginePrefix(), getPickupEngineMessageFormat());

        e.addContent(values = new Element(Xml.DROP_ENG_FORMAT));
        storeXmlMessageFormat(values, getDropEnginePrefix(), getDropEngineMessageFormat());

        e.addContent(values = new Element(Xml.PICKUP_CAR_FORMAT));
        storeXmlMessageFormat(values, getPickupCarPrefix(), getPickupManifestMessageFormat());

        e.addContent(values = new Element(Xml.DROP_CAR_FORMAT));
        storeXmlMessageFormat(values, getDropCarPrefix(), getDropManifestMessageFormat());

        e.addContent(values = new Element(Xml.LOCAL_FORMAT));
        storeXmlMessageFormat(values, getLocalPrefix(), getLocalManifestMessageFormat());

        e.addContent(values = new Element(Xml.MISSING_CAR_FORMAT));
        storeXmlMessageFormat(values, NONE, getMissingCarMessageFormat());

        e.addContent(values = new Element(Xml.SWITCH_LIST));
        values.setAttribute(Xml.SAME_AS_MANIFEST, isSwitchListFormatSameAsManifest() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.REAL_TIME, isSwitchListRealTime() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.ALL_TRAINS, isSwitchListAllTrainsEnabled() ? Xml.TRUE : Xml.FALSE);

        // save switch list format
        String format = Xml.PAGE_NORMAL;
        if (getSwitchListPageFormat().equals(PAGE_PER_TRAIN)) {
            format = Xml.PAGE_PER_TRAIN;
            values.setAttribute(Xml.PAGE_MODE, Xml.TRUE); // backwards compatible for versions before 3.11
        } else if (getSwitchListPageFormat().equals(PAGE_PER_VISIT)) {
            format = Xml.PAGE_PER_VISIT;
        }
        values.setAttribute(Xml.PAGE_FORMAT, format);

        values.setAttribute(Xml.PRINT_ROUTE_LOCATION, isSwitchListRouteLocationCommentEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.TRACK_SUMMARY, isTrackSummaryEnabled() ? Xml.TRUE : Xml.FALSE);

        e.addContent(values = new Element(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT));
        storeXmlMessageFormat(values, getSwitchListPickupCarPrefix(), getPickupSwitchListMessageFormat());

        e.addContent(values = new Element(Xml.SWITCH_LIST_DROP_CAR_FORMAT));
        storeXmlMessageFormat(values, getSwitchListDropCarPrefix(), getDropSwitchListMessageFormat());

        e.addContent(values = new Element(Xml.SWITCH_LIST_LOCAL_FORMAT));
        storeXmlMessageFormat(values, getSwitchListLocalPrefix(), getLocalSwitchListMessageFormat());

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
        values.setAttribute(Xml.PRINT_TRAIN_SCHEDULE, isPrintTrainScheduleNameEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.USE12HR_FORMAT, is12hrFormatEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.PRINT_VALID, isPrintValidEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.SORT_BY_TRACK, isSortByTrackNameEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.PRINT_HEADERS, isPrintHeadersEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.TRUNCATE, isTruncateManifestEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.USE_DEPARTURE_TIME, isUseDepartureTimeEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.USE_EDITOR, isManifestEditorEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.PRINT_CABOOSE_LOAD, isPrintCabooseLoadEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.PRINT_PASSENGER_LOAD, isPrintPassengerLoadEnabled() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.HAZARDOUS_MSG, getHazardousMsg());

        // new format June 2014
        e.addContent(values = new Element(Xml.MANIFEST_FORMAT));

        // save manifest format
        String value = Xml.STANDARD;
        if (getManifestFormat().equals(TWO_COLUMN_FORMAT)) {
            value = Xml.TWO_COLUMN;
        } else if (getManifestFormat().equals(TWO_COLUMN_TRACK_FORMAT)) {
            value = Xml.TWO_COLUMN_TRACK;
        }
        values.setAttribute(Xml.VALUE, value);

        if (!getManifestLogoURL().equals(NONE)) {
            values = new Element(Xml.MANIFEST_LOGO);
            values.setAttribute(Xml.NAME, getManifestLogoURL());
            e.addContent(values);
        }

        // manifest save file options
        e.addContent(values = new Element(Xml.MANIFEST_FILE_OPTIONS));
        values.setAttribute(Xml.MANIFEST_SAVE, isSaveTrainManifestsEnabled() ? Xml.TRUE : Xml.FALSE);

        e.addContent(values = new Element(Xml.BUILD_OPTIONS));
        values.setAttribute(Xml.AGGRESSIVE, isBuildAggressive() ? Xml.TRUE : Xml.FALSE);
        values.setAttribute(Xml.NUMBER_PASSES, Integer.toString(getNumberPasses()));

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

        e.addContent(values = new Element(Xml.DISPLAY));
        values.setAttribute(Xml.SHOW_TRACK_MOVES, isShowTrackMovesEnabled() ? Xml.TRUE : Xml.FALSE);

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
        StringBuilder buf = new StringBuilder();
        stringToTagConversion(messageFormat);
        for (String attibute : messageFormat) {
            buf.append(attibute).append(",");
        }
        values.setAttribute(Xml.SETTING, buf.toString());
    }

    public static void load(Element e) {
        if (e.getChild(Xml.OPERATIONS) == null) {
            log.debug("operation setup values missing");
            return;
        }
        Element operations = e.getChild(Xml.OPERATIONS);
        org.jdom2.Attribute a;

        if ((operations.getChild(Xml.RAIL_ROAD) != null)
                && (a = operations.getChild(Xml.RAIL_ROAD).getAttribute(Xml.NAME)) != null) {
            String name = a.getValue();
            log.debug("railroadName: {}", name);
            // code before 4.11 "useJmriRailroadName" when using the preferences railroad name.
            // here for backwards compatibility
            if (!name.equals(Xml.USE_JMRI_RAILROAD_NAME)) {
                getDefault().railroadName = name; // don't set the dirty bit
            }
        }

        if ((operations.getChild(Xml.SETUP) != null)
                && (a = operations.getChild(Xml.SETUP).getAttribute(Xml.COMMENT)) != null) {
            String comment = a.getValue();
            log.debug("setup comment: {}", comment);
            getDefault().setupComment = comment;
        }

        if (operations.getChild(Xml.SETTINGS) != null) {
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.MAIN_MENU)) != null) {
                String enabled = a.getValue();
                log.debug("mainMenu: {}", enabled);
                setMainMenuEnabled(enabled.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CLOSE_ON_SAVE)) != null) {
                String enabled = a.getValue();
                log.debug("closeOnSave: {}", enabled);
                setCloseWindowOnSaveEnabled(enabled.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_DIRECTION)) != null) {
                String dir = a.getValue();
                log.debug("direction: {}", dir);
                try {
                    setTrainDirection(Integer.parseInt(dir));
                } catch (NumberFormatException ee) {
                    log.error("Train direction ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_LENGTH)) != null) {
                String length = a.getValue();
                log.debug("Max train length: {}", length);
                try {
                    setMaxTrainLength(Integer.parseInt(length));
                } catch (NumberFormatException ee) {
                    log.error("Train maximum length ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.MAX_ENGINES)) != null) {
                String size = a.getValue();
                log.debug("Max number of engines: {}", size);
                try {
                    setMaxNumberEngines(Integer.parseInt(size));
                } catch (NumberFormatException ee) {
                    log.error("Maximum number of engines ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.HPT)) != null) {
                String value = a.getValue();
                log.debug("HPT: {}", value);
                try {
                    setHorsePowerPerTon(Integer.parseInt(value));
                } catch (NumberFormatException ee) {
                    log.error("Train HPT ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SCALE)) != null) {
                String scale = a.getValue();
                log.debug("scale: {}", scale);
                try {
                    setScale(Integer.parseInt(scale));
                } catch (NumberFormatException ee) {
                    log.error("Scale ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_TYPES)) != null) {
                String types = a.getValue();
                log.debug("CarTypes: {}", types);
                setCarTypes(types);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SWITCH_TIME)) != null) {
                String minutes = a.getValue();
                log.debug("switchTime: {}", minutes);
                try {
                    setSwitchTime(Integer.parseInt(minutes));
                } catch (NumberFormatException ee) {
                    log.error("Switch time ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAVEL_TIME)) != null) {
                String minutes = a.getValue();
                log.debug("travelTime: {}", minutes);
                try {
                    setTravelTime(Integer.parseInt(minutes));
                } catch (NumberFormatException ee) {
                    log.error("Travel time ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SHOW_VALUE)) != null) {
                String enable = a.getValue();
                log.debug("showValue: {}", enable);
                setValueEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.VALUE_LABEL)) != null) {
                String label = a.getValue();
                log.debug("valueLabel: {}", label);
                setValueLabel(label);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SHOW_RFID)) != null) {
                String enable = a.getValue();
                log.debug("showRfid: {}", enable);
                setRfidEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.RFID_LABEL)) != null) {
                String label = a.getValue();
                log.debug("rfidLabel: {}", label);
                setRfidLabel(label);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.LENGTH_UNIT)) != null) {
                String unit = a.getValue();
                log.debug("lengthUnit: {}", unit);
                setLengthUnit(unit);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.YEAR_MODELED)) != null) {
                String year = a.getValue();
                log.debug("yearModeled: {}", year);
                setYearModeled(year);
            }
            // next eight attributes are here for backward compatibility
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_LOC_COMMENTS)) != null) {
                String enable = a.getValue();
                log.debug("printLocComments: {}", enable);
                setPrintLocationCommentsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_ROUTE_COMMENTS)) != null) {
                String enable = a.getValue();
                log.debug("printRouteComments: {}", enable);
                setPrintRouteCommentsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_LOADS_EMPTIES)) != null) {
                String enable = a.getValue();
                log.debug("printLoadsEmpties: {}", enable);
                setPrintLoadsAndEmptiesEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_TRAIN_SCHEDULE)) != null) {
                String enable = a.getValue();
                log.debug("printTrainSchedule: {}", enable);
                setPrintTrainScheduleNameEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.USE12HR_FORMAT)) != null) {
                String enable = a.getValue();
                log.debug("use12hrFormat: {}", enable);
                set12hrFormatEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_VALID)) != null) {
                String enable = a.getValue();
                log.debug("printValid: {}", enable);
                setPrintValidEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.SORT_BY_TRACK)) != null) {
                String enable = a.getValue();
                log.debug("sortByTrack: {}", enable);
                setSortByTrackNameEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.PRINT_HEADERS)) != null) {
                String enable = a.getValue();
                log.debug("printHeaders: {}", enable);
                setPrintHeadersEnabled(enable.equals(Xml.TRUE));
            }
        }
        if (operations.getChild(Xml.PICKUP_ENG_FORMAT) != null) {
            if ((a = operations.getChild(Xml.PICKUP_ENG_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setPickupEnginePrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.PICKUP_ENG_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("pickupEngFormat: {}", setting);
                String[] keys = setting.split(",");
                keyToStringConversion(keys);
                setPickupEngineMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.DROP_ENG_FORMAT) != null) {
            if ((a = operations.getChild(Xml.DROP_ENG_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setDropEnginePrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.DROP_ENG_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("dropEngFormat: {}", setting);
                String[] keys = setting.split(",");
                keyToStringConversion(keys);
                setDropEngineMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.PICKUP_CAR_FORMAT) != null) {
            if ((a = operations.getChild(Xml.PICKUP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setPickupCarPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.PICKUP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("pickupCarFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setPickupManifestMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.DROP_CAR_FORMAT) != null) {
            if ((a = operations.getChild(Xml.DROP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setDropCarPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.DROP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("dropCarFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setDropManifestMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.LOCAL_FORMAT) != null) {
            if ((a = operations.getChild(Xml.LOCAL_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setLocalPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.LOCAL_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("localFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setLocalManifestMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.MISSING_CAR_FORMAT) != null) {
            if ((a = operations.getChild(Xml.MISSING_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("missingCarFormat: {}", setting);
                String[] keys = setting.split(",");
                keyToStringConversion(keys);
                setMissingCarMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.SWITCH_LIST) != null) {
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.SAME_AS_MANIFEST)) != null) {
                String b = a.getValue();
                log.debug("sameAsManifest: {}", b);
                setSwitchListFormatSameAsManifest(b.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.REAL_TIME)) != null) {
                String b = a.getValue();
                log.debug("realTime: {}", b);
                getDefault().switchListRealTime = b.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.ALL_TRAINS)) != null) {
                String b = a.getValue();
                log.debug("allTrains: {}", b);
                getDefault().switchListAllTrains = b.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.PAGE_FORMAT)) != null) {
                switch (a.getValue()) {
                    case Xml.PAGE_NORMAL:
                        getDefault().switchListPageFormat = PAGE_NORMAL;
                        break;
                    case Xml.PAGE_PER_TRAIN:
                        getDefault().switchListPageFormat = PAGE_PER_TRAIN;
                        break;
                    case Xml.PAGE_PER_VISIT:
                        getDefault().switchListPageFormat = PAGE_PER_VISIT;
                        break;
                    default:
                        log.error("Unknown switch list page format {}", a.getValue());
                }
            } // old way to save switch list page format pre 3.11
            else if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.PAGE_MODE)) != null) {
                String b = a.getValue();
                log.debug("old style pageMode: {}", b);
                if (b.equals(Xml.TRUE)) {
                    getDefault().switchListPageFormat = PAGE_PER_TRAIN;
                }
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.PRINT_ROUTE_LOCATION)) != null) {
                String b = a.getValue();
                log.debug("print route location comment: {}", b);
                setSwitchListRouteLocationCommentEnabled(b.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST).getAttribute(Xml.TRACK_SUMMARY)) != null) {
                String b = a.getValue();
                log.debug("track summary: {}", b);
                setTrackSummaryEnabled(b.equals(Xml.TRUE));
            }
        }
        if (operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT) != null) {
            if ((a = operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setSwitchListPickupCarPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST_PICKUP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("switchListpickupCarFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setPickupSwitchListMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT) != null) {
            if ((a = operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setSwitchListDropCarPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST_DROP_CAR_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("switchListDropCarFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setDropSwitchListMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT) != null) {
            if ((a = operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT).getAttribute(Xml.PREFIX)) != null) {
                setSwitchListLocalPrefix(a.getValue());
            }
            if ((a = operations.getChild(Xml.SWITCH_LIST_LOCAL_FORMAT).getAttribute(Xml.SETTING)) != null) {
                String setting = a.getValue();
                log.debug("switchListLocalFormat: {}", setting);
                String[] keys = setting.split(",");
                replaceOldFormat(keys);
                xmlAttributeToKeyConversion(keys);
                keyToStringConversion(keys);
                setLocalSwitchListMessageFormat(keys);
            }
        }
        if (operations.getChild(Xml.PANEL) != null) {
            if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.NAME)) != null) {
                String panel = a.getValue();
                log.debug("panel: {}", panel);
                setPanelName(panel);
            }
            if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.TRAIN_ICONXY)) != null) {
                String enable = a.getValue();
                log.debug("TrainIconXY: {}", enable);
                setTrainIconCordEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.PANEL).getAttribute(Xml.TRAIN_ICON_APPEND)) != null) {
                String enable = a.getValue();
                log.debug("TrainIconAppend: {}", enable);
                setTrainIconAppendEnabled(enable.equals(Xml.TRUE));
            }
        }
        if ((operations.getChild(Xml.FONT_NAME) != null)
                && (a = operations.getChild(Xml.FONT_NAME).getAttribute(Xml.NAME)) != null) {
            String font = a.getValue();
            log.debug("fontName: {}", font);
            setFontName(font);
        }
        if ((operations.getChild(Xml.FONT_SIZE) != null)
                && (a = operations.getChild(Xml.FONT_SIZE).getAttribute(Xml.SIZE)) != null) {
            String size = a.getValue();
            log.debug("fontsize: {}", size);
            try {
                setManifestFontSize(Integer.parseInt(size));
            } catch (NumberFormatException ee) {
                log.error("Manifest font size ({}) isn't a valid number", a.getValue());
            }
        }
        if ((operations.getChild(Xml.PAGE_ORIENTATION) != null)) {
            if ((a = operations.getChild(Xml.PAGE_ORIENTATION).getAttribute(Xml.MANIFEST)) != null) {
                String orientation = a.getValue();
                log.debug("manifestOrientation: {}", orientation);
                setManifestOrientation(orientation);
            }
            if ((a = operations.getChild(Xml.PAGE_ORIENTATION).getAttribute(Xml.SWITCH_LIST)) != null) {
                String orientation = a.getValue();
                log.debug("switchListOrientation: {}", orientation);
                setSwitchListOrientation(orientation);
            }
        }
        if ((operations.getChild(Xml.MANIFEST_COLORS) != null)) {
            if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.DROP_COLOR)) != null) {
                String dropColor = a.getValue();
                log.debug("dropColor: {}", dropColor);
                setDropTextColor(dropColor);
            }
            if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.PICKUP_COLOR)) != null) {
                String pickupColor = a.getValue();
                log.debug("pickupColor: {}", pickupColor);
                setPickupTextColor(pickupColor);
            }
            if ((a = operations.getChild(Xml.MANIFEST_COLORS).getAttribute(Xml.LOCAL_COLOR)) != null) {
                String localColor = a.getValue();
                log.debug("localColor: {}", localColor);
                setLocalTextColor(localColor);
            }
        }
        if ((operations.getChild(Xml.TAB) != null)) {
            if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("tab: {}", enable);
                setTabEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.LENGTH)) != null) {
                String length = a.getValue();
                log.debug("tab 1 length: {}", length);
                try {
                    setTab1length(Integer.parseInt(length));
                } catch (NumberFormatException ee) {
                    log.error("Tab 1 length ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.TAB2_LENGTH)) != null) {
                String length = a.getValue();
                log.debug("tab 2 length: {}", length);
                try {
                    setTab2length(Integer.parseInt(length));
                } catch (NumberFormatException ee) {
                    log.error("Tab 2 length ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.TAB).getAttribute(Xml.TAB3_LENGTH)) != null) {
                String length = a.getValue();
                log.debug("tab 3 length: {}", length);
                try {
                    setTab3length(Integer.parseInt(length));
                } catch (NumberFormatException ee) {
                    log.error("Tab 3 length ({}) isn't a valid number", a.getValue());
                }
            }
        }
        if ((operations.getChild(Xml.MANIFEST) != null)) {
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_LOC_COMMENTS)) != null) {
                String enable = a.getValue();
                log.debug("manifest printLocComments: {}", enable);
                setPrintLocationCommentsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_ROUTE_COMMENTS)) != null) {
                String enable = a.getValue();
                log.debug("manifest printRouteComments: {}", enable);
                setPrintRouteCommentsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_LOADS_EMPTIES)) != null) {
                String enable = a.getValue();
                log.debug("manifest printLoadsEmpties: {}", enable);
                setPrintLoadsAndEmptiesEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_TRAIN_SCHEDULE)) != null) {
                String enable = a.getValue();
                log.debug("manifest printTrainSchedule: {}", enable);
                setPrintTrainScheduleNameEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE12HR_FORMAT)) != null) {
                String enable = a.getValue();
                log.debug("manifest use12hrFormat: {}", enable);
                set12hrFormatEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_VALID)) != null) {
                String enable = a.getValue();
                log.debug("manifest printValid: {}", enable);
                setPrintValidEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.SORT_BY_TRACK)) != null) {
                String enable = a.getValue();
                log.debug("manifest sortByTrack: {}", enable);
                setSortByTrackNameEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_HEADERS)) != null) {
                String enable = a.getValue();
                log.debug("manifest print headers: {}", enable);
                setPrintHeadersEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.TRUNCATE)) != null) {
                String enable = a.getValue();
                log.debug("manifest truncate: {}", enable);
                setTruncateManifestEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE_DEPARTURE_TIME)) != null) {
                String enable = a.getValue();
                log.debug("manifest use departure time: {}", enable);
                setUseDepartureTimeEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.USE_EDITOR)) != null) {
                String enable = a.getValue();
                log.debug("manifest useEditor: {}", enable);
                setManifestEditorEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_CABOOSE_LOAD)) != null) {
                String enable = a.getValue();
                log.debug("manifest print caboose load: {}", enable);
                setPrintCabooseLoadEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.PRINT_PASSENGER_LOAD)) != null) {
                String enable = a.getValue();
                log.debug("manifest print passenger load: {}", enable);
                setPrintPassengerLoadEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.MANIFEST).getAttribute(Xml.HAZARDOUS_MSG)) != null) {
                String message = a.getValue();
                log.debug("manifest hazardousMsg: {}", message);
                setHazardousMsg(message);
            }
        }
        if ((operations.getChild(Xml.MANIFEST_FORMAT) != null)) {
            if ((a = operations.getChild(Xml.MANIFEST_FORMAT).getAttribute(Xml.VALUE)) != null) {
                switch (a.getValue()) {
                    case Xml.STANDARD:
                        getDefault().manifestFormat = STANDARD_FORMAT;
                        break;
                    case Xml.TWO_COLUMN:
                        getDefault().manifestFormat = TWO_COLUMN_FORMAT;
                        break;
                    case Xml.TWO_COLUMN_TRACK:
                        getDefault().manifestFormat = TWO_COLUMN_TRACK_FORMAT;
                        break;
                    default:
                        log.debug("Unknown manifest format");
                }
            }
        } else if ((operations.getChild(Xml.COLUMN_FORMAT) != null)) {
            if ((a = operations.getChild(Xml.COLUMN_FORMAT).getAttribute(Xml.TWO_COLUMNS)) != null) {
                String enable = a.getValue();
                log.debug("two columns: {}", enable);
                if (enable.equals(Xml.TRUE)) {
                    setManifestFormat(TWO_COLUMN_FORMAT);
                }
            }
        }
        // get manifest logo
        if ((operations.getChild(Xml.MANIFEST_LOGO) != null)) {
            if ((a = operations.getChild(Xml.MANIFEST_LOGO).getAttribute(Xml.NAME)) != null) {
                setManifestLogoURL(a.getValue());
            }
        }
        // manifest file options
        if ((operations.getChild(Xml.MANIFEST_FILE_OPTIONS) != null)) {
            if ((a = operations.getChild(Xml.MANIFEST_FILE_OPTIONS).getAttribute(Xml.MANIFEST_SAVE)) != null) {
                String enable = a.getValue();
                log.debug("manifest file save option: {}", enable);
                getDefault().saveTrainManifests = enable.equals(Xml.TRUE);
            }
        }
        if ((operations.getChild(Xml.BUILD_OPTIONS) != null)) {
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.AGGRESSIVE)) != null) {
                String enable = a.getValue();
                log.debug("aggressive: {}", enable);
                setBuildAggressive(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.NUMBER_PASSES)) != null) {
                String number = a.getValue();
                log.debug("number of passes: {}", number);
                try {
                    setNumberPasses(Integer.parseInt(number));
                } catch (NumberFormatException ne) {
                    log.debug("Number of passes isn't a number");
                }
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_INTERCHANGE)) != null) {
                String enable = a.getValue();
                log.debug("noLocalInterchange: {}", enable);
                setLocalInterchangeMovesEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_SPUR)) != null) {
                String enable = a.getValue();
                log.debug("noLocalSpur: {}", enable);
                setLocalSpurMovesEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_LOCAL_YARD)) != null) {
                String enable = a.getValue();
                log.debug("noLocalYard: {}", enable);
                setLocalYardMovesEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.STAGING_RESTRICTION_ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("stagingRestrictionEnabled: {}", enable);
                setTrainIntoStagingCheckEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.STAGING_TRACK_AVAIL)) != null) {
                String enable = a.getValue();
                log.debug("stagingTrackAvail: {}", enable);
                setStagingTrackImmediatelyAvail(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.ALLOW_RETURN_STAGING)) != null) {
                String enable = a.getValue();
                log.debug("allowReturnStaging: {}", enable);
                getDefault().allowCarsReturnStaging = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.PROMPT_STAGING_ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("promptStagingEnabled: {}", enable);
                setPromptFromStagingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.PROMPT_TO_STAGING_ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("promptToStagingEnabled: {}", enable);
                setPromptToStagingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.GENERATE_CSV_MANIFEST)) != null) {
                String enable = a.getValue();
                log.debug("generateCvsManifest: {}", enable);
                getDefault().generateCsvManifest = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.BUILD_OPTIONS).getAttribute(Xml.GENERATE_CSV_SWITCH_LIST)) != null) {
                String enable = a.getValue();
                log.debug("generateCvsSwitchList: {}", enable);
                getDefault().generateCsvSwitchList = enable.equals(Xml.TRUE);
            }
        }
        if (operations.getChild(Xml.BUILD_REPORT) != null) {
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.LEVEL)) != null) {
                String level = a.getValue();
                log.debug("buildReportLevel: {}", level);
                setBuildReportLevel(level);
            }
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.ROUTER_LEVEL)) != null) {
                String level = a.getValue();
                log.debug("routerBuildReportLevel: {}", level);
                setRouterBuildReportLevel(level);
            }
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.USE_EDITOR)) != null) {
                String enable = a.getValue();
                log.debug("build report useEditor: {}", enable);
                setBuildReportEditorEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.INDENT)) != null) {
                String enable = a.getValue();
                log.debug("build report indent: {}", enable);
                setBuildReportIndentEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.FONT_SIZE)) != null) {
                String size = a.getValue();
                log.debug("build font size: {}", size);
                try {
                    setBuildReportFontSize(Integer.parseInt(size));
                } catch (NumberFormatException ee) {
                    log.error("Build report font size ({}) isn't a valid number", a.getValue());
                }
            }
            if ((a = operations.getChild(Xml.BUILD_REPORT).getAttribute(Xml.ALWAYS_PREVIEW)) != null) {
                String enable = a.getValue();
                log.debug("build report always preview: {}", enable);
                setBuildReportAlwaysPreviewEnabled(enable.equals(Xml.TRUE));
            }
        }

        if (operations.getChild(Xml.ROUTER) != null) {
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingEnabled: {}", enable);
                setCarRoutingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_VIA_YARDS)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingViaYards: {}", enable);
                setCarRoutingViaYardsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CAR_ROUTING_VIA_STAGING)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingViaStaging: {}", enable);
                setCarRoutingViaStagingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.FORWARD_TO_YARD)) != null) {
                String enable = a.getValue();
                log.debug("forwardToYard: {}", enable);
                setForwardToYardEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.ONLY_ACTIVE_TRAINS)) != null) {
                String enable = a.getValue();
                log.debug("onlyActiveTrains: {}", enable);
                setOnlyActiveTrainsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.ROUTER).getAttribute(Xml.CHECK_CAR_DESTINATION)) != null) {
                String enable = a.getValue();
                log.debug("checkCarDestination: {}", enable);
                setCheckCarDestinationEnabled(enable.equals(Xml.TRUE));
            }
        } else if (operations.getChild(Xml.SETTINGS) != null) {
            // the next four items are for backwards compatibility
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_ENABLED)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingEnabled: {}", enable);
                setCarRoutingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_VIA_YARDS)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingViaYards: {}", enable);
                setCarRoutingViaYardsEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_ROUTING_VIA_STAGING)) != null) {
                String enable = a.getValue();
                log.debug("carRoutingViaStaging: {}", enable);
                setCarRoutingViaStagingEnabled(enable.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.FORWARD_TO_YARD)) != null) {
                String enable = a.getValue();
                log.debug("forwardToYard: {}", enable);
                setForwardToYardEnabled(enable.equals(Xml.TRUE));
            }
        }

        if ((operations.getChild(Xml.OWNER) != null)
                && (a = operations.getChild(Xml.OWNER).getAttribute(Xml.NAME)) != null) {
            String owner = a.getValue();
            log.debug("owner: {}", owner);
            setOwnerName(owner);
        }
        if (operations.getChild(Xml.ICON_COLOR) != null) {
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.NORTH)) != null) {
                String color = a.getValue();
                log.debug("north color: {}", color);
                setTrainIconColorNorth(color);
            }
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.SOUTH)) != null) {
                String color = a.getValue();
                log.debug("south color: {}", color);
                setTrainIconColorSouth(color);
            }
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.EAST)) != null) {
                String color = a.getValue();
                log.debug("east color: {}", color);
                setTrainIconColorEast(color);
            }
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.WEST)) != null) {
                String color = a.getValue();
                log.debug("west color: {}", color);
                setTrainIconColorWest(color);
            }
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.LOCAL)) != null) {
                String color = a.getValue();
                log.debug("local color: {}", color);
                setTrainIconColorLocal(color);
            }
            if ((a = operations.getChild(Xml.ICON_COLOR).getAttribute(Xml.TERMINATE)) != null) {
                String color = a.getValue();
                log.debug("terminate color: {}", color);
                setTrainIconColorTerminate(color);
            }
        }
        if (operations.getChild(Xml.COMMENTS) != null) {
            if ((a = operations.getChild(Xml.COMMENTS).getAttribute(Xml.MISPLACED_CARS)) != null) {
                String comment = a.getValue();
                log.debug("Misplaced comment: {}", comment);
                setMiaComment(comment);
            }
        }

        if (operations.getChild(Xml.DISPLAY) != null) {
            if ((a = operations.getChild(Xml.DISPLAY).getAttribute(Xml.SHOW_TRACK_MOVES)) != null) {
                String enable = a.getValue();
                log.debug("show track moves: {}", enable);
                getDefault().showTrackMoves = enable.equals(Xml.TRUE);
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
                log.debug("autoSave: {}", enabled);
                setAutoSaveEnabled(enabled.equals(Xml.TRUE));
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.AUTO_BACKUP)) != null) {
                String enabled = a.getValue();
                log.debug("autoBackup: {}", enabled);
                setAutoBackupEnabled(enabled.equals(Xml.TRUE));
            }
        }

        if (operations.getChild(Xml.LOGGER) != null) {
            if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.CAR_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("carLogger: {}", enable);
                getDefault().carLogger = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.ENGINE_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("engineLogger: {}", enable);
                getDefault().engineLogger = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.LOGGER).getAttribute(Xml.TRAIN_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("trainLogger: {}", enable);
                getDefault().trainLogger = enable.equals(Xml.TRUE);
            }
        } else if (operations.getChild(Xml.SETTINGS) != null) {
            // for backward compatibility
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.CAR_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("carLogger: {}", enable);
                getDefault().carLogger = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.ENGINE_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("engineLogger: {}", enable);
                getDefault().engineLogger = enable.equals(Xml.TRUE);
            }
            if ((a = operations.getChild(Xml.SETTINGS).getAttribute(Xml.TRAIN_LOGGER)) != null) {
                String enable = a.getValue();
                log.debug("trainLogger: {}", enable);
                getDefault().trainLogger = enable.equals(Xml.TRUE);
            }
        }
    }

    // replace old pickup and drop message keys
    // Change happened from 2.11.3 to 2.11.4
    // 4/16/2014
    private static void replaceOldFormat(String[] format) {
        for (int i = 0; i < format.length; i++) {
            if (format[i].equals("Pickup Msg")) // NOI18N
            {
                format[i] = PICKUP_COMMENT;
            } else if (format[i].equals("Drop Msg")) // NOI18N
            {
                format[i] = DROP_COMMENT;
            }
        }
    }

    /**
     * Converts the xml key to the proper locale text
     *
     */
    private static void keyToStringConversion(String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].equals(BLANK)) {
                continue;
            }
            try {
                keys[i] = Bundle.getMessage(keys[i]);
            } catch (Exception e) {
                log.debug("Key {}: ({}) not found", i, keys[i]);
            }
        }
    }

    /*
     * Converts the strings into English tags for xml storage
     *
     */
    private static void stringToTagConversion(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            String old = strings[i];
            if (old.equals(BLANK)) {
                continue;
            }
            for (String key : KEYS) {
                if (strings[i].equals(Bundle.getMessage(key))) {
                    strings[i] = Bundle.getMessage(Locale.ROOT, key);
                    break;
                }
            }
            // log.debug("Converted {} to {}", old, strings[i]);
        }
    }

    /*
     * The xml attributes stored using the English translation. This converts
     * the attribute to the appropriate key for language conversion.
     */
    private static void xmlAttributeToKeyConversion(String[] format) {
        for (int i = 0; i < format.length; i++) {
            for (String key : KEYS) {
                if (format[i].equals(Bundle.getMessage(Locale.ROOT, key))) {
                    format[i] = key;
                }
            }
        }
    }

    static java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(Setup.class);

    public static synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected static void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(OperationsSetupXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    public static Setup getDefault() {
        return InstanceManager.getDefault(Setup.class);
    }

    private static final Logger log = LoggerFactory.getLogger(Setup.class);

    @Override
    public void dispose() {
        new AutoSave().stop();
    }

}
