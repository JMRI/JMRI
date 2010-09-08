// PrintCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version     $Revision: 1.13 $
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
    CarPrintOptionFrame cpof = null;

    public void actionPerformed(ActionEvent e) {
    	if (cpof == null)
    		cpof = new CarPrintOptionFrame(this);
    	else
    		cpof.setVisible(true);
    }
    
    private void printCars(){
 
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
        String length = "";
        String weight = "";
        String color = "";
        String owner = "";
        String built = "";
        String load = "";
        
        boolean printOnly = printCarsWithLocation.isSelected();
        boolean printLength = printCarLength.isSelected();
        boolean printWeight = printCarWeight.isSelected();
        boolean printColor = printCarColor.isSelected();
        boolean printOwner = printCarOwner.isSelected();
        boolean printBuilt = printCarBuilt.isSelected();
        boolean printLoad = printCarLoad.isSelected();
        
		int locMaxLen = 15;
		int ownerMaxLen = 4;
		// adjust the max length for the location and track field
		if (!printLength)
			locMaxLen = locMaxLen + Control.MAX_LEN_STRING_LENGTH_NAME+1;
		if (!printWeight)
			locMaxLen = locMaxLen + Control.MAX_LEN_STRING_WEIGHT_NAME+1;
		if (!printColor)
			locMaxLen = locMaxLen + Control.MAX_LEN_STRING_ATTRIBUTE;
   		if (!printOwner)
			locMaxLen = locMaxLen +5;
  		if (!printBuilt)
			locMaxLen = locMaxLen + Control.MAX_LEN_STRING_BUILT_NAME+1;
 		if (!printLoad)
			locMaxLen = locMaxLen + Control.MAX_LEN_STRING_ATTRIBUTE;
  		
  		// now adjust the owner name field if there is space available
  		if (locMaxLen > Control.MAX_LEN_STRING_LOCATION_NAME + Control.MAX_LEN_STRING_TRACK_NAME + ownerMaxLen){
  			ownerMaxLen = locMaxLen - (Control.MAX_LEN_STRING_LOCATION_NAME + Control.MAX_LEN_STRING_TRACK_NAME);
  			if (ownerMaxLen > Control.MAX_LEN_STRING_ATTRIBUTE)
  				ownerMaxLen = Control.MAX_LEN_STRING_ATTRIBUTE;
  		}
        
        List<String> cars = panel.getSortByList();
        try {
        	String s = rb.getString("Number") + "\t" + rb.getString("Road")
        	+ "\t" + rb.getString("Type") + "\t  "
        	+ (printLength?rb.getString("Length")+ " ":"  ") 
        	+ (printWeight?rb.getString("Weight")+ " ":" ")
        	+ (printColor?rb.getString("Color")+ "       ":"")
        	+ (printLoad?rb.getString("Load")+ "        ":"")
        	+ (printOwner?rb.getString("Owner")+" ":"")
        	+ (printBuilt?rb.getString("Built"):"")
        	+ " " + rb.getString("Location")
        	+ newLine;
        	writer.write(s, 0, s.length());
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getById(cars.get(i));

        		location = "";
        		
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
        			if (location.length() > locMaxLen)
        				location = location.substring(0, locMaxLen);
        		}else if (printOnly)
        			continue;	// car doesn't have a location skip
        		
        		// car number
        		number = car.getNumber().trim();
        		StringBuffer buf = new StringBuffer(number);
        		for (int j=number.length(); j<7; j++)
        			buf.append(" ");
        		number = buf.toString();
        		
        		// car road
        		road = car.getRoad().trim();
        		if (road.length() > 7)
        			road = road.substring(0, 7);
        		buf = new StringBuffer(road);
        		for (int j=road.length(); j<7; j++)
        			buf.append(" ");
        		road = buf.toString();
        		
        		// car type
        		type = car.getType().trim();
        		if (type.length() > Control.MAX_LEN_STRING_ATTRIBUTE)
        			type = type.substring(0, Control.MAX_LEN_STRING_ATTRIBUTE);
           		buf = new StringBuffer(type);
        		for (int j=type.length(); j<Control.MAX_LEN_STRING_ATTRIBUTE+1; j++)
        			buf.append(" ");
        		type = buf.toString();
 
        		if (printLength){
        			length = car.getLength().trim();
        			buf = new StringBuffer(length);
        			for (int j=length.length(); j<Control.MAX_LEN_STRING_LENGTH_NAME+1; j++)
        				buf.append(" ");
        			length = buf.toString();
        		}

        		if (printWeight){
        			weight = car.getWeight().trim();
        			if (weight.length() > 4)
        				weight = weight.substring(0, 4);
        			buf = new StringBuffer(weight);
        			for (int j=weight.length(); j<Control.MAX_LEN_STRING_WEIGHT_NAME+1; j++)
        				buf.append(" ");
        			weight = buf.toString();
        		}

        		if (printColor){
        			color = car.getColor().trim();
        			if (color.length() > Control.MAX_LEN_STRING_ATTRIBUTE)
        				color = color.substring(0, Control.MAX_LEN_STRING_ATTRIBUTE);
        			buf = new StringBuffer(color);
        			for (int j=color.length(); j<Control.MAX_LEN_STRING_ATTRIBUTE+1; j++)
        				buf.append(" ");
        			color = buf.toString();
        		}
        		
           		if (printLoad){
        			load = car.getLoad().trim();
        			if (load.length() > Control.MAX_LEN_STRING_ATTRIBUTE)
        				load = load.substring(0, Control.MAX_LEN_STRING_ATTRIBUTE);
        			buf = new StringBuffer(load);
        			for (int j=load.length(); j<Control.MAX_LEN_STRING_ATTRIBUTE+1; j++)
           				buf.append(" ");
        			load = buf.toString();
        		}

        		if (printOwner){
        			owner = car.getOwner().trim();
        			if (owner.length() > ownerMaxLen)
        				owner = owner.substring(0, ownerMaxLen);
        			buf = new StringBuffer(owner);
        			for (int j=owner.length(); j<ownerMaxLen+1; j++)
           				buf.append(" ");
        			owner = buf.toString();
        		}

        		if (printBuilt){
        			built = car.getBuilt().trim();
        			if (built.length() > 4)
        				built = built.substring(0, 4);
        			buf = new StringBuffer(built);
        			for (int j=built.length(); j<Control.MAX_LEN_STRING_BUILT_NAME+1; j++)
         				buf.append(" ");
        			built = buf.toString();
        		}

        		s = number + " " + road + " " + type
        		+ length + weight + color + load + owner + built
        		+ location + newLine;
        		writer.write(s, 0, s.length());
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing car roster");
        }
    }
    
    JCheckBox printCarsWithLocation = new JCheckBox(rb.getString("PrintCarsWithLocation"));
    JCheckBox printCarLength = new JCheckBox(rb.getString("PrintCarLength"));
    JCheckBox printCarWeight = new JCheckBox(rb.getString("PrintCarWeight"));
    JCheckBox printCarColor = new JCheckBox(rb.getString("PrintCarColor"));
    JCheckBox printCarOwner = new JCheckBox(rb.getString("PrintCarOwner"));
    JCheckBox printCarBuilt = new JCheckBox(rb.getString("PrintCarBuilt"));
    JCheckBox printCarLoad = new JCheckBox(rb.getString("PrintCarLoad"));
    
    JButton okayButton = new JButton(rb.getString("ButtonOkay"));
    
    public class CarPrintOptionFrame extends OperationsFrame{
    	 PrintCarRosterAction pcr;
    	
    	public CarPrintOptionFrame(PrintCarRosterAction pcr){
    		super();
    		this.pcr = pcr;
    		// create panel
    		JPanel pPanel = new JPanel();
    		pPanel.setLayout(new BoxLayout(pPanel,BoxLayout.Y_AXIS));
    		pPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("PrintOptions")));
    		pPanel.add(printCarsWithLocation);
    		pPanel.add(printCarLength);
    		pPanel.add(printCarWeight);
    		pPanel.add(printCarColor);
    		pPanel.add(printCarLoad);
    		pPanel.add(printCarOwner);
    		pPanel.add(printCarBuilt);
    		
    		// set defaults
    		printCarsWithLocation.setSelected(false);
    		printCarLength.setSelected(true);
    		printCarWeight.setSelected(false);
    		printCarColor.setSelected(true);
    		printCarOwner.setSelected(false);
    		printCarBuilt.setSelected(false);
    		printCarLoad.setSelected(false);
    		
    		JPanel pButtons = new JPanel();  
    		pButtons.setLayout(new GridBagLayout());
    		pButtons.add(okayButton);
    		addButtonAction(okayButton);
    	   		
    		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    		getContentPane().add(pPanel);
    		getContentPane().add(pButtons);
    		pack();
    		setVisible(true);
    	}
    	
    	public void buttonActionPerformed(java.awt.event.ActionEvent ae) { 		
    		setVisible(false);
    		pcr.printCars();  		
    	}
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintCarRosterAction.class.getName());
}
