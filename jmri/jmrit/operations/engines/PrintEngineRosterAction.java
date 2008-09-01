// PrintEngineRosterAction.java

package jmri.jmrit.operations.engines;

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
 * @version     $Revision: 1.1 $
 */
public class PrintEngineRosterAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.engines.JmritOperationsEnginesBundle");
	
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
            writer = new HardcopyWriter(mFrame, "Engine Roster", 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
 
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        String location;
        String model;		
        List engines = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Model") + "\t"
					+ rb.getString("Length") + "\t" + rb.getString("Owner")
					+ "\t" + rb.getString("Built") + "\t"
					+ rb.getString("Location") + newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<engines.size(); i++){
        		Engine engine = manager.getEngineById((String)engines.get(i));
        		location = "";
        		if (!engine.getLocationName().equals("")){
        			location = engine.getLocationName() + " - " + engine.getSecondaryLocationName();
        		}
         		model = engine.getModel();
        		switch (model.length()){
        		case 0: model = model+" ";
        		case 1: model = model+" ";
        		case 2: model = model+" ";
        		case 3: model = model+" ";
        		case 4: model = model+" ";
        		case 5: model = model+" ";
        		case 6: model = model+" ";
        		case 7: model = model+" ";
        		case 8: model = model+" ";
        		case 9: model = model+" ";
        		case 10: model = model+" ";
        		case 11: model = model+" ";
        		}
         		
				s = engine.getNumber() + "\t" + engine.getRoad() + "\t"
						+ model + " " + engine.getLength() + "\t "
						+ engine.getOwner() + "\t" + engine.getBuilt() + "\t"
						+ location + newLine;
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
