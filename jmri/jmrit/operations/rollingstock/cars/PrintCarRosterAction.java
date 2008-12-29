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
 * @version     $Revision: 1.4 $
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
            writer = new HardcopyWriter(mFrame, rb.getString("TitleCarRoster"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
 
        // Loop through the Roster, printing as needed
        String newLine = "\n";
        String location;
        String number;
        String type;
        String weight;
        String color;
        List<String> cars = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Type") + "\t"
					+ rb.getString("Length") + " " + rb.getString("Weight") +" "
					+ rb.getString("Color") + "    " + rb.getString("Owner") +" "
					+ rb.getString("Built") + "\t" + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getCarById(cars.get(i));
        		location = "";
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName() + " - " + car.getTrackName();
        		}
        		// pad out the fields
         		number = car.getNumber();
         		for (int j=number.length(); j<7; j++)
         			number += " ";
         		
         		type = car.getType();
         		for (int j=type.length(); j<11; j++)
         			type += " ";
 
        		color = car.getColor();
         		for (int j=color.length(); j<11; j++)
         			color += " ";
         		
        		weight = car.getWeight();
        		for (int j=weight.length(); j<4; j++)
        			weight += " ";
           		
 				s = number + " " + car.getRoad() + "\t"
						+ type + " " + car.getLength() + " "
						+ weight + " " + color + " " 
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
