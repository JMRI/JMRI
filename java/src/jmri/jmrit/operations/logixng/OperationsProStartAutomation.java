package jmri.jmrit.operations.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.util.ThreadingUtil;

/**
 * This action starts an OperationsPro automation.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class OperationsProStartAutomation extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectComboBox _selectAutomation =
            new LogixNG_SelectComboBox(this, new AutomationItem[]{}, null, this);

    private final AutomationManager _automationManager;
    private final List<AutomationItem> _automationList = new ArrayList<>();


    public OperationsProStartAutomation(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);

        _automationManager = InstanceManager.getDefault(AutomationManager.class);
        updateList();
        _automationManager.addPropertyChangeListener(AutomationManager.LISTLENGTH_CHANGED_PROPERTY, this);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        OperationsProStartAutomation copy = new OperationsProStartAutomation(sysName, userName);
        copy.setComment(getComment());
        _selectAutomation.copy(copy._selectAutomation);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectComboBox getSelectAutomations() {
        return _selectAutomation;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return CategoryOperations.OPERATIONS;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        AutomationItem automation = (AutomationItem) _selectAutomation
                .evaluateValue(getConditionalNG());

        if (automation != null) {
            ThreadingUtil.runOnGUIWithJmriException(() -> {
                automation._automation.run();
            });
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "OperationsProStartAutomation_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String automation = _selectAutomation.getDescription(locale);

        return Bundle.getMessage(locale, "OperationsProStartAutomation_Long", automation);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectAutomation.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectAutomation.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (AutomationManager.LISTLENGTH_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
            updateList();
        }
    }

    private void updateList() {
        _automationList.clear();
        for (Automation automation : _automationManager.getAutomationsByNameList()) {
            _automationList.add(new AutomationItem(automation));
        }
        AutomationItem[] automations = _automationList.toArray(AutomationItem[]::new);
        _selectAutomation.setValues(automations);
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public static class AutomationItem implements LogixNG_SelectComboBox.Item {

        private final Automation _automation;

        public AutomationItem(Automation automation) {
            this._automation = automation;
        }

        @Override
        public String getKey() {
            return _automation.getId();
        }

        @Override
        public String toString() {
            return _automation.getName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensor.class);
}
