package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Sets a StringIO.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringActionStringIO extends AbstractStringAction
        implements VetoableChangeListener {

    private NamedBeanHandle<StringIO> _stringIOHandle;

    public StringActionStringIO(String sys, String user) {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        StringActionManager manager = InstanceManager.getDefault(StringActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        StringActionStringIO copy = new StringActionStringIO(sysName, userName);
        copy.setComment(getComment());
        if (_stringIOHandle != null) copy.setStringIO(_stringIOHandle);
        return manager.registerAction(copy);
    }

    public void setStringIO(@Nonnull String stringIOName) {
        assertListenersAreNotRegistered(log, "setStringIO");
        StringIO stringIO = InstanceManager.getDefault(StringIOManager.class).getNamedBean(stringIOName);
        if (stringIO != null) {
            setStringIO(stringIO);
        } else {
            removeStringIO();
            log.error("stringIO \"{}\" is not found", stringIOName);
        }
    }

    public void setStringIO(@Nonnull NamedBeanHandle<StringIO> handle) {
        assertListenersAreNotRegistered(log, "setStringIO");
        _stringIOHandle = handle;
        InstanceManager.getDefault(StringIOManager.class).addVetoableChangeListener(this);
    }

    public void setStringIO(@Nonnull StringIO stringIO) {
        assertListenersAreNotRegistered(log, "setStringIO");
        setStringIO(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(stringIO.getDisplayName(), stringIO));
    }

    public void removeStringIO() {
        assertListenersAreNotRegistered(log, "setStringIO");
        if (_stringIOHandle != null) {
            InstanceManager.getDefault(StringIOManager.class).removeVetoableChangeListener(this);
            _stringIOHandle = null;
        }
    }

    public NamedBeanHandle<StringIO> getStringIO() {
        return _stringIOHandle;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(String value) throws JmriException {
        if (_stringIOHandle != null) {
            _stringIOHandle.getBean().setCommandedStringValue(value);
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof StringIO) {
                if (evt.getOldValue().equals(getStringIO().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("StringStringIO_StringIOInUseStringIOExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof StringIO) {
                if (evt.getOldValue().equals(getStringIO().getBean())) {
                    removeStringIO();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringActionStringIO_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        if (_stringIOHandle != null) {
            return Bundle.getMessage(locale, "StringActionStringIO_Long", _stringIOHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "StringActionStringIO_Long", "none");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: StringActionStringIO: bean = {}, report = {}", cdl, report);
        if (getStringIO() != null && bean.equals(getStringIO().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionStringIO.class);

}
