package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.LocoAddress.Protocol;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.XmlFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
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

    private DccThrottle _throttle;
    private boolean _isForward = true;
    private float _rampThrottleIncrement;   // user specified throttle increment for ramping
    private int _rampTimeIncrement; // user specified time for ramp step increment

    private RosterSpeedProfile _mergeProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 
    private float _ma;  // milliseconds needed to increase speed by throttle step amount 
    private float _md;  // milliseconds needed to decrease speed by throttle step amount

    // A SCALE_FACTOR of 45 divided by _scale, computes a scale speed of 100mph at full throttle.
    // This is set arbitrarily and can be modified by the Preferences "throttle Factor".
    // Only used when there is no SpeedProfile.
    public static final float SCALE_FACTOR = 45; // divided by _scale, gives a rough approximation for track speed

    protected SpeedUtil() {
    }
    
    protected void setIsForward(boolean forward) {
        _isForward = forward;
    }
    
    /**
     * @return RosterEntry
     */
    public RosterEntry getRosterEntry() {
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
     * Called from:
     *    SpeedUtil.setDccAdress(String) - main parser
     *    WarrantFrame.setup() - edit existing warrant
     *    WarrantManagerXml - load warrant
     * @param id key to speedProfile
     */
    public void setRosterId(String id) {
        if (log.isTraceEnabled()) {
            log.debug("setRosterId({}) old={}", id, _rosterId);
        }
        if (id == null || id.isEmpty()) {
            _rosterEntry = null;
            _mergeProfile = null;
            _sessionProfile = null;
            return;
        }
        if (!id.equals(_rosterId)) {
            _mergeProfile = null;
            _sessionProfile = null;
            RosterEntry re = Roster.getDefault().getEntryForId(id);
            if (re != null) {
                _rosterEntry = re;
                setDccAddress(Integer.parseInt(re.getDccAddress()), re.getProtocolAsString());
                _rosterId = id;
            }
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

    /**
     * Called by:
     * Warrant.setRunMode() about to run a warrant
     * WarrantFrame.setup() for an existing warrant
     * WarrantTableModel.cloneWarrant() when cloning an existing warrant
     * 
     * @param dccAddr DccLocoAddress
     */
    protected void setDccAddress(DccLocoAddress dccAddr) {
        if (log.isTraceEnabled()) log.debug("setDccAddress(DccLocoAddress) _dccAddress= {}", _dccAddress);
        if (dccAddr == null) {
            _mergeProfile = null;
            _sessionProfile = null;
            _rosterId = null;
            _rosterEntry = null;
            _dccAddress = null;
            return;
        }
        if (!dccAddr.equals(_dccAddress)) {
            _mergeProfile = null;
            _sessionProfile = null;
            _dccAddress = dccAddr;
        }
    }

    public boolean setDccAddress(int number, String type) {
        if (log.isTraceEnabled()) {
            log.debug("setDccAddress({}, {})", number, type);
        }
        LocoAddress.Protocol protocol;
        if (type.equals("L") || type.equals("l")) {
            protocol = LocoAddress.Protocol.DCC_LONG;
        } else if (type.equals("S") || type.equals("s")) {
            protocol = LocoAddress.Protocol.DCC_SHORT;
        } else {
            try {
                protocol = Protocol.getByPeopleName(type);                
            } catch (IllegalArgumentException iae) {
                try {
                    type = type.toLowerCase();
                    protocol = Protocol.getByShortName(type);                
                } catch (IllegalArgumentException e) {
                    _dccAddress = null;
                    return false;
                }
            }
        }
        DccLocoAddress dccAddress = new DccLocoAddress(number, protocol);
        if (_dccAddress == null || !_dccAddress.equals(dccAddress) || _rosterEntry == null) {
            _dccAddress = dccAddress;
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null,
                    String.valueOf(number), null, null, null, null);
            if (!l.isEmpty()) {
                _rosterEntry = l.get(0);
                setRosterId(_rosterEntry.getId());
                if (l.size() != 1) {
                    log.info("{} entries for address {}, {}", l.size(), number, type);
                }
            } else {
                // DCC address is set, but there is not a Roster entry for it
                _rosterId = "$"+_dccAddress.toString()+"$";
                _rosterEntry = new RosterEntry();
                _rosterEntry.setId(_rosterId);
                _mergeProfile = null;
                _sessionProfile = null;
            }
            return true;
        }
        return false;
    }


    /**
     * Sets dccAddress and key for a speedProfile.  Will fetch RosterEntry if one exists.
     * If _rosterEntry exists, _rosterId set to RosterEntry Id (which may or not be "id")
     * else _rosterId set to "id" or decoder address.
     * Called from:
     *    DefaultConditional.takeActionIfNeeded() - execute a setDccAddress action
     *    SpeedUtil.makeSpeedTree() - need to use track speeds
     *    WarrantFrame.checkTrainId() - about to run, assures address is set 
     *    Warrantroute.getRoster() - selection form _rosterBox
     *    WarrantRoute.setAddress() - whatever is in _dccNumBox.getText()
     *    WarrantRoute.setTrainPanel() - whatever in _dccNumBox.getText()
     *    WarrantTableModel.setValue() - whatever address is put into the ADDRESS_COLUMN
     * @param id address as a String, either RosterEntryTitle or decoder address
     * @return true if address found for id
     */
    public boolean setAddress(String id) {
        if (log.isTraceEnabled()) {
            log.debug("setDccAddress: id= {}, _rosterId= {}", id, _rosterId);
        }
        if (id == null || id.isEmpty()) {
            setDccAddress(null);
            return false;
        }
        int index = - 1;
        for (int i=0; i<id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                index = i;
                break;
            }
        }
        String numId;
        String type;
        if (index == -1) {
            numId = id;
            type = null;
        } else {
            int beginIdx;
            int endIdx;
            if (id.charAt(index) == '(') {
                beginIdx = index + 1;
            } else {
                beginIdx = index;
            }
            if (id.charAt(id.length() - 1) == ')') {
                endIdx = id.length() - 1;
            } else {
                endIdx = id.length();
            }
            numId = id.substring(0, index);
            type = id.substring(beginIdx, endIdx);
        }

        int num;
        try {
            num = Integer.parseInt(numId);
            if (type == null) {
                if (num > 128) {
                    type = "L";
                } else {
                    type = "S";
                }
            }
        } catch (NumberFormatException e) {
            num = 0;
        }
        if (!setDccAddress(num, type)) {
            log.error("setDccAddress failed for  number={} type={}", num, type);
            return false;
        } else if (log.isTraceEnabled()) {
            log.debug("setDccAddress({}): _rosterId= {}, _dccAddress= {}",
                    id, _rosterId, _dccAddress.toString());
        }
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

    protected RosterSpeedProfile getMergeProfile() {
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
        if (log.isTraceEnabled()) log.debug("makeSpeedTree for {}.", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _mergeProfile = manager.getMergeProfile(_rosterId);
        _sessionProfile = manager.getSessionProfile(_rosterId);
        manager.setSpeedProfiles(_rosterId, _mergeProfile, _sessionProfile);
        _signalSpeedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);

        if (log.isTraceEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to mm/s= {}",
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
                log.error("Exception while loading warrant preferences: {}",eb);
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
        RosterSpeedProfile speedProfile = getMergeProfile();
        if (speedProfile == null) {
            return false;
        }
        return (speedProfile.hasForwardSpeeds() || speedProfile.hasReverseSpeeds());
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
        getMergeProfile();
        // adjust user's setting to be throttle speed step settings
        float stepIncrement = _throttle.getSpeedIncrement();
        _rampThrottleIncrement = stepIncrement * Math.round(getRampThrottleIncrement()/stepIncrement);
        if (log.isTraceEnabled()) log.debug("User's Ramp increment modified to {} ({} speed steps)",
                _rampThrottleIncrement, Math.round(_rampThrottleIncrement/stepIncrement));
    }

    protected DccThrottle getThrottle() {
        return _throttle;
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
        log.trace("modifySpeed speed= {} for SpeedType= \"{}\"", tSpeed, sType);
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
                }
                break;

            case SignalSpeedMap.SPEED_KMPH:
                signalSpeed = signalSpeed / _signalSpeedMap.getLayoutScale();
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms -> kmph
                trackSpeed = getTrackSpeed(throttleSpeed);
                if (signalSpeed < trackSpeed) {
                    throttleSpeed = getThrottleSettingForSpeed(signalSpeed);
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
        RosterSpeedProfile speedProfile = getMergeProfile();
        // Note SpeedProfile uses millimeters per second.
        float speed = speedProfile.getSpeed(throttleSetting, _isForward) / 1000;            
        if (speed <= 0.0f) {
            speed = speedProfile.getSpeed(throttleSetting, !_isForward) / 1000;
        }
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
        RosterSpeedProfile speedProfile = getMergeProfile();
        float throttleSpeed = speedProfile.getThrottleSetting(trackSpeed * 1000, _isForward);
        if (throttleSpeed <= 0.0f) {
            throttleSpeed =  trackSpeed * _signalSpeedMap.getLayoutScale() / (SCALE_FACTOR *_signalSpeedMap.getDefaultThrottleFactor());
        }
        return throttleSpeed;
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
        if (currSpeed < 0) {
            currSpeed = 0;
        }
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
    int _numchanges;            // number of time changes within the block

    /**
     * Just entered a new block at 'toTime'. Do the calculation of speed of the
     * previous block from when the previous block block was entered.
     * 
     * Throttle changes within the block will cause different speeds.  We attempt
     * to accumulate these time and distances to calculate a weighted speed average.
     * See method speedChange() below.
     *  Dynamic measurement of speed profile is being studied further.  For now the
     *  only recorded speeds are those at constant speed. i.e. weighted averages not
     *  used
     * @param block went active
     * @param length distance traveled. (from user's input of path lengths
     */
    protected void enteredBlock(OBlock block, float length) {
        long exitTime = System.currentTimeMillis();
        boolean isForward = _throttle.getIsForward();
        float throttle = _throttle.getSpeedSetting();   // may not be a multiple of a speed step
        long elapsedTime = exitTime - _changetime;

        // distance traveled according to current speed profile.
        _distanceTravelled += getDistanceOfSpeedChange(_prevSpeed, throttle, elapsedTime);
        // weighted speed total
        _settingsTravelled += throttle * elapsedTime;
        _timeAtSpeed += elapsedTime;
        log.debug("enteredBlock= {}, elapsedTime={}ms, distanceTravelled= {}, settingsTravelled={}, timeAtSpeed={}, length= {}mm", 
                block.getDisplayName(), elapsedTime, _distanceTravelled, _settingsTravelled, _timeAtSpeed, length);

        float measuredSpeed = 0;
        float aveSettings = 0;
        if (length <= 0) {
            measuredSpeed = _distanceTravelled / _timeAtSpeed;
        } else {
            measuredSpeed = length / _timeAtSpeed;
        }
        measuredSpeed *= 1000;    // SpeedProfile is mm/sec
        aveSettings = _settingsTravelled / _timeAtSpeed;
        float profileSpeed =  getMergeProfile().getSpeed(aveSettings, isForward);
        if (log.isDebugEnabled()) {
            float aveSpeed = 1000 * _distanceTravelled / _timeAtSpeed;
            log.debug("{} changes dist Path={}mm CalcDist={}mm curThrottle={}. aveThrottle={},  speed={}, aveProfileSpeed={} aveCalcSpeed={}",
                    _numchanges, Math.round(length), Math.round(_distanceTravelled), throttle, aveSettings, 
                    measuredSpeed,  profileSpeed, aveSpeed);
        }
        clearStats();
        _changetime = exitTime;
        setSpeedProfile(_sessionProfile, aveSettings, measuredSpeed, isForward);   // post session result, regardless
        setSpeedProfile(_mergeProfile, aveSettings, measuredSpeed, isForward);
    }
 
    // if a speed has been recorded, average it. Otherwise write measuredSpeed
    private void setSpeedProfile(RosterSpeedProfile profile, float throttle, float measuredSpeed, boolean isForward) {
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
    
    protected void clearStats() {
        _timeAtSpeed = 0;
        _changetime = System.currentTimeMillis();
        _distanceTravelled = 0.0f;
        _settingsTravelled = 0.0f;            
        _numchanges = 0;
    }

    /*
     * Speed about to be changed. 
     * The engineer makes this notification >BEFORE< setting a new speed
     */
    protected void speedChange() {
        _numchanges++;
        long time = System.currentTimeMillis();
        float throttleSetting = _throttle.getSpeedSetting();
        if (throttleSetting < 0) {
            throttleSetting = 0;
        }
        long elapsedTime = time - _changetime;
        _distanceTravelled += getDistanceOfSpeedChange(_prevSpeed, throttleSetting, elapsedTime);
        _settingsTravelled += throttleSetting * elapsedTime;
        log.debug("speedChange: _settingTravelled={}, et={}, sum={}", 
                (throttleSetting * elapsedTime), elapsedTime, _settingsTravelled);
        if (throttleSetting > 0.0f || _prevSpeed > 0.0f) {
            _timeAtSpeed += elapsedTime;
        }
        _changetime = time;
        _prevSpeed = throttleSetting;
    }
    
    protected void setDistanceTravelled(float dist) {
        clearStats();
        _distanceTravelled = dist;
    }

    private static final Logger log = LoggerFactory.getLogger(SpeedUtil.class);
}
