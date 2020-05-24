package jmri.jmrit.logixng.digital.actions;

import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.DigitalActionPlugin;
import jmri.jmrit.logixng.implementation.InternalBase;

/**
 * This class has a plugin class.
 */
public class DigitalActionPluginSocket extends AbstractDigitalAction {

    private final DigitalActionPlugin _actionPlugin;
    
    public DigitalActionPluginSocket(
            @Nonnull String sys, @Nonnull String user, @Nonnull DigitalActionPlugin actionPlugin) {
        super(sys, user);
        Objects.requireNonNull(actionPlugin, "parameter actionPlugin must not be null");
        _actionPlugin = actionPlugin;
    }
    
    @Override
    public Category getCategory() {
        return _actionPlugin.getCategory();
    }

    @Override
    public boolean isExternal() {
        return _actionPlugin.isExternal();
    }

    @Override
    public void execute() throws JmriException {
        _actionPlugin.execute();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        return _actionPlugin.getChild(index);
    }

    @Override
    public int getChildCount() {
        return _actionPlugin.getChildCount();
    }
    
    @Override
    public String getShortDescription(Locale locale) {
        return _actionPlugin.getShortDescription(locale);
    }

    @Override
    public String getLongDescription(Locale locale) {
        return _actionPlugin.getLongDescription(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        _actionPlugin.setup();
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        ((InternalBase)_actionPlugin).registerListeners();
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        ((InternalBase)_actionPlugin).unregisterListeners();
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        _actionPlugin.dispose();
    }

}
