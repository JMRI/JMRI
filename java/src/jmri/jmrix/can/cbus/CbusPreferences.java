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
 * Ideally all changes should be done by user via @CbusPreferencesPane
 *
 * @author Steve Young (c) 2019
 */
public class CbusPreferences extends PreferencesBean {

    // defaults
    private int _reporterMode=0; // defaults to 0, ie normal 5 byte RfID
    private Boolean _allowAutoSensorCreation = false;

    public CbusPreferences() {
        super(ProfileManager.getDefault().getActiveProfile());
        Preferences sharedPreferences = ProfileUtils.getPreferences(super.getProfile(), this.getClass(), true);
        this.readPreferences(sharedPreferences);
    }

    private void readPreferences(Preferences sharedPreferences) {

        this._reporterMode = sharedPreferences.getInt("reporterMode", this.getReporterMode());
        this._allowAutoSensorCreation = sharedPreferences.getBoolean("allowReporterAutoSensorCreation", this.getAllowAutoSensorCreation());
       // this.setIsDirty(false);
    }

    public void savePreferences() {
        
        Preferences sharedPreferences = ProfileUtils.getPreferences(this.getProfile(), this.getClass(), true);
        sharedPreferences.putInt("reporterMode", this.getReporterMode());
        sharedPreferences.putBoolean("allowReporterAutoSensorCreation", this.getAllowAutoSensorCreation());
        try {
            sharedPreferences.sync();
            log.info("Updated Cbus Preferences");
          //  setIsDirty(false);  //  Resets only when stored
        } catch (BackingStoreException ex) {
            log.error("Exception while saving web server preferences", ex);
        }
    }

    boolean isPreferencesValid() {
        return this._reporterMode > -1 && this._reporterMode < 3;
    }
    
    public boolean getAllowAutoSensorCreation() {
        return _allowAutoSensorCreation;
    }
    
    /**
     * Set true if reporters should provide and update a sensor to indicate if reporter is active
     *
     */
    public void setAllowAutoSensorCreation( Boolean newval ) {
        _allowAutoSensorCreation = newval;
    }
    
    public int getReporterMode() {
        return _reporterMode;
    }

    public void setReporterMode(int newval) {
        _reporterMode = newval;
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusPreferences.class);
    
}
