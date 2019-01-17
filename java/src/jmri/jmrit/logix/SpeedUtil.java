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
    private float _stepRampThrottleIncrement;   // user specified throttle increment for ramping
    private int _stepRampTimeIncrement; // user specified time for ramp step increment
    private RosterSpeedProfile _mergeProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 
    private float _ma;  // milliseconds needed to increase speed by throttle step amount 
    private float _md;  // milliseconds needed to decrease speed by throttle step amount

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
       if (log.isTraceEnabled()) log.debug("setRosterId({}) _rosterId= {}", id, _rosterId);
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
    
    protected String getAddress() {
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
        if (log.isTraceEnabled()) log.debug("setDccAddress({}) _rosterId= {}", id, _rosterId);
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
        if (log.isTraceEnabled()) log.debug("setDccAddress: _rosterId= {}, _dccAddress= {}",_rosterId, _dccAddress);
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
    /** ms time to change one _stepRampThrottleIncrement amount
     * @param delta throttle change
     * @param increasing  is acceleration
     * @return momentum time
     */
    protected float getMomentumTime(float delta, boolean increasing) {
        float incr = getThrottleSpeedStepIncrement();  // step amount
        if (increasing) {
            return _ma * Math.abs(delta) / incr;   // accelerating         
        } else {
            return _md * Math.abs(delta) / incr;
        }
    }

    /**
     * throttle's minimum speed change amount
     * @return speed step amount
     */
    protected float getThrottleSpeedStepIncrement() {
        if (_throttle != null) {
            return _throttle.getSpeedIncrement();
        }
        return 1.0f / 126.0f;
    }

    protected RosterSpeedProfile getSpeedProfile() {
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

        if (log.isTraceEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to m/s= {}",
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
        _ma = 0;  // acceleration momentum time 
        _md = 0;  // deceleration momentum time
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
                        _ma += getMomentumFactor(cv);
                       count++;
                    } else if (attr.getValue().equals("4")) {
                        _md += getMomentumFactor(cv);
                        count++;
                    } else if (attr.getValue().equals("23")) {
                        _ma += getMomentumAdustment(cv);
                        count++;
                    } else if (attr.getValue().equals("24")) {
                        _md += getMomentumAdustment(cv);
                        count++;
                    }
                }
                if (count > 3) {
                    break;
                }
            }
            if (_ma < 10) {
                _ma = 10;
            }
            if (_md < 10) {
                _md = 10;
            }
            if (_stepRampTimeIncrement < _ma) {
                _stepRampTimeIncrement = (int)_ma;
            }
            if (_stepRampTimeIncrement < _md) {
                _stepRampTimeIncrement = (int)_md;
            }
            if (log.isDebugEnabled()) log.debug("makeRampParameters for {}, _ma= {}ms, _md= {}ms. throttleIncr= {} timeIncr= {}",
                    _rosterId, _ma, _md, _stepRampThrottleIncrement, _stepRampTimeIncrement);
        }
    }

    // return milliseconds per one speed step
    private float getMomentumFactor(Element cv) {
        Attribute attr = cv.getAttribute("value");
        float num = 0;
        if (attr != null) {
            try {
                 num = Integer.parseInt( attr.getValue());
                // even with instant speed change, allow some time for new speed to be attained
                // therefore add 1.  (.896 is NMRA spec)
//                return (num + 1) * 896 / 28;     // milliseconds per step
                num = (num + 1) * 896 * getThrottleSpeedStepIncrement();     // milliseconds per step
            } catch (NumberFormatException nfe) {
                num = 0;
            }
        }
        if (log.isDebugEnabled()) log.debug("getMomentumFactor for cv {} {}, num= {}", 
                cv.getAttribute("name"), attr, num);
        return num;
    }
    
    // return milliseconds per one speed step
    private float getMomentumAdustment(Element cv) {
        Attribute attr = cv.getAttribute("value");
        float num = 0;
        if (attr != null) {
            try {
                int val = Integer.parseInt(attr.getValue());
                num = val & 0x3F;  //value is 6 bits
                if ((val & 0x40) != 0) {    // 7th bit sign
                    num = -num;
                }
            } catch (NumberFormatException nfe) {
                num = 0;
            }
        }
        if (log.isDebugEnabled()) log.debug("getMomentumAdustment for cv {} {},  num= {}",
                cv.getAttribute("name"), attr, num);
        return num;
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
        if (updateSpeedProfile) {
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            manager.setSpeedProfiles(_rosterId, _mergeProfile, _sessionProfile);
        }
    }

    /************* runtime speed needs - throttle, engineer acquired ***************/

    /**
     * @param throttle set DccThrottle
     */
    protected void setThrottle( DccThrottle throttle) {
        _throttle = throttle;
        getSpeedProfile();
        // adjust user's setting to be throttle speed step settings
        float stepIncrement = _throttle.getSpeedIncrement();
        _stepRampThrottleIncrement = stepIncrement * Math.round(_stepRampThrottleIncrement/stepIncrement);
        if (log.isDebugEnabled()) log.debug("User's Ramp increment modified to {} ({} speed steps)",_stepRampThrottleIncrement, _stepRampThrottleIncrement/stepIncrement);
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
        if (log.isTraceEnabled()) log.trace("modifySpeed: from {}, to {}, signalSpeed= {}. interpretation= {}",
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
        if (distance <= 0 || speed <= 0) {
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
        return rampLengthForSpeedChange(fromSpeed, toSpeed, isForward);
    }

    /**
     * Get the length of ramp for a speed change
     * @param fSpeed - starting speed setting
     * @param toSpeed - ending speed setting
     * @param isForward - direction
     * @return distance in millimeters
     */
    protected float rampLengthForSpeedChange(float fSpeed, float toSpeed, boolean isForward) {
        float fromSpeed = fSpeed;
        float rampLength = 0.0f;
        int deltaTime = getRampTimeIncrement();
        float deltaThrottle = getRampThrottleIncrement();
        int numSteps = 0;
        boolean increasing = (fromSpeed <= toSpeed);

        if (increasing) {
            while (fromSpeed < toSpeed) {
                float dist = getDistanceFromSpeedChange(fromSpeed, fromSpeed+deltaThrottle, isForward, deltaTime);
                fromSpeed += deltaThrottle;
                if (fromSpeed <= toSpeed) {
                    rampLength += dist;
                } else {
                    rampLength += (fromSpeed - toSpeed) * dist / deltaThrottle;
                }
                deltaThrottle *= NXFrame.INCRE_RATE;
                numSteps++;
            }
        } else {    // decreasing
            // Get largest throttle increment to start
            toSpeed += deltaThrottle;
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
                float dist = getDistanceFromSpeedChange(fromSpeed, nextSpeed, isForward, deltaTime);
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
        if (log.isTraceEnabled()) log.debug("rampLengthForSpeedChange()= {} in {}ms from speed= {} to speed= {}",
                rampLength, deltaTime*numSteps, fSpeed, toSpeed);
        return rampLength;
    }

    /**
     * Return the distance traveled at current speed after a speed change was made.
     * Takes into account the momentum configured for the decoder to change from
     * the previous speed to the current speed.  Assumes the velocity change is linear.
     * 
     * @param prevSpeed throttle setting when speed changed to currSpeed
     * @param currSpeed throttle setting being set
     * @param isForward direction of decoder
     * @param speedTime elapsed time from when the speed change was made
     * @return distance traveled
     */
    protected float getDistanceFromSpeedChange(float prevSpeed, float currSpeed, boolean isForward, long speedTime) {
        boolean increasing = (prevSpeed <= currSpeed);
        float momentumTime = getMomentumTime(currSpeed - prevSpeed, increasing);
        if (speedTime <=momentumTime ) {
            // most likely will be too far since currSpeed is not attained
            return getTrackSpeed((prevSpeed + currSpeed)/2, isForward) * speedTime;
        }
        // assume a linear change of speed
        float dist = getTrackSpeed((prevSpeed + currSpeed)/2, isForward) * momentumTime;
        if (speedTime > momentumTime) { // time remainder at changed speed
            dist += getTrackSpeed(currSpeed, isForward) * (speedTime - momentumTime);
        }
        return dist;
    }
    /*************** dynamic calibration ***********************/
    long _timeAtSpeed;
    float _prevSpeed;
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
        if (lastIdx <= 0) {
            return;
        }
        boolean isForward = _throttle.getIsForward();
        float throttle = _throttle.getSpeedSetting();
        BlockOrder blkOrder = _orders.get(lastIdx);
        OBlock toBlock = _orders.get(newIdx).getBlock();
        OBlock fromBlock = blkOrder.getBlock();
        if (_changetime < fromBlock._entryTime) {
            _changetime = fromBlock._entryTime;
        }
        long elapsedTime = toBlock._entryTime - _changetime;

        // distance traveled according to current speed profile.
        _distanceTravelled += getDistanceFromSpeedChange(_prevSpeed, throttle, isForward, elapsedTime);
        // weighted speed total
        _settingsTravelled += throttle * elapsedTime;
        _timeAtSpeed += elapsedTime;

        String msg = "";
        boolean mergeOK = true;
        float length = blkOrder.getPath().getLengthMm();
        if (length <= 0) {
            if ( length <= 0) {
                msg = " length <= 0";
                mergeOK = false;
            }
        }
        float ratio = length / _distanceTravelled;
        if (ratio < .8f || ratio > 1.25f ) {
            msg.concat(" \"{}\" SpeedProfile values do not provide good distance data. "+_rosterId);
        }
        elapsedTime = toBlock._entryTime - fromBlock._entryTime;
        if (Math.abs(elapsedTime -_timeAtSpeed) > 1) {
            msg.concat(" elapsedTime=" + elapsedTime + " but _timeAtSpeed= " + _timeAtSpeed);
            mergeOK = false;
        }
        float measuredSpeed = length / elapsedTime;
        float aveSettings = _settingsTravelled / _timeAtSpeed;
        float aveSpeed = 1000 * _distanceTravelled / _timeAtSpeed;

        measuredSpeed *= 1000;   // SpeedProfile is mm/sec
        float profileSpeed =  _mergeProfile.getSpeed(aveSettings, isForward);
        if (log.isDebugEnabled()) {
            log.debug("{} changes on block {}: length={} throttle={} speed={}. _distanceTravelled={} aveSettings={} aveProfileSpeed={} calcAveSpeed={}",
                   _numchanges, fromBlock.getDisplayName(), length, throttle, measuredSpeed, _distanceTravelled, aveSettings, profileSpeed, aveSpeed);
        }
        if (!mergeOK || _numchanges > 0) {
            if (msg.length() > 0) {
                msg = "Block "+fromBlock.getDisplayName()+msg;
                log.warn(msg);
            }
            clearStats();
            return;
        }
        float stepIncrement = _throttle.getSpeedIncrement();
        throttle = stepIncrement * Math.round(throttle/stepIncrement);
        
        float mergeSpeed = _mergeProfile.getSpeed(throttle, isForward);                
        mergeSpeed = (mergeSpeed + measuredSpeed) / 2;
        int step = Math.round(throttle * 1000);
        if (isForward) {
            _mergeProfile.setForwardSpeed(throttle, mergeSpeed);
            _sessionProfile.setForwardSpeed(throttle, measuredSpeed);
        } else {
            _mergeProfile.setReverseSpeed(throttle, mergeSpeed);            
            _sessionProfile.setReverseSpeed(throttle, measuredSpeed);
        }
        if (log.isDebugEnabled())  {
            log.debug("Set Profile Speeds throttle= {} (speedStep= {}), sessionSpeed= {} mergeSpeed= {} {}",
                   throttle, step, measuredSpeed, mergeSpeed, (isForward?"forward":"reverse"));
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
     * Speed is being changed
     */
    protected void speedChange() {
        _numchanges++;
        long time = System.currentTimeMillis();
        float throttleSetting = _throttle.getSpeedSetting();
        long elapsedTime = time - _changetime;
        _distanceTravelled += getDistanceFromSpeedChange(_prevSpeed, throttleSetting, _throttle.getIsForward(), elapsedTime);
        _settingsTravelled += throttleSetting * elapsedTime;
        _changetime = time;
        if (throttleSetting > 0.0f  ) {
            _timeAtSpeed += elapsedTime;
        }
        _prevSpeed = throttleSetting;
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