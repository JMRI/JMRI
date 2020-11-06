package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates the state of a Conditional.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class ExpressionConditional extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private Conditional _conditional;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private ConditionalState _conditionalState = ConditionalState.True;

    public ExpressionConditional(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setConditional(@Nonnull String conditionalName) {
        assertListenersAreNotRegistered(log, "setConditional");
        Conditional conditional = InstanceManager.getDefault(ConditionalManager.class).getConditional(conditionalName);
        if (conditional != null) {
            setConditional(conditional);
        } else {
            removeConditional();
            log.error("conditional \"{}\" is not found", conditionalName);
        }
    }
    
    public void setConditional(@Nonnull Conditional conditional) {
        assertListenersAreNotRegistered(log, "setConditional");
        _conditional = conditional;
        InstanceManager.getDefault(ConditionalManager.class).addVetoableChangeListener(this);
    }
    
    public void removeConditional() {
        assertListenersAreNotRegistered(log, "setConditional");
        if (_conditional != null) {
            InstanceManager.getDefault(ConditionalManager.class).removeVetoableChangeListener(this);
            _conditional = null;
        }
    }
    
    public Conditional getConditional() {
        return _conditional;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setConditionalState(ConditionalState state) {
        _conditionalState = state;
    }
    
    public ConditionalState getConditionalState() {
        return _conditionalState;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Conditional) {
                if (evt.getOldValue().equals(getConditional())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Conditional_ConditionalInUseConditionalExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Conditional) {
                if (evt.getOldValue().equals(getConditional())) {
                    removeConditional();
                }
            }
        }
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
    public boolean evaluate() {
        if (_conditional == null) return false;
        
        ConditionalState currentConditionalState = ConditionalState.get(_conditional.getState());
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentConditionalState == _conditionalState;
        } else {
            return currentConditionalState != _conditionalState;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        return Bundle.getMessage(locale, "Conditional_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String conditionalName;
        if (_conditional != null) {
            conditionalName = _conditional.getDisplayName();
        } else {
            conditionalName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "Conditional_Long", conditionalName, _is_IsNot.toString(), _conditionalState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_conditional != null)) {
            _conditional.addPropertyChangeListener("KnownState", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _conditional.removePropertyChangeListener("KnownState", this);
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum ConditionalState {
        False(Conditional.FALSE, Bundle.getMessage("ConditionalStateFalse")),
        True(Conditional.TRUE, Bundle.getMessage("ConditionalStateTrue")),
        Other(-1, Bundle.getMessage("ConditionalOtherStatus"));
        
        private final int _id;
        private final String _text;
        
        private ConditionalState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public ConditionalState get(int id) {
            switch (id) {
                case Conditional.FALSE:
                    return False;
                    
                case Conditional.TRUE:
                    return True;
                    
                default:
                    return Other;
            }
        }
        
        public int getID() {
            return _id;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionConditional.class);
    
}
