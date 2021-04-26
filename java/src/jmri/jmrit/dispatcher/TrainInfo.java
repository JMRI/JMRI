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

    public void setTrainName(String s) {
        trainName = s;
    }

    public String getTrainName() {
        return trainName;
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

    public void setTerminateWhenDone(boolean b) {
        terminateWhenDone = b;
    }

    public boolean getTerminateWhenDone() {
        return terminateWhenDone;
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

    public void setRampRate(String s) {
        rampRate = s;
    }

    public String getRampRate() {
        return rampRate;
    }

    public void setResistanceWheels(boolean b) {
        resistanceWheels = b;
    }

    public boolean getResistanceWheels() {
        return resistanceWheels;
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

    public void setMaxTrainLength(float f) {
        maxTrainLength = f;
    }

    public float getMaxTrainLength() {
        return maxTrainLength;
    }
}
