package jmri.jmrix.ecos;

import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrix.ecos.swing.preferences.PreferencesPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores User Preferences on how to deal with synchronising the Ecos Database
 * with JMRI.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class EcosPreferences /*implements java.beans.PropertyChangeListener*/ {

    public EcosPreferences(EcosSystemConnectionMemo memo) {
        if (log.isDebugEnabled()) {
            log.debug("creating a new EcosPreferences object");
        }

        ecosPreferencesShutDownTask = new QuietShutDownTask("Ecos Preferences Shutdown") {
            @Override
            public boolean execute() {
                if (getChangeMade()) {
                    InstanceManager.getDefault(jmri.ConfigureManager.class).storePrefs();
                }
                return true;
            }
        };
        InstanceManager.getDefault(jmri.ShutDownManager.class).register(ecosPreferencesShutDownTask);

        adaptermemo = memo;
        InstanceManager.store(new PreferencesPane(this),jmri.swing.PreferencesPanel.class);
    }

    private final EcosSystemConnectionMemo adaptermemo;

    boolean preferencesLoaded = false;

    public boolean getPreferencesLoaded() {
        return preferencesLoaded;
    }

    public void setPreferencesLoaded() {
        preferencesLoaded = true;
        firePropertyChange("loaded", null, null);
    }

    ShutDownTask ecosPreferencesShutDownTask = null;

    public static final int ASK = 0x00; // ie always ask the question
    public static final int NO = 0x01; //ie never do the operation
    public static final int YES = 0x02; //ie always perform the operation

    private boolean _changeMade = false;

    public boolean getChangeMade() {
        return _changeMade;
    }

    /**
     * Reset is used after the preferences have been loaded for the first time.
     */
    public void resetChangeMade() {
        _changeMade = false;
    }

    /**
     * Store the user's preference for when a loco is created in the Roster,
     * should it also be created in the ECoS, if it does not exist? Currently
     * not implemented.
     */
    private int _addlocotoecos = ASK;

    public int getAddLocoToEcos() {
        return _addlocotoecos;
    }

    public void setAddLocoToEcos(int boo) {
        _addlocotoecos = boo;
        changeMade();
    }

    /**
     * Store the user's preference if a loco has been created on the ECoS,
     * should an entry in the JMRI Roster be created. Currently not implemented.
     */
    private int _addlocotojmri = ASK;

    public int getAddLocoToJMRI() {
        return _addlocotojmri;
    }

    public void setAddLocoToJMRI(int boo) {
        _addlocotojmri = boo;
        changeMade();
    }

    /**
     * Store the user's preference on how the ECoS loco description, should be
     * formatted. Currently not implemented
     */
    private String _ecoslocodescription = null;

    public String getEcosLocoDescription() {
        return _ecoslocodescription;
    }

    public void setEcosLocoDescription(String descript) {
        _ecoslocodescription = descript;
        changeMade();
    }

    /**
     * If there is a conflict in loco information between the ECoS and JMRI,
     * this determines which system wins. Currently not implemented.
     */
    private static final int NOSYNC = 0x00;
    private static final int WARN = 0x01;
    private static final int JMRI = 0x02;
    private static final int ECOS = 0x03;

    private int _locomaster = 0x00;

    public int getLocoMaster() {
        return _locomaster;
    }

    public void setLocoMaster(int master) {
        _locomaster = master;
    }

    /**
     * Determine system description from GUI string for how to solve
     * conflicts between rosters in JMRI and ECoS and store in _locomaster.
     * <p>
     * Keep identical to {@link jmri.jmrix.ecos.swing.preferences.PreferencesPane}#initializeMasterControlCombo(javax.swing.JComboBox)
     *
     * @param master setting for conflict syncing
     */
    public void setLocoMaster(String master) {
        if (master.equals(Bundle.getMessage("NOSYNC"))) {
            _locomaster = NOSYNC;
        } else if (master.equals(Bundle.getMessage("WARNING"))) {
            _locomaster = WARN;
        } else if (master.equals("JMRI")) {
            _locomaster = JMRI;
        } else if (master.equals("ECoS")) {
            _locomaster = ECOS;
        } else {
            _locomaster = NOSYNC;
        }
        changeMade();
    }

    /**
     * Determine GUI string from system description for how to solve
     * conflicts between rosters in JMRI and ECoS.
     * <p>
     * Keep identical to {@link jmri.jmrix.ecos.swing.preferences.PreferencesPane}#initializeMasterControlCombo(javax.swing.JComboBox)
     *
     * @return GUI string
     */
    public String getLocoMasterAsString() {
        String result;
        switch (_locomaster) {
            case 0x00:
                result = Bundle.getMessage("NOSYNC");
                break;
            case 0x01:
                result = Bundle.getMessage("WARNING");
                break;
            case 0x02:
                result = "JMRI";
                break;
            case 0x03:
                result = "ECoS";
                break;
            default:
                result = Bundle.getMessage("NOSYNC");
                break;
        }
        return result;
    }

    /**
     * Store the user's preference if a loco has been created ad-hoc, on the
     * Throttle, should the entry created for it in the ECoS be deleted.
     * Currently not implemented.
     */
    private int _adhoclocofromecos = ASK;

    //0x00 - always ask
    //0x01 - always leave loco
    //0x02 - always remove loco
    public int getAdhocLocoFromEcos() {
        return _adhoclocofromecos;
    }

    public void setAdhocLocoFromEcos(int boo) {
        _adhoclocofromecos = boo;
        changeMade();
    }

    /**
     * Store the user's preference to deal with if another device has control
     * over the loco
     */
    private int _forcecontrolfromecos = ASK;

    //0x00 - always ask
    //0x01 - always always fail
    //0x02 - always force control
    public int getForceControlFromEcos() {
        return _forcecontrolfromecos;
    }

    public void setForceControlFromEcos(int boo) {
        _forcecontrolfromecos = boo;
        changeMade();
    }

    private boolean _locoControl = false;

    public boolean getLocoControl() {
        return _locoControl;
    }

    public void setLocoControl(boolean boo) {
        _locoControl = boo;
        changeMade();
    }

    /**
     * Store the user's preference if a loco has been created ad-hoc, on the
     * Throttle, should the entry created for it in the ECOS be deleted.
     * Currently not implemented.
     */

    /*private String _defaultecosprotocol = "DCC128";

     public String getDefaultEcosProtocol(){
     return _defaultecosprotocol;
     }

     public void setDefaultEcosProtocol(String boo){
     _defaultecosprotocol = boo;
     changeMade();
     }*/

    /**
     * Store the user's preference for deleting a loco from the roster should
     * it, also be deleted from the ECOS. Currently not implemented.
     */
    //0x00 - always ask
    //0x01 - always leave loco
    //0x02 - always remove loco
    private int _removelocofromecos = ASK;

    public int getRemoveLocoFromEcos() {
        return _removelocofromecos;
    }

    public void setRemoveLocoFromEcos(int boo) {
        _removelocofromecos = boo;
        changeMade();
    }

    /**
     * Store the user's preference for deleting a loco from the ECOS should it,
     * also be deleted from the JMRI Roster. Currently not implemented.
     */
    private int _removelocofromjmri = ASK;

    public int getRemoveLocoFromJMRI() {
        return _removelocofromjmri;
    }

    public void setRemoveLocoFromJMRI(int boo) {
        _removelocofromjmri = boo;
        changeMade();
    }

    /**
     * Store the user's preference when creating a turnout in JMRI should it
     * also be created on the ECOS. Currently not implemented.
     */
    private int _addturnoutstoecos = ASK;

    public int getAddTurnoutsToEcos() {
        return _addturnoutstoecos;
    }

    public void setAddTurnoutsToEcos(int boo) {
        _addturnoutstoecos = boo;
        changeMade();
    }

    /**
     * Store the user's preference when a new turnout is created on the ECOS
     * should it also be created in JMRI. Currently not implemented.
     */
    private int _addturnoutstojmri = ASK;

    public int getAddTurnoutsToJMRI() {
        return _addturnoutstojmri;
    }

    public void setAddTurnoutsToJMRI(int boo) {
        _addturnoutstojmri = boo;
        changeMade();
    }

    /**
     * Store the user's preference when a new turnout is removed from the ECOS
     * should it also be removed from JMRI. Currently not implemented.
     */
    private int _removeturnoutsfromjmri = ASK;

    public int getRemoveTurnoutsFromJMRI() {
        return _removeturnoutsfromjmri;
    }

    public void setRemoveTurnoutsFromJMRI(int boo) {
        _removeturnoutsfromjmri = boo;
        changeMade();
    }

    /**
     * Store the user's preference when a new turnout is removed from JMRI
     * should it also be removed from the ECoS. Currently not implemented.
     */
    private int _removeturnoutsfromecos = ASK;

    public int getRemoveTurnoutsFromEcos() {
        return _removeturnoutsfromecos;
    }

    public void setRemoveTurnoutsFromEcos(int boo) {
        _removeturnoutsfromecos = boo;
        changeMade();
    }

    private String _rosterAttribute = "EcosObject";

    public void setRosterAttribute(String att) {
        //If no suffix is passed then we just use the default.
        if ((att == null) || (att.equals(""))) {
            _rosterAttribute = "EcosObject";
        } else if (att.startsWith("EcosObject")) {
            _rosterAttribute = att;
        } else {
            _rosterAttribute = "EcosObject:" + att;
        }
        changeMade();
    }

    public String getRosterAttribute() {
        return _rosterAttribute;
    }

    public String getRosterAttributeSuffix() {
        /*This is a simple case, if the string is not 11 characters long, then no
         prefix has been set, therefore we can just return ""*/
        try {
            return _rosterAttribute.substring(11);
        } catch (java.lang.StringIndexOutOfBoundsException e) {
        }
        return null;
    }

    public String name() {
        return null;
    }

    void changeMade() {
        _changeMade = true;
        firePropertyChange("update", null, null);
    }

    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(EcosPreferences.class);

    /**
     * @return the adaptermemo
     */
    public EcosSystemConnectionMemo getAdaptermemo() {
        return adaptermemo;
    }

}
