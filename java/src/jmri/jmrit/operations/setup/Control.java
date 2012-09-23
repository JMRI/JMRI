package jmri.jmrit.operations.setup;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Controls for operations developers. Debug Property changes and instance
 * creation, maximum panel width, etc.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * 
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_CANNOT_BE_FINAL")
public class Control {
	
	// debug flags
	public static final boolean showProperty = false;
	public static final boolean showInstance = false;
	
	// Default panel width
	public static final int panelWidth = 1025;
	
	// Default panel height
	public static final int panelHeight = 500;
	
	/*
	static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();	
	// Maximum panel height
	public static final int panelMaxHeight = screenSize.height;
	*/
	
	// Default panel edit locations
	public static final int panelX = 0;
	public static final int panelY = 0;
	
	
	// Train build parameters
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_SHOULD_BE_FINAL") // allow access for testing
	public static boolean fullTrainOnly = false;
	
	// Car and Engine attribute maximum string length	
	public static int max_len_string_attibute = 12;

	// Car and Engine number maximum string length	
	public static int max_len_string_road_number = 10; 

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
	public static int max_len_string_built_name = 4;
	
	// Train name maximum string length
	public static int max_len_string_train_name = 25;
	
	// Route name maximum string length
	public static int max_len_string_route_name = 25;
	
	// Backward compatibility for xml saves (pre 2013 releases)
	// TODO turn backward compatibility to false in 2013
	public static boolean backwardCompatible = true;
	
    // must synchronize changes with operation-config.dtd
    public static Element store(){
    	Element values;
    	Element length;
    	Element e = new Element("control");
    	// backward compatibility
    	e.addContent(values = new Element("backwardCompatibility"));
    	// TODO Enable saving the compatibility attribute in 2013
    	//values.setAttribute("saveUsingPre_2013_Format", backwardCompatible?"true":"false");
    	// maximum string lengths
    	e.addContent(values = new Element("maximumStringLengths"));
    	values.addContent(length = new Element("max_len_string_attibute"));
    	length.setAttribute("length", Integer.toString(max_len_string_attibute));  	
    	values.addContent(length = new Element("max_len_string_road_number"));
    	length.setAttribute("length", Integer.toString(max_len_string_road_number));    	
    	values.addContent(length = new Element("max_len_string_location_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_location_name));    	
    	values.addContent(length = new Element("max_len_string_track_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_track_name));    	
    	values.addContent(length = new Element("max_len_string_track_length_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_track_length_name));
    	values.addContent(length = new Element("max_len_string_length_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_length_name));
    	values.addContent(length = new Element("max_len_string_weight_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_weight_name));
    	values.addContent(length = new Element("max_len_string_built_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_built_name));
    	values.addContent(length = new Element("max_len_string_train_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_train_name));
    	values.addContent(length = new Element("max_len_string_route_name"));
    	length.setAttribute("length", Integer.toString(max_len_string_route_name));
    	return e;
    }
    
    public static void load(Element e) {
    	Element control = e.getChild("control");
        if (control == null)
        	return;
        Element backwardCompatibility = control.getChild("backwardCompatibility");
        if (backwardCompatibility != null){
        	Attribute format;
            if ((format = backwardCompatibility.getAttribute("saveUsingPre_2013_Format")) != null){
            	backwardCompatible = format.getValue().equals("true");
            }
        }
        Element maximumStringLengths = control.getChild("maximumStringLengths");
        if (maximumStringLengths != null){
            Attribute length;
            if ((maximumStringLengths.getChild("max_len_string_attibute") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_attibute").getAttribute("length"))!= null){
            	max_len_string_attibute = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_road_number") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_road_number").getAttribute("length"))!= null){
            	max_len_string_road_number = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_location_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_location_name").getAttribute("length"))!= null){
            	max_len_string_location_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_track_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_track_name").getAttribute("length"))!= null){
            	max_len_string_track_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_track_length_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_track_length_name").getAttribute("length"))!= null){
            	max_len_string_track_length_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_length_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_length_name").getAttribute("length"))!= null){
            	max_len_string_length_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_weight_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_weight_name").getAttribute("length"))!= null){
            	max_len_string_weight_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_built_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_built_name").getAttribute("length"))!= null){
            	max_len_string_built_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_train_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_train_name").getAttribute("length"))!= null){
            	max_len_string_train_name = Integer.parseInt(length.getValue());
            }
            if ((maximumStringLengths.getChild("max_len_string_route_name") != null) && 
            		(length = maximumStringLengths.getChild("max_len_string_route_name").getAttribute("length"))!= null){
            	max_len_string_route_name = Integer.parseInt(length.getValue());
            }
        }
    }
}

