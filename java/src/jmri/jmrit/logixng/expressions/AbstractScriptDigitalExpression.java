package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Locale;

import java.util.Map;

import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Abstract class for scripts that implements a digital expression.
 * <P>
 * A script that extends this class must implement the method evaluate().
 * It may also implement the methods setup(), registerScriptListeners(),
 * unregisterScriptListeners() and disposeMe().
 * <P>
 * If the script needs to run the ConditionalNG that this expression belongs to,
 * it does that by calling the method propertyChange().
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public abstract class AbstractScriptDigitalExpression extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private final DigitalExpression _parentDigitalExpression;


    public AbstractScriptDigitalExpression(DigitalExpression de)
            throws BadUserNameException, BadSystemNameException {
        // This bean is never stored in a manager and
        // its system name and user name is never used.
        super("IQDE0", null);
        _parentDigitalExpression = de;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
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
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getLongDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
    }
    
    /**
     * Register listeners if this object needs that.
     * The script can override this method to register any listener it needs.
     * <P>
     * This method is only called if listeners are not registered.
     */
    protected void registerScriptListeners() {
    }
    
    /**
     * Unregister listeners if this object needs that.
     * The script can override this method to unregister any listener it needs.
     * <P>
     * This method is only called if listeners are registered.
     */
    protected void unregisterScriptListeners() {
    }
    
    /** {@inheritDoc} */
    @Override
    public final void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            registerScriptListeners();
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            unregisterScriptListeners();
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        _parentDigitalExpression.getConditionalNG().execute();
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
}
