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
 * @version     $Revision: 1.7 $
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

        // Print all cars?
        boolean printAll = true;
        
        int selectedValue = JOptionPane.showOptionDialog(mFrame, 
        		rb.getString("PrintAllCars"), null,
        		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
        		null, new Object[] { rb.getString("ButtonYes"), rb.getString("ButtonNo")},
        		rb.getString("ButtonYes"));

        if (selectedValue == 1)
        	printAll = false;
 
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
        String road;
        String type;
        String length;
        String weight;
        String color;
        String owner;
        String built;
        
        List<String> cars = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
					+ "\t" + rb.getString("Type") + "\t "
					+ rb.getString("Length") + " " + rb.getString("Weight") +" "
					+ rb.getString("Color") + "    " + rb.getString("Owner") +" "
					+ rb.getString("Built") + "\t" + rb.getString("Location")
					+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getCarById(cars.get(i));
        		// skip cars without a location?
        		if (car.getLocationName().equals("") && !printAll)
        			continue;
        		location = "";
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
        			if (location.length() > 29)
        				location = location.substring(0, 29);
        		}
        		// pad out the fields
         		number = car.getNumber().trim();
         		for (int j=number.length(); j<7; j++)
         			number += " ";
         		
         		road = car.getRoad().trim();
        		if (road.length() > 7)
        			road = road.substring(0, 7);
         		for (int j=road.length(); j<7; j++)
         			road += " ";
         		
         		type = car.getType().trim();
           		if (type.length() > 11)
        			type = type.substring(0, 11);
         		for (int j=type.length(); j<11; j++)
         			type += " ";
         		
        		length = car.getLength().trim();
         		for (int j=length.length(); j<3; j++)
         			length += " ";
 
        		weight = car.getWeight().trim();
          		if (weight.length() > 4)
          			weight = weight.substring(0, 4);
        		for (int j=weight.length(); j<4; j++)
        			weight += " ";
        		
           		color = car.getColor().trim();
          		if (color.length() > 11)
        			color = color.substring(0, 11);
         		for (int j=color.length(); j<11; j++)
         			color += " ";
         		
          		owner = car.getOwner().trim();
          		if (owner.length() > 4)
          			owner = owner.substring(0, 4);
         		for (int j=owner.length(); j<4; j++)
         			owner += " ";
         		
         		built = car.getBuilt().trim();
          		if (built.length() > 4)
          			built = built.substring(0, 4);
         		for (int j=built.length(); j<4; j++)
         			built += " ";
         		         		
 				s = number + " " + road + " " + type + " " + length + " "
						+ weight + " " + color + " " + owner + " " + built
						+ " " + location + newLine;
        		writer.write(s, 0, s.length());
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintCarRosterAction.class.getName());
}
