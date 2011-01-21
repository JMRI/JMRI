// PrintCarLoadsAction.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainCommon;


/**
 * Action to print a summary of car loads ordered by car type.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2011
 * @version     $Revision: 1.1 $
 */
public class PrintCarLoadsAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();

    public PrintCarLoadsAction(String actionName, boolean preview, Component pWho) {
        super(actionName);
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    CarPrintOptionFrame cpof = null;

    public void actionPerformed(ActionEvent e) {
    	if (cpof == null)
    		cpof = new CarPrintOptionFrame();
    	else
    		cpof.setVisible(true);
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
    	
    	// no frame needed for now
    	public CarPrintOptionFrame(){
    		super();
    		printCars();
    	}
    	
        private void printCars(){
        	 
            // obtain a HardcopyWriter to do this
            HardcopyWriter writer = null;
            Frame mFrame = new Frame();
            try {
                writer = new HardcopyWriter(mFrame, rb.getString("TitleCarLoads"), 10, .5, .5, .5, .5, isPreview);
            } catch (HardcopyWriter.PrintCanceledException ex) {
                log.debug("Print cancelled");
                return;
            }
            
            // Loop through the Roster, printing as needed
            String newLine = "\n";
            CarLoads carLoads = CarLoads.instance();
            Hashtable<String, List<CarLoad>> list = carLoads.getList();
            try {
            	String s = rb.getString("Type") + "\t"
            	+ rb.getString("Load") + "\t" 
            	+ rb.getString("BorderLayoutPriority") + "  "
            	+ rb.getString("BorderLayoutOptionalPickup") + "  "
            	+ rb.getString("BorderLayoutOptionalDrop")           	
            	+ newLine;
            	writer.write(s);
        		Enumeration<String> en = list.keys();
        		while(en.hasMoreElements()) {
        			String key = en.nextElement();
        			writer.write(key + newLine);
        			List<CarLoad> loads = list.get(key);
     
        			for (int j=0; j<loads.size(); j++){
        				StringBuffer buf = new StringBuffer("\t");
        				String load = loads.get(j).getName();
        				// don't print out default load or empty
        				if (load.equals(carLoads.getDefaultEmptyName()) 
        						|| load.equals(carLoads.getDefaultLoadName()))
        						continue;
        				buf.append(TrainCommon.tabString(load, Control.MAX_LEN_STRING_ATTRIBUTE) + " ");
        				buf.append(TrainCommon.tabString(loads.get(j).getPriority(), 5));
        				buf.append(TrainCommon.tabString(loads.get(j).getPickupComment(), 33));
        				buf.append(loads.get(j).getDropComment());
        				writer.write(buf.toString() + newLine);
        			}
            	}
            	// and force completion of the printing
            	writer.close();
            } catch (IOException we) {
            	log.error("Error printing car roster");
            }
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintCarLoadsAction.class.getName());
}
