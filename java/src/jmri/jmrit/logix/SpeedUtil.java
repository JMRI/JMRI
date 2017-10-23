package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
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
    private RosterEntry _rosterEntry;
    private List<BlockOrder> _orders;

    private DccThrottle _throttle;
    private float _stepIncrement = 1.0f / 126.0f;   // decoder throttle step interval
    private float _stepRampThrottleIncrement;   // user specified throttle increment for ramping
    private int _stepRampTimeIncrement; // user specified time for ramp step increment
    private RosterSpeedProfile _mergeProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 
    private int _ma;  // milliseconds needed to increase speed by _stepRampThrottleIncrement amount 
    private int _md;  // milliseconds needed to decrease speed by _stepRampThrottleIncrement amount
    private int _mp;  // default milliseconds needed to change speed by _stepRampThrottleIncrement amount

    public static float SCALE_FACTOR = 125; // divided by _scale, gives a rough correction for track speed

    protected SpeedUtil(List<BlockOrder> orders) {
        if (orders !=null) {
            _orders = orders;
        }
    }
    
    protected void setOrders(List<BlockOrder> orders) {
        if (orders !=null) {
            _orders = orders;
        }
    }
    
    public RosterEntry getRosterEntry() {
        if (_rosterEntry == null) {
            _rosterEntry = Roster.getDefault().entryFromTitle(_rosterId);
        }
        return _rosterEntry;
    }

    /**
     * Set the identifier for the Speed Profile
     * If a RosterEntry exists, _rosterId is the RosterEntry id,
     * otherwise it may be just the decoder address
     * @return key to speedProfile
     */
    public String getRosterId() {
        return _rosterId;
    }

    public void setRosterId(String id) {
       if (log.isDebugEnabled()) log.debug("setRosterId({}) _rosterId= {}", id, _rosterId);
       if (id == null || !id.equals(_rosterId)) {
            _mergeProfile = null;
            _sessionProfile = null;
            if (id != null) {
                _rosterId = id;
                makeSpeedTree();
                makeRampParameters();
            } else {
                _rosterId = null;
                _rosterEntry = null;
            }
        }
    }
    
    public DccLocoAddress getDccAddress() {
        if (_dccAddress == null) {
            if (_rosterEntry != null) {
                _dccAddress = _rosterEntry.getDccLocoAddress();
            } else if (_rosterId != null){
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
     * Sets dccAddress and will fetch RosterEntry if one exists.
     * If _rosterEntry exists, _rosterId set to RosterEntry Id (which may or not be "id")
     * else _rosterId set to "id" or decoder address.
     * Called from: 
     *    WarrantRoute.setAddress() - whatever is in _dccNumBox.getText()
     *    WarrantRoute.setTrainInfo(name) - whatever = name
     *    WarrantTableModel - whatever address is put into the ADDRESS_COLUMN
     * @param id address as a String, either RosterEntryTitle or decoder address
     * @return true if address found for id
     */
    public boolean setDccAddress(String id) {
        if (log.isDebugEnabled()) log.debug("setDccAddress({}) _rosterId= {}", id, _rosterId);
        if (id == null || id.trim().length()==0) {
            setRosterId(null);   // set _rosterId
            _dccAddress = null;           
           return false;
        } else if (id.equals(_rosterId)){
            return true;
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
                        setRosterId(_rosterEntry.getId());
                    }
                    _dccAddress = _rosterEntry.getDccLocoAddress();           
                } else {
                    boolean isLong = true;
                    if ((index + 1) < id.length()
                            && (id.charAt(index + 1) == 'S' || id.charAt(index + 1) == 's')) {
                        isLong = false;
                    }
                    _dccAddress = new DccLocoAddress(num, isLong);
                    setRosterId(_dccAddress.toString()); // not a rosterId, but does identify the  DccLocoAddress                       
               }
            } catch (NumberFormatException e) {
                _dccAddress = null;
                return false;
            }
        } else {
            setRosterId(id);
            _dccAddress = _rosterEntry.getDccLocoAddress();
//            _rosterId = _rosterEntry.getId();
        }
        if (log.isDebugEnabled()) log.debug("setDccAddress: _rosterId= {}, _dccAddress= {}",_rosterId, _dccAddress);
        return true;
    }

    // Possibly customize these ramping values per warrant or loco later
    // for now use global values set in WarrantPreferences
    // user's ramp speed increase amount
    protected float getRampThrottleIncrement() {
        return _stepRampThrottleIncrement;
    }
    protected int getRampTimeIncrement() {
        return _stepRampTimeIncrement;
    }
    protected float getMomentumTime(boolean increasing) {
        if (increasing) {
            return _ma;            
        } else {
            return _md;
        }
    }

    // throttle's minimum speed increase amount 
    protected float getThrottleSpeedStepIncrement() {
        return _stepIncrement;
    }

    protected RosterSpeedProfile getSpeedProfile() {
        if (_sessionProfile == null) {
            if (_mergeProfile == null) {
                makeSpeedTree();
                makeRampParameters();                
            } else {
                return _mergeProfile;
            }
        } else if (!_sessionProfile.hasForwardSpeeds() && !_sessionProfile.hasReverseSpeeds()) {
                return _mergeProfile;
        }
        return _sessionProfile;
    }
    
    protected RosterSpeedProfile getMergeProfile() {
        if (_mergeProfile == null) {
            makeSpeedTree();
            makeRampParameters();                
        }
        return _mergeProfile;
    }

    private void makeSpeedTree() {
        if (_rosterId == null) {
            setDccAddress(getAddress());
        }
        if (log.isDebugEnabled()) log.debug("makeSpeedTree for {}.", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _mergeProfile = manager.getMergeProfile(_rosterId);
        _sessionProfile = manager.getSessionProfile(_rosterId);
        if (_sessionProfile == null) {
            _sessionProfile = new RosterSpeedProfile(null);
        }
        if (_mergeProfile == null) {
            _mergeProfile = new RosterSpeedProfile(getRosterEntry());   // will be a copy or an empty profile            
            if (_rosterEntry!=null) {
                if (log.isDebugEnabled()) log.debug("makeSpeedTree - Copy TreeMap");
                RosterSpeedProfile speedProfile = _rosterEntry.getSpeedProfile();
                if (speedProfile!=null) { // make copy of tree
                    TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
                    for (Map.Entry<Integer, SpeedStep> entry : rosterTree.entrySet()) {
                        _mergeProfile.setSpeed(entry.getKey(), entry.getValue().getForwardSpeed(), entry.getValue().getReverseSpeed());
                    }
                }
            }
        }
        _signalSpeedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);

        if (log.isDebugEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to m/s= {}",
                _signalSpeedMap.getDefaultThrottleFactor(), _signalSpeedMap.getLayoutScale(),
                _signalSpeedMap.getDefaultThrottleFactor() * _signalSpeedMap.getLayoutScale() / SCALE_FACTOR);
    }
    
    private void makeRampParameters() {
        WarrantPreferences preferences = WarrantPreferences.getDefault();
        _stepRampTimeIncrement = preferences.getTimeIncrement();
        _stepRampThrottleIncrement = preferences.getThrottleIncrement();
        // Can't use actual speed step amount since these numbers are needed before throttle is acquired
        // Nevertheless throttle % is a reasonable approximation
        // default cv setting of momentum speed change per 1% of throttle increment
        _mp = (int)(9000 * _stepRampThrottleIncrement);
        _ma = _mp;  // acceleration momentum time
        _md = _mp;  // deceleration momentum time
        if (_stepRampTimeIncrement < _mp) {
            _stepRampTimeIncrement = _mp;
        }
        if (_rosterEntry!=null) {
            String fileName = jmri.jmrit.roster.LocoFile.getFileLocation() + _rosterEntry.getFileName();
            File file;
            Element root;
            XmlFile xmlFile = new XmlFile() {};
            try {
                file = new File(fileName);
                if (file.length() == 0) {
                    return;
                }
                root = xmlFile.rootFromFile(file);
            } catch (NullPointerException npe) { 
                return;
            } catch (IOException | JDOMException eb) {
                log.error("Exception while loading warrant preferences: " + eb);
                return;
            }
            if (root == null) {
                return;
            }
            Element child = root.getChild("locomotive");
            if (child == null) {
                return;
            }
            child = child.getChild("values");
            if (child == null) {
                return;
            }
            List<Element> list = child.getChildren("CVvalue");
            int count = 0;
            for (Element cv : list) {
                Attribute attr = cv.getAttribute("name");
                if (attr != null) {
                    if (attr.getValue().equals("3")) {
                        _ma = getMomentumFactor(cv);
                        if (_stepRampTimeIncrement < _ma) {
                            _stepRampTimeIncrement = _ma;
                        }
                       count++;
                    }
                    if (attr.getValue().equals("4")) {
                        _md = getMomentumFactor(cv);
                        if (_stepRampTimeIncrement < _md) {
                            _stepRampTimeIncrement = _md;
                        }
                        count++;
                    }
                }
                if (count > 1) {
                    break;
                }
            }
            if (log.isDebugEnabled()) log.debug("makeRampParameters for {} _mp= {}ms, _ma= {}ms, _md= {}ms. rampIncr= {}",
                    _rosterId, _mp, _ma, _md, _stepRampThrottleIncrement);
        }
    }
    
    private int getMomentumFactor(Element cv) {
        Attribute attr = cv.getAttribute("value");
        if (attr != null) {
            try {
                int num = Integer.parseInt( attr.getValue());
                // even with instant speed change, allow some time for new speed to be attained
                // therefore .896 factor is ignored and 30ms added per 1% of throttle increment
                return (int) ((num + 1) * _stepRampThrottleIncrement * 896); 
            } catch (NumberFormatException nfe) {
                return _mp;
            }
        } else {
            return _mp;
        }
    }
    
    protected boolean profileHasSpeedInfo() {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        if (speedProfile == null) {
            return false;
        }
        if (speedProfile.hasForwardSpeeds() || speedProfile.hasReverseSpeeds()) {
            return true;
        }
        return false;
    }

    protected void stopRun(boolean updateSpeedProfile) {
        if (updateSpeedProfile && _sessionProfile != null) {
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            manager.setSpeedProfiles(_rosterId, _mergeProfile, _sessionProfile);
        }
        if (_throttle != null) {  // quiet
            _throttle.setF0(false);
            _throttle.setF1(false);
            _throttle.setF2(false);
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
        float speed = getTrackSpeed(_throttle.getSpeedSetting(), _throttle.getIsForward()) * _signalSpeedMap.getLayoutScale();

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

    // return true if the speed named 'speed2' is strictly greater than that of 'speed1'
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

    /**
     * Modify a throttle setting to match a speed name type
     * Modification is done according to the interpretation of the speed name
     * @param tSpeed throttle setting (current)
     * @param sType speed type name
     * @param isForward direction of travel
     * @return modified throttle setting
     */
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
                    throttleSpeed = getThrottleSettingForSpeed(signalSpeed, _throttle.getIsForward());
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
                    throttleSpeed = getThrottleSettingForSpeed(signalSpeed, _throttle.getIsForward());
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

    /**
     * Get the track speed in millimeters per millisecond (= meters/sec)
     * If SpeedProfile has no speed information an estimate is given using the WarrantPreferences
     * throttleFactor.
     * NOTE:  Call profileHasSpeedInfo() first to determine if a reliable speed is known.
     * for a given throttle setting and direction. 
     * SpeedProfile returns 0 if it has no speed information
     * @param throttleSetting throttle setting
     * @param isForward direction
     * @return track speed in millimeters/millisecond (not mm/sec)
     */
    protected float getTrackSpeed(float throttleSetting, boolean isForward) {
        if (throttleSetting <= 0.0f) {
            return 0.0f;
        }
        RosterSpeedProfile speedProfile = getSpeedProfile();
        // Note SpeedProfile uses milliseconds per second.
        float speed = speedProfile.getSpeed(throttleSetting, isForward) / 1000;            
        if (speed <= 0.0f) {
            float factor = _signalSpeedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _signalSpeedMap.getLayoutScale();
            speed = throttleSetting * factor;
            if (log.isTraceEnabled()) log.trace("getTrackSpeed for setting= {}, speed= {}, by factor= {}. train= {}",
                    throttleSetting, speed, factor, _rosterId);
        } else {
            if (log.isTraceEnabled()) log.trace("getTrackSpeed for setting= {}, speed= {}, SpeedProfile. train= {}",
                    throttleSetting, speed, _rosterId);            
        }
        return speed;
    }

    /**
     * Get the throttle setting needed to achieve a given track speed
     * track speed is mm/ms.  SpeedProfile wants mm/s
     * SpeedProfile returns 0 if it has no speed information
     * @param trackSpeed in millimeters per millisecond (m/s)
     * @param isForward direction
     * @return throttle setting or 0
     */
    protected float getThrottleSettingForSpeed(float trackSpeed, boolean isForward) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        return speedProfile.getThrottleSetting(trackSpeed * 1000, isForward);
    }

    /**
     * Get distance traveled at a constant speed. If this is called at 
     * a speed change the throttleSetting should be modified to reflect the
     * average speed over the time interval.
     * @param speedSetting Recorded (Normal) throttle setting
     * @param speedtype speed name to modify throttle setting to get modified speed
     * @param time milliseconds
     * @param isForward direction
     * @return distance in millimeters
     */
    protected float getDistanceTraveled(float speedSetting, String speedtype, float time, boolean isForward) {
        if (time <= 0) {
            return 0;
        }
        float throttleSetting = modifySpeed(speedSetting, speedtype, isForward);
        return getTrackSpeed(throttleSetting, isForward) * time;
    }

    /**
     * Get time needed to travel a distance at a constant speed.
     * @param throttleSetting Throttle setting
     * @param distance in millimeters
     * @param isForward direction
     * @return time in milliseconds
     */
    protected float getTimeForDistance(float throttleSetting, float distance, boolean isForward) {
        float speed = getTrackSpeed(throttleSetting, isForward);
        if (distance < 0 || speed <= 0) {
            return 0.0f;
        }
        return (distance/speed);
    }

    /**
     * Get ramp length needed to change speed using the WarrantPreference deltas for 
     * throttle increment and time increment.  This should only be used for ramping down.
     * @param curSetting current throttle setting
     * @param curSpeedType current speed type
     * @param toSpeedType Speed type change
     * @param isForward direction
     * @return distance in millimeters
     */
    protected float rampLengthForRampDown(float curSetting, String curSpeedType, String toSpeedType,
            boolean isForward) {
        if (curSpeedType.equals(toSpeedType)) {
            return 0.0f;
        }
        float fromSpeed = modifySpeed(curSetting, curSpeedType, isForward);
        float toSpeed = modifySpeed(curSetting, toSpeedType, isForward);
        if (toSpeed > fromSpeed) {      // insure it is ramp down regardless of speedType order
            float tmp = fromSpeed;
            fromSpeed = toSpeed;
            toSpeed = tmp;
        }
        return rampLengthForSpeedChange(fromSpeed, toSpeed, isForward);
    }

    protected float rampLengthForSpeedChange(float fromSpeed, float toSpeed, boolean isForward) {
        float rampLength = 0.0f;
        float deltaTime = getRampTimeIncrement();
        float deltaThrottle = getRampThrottleIncrement();
        int numSteps = 0;
        boolean increasing = (fromSpeed <= toSpeed);
        float momentumTime = getMomentumTime(increasing);

        if (increasing) {
            while (fromSpeed < toSpeed) {
                float dist = getTrackSpeed(fromSpeed, isForward) * momentumTime
                        + getTrackSpeed(fromSpeed + deltaThrottle, isForward) * (deltaTime - momentumTime);
                if (dist <= 0.0f) {
                    break;
                }
                fromSpeed += deltaThrottle;
                if (fromSpeed <= toSpeed) {
                    rampLength += dist;
                } else {
                    rampLength += (fromSpeed - toSpeed) * dist / deltaThrottle;
                }
                deltaThrottle *= NXFrame.INCRE_RATE;
                numSteps++;
            }
        } else {
            // Start with largest throttle increment
            float tempSpeed = toSpeed;
            while (tempSpeed + deltaThrottle <= fromSpeed) {
                tempSpeed += deltaThrottle;
                deltaThrottle *= NXFrame.INCRE_RATE;
            }
            while (fromSpeed >= toSpeed) {
                float nextSpeed;
                if (fromSpeed < deltaThrottle) {
                    nextSpeed = deltaThrottle - fromSpeed;
                } else {
                    nextSpeed = fromSpeed - deltaThrottle;
                }
                float dist = getTrackSpeed(fromSpeed, isForward) * momentumTime
                        + getTrackSpeed(nextSpeed, isForward) * (deltaTime - momentumTime);
                if (dist <= 0.0f) {
                    break;
                }
                fromSpeed -= deltaThrottle;
                if (fromSpeed > toSpeed) {
                    rampLength += dist;
                } else {
                    rampLength += (toSpeed - fromSpeed) * dist / deltaThrottle;
                }
                deltaThrottle /= NXFrame.INCRE_RATE;
                numSteps++;
            }
        }
        if (log.isDebugEnabled()) log.debug("rampLengthForSpeedChange()= {} in {}ms from speed= {} to speed= {}",
                rampLength, deltaTime*numSteps, fromSpeed, toSpeed);
        return rampLength;
    }
    
    /*************** dynamic calibration ***********************/

    long _timeAtSpeed;
    float _distanceTravelled;
    float _settingsTravelled;
    long _changetime;
    int _numchanges;

    /**
     * Just entered block at newIdx. Do that calculation of speed from lastIdx
     *  Dynamic measurement of speed profile is being studied further.  For now the
     *  only recorded speeds are those at constant speed.
     * @param lastIdx BlockOrder index of where data collection started
     * @param newIdx BlockOrder index of block just entered
     */
    protected void enteredBlock(int lastIdx, int newIdx) {
        speedChange();
        if (lastIdx > 0) {   // Distance traveled in 1st block unknown 
            if (!log.isDebugEnabled() && _numchanges > 1) {
                return;
            }
            float totalLength = 0.0f;
            boolean isForward = _throttle.getIsForward();
            boolean mergeOK = true;
            // actual exit - entry times
            if (newIdx > 1) {
                for (int i=lastIdx; i<newIdx; i++) {
                    BlockOrder blkOrder = _orders.get(i);
                    float length = blkOrder.getPath().getLengthMm();
                    if (length <= 0) {
                        log.warn("Block {} does not have a length for path {}",
                                blkOrder.getBlock().getDisplayName(), blkOrder.getPathName());
                        mergeOK = false;
                    }
                    totalLength += length;
                }                
            }
            OBlock fromBlock = _orders.get(newIdx).getBlock();
            OBlock toBlock = _orders.get(lastIdx).getBlock();
            if (!mergeOK || (_numchanges > 1 && Math.abs(_distanceTravelled - totalLength) < 25.0f)) {   // allow 1 inch
                clearStats();
                if (log.isDebugEnabled())
                    log.debug("Speed data invalid between {} and {} (bad length data)", fromBlock.getDisplayName(), toBlock.getDisplayName());
                return;
            }
            long elpsedTime = fromBlock._entryTime - toBlock._entryTime;
            float speed;
            float throttle;
            float aveSpeed = totalLength / elpsedTime;
            if (_numchanges == 1) {
                throttle = _throttle.getSpeedSetting();
                speed = aveSpeed;
            } else {
                if (Math.abs(elpsedTime - _timeAtSpeed) < 30) { // only allow 30ms
                    clearStats();
                    if (log.isDebugEnabled())
                        log.debug("Speed data invalid between {} and {} (timing bad)", fromBlock.getDisplayName(), toBlock.getDisplayName());
                    return;
                }
                speed = totalLength / _timeAtSpeed;
                throttle = _settingsTravelled / _timeAtSpeed;
            }
            speed *= 1000;   // SpeedProfile is mm/sec
            
            if (throttle < _stepIncrement || speed <= 0.0f || Math.abs(aveSpeed - speed) < 20) {
                clearStats();
                if (log.isDebugEnabled())
                    log.debug("Speeds invalid between {} and {}", fromBlock.getDisplayName(), toBlock.getDisplayName());
                return;
            }
            RosterSpeedProfile mergeProfile = getMergeProfile();
            float mergeSpeed = mergeProfile.getSpeed(throttle, isForward);                
            float profileSpeed = _sessionProfile.getSpeed(throttle, isForward);                
            throttle = _stepIncrement * Math.round(throttle/_stepIncrement);
            if (log.isDebugEnabled()) {
                log.debug("{} changes between {} and {}. ave speed= {}",
                        _numchanges, fromBlock.getDisplayName(), toBlock.getDisplayName(), aveSpeed);
                log.debug("throttle= {}, speed= {}, profileSpeed={}, mergeSpeed={}",
                        throttle, speed, profileSpeed, mergeSpeed);
            }
            if (_numchanges == 1) {
                mergeSpeed = (mergeSpeed + speed) / 2;
                if (isForward) {
                    _mergeProfile.setForwardSpeed(throttle, mergeSpeed);
                    _sessionProfile.setForwardSpeed(throttle, speed);
                } else {
                    _mergeProfile.setReverseSpeed(throttle, mergeSpeed);            
                    _sessionProfile.setReverseSpeed(throttle, speed);
                }
                if (log.isDebugEnabled()) log.debug("Set ProfileSpeed throttle= {}, sessionSpeed= {} mergeSpeed={}", throttle, speed, mergeSpeed);
            }
        }
        clearStats();
    }
    
    private void clearStats() {
        _timeAtSpeed = 0;
        _changetime = System.currentTimeMillis();
        _distanceTravelled = 0.0f;
        _settingsTravelled = 0.0f;            
        _numchanges = 0;
    }

    /*
     * 
     */
    protected void speedChange() {
        _numchanges++;
        if (!log.isDebugEnabled() && _numchanges > 1) {
            return;
        }
        long time = System.currentTimeMillis();
        float throttleSetting = _throttle.getSpeedSetting();
        long elapsedTime = time - _changetime;
        if (throttleSetting > 0.0f) {
            _timeAtSpeed += elapsedTime;
            RosterSpeedProfile speedProfile = getSpeedProfile();
            float speed = speedProfile.getSpeed(throttleSetting, _throttle.getIsForward());
            if (speed > 0.0f) {
                _distanceTravelled += elapsedTime * speed / 1000;
            }
            _settingsTravelled += throttleSetting * elapsedTime;
        }
        _changetime = time;            
    }
    
    protected float getDistanceTravelled() {
        return _distanceTravelled;            
    }
    protected void setDistanceTravelled(float dist) {
        clearStats();
        _distanceTravelled = dist;
    }

    private final static Logger log = LoggerFactory.getLogger(SpeedUtil.class);
}