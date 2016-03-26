package jmri.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProfileProperties implements AuxiliaryProperties {

    private final Profile project;
    
    private final static Logger log = LoggerFactory.getLogger(ProfileProperties.class);
    
    public ProfileProperties(Profile project) {
        this.project = project;
    }
    
    @Override
    public String get(String key, boolean shared) {
        return ProfileUtils.getPreferences(this.project, null, shared).node(Profile.PROFILE).get(key, null);
    }

    @Override
    public Iterable<String> listKeys(boolean shared) {
        try {
            String[] keys = ProfileUtils.getPreferences(this.project, null, shared).node(Profile.PROFILE).keys();
            return new ArrayList<>(Arrays.asList(keys));
        } catch (BackingStoreException ex) {
            log.error("Unable to read properties.", ex);
            return new ArrayList<>();
        }
    }

    @Override
    public void put(String key, String value, boolean shared) {
        ProfileUtils.getPreferences(this.project, null, shared).node(Profile.PROFILE).put(key, value);
    }
    
}
