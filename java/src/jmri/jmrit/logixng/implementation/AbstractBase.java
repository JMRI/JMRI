package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Locale;
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
public abstract class AbstractBase extends AbstractNamedBean implements Base {

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
        if (this instanceof ConditionalNG) {
            return (ConditionalNG) this;
        } else {
            Base parent = getParent();
            while ((parent != null) && (!(parent instanceof ConditionalNG))) {
                parent = parent.getParent();
            }
            return (ConditionalNG) parent;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final LogixNG getLogixNG() {
        if (this instanceof LogixNG) {
            return (LogixNG) this;
        } else {
            Base parent = getParent();
            while ((parent != null) && (!(parent instanceof LogixNG))) {
                parent = parent.getParent();
            }
            return (LogixNG) parent;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        Base current = this;
        Base parent = getParent();
        while (parent != null) {
            current = parent;
            parent = parent.getParent();
        }
        return current;
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
        registerListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).registerListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final void unregisterListeners() {
        unregisterListenersForThisClass();
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean isActive() {
        return isEnabled() && ((getParent() == null) || getParent().isEnabled());
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
