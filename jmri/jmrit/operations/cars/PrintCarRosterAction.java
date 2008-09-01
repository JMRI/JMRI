// PrintCarRosterAction.java

package jmri.jmrit.operations.cars;

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
public class PrintCarRosterAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();

    public PrintCarRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (CarsTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    CarsTableFrame panel;
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "Car Roster", 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
 
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        String location;
        String type;		
        String color;
        List cars = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Type") + "\t"
					+ rb.getString("Length") + "\t" + rb.getString("Weight") +" "
					+ rb.getString("Color") + "\t" + rb.getString("Owner") +" "
					+ rb.getString("Built") + "\t" + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getCarById((String)cars.get(i));
        		location = "";
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName() + " - " + car.getSecondaryLocationName();
        		}
         		type = car.getType();
        		switch (type.length()){
        		case 0: type = type+" ";
        		case 1: type = type+" ";
        		case 2: type = type+" ";
        		case 3: type = type+" ";
        		case 4: type = type+" ";
        		case 5: type = type+" ";
        		case 6: type = type+" ";
        		case 7: type = type+" ";
        		case 8: type = type+" ";
        		case 9: type = type+" ";
        		case 10: type = type+" ";
        		case 11: type = type+" ";
        		}
        		color = car.getColor();
        		switch (color.length()){
        		case 0: color = color+" ";
        		case 1: color = color+" ";
        		case 2: color = color+" ";
        		case 3: color = color+" ";
        		case 4: color = color+" ";
        		case 5: color = color+" ";
        		case 6: color = color+" ";
        		case 7: color = color+" ";
        		case 8: color = color+" ";
        		case 9: color = color+" ";
        		case 10: color = color+" ";
        		case 11: color = color+" ";
        		}
        		
				s = car.getNumber() + "\t" + car.getRoad() + "\t"
						+ type + " " + car.getLength() + " "
						+ car.getWeight() + " " + color + " "
						+ car.getOwner() + " " + car.getBuilt() + " "
						+ location + newLine;
        		writer.write(s, 0, s.length());
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintCarRosterAction.class.getName());
}
