// PrintTrainAction.java

package jmri.jmrit.operations.trains;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;


/**
 * Action to print a summary of a train
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009
 * @version     $Revision: 1.2 $
 */
public class PrintTrainAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	String newLine = "\n";
	public static final int MAX_NAME_LENGTH = 15;

    public PrintTrainAction(String actionName, Frame frame, boolean preview, Train train) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        this.train = train;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    Train train;
    

    public void actionPerformed(ActionEvent e) {
    	if (train == null)
    		return;

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, MessageFormat.format(rb.getString("TitleTrain"), new Object[] {train.getName()}), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        try {
        	String s = rb.getString("Name") + ": " + train.getName() + newLine;
        	writer.write(s, 0, s.length());
        	s = rb.getString("Description") + ": " + train.getDescription() + newLine;
        	writer.write(s, 0, s.length());
        	s = rb.getString("Departs") + ": " + train.getTrainDepartsName() + newLine;
        	writer.write(s, 0, s.length());
        	s = rb.getString("DepartTime") + ": " + train.getDepartureTime() + newLine;
        	writer.write(s, 0, s.length());
        	s = rb.getString("Terminates") + ": " + train.getTrainTerminatesName() + newLine;
        	writer.write(s, 0, s.length());
        	s = newLine;
        	writer.write(s, 0, s.length());
        	s = rb.getString("Route") + ": " + train.getTrainRouteName() + newLine;
        	writer.write(s, 0, s.length());
        	Route route = train.getRoute();
        	if (route != null){
        		List locations = route.getLocationsBySequenceList();
        		for (int i=0; i<locations.size(); i++){
        			RouteLocation rl = route.getLocationById((String)locations.get(i));   
        			s = "\t" + rl.getName() + newLine;
        			writer.write(s, 0, s.length());		
        		}
        	}
        	
        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing ConsistRosterEntry: " + e);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintTrainAction.class.getName());
}
