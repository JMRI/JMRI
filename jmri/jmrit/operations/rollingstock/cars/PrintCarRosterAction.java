// PrintCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;

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
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
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
        String weight;
        String color;
        String built;
        List cars = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Type") + "\t"
					+ rb.getString("Length") + " " + rb.getString("Weight") +" "
					+ rb.getString("Color") + "    " + rb.getString("Owner") +" "
					+ rb.getString("Built") + "\t" + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getCarById((String)cars.get(i));
        		location = "";
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName() + " - " + car.getTrackName();
        		}
         		type = car.getType();
        		switch (type.length()){
        		case 0: type += " ";
        		case 1: type += " ";
        		case 2: type += " ";
        		case 3: type += " ";
        		case 4: type += " ";
        		case 5: type += " ";
        		case 6: type += " ";
        		case 7: type += " ";
        		case 8: type += " ";
        		case 9: type += " ";
        		case 10: type += " ";
        		case 11: type += " ";
        		}
        		color = car.getColor();
        		switch (color.length()){
        		case 0: color += " ";
        		case 1: color += " ";
        		case 2: color += " ";
        		case 3: color += " ";
        		case 4: color += " ";
        		case 5: color += " ";
        		case 6: color += " ";
        		case 7: color += " ";
        		case 8: color += " ";
        		case 9: color += " ";
        		case 10: color += " ";
        		case 11: color += " ";
        		}
        		
        		weight = car.getWeight();
           		switch (weight.length()){
        		case 0: weight += " ";
        		case 1: weight += " ";
        		case 2: weight += " ";
        		case 3: weight += " ";
           		}
           		
           		built = car.getBuilt();
           		switch (built.length()){
        		case 0: built += " ";
        		case 1: built += " ";
        		case 2: built += " ";
        		case 3: built += " ";
           		}
        		
				s = car.getNumber() + "\t" + car.getRoad() + "\t"
						+ type + " " + car.getLength() + " "
						+ weight + " " + color + " "
						+ car.getOwner() + " " + built + " "
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
