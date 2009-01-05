// PrintEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;

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
 * @version     $Revision: 1.4 $
 */
public class PrintEngineRosterAction  extends AbstractAction {
	
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
					+ "\t" + rb.getString("Model") + "\t    "
					+ rb.getString("Type") + "    " + rb.getString("Length")
					+ " " + rb.getString("Owner") + " "
					+ rb.getString("Built") + "\t" + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<engines.size(); i++){
        		Engine engine = manager.getEngineById(engines.get(i));
        		location = "";
        		if (!engine.getLocationName().equals("")){
        			location = engine.getLocationName() + " - " + engine.getTrackName();
           			if (location.length() > 34)
        				location = location.substring(0, 34);
        		}
         		
        		road = engine.getRoad().trim();
        		if (road.length() > 7)
        			road = road.substring(0, 7);
         		for (int j=road.length(); j<7; j++)
         			road += " ";
         		
         		model = engine.getModel();
         		if (model.length() > 11)
         			model = model.substring(0, 11);
         		for (int j=model.length(); j<11; j++)
         			model += " ";
         		
        		type = engine.getType().trim();
           		if (type.length() > 11)
        			type = type.substring(0, 11);
         		for (int j=type.length(); j<11; j++)
         			type += " ";
         		
           		length = engine.getLength().trim();
         		for (int j=length.length(); j<3; j++)
         			length += " ";
 		
         		owner = engine.getOwner().trim();
          		if (owner.length() > 4)
          			owner = owner.substring(0, 4);
         		for (int j=owner.length(); j<4; j++)
         			owner += " ";
         		
           		built = engine.getBuilt().trim();
          		if (built.length() > 4)
          			built = built.substring(0, 4);
         		for (int j=built.length(); j<4; j++)
         			built += " ";
         		
				s = engine.getNumber() + "\t" + road + " " + model + " " + type
						+ " " + length + " " + owner + " "
						+ built + " " + location + newLine;
        		writer.write(s, 0, s.length());
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintEngineRosterAction.class.getName());
}
