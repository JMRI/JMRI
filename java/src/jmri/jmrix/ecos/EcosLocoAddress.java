package jmri.jmrix.ecos;

import java.util.HashMap;
import java.util.List;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Stores all the loco information from the ECoS into JMRI
 *
 * @author Kevin Dickerson
 */
public class EcosLocoAddress implements jmri.LocoAddress {

    private String _ecosObject = null;
    private int _dccAddress = 0;
    private String _ecosDescription = null;
    private String _rosterId = null;
    private String _ecosProtocolString = null;
    private LocoAddress.Protocol _protocol = LocoAddress.Protocol.DCC;
    private SpeedStepMode _speedSteps = SpeedStepMode.NMRA_DCC_128;
    boolean direction;
    int currentSpeed;
    private boolean doNotAddToRoster = false;
    public static int MFX_DCCAddressOffset = 20000;

    public EcosLocoAddress(int dCCAddress) {
        _dccAddress = dCCAddress;
    }

    public EcosLocoAddress(String ecosObject, String rosterAtt) {
        _ecosObject = ecosObject;
        //We see if there is a matching roster entry with out object against it
        //if so we add the rosterId to the ecoclocoaddress entry.
        List<RosterEntry> l = Roster.getDefault().getEntriesWithAttributeKeyValue(rosterAtt, ecosObject);
        //It should be unique
        if (l.size() > 0) {
            _rosterId = l.get(0).getId();
        }
    }

    HashMap<Integer, Integer> cvValues = new HashMap<Integer, Integer>();

    public void setCV(int cv, int value) {
        cvValues.put(cv, value);
    }

    public int getCV(int cv) {
        if (cvValues.containsKey(cv)) {
            return cvValues.get(cv);
        }
        return -1;
    }

    public String getCVAsString(int cv) {
        int val = getCV(cv);
        if (val == -1) {
            return null;
        }
        return "" + val; //Do correctly

    }

    public void setLocoAddress(int dCCAddress) {
        _dccAddress = dCCAddress;
    }

    /**
     * @return the loco address configured on the ECoS for this loco
     */
    @Override
    public int getNumber() {
        return _dccAddress;
    }

    /**
     * @return the loco object as a string on the ECOS for this loco
     */
    public String getEcosObject() {
        return _ecosObject;
    }

    /**
     * @return the loco object as a integer on the ECOS for this loco
     */
    public int getEcosObjectAsInt() {
        return Integer.parseInt(_ecosObject);
    }

    public void doNotAddToRoster() {
        doNotAddToRoster = true;
    }

    public void allowAddToRoster() {
        doNotAddToRoster = false;
    }

    public boolean addToRoster() {
        return !doNotAddToRoster;
    }

    protected void setSpeed(int speed) {
        int oldspeed = currentSpeed;
        currentSpeed = speed;
        firePropertyChange("Speed", oldspeed, currentSpeed); // NOI18N
    }

    public int getSpeed() {
        return currentSpeed;
    }

    protected void setDirection(String line) {
        setDirection(getDirection(line));
    }

    protected void setDirection(boolean dir) {
        boolean olddir = direction;
        direction = dir;
        firePropertyChange("Direction", olddir, direction);
    }

    public boolean getDirection() {
        return direction;
    }

    public String getDirectionAsString() {
        if (direction) {
            return Bundle.getMessage("Forward");
        }
        return Bundle.getMessage("Reverse");
    }

    //Should this option be made public? should setting the object only be available when the Loco is created.
    //It needs a bit of a re-think!
    //It is set this way because of adhoc locos being created for throttles.
    public void setEcosObject(String ecosObject) {
        _ecosObject = ecosObject;
    }

    /**
     * @return the loco object description held on the ECOS for this loco
     */
    public String getEcosDescription() {
        return _ecosDescription;
    }

    public void setEcosDescription(String description) {
        if (description.startsWith("\"")) description = description.substring(1, description.length());
        if (description.endsWith("\"")) description = description.substring(0, description.length() - 1);
        String oldValue = _ecosDescription;
        _ecosDescription = description;
        firePropertyChange("name", oldValue, _ecosDescription); // NOI18N
    }

    /**
     * @return the JMRI Roster ID for this loco
     */
    public String getRosterId() {
        return _rosterId;
    }

    public void setRosterId(String roster) {
        String oldValue = _rosterId;
        _rosterId = roster;
        firePropertyChange("RosterId", oldValue, _rosterId); // NOI18N
    }

    //Protocol is here as it is a potential value from the ecos
    // We may not actually use it.
    public String getECOSProtocol() {
        return _ecosProtocolString;
    }

    //@TODO Need to udate this to return the new Protocol option from LocoAddress
    public SpeedStepMode getSpeedStepMode() {
        return _speedSteps;
    }

    public void setProtocol(String protocol) {
        //funcexists
        String oldValue = _ecosProtocolString;
        _ecosProtocolString = protocol;
        firePropertyChange("protocol", oldValue, _ecosProtocolString);
        if (protocol.startsWith("DCC")) {
            _protocol = LocoAddress.Protocol.DCC;
        } else if (protocol.startsWith("MM")) {
            _protocol = LocoAddress.Protocol.MOTOROLA;
        } else if (protocol.startsWith("SX")) {  //SX32
            _protocol = LocoAddress.Protocol.SELECTRIX;
        } else if (protocol.startsWith("LBG")) {  //LBG14
            _protocol = LocoAddress.Protocol.SELECTRIX;
        }
        if (protocol.endsWith("128")) {
            _speedSteps = SpeedStepMode.NMRA_DCC_128;
        } else if (protocol.endsWith("28")) {
            _speedSteps = SpeedStepMode.NMRA_DCC_28;
        } else if (protocol.endsWith("27")) {
            _speedSteps = SpeedStepMode.NMRA_DCC_27;
        } else if (protocol.endsWith("14")) {
            _speedSteps = SpeedStepMode.NMRA_DCC_14;
        }
    }

    @Override
    public LocoAddress.Protocol getProtocol() {
        return _protocol;
    }

    /*
     The Temporary Entry Field is used to determine if JMRI has had to create the entry on an ad-hoc basis
     for the throttle.  If this is set to True, the throttle can evaluate this field to determine if the 
     loco should be removed from the Ecos Database when closing the application.
     */
    boolean _tempEntry = false;

    public void setEcosTempEntry(boolean boo) {
        _tempEntry = boo;
    }

    public boolean getEcosTempEntry() {
        return _tempEntry;
    }

    public void reply(EcosReply m) {
        String msg = m.toString();
        if (m.getResultCode() != 0) {
            return; //The result is not valid therefore we can not set it.
        }
        if (msg.startsWith("<REPLY get(" + _ecosObject + ",") || msg.startsWith("<EVENT " + _ecosObject + ">")) {
            if (msg.contains("speed")) {
                setSpeed(Integer.parseInt(EcosReply.getContentDetails(msg, "speed")));
            }
            if (msg.contains("dir")) {
                setDirection(getDirection(msg));
            }
            if (msg.contains("protocol")) {
                setProtocol(EcosReply.getContentDetails(msg, "protocol"));
            }
            if (msg.contains("name")) {
                String name = EcosReply.getContentDetails(msg, "name").trim();
                setEcosDescription(name);
            }
        }
    }

    boolean getDirection(String line) {
        boolean newDirection = false;
        if (EcosReply.getContentDetails(line, "dir").equals("0")) {
            newDirection = true;
        }
        return newDirection;
    }

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //public void firePropertyChange(String propertyName, Object oldValue, Object newValue);
    // _once_ if anything has changed state
    // since we can't do a "super(this)" in the ctor to inherit from PropertyChangeSupport, we'll
    // reflect to it
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public synchronized int getNumPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners().length;
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public void dispose() {
        pcs = null;
    }

}
