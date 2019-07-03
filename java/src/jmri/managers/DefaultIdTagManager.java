package jmri.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.Disposable;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.implementation.DefaultIdTag;
import jmri.managers.configurexml.DefaultIdTagManagerXml;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation for the Internal {@link jmri.IdTagManager} interface.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class DefaultIdTagManager extends AbstractManager<IdTag> implements IdTagManager, Disposable {

    protected boolean dirty = false;
    private boolean initialised = false;
    private boolean loading = false;
    private boolean storeState = false;
    private boolean useFastClock = false;
    private ShutDownTask shutDownTask = null;

    public DefaultIdTagManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.IDTAGS;
    }

    @Override
    public boolean isInitialised() {
        return initialised;
    }

    @Override
    public void init() {
        log.debug("init called");
        if (!initialised && !loading) {
            log.debug("Initialising");
            // Load when created
            loading = true;
            readIdTagDetails();
            loading = false;
            dirty = false;

            // Create shutdown task to save
            log.debug("Register ShutDown task");
            if (this.shutDownTask == null) {
                this.shutDownTask = new jmri.implementation.AbstractShutDownTask("Writing IdTags") { // NOI18N
                    @Override
                    public boolean execute() {
                        // Save IdTag details prior to exit, if necessary
                        log.debug("Start writing IdTag details...");
                        try {
                            writeIdTagDetails();
                        } catch (java.io.IOException ioe) {
                            log.error("Exception writing IdTags: {}", (Object) ioe);
                        }

                        // continue shutdown
                        return true;
                    }
                };
                InstanceManager.getDefault(ShutDownManager.class).register(this.shutDownTask);
            }
            initialised = true;
        }
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
    public IdTag provide(String name) throws IllegalArgumentException {
        return provideIdTag(name);
    }

    @Override
    public IdTag provideIdTag(String name) throws IllegalArgumentException {
        if (!initialised && !loading) {
            init();
        }
        IdTag t = getIdTag(name);
        if (t != null) {
            return t;
        }
        if (name.startsWith(getSystemPrefix() + typeLetter())) {
            return newIdTag(name, null);
        } else if (!name.isEmpty()) {
            return newIdTag(makeSystemName(name), null);
        } else {
            throw new IllegalArgumentException("\"" + name + "\" is invalid");
        }
    }

    @Override
    public IdTag getIdTag(String name) {
        if (!initialised && !loading) {
            init();
        }

        IdTag t = getBySystemName(makeSystemName(name));
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
    public IdTag getBySystemName(String name) {
        if (!initialised && !loading) {
            init();
        }
        return _tsys.get(name);
    }

    @Override
    public IdTag getByUserName(String key) {
        if (!initialised && !loading) {
            init();
        }
        return _tuser.get(key);
    }

    @Override
    public IdTag getByTagID(String tagID) {
        if (!initialised && !loading) {
            init();
        }
        return getBySystemName(makeSystemName(tagID));
    }

    protected IdTag createNewIdTag(String systemName, String userName) {
        // Names start with the system prefix followed by D.
        // Add the prefix if not present.
        if (!systemName.startsWith(getSystemPrefix() + typeLetter() )) // NOI18N
        {
            systemName = getSystemPrefix() + typeLetter() + systemName; // NOI18N
        }
        return new DefaultIdTag(systemName, userName);
    }

    @Override
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName) {
        if (!initialised && !loading) {
            init();
        }
        log.debug("new IdTag:{};{}", systemName, (userName == null) ? "null" : userName); // NOI18N
        Objects.requireNonNull(systemName, "SystemName cannot be null.");

        // return existing if there is one
        IdTag s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName());
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found IdTag via system name ({}) with non-null user name ({})", systemName, userName); // NOI18N
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
    public void register(IdTag s) {
        super.register(s);
        this.setDirty(true);
    }

    @Override
    public void deregister(IdTag s) {
        super.deregister(s);
        this.setDirty(true);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        this.setDirty(true);
    }

    public void writeIdTagDetails() throws java.io.IOException {
        if (this.dirty) {
            new DefaultIdTagManagerXml(this,"IdTags.xml").store();  //NOI18N
            this.dirty = false;
            log.debug("...done writing IdTag details");
        }
    }

    public void readIdTagDetails() {
        log.debug("reading idTag Details");
        new DefaultIdTagManagerXml(this,"IdTags.xml").load();  //NOI18N
        this.dirty = false;
        log.debug("...done reading IdTag details");
    }


    @Override
    public void setStateStored(boolean state) {
        if (state != storeState) {
            this.setDirty(true);
        }
        storeState = state;
    }

    @Override
    public boolean isStateStored() {
        return storeState;
    }

    @Override
    public void setFastClockUsed(boolean fastClock) {
        if (fastClock != useFastClock) {
            this.setDirty(true);
        }
        useFastClock = fastClock;
    }

    @Override
    public boolean isFastClockUsed() {
        return useFastClock;
    }

    @Override
    public List<IdTag> getTagsForReporter(Reporter reporter, long threshold) {
        List<IdTag> out = new ArrayList<>();
        Date lastWhenLastSeen = new Date(0);

        // First create a list of all tags seen by specified reporter
        // and record the time most recently seen
        for (IdTag n : _tsys.values()) {
            IdTag t = n;
            if (t.getWhereLastSeen() == reporter) {
                out.add(t);
                Date tagLastSeen = t.getWhenLastSeen();
                if (tagLastSeen != null && tagLastSeen.after(lastWhenLastSeen)) {
                    lastWhenLastSeen = tagLastSeen;
                }
            }
        }

        // Calculate the threshold time based on the most recently seen tag
        Date thresholdTime = new Date(lastWhenLastSeen.getTime() - threshold);

        // Now remove from the list all tags seen prior to the threshold time
        out.removeIf((t) -> {
            Date tagLastSeen = t.getWhenLastSeen();
            return tagLastSeen == null || tagLastSeen.before(thresholdTime);
        });

        return out;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(ShutDownManager.class).deregister(this.shutDownTask);
        super.dispose();
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameReporters" : "BeanNameReporter");
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(IdTagManager.class)) {
                return new DefaultIdTagManager();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(IdTagManager.class);
            return set;
        }
    }

}
