package jmri.managers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.*;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.implementation.DefaultIdTag;
import jmri.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
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
    private Runnable shutDownTask = null;

    public DefaultIdTagManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.IDTAGS;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInitialised() {
        return initialised;
    }

    /** {@inheritDoc} */
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
            initShutdownTask();
            initialised = true;
        }
    }

    protected void initShutdownTask(){
        // Create shutdown task to save
        log.debug("Register ShutDown task");
        if (this.shutDownTask == null) {
            this.shutDownTask = () -> {
                // Save IdTag details prior to exit, if necessary
                log.debug("Start writing IdTag details...");
                try {
                    writeIdTagDetails();
                } catch (java.io.IOException ioe) {
                    log.error("Exception writing IdTags", ioe);
                }
            };
            InstanceManager.getDefault(ShutDownManager.class).register(this.shutDownTask);
        }
    }

    /**
     * {@inheritDoc}
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {
        // override to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'D';
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IdTag provide(@Nonnull String name) throws IllegalArgumentException {
        return provideIdTag(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public IdTag provideIdTag(@Nonnull String name) throws IllegalArgumentException {
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

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public IdTag getIdTag(@Nonnull String name) {
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

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public IdTag getBySystemName(@Nonnull String name) {
        if (!initialised && !loading) {
            init();
        }
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public IdTag getByUserName(@Nonnull String key) {
        if (!initialised && !loading) {
            init();
        }
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @CheckForNull
    @Override
    public IdTag getByTagID(@Nonnull String tagID) {
        if (!initialised && !loading) {
            init();
        }
        return getBySystemName(makeSystemName(tagID));
    }

    @Nonnull
    protected IdTag createNewIdTag(String systemName, String userName) throws IllegalArgumentException {
        // Names start with the system prefix followed by D.
        // Add the prefix if not present.
        if (!systemName.startsWith(getSystemPrefix() + typeLetter())) {
            systemName = getSystemPrefix() + typeLetter() + systemName;
        }
        return new DefaultIdTag(systemName, userName);
    }

    /**
     * Provide ID Tag by UserName then SystemName, creates new IdTag if not found.
     * {@inheritDoc} */
    @Override
    @Nonnull
    public IdTag newIdTag(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        if (!initialised && !loading) {
            init();
        }
        log.debug("new IdTag:{};{}", systemName,userName); // NOI18N
        Objects.requireNonNull(systemName, "SystemName cannot be null.");

        // return existing if there is one
        IdTag s;
        if (userName != null)  {
            s = getByUserName(userName);
            if (s != null) {
                if (getBySystemName(systemName) != s) {
                    log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})", userName, systemName, s.getSystemName());
                }
                return s;
            }
        }
        s = getBySystemName(systemName);
        if (s != null) {
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

        return s;
    }

    /** {@inheritDoc} */
    @Override
    public void register(@Nonnull IdTag s) {
        super.register(s);
        this.setDirty(true);
    }

    /** {@inheritDoc} */
    @Override
    public void deregister(@Nonnull IdTag s) {
        super.deregister(s);
        this.setDirty(true);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);
        this.setDirty(true);
    }

    public void writeIdTagDetails() throws java.io.IOException {
        if (this.dirty) {
            new DefaultIdTagManagerXml(this,"IdTags.xml").store();  // NOI18N
            this.dirty = false;
            log.debug("...done writing IdTag details");
        }
    }

    public void readIdTagDetails() {
        log.debug("reading idTag Details");
        new DefaultIdTagManagerXml(this,"IdTags.xml").load();  // NOI18N
        this.dirty = false;
        log.debug("...done reading IdTag details");
    }

    /** {@inheritDoc} */
    @Override
    public void setStateStored(boolean state) {
        if (!initialised && !loading) {
            init();
        }
        if (state != storeState) {
            this.setDirty(true);
        }
        boolean old = storeState;
        storeState = state;
        firePropertyChange("StateStored", old, state);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isStateStored() {
        if (!initialised && !loading) {
            init();
        }
        return storeState;
    }

    /** {@inheritDoc} */
    @Override
    public void setFastClockUsed(boolean fastClock) {
        if (!initialised && !loading) {
            init();
        }
        if (fastClock != useFastClock) {
            this.setDirty(true);
        }
        boolean old = useFastClock;
        useFastClock  = fastClock;
        firePropertyChange("UseFastClock", old, fastClock);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFastClockUsed() {
        if (!initialised && !loading) {
            init();
        }
        return useFastClock;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<IdTag> getTagsForReporter(@Nonnull Reporter reporter, long threshold) {
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
        out.removeIf(t -> {
            Date tagLastSeen = t.getWhenLastSeen();
            return tagLastSeen == null || tagLastSeen.before(thresholdTime);
        });

        return out;
    }

    private void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if(this.shutDownTask!=null) {
            InstanceManager.getDefault(ShutDownManager.class).deregister(this.shutDownTask);
        }
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameReporters" : "BeanNameReporter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<IdTag> getNamedBeanClass() {
        return IdTag.class;
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultIdTagManager.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        @Nonnull
        public <T> Object getDefault(Class<T> type) {
            if (type.equals(IdTagManager.class)) {
                return new DefaultIdTagManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
            }
            return super.getDefault(type);
        }

        @Override
        @Nonnull
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(IdTagManager.class);
            return set;
        }
    }

}
