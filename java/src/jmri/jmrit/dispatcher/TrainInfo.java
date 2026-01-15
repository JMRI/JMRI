package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.dispatcher.ActiveTrain.TrainDetection;
import jmri.jmrit.dispatcher.ActiveTrain.TrainLengthUnits;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;

/**
 * TrainInfo is a temporary object specifying New Train information just read
 * from disk, or to be written to disk
 * <p>
 * Used in conjunction with TrainInfoFile.java to save and retrieve New Train
 * information
 * <p>
 * When adding a new item of New Train information, modifications need to be
 * made to TrainInfoFile.java and dispatcher-traininfo.DTD as well as this
 * module.
 *
 * @author Dave Duchamp Copyright (C) 2009
 */
public class TrainInfo {

    public TrainInfo() {
    }

    // instance variables for both manual and automatic operation
    private int version = 1;
    private boolean dynamicTransit = false;
    private boolean dynamicTransitCloseLoopIfPossible = false;
    private String transitName = "";
    private String transitId = "";
    private String trainName = "";
    private String rosterID = "";
    private String trainUserName = "";
    private String dccAddress = "3";
    private boolean trainInTransit = false;
    private String startBlockName = "";
    private String viaBlockName = "";
    private String startBlockId = "";
    private int startBlockSeq = -1;
    private String destinationBlockName = "";
    private String destinationBlockId = "";
    private int destinationBlockSeq = -1;
    private boolean trainFromRoster = true;
    private boolean trainFromTrains = false;
    private boolean trainFromUser = false;
    private boolean trainFromSetLater = false;
    private int priority = 5;
    private boolean autoRun = false;
    private boolean resetWhenDone = false;
    private boolean allocateAllTheWay = false;
    private int allocationMethod = 3;
    private boolean reverseAtEnd = false;
    private int delayedStart = ActiveTrain.NODELAY;
    private int delayedRestart = ActiveTrain.NODELAY;
    private int departureTimeHr = 8;
    private int departureTimeMin = 00;
    private String delaySensorName = null;
    private boolean resetStartSensor = true;

    private String restartSensorName = null;
    private boolean resetRestartSensor = true;
    private int restartDelayMin = 0;

    private int reverseDelayedRestart = ActiveTrain.NODELAY;
    private String reverseRestartSensorName = null;
    private boolean reverseResetRestartSensor = true;
    private int reverseRestartDelayMin = 0;

    private String trainType = "";
    private boolean terminateWhenDone = false;
    private String nextTrain = "None";
    private boolean loadAtStartup = false;

    // instance variables for automatic operation
    private float speedFactor = 1.0f;
    private float maxSpeed = 1.0f;
    private float minReliableOperatingSpeed = 0.0f;
    private String rampRate = Bundle.getMessage("RAMP_NONE");
    private TrainDetection trainDetection = TrainDetection.TRAINDETECTION_WHOLETRAIN;
    private boolean runInReverse = false;
    private boolean soundDecoder = false;
    private float maxTrainLength = 100.0f;
    private float maxTrainLengthMeters = 30.0f;
    private TrainLengthUnits trainLengthUnits = TrainLengthUnits.TRAINLENGTH_SCALEFEET; // units used to enter value
    private boolean useSpeedProfile = false;
    private boolean stopBySpeedProfile = false;
    private float stopBySpeedProfileAdjust = 1.0f;
    private int fNumberLight = 0;
    private int fNumberBell = 1;
    private int fNumberHorn = 2;

    private float waitTime = 3.0f; //seconds:  required only by dispatcher system to pause train at beginning of transit (station)

    private String blockName = ""; //required only by Dispatcher System to inhibit running of transit if this block is occupied


    //
    // Access methods for manual and automatic instance variables
    //
    public void setVersion(int ver) {
        version = ver;
    }
    public int getVersion() {
        return version;
    }

    public void setTransitName(String s) {
        transitName = s;
    }

    public String getTransitName() {
        return transitName;
    }

    public void setTransitId(String s) {
        transitId = s;
    }

    public String getTransitId() {
        return transitId;
    }

    public void setDynamicTransit(boolean b) {
        dynamicTransit = b;
    }

    public boolean getDynamicTransit() {
        return dynamicTransit;
    }

    public void setDynamicTransitCloseLoopIfPossible(boolean b) {
        dynamicTransitCloseLoopIfPossible = b;
    }

    public boolean getDynamicTransitCloseLoopIfPossible() {
        return dynamicTransitCloseLoopIfPossible;
    }
    public void setTrainName(String s) {
        trainName = s;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setRosterId(String s) {
        rosterID = s;
    }

    public String getRosterId() {
        return rosterID;
    }

    public void setTrainUserName(String s) {
        trainUserName = s;
    }

    public String getTrainUserName() {
        return trainUserName;
    }

    public void setDccAddress(String s) {
        dccAddress = s;
    }

    public String getDccAddress() {
        return dccAddress;
    }

    public void setTrainInTransit(boolean b) {
        trainInTransit = b;
    }

    public boolean getTrainInTransit() {
        return trainInTransit;
    }

    public void setStartBlockName(String s) {
        startBlockName = s;
    }

    public String getStartBlockName() {
        return startBlockName;
    }

    public void setViaBlockName(String s) {
        viaBlockName = s;
    }

    public String getViaBlockName() {
        return viaBlockName;
    }

    public void setStartBlockId(String s) {
        startBlockId = s;
    }

    public String getStartBlockId() {
        return startBlockId;
    }

    public void setStartBlockSeq(int i) {
        startBlockSeq = i;
    }

    public int getStartBlockSeq() {
        return startBlockSeq;
    }

    public void setDestinationBlockName(String s) {
        destinationBlockName = s;
    }

    public String getDestinationBlockName() {
        return destinationBlockName;
    }

    public void setDestinationBlockId(String s) {
        destinationBlockId = s;
    }

    public String getDestinationBlockId() {
        return destinationBlockId;
    }

    public void setDestinationBlockSeq(int i) {
        destinationBlockSeq = i;
    }

    public int getDestinationBlockSeq() {
        return destinationBlockSeq;
    }

    public void setTrainsFrom(TrainsFrom value) {
        trainFromRoster = false;
        trainFromTrains = false;
        trainFromUser = false;
        trainFromSetLater = false;
        switch (value) {
            case TRAINSFROMROSTER:
                trainFromRoster = true;
                break;
            case TRAINSFROMOPS:
                trainFromTrains = true;
                break;
            case TRAINSFROMUSER:
                trainFromUser = true;
                break;
            case TRAINSFROMSETLATER:
            default:
                trainFromSetLater = true;
        }
    }

    public TrainsFrom getTrainsFrom() {
        if (trainFromRoster) {
            return TrainsFrom.TRAINSFROMROSTER;
        } else if (trainFromTrains) {
            return TrainsFrom.TRAINSFROMOPS;
        } else if (trainFromUser) {
            return TrainsFrom.TRAINSFROMUSER;
        }
        return TrainsFrom.TRAINSFROMSETLATER;
    }

    public void setTrainFromRoster(boolean b) {
        trainFromRoster = b;
    }

    public boolean getTrainFromRoster() {
        return trainFromRoster;
    }

    public void setTrainFromTrains(boolean b) {
        trainFromTrains = b;
    }

    public boolean getTrainFromTrains() {
        return trainFromTrains;
    }

    public void setTrainFromUser(boolean b) {
        trainFromUser = b;
    }

    public boolean getTrainFromUser() {
        return trainFromUser;
    }

    public void setTrainFromSetLater(boolean b) {
        trainFromSetLater = b;
    }

    public boolean getTrainFromSetLater() {
        return trainFromSetLater;
    }

    public void setTerminateWhenDone(boolean b) {
        terminateWhenDone = b;
    }

    public boolean getTerminateWhenDone() {
        return terminateWhenDone;
    }

    public void setNextTrain(String s) {
        nextTrain = s;
    }

    public String getNextTrain() {
        return nextTrain;
    }


    public void setPriority(int pri) {
        priority = pri;
    }

    public int getPriority() {
        return priority;
    }

    public void setAutoRun(boolean b) {
        autoRun = b;
    }

    public boolean getAutoRun() {
        return autoRun;
    }

    public void setResetWhenDone(boolean b) {
        resetWhenDone = b;
    }

    public boolean getResetWhenDone() {
        return resetWhenDone;
    }

    public void setAllocateAllTheWay(boolean b) {
        allocateAllTheWay = b;
    }

    public boolean getAllocateAllTheWay() {
        return allocateAllTheWay;
    }

    public void setAllocationMethod(int i) {
        allocationMethod = i;
    }

    public int getAllocationMethod() {
        return allocationMethod;
    }

    public void setUseSpeedProfile(boolean b) {
        useSpeedProfile = b;
    }

    public boolean getUseSpeedProfile() {
        return useSpeedProfile;
    }

    public void setStopBySpeedProfile(boolean b) {
        stopBySpeedProfile = b;
    }

    public boolean getStopBySpeedProfile() {
        return stopBySpeedProfile;
    }

    public void setStopBySpeedProfileAdjust(float f) {
        stopBySpeedProfileAdjust = f;
    }

    public float getStopBySpeedProfileAdjust() {
        return stopBySpeedProfileAdjust;
    }

    public void setReverseAtEnd(boolean b) {
        reverseAtEnd = b;
    }

    public boolean getReverseAtEnd() {
        return reverseAtEnd;
    }

    public void setDelayedStart(int ds) {
        delayedStart = ds;
    }

    /**
     * delayed start code for this train
     *
     * @return one of ActiveTrain.NODELAY,TIMEDDELAY,SENSORDELAY
     */
    public int getDelayedStart() {
        return delayedStart;
    }

    public void setDepartureTimeHr(int hr) {
        departureTimeHr = hr;
    }

    public int getDepartureTimeHr() {
        return departureTimeHr;
    }

    public void setDepartureTimeMin(int min) {
        departureTimeMin = min;
    }

    public int getDepartureTimeMin() {
        return departureTimeMin;
    }

    public void setDelaySensorName(String sen) {
        delaySensorName = sen;
    }

    public String getDelaySensorName() {
        return delaySensorName;
    }

    public void setReverseDelayedRestart(int ds) {
        reverseDelayedRestart = ds;
    }

    /**
     * return restart code for this train, only used for continuous running
     *
     * @return one of ActiveTrain.NODELAY,TIMEDDELAY,SENSORDELAY
     */
    public int getReverseDelayedRestart() {
        return reverseDelayedRestart;
    }

    public void setReverseRestartSensorName(String value) {
        reverseRestartSensorName = value;
    }

    public String getReverseRestartSensorName() {
        return reverseRestartSensorName;
    }

    public void setReverseResetRestartSensor(boolean value) {
        reverseResetRestartSensor = value;
    }

    public boolean getReverseResetRestartSensor() {
        return reverseResetRestartSensor;
    }

    public Sensor getReverseRestartSensor() {
        if (reverseRestartSensorName == null) {
            return null;
        }
        return jmri.InstanceManager.sensorManagerInstance().getSensor(reverseRestartSensorName);
    }

    public void setReverseRestartDelayMin(int value) {
        reverseRestartDelayMin = value;
    }

    public int getReverseRestartDelayMin() {
        return reverseRestartDelayMin;
    }

    /**
     * retrieve the startup delay sensor using the delay sensor name
     *
     * @return delay sensor, or null if delay sensor name not set
     */
    public Sensor getDelaySensor() {
        if (delaySensorName == null) {
            return null;
        }
        return InstanceManager.getDefault(SensorManager.class).getSensor(delaySensorName);
    }

    public boolean getResetStartSensor() {
        return resetStartSensor;
    }

    public void setResetStartSensor(boolean b) {
        resetStartSensor = b;
    }

    public void setTrainType(String s) {
        trainType = s;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setDelayedRestart(int ds) {
        delayedRestart = ds;
    }

    /**
     * return restart code for this train, only used for continuous running
     *
     * @return one of ActiveTrain.NODELAY,TIMEDDELAY,SENSORDELAY
     */
    public int getDelayedRestart() {
        return delayedRestart;
    }

    public void setRestartSensorName(String sen) {
        restartSensorName = sen;
    }

    public String getRestartSensorName() {
        return restartSensorName;
    }

    /**
     * retrieve the restart sensor using the restart sensor name
     *
     * @return restart sensor, or null if the restart sensor name not set
     */
    public Sensor getRestartSensor() {
        if (restartSensorName == null) {
            return null;
        }
        return jmri.InstanceManager.sensorManagerInstance().getSensor(restartSensorName);
    }

    public boolean getResetRestartSensor() {
        return resetRestartSensor;
    }

    public void setResetRestartSensor(boolean b) {
        resetRestartSensor = b;
    }

    /**
     * number of minutes to delay between restarting for continuous runs
     *
     * @param s number of minutes to delay
     */
    public void setRestartDelayMin(int s) {
        restartDelayMin = s;
    }

    public int getRestartDelayMin() {
        return restartDelayMin;
    }

    public boolean getLoadAtStartup() {
        return loadAtStartup;
    }

    public void setLoadAtStartup(boolean loadAtStartup) {
        this.loadAtStartup = loadAtStartup;
    }

    //
    // Access methods for automatic operation instance variables
    //
    public void setSpeedFactor(float f) {
        speedFactor = f;
    }

    public Float getSpeedFactor() {
        return speedFactor;
    }

    public void setMaxSpeed(float f) {
        maxSpeed = f;
    }

    public Float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMinReliableOperatingSpeed(float f) {
        minReliableOperatingSpeed = f;
    }

    public float getMinReliableOperatingSpeed() {
        return minReliableOperatingSpeed;
    }

    public void setRampRate(String s) {
        rampRate = s;
    }

    public String getRampRate() {
        return rampRate;
    }

    /**
     * Set the detection get
     * @param b {@link ActiveTrain.TrainDetection}
     */
    public void setTrainDetection(TrainDetection b) {
        trainDetection = b;
    }

    /**
     * Get the detection type
     * @return  {@link ActiveTrain.TrainDetection}
     */
    public TrainDetection getTrainDetection() {
        return trainDetection;
    }

    /**
     * @deprecated use {@link #setTrainDetection}
     * @param b true or false
     */
    @Deprecated (since="5.7.6",forRemoval=true)
    public void setResistanceWheels(boolean b) {
        if (b) {
            trainDetection = TrainDetection.TRAINDETECTION_WHOLETRAIN;
        } else {
            trainDetection = TrainDetection.TRAINDETECTION_HEADONLY;
        }
    }

    /**
     * @deprecated use {@link #getTrainDetection}
     * @return true or false
     */
    @Deprecated (since="5.7.6",forRemoval=true)
    public boolean getResistanceWheels() {
        if (trainDetection == TrainDetection.TRAINDETECTION_WHOLETRAIN) {
            return true;
        }
        return false;
    }

    public void setRunInReverse(boolean b) {
        runInReverse = b;
    }

    public boolean getRunInReverse() {
        return runInReverse;
    }

    public void setSoundDecoder(boolean b) {
        soundDecoder = b;
    }

    public boolean getSoundDecoder() {
        return soundDecoder;
    }

    /**
     * Sets F number for the Light
     * @param value F Number.
     */
    public void setFNumberLight(int value) {
        fNumberLight = value;
    }

    /**
     * returns the F number for the Light
     * @return F Number
     */
    public int getFNumberLight() {
        return fNumberLight;
    }

    /**
     * Sets F number for the Bell
     * @param value F Number.
     */
    public void setFNumberBell(int value) {
        fNumberBell = value;
    }

    /**
     * returns the F number for the Bell
     * @return F Number
     */
    public int getFNumberBell() {
        return fNumberBell;
    }

    /**
     * Sets F number for the Horn
     * @param value F Number.
     */
    public void setFNumberHorn(int value) {
        fNumberHorn = value;
    }

    /**
     * returns the F number for the Horn
     * @return F Number
     */
    public int getFNumberHorn() {
        return fNumberHorn;
    }

    /**
     * @deprecated use {@link #setMaxTrainLengthScaleMeters}
     *               or {@link #setMaxTrainLengthScaleMeters}
     * @param f train length
     */
    @Deprecated (since="5.9.7",forRemoval=true)
    public void setMaxTrainLength(float f) {
        maxTrainLength = f;
    }

    /**
     * @deprecated use {@link #getMaxTrainLengthScaleMeters}
     *              or {@link #getMaxTrainLengthScaleFeet}
     * @return train length of in units of the writing application
     */
    @Deprecated (since="5.9.7",forRemoval=true)
    public float getMaxTrainLength() {
        return maxTrainLength;
    }

    /**
     * Sets the max train length expected during run
     * @param f scale Meters.
     */
    public void setMaxTrainLengthScaleMeters(float f) {
        maxTrainLengthMeters = f;
    }

    /**
     * Gets the Max train length expected during run
     * @return scale meters
     */
    public float getMaxTrainLengthScaleMeters() {
        return maxTrainLengthMeters;
    }

    /**
     * Sets the max train length expected
     * @param f scale Meters.
     */
    public void setMaxTrainLengthScaleFeet(float f) {
        maxTrainLengthMeters = f / 3.28084f;
    }

    /**
     * Gets the Max train length expected during route
     * @return scale meters
     */
    public float getMaxTrainLengthScaleFeet() {
        return maxTrainLengthMeters * 3.28084f;
    }

   /**
     * Sets the gui units used to enter or display (The units are always held in scale meters)
     * @param value {@link ActiveTrain.TrainLengthUnits}
     */
    public void setTrainLengthUnits(TrainLengthUnits value) {
        trainLengthUnits = value;
    }

    /**
     * Get the GUI units entered (The data is held in scale Meters)
     * @return  {@link ActiveTrain.TrainLengthUnits}
     */
    public TrainLengthUnits getTrainLengthUnits() {
        return trainLengthUnits;
    }

    public void setWaitTime(float f) { waitTime = f; }

    public float getWaitTime() {
        return waitTime;
    }

    public void setBlockName(String s) { blockName = s; }

    public String getBlockName() { return blockName; }

}
