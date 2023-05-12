package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * This action sets the state of a turnout.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ShutdownComputer extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectEnum<Operation> _selectEnum =
            new LogixNG_SelectEnum<>(this, Operation.values(), Operation.ShutdownJMRI, this);


    public ShutdownComputer(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ShutdownComputer copy = new ShutdownComputer(sysName, userName);
        copy.setComment(getComment());
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<Operation> getSelectEnum() {
        return _selectEnum;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Operation operation = _selectEnum.evaluateEnum(getConditionalNG());

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            switch (operation) {
                case ShutdownComputer:
                    InstanceManager.getDefault(ShutDownManager.class).shutdownOS();
                    break;

                case RebootComputer:
                    InstanceManager.getDefault(ShutDownManager.class).restartOS();
                    break;

                case ShutdownJMRI:
                    InstanceManager.getDefault(ShutDownManager.class).shutdown();
                    break;

                case RebootJMRI:
                    InstanceManager.getDefault(ShutDownManager.class).restart();
                    break;

                default:
                    throw new RuntimeException("_operation has invalid value: " + operation.name());
            }
        });
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
        return Bundle.getMessage(locale, "ShutdownComputer_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ShutdownComputer_Long", _selectEnum.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectEnum.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }



    public enum Operation {
        ShutdownComputer(Bundle.getMessage("ShutdownComputer_ShutdownComputer")),
        RebootComputer(Bundle.getMessage("ShutdownComputer_RebootComputer")),
        ShutdownJMRI(Bundle.getMessage("ShutdownComputer_ShutdownJMRI")),
        RebootJMRI(Bundle.getMessage("ShutdownComputer_RebootJMRI"));

        private final String _text;

        private Operation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShutdownComputer.class);
}
