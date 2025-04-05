package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;

/**
 * This action executes a program.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ExecuteProgram extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectProgram =
            new LogixNG_SelectString(this, this);
    private final LogixNG_SelectString _selectParameters =
            new LogixNG_SelectString(this, this);
    private String _resultLocalVariable = "";


    public ExecuteProgram(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExecuteProgram copy = new ExecuteProgram(sysName, userName);
        copy.setComment(getComment());
        _selectProgram.copy(copy._selectProgram);
        _selectParameters.copy(copy._selectParameters);
        copy.setResultLocalVariable(_resultLocalVariable);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectString getSelectProgram() {
        return _selectProgram;
    }

    public LogixNG_SelectString getSelectParameters() {
        return _selectParameters;
    }

    public void setResultLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setResultLocalVariable");
        _resultLocalVariable = localVariable;
    }

    public String getResultLocalVariable() {
        return _resultLocalVariable;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        final ConditionalNG conditionalNG = getConditionalNG();

        String program = _selectProgram.evaluateValue(conditionalNG);
        String parameters = _selectParameters.evaluateValue(conditionalNG);

        List<String> result = new ArrayList<>();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{program,parameters});
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getInputStream())))  {
                String line;
                while ((line = buffer.readLine()) != null) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            throw new JmriException(e);
        }

        if (_resultLocalVariable != null && !_resultLocalVariable.isBlank()) {
            String res = String.join("\n", result);
            conditionalNG.getSymbolTable().setValue(_resultLocalVariable, res);
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
        return Bundle.getMessage(locale, "ExecuteProgram_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String program = _selectProgram.getDescription(locale);
        String parameters = _selectParameters.getDescription(locale);

        return Bundle.getMessage(locale, "ExecuteProgram_Long", program, parameters);
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
            _selectProgram.registerListeners();
            _selectParameters.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectProgram.unregisterListeners();
        _selectParameters.unregisterListeners();
        _listenersAreRegistered = false;
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


    public enum MemoryOperation {
        SetToNull(Bundle.getMessage("ActionMemory_MemoryOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionMemory_MemoryOperation_SetToString")),
        CopyVariableToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyVariableToMemory")),
        CopyMemoryToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyMemoryToMemory")),
        CopyTableCellToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyTableCellToMemory")),
        CalculateFormula(Bundle.getMessage("ActionMemory_MemoryOperation_CalculateFormula"));

        private final String _text;

        private MemoryOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgram.class);

}
