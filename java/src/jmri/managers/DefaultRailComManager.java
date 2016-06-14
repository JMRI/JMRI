package jmri.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.RailCom;
import jmri.RailComManager;
import jmri.Reporter;
import jmri.implementation.DefaultRailCom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.RailComManager}
 * interface.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 * @since 2.99.4
 */
public class DefaultRailComManager extends AbstractManager
        implements RailComManager {

    public DefaultRailComManager() {
        InstanceManager.store(this, RailComManager.class);
        if (jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class) == null) {
            new jmri.jmrit.beantable.ListedTableFrame();
        }
        jmri.InstanceManager.getDefault(jmri.jmrit.beantable.ListedTableFrame.class).addTable("jmri.jmrit.beantable.RailComTableAction", "RailComm Table", true);
    }

    @Override
    public RailCom provideIdTag(String name) {
        RailCom t = getIdTag(name);
        if (t != null) {
            return t;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            return newIdTag(name, null);
        } else {
            return newIdTag(makeSystemName(name), null);
        }
    }

    protected RailCom createNewIdTag(String systemName, String userName) {
        // we've decided to enforce that IdTag system
        // names start with ID by prepending if not present
        if (!systemName.startsWith("ID")) {
            systemName = "ID" + systemName;
        }
        return new DefaultRailCom(systemName, userName);
    }

    @Override
    public RailCom newIdTag(String systemName, String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new IdTag:"
                    + ((systemName == null) ? "null" : systemName)
                    + ";" + ((userName == null) ? "null" : userName));
        }
        if (systemName == null) {
            log.error("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
            throw new IllegalArgumentException("SystemName cannot be null. UserName was "
                    + ((userName == null) ? "null" : userName));
        }
        // return existing if there is one
        RailCom s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
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

        // if that failed, blame it on the input arguements
        if (s == null) {
            throw new IllegalArgumentException();
        }

        return s;
    }

    @Override
    public RailCom getIdTag(String name) {
        RailCom t = getBySystemName(makeSystemName(name));
        if (t != null) {
            return t;
        }

        t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public RailCom getByTagID(String tagID) {
        return getBySystemName(makeSystemName(tagID));
    }

    @Override
    public RailCom getBySystemName(String name) {
        return (RailCom) _tsys.get(name);
    }

    @Override
    public RailCom getByUserName(String key) {
        return (RailCom) _tuser.get(key);
    }

    @Override
    public List<IdTag> getTagsForReporter(Reporter reporter, long threshold) {
        List<IdTag> out = new ArrayList<>();
        Date lastWhenLastSeen = new Date(0);

        // First create a list of all tags seen by specified reporter
        // and record the time most recently seen
        for (NamedBean n : _tsys.values()) {
            IdTag t = (IdTag) n;
            if (t.getWhereLastSeen() == reporter) {
                out.add(t);
                if (t.getWhenLastSeen().after(lastWhenLastSeen)) {
                    lastWhenLastSeen = t.getWhenLastSeen();
                }
            }
        }

        if (out.size() > 0) {
            // Calculate the threshold time based on the most recently seen tag
            Date thresholdTime = new Date(lastWhenLastSeen.getTime() - threshold);

            // Now remove from the list all tags seen prior to the threshold time
            for (IdTag t : out) {
                if (t.getWhenLastSeen().before(thresholdTime)) {
                    out.remove(t);
                }
            }
        }

        return out;
    }

    /**
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {
    }

    @Override
    public char typeLetter() {
        return 'D';
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.IDTAGS;
    }

    @Override
    public boolean isInitialised() {
        return true;
    }

    @Override
    public void setStateStored(boolean state) {
    }

    @Override
    public boolean isStateStored() {
        return false;
    }

    @Override
    public void setFastClockUsed(boolean fastClock) {
    }

    @Override
    public boolean isFastClockUsed() {
        return false;
    }

    @Override
    public void init() {
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameReporter");
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultRailComManager.class.getName());

}
