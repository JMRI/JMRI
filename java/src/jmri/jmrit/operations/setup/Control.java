package jmri.jmrit.operations.setup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls for operations developers. Debug Property changes and instance
 * creation, maximum panel width, etc.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 *
 */
@SuppressFBWarnings(value = "MS_CANNOT_BE_FINAL")
public class Control {

    // debug flags
    public static final boolean SHOW_PROPERTY = false;

    // Default panel width
    public static final int panelWidth1025 = 1025;
    public static final int panelWidth700 = 700;
    public static final int panelWidth600 = 600;
    public static final int panelWidth500 = 500;
    public static final int panelWidth400 = 400;
    public static final int panelWidth300 = 300;

    // Default panel height
    public static final int panelHeight600 = 600;
    public static final int panelHeight500 = 500;
    public static final int panelHeight400 = 400;
    public static final int panelHeight300 = 300;
    public static final int panelHeight250 = 250;
    public static final int panelHeight200 = 200;
    public static final int panelHeight100 = 100;

    /*
     static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
     // Maximum panel height
     public static final int panelMaxHeight = screenSize.height;
     */
    // Default panel edit locations
    public static final int panelX = 0;
    public static final int panelY = 0;

    // Train build parameters
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // allow access for testing
    public static boolean fullTrainOnly = false;

    // Car and Engine attribute maximum string length 
    public static int max_len_string_attibute = 12;

    // Car and Engine number maximum string length 
    public static int max_len_string_road_number = 10;
    
    // Car and Engine number maximum string length when printing  
    public static int max_len_string_print_road_number = 6;

    // Location name maximum string length
    public static int max_len_string_location_name = 25;

    // Track name maximum string length
    public static int max_len_string_track_name = 25;

    // Track length maximum string length
    public static int max_len_string_track_length_name = 5;

    // Car and Engine length maximum string length
    public static int max_len_string_length_name = 4;

    // Car weight maximum string length
    public static int max_len_string_weight_name = 4;

    // Car and Engine built date maximum string length
    public static int max_len_string_built_name = 5;

    // Train name maximum string length
    public static int max_len_string_train_name = 25;

    // Route name maximum string length
    public static int max_len_string_route_name = 25;
    
    // Automation name maximum string length
    public static int max_len_string_automation_name = 25;

    // Backward compatibility for xml saves (pre 2013 releases)
    // backward compatibility to false in 2014
    public static boolean backwardCompatible = false;
    
    public static int reportFontSize = 10;
    public static String reportFontName = ""; // use default
    
    public static int excelWaitTime = 120; // in seconds

    // must synchronize changes with operation-config.dtd
    public static Element store() {
        Element values;
        Element length;
        Element e = new Element(Xml.CONTROL);
        // backward compatibility default set to false as of 3.7.1 (early 2014)
        e.addContent(values = new Element(Xml.BACKWARD_COMPATIBILITY));
        values.setAttribute(Xml.SAVE_USING_PRE_2013_FORMAT, backwardCompatible ? Xml.TRUE : Xml.FALSE);
        // maximum string lengths
        e.addContent(values = new Element(Xml.MAXIMUM_STRING_LENGTHS));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_ATTRIBUTE));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_attibute));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_ROAD_NUMBER));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_road_number));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_PRINT_ROAD_NUMBER));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_print_road_number));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_LOCATION_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_location_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_TRACK_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_track_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_TRACK_LENGTH_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_track_length_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_LENGTH_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_length_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_WEIGHT_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_weight_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_BUILT_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_built_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_TRAIN_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_train_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_ROUTE_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_route_name));
        values.addContent(length = new Element(Xml.MAX_LEN_STRING_AUTOMATION_NAME));
        length.setAttribute(Xml.LENGTH, Integer.toString(max_len_string_automation_name));
        // reports
        e.addContent(values = new Element(Xml.REPORTS));
        values.setAttribute(Xml.FONT_SIZE, Integer.toString(reportFontSize));
        values.setAttribute(Xml.FONT_NAME, reportFontName);
        // actions
        e.addContent(values = new Element(Xml.ACTIONS));
        values.setAttribute(Xml.EXCEL_WAIT_TIME, Integer.toString(excelWaitTime));
        
        return e;
    }

    public static void load(Element e) {
        Element eControl = e.getChild(Xml.CONTROL);
        if (eControl == null) {
            return;
        }
        Element backwardCompatibility = eControl.getChild(Xml.BACKWARD_COMPATIBILITY);
        if (backwardCompatibility != null) {
            Attribute format;
            if ((format = backwardCompatibility.getAttribute(Xml.SAVE_USING_PRE_2013_FORMAT)) != null) {
                backwardCompatible = format.getValue().equals(Xml.TRUE);
            }
        }
        Element maximumStringLengths = eControl.getChild(Xml.MAXIMUM_STRING_LENGTHS);
        if (maximumStringLengths != null) {
            Attribute length;
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ATTRIBUTE) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ATTRIBUTE).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_attibute = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ROAD_NUMBER) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ROAD_NUMBER).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_road_number = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_PRINT_ROAD_NUMBER) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_PRINT_ROAD_NUMBER).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_print_road_number = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_LOCATION_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_LOCATION_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_location_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRACK_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRACK_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_track_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRACK_LENGTH_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRACK_LENGTH_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_track_length_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_LENGTH_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_LENGTH_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_length_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_WEIGHT_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_WEIGHT_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_weight_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_BUILT_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_BUILT_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_built_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRAIN_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_TRAIN_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_train_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ROUTE_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_ROUTE_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_route_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild(Xml.MAX_LEN_STRING_AUTOMATION_NAME) != null)
                    && (length = maximumStringLengths.getChild(Xml.MAX_LEN_STRING_AUTOMATION_NAME).getAttribute(Xml.LENGTH)) != null) {
                max_len_string_automation_name = Integer.parseInt(length.getValue());
            }
        }
        Element eReports = eControl.getChild(Xml.REPORTS);
        if (eReports != null) {
            Attribute a;
            if ((a = eReports.getAttribute(Xml.FONT_SIZE)) != null) {
                try {
                    reportFontSize = a.getIntValue();
                } catch (DataConversionException e1) {
                    log.error("Report font size ({}) isn't a number", a.getValue());
                }
            }
            if ((a = eReports.getAttribute(Xml.FONT_NAME)) != null) {
                reportFontName = a.getValue();
            }
        }   
        Element eActions = eControl.getChild(Xml.ACTIONS);
        if (eActions != null) {
            Attribute a;
            if ((a = eActions.getAttribute(Xml.EXCEL_WAIT_TIME)) != null) {
                try {
                    excelWaitTime = a.getIntValue();
                } catch (DataConversionException e1) {
                    log.error("Excel wait time ({}) isn't a number", a.getValue());
                }
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(Control.class);
}
