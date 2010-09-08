// PrintEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.setup.Control;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008
 * @version     $Revision: 1.7 $
 */
public class PrintEngineRosterAction  extends AbstractAction {
	
	final int ownerMaxLen = 4;	// Only show the first 4 characters of the owner's name
	final int locMaxLen = 33;	// limit the number of characters for location and track
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();

    public PrintEngineRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (EnginesTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    EnginesTableFrame panel;
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, rb.getString("TitleEngineRoster"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        String location;
        String road;
        String model;
        String type;
        String length;
        String owner;
        String built;
 
        List<String> engines = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Model") + "\t     "
					+ rb.getString("Type") + "      " + rb.getString("Length")
					+ " " + rb.getString("Owner") + " "
					+ rb.getString("Built") + " " + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<engines.size(); i++){
        		Engine engine = manager.getById(engines.get(i));
        		location = "";
        		if (!engine.getLocationName().equals("")){
        			location = engine.getLocationName() + " - " + engine.getTrackName();
           			if (location.length() > locMaxLen)
        				location = location.substring(0, locMaxLen);
        		}
         		
           		road = engine.getRoad().trim();
        		if (road.length() > 7)
        			road = road.substring(0, 7);
        		StringBuffer buf = new StringBuffer(road);
        		for (int j=road.length(); j<7; j++)
        			buf.append(" ");
        		road = buf.toString();
         		
         		model = engine.getModel();
         		if (model.length() > Control.MAX_LEN_STRING_ATTRIBUTE)
         			model = model.substring(0, Control.MAX_LEN_STRING_ATTRIBUTE);
         		buf = new StringBuffer(model);
         		for (int j=model.length(); j<Control.MAX_LEN_STRING_ATTRIBUTE+1; j++)		
         			buf.append(" ");
         		model = buf.toString();
         		
        		type = engine.getType().trim();
        		if (type.length() > Control.MAX_LEN_STRING_ATTRIBUTE)
        			type = type.substring(0, Control.MAX_LEN_STRING_ATTRIBUTE);
           		buf = new StringBuffer(type);
        		for (int j=type.length(); j<Control.MAX_LEN_STRING_ATTRIBUTE+1; j++)
        			buf.append(" ");
        		type = buf.toString();
         		
      			length = engine.getLength().trim();
    			buf = new StringBuffer(length);
    			for (int j=length.length(); j<Control.MAX_LEN_STRING_LENGTH_NAME+1; j++)
    				buf.append(" ");
    			length = buf.toString();
 		
       			owner = engine.getOwner().trim();
    			if (owner.length() > ownerMaxLen)
    				owner = owner.substring(0, ownerMaxLen);
    			buf = new StringBuffer(owner);
    			for (int j=owner.length(); j<ownerMaxLen+1; j++)
       				buf.append(" ");
    			owner = buf.toString();
         		
       			built = engine.getBuilt().trim();
    			if (built.length() > 4)
    				built = built.substring(0, 4);
    			buf = new StringBuffer(built);
    			for (int j=built.length(); j<Control.MAX_LEN_STRING_BUILT_NAME+1; j++)
     				buf.append(" ");
    			built = buf.toString();
         		
				s = engine.getNumber() + "\t" + road + " " + model + type
						+ length + owner + built + location + newLine;			
        		writer.write(s, 0, s.length());
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintEngineRosterAction.class.getName());
}
