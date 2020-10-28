package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
// import jmri.implementation.JmriSimplePropertyListener;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.ModuleManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of LogixNG.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultModule extends AbstractNamedBean
        implements Module {
    
    private FemaleSocket _rootSocket;
    
    
    public DefaultModule(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(ModuleManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        throw new UnsupportedOperationException("A Module cannot have a parent");
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameModule");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in DefaultModule.");  // NOI18N
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultModule.");  // NOI18N
        return UNKNOWN;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultModule.class);

    @Override
    public String getShortDescription(Locale locale) {
        return "Module";
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "Module: "+getDisplayName();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLock(Lock lock) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    final public void setup() {
        _rootSocket.setup();
    }
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG getLogixNG() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void setParentForAllChildren() {
        _rootSocket.setParent(this);
        _rootSocket.setParentForAllChildren();
    }

    /*.* {@inheritDoc} *./
    @Override
    public void registerListeners() {
        throw new UnsupportedOperationException("Not supported");
    }

    /*.* {@inheritDoc} *./
    @Override
    public void unregisterListeners() {
        throw new UnsupportedOperationException("Not supported");
    }
*/    
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

        _rootSocket.printTree(locale, writer, indent, currentIndent+indent);
    }

    @Override
    public void setRootSocket(FemaleSocket rootSocket) {
        if (_rootSocket.isConnected()) throw new RuntimeException("Cannot set root socket when it's connected");
        _rootSocket = rootSocket;
    }

    @Override
    public FemaleSocket getRootSocket() {
        return _rootSocket;
    }

    @Override
    public boolean isActive() {
        // A module is always active.
        return true;
    }

}
