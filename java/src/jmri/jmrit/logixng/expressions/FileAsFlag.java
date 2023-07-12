package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;


/**
 * Does a file exists? If so, delete the file and return true.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class FileAsFlag extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectFilename =
            new LogixNG_SelectString(this, this);

    private final LogixNG_SelectEnum<DeleteOrKeep> _selectDeleteOrKeep =
            new LogixNG_SelectEnum<>(this, DeleteOrKeep.values(), DeleteOrKeep.Keep, this);


    public FileAsFlag(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        FileAsFlag copy = new FileAsFlag(sysName, userName);
        copy.setComment(getComment());
        _selectFilename.copy(copy._selectFilename);
        _selectDeleteOrKeep.copy(copy._selectDeleteOrKeep);
        return manager.registerExpression(copy);
    }

    public LogixNG_SelectString getSelectFilename() {
        return _selectFilename;
    }

    public LogixNG_SelectEnum<DeleteOrKeep> getSelectDeleteOrKeep() {
        return _selectDeleteOrKeep;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {

        String filename = _selectFilename.evaluateValue(getConditionalNG());
        DeleteOrKeep deleteOrKeep = _selectDeleteOrKeep.evaluateEnum(getConditionalNG());

        if (filename == null) return false;

        try {
            File file = new File(jmri.util.FileUtil.getExternalFilename(filename));
            if (file.exists()) {
                if (deleteOrKeep == DeleteOrKeep.Delete) {
                    Files.delete(file.toPath());
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new JmriException("IOException has occurred: " + e.getLocalizedMessage(), e);
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
        return Bundle.getMessage(locale, "FileAsFlag_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String filename = _selectFilename.getDescription(locale);
        String deleteOrKeep = _selectDeleteOrKeep.getDescription(locale);

        return Bundle.getMessage(locale, "FileAsFlag_Long", filename, deleteOrKeep);
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
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
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
        // Do nothing
    }


    public enum DeleteOrKeep {
        Delete(Bundle.getMessage("FileAsFlag_Delete")),
        Keep(Bundle.getMessage("FileAsFlag_Keep"));

        private final String _text;

        private DeleteOrKeep(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileAsFlag.class);

}
