package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
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
            while (! (parent instanceof ConditionalNG)) {
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
    
    /**
     * Print the tree to a stream.
     * This method is the implementation of printTree(PrintStream, String)
     * 
     * @param writer the stream to print the tree to
     * @param currentIndent the current indentation
     */
    protected void printTreeRow(PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription());
        writer.println();
    }
    
    /**
     * Print the tree to a stream.
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(writer, indent, "");
    }
    
    /**
     * Print the tree to a stream.
     * This method is the implementation of printTree(PrintStream, String)
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param currentIndent the current indentation
     */
    @Override
    public void printTree(PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(writer, currentIndent);
        
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).printTree(writer, indent, currentIndent+indent);
        }
    }
    
    /**
     * Disposes this object.
     * This must remove _all_ connections!
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
