package jmri.jmrix.can.cbus;

import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import jmri.InstanceManager;
import jmri.beans.PreferencesBean;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences for the MERG CBUS connections.
 *
 * @author Steve Young (c) 2019
 */
public class CbusPreferences extends PreferencesBean {

    // defaults
    private boolean _addCommandStations = true;
    private boolean _addNodes = true;
    private boolean _allocateNnListener = true;
    private long _nodeBackgroundFetchDelay = 100L; // needs to match an option in NodeConfigToolPane
    private boolean _startupSearchForCs = true;
    private boolean _startupSearchForNodes = true;
    private boolean _saveRestoreEventTable = true;

    public CbusPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {

        this._addCommandStations = sharedPreferences.getBoolean("_addCommandStations",this.getAddCommandStations());
        this._addNodes = sharedPreferences.getBoolean("_addNodes",this.getAddNodes());
        this._allocateNnListener = sharedPreferences.getBoolean("_allocateNnListener",this.getAllocateNNListener());
        this._nodeBackgroundFetchDelay = sharedPreferences.getLong("_nodeBgFetchDelay",this.getNodeBackgroundFetchDelay());
        
        this._startupSearchForCs = sharedPreferences.getBoolean("_startupSearchForCs",this.getStartupSearchForCs());
        this._startupSearchForNodes = sharedPreferences.getBoolean("_startupSearchForNodes",this.getStartupSearchForNodes());
        
        this._saveRestoreEventTable = sharedPreferences.getBoolean(
            "saveRestoreEventTable",this.getSaveRestoreEventTable() );

    }

    public void savePreferences() {

        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        
        sharedPreferences.putBoolean("_addCommandStations", this.getAddCommandStations() );
        sharedPreferences.putBoolean("_addNodes", this.getAddNodes() );
        sharedPreferences.putBoolean("_allocateNnListener", this.getAllocateNNListener() );
        sharedPreferences.putLong("_nodeBgFetchDelay", this.getNodeBackgroundFetchDelay() );
        
        sharedPreferences.putBoolean("_startupSearchForCs", this.getStartupSearchForCs() );
        sharedPreferences.putBoolean("_startupSearchForNodes", this.getStartupSearchForNodes() );
        
        sharedPreferences.putBoolean("saveRestoreEventTable", this.getSaveRestoreEventTable() );
        
        try {
            sharedPreferences.sync();
            log.debug("Updated Cbus Preferences");
          //  setIsDirty(false);  //  Resets only when stored
        } catch (BackingStoreException ex) {
            log.error("Exception while saving preferences", ex);
        }
    }

    boolean isPreferencesValid() {
        return true;
    }
    
    
    public boolean getAddCommandStations() {
        return _addCommandStations;
    }

    public void setAddCommandStations( boolean newVal ) {
        _addCommandStations = newVal;
        savePreferences();
    }
    
    public boolean getAddNodes() {
        return _addNodes;
    }
    
    public void setAddNodes( boolean newVal ) {
        _addNodes = newVal;
        savePreferences();
    }
    
    public boolean getAllocateNNListener(){
        return _allocateNnListener;
    }
    
    public void setAllocateNNListener( boolean newVal ){
        _allocateNnListener = newVal;
        savePreferences();
    }
    
    public long getNodeBackgroundFetchDelay() {
        return _nodeBackgroundFetchDelay;
    }
    
    public void setNodeBackgroundFetchDelay( long newVal ) {
        _nodeBackgroundFetchDelay = newVal;
        savePreferences();
    }
    
    public boolean getStartupSearchForCs(){
        return _startupSearchForCs;
    }
    
    public void setStartupSearchForCs( boolean newVal ){
        _startupSearchForCs = newVal;
        savePreferences();
    }    
    
    public boolean getStartupSearchForNodes(){
        return _startupSearchForNodes;
    }
    
    public void setStartupSearchForNodes( boolean newVal ){
        _startupSearchForNodes = newVal;
        savePreferences();
    }
    
    public boolean getSaveRestoreEventTable(){
        return _saveRestoreEventTable;
    }
    
    public void setSaveRestoreEventTable( boolean newVal ){
        _saveRestoreEventTable = newVal;
        savePreferences();
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusPreferences.class);

}
