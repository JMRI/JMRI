package jmri.jmrix.loconet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.managers.configurexml.DefaultIdTagManagerXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.IdTagManager}
 * interface that manages TranspondingTags.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public class TranspondingTagManager extends jmri.managers.DefaultIdTagManager {

    @SuppressWarnings("deprecation")
    public TranspondingTagManager() {
        super(new jmri.jmrix.ConflictingSystemConnectionMemo("L", "LocoNet")); // NOI18N
        InstanceManager.store(this, TranspondingTagManager.class);
    }

    @Override
    protected TranspondingTag createNewIdTag(String systemName, String userName) {
        if (!systemName.startsWith(getSystemPrefix() + typeLetter() )) {
            systemName = getSystemPrefix() + typeLetter() + systemName;
        }
        return new TranspondingTag(systemName, userName);
    }
    
    @Override
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new IdTag:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        // return existing if there is one
        TranspondingTag s;
        if ((userName != null) && ((s = (TranspondingTag)getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = (TranspondingTag) getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found IdTag via system name (" + systemName
                        + ") with non-null user name (" + userName + ")");
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewIdTag(systemName, userName);

        // save in the maps
        register(s);

        // if that failed, blame it on the input arguments
        if (s == null) {
            throw new IllegalArgumentException();
        }

        return s;
    }

    @Override
    public void writeIdTagDetails() throws java.io.IOException {
        if (this.dirty) {
            new DefaultIdTagManagerXml(this,"TranspondingIdTags.xml").store();  //NOI18N
            this.dirty = false;
            log.debug("...done writing IdTag details");
        }
    }

    @Override
    public void readIdTagDetails() {
        log.debug("reading idTag Details");
        new DefaultIdTagManagerXml(this,"TranspondingIdTags.xml").load();  //NOI18N
        this.dirty = false;
        log.debug("...done reading IdTag details");
    }

    private static final Logger log = LoggerFactory.getLogger(TranspondingTagManager.class);

}
