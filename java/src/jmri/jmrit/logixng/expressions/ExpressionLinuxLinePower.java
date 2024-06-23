package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TimerUtil;

/**
 * Check the status of battery and power supply.
 *
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class ExpressionLinuxLinePower extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private ProtectedTimerTask _timerTask;
    private final int _delay = 5;
    private boolean _lastResult;
    private JmriException _thrownException;

    public ExpressionLinuxLinePower(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);

        try {
            _lastResult = internalEvaluate();
        } catch (JmriException e) {
            _thrownException = e;
        }
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionLinuxLinePower copy = new ExpressionLinuxLinePower(sysName, userName);
        copy.setComment(getComment());
        copy.set_Is_IsNot(_is_IsNot);
        return manager.registerExpression(copy);
    }

    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }

    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.LINUX;
    }

    private List<String> getLinuxPowerSupplies() throws IOException, NoPowerSuppliesException {
        List<String> powerSupplies = new ArrayList<>();

        Process process = Runtime.getRuntime().exec(new String[]{"upower","-e"});
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream())))  {
            String line;
            while ((line = buffer.readLine()) != null) {
                powerSupplies.add(line);
            }
        }

//        powerSupplies.clear();  // For testing only

        if (powerSupplies.isEmpty()) throw new NoPowerSuppliesException();

        return powerSupplies;
    }

    public boolean isLinePowerOnline() throws IOException, JmriException {
        boolean isPowerOnline = false;

        for (String powerSupply : getLinuxPowerSupplies()) {
            Process process = Runtime.getRuntime().exec(new String[]{"upower", "-i", powerSupply});
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream())))  {
                String line;
                boolean linePowerFound = false;
                while ((line = buffer.readLine()) != null) {
                    if (line.isBlank()) continue;

                    if (line.startsWith("  ")) {
                        line = line.substring(2);
                    } else {
                        throw new JmriException("Unknown string. It doesn't start with two spaces.");
                    }

//                    System.out.format("'%s'%n", line);

                    if ("line-power".equals(line)) {
//                        System.out.format("Line power found%n");
                        linePowerFound = true;
                    } else if (line.startsWith("  ")) {
//                        System.out.format("Spaces found%n");
                        line = line.substring(2);
                        if (linePowerFound && line.startsWith("online:")) {
//                            System.out.format("Online found%n");
                            String[] parts = line.split("\\s+");
//                            System.out.format("Line: '%s', part0: '%s', part1: '%s'%n", line, parts[0], parts[1]);
                            if ("yes".equals(parts[1])) {
                                isPowerOnline = true;
                            }
                        }
                    } else {
//                        System.out.format("Other: %s%n", line);
                        linePowerFound = false;
                    }
                }
            }
        }

        return isPowerOnline;
    }

    private boolean internalEvaluate() throws JmriException {

        try {
            if (_is_IsNot == Is_IsNot_Enum.Is) {
                return isLinePowerOnline();
            } else {
                return !isLinePowerOnline();
            }
        } catch (java.io.IOException e) {
            throw new JmriException("IO Exception: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {

        if (_thrownException != null) {
            JmriException e = _thrownException;
            _thrownException = null;
            throw e;
        }

        // Check this every ?? seconds
        return internalEvaluate();
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
        return Bundle.getMessage(locale, "LinuxLinePower_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "LinuxLinePower_Long", _is_IsNot.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _timerTask = new ProtectedTimerTask() {
                @Override
                public void execute() {
                    try {
                        boolean _lastLastResult = _lastResult;
                        _lastResult = internalEvaluate();
                        if (_lastResult != _lastLastResult) {
                            getConditionalNG().execute();
                        }
                    } catch (JmriException e) {
                        _thrownException = e;
                    }
                }
            };

            TimerUtil.schedule(_timerTask, _delay*1000, _delay*1000);
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _timerTask.cancel();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public static class NoPowerSuppliesException extends JmriException {

        /**
         * Creates a new instance of <code>NoPowerSuppliesException</code>.
         */
        public NoPowerSuppliesException() {
            super(Bundle.getMessage("LinuxLinePower_NoPowerSuppliesException"));
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionLinuxLinePower.class);

}
