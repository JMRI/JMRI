package jmri.jmrit.logixng.template;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.SignalHead;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;

/**
 * A null signal head.
 */
public class NullSignalHead extends AbstractNullNamedBean implements SignalHead {

    private Base _parent = null;
    private Lock _lock = Lock.NONE;

    /**
     * Create a new NullSignalHead instance using only a system name.
     *
     * @param sys the system name for this bean; must not be null and must
     *            be unique within the layout
     */
    public NullSignalHead(@Nonnull String sys) {
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
        return Bundle.getMessage("BeanNameSignalHead");
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
    public int getAppearance() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAppearance(int newAppearance) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getAppearanceName() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getAppearanceName(int appearance) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean getLit() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLit(boolean newLit) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean getHeld() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setHeld(boolean newHeld) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int[] getValidStates() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String[] getValidStateNames() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isCleared() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isShowingRestricting() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isAtStop() {
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
