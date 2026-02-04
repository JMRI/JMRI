package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.FileUtil;

/**
 * This action executes a program.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ExecuteProgram extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectProgram =
            new LogixNG_SelectString(this, this);
    private final LogixNG_SelectStringList _selectParameters =
            new LogixNG_SelectStringList();
    private final LogixNG_SelectString _selectWorkingDirectory =
            new LogixNG_SelectString(this, "", this);


    public ExecuteProgram(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException, JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExecuteProgram copy = new ExecuteProgram(sysName, userName);
        copy.setComment(getComment());
        _selectProgram.copy(copy._selectProgram);
        _selectParameters.copy(copy._selectParameters);
        _selectWorkingDirectory.copy(copy._selectWorkingDirectory);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectString getSelectProgram() {
        return _selectProgram;
    }

    public LogixNG_SelectStringList getSelectParameters() {
        return _selectParameters;
    }

    public LogixNG_SelectString getSelectWorkingDirectory() {
        return _selectWorkingDirectory;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        final ConditionalNG conditionalNG;

        synchronized(this) {
            conditionalNG = getConditionalNG();
        }

        String program = _selectProgram.evaluateValue(conditionalNG);
        List<String> parameters = _selectParameters.evaluateValue(conditionalNG);
        String workingDirectory = _selectWorkingDirectory.evaluateValue(conditionalNG);

        File workingDirectoryFile;
        if (!workingDirectory.isBlank()) {
            workingDirectoryFile = new File(workingDirectory);
        } else {
            // Ensure the default folder is the preferences folder.
            workingDirectoryFile = new File(FileUtil.getUserFilesPath());
        }

        List<String> programAndParameters = new ArrayList<>();
        programAndParameters.add(program);
        programAndParameters.addAll(parameters);

        try {
            Runtime.getRuntime().exec(
                    programAndParameters.toArray(String[]::new),
                    null,
                    workingDirectoryFile);
        } catch (IOException e) {
            throw new JmriException(e);
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ExecuteProgram_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String program = _selectProgram.getDescription(locale);
        return Bundle.getMessage(locale, "ExecuteProgram_Long", program);
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
            _selectWorkingDirectory.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectProgram.unregisterListeners();
        _selectWorkingDirectory.unregisterListeners();
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgram.class);

}
