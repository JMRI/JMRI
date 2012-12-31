// PrintRouteAction.java

package jmri.jmrit.operations.routes;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.*;

import java.util.List;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;


/**
 * Action to print a summary of a route.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2009, 2012
 * @version     $Revision$
 */
public class PrintRouteAction  extends AbstractAction {
	
	static final String newLine = "\n";
	private static final int MAX_NAME_LENGTH = 20;

    public PrintRouteAction(String actionName, boolean preview, Route route) {
        super(actionName);
        isPreview = preview;
        this.route = route;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame = new Frame();
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    Route route;
    

    public void actionPerformed(ActionEvent e) {
    	if (route == null)
    		return;

    	// obtain a HardcopyWriter to do this
    	HardcopyWriter writer = null;
    	try {
    		writer = new HardcopyWriter(mFrame, MessageFormat.format(Bundle.getString("TitleRoute"), new Object[] {route.getName()}), 10, .5, .5, .5, .5, isPreview);
    	} catch (HardcopyWriter.PrintCanceledException ex) {
    		log.debug("Print cancelled");
    		return;
    	}
    	printRoute(writer, route);
    	// and force completion of the printing
    	writer.close();
    }
    	
    protected void printRoute(HardcopyWriter writer, Route route) {
    	try {
    		writer.write(route.getComment()+newLine);
        	String s = Bundle.getString("Location") 
        	+ "\t    " + Bundle.getString("Direction") 
        	+ " " + Bundle.getString("MaxMoves") 
        	+ " " + Bundle.getString("Pickups")
        	+ " " + Bundle.getString("Drops")
        	+ " " + Bundle.getString("Wait")
        	+ "\t" + Bundle.getString("Length")     	
        	+ "\t" + Bundle.getString("Grade")
        	+ "\t" + Bundle.getString("X")
        	+ "    " + Bundle.getString("Y")
        	+ newLine;
        	writer.write(s);
    		List<String> locations = route.getLocationsBySequenceList();
    		for (int i=0; i<locations.size(); i++){
    			RouteLocation rl = route.getLocationById(locations.get(i)); 
    			String name = rl.getName();
    			name = truncate(name);
    			String pad = " ";
    			if (rl.getTrainIconX() < 10)
    				pad = "    ";
    			else if (rl.getTrainIconX() < 100)
    				pad = "   ";
    			else if (rl.getTrainIconX() < 1000)
    				pad = "  ";
    			s = name 
    			+ "\t" + rl.getTrainDirectionString() 
    			+ "\t" + rl.getMaxCarMoves()
    			+ "\t" + (rl.canPickup()?Bundle.getString("yes"):Bundle.getString("no"))
    			+ "\t" + (rl.canDrop()?Bundle.getString("yes"):Bundle.getString("no"))
    			+ "\t" + rl.getWait()
    			+ "\t" + rl.getMaxTrainLength()
    			+ "\t" + rl.getGrade()
    			+ "\t" + rl.getTrainIconX()
    			+ pad + rl.getTrainIconY()
    			+ newLine;
    			writer.write(s);		
    		}
    		s = newLine + Bundle.getString("Location") 
        	+ "\t" + Bundle.getString("DepartTime") 
        	+ "\t" + Bundle.getString("Comment")
        	+ newLine;
    		writer.write(s);
    		for (int i=0; i<locations.size(); i++){
    			RouteLocation rl = route.getLocationById(locations.get(i)); 
    			String name = rl.getName();
    			name = truncate(name);
    			s = name 
    			+ "\t" + rl.getDepartureTime()
    			+ "\t" + rl.getComment()
    			+ newLine;
    			writer.write(s);
    		}
    	} catch (IOException we) {
    		log.error("Error printing route");
    	}
    }
    
    private String truncate (String string){
		string = string.trim();
		if (string.length()>MAX_NAME_LENGTH)
			string = string.substring(0, MAX_NAME_LENGTH);
		// pad out the string
		StringBuffer buf = new StringBuffer(string);
		for (int j=string.length(); j < MAX_NAME_LENGTH; j++) {
			buf.append(" ");
		}
		return buf.toString();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintRouteAction.class.getName());
}
