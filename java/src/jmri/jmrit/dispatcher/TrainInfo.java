package jmri.jmrit.dispatcher;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;

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
    private String transitName = "";
    private String transitId = "";
    private String trainName = "";
    private String dccAddress = "";
    private boolean trainInTransit = false;
    private String startBlockName = "";
    private String startBlockId = "";
    private int startBlockSeq = -1;
    private String destinationBlockName = "";
    private String destinationBlockId = "";
    private int destinationBlockSeq = -1;
    private boolean trainFromRoster = true;
    private boolean trainFromTrains = false;
    private boolean trainFromUser = false;
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
    private String trainType = "";
    private boolean terminateWhenDone = false;
    private boolean loadAtStartup = false;

    // instance variables for automatic operation
    private float speedFactor = 1.0f;
    private float maxSpeed = 0.6f;
    private String rampRate = Bundle.getMessage("RAMP_NONE");
    private boolean resistanceWheels = true;
    private boolean runInReverse = false;
    private boolean soundDecoder = false;
    private float maxTrainLength = 200.0f;
    private boolean useSpeedProfile = false;
    private boolean stopBySpeedProfile = false;
    private float stopBySpeedProfileAdjust = 1.0f;


    //
    // Access methods for manual and automatic instance variables
    //
    protected void setTransitName(String s) {
        transitName = s;
    }

    protected String getTransitName() {
        return transitName;
    }

    protected void setTransitId(String s) {
        transitId = s;
    }

    protected String getTransitId() {
        return transitId;
    }

    protected void setTrainName(String s) {
        trainName = s;
    }

    protected String getTrainName() {
        return trainName;
    }

    protected void setDccAddress(String s) {
        dccAddress = s;
    }

    protected String getDccAddress() {
        return dccAddress;
    }

    protected void setTrainInTransit(boolean b) {
        trainInTransit = b;
    }

    protected boolean getTrainInTransit() {
        return trainInTransit;
    }

    protected void setStartBlockName(String s) {
        startBlockName = s;
    }

    protected String getStartBlockName() {
        return startBlockName;
    }

    protected void setStartBlockId(String s) {
        startBlockId = s;
    }

    protected String getStartBlockId() {
        return startBlockId;
    }

    protected void setStartBlockSeq(int i) {
        startBlockSeq = i;
    }

    protected int getStartBlockSeq() {
        return startBlockSeq;
    }

    protected void setDestinationBlockName(String s) {
        destinationBlockName = s;
    }

    protected String getDestinationBlockName() {
        return destinationBlockName;
    }

    protected void setDestinationBlockId(String s) {
        destinationBlockId = s;
    }

    protected String getDestinationBlockId() {
        return destinationBlockId;
    }

    protected void setDestinationBlockSeq(int i) {
        destinationBlockSeq = i;
    }

    protected int getDestinationBlockSeq() {
        return destinationBlockSeq;
    }

    protected void setTrainFromRoster(boolean b) {
        trainFromRoster = b;
    }

    protected boolean getTrainFromRoster() {
        return trainFromRoster;
    }

    protected void setTrainFromTrains(boolean b) {
        trainFromTrains = b;
    }

    protected boolean getTrainFromTrains() {
        return trainFromTrains;
    }

    protected void setTrainFromUser(boolean b) {
        trainFromUser = b;
    }

    protected boolean getTrainFromUser() {
        return trainFromUser;
    }

    protected void setTerminateWhenDone(boolean b) {
        terminateWhenDone = b;
    }

    protected boolean getTerminateWhenDone() {
        return terminateWhenDone;
    }

    protected void setPriority(int pri) {
        priority = pri;
    }

    protected int getPriority() {
        return priority;
    }

    protected void setAutoRun(boolean b) {
        autoRun = b;
    }

    protected boolean getAutoRun() {
        return autoRun;
    }

    protected void setResetWhenDone(boolean b) {
        resetWhenDone = b;
    }

    protected boolean getResetWhenDone() {
        return resetWhenDone;
    }

    protected void setAllocateAllTheWay(boolean b) {
        allocateAllTheWay = b;
    }

    protected boolean getAllocateAllTheWay() {
        return allocateAllTheWay;
    }

    protected void setAllocationMethod(int i) {
        allocationMethod = i;
    }

    protected int getAllocationMethod() {
        return allocationMethod;
    }

    protected void setUseSpeedProfile(boolean b) {
        useSpeedProfile = b;
    }

    protected boolean getUseSpeedProfile() {
        return useSpeedProfile;
    }

    protected void setStopBySpeedProfile(boolean b) {
        stopBySpeedProfile = b;
    }

    protected boolean getStopBySpeedProfile() {
        return stopBySpeedProfile;
    }

    protected void setStopBySpeedProfileAdjust(float f) {
        stopBySpeedProfileAdjust = f;
    }

    protected float getStopBySpeedProfileAdjust() {
        return stopBySpeedProfileAdjust;
    }

    protected void setReverseAtEnd(boolean b) {
        reverseAtEnd = b;
    }

    protected boolean getReverseAtEnd() {
        return reverseAtEnd;
    }

    protected void setDelayedStart(int ds) {
        delayedStart = ds;
    }

    /**
     * delayed start code for this train
     *
     * @return one of ActiveTrain.NODELAY,TIMEDDELAY,SENSORDELAY
     */
    protected int getDelayedStart() {
        return delayedStart;
    }

    protected void setDepartureTimeHr(int hr) {
        departureTimeHr = hr;
    }

    protected int getDepartureTimeHr() {
        return departureTimeHr;
    }

    protected void setDepartureTimeMin(int min) {
        departureTimeMin = min;
    }

    protected int getDepartureTimeMin() {
        return departureTimeMin;
    }

    protected void setDelaySensorName(String sen) {
        delaySensorName = sen;
    }

    protected String getDelaySensorName() {
        return delaySensorName;
    }

    /**
     * retrieve the startup delay sensor using the delay sensor name
     *
     * @return delay sensor, or null if delay sensor name not set
     */
    protected Sensor getDelaySensor() {
        if (delaySensorName == null) {
            return null;
        }
        return InstanceManager.getDefault(SensorManager.class).getSensor(delaySensorName);
    }

    protected boolean getResetStartSensor() {
        return resetStartSensor;
    }

    protected void setResetStartSensor(boolean b) {
        resetStartSensor = b;
    }

    protected void setTrainType(String s) {
        trainType = s;
    }

    protected String getTrainType() {
        return trainType;
    }

    protected void setDelayedRestart(int ds) {
        delayedRestart = ds;
    }

    /**
     * return restart code for this train, only used for continuous running
     *
     * @return one of ActiveTrain.NODELAY,TIMEDDELAY,SENSORDELAY
     */
    protected int getDelayedRestart() {
        return delayedRestart;
    }

    protected void setRestartSensorName(String sen) {
        restartSensorName = sen;
    }

    protected String getRestartSensorName() {
        return restartSensorName;
    }

    /**
     * retrieve the restart sensor using the restart sensor name
     *
     * @return restart sensor, or null if the restart sensor name not set
     */
    protected Sensor getRestartSensor() {
        if (restartSensorName == null) {
            return null;
        }
        return jmri.InstanceManager.sensorManagerInstance().getSensor(restartSensorName);
    }

    protected boolean getResetRestartSensor() {
        return resetRestartSensor;
    }

    protected void setResetRestartSensor(boolean b) {
        resetRestartSensor = b;
    }
    
    /**
     * number of minutes to delay between restarting for continuous runs
     *
     * @param s number of minutes to delay
     */
    protected void setRestartDelayMin(int s) {
        restartDelayMin = s;
    }

    protected int getRestartDelayMin() {
        return restartDelayMin;
    }

    protected boolean getLoadAtStartup() {
        return loadAtStartup;
    }

    protected void setLoadAtStartup(boolean loadAtStartup) {
        this.loadAtStartup = loadAtStartup;
    }

    //
    // Access methods for automatic operation instance variables
    //
    protected void setSpeedFactor(float f) {
        speedFactor = f;
    }

    protected Float getSpeedFactor() {
        return speedFactor;
    }

    protected void setMaxSpeed(float f) {
        maxSpeed = f;
    }

    protected Float getMaxSpeed() {
        return maxSpeed;
    }

    protected void setRampRate(String s) {
        rampRate = s;
    }

    protected String getRampRate() {
        return rampRate;
    }

    protected void setResistanceWheels(boolean b) {
        resistanceWheels = b;
    }

    protected boolean getResistanceWheels() {
        return resistanceWheels;
    }

    protected void setRunInReverse(boolean b) {
        runInReverse = b;
    }

    protected boolean getRunInReverse() {
        return runInReverse;
    }

    protected void setSoundDecoder(boolean b) {
        soundDecoder = b;
    }

    protected boolean getSoundDecoder() {
        return soundDecoder;
    }

    protected void setMaxTrainLength(float f) {
        maxTrainLength = f;
    }

    protected float getMaxTrainLength() {
        return maxTrainLength;
    }
}
