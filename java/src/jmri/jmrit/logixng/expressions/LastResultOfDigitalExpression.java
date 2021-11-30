package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Returns the last result of a digital expression.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class LastResultOfDigitalExpression extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<DigitalExpressionBean> _digitalExpressionHandle;
    
    public LastResultOfDigitalExpression(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        LastResultOfDigitalExpression copy = new LastResultOfDigitalExpression(sysName, userName);
        copy.setComment(getComment());
        if (_digitalExpressionHandle != null) copy.setDigitalExpression(_digitalExpressionHandle);
        return manager.registerExpression(copy);
    }
    
    public void setDigitalExpression(@Nonnull String digitalExpressionName) {
        assertListenersAreNotRegistered(log, "setDigitalExpression");
        DigitalExpressionBean digitalExpression =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .getNamedBean(digitalExpressionName);
        if (digitalExpression != null) {
            // We need the male socket, not the expression itself
            if (!(digitalExpression instanceof MaleSocket)) {
                digitalExpression = (DigitalExpressionBean)digitalExpression.getParent();
            }
            setDigitalExpression(digitalExpression);
        } else {
            removeDigitalExpression();
            log.error("digitalExpression \"{}\" is not found", digitalExpressionName);
        }
    }
    
    public void setDigitalExpression(@Nonnull NamedBeanHandle<DigitalExpressionBean> handle) {
        assertListenersAreNotRegistered(log, "setDigitalExpression");
        _digitalExpressionHandle = handle;
        InstanceManager.getDefault(DigitalExpressionManager.class).addVetoableChangeListener(this);
    }
    
    public void setDigitalExpression(@Nonnull DigitalExpressionBean digitalExpression) {
        assertListenersAreNotRegistered(log, "setDigitalExpression");
        setDigitalExpression(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(digitalExpression.getDisplayName(), digitalExpression));
    }
    
    public void removeDigitalExpression() {
        assertListenersAreNotRegistered(log, "setDigitalExpression");
        if (_digitalExpressionHandle != null) {
            InstanceManager.getDefault(DigitalExpressionManager.class).removeVetoableChangeListener(this);
            _digitalExpressionHandle = null;
        }
    }
    
    public NamedBeanHandle<DigitalExpressionBean> getDigitalExpression() {
        return _digitalExpressionHandle;
    }
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DigitalExpression) {
                if (evt.getOldValue().equals(getDigitalExpression().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("LastResultOfDigitalExpression_BeanInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof DigitalExpression) {
                if (evt.getOldValue().equals(getDigitalExpression().getBean())) {
                    removeDigitalExpression();
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        if (_digitalExpressionHandle != null) {
            MaleSocket m = (MaleSocket) _digitalExpressionHandle.getBean();
            while ((m != null) && (! (m instanceof MaleDigitalExpressionSocket))) {
                m = (MaleSocket) m.getObject();
            }
            if (m == null) {
                throw new RuntimeException("The digital expression "
                        + _digitalExpressionHandle.getName()
                        + " is not contained in a DigitalExpressionSocket");
            }
            return ((MaleDigitalExpressionSocket)m).getLastResult();
        }
        return false;
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
        return Bundle.getMessage(locale, "LastResultOfDigitalExpression_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        String beanName;
        if (_digitalExpressionHandle != null) {
            beanName = _digitalExpressionHandle.getBean().getDisplayName();
        } else {
            beanName = Bundle.getMessage(locale, "BeanNotSelected");
        }
        return Bundle.getMessage(locale, "LastResultOfDigitalExpression_Long",
                beanName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_digitalExpressionHandle != null)) {
            _digitalExpressionHandle.getBean()
                    .addPropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _digitalExpressionHandle.getBean()
                    .removePropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
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
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastResultOfDigitalExpression.class);

}
