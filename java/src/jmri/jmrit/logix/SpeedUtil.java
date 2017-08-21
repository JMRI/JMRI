package jmri.jmrit.logix;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    private RosterEntry _rosterEntry;
    private Warrant _warrant;

//    private TreeMap<Integer, SpeedStep> _speedTree;
    private DccThrottle _throttle;
    private float _stepIncrement;   // min throttle
    private RosterSpeedProfile _speedProfile; // merge of existing Roster speeedProfile and session speeedProfile
    private RosterSpeedProfile _sessionProfile; // speeds measured in the session
    private SignalSpeedMap _signalSpeedMap; 

    public static float SCALE_FACTOR = 55; // divided by _scale, gives a rough correction for track speed

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
        if (id == null || !id.equals(_rosterId)) {
            _speedProfile = null;
            _sessionProfile = null;
            if (id != null) {
                _rosterId = id;
                makeSpeedTree();
            } else {
                _rosterId = null;
            }
        }
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
        if (id == null || id.trim().length()==0) {
            _rosterEntry = null;
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
            _rosterId = _rosterEntry.getId();
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

    /**
     * Must be called from eventThread
     * @param frame JmriJFrame caller
     * @return speedProfile
     */
    protected RosterSpeedProfile getValidSpeedProfile(java.awt.Frame frame) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        HashMap<Integer, Boolean> anomalies = MergePrompt.validateSpeedProfile(speedProfile, _rosterId);
        if (anomalies != null && anomalies.size() > 0) {
            if (log.isDebugEnabled()) log.debug("getValidSpeedProfile for {} #anomalies={} on Gui {}",
                    _rosterId, anomalies.size(), jmri.util.ThreadingUtil.isLayoutThread());
            if (jmri.util.ThreadingUtil.isLayoutThread()) { // safety
                JDialog dialog = new JDialog(frame, Bundle.getMessage("viewTitle", _rosterId), true);
                JButton ok = new JButton(Bundle.getMessage("ButtonDone"));
                ok.addActionListener((ActionEvent evt) -> {
                    dialog.dispose();
                });
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
                JLabel l = new JLabel(Bundle.getMessage("anomaly",_rosterId));
                l.setForeground(java.awt.Color.RED);
                l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                p.add(l);
                JLabel label = new JLabel(Bundle.getMessage("deletePrompt1"));
                label.setForeground(java.awt.Color.RED);
                label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                p.add(label);
                label = new JLabel(Bundle.getMessage("deletePrompt2"));
                label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                p.add(label);
                label = new JLabel(Bundle.getMessage("deletePrompt3"));
                label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                p.add(label);

                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
                panel.add(p);
                panel.add(new SpeedProfilePanel(speedProfile, anomalies));
                panel.add(ok);
                dialog.getContentPane().add(panel);
                dialog.pack();
                dialog.setVisible(true);
            }
        }
        return _speedProfile;
    }

    protected RosterSpeedProfile getSpeedProfile() {
        if (_speedProfile == null) {
            makeSpeedTree();
        }
        return _speedProfile;
    }

    private void makeSpeedTree() {
        if (_rosterId == null) {
            setDccAddress(getAddress());
        }
        if (log.isDebugEnabled()) log.debug("makeSpeedTree for {}.", _rosterId);
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _speedProfile = manager.getMergeProfile(_rosterId);
        _sessionProfile = manager.getSessionProfile(_rosterId);
        if (_sessionProfile == null) {
            _sessionProfile = new RosterSpeedProfile(null);
        }
        if (_speedProfile == null) {
            _speedProfile = new RosterSpeedProfile(getRosterEntry());   // will be a copy or an empty profile            
            if (_rosterEntry!=null) {
                if (log.isDebugEnabled()) log.debug("makeSpeedTree - Copy TreeMap");
                RosterSpeedProfile speedProfile = _rosterEntry.getSpeedProfile();
                if (speedProfile!=null) { // make copy of tree
                    TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
                    for (Map.Entry<Integer, SpeedStep> entry : rosterTree.entrySet()) {
                        _speedProfile.setSpeed(entry.getKey(), entry.getValue().getForwardSpeed(), entry.getValue().getReverseSpeed());
                    }
                }
            }
        }
        _signalSpeedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);

        if (log.isDebugEnabled()) log.debug("SignalSpeedMap: throttle factor= {}, layout scale= {} convesion to m/s= {}",
                _signalSpeedMap.getDefaultThrottleFactor(), _signalSpeedMap.getLayoutScale(),
                _signalSpeedMap.getDefaultThrottleFactor() * _signalSpeedMap.getLayoutScale() / SCALE_FACTOR);
    }
    
    protected boolean profileHasSpeedInfo(boolean isForward) {
        if (_speedProfile == null) {
            return false;
        }
        if (isForward) {
            return _speedProfile.hasForwardSpeeds();            
        } else {
            return _speedProfile.hasReverseSpeeds();            
        }
    }

    protected void stopRun(boolean updateSpeedProfile) {
        if (updateSpeedProfile && _speedProfile!=null) {
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            manager.setSpeedProfiles(_rosterId, _speedProfile, _sessionProfile);
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
        float speed = 0.0f;
        RosterSpeedProfile speedProfile = getSpeedProfile();
        // Note SpeedProfile uses milliseconds per second.
        speed = speedProfile.getSpeed(throttleSetting, isForward) / 1000;            
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
     * @return throttle setting or 0
     */
    protected float getThrottleSetting(float trackSpeed) {
        RosterSpeedProfile speedProfile = getSpeedProfile();
        return speedProfile.getThrottleSetting(trackSpeed * 1000, _throttle.getIsForward());
    }

    /**
     * @param speedSetting Recorded (Normal) throttle setting
     * @param speedtype speed name to modify throttle setting to get modified speed
     * @param time milliseconds
     * @param isForward direction
     * @return distance in millimeters
     */
    protected float getDistanceTraveled(float speedSetting, String speedtype, float time, boolean isForward) {
        float throttleSetting = modifySpeed(speedSetting, speedtype, isForward);
        return getTrackSpeed(throttleSetting, isForward) * time;
    }

    /**
     * Get time needed to travel a distance
     * @param throttleSetting Throttle setting
     * @param distance in millimeters
     * @param isForward direction
     * @return time in milliseconds
     */
    protected float getTimeForDistance(float throttleSetting, float distance, boolean isForward) {
        float speed = getTrackSpeed(throttleSetting, isForward);
        if (distance < 0.0f) {
            return 0.0f;
        }
        return (distance/speed);
    }

    /**
     * Get ramp length needed to change speed using the WarrantPreference deltas for 
     * throttle increment and time increment
     * @param curSetting current throttle setting
     * @param curSpeedType current speed type
     * @param toSpeedType Speed type change
     * @param isForward direction
     * @return distance in millimeters
     */
    protected RampData rampLengthForSpeedChange(float curSetting, String curSpeedType, String toSpeedType, boolean isForward) {
        if (curSpeedType.equals(toSpeedType)) {
            return new RampData(0.0f, 0);
        }
        float fromSpeed = modifySpeed(curSetting, curSpeedType, isForward);
        float toSpeed = modifySpeed(curSetting, toSpeedType, isForward);
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
            float dist = getTrackSpeed(speed - deltaThrottle*NXFrame._mf, isForward) * deltaTime;
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
                speed *= 1000;
                aveThrottle = _settingsTravelled / _timeAtSpeed;
                // throttle setting should be a step increment
                aveThrottle = _stepIncrement * Math.round(aveThrottle/_stepIncrement);
                
            }
            float spSpeed = _speedProfile.getSpeed(aveThrottle, isForward);                

            if (log.isDebugEnabled()) {
                log.debug("{} speed changes. AveThtle= {}, curThtle= {}. \"dist\"= {}, dist = {}. \"et\"= {}, et= {}.",
                        _numchanges, aveThrottle, throttle, _distanceTravelled, totalLength, _timeAtSpeed, elpsedTime);
                log.debug("Speeds: SpeedProfile= {}, aveSpeed= {}, speed= {}, over block {} to {}",
                        spSpeed, aveSpeed, speed, _warrant._orders.get(lastIdx).getBlock().getDisplayName(),
                        _warrant._orders.get(newIdx).getBlock().getDisplayName());
            }
            if (spSpeed > 0.0f && aveSpeed > 0.0f) {   // perhaps spSpeed should be weighted.  but how much?
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
        _changetime = System.currentTimeMillis();
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