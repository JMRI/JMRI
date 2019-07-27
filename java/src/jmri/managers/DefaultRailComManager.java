package jmri.managers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.RailCom;
import jmri.RailComManager;
import jmri.implementation.DefaultRailCom;
import jmri.managers.configurexml.DefaultIdTagManagerXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.RailComManager}
 * interface.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public class DefaultRailComManager extends DefaultIdTagManager
        implements RailComManager {

    @SuppressWarnings("deprecation")
    public DefaultRailComManager() {
        super(new jmri.jmrix.ConflictingSystemConnectionMemo("R", "RailCom")); // NOI18N
        InstanceManager.store(this, RailComManager.class);
        InstanceManager.setIdTagManager(this);
    }

    @Override
    protected RailCom createNewIdTag(String systemName, String userName) {
        // we've decided to enforce that IdTag system
        // names start with RD by prepending if not present
        if (!systemName.startsWith(getSystemPrefix() + "D")) {
            systemName = getSystemPrefix() + "D" + systemName;
        }
        return new DefaultRailCom(systemName, userName);
    }

    @SuppressFBWarnings(value="RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", justification="defensive programming check of @Nonnull argument")
    private void checkSystemName(@Nonnull String systemName, @CheckForNull String userName) {
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
            throw new IllegalArgumentException("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
        }
    }
    
    @Override
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new IdTag:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        checkSystemName(systemName, userName);
        
        // return existing if there is one
        RailCom s;
        if ((userName != null) && ((s = (RailCom)getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = (RailCom) getBySystemName(systemName)) != null) {
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
            new DefaultIdTagManagerXml(this,"RailComIdTags.xml").store();  //NOI18N
            this.dirty = false;
            log.debug("...done writing IdTag details");
        }
    }

    @Override
    public void readIdTagDetails() {
        log.debug("reading idTag Details");
        new DefaultIdTagManagerXml(this,"RailComIdTags.xml").load();  //NOI18N
        this.dirty = false;
        log.debug("...done reading IdTag details");
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultRailComManager.class);

}
