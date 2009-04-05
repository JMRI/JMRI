// TrainInfo.java

package jmri.jmrit.dispatcher;

/**
 * TrainInfo is a temporary object specifying New Train information just read 
 *		from disk, or to be written to disk
 * <P>
 * Used in conjunction with TrainInfoFile.java to save and retrieve New Train information
 * <P>
 * When adding a new item of New Train information, modifications need to be made to
 *		TrainInfoFile.java and dispatcher-traininfo.DTD as well as this module.
 *
 * @author	Dave Duchamp  Copyright (C) 2009
 * @version	$Revision: 1.3 $
 */
public class TrainInfo {

    public TrainInfo() {
    }
    
	// instance variables for both manual and automatic operation
	String transitName = "";
	String trainName = "";
	String dccAddress = "";
	boolean trainInTransit = false;
	String startBlockName = "";
	String destinationBlockName = "";
	boolean trainFromRoster = true;
	boolean trainFromTrains = false;
	boolean trainFromUser = false;
	String priority = "";
	boolean autoRun = false;
	boolean resetWhenDone = false;
	
	// instance variables for automatic operation 
	
	// temporary instance variables
	
	/**
     * Access methods for manual and automatic instance variables
     */
	protected void setTransitName(String s) {transitName = s;}
	protected String getTransitName() {return transitName;}
	protected void setTrainName(String s) {trainName = s;}
	protected String getTrainName() {return trainName;}
	protected void setDCCAddress(String s) { dccAddress = s;}
	protected String getDCCAddress() {return dccAddress;}
	protected void setTrainInTransit(boolean b) {trainInTransit = b;}
	protected boolean getTrainInTransit() {return trainInTransit;}
	protected void setStartBlockName(String s) {startBlockName = s;}
	protected String getStartBlockName() {return startBlockName;}
	protected void setDestinationBlockName(String s) {destinationBlockName = s;}
	protected String getDestinationBlockName() {return destinationBlockName;}
	protected void setTrainFromRoster(boolean b) {trainFromRoster = b;}
	protected boolean getTrainFromRoster() {return trainFromRoster;}
	protected void setTrainFromTrains(boolean b) {trainFromTrains = b;}
	protected boolean getTrainFromTrains() {return trainFromTrains;}
	protected void setTrainFromUser(boolean b) {trainFromUser = b;}
	protected boolean getTrainFromUser() {return trainFromUser;}
	protected void setPriority(String s) {priority = s;}
	protected String getPriority() {return priority;}
	protected void setRunAuto(boolean b) {autoRun = b;}
	protected boolean getRunAuto() {return autoRun;}
	protected void setResetWhenDone(boolean b) {resetWhenDone = b;}
	protected boolean getResetWhenDone() {return resetWhenDone;}
	
	/**
     * Access methods for automatic operation instance variables
     */

    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainInfo.class.getName());
}

/* @(#)TrainInfo.java */
