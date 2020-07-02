package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.NamedBean;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.MaleSocket;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 */
public abstract class AbstractBase
        extends AbstractNamedBean
        implements Base, InternalBase {

    public AbstractBase(String sys) throws BadSystemNameException {
        super(sys);
    }

    public AbstractBase(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    /** {@inheritDoc} */
    @Override
    public final ConditionalNG getConditionalNG() {
        Base item = this;
        while ((item != null) && !(item instanceof ConditionalNG)) {
            item = item.getParent();
        }
        return (ConditionalNG)item;
    }
    
    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        Base item = this;
        while ((item != null) && !(item instanceof LogixNG)) {
            item = item.getParent();
        }
        return (LogixNG)item;
    }
    
    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        Base item = this;
        while (item.getParent() != null) {
            item = item.getParent();
        }
        return item;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void setParentForAllChildren() {
        for (int i=0; i < getChildCount(); i++) {
            FemaleSocket femaleSocket = getChild(i);
            femaleSocket.setParent(this);
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                connectedSocket.setParent(femaleSocket);
                connectedSocket.setParentForAllChildren();
            }
        }
    }
    
    /**
     * Register listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    abstract protected void registerListenersForThisClass();
    
    /**
     * Unregister listeners if this object needs that.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    abstract protected void unregisterListenersForThisClass();
    
    /** {@inheritDoc} */
    @Override
    public final void registerListeners() {
        if (isActive()) {
            registerListenersForThisClass();
            for (int i=0; i < getChildCount(); i++) {
                ((InternalBase)getChild(i)).registerListeners();
            }
        }
    }
    
    /*.* {@inheritDoc} *./
    @Override
    public final void registerListeners() {
        registerListenersInternal();
//        registerListenersForThisClass();
//        for (int i=0; i < getChildCount(); i++) {
//            getChild(i).registerListeners();
//        }
    }
*/    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        unregisterListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            ((InternalBase)getChild(i)).unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isActive());
    }
    
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        printTree(locale, writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);
        
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).printTree(locale, writer, indent, currentIndent+indent);
        }
    }
    
    /**
     * {@inheritDoc} 
     * 
     * Do a string comparison.
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        return suffix1.compareTo(suffix2);
    }
    
    /**
     * Dispose this class.
     * Listeners do not need to be unregistered by this method since they are
     * unregistered by dispose().
     */
    abstract protected void disposeMe();
    
    /** {@inheritDoc} */
    @Override
    public final void dispose() {
        super.dispose();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).dispose();
        }
        unregisterListeners();
        disposeMe();
    }
    
}
