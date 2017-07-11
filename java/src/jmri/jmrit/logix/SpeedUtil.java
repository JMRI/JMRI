package jmri.jmrit.logix;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.logix.Engineer.RampData;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All speed related method transferred from Engineer and Warrant classes.
 * Until June 2017, the problem of determining the actual track speed of a
 * model train in millimeters per millisecond (same as meters/sec) from the
 * throttle setting was usually done with an ad hoc "throttle factor".  When
 * created, the RosterSpeedProfile provides this needed conversion but
 * generally is not done by users for each of their locos.
 * 
 * Methods to dynamically determine a RosterSpeedProfile for each loco are 
 * implemented in this class.
 * 
 * @author Pete Cressman Copyright (C) 2009, 2010, 2017
 *
 */
public class SpeedUtil {

    private DccLocoAddress _dccAddress;
    private String _rosterId;        // Roster title for train
    private boolean _newRosterId;
    private RosterEntry _rosterEntry;
    private Warrant _warrant;

//    private TreeMap<Integer, SpeedStep> _speedTree;
    private DccThrottle _throttle;
    private float _stepIncrement;   // min throttle
    private RosterSpeedProfile _speedProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 

    public static float SCALE_FACTOR = 140; // divided by _scale, gives a rough correction for track speed

    protected SpeedUtil(Warrant war) {
        if (war !=null) {
            _warrant = war;
        }
    }
    
    protected void setWarrant(Warrant w) {
        if (w !=null) {
            _warrant = w;
        }
    }
    
    public RosterEntry getRosterEntry() {
        if (_rosterEntry == null) {
            _rosterEntry = Roster.getDefault().entryFromTitle(_rosterId);
        }
        return _rosterEntry;
    }

    public String getTrainId() {
        return _rosterId;
    }

    public void setTrainId(String id) {
        if (_rosterId != null && !_rosterId.equals(id)) {
            _newRosterId = true;            
        }
        _rosterId = id;
    }
    
    public DccLocoAddress getDccAddress() {
        if (_dccAddress == null) {
            if (_rosterEntry != null) {
                _dccAddress = _rosterEntry.getDccLocoAddress();
            } else {
                setDccAddress(_rosterId);
            }
        }
        return _dccAddress;            
    }
    
    public String getAddress() {
        if (_dccAddress != null) {
            return _dccAddress.toString();
        }
        return null;
    }
    
    protected void setDccAddress(DccLocoAddress dccAddr) {
        _dccAddress = dccAddr;
    }

   /**
     * Sets dccAddress and will fetch RosterEntry if one exists
     * @param id address as a String, either RosterTitle or decoder address
     * @return true if address found for id
     */
    public boolean setDccAddress(String id) {
        if (id == null || id.trim().length()==0) {
            _rosterEntry = null;
            setTrainId(null);   // set _rosterId
            _dccAddress = null;           
           return false;
        }
        _rosterEntry = Roster.getDefault().entryFromTitle(id);
        if (_rosterEntry == null) {
            int index = id.indexOf('(');
            String numId;
            if (index >= 0) {
                numId = id.substring(0, index);
            } else {
                Character ch = id.charAt(id.length() - 1);
                if (!Character.isDigit(ch)) {
                    numId = id.substring(0, id.length() - 1);
                } else {
                    numId = id;
                }
            }
            int num = Integer.parseInt(numId);
            try {
                List<RosterEntry> l = Roster.getDefault().matchingList(null, null, numId, null, null, null, null);
                if (l.size() > 0) {
                    _rosterEntry = l.get(0);
                    if (num != 0) {
                        // In some systems, such as Maerklin MFX or ESU ECOS M4, the DCC address is always 0.
                        // That should not make us overwrite the _trainId.
                        setTrainId(_rosterEntry.getId());
                    }
                    _dccAddress = _rosterEntry.getDccLocoAddress();           
                } else {
                    boolean isLong = true;
                    if ((index + 1) < id.length()
                            && (id.charAt(index + 1) == 'S' || id.charAt(index + 1) == 's')) {
                        isLong = false;
                    }
                    _dccAddress = new DccLocoAddress(num, isLong);
                    setTrainId(_dccAddress.toString()); // not a rosterId, but does identify the  DccLocoAddress                       
               }
            } catch (NumberFormatException e) {
                _dccAddress = null;
                return false;
            }
        } else {
            setTrainId(id);
            _dccAddress = _rosterEntry.getDccLocoAddress();           
        }
        if (log.isDebugEnabled()) log.debug("setDccAddress: _rosterId= {}, _dccAddress= {}",_rosterId, _dccAddress);
        return true;
    }

    // Possibly customize these ramping values per warrant or loco later
    // for now use global values set in WarrantPreferences
    protected float getRampThrottleIncrement() {
        return _signalSpeedMap.getStepIncrement();
    }
    protected int getRampTimeIncrement() {
        return _signalSpeedMap.getStepDelay();
    }
    
    protected RosterSpeedProfile getSpeedProfile() {
        if (_speedProfile == null || _newRosterId) {
            makeSpeedTree();
        }
        return _speedProfile;
    }

    private void makeSpeedTree() {
        if (log.isDebugEnabled()) log.debug("makeSpeedTree for {}", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _speedProfile = manager.getMergeProfile(_rosterId);
        _sessionProfile = manager.getSessionProfile(_rosterId);
        if (_sessionProfile == null) {
            _sessionProfile = new RosterSpeedProfile(null);
        }
        if (_speedProfile == null) {
            _speedProfile = new RosterSpeedProfile(_rosterEntry);   // will be a copy or an empty profile            
            if (_rosterEntry!=null) {
                if (log.isDebugEnabled()) log.debug("makeSpeedTree - Copy TreeMap");
                RosterSpeedProfile speedProfile = _rosterEntry.getSpeedProfile();
                if (speedProfile!=null) { // make copy of tree
                    TreeMap<Integer, SpeedStep> speedtree = new TreeMap<Integer, SpeedStep> ();
                    TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
                    for (Map.Entry<Integer, SpeedStep> entry : rosterTree.entrySet()) {
                        _speedProfile.setSpeed(entry.getKey(), entry.getValue().getForwardSpeed(), entry.getValue().getReverseSpeed());
                    }
                }
            }
        }
        _newRosterId = false;

        _signalSpeedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
        if (log.isDebugEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to m/s= {}",
                _signalSpeedMap.getDefaultThrottleFactor(), _signalSpeedMap.getLayoutScale(),
                _signalSpeedMap.getDefaultThrottleFactor() * _signalSpeedMap.getLayoutScale() / SCALE_FACTOR);
    }

    protected void stopRun(boolean updateSpeedProfile) {
        if (updateSpeedProfile && _speedProfile!=null) {
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            manager.setSpeedProfiles(_rosterId, _speedProfile, _sessionProfile);
        }
    }

    /************* runtime speed needs - throttle, engineer acquired ***************/

    /**
     * @param throttle set DccThrottle
     */
    protected void setThrottle( DccThrottle throttle) {
        _throttle = throttle;
        _stepIncrement = _throttle.getSpeedIncrement();
        getSpeedProfile();
    }
    /**
     * Calculates the scale speed of the current throttle setting for display
     * @param speedType name of current speed
     * @return text message
     */
//    @SuppressFBWarnings(value="IS2_INCONSISTENT_SYNC", justification="speed type name in message is ok")
    public String getSpeedMessage(String speedType) {
        float speed = getTrackSpeed(getSpeedSetting(), _throttle.getIsForward()) * _signalSpeedMap.getLayoutScale();

        String units;
        if (_signalSpeedMap.getInterpretation() == SignalSpeedMap.SPEED_KMPH) {
            units = "Kmph";
            speed = speed * 3.6f;
        } else {
            units = "Mph";
            speed = speed * 2.2369363f;
        }
        return Bundle.getMessage("atSpeed", speedType, Math.round(speed), units);
    }

    // return a boolean so minSpeedType() can return a non-null String if possible
    protected boolean secondGreaterThanFirst(String speed1, String speed2) {
        if (speed2 == null) {
            return false;
        }
        if (speed1 == null) {
            return true;
        }
        float s1 = _signalSpeedMap.getSpeed(speed1);
        float s2 = _signalSpeedMap.getSpeed(speed2);
        return (s1 < s2);
    }

    protected float modifySpeed(float tSpeed, String sType, boolean isForward) {
//        if (log.isTraceEnabled()) log.trace("modifySpeed speed= {} for SpeedType= \"{}\"", tSpeed, sType);
        if (sType.equals(Warrant.Stop)) {
            return 0.0f;
        }
        if (sType.equals(Warrant.EStop)) {
            return -1.0f;
        }
        float throttleSpeed = tSpeed;       // throttleSpeed is a throttle setting
        if (sType.equals(Warrant.Normal)) {
            return throttleSpeed;
        }
        float signalSpeed = _signalSpeedMap.getSpeed(sType);

        switch (_signalSpeedMap.getInterpretation()) {
            case SignalSpeedMap.PERCENT_NORMAL:
                throttleSpeed *= signalSpeed / 100;      // ratio of normal
                break;
            case SignalSpeedMap.PERCENT_THROTTLE:
                signalSpeed = signalSpeed / 100;            // ratio of full throttle setting
                if (signalSpeed < throttleSpeed) {
                    throttleSpeed = signalSpeed;
                }
                break;

            case SignalSpeedMap.SPEED_MPH:          // convert miles per hour to track speed
                signalSpeed = signalSpeed / _signalSpeedMap.getLayoutScale();
                signalSpeed = signalSpeed / 2.2369363f;  // layout track speed mph -> mm/ms
                float trackSpeed = getTrackSpeed(throttleSpeed, isForward);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSetting(signalSpeed);
                    if (throttleSpeed <= 0.0f) {
                        return signalSpeed * _signalSpeedMap.getLayoutScale() / (SCALE_FACTOR *_signalSpeedMap.getDefaultThrottleFactor());
                    }
                }
                break;

            case SignalSpeedMap.SPEED_KMPH:
                signalSpeed = signalSpeed / _signalSpeedMap.getLayoutScale();
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms -> kmph
                trackSpeed = getTrackSpeed(throttleSpeed, isForward);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSetting(signalSpeed);
                    if (throttleSpeed <= 0.0f) {
                        return signalSpeed * _signalSpeedMap.getLayoutScale() / (SCALE_FACTOR *_signalSpeedMap.getDefaultThrottleFactor());
                    }
                }
                break;
            default:
                log.error("Unknown speed interpretation {}", _signalSpeedMap.getInterpretation());
                throw new java.lang.IllegalArgumentException("Unknown speed interpretation " + _signalSpeedMap.getInterpretation());
        }
        if (log.isTraceEnabled()) log.trace("modifySpeed: from {}, to {}, signalSpeed= {} using interpretation {}",
                tSpeed, throttleSpeed, signalSpeed, _signalSpeedMap.getInterpretation());
        return throttleSpeed;
    }

    // return millimeters per millisecond (= meters/sec)
    protected float getTrackSpeed(float throttleSetting, boolean isForward) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        // Note SpeedProfile uses milliseconds per second.
        float speed = speedProfile.getSpeed(throttleSetting, isForward) / 1000;
        boolean byFactor = false;
        if (speed < 0.0f) {
            speed = throttleSetting *_signalSpeedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _signalSpeedMap.getLayoutScale();
            byFactor = true;
        }
        if (log.isTraceEnabled()) log.trace("getTrackSpeed for setting= {}, speed= {}, by {}. train= {}",
                    throttleSetting, speed, (byFactor?"factor":"profile"), _rosterId);
        return speed;
    }

    // track speed is mm/ms.  SpeedProfile wants mm/s
    protected float getThrottleSetting(float trackSpeed) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        return speedProfile.getThrottleSetting(trackSpeed * 1000, _throttle.getIsForward());
    }

    /**
     * @param speedSetting Recorded (Normanl) throttle setting
     * @param speedtype speed name to modify throttle setting
     * @param time milliseconds
     * @param isForward direction
     * @return distance in millimeters
     * 
     */
    protected float getDistanceTraveled(float speedSetting, String speedtype, float time, boolean isForward) {
        float throttleSetting = modifySpeed(speedSetting, speedtype, isForward);
        return getTrackSpeed(throttleSetting, isForward) * time;
    }

    protected float getTimeForDistance(float throttleSetting, float distance, boolean isForward) {
        float speed = getTrackSpeed(throttleSetting, isForward);
        return (distance/speed);
    }

    protected RampData rampLengthForSpeedChange(float curSpeed, String curSpeedType, String toSpeedType, boolean isForward) {
        if (curSpeedType.equals(toSpeedType)) {
            return new RampData(0.0f, 0);
        }
        float fromSpeed = modifySpeed(curSpeed, curSpeedType, isForward);
        float toSpeed = modifySpeed(curSpeed, toSpeedType, isForward);
        if (toSpeed > fromSpeed) {
            float tmp = fromSpeed;
            fromSpeed = toSpeed;
            toSpeed = tmp;
        }
        float rampLength = 0.0f;
        int deltaTime = getRampTimeIncrement();
        float deltaThrottle = getRampThrottleIncrement();
        float speed = fromSpeed;
        int steps = 0;
        while (speed >= toSpeed) {
            float dist = getTrackSpeed(speed - deltaThrottle / 2, isForward) * deltaTime;
            if (dist <= 0.0f) {
                break;
            }
            speed -= deltaThrottle;
            if (speed >= toSpeed) {
                rampLength += dist;
            } else {
                rampLength += (speed + deltaThrottle - toSpeed) * dist / deltaThrottle;
            }
            steps++;
        }
        int rampTime = deltaTime*steps;
        if (log.isTraceEnabled()) log.trace("rampLengthForSpeedChange()= {} in {}ms for speed= {}, {} to {}, speed= {}",
                rampLength, rampTime, fromSpeed, curSpeedType, toSpeedType, toSpeed);
        return new RampData(rampLength, rampTime);   // add 1cm for safety (all scales)
    }
    
    protected float getSpeedSetting() {
        return _throttle.getSpeedSetting();
    }

    /*************** dynamic calibration ***********************/

    long _timeAtSpeed = 0;
    float _distanceTravelled = 0;
    float _settingsTravelled = 0;
    long _changetime;
    int _numchanges = 0;
    boolean _distanceValid;

    /**
     * Just entered block at newIdx. Do that calculation of speed from lastIdx
     * @param lastIdx BlockOrder index of where data collection started
     * @param newIdx BlockOrder index of block just entered
     */
    protected void enteredBlock(int lastIdx, int newIdx) {
        if (lastIdx > 0) {   // Distance traveled in 1st block unknown
            speedChange();
 
            float throttle = getSpeedSetting();
            float totalLength = 0.0f;
            boolean isForward = _throttle.getIsForward();
            boolean mergeOK = true;
            long elpsedTime = _warrant._orders.get(newIdx).getBlock()._entryTime - _warrant._orders.get(lastIdx).getBlock()._entryTime;
            if (newIdx > 1) {
                for (int i=lastIdx; i<newIdx; i++) {
                    BlockOrder blkOrder = _warrant._orders.get(i);
                    float length = blkOrder.getPath().getLengthMm();
                    if (length <= 0) {
                        log.warn("Block {} does not have a length for path {}",
                                blkOrder.getBlock().getDisplayName(), blkOrder.getPathName());
                        mergeOK = false;
                    }
                    totalLength += length;
                }                
            }
            float aveSpeed = 0;
            float aveThrottle = 0;
            float speed = 0;
            if (_timeAtSpeed > 1) {
                aveSpeed = totalLength / _timeAtSpeed;
                aveSpeed *= 1000;   // SpeedProfile is mm/sec
                speed = totalLength / elpsedTime;
                aveThrottle = _settingsTravelled / _timeAtSpeed;
                // throttle setting should be a step increment
                aveThrottle = _stepIncrement * Math.round(aveThrottle/_stepIncrement);
                
            }
            float spSpeed = _speedProfile.getSpeed(aveThrottle, isForward);                

            if (log.isDebugEnabled()) {
                log.debug("{} speed changes. AveThrottle= {}, throttle= {}. dist= {}, pathLength = {}. tas= {}, et= {}.",
                        _numchanges, aveThrottle, throttle, _distanceTravelled, totalLength, _timeAtSpeed, elpsedTime);
                log.debug("Speeds: SpeedProfile= {}, aveSpeed= {}, speed= {}, over block {} to {}",
                        spSpeed, aveSpeed, speed, _warrant._orders.get(lastIdx).getBlock().getDisplayName(),
                        _warrant._orders.get(newIdx).getBlock().getDisplayName());
            }
            if (spSpeed > 0.0f) {   // perhaps spSpeed should be weighted.  but how much?
                aveSpeed = (aveSpeed + spSpeed) / 2;
            } else if (aveSpeed < _stepIncrement){
                mergeOK = false;
            }
            if (mergeOK) {
                if (_numchanges == 1) {
                    aveThrottle = throttle;
                    aveSpeed = speed;
                }
                if (isForward) {
                    _speedProfile.setForwardSpeed(aveThrottle, aveSpeed);
                    _sessionProfile.setForwardSpeed(aveThrottle, aveSpeed);
                } else {
                    _speedProfile.setReverseSpeed(aveThrottle, aveSpeed);            
                    _sessionProfile.setReverseSpeed(aveThrottle, aveSpeed);
                }
                if (log.isDebugEnabled()) log.debug("Set ProfileSpeed aveThrottle= {}, aveSpeed= {}", aveThrottle, aveSpeed);
            }            
        }
 
        _timeAtSpeed = 0;
        _distanceTravelled = 0.0f;
        _settingsTravelled = 0.0f;
        _numchanges = 0;
        _distanceValid = true;
    }

    /*
     * 
     */
    protected void speedChange() {
        long time = System.currentTimeMillis();
        float throttleSetting = getSpeedSetting();
        long elapsedTime = time - _changetime;
        if (throttleSetting > 0.0f) {
            _timeAtSpeed += elapsedTime;
            float speed = _speedProfile.getSpeed(throttleSetting, _throttle.getIsForward());
            if (speed > 0.0f) {
                _distanceTravelled += elapsedTime * speed / 1000;
            } else {
                _distanceValid = false;
            }
            _settingsTravelled += throttleSetting * elapsedTime;
        }
        _numchanges++;
        _changetime = time;
    }
    
    protected float getDistanceTravelled() {
        if (_distanceValid) {
            return _distanceTravelled;            
        } else {
            return -1;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedUtil.class);
}