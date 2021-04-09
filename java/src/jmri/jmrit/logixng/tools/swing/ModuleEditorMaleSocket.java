package jmri.jmrit.logixng.tools.swing;

import java.beans.*;
import java.util.ArrayList;
import java.util.Locale;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;

/**
 * MaleSocket for a Module.
 * This class is used by the ModuleEditor class
 * 
 * @author Daniel Bergqvist 2020
 */
class ModuleEditorMaleSocket extends AbstractMaleSocket {
    
//    Module _module;

    public ModuleEditorMaleSocket(BaseManager<? extends NamedBean> manager, Module module) {
        super(manager, module);
    }

    @Override
    protected void registerListenersForThisClass() {
        // Do nothing
    }

    @Override
    protected void unregisterListenersForThisClass() {
        // Do nothing
    }

    @Override
    protected void disposeMe() {
        ((Module)getObject()).dispose();
    }

    @Override
    public void setEnabled(boolean enable) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void setEnabledFlag(boolean enable) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setDebugConfig(DebugConfig config) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DebugConfig getDebugConfig() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DebugConfig createDebugConfig() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getComment() {
        return ((Module)getObject()).getComment();
    }

    @Override
    public void setComment(String comment) {
        ((Module)getObject()).setComment(comment);
    }

}
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGEditor.class);
