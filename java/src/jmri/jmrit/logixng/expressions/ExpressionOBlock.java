package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;

/**
 * Evaluates the state of a OBlock.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionOBlock extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private OBlock _oblock;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private OBlock.OBlockStatus _oblockStatus = OBlock.OBlockStatus.Unoccupied;

    public ExpressionOBlock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public void setOBlock(@Nonnull String oblockName) {
        assertListenersAreNotRegistered(log, "setOBlock");
        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getOBlock(oblockName);
        if (oblock != null) {
            setOBlock(oblock);
        } else {
            removeOBlock();
            log.error("oblock \"{}\" is not found", oblockName);
        }
    }
    
    public void setOBlock(@Nonnull OBlock oblock) {
        assertListenersAreNotRegistered(log, "setOBlock");
        _oblock = oblock;
        InstanceManager.getDefault(OBlockManager.class).addVetoableChangeListener(this);
    }
    
    public void removeOBlock() {
        assertListenersAreNotRegistered(log, "setOBlock");
        if (_oblock != null) {
            InstanceManager.getDefault(OBlockManager.class).removeVetoableChangeListener(this);
            _oblock = null;
        }
    }
    
    public OBlock getOBlock() {
        return _oblock;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setOBlockStatus(OBlock.OBlockStatus state) {
        _oblockStatus = state;
    }
    
    public OBlock.OBlockStatus getOBlockStatus() {
        return _oblockStatus;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof OBlock) {
                if (evt.getOldValue().equals(getOBlock())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("OBlock_OBlockInUseOBlockExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof OBlock) {
                if (evt.getOldValue().equals(getOBlock())) {
                    removeOBlock();
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
        if (_oblock == null) return false;
        
        boolean result = (_oblock.getState() == _oblockStatus.getStatus());
        
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return result;
        } else {
            return !result;
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
        return Bundle.getMessage(locale, "OBlock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String oblockName;
        if (_oblock != null) {
            oblockName = _oblock.getDisplayName();
        } else {
            oblockName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "OBlock_Long", oblockName, _is_IsNot.toString(), _oblockStatus.getDescr());
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_oblock != null)) {
            _oblock.addPropertyChangeListener("state", this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _oblock.removePropertyChangeListener("state", this);
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
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionOBlock.class);
    
}
