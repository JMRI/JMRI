package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Sets a Memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class StringActionMemory extends AbstractStringAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Memory> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    public StringActionMemory(String sys, String user) {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        StringActionManager manager = InstanceManager.getDefault(StringActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        StringActionMemory copy = new StringActionMemory(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Memory> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public void setValue(String value) throws JmriException {
        Memory memory = _selectNamedBean.evaluateNamedBean(getConditionalNG());
        if (memory != null) {
            memory.setValue(value);
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
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringActionMemory_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringActionMemory_Long", _selectNamedBean.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectNamedBean.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionMemory.class);

}
