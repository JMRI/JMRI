//AbstractOperationsServer.java

package jmri.jmris;

import java.io.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.routes.*;

/**
 * Abstract interface between the JMRI operations and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 17977 $
 */

abstract public class AbstractOperationsServer implements java.beans.PropertyChangeListener {

   TrainManager tm = null;
   LocationManager lm = null;
   public AbstractOperationsServer(){
	tm=TrainManager.instance();
	tm.addPropertyChangeListener(this);
	lm=LocationManager.instance();
	lm.addPropertyChangeListener(this);
   }


   public void propertyChange(java.beans.PropertyChangeEvent ev) {
       // we may need to do something with the properties at some
       // point, but we don't know what yet.
   }

   public void dispose() {
       if (tm!=null) tm.removePropertyChangeListener(this);
       if (lm!=null) lm.removePropertyChangeListener(this);
   }

   /* send a list of trains */
   public void sendTrainList() throws IOException {
	java.util.List<String> trainList=tm.getTrainsByNameList();
	for(String trainID : trainList )
	   sendInfoString(tm.getTrainById(trainID).getName());
	// end list with a . on a line by itself
	sendInfoString(".");
   }

   /* send a list of locations */
   public void sendLocationList() throws IOException {
	java.util.List<String> locationList=lm.getLocationsByNameList();
	for(String LocationID : locationList )
	   sendInfoString(lm.getLocationById(LocationID).getName());
	// end list with a . on a line by itself
	sendInfoString(".");
   }

   /* send train status */
   public void sendTrainStatus(String trainName) throws IOException {
	Train train=tm.getTrainByName(trainName);
	sendInfoString("OPERATIONS " + trainName +" STATUS "+train.getStatus());
   }

   /* send train location */
   public void sendTrainLocation(String trainName) throws IOException {
	Train train=tm.getTrainByName(trainName);
	sendInfoString("OPERATIONS "+ trainName + " LOCATION " + train.getCurrentLocationName());
   }

   /* Set the current location of the train */
   public void setTrainLocation(String trainName, String locationName) throws IOException {
        log.debug("Set train " + trainName + " Location " +locationName);
	Train train = tm.getTrainByName(trainName);
	//Location location = lm.getLocationByName(locationName);
	//train.setCurrentLocation(train.getRoute().getLocationById(location.getId()));
	if(locationName.equals(train.getNextLocationName()))
           train.setCurrentLocation(train.getNextLocation());
	sendTrainLocation(trainName);
   }

   /* send train length */
   public void sendTrainLength(String trainName) throws IOException {
	Train train=tm.getTrainByName(trainName);
	sendInfoString("OPERATIONS " + trainName + " LENGTH " + train.getTrainLength());
   }
 
   /*
    * Protocol Specific Abstract Functions
    */

    abstract public void sendInfoString(String statusString) throws IOException; 
    abstract public void sendErrorStatus() throws IOException;
    abstract public void parseStatus(String statusString) throws jmri.JmriException;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractOperationsServer.class.getName());

}
