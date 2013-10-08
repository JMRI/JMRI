// TrainInfo.java

package jmri.jmrit.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ResourceBundle;

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
 * @version	$Revision$
 */
public class TrainInfo {

    public TrainInfo() {
    }

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.dispatcher.DispatcherBundle");
    
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
	boolean reverseAtEnd = false;
	int delayedStart = ActiveTrain.NODELAY;
	String departureTimeHr = "08";
	String departureTimeMin = "00";
    String delaySensor = null;
    int delayedRestart = ActiveTrain.NODELAY;
    String restartDelaySensor = null;
    String delayedRestartTime = "0";
	String trainType = "";
    boolean terminateWhenDone = false;
	
	// instance variables for automatic operation
	String speedFactor = ""+1.0f;
	String maxSpeed = ""+0.6f;
	String rampRate = rb.getString("RAMP_NONE"); 
	boolean resistanceWheels = true;
	boolean runInReverse = false;
	boolean soundDecoder = false;
	String maxTrainLength = "200.0";
	
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
	protected void setTerminateWhenDone(boolean b) {terminateWhenDone = b;}
	protected boolean getTerminateWhenDone() {return terminateWhenDone;}
	protected void setPriority(String s) {priority = s;}
	protected String getPriority() {return priority;}
	protected void setRunAuto(boolean b) {autoRun = b;}
	protected boolean getRunAuto() {return autoRun;}
	protected void setResetWhenDone(boolean b) {resetWhenDone = b;}
	protected boolean getResetWhenDone() {return resetWhenDone;}
	protected void setReverseAtEnd(boolean b) {reverseAtEnd = b;}
	protected boolean getReverseAtEnd() {return reverseAtEnd;}
	protected void setDelayedStart(int ds) {delayedStart = ds;}
	protected int getDelayedStart() {return delayedStart;}
	protected void setDepartureTimeHr(String s) {departureTimeHr = s;}
	protected String getDepartureTimeHr() {return departureTimeHr;}
	protected void setDepartureTimeMin(String s) {departureTimeMin = s;}
	protected String getDepartureTimeMin() {return departureTimeMin;}
    protected void setDelaySensor(String sen) { delaySensor = sen; }
    protected String getDelaySensor() {return delaySensor; }
	protected void setTrainType(String s) {trainType = s;}
	protected String getTrainType() {return trainType;}
    
    protected void setDelayedRestart(int ds) {delayedRestart = ds;}
	protected int getDelayedRestart() {return delayedRestart;}
    protected void setRestartDelaySensor(String sen) { restartDelaySensor = sen; }
    protected String getRestartDelaySensor() {return restartDelaySensor; }
    protected void setRestartDelayTime(String s) {delayedRestartTime = s;}
	protected String getRestartDelayTime() {return delayedRestartTime;}
    
	/**
     * Access methods for automatic operation instance variables
     */
	protected void setSpeedFactor(String s) {speedFactor = s;}
	protected String getSpeedFactor() {return speedFactor;}
	protected void setMaxSpeed(String s) {maxSpeed = s;}
	protected String getMaxSpeed() {return maxSpeed;}
	protected void setRampRate(String s) {rampRate = s;}
	protected String getRampRate() {return rampRate;}
	protected void setResistanceWheels(boolean b) {resistanceWheels = b;}
	protected boolean getResistanceWheels() {return resistanceWheels;}
	protected void setRunInReverse(boolean b) {runInReverse = b;}
	protected boolean getRunInReverse() {return runInReverse;}
	protected void setSoundDecoder(boolean b) {soundDecoder = b;}
	protected boolean getSoundDecoder() {return soundDecoder;}
	protected void setMaxTrainLength(String s) {maxTrainLength = s;}
	protected String getMaxTrainLength() {return maxTrainLength;}

    
    static Logger log = LoggerFactory.getLogger(TrainInfo.class.getName());
}

/* @(#)TrainInfo.java */
