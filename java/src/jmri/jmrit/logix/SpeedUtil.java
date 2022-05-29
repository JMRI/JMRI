package jmri.jmrit.logix;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.LocoAddress.Protocol;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.XmlFile;
import jmri.jmrit.logix.ThrottleSetting.Command;
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

    private DccThrottle _throttle;
    private boolean _isForward = true;
    private float _rampThrottleIncrement;   // user specified throttle increment for ramping
    private int _rampTimeIncrement; // user specified time for ramp step increment
    private float _speedIncrement;  // throttle's minimum speed step

    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private boolean _noProfile =  true;
    private SignalSpeedMap _signalSpeedMap;
    private float _ma;  // milliseconds needed to increase speed by throttle step amount
    private float _md;  // milliseconds needed to decrease speed by throttle step amount
    private ArrayList<BlockSpeedInfo> _speedInfo; // map max speeds and occupation times of each block in route

    // A SCALE_FACTOR of 45 divided by _scale, computes a scale speed of 100mph at full throttle.
    // This is set arbitrarily and can be modified by the Preferences "throttle Factor".
    // Only used when there is no SpeedProfile.
    public static final float SCALE_FACTOR = 45; // divided by _scale, gives a rough approximation for track speed
    public static final float MAX_TGV_SPEED = 88889;   // maximum speed of a Bullet train (320 km/hr) in millimeters/sec

    protected SpeedUtil() {
        _signalSpeedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
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
     * @return true if RosterEntry exists for id
     */
    public boolean setRosterId(String id) {
        if (log.isTraceEnabled()) {
            log.debug("setRosterId({}) old={}", id, _rosterId);
        }
        if (id == null || id.isEmpty()) {
            _rosterEntry = null;
            _sessionProfile = null;
            return false;
        }
        if (id.equals(_rosterId)) {
            return true;
        } else {
            _sessionProfile = null;
            RosterEntry re = Roster.getDefault().getEntryForId(id);
            if (re != null) {
                _rosterEntry = re;
                _dccAddress = re.getDccLocoAddress();
                _rosterId = id;
                return true;
            }
        }
        return false;
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
            _sessionProfile = null;
            _rosterId = null;
            _rosterEntry = null;
            _dccAddress = null;
            return;
        }
        if (!dccAddr.equals(_dccAddress)) {
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
        DccLocoAddress addr = new DccLocoAddress(number, protocol);
        if (_rosterEntry != null && addr.equals(_rosterEntry.getDccLocoAddress())) {
            return true;
        } else {
            _dccAddress = addr;
            String numStr = String.valueOf(number);
            List<RosterEntry> l = Roster.getDefault().matchingList(null, null,
                    numStr, null, null, null, null);
            if (!l.isEmpty()) {
                int size = l.size();
                if ( size!= 1) {
                    log.info("{} entries for address {}, {}", l.size(), number, type);
                }
                _rosterEntry = l.get(size - 1);
                setRosterId(_rosterEntry.getId());
            } else {
                // DCC address is set, but there is not a Roster entry for it
                _rosterId = "$"+_dccAddress.toString()+"$";
                makeRosterEntry(_rosterId);
                _sessionProfile = null;
            }
        }
        return true;
    }

    protected RosterEntry makeRosterEntry(String id) {
        RosterEntry rosterEntry = new RosterEntry();
        rosterEntry.setId(id);
        DccLocoAddress dccAddr = getDccAddress();
        rosterEntry.setDccAddress(String.valueOf(dccAddr.getNumber()));
        rosterEntry.setProtocol(dccAddr.getProtocol());
        rosterEntry.ensureFilenameExists();
        return rosterEntry;
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
            return false;
        }
        if (setRosterId(id)) {
            return true;
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
        } catch (NumberFormatException e) {
            num = 0;
        }
        if (type == null) {
            if (num > 128) {
                type = "L";
            } else {
                type = "S";
            }
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
        if (time < 50) {
            time = 50;  // Even with CV == 0, there must be some time to change speed
        }
        return time;
    }

    /**
     * throttle's minimum speed change amount
     * @return speed step amount
     */
    protected float getThrottleSpeedStepIncrement() {
        if (_throttle != null) {
            _speedIncrement = _throttle.getSpeedIncrement();
            return _speedIncrement;
        }
        if (_speedIncrement > .001f) {
            return _speedIncrement;
        }
        return 1.0f / 126.0f;
    }

    // treeMap implementation in _mergeProfile is not synchronized
    synchronized protected RosterSpeedProfile getMergeProfile() {
        if (_sessionProfile == null) {
            makeSpeedTree();
            makeRampParameters();
        }
        return _sessionProfile;
    }

    synchronized private void makeSpeedTree() {
        if (log.isTraceEnabled()) log.debug("makeSpeedTree for {}.", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _sessionProfile = manager.getMergeProfile(_rosterId);
        if (_sessionProfile == null) {
            _rosterEntry = Roster.getDefault().getEntryForId(_rosterId);
            RosterSpeedProfile profile;
            if (_rosterEntry == null) {
                _noProfile =  true;
                _rosterEntry = makeRosterEntry(_rosterId);
                profile = new RosterSpeedProfile(_rosterEntry);
            } else {
                _noProfile =  false;
                profile = _rosterEntry.getSpeedProfile();
                if (profile == null) {
                    profile = new RosterSpeedProfile(_rosterEntry);
                    _rosterEntry.setSpeedProfile(profile);
                }
            }
            _sessionProfile = manager.makeProfileCopy(profile, _rosterEntry);
            manager.setMergeProfile(_rosterId, _sessionProfile);
        }

        if (log.isTraceEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to mm/s= {}",
                _signalSpeedMap.getDefaultThrottleFactor(), _signalSpeedMap.getLayoutScale(),
                _signalSpeedMap.getDefaultThrottleFactor() * _signalSpeedMap.getLayoutScale() / SCALE_FACTOR);
    }

    private void makeRampParameters() {
        _rampTimeIncrement = getRampTimeIncrement();    // get a value if not already set
        _rampThrottleIncrement = getRampThrottleIncrement();
        // default cv setting of momentum speed change per 1% of throttle increment
        _ma = 20;  // time needed to accelerate one throttle speed step
        _md = 20;  // time needed to decelerate one throttle speed step
        if (_rosterEntry!=null) {
            String fileName = Roster.getDefault().getRosterFilesLocation() + _rosterEntry.getFileName();
            Element elem;
            XmlFile xmlFile = new XmlFile() {};
            try {
                elem = xmlFile.rootFromFile(new File(fileName));
            } catch (FileNotFoundException npe) {
                elem = null;
            } catch (IOException | JDOMException eb) {
                log.error("Exception while loading warrant preferences",eb);
                elem = null;
            }
            if (elem != null) {
                elem = elem.getChild("locomotive");
            }
            if (elem != null) {
                elem = elem.getChild("values");
            }
            if (elem != null) {
                List<Element> list = elem.getChildren("CVvalue");
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
            }
        }
        // changing speed must take some amount of time
        if (_ma < 60) {
            _ma = 60;
        }
        if (_md < 40) {
            _md = 40;
        }
        if (_rampTimeIncrement < _ma || _rampTimeIncrement < _md) {
            _rampTimeIncrement = (int)_ma;
        }
        if (log.isDebugEnabled()) {
            log.debug("makeRampParameters for {}, addr={}. _ma= {}ms/step, _md= {}ms/step. rampStepIncr= {} timeIncr= {} throttleStep= {}",
                    _rosterId, getAddress(), _ma, _md, _rampThrottleIncrement, _rampTimeIncrement, getThrottleSpeedStepIncrement());
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
/*
    private void mergeEntries(Entry<Integer, SpeedStep> sEntry, Entry<Integer, SpeedStep> mEntry) {
        SpeedStep sStep = sEntry.getValue();
        SpeedStep mStep = mEntry.getValue();
        float sTrackSpeed = sStep.getForwardSpeed();
        float mTrackSpeed = mStep.getForwardSpeed();
        if (sTrackSpeed > 0) {
            if (mTrackSpeed > 0) {
                mTrackSpeed = (mTrackSpeed + sTrackSpeed) / 2;
            } else {
                mTrackSpeed = sTrackSpeed;
            }
            mStep.setForwardSpeed(mTrackSpeed);
        }
        sTrackSpeed = sStep.getReverseSpeed();
        mTrackSpeed = mStep.getReverseSpeed();
        if (sTrackSpeed > 0) {
            if (sTrackSpeed > 0) {
                if (mTrackSpeed > 0) {
                    mTrackSpeed = (mTrackSpeed + sTrackSpeed) / 2;
                } else {
                    mTrackSpeed = sTrackSpeed;
                }
            }
            mStep.setReverseSpeed(mTrackSpeed);
        }
    }*/

    synchronized protected void mergeSpeedProfile() {
//        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
//        manager.setMergeProfile(_rosterId, _sessionProfile);
    }

    protected void setIsForward(boolean direction) {
        _isForward = direction;
    }

    protected boolean getIsForward() {
        if (_throttle != null) {
            _isForward = _throttle.getIsForward();
        }
        return _isForward;
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
        if (speed1.equals(speed2)) {
            return false;
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
                signalSpeed = signalSpeed / 3.6f;  // layout track speed mm/ms -> km/hr
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
     * A a train's speed at a given throttle setting and time would travel a distance.
     * return the time it would take for the train at another throttle setting to
     * travel the same distance.
     * @param speed a given throttle setting
     * @param time a given time
     * @param modifiedSpeed a different speed setting
     * @return the time to travel the same distance at the different setting
     */
    static protected long modifyTime(float speed, long time, float modifiedSpeed) {
        if (Math.abs(speed - modifiedSpeed) > .0001f) {
            return (long)((speed / modifiedSpeed) * time);
        } else {
            return time;
        }
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
        if (_dccAddress == null || _noProfile) {
            return factorSpeed(throttleSetting);
        }
        boolean isForward = getIsForward();
        // Note SpeedProfile uses millimeters per second.
        float speed = _sessionProfile.getSpeed(throttleSetting, isForward) / 1000;
        if (speed <= 0.0f) {
            speed = _sessionProfile.getSpeed(throttleSetting, !isForward) / 1000;
        }
        if (speed <= 0.0f) {
            return factorSpeed(throttleSetting);
        }
        return speed;
    }


    private float factorSpeed(float throttleSetting) {
        float factor = _signalSpeedMap.getDefaultThrottleFactor() * SCALE_FACTOR / _signalSpeedMap.getLayoutScale();
        return throttleSetting * factor;
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
        float throttleSpeed = speedProfile.getThrottleSetting(trackSpeed * 1000, getIsForward());
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

    /*************** Block Speed Info *****************/
    /**
     * build map of BlockSpeedInfo's for the route. Map corresponds to list
     * of BlockOrders of a Warrant
     * @param commands list of script commands
     * @param orders list of BlockOrders
     */
    protected void getBlockSpeedTimes(List<ThrottleSetting> commands, List<BlockOrder> orders) {
        _speedInfo = new ArrayList<BlockSpeedInfo>();
        float firstSpeed = 0.0f; // used for entrance
        float speed = 0.0f;
        float intStartSpeed = 0.0f;
        float intEndSpeed = 0.0f;
        long blkTime = 0;
        float pathDist = 0;
        float calcDist = 0;
        int firstIdx = 0; // for all blocks except first, this is index of NOOP command
        int blkOrderIdx = 0;
        ThrottleSetting ts = commands.get(0);
        OBlock blk = (OBlock)ts.getNamedBeanHandle().getBean();
        String blkName = blk.getDisplayName();
        for (int i = 0; i < commands.size(); i++) {
            ts = commands.get(i);
            ThrottleSetting.CommandValue cmdVal = ts.getValue();
            long time = ts.getTime();
            blkTime += time;
            float dist = getDistanceOfSpeedChange(intStartSpeed, intEndSpeed, time);
//            log.debug("block: {} Cmd#{} Dist= {} in {}ms from {} to {}", blkName, i+1, dist, time, intStartSpeed, intEndSpeed);
            calcDist += dist;
            if (cmdVal.getType() == ThrottleSetting.ValueType.VAL_FLOAT) {
                speed = cmdVal.getFloat();
                if (speed <= 0) {
                    intStartSpeed = 0;
                    intEndSpeed = 0;
                }else {
                    intStartSpeed = intEndSpeed;
                    intEndSpeed = speed;
                }
            }
            if (ts.getCommand().equals(Command.NOOP)) {
                // make map entry
                boolean trace = false;
                if (calcDist<= 0) {
                    log.warn("block: {} Path distance or SpeedProfile unreliable! pathDist= {}, calcDist={}!", blkName, pathDist, calcDist);
                    trace = true;
                }
                if (blkOrderIdx > 0 && blkOrderIdx < commands.size() - 1) {
                    pathDist = orders.get(blkOrderIdx).getPathLength();
                    float ratio = pathDist / calcDist;
                    if (Math.abs(ratio) > 2.0f || Math.abs(ratio) < 0.5f) {
                        log.warn("block: {} Path distance or SpeedProfile unreliable! pathDist= {}, calcDist={}!", blkName, pathDist, calcDist);
                        trace = true;
                    }
                } else {
                    BlockOrder bo = orders.get(blkOrderIdx);
                    pathDist = bo.getPathLength() / 2;
                }
                _speedInfo.add(new BlockSpeedInfo(blkName, firstSpeed, speed, blkTime, pathDist, calcDist, firstIdx, i));
                if (trace || log.isDebugEnabled()) {
                   log.debug("block: {} speeds: entrance= {}, exit= {}. time= {}ms, pathDist= {}, calcDist= {}. index {} to {}",
                            blkName, firstSpeed, speed, blkTime, pathDist, calcDist, firstIdx, i);
                }
                blkOrderIdx++;
                blk = (OBlock)ts.getNamedBeanHandle().getBean();
                blkName = blk.getDisplayName();
                blkTime = 0;
                calcDist = 0;
                firstSpeed = speed;
                firstIdx = i + 1; // first in next block is next index
            }
            // set up recording track speeds
        }
        _speedInfo.add(new BlockSpeedInfo(blkName, firstSpeed, speed, blkTime, pathDist, calcDist, firstIdx, commands.size() - 1));
        if (log.isDebugEnabled()) {
            log.debug("block: {} speeds: entrance= {}, exit= {}. time= {}ms pathDist= {}, calcDist= {}. index {} to {}",
                    blkName, firstSpeed, speed, blkTime, pathDist, calcDist, firstIdx, (commands.size() - 1));
        }
        clearStats();
        _intStartSpeed = 0;
        _intEndSpeed = 0;
        _prevChangeTime = -1;
    }

    protected BlockSpeedInfo getBlockSpeedInfo(int idxBlockOrder) {
        return _speedInfo.get(idxBlockOrder);
    }

    /**
     * Get the ramp for a speed change
     * @param fromSpeed - starting speed setting
     * @param toSpeed - ending speed setting
     * @return ramp data
     */
    protected RampData getRampForSpeedChange(float fromSpeed, float toSpeed) {
        RampData ramp = new RampData(this, getRampThrottleIncrement(), getRampTimeIncrement(), fromSpeed, toSpeed);
        return ramp;
    }

    protected float getRampLengthForEntry(float currentSpeed, float endSpeed) {
        RampData ramp = getRampForSpeedChange(currentSpeed, endSpeed);
        float enterLen = ramp.getRampLength();
        if (log.isTraceEnabled()) {
            log.debug("getRampLengthForEntry: from speed={} to speed={}. rampLen={}",
                    currentSpeed, endSpeed, enterLen);
        }
        return enterLen;
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
        if (prevSpeed < 0) {
            prevSpeed = 0;
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
    private long _timeAtSpeed = 0;
    private float _intStartSpeed = 0.0f;
    private float _intEndSpeed = 0.0f;
//    private float _prevSpeed = 0;
    private float _distanceTravelled = 0;
    private float _settingsTravelled = 0;
    private long _prevChangeTime = -1;
    private int _numchanges = 0;        // number of time changes within the block
    private long _entertime = 0;        // entrance time to block
    private long _timeOffset = 0;
//    private boolean _halted = false;    // speed has at 0 at some time while in the block

    protected void enterTimeOffset(long time) {
        _timeOffset = time;
    }

    /**
     * Just entered a new block at 'toTime'. Do the calculation of speed of the
     * previous block from when the previous block block was entered.
     *
     * Throttle changes within the block will cause different speeds.  We attempt
     * to accumulate these time and distances to calculate a weighted speed average.
     * See method speedChange() below.
     * @param blkIdx BlockOrder index of the block the engine just left. (not train)
     * The lead engine just entered the next block after blkIdx.
     */
    protected void leavingBlock(int blkIdx) {
        long exitTime = System.currentTimeMillis();
        boolean isForward = getIsForward();
        float throttle = _throttle.getSpeedSetting();   // may not be a multiple of a speed step
        BlockSpeedInfo blkInfo = getBlockSpeedInfo(blkIdx);
        float length = blkInfo.getPathLen();
        if (_numchanges == 0) {
            long elapsedTime = exitTime - _prevChangeTime;
            _distanceTravelled = getTrackSpeed(throttle) * elapsedTime;
            _settingsTravelled = throttle * elapsedTime;
            _timeAtSpeed = elapsedTime;            
        } else {
            long elapsedTime = exitTime - _prevChangeTime;
            float dist = getDistanceOfSpeedChange(_intStartSpeed, _intEndSpeed, elapsedTime);
            if (_intStartSpeed > 0 || _intEndSpeed > 0) {
                _timeAtSpeed += elapsedTime;
            }
            if (log.isDebugEnabled()) {
                log.debug("speedChange to {}: dist={} in {}ms from speed {} to {}.",
                        throttle, dist, elapsedTime, _intStartSpeed, _intEndSpeed);
            }
            _distanceTravelled += dist;
            _settingsTravelled += throttle * elapsedTime;
        }

        float measuredSpeed = 0;
        float distRatio;
        if (length <= 0) {
            // Origin and Destination block lengths immaterial
            measuredSpeed = _distanceTravelled / _timeAtSpeed;
            distRatio = 2;    // actual start and end positions unknown
        } else {
            measuredSpeed = length / _timeAtSpeed;
            distRatio = blkInfo.getCalcLen()/_distanceTravelled;
        }
        measuredSpeed *= 1000;    // SpeedProfile is mm/sec
        float aveSettings = _settingsTravelled / _timeAtSpeed;
        if (log.isDebugEnabled()) {
            log.debug("Block: {}", blkInfo );
            float timeRatio = (exitTime - _entertime + _timeOffset) / (float)_timeAtSpeed;
            log.debug("distRatio= {}, timeRatio= {}, _timeOffset= {}, aveSpeed= {}, length= {}, calcLength= {}, elapsedTime= {}", 
                    distRatio, timeRatio, _timeOffset, measuredSpeed, length, _distanceTravelled, (exitTime - _entertime));
        }
        if (aveSettings > 1.0 || measuredSpeed > MAX_TGV_SPEED*aveSettings/_signalSpeedMap.getLayoutScale()
                || distRatio > 1.15f || distRatio < 0.87f) {
            if (log.isDebugEnabled()) {
                // We assume bullet train's speed is linear from 0 throttle to max throttle.
                // we also tolerate distance calculation errors up to 20% longer or shorter
                log.info("Bad speed measurements data for block {}. aveThrottle= {},  measuredSpeed= {},(TGVmax= {}), distTravelled= {}, pathLen= {}",
                        blkInfo.getBlockDisplayName(), aveSettings,  measuredSpeed, MAX_TGV_SPEED*aveSettings/_signalSpeedMap.getLayoutScale(),
                        _distanceTravelled, length);
            }
        } else /*if (_numchanges < 1)*/ {
            setSpeedProfile(_sessionProfile, aveSettings, measuredSpeed, isForward);
        }
        if (log.isDebugEnabled()) {
            log.debug("{} changes in block \'{}\". measuredDist={}, pathLen={}, measuredThrottle={},  measuredTrkSpd={}, profileTrkSpd={} curThrottle={}.",
                    _numchanges, blkInfo.getBlockDisplayName(), Math.round(_distanceTravelled), length,
                    aveSettings, measuredSpeed, getTrackSpeed(aveSettings)*1000, throttle);
        }
        clearStats();
        _prevChangeTime = exitTime;
        _entertime = exitTime;   // entry of next block
        _timeOffset = 0;
    }

    private void setSpeedProfile(RosterSpeedProfile profile, float throttle, float measuredSpeed, boolean isForward) {
        int keyIncrement = Math.round(getThrottleSpeedStepIncrement() * 1000);
        TreeMap<Integer, SpeedStep> speeds = profile.getProfileSpeeds();
        int key = Math.round(throttle * 1000);
        Entry<Integer, SpeedStep> entry = speeds.floorEntry(key);
        if (entry != null) {
            if (mergeEntry(key, measuredSpeed, entry, keyIncrement, isForward)) {
                return;
            }
        }
        entry = speeds.ceilingEntry(key);
        if (entry != null) {
            if (mergeEntry(key, measuredSpeed, entry, keyIncrement, isForward)) {
                return;
            }
        }
        if (isForward) {
            profile.setForwardSpeed(throttle, measuredSpeed, _throttle.getSpeedIncrement());
        } else {
            profile.setReverseSpeed(throttle, measuredSpeed, _throttle.getSpeedIncrement());
        }
        if (log.isDebugEnabled()) {
            log.debug("Put measuredThrottle={} and measuredTrkSpd={} for isForward= {} curThrottle={}.",
                    throttle, measuredSpeed, isForward, throttle);
        }
    }

    private boolean mergeEntry(int key, float measuredSpeed, Entry<Integer, SpeedStep> entry, int keyIncrement, boolean isForward) {
        Integer sKey = entry.getKey();
        if (Math.abs(sKey - key) < keyIncrement) {
            SpeedStep sStep = entry.getValue();
            float sTrackSpeed;
            if (isForward) {
                sTrackSpeed = sStep.getForwardSpeed();
                if (sTrackSpeed > 0) {
                    if (sTrackSpeed > 0) {
                        sTrackSpeed = (sTrackSpeed + measuredSpeed) / 2;
                    } else {
                        sTrackSpeed = measuredSpeed;
                    }
                    sStep.setForwardSpeed(sTrackSpeed);
                }
            } else {
                sTrackSpeed = sStep.getReverseSpeed();
                if (sTrackSpeed > 0) {
                    if (sTrackSpeed > 0) {
                        sTrackSpeed = (sTrackSpeed + measuredSpeed) / 2;
                    } else {
                        sTrackSpeed = measuredSpeed;
                    }
                    sStep.setReverseSpeed(sTrackSpeed);
                }
            }
        }
       return false;
    }
    private void clearStats() {
        _timeAtSpeed = 0;
        _distanceTravelled = 0.0f;
        _settingsTravelled = 0.0f;
        _numchanges = 0;
    }

    /*
     * The engineer makes this notification before setting a new speed
     * Calculate the distance traveled since the last speed change.
     */
    synchronized protected void speedChange(float throttleSetting) {
        if (Math.abs(_intEndSpeed - throttleSetting) < 0.00001f) {
            return;
        }
        _numchanges++;
        long time = System.currentTimeMillis();
        if (throttleSetting <= 0) {
            throttleSetting = 0;
        }
        if (_prevChangeTime > 0) {
            long elapsedTime = time - _prevChangeTime;
            float dist = getDistanceOfSpeedChange(_intStartSpeed, _intEndSpeed, elapsedTime);
            if (_intStartSpeed > 0 || _intEndSpeed > 0) {
                _timeAtSpeed += elapsedTime;
            }
            if (log.isDebugEnabled()) {
                log.debug("speedChange to {}: dist={} in {}ms from speed {} to {}.",
                        throttleSetting, dist, elapsedTime, _intStartSpeed, _intEndSpeed);
            }
            _distanceTravelled += dist;
            _settingsTravelled += throttleSetting * elapsedTime;
        }
        if (_entertime <= 0) {
            _entertime = time;  // time of first non-zero speed
        }
        _prevChangeTime = time;
        _intStartSpeed = _intEndSpeed;
        _intEndSpeed = throttleSetting;
    }

    private static final Logger log = LoggerFactory.getLogger(SpeedUtil.class);
}
