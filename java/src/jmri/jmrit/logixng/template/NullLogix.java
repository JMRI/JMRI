package jmri.jmrit.logixng.template;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.Conditional;
import jmri.Logix;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;

/**
 * A null logix.
 */
public class NullLogix extends AbstractNullNamedBean implements Logix {

//    static final ResourceBundle rbm = ResourceBundle.getBundle("jmri.implementation.ImplementationBundle");
    
    private Base _parent = null;
    private Lock _lock = Lock.NONE;

    /**
     * Create a new NullLogix instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullLogix(@Nonnull String sys) {
        super(sys);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameLogix");
//        return rbm.getString("BeanNameLogix");
    }

    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
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
        return getShortDescription(locale);
    }

    @Override
    public void setEnabled(boolean state) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean getEnabled() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getNumConditionals() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void swapConditional(int nextInOrder, int row) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getConditionalByNumberOrder(int order) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addConditional(String systemName, int order) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addConditional(String systemName, Conditional conditional) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Conditional getConditional(String systemName) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String[] deleteConditional(String systemName) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void calculateConditionals() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void activateLogix() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void deActivateLogix() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setGuiNames() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return _parent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        return _lock;
    }

    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        _lock = lock;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    @Override
    public Base getNewObjectBasedOnTemplate() {
        throw new UnsupportedOperationException("Not supported.");
    }

}
