package jmri.jmrit.logixng.implementation;

import java.io.PrintWriter;
import java.util.Locale;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.MaleSocket;

/**
 * The abstract class that is the base class for all LogixNG classes that
 * implements the Base interface.
 */
public abstract class AbstractMaleSocket implements Base, InternalBase {

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
            ((InternalBase)getChild(i)).registerListeners();
        }
    }
    
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
    
    /**
     * Print the tree to a stream.
     * This method is the implementation of printTree(PrintStream, String)
     * 
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     * @param currentIndent the current indentation
     */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);
        
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).printTree(locale, writer, indent, currentIndent+indent);
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
        for (int i=0; i < getChildCount(); i++) {
            getChild(i).dispose();
        }
        disposeMe();
    }
    
}
