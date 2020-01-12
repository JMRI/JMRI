package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
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
    private boolean _isForward = true;
    private float _rampThrottleIncrement;   // user specified throttle increment for ramping
    private int _rampTimeIncrement; // user specified time for ramp step increment

    private RosterSpeedProfile _mergeProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 
    private float _ma;  // milliseconds needed to increase speed by throttle step amount 
    private float _md;  // milliseconds needed to decrease speed by throttle step amount

    public static float SCALE_FACTOR = 110; // divided by _scale, gives a rough correction for track speed

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

    protected void setIsForward(boolean forward) {
        _isForward = forward;
    }
    
    /**
     * If one exists, return RosterEntry for the rosterId key
     * @return RosterEntry
     */
    public RosterEntry getRosterEntry() {
        if (_rosterEntry == null) {
            _rosterEntry = Roster.getDefault().getEntryForId(_rosterId);
        }
        if (_rosterEntry == null) {
            _rosterEntry = Roster.getDefault().entryFromTitle(_rosterId);
        }
        if (_rosterEntry != null) {
        	_dccAddress = _rosterEntry.getDccLocoAddress();           
        }
        if (log.isTraceEnabled()) log.debug("getRosterEntry() _rosterId= {}, _dccAddress= {}, _rosterEntry {} null}",
        		_rosterId, _dccAddress, (_rosterEntry!=null?"NOT":""));
        return _rosterEntry;
    }

    /**
     * Set the key identifier for the Speed Profile
     * If a RosterEntry exists, _rosterId is the RosterEntry id
     * or possibly is the RosterEntrytitle.
     * Otherwise it may be just the decoder address
     * @return key to speedProfile
     */
    public String getRosterId() {
        return _rosterId;
    }

    /**
     * Set a key to a loco's roster and speed info.
     * If there is no RosterEntry, the id still locates
     * a session SpeedProfile for the loco.
     * @param id key to speedProfile
     */
    public void setRosterId(String id) {
        if (log.isTraceEnabled()) log.debug("setRosterId({}) old= {}", id, _rosterId);
        if (id == null) {
        	_rosterEntry = null;
        	return;
        }
        if (!id.equals(_rosterId)) {
        	_rosterEntry = null;
            _rosterId = id;
            getRosterEntry();	// set _rosterEntry  and _dccAddress too
        }
    }
    
    public DccLocoAddress getDccAddress() {
        if (_dccAddress == null) {
            if (_rosterEntry != null) {
                _dccAddress = _rosterEntry.getDccLocoAddress();
            }
        }
        return _dccAddress;            
    }
    
    protected String getAddress() {
    	if (_dccAddress == null) {
    		_dccAddress = getDccAddress();
    	}
        if (_dccAddress != null) {
            return _dccAddress.toString();
        }
        return null;
    }
    
    protected void setDccAddress(DccLocoAddress dccAddr) {
        if (log.isTraceEnabled()) log.debug("setDccAddress(DccLocoAddress) _dccAddress= {}", _dccAddress);
        if (dccAddr == null || !dccAddr.equals(_dccAddress)) {
            _mergeProfile = null;
            _sessionProfile = null;
            _rosterId = null;
            _rosterEntry = null;
        }
        _dccAddress = dccAddr;
    }

   /**
     * Sets dccAddress and key for a speedProfile.  Will fetch RosterEntry if one exists.
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
        if (log.isTraceEnabled()) log.debug("setDccAddress(String) id= {}, _rosterId= {}", id, _rosterId);
        if (id == null || id.trim().length()==0) {
            setRosterId(null);
            _dccAddress = null;           
           return false;
        }
        if (_dccAddress !=null && id.equals(_dccAddress.toString())) {
        	return false;
        }
        setRosterId(id);	// sets _rosterEntry too
        if (_rosterEntry == null) {
        	_rosterId = null;
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
            try {
                int num = Integer.parseInt(numId);
                List<RosterEntry> l = Roster.getDefault().matchingList(null, null, numId, null, null, null, null);
                if (l.size() > 0) {
                	RosterEntry re = l.get(0);
                    if (num != 0) {
                        // In some systems, such as Maerklin MFX or ESU ECOS M4, the DCC address is always 0.
                        // That should not make us overwrite the _rosterId.
                        setRosterId(re.getId());
                    }
                    setDccAddress(re.getDccLocoAddress());           
                } else {
                    boolean isLong = true;
                    if ((index + 1) < id.length()
                            && (id.charAt(index + 1) == 'S' || id.charAt(index + 1) == 's')) {
                        isLong = false;
                    }
                    setDccAddress(new DccLocoAddress(num, isLong));
               }
            } catch (NumberFormatException e) {
            	_dccAddress = null;
            }
        }
        if (log.isTraceEnabled()) log.debug("setDccAddress: _rosterId= {}, _dccAddress= {}",_rosterId, _dccAddress);
        return true;
    }

    // Possibly customize these ramping values per warrant or loco later
    // for now use global values set in WarrantPreferences
    // user's ramp speed increase amount
    protected float getRampThrottleIncrement() {
        if (_rampThrottleIncrement <= 0) {
            _rampThrottleIncrement = WarrantPreferences.getDefault().getThrottleIncrement();
        }
        return _rampThrottleIncrement;
    }
    protected void setRampThrottleIncrement(float incr) {
    	_rampThrottleIncrement = incr;
    }

    protected int getRampTimeIncrement() {
        if (_rampTimeIncrement <= 0) {
            _rampTimeIncrement = WarrantPreferences.getDefault().getTimeIncrement();
        }
        return _rampTimeIncrement;
    }
    protected void setRampTimeIncrement(int incr) {
    	_rampTimeIncrement = incr;
    }

    /** ms momentum time to change speed for a throttle amount
     * @param delta throttle change
     * @param increasing  is acceleration
     * @return momentum time
     */
    protected float getMomentumTime(float delta, boolean increasing) {
        float incr = getThrottleSpeedStepIncrement();  // step amount
        float time;
        if (increasing) {
            time = _ma * Math.abs(delta) / incr;   // accelerating         
        } else {
            time = _md * Math.abs(delta) / incr;
        }
        if (time < 10) {
            time = 10;  // Even with CV == 0, there must be some time to move or halt
        }
        return time;
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
    protected RosterSpeedProfile getSessionProfile() {
        return _sessionProfile;
    }
    protected void resetSpeedProfile() {
        _mergeProfile = null;
    }

    private void makeSpeedTree() {
        if (_rosterId == null) {
            setDccAddress(getAddress());
        }
        if (log.isTraceEnabled()) log.debug("makeSpeedTree for {}.", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _mergeProfile = manager.getMergeProfile(_rosterId);
        _sessionProfile = manager.getSessionProfile(_rosterId);
        if (_sessionProfile == null) {
            _sessionProfile = new RosterSpeedProfile(null);
        }
        if (_mergeProfile == null) {
            _mergeProfile = new RosterSpeedProfile(_rosterEntry);   // will be an empty profile
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
        _rampTimeIncrement = getRampTimeIncrement();    // get a value if not already set
        _rampThrottleIncrement = getRampThrottleIncrement();
        // Can't use actual speed step amount since these numbers are needed before throttle is acquired
        // Nevertheless throttle % is a reasonable approximation
        // default cv setting of momentum speed change per 1% of throttle increment
        _ma = 10;  // acceleration momentum time 
        _md = 10;  // deceleration momentum time
        if (_rosterEntry!=null) {
            String fileName = Roster.getDefault().getRosterFilesLocation() + _rosterEntry.getFileName();
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
            if (_rampTimeIncrement < _ma) {
                _rampTimeIncrement = (int)_ma;
            }
            if (_rampTimeIncrement < _md) {
                _rampTimeIncrement = (int)_md;
            }
        }
        if (log.isDebugEnabled()) log.debug("makeRampParameters for {}, addr={}. _ma= {}ms/step, _md= {}ms/step. rampStepIncr= {} timeIncr= {} throttleStep= {}",
                _rosterId, getAddress(), _ma, _md, _rampThrottleIncrement, _rampTimeIncrement, getThrottleSpeedStepIncrement());
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
        if (log.isTraceEnabled()) log.debug("getMomentumFactor for cv {} {}, num= {}", 
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
        if (log.isTraceEnabled()) log.debug("getMomentumAdustment for cv {} {},  num= {}",
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
        _rampThrottleIncrement = stepIncrement * Math.round(getRampThrottleIncrement()/stepIncrement);
        if (log.isTraceEnabled()) log.debug("User's Ramp increment modified to {} ({} speed steps)",
                _rampThrottleIncrement, Math.round(_rampThrottleIncrement/stepIncrement));
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
     * @return modified throttle setting
     */
    protected float modifySpeed(float tSpeed, String sType) {
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
                float trackSpeed = getTrackSpeed(throttleSpeed);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSettingForSpeed(signalSpeed);
                    if (throttleSpeed <= 0.0f) {
                        return signalSpeed * _signalSpeedMap.getLayoutScale() / (SCALE_FACTOR *_signalSpeedMap.getDefaultThrottleFactor());
                    }
                }
                break;

            case SignalSpeedMap.SPEED_KMPH:
                signalSpeed = signalSpeed / _signalSpeedMap.getLayoutScale();
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms -> kmph
                trackSpeed = getTrackSpeed(throttleSpeed);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSettingForSpeed(signalSpeed);
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
     * @return track speed in millimeters/millisecond (not mm/sec)
     */
    protected float getTrackSpeed(float throttleSetting) {
        if (throttleSetting <= 0.0f) {
            return 0.0f;
        }
        RosterSpeedProfile speedProfile = getSpeedProfile();
        // Note SpeedProfile uses milliseconds per second.
        float speed = speedProfile.getSpeed(throttleSetting, _isForward) / 1000;            
        if (speed <= 0.0f) {
            float factor = _signalSpeedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _signalSpeedMap.getLayoutScale();
            speed = throttleSetting * factor;
        }
        return speed;
    }

    /**
     * Get the throttle setting needed to achieve a given track speed
     * track speed is mm/ms.  SpeedProfile wants mm/s
     * SpeedProfile returns 0 if it has no speed information
     * @param trackSpeed in millimeters per millisecond (m/s)
     * @return throttle setting or 0
     */
    protected float getThrottleSettingForSpeed(float trackSpeed) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        return speedProfile.getThrottleSetting(trackSpeed * 1000, _isForward);
    }

    /**
     * Get distance traveled at a constant speed. If this is called at 
     * a speed change the throttleSetting should be modified to reflect the
     * average speed over the time interval.
     * @param speedSetting Recorded (Normal) throttle setting
     * @param speedtype speed name to modify throttle setting to get modified speed
     * @param time milliseconds
     * @return distance in millimeters
     */
    protected float getDistanceTraveled(float speedSetting, String speedtype, float time) {
        if (time <= 0) {
            return 0;
        }
        float throttleSetting = modifySpeed(speedSetting, speedtype);
        return getTrackSpeed(throttleSetting) * time;
    }

    /**
     * Get time needed to travel a distance at a constant speed.
     * @param throttleSetting Throttle setting
     * @param distance in millimeters
     * @return time in milliseconds
     */
    protected float getTimeForDistance(float throttleSetting, float distance) {
        float speed = getTrackSpeed(throttleSetting);
        if (distance <= 0 || speed <= 0) {
            return 0.0f;
        }
        return (distance/speed);
    }

    /**
     * Get ramp length needed to change speed using the WarrantPreference deltas for 
     * throttle increment and time increment.  This should only be used for ramping down
     * when the warrant is interrupted for a signal or obstacle ahead.  The length is 
     * increased by 40 scale feet to allow a safety margin.
     * @param curSetting current throttle setting
     * @param curSpeedType current speed type
     * @param toSpeedType Speed type change
     * @return distance in millimeters
     */
    protected float rampLengthForRampDown(float curSetting, String curSpeedType, String toSpeedType) {
        if (curSpeedType.equals(toSpeedType)) {
            return 0.0f;
        }
        float fromSpeed = modifySpeed(curSetting, curSpeedType);
        float toSpeed = modifySpeed(curSetting, toSpeedType);
        return getRampForSpeedChange(fromSpeed, toSpeed).getRampLength()
                + 12192 / WarrantPreferences.getDefault().getLayoutScale();
    }

    /**
     * Get the length of ramp for a speed change
     * @param fromSpeed - starting speed setting
     * @param toSpeed - ending speed setting
     * @return distance in millimeters
     */
    protected RampData getRampForSpeedChange(float fromSpeed, float toSpeed) {
        RampData ramp = new RampData(getRampThrottleIncrement(), getRampTimeIncrement());
        ramp.makeThrottleSettings(fromSpeed, toSpeed);
        if (ramp.isUpRamp()) {
            makeUpRamp(ramp);
        } else {
            makeDownRamp(ramp);
        }

        if (log.isTraceEnabled()) log.debug("rampLengthForSpeedChange()= {} for fromSpeed= {} toSpeed= {}",
                ramp.getRampLength(), fromSpeed, toSpeed);
        return ramp;
    }

    private void makeUpRamp(RampData ramp) {
        float rampLength = 0.0f;
        float prevSetting = 0.0f;
        float nextSetting;
        ListIterator<Float> iter = ramp.speedIterator(true);
        if (iter.hasNext()) {
            prevSetting = iter.next().floatValue();
        }
        while (iter.hasNext()) {
            nextSetting = iter.next().floatValue();
            rampLength += getDistanceOfSpeedChange(prevSetting, nextSetting, _rampTimeIncrement);
//            if (log.isDebugEnabled()) log.debug("makeUpRamp()= {} for fromSpeed= {} toSpeed= {} dist= {}",
//                    ramp.getRampLength(), prevSetting, nextSetting, getDistanceOfSpeedChange(prevSetting, nextSetting, _rampTimeIncrement));
            prevSetting = nextSetting;
        }
        ramp.setRampLength(rampLength);
    }

    private void makeDownRamp(RampData ramp) {
        float rampLength = 0.0f;
        float prevSetting = 0.0f;
        float nextSetting;
        ListIterator<Float> iter = ramp.speedIterator(false);
        if (iter.hasPrevious()) {
            prevSetting = iter.previous().floatValue();
        }
        while (iter.hasPrevious()) {
            nextSetting = iter.previous().floatValue();
            rampLength += getDistanceOfSpeedChange(prevSetting, nextSetting, _rampTimeIncrement);
//            if (log.isDebugEnabled()) log.debug("makeDownRamp()= {} for fromSpeed= {} toSpeed= {} dist= {}",
//                    ramp.getRampLength(), prevSetting, nextSetting, getDistanceOfSpeedChange(prevSetting, nextSetting, _rampTimeIncrement));
            prevSetting = nextSetting;
        }
        ramp.setRampLength(rampLength);
    }

    /**
     * Return the distance traveled at current speed after a speed change was made.
     * Takes into account the momentum configured for the decoder to change from
     * the previous speed to the current speed.  Assumes the velocity change is linear.
     * 
     * @param prevSpeed throttle setting when speed changed to currSpeed
     * @param currSpeed throttle setting being set
     * @param speedTime elapsed time from when the speed change was made to now
     * @return distance traveled
     */
    protected float getDistanceOfSpeedChange(float prevSpeed, float currSpeed, long speedTime) {
        boolean increasing = (prevSpeed <= currSpeed);
        float momentumTime = getMomentumTime(currSpeed - prevSpeed, increasing);
        if (speedTime <=momentumTime ) {
            // most likely will be too far since currSpeed is not attained
            return getTrackSpeed((prevSpeed + currSpeed)/2) * speedTime;
        }
        // assume a linear change of speed
        float dist = getTrackSpeed((prevSpeed + currSpeed)/2) * momentumTime;
        if (speedTime > momentumTime) { // time remainder at changed speed
            dist += getTrackSpeed(currSpeed) * (speedTime - momentumTime);
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
            clearStats();
            return;
        }
        boolean isForward = _throttle.getIsForward();
        float throttle = _throttle.getSpeedSetting();   // may not be a multiple of a speed step
        BlockOrder blkOrder = _orders.get(lastIdx);
        OBlock toBlock = _orders.get(newIdx).getBlock();
        OBlock fromBlock = blkOrder.getBlock();
        if (_numchanges == 0) {
            _changetime = fromBlock._entryTime;
        }
        long elapsedTime = toBlock._entryTime - _changetime;

        // distance traveled according to current speed profile.
        _distanceTravelled += getDistanceOfSpeedChange(_prevSpeed, throttle, elapsedTime);
        // weighted speed total
        _settingsTravelled += throttle * elapsedTime;
        _timeAtSpeed += elapsedTime;

        float length = blkOrder.getPath().getLengthMm();
        boolean mergeOK = (length > 0);
        elapsedTime = toBlock._entryTime - fromBlock._entryTime;
        if (Math.abs(elapsedTime - _timeAtSpeed) > 10) {
            mergeOK = false;
        }
        float measuredSpeed = 0;
        float aveSettings = 0;
        if (mergeOK) {
            // measuredSpeed is the actual measured speed
            measuredSpeed = length / elapsedTime;
            aveSettings = _settingsTravelled / _timeAtSpeed;
            /*  if (log.isDebugEnabled()) {
                float aveSpeed = 1000 * _distanceTravelled / _timeAtSpeed;
                float profileSpeed =  getSpeedProfile().getSpeed(aveSettings, isForward);
                log.debug("{} changes on block {}: length={} exitSetting={} speed={}. calcDist={} aveThrottleSetting={} aveProfileSpeed={} calcAveSpeed={}",
                       _numchanges, fromBlock.getDisplayName(), length, throttle, measuredSpeed, _distanceTravelled, aveSettings, profileSpeed, aveSpeed);
            }*/
            // check for legitimate speed - derailing, abort, etc. can make bogus measurement.
            float ratio = getTrackSpeed(aveSettings) / measuredSpeed;
            if (ratio > 0.0f && (ratio < 0.5f || ratio > 2.0f)) {
                mergeOK = false;    // discard
            }
            measuredSpeed *= 1000;    // SpeedProfile is mm/sec
        }
        if (!mergeOK) {
            clearStats();
            return;
        }

        float stepIncrement = _throttle.getSpeedIncrement();
        aveSettings = stepIncrement * Math.round(aveSettings/stepIncrement);
        setSpeed(_sessionProfile, aveSettings, measuredSpeed, isForward);
        if (log.isDebugEnabled())  {
            int step = Math.round(aveSettings / stepIncrement);
            log.debug("Block \"{}\", train \"{}\", {} Profile setting= {} (speedStep= {}), measuredSpeed= {} {}",
                    fromBlock.getDisplayName(), _rosterId, _numchanges, aveSettings, step, measuredSpeed, (isForward?"forward":"reverse"));
        }

        setSpeed(_mergeProfile, aveSettings, measuredSpeed, isForward);
/*        throttle = stepIncrement * Math.round(throttle/stepIncrement);
        if (_numchanges == 0 || _mergeProfile.getProfileSize() <= _sessionProfile.getProfileSize()) {
            setSpeed(_mergeProfile, throttle, measuredSpeed, isForward);
        }*/
        clearStats();
    }
 
    // if a speed has been recorded, average it. Otherwise write measuredSpeed
    private void setSpeed(RosterSpeedProfile profile, float throttle, float measuredSpeed, boolean isForward) {
        RosterSpeedProfile.SpeedStep ss = profile.getSpeedStep(throttle);
        float mergeSpeed;
        if (ss != null) {
            if (isForward) {
                if (ss.getForwardSpeed() > 0f) {
                    mergeSpeed = (ss.getForwardSpeed() + measuredSpeed) / 2;
                } else {
                    mergeSpeed = measuredSpeed;
                }
            } else {
                if (ss.getReverseSpeed() > 0f) {
                    mergeSpeed = (ss.getReverseSpeed() + measuredSpeed) / 2;
                } else {
                    mergeSpeed = measuredSpeed;
                }
            }
        } else {
            mergeSpeed = measuredSpeed;
        }
        if (isForward) {
            profile.setForwardSpeed(throttle, mergeSpeed);
        } else {
            profile.setReverseSpeed(throttle, mergeSpeed);            
        }
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
        _distanceTravelled += getDistanceOfSpeedChange(_prevSpeed, throttleSetting, elapsedTime);
        _settingsTravelled += throttleSetting * elapsedTime;
        if (throttleSetting > 0.0f  ) {
            _timeAtSpeed += elapsedTime;
        }
        _changetime = time;
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
