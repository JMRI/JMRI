package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.ModuleManager;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;
import jmri.jmrit.logixng.implementation.AbstractMaleSocket;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;

/**
 * Editor of Module
 * 
 * @author Daniel Bergqvist 2018
 */
public class ModuleEditor extends TreeEditor implements AbstractLogixNGEditor<Module> {

    BeanTableDataModel<Module> beanTableDataModel;
    
//    private final ModuleManager _moduleManager =
//            InstanceManager.getDefault(ModuleManager.class);
    
    protected final Module _module;
    
    /**
     * Maintain a list of listeners -- normally only one.
     */
    private final List<EditorEventListener> listenerList = new ArrayList<>();
    
    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    final HashMap<String, String> moduleData = new HashMap<>();
    
    /**
     * Create a new ConditionalNG List View editor.
     *
     * @param f the bean table frame
     * @param m the bean table model
     * @param sName name of the LogixNG being edited
     */
    public ModuleEditor(BeanTableFrame<Module> f,
            BeanTableDataModel<Module> m, String sName) {
        
        super(setupRootSocket(sName), true);
/*        
        super(new RootSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                // Do nothing
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                // Do nothing
            }
        }, "Root"), true);
*/        
        this.beanTableDataModel = m;
        
        if (!_femaleRootSocket.isConnected()) {
            // This should never happen
            throw new RuntimeException("Module is not connected");
        }
        if (!(_femaleRootSocket.getConnectedSocket().getObject() instanceof Module)) {
            // This should never happen
            throw new RuntimeException("Connected socket is not a Module");
        }
        _module = (Module) _femaleRootSocket.getConnectedSocket().getObject();
//        makeEditLogixNGWindow();
    }
    
    private static FemaleSocket setupRootSocket(String sName) {
        FemaleSocket socket = new RootSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                // Do nothing
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                // Do nothing
            }
        }, "Root");
        
        Module module = InstanceManager.getDefault(ModuleManager.class).getBySystemName(sName);
        
        try {
            socket.connect(new ModuleEditorMaleSocket(null, module));
        } catch (SocketAlreadyConnectedException e) {
            // This should never happen
            throw new RuntimeException("Socket already connected", e);
        }
        
        return socket;
    }

    /*.*
     * Construct a ModuleEditor.
     * <p>
     * This is used by JmriUserPreferencesManager since it tries to create an
     * instance of this class.
     *./
    public ModuleEditor() {
        super(InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(null, new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }
        }, "A"), false);
        _module = null;
    }
    
    /*.*
     * Construct a ConditionalEditor.
     *
     * @param module the Module to be edited
     *./
    public ModuleEditor(@Nonnull jmri.jmrit.logixng.Module module) {
        super(module.getRootSocket(), true);
        
        _module = module;
        
        if (_module.getUserName() == null) {
            setTitle(Bundle.getMessage("TitleEditModule", _module.getSystemName()));
        } else {
            setTitle(Bundle.getMessage("TitleEditModule2", _module.getSystemName(), _module.getUserName()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        moduleData.clear();
        moduleData.put("Finish", _module.getSystemName());  // NOI18N
        fireModuleEvent();
    }
    
    /**
     * Notify the listeners to check for new data.
     */
    void fireModuleEvent() {
        for (EditorEventListener l : listenerList) {
            l.editorEventOccurred(moduleData);
        }
    }

    @Override
    public void addEditorEventListener(EditorEventListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeEditorEventListener(EditorEventListener listener) {
        listenerList.remove(listener);
    }

    @Override
    public void bringToFront() {
        this.setVisible(true);
    }
    
    
    private static class RootSocket extends AbstractFemaleSocket {

        public RootSocket(Base parent, FemaleSocketListener listener, String name) {
            super(parent, listener, name);
        }
        
        @Override
        public boolean canDisconnect() {
            return false;
        }
        
        @Override
        public void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }
        
        @Override
        public boolean isCompatible(MaleSocket socket) {
            return socket instanceof ModuleEditorMaleSocket;
        }
        
        @Override
        public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
//            Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
//            List<Class<? extends Base>> list = new ArrayList<>();
//            map.put(Category.OTHER, list);
//            list.add(Module.class);
//            return map;
            throw new UnsupportedOperationException("Not supported");
        }
        
        @Override
        public String getShortDescription(Locale locale) {
            return Bundle.getMessage(locale, "ModuleEditor_RootSocket_Short");
        }
        
        @Override
        public String getLongDescription(Locale locale) {
            return Bundle.getMessage(locale, "ModuleEditor_RootSocket_Long", getName());
        }
        
    }
    
    
    private static class ModuleEditorMaleSocket extends AbstractMaleSocket {

        Module _module;
        
        public ModuleEditorMaleSocket(BaseManager<? extends NamedBean> manager, Module module) {
            super(manager);
            _module = module;
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
            _module.dispose();
        }

        @Override
        public void setEnabled(boolean enable) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public Base getObject() {
            return _module;
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
        public String getSystemName() {
            return _module.getSystemName();
        }

        @Override
        public String getUserName() {
            return _module.getUserName();
        }

        @Override
        public String getComment() {
            return _module.getComment();
        }

        @Override
        public void setUserName(String s) throws NamedBean.BadUserNameException {
            _module.setUserName(s);
        }

        @Override
        public void setComment(String comment) {
            _module.setComment(comment);
        }

        @Override
        public String getShortDescription(Locale locale) {
            return _module.getShortDescription(locale);
        }

        @Override
        public String getLongDescription(Locale locale) {
            return _module.getLongDescription(locale);
        }

        @Override
        public ConditionalNG getConditionalNG() {
            return null;
        }

        @Override
        public LogixNG getLogixNG() {
            return null;
        }

        @Override
        public Base getRoot() {
            return _module.getRoot();
        }

        @Override
        public FemaleSocket getChild(int index)
                throws IllegalArgumentException, UnsupportedOperationException {
            return _module.getChild(index);
        }

        @Override
        public int getChildCount() {
            return _module.getChildCount();
        }

        @Override
        public Category getCategory() {
            return _module.getCategory();
        }

        @Override
        public boolean isExternal() {
            return _module.isExternal();
        }

        @Override
        public Lock getLock() {
            return _module.getLock();
        }

        @Override
        public void setLock(Lock lock) {
            _module.setLock(lock);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
            _module.addPropertyChangeListener(listener, name, listenerRef);
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
            _module.addPropertyChangeListener(propertyName, listener, name, listenerRef);
        }

        @Override
        public void updateListenerRef(PropertyChangeListener l, String newName) {
            _module.updateListenerRef(l, newName);
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            _module.vetoableChange(evt);
        }

        @Override
        public String getListenerRef(PropertyChangeListener l) {
            return _module.getListenerRef(l);
        }

        @Override
        public ArrayList<String> getListenerRefs() {
            return _module.getListenerRefs();
        }

        @Override
        public int getNumPropertyChangeListeners() {
            return _module.getNumPropertyChangeListeners();
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
            return _module.getPropertyChangeListenersByReference(name);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            _module.addPropertyChangeListener(listener);
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            _module.addPropertyChangeListener(propertyName, listener);
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners() {
            return _module.getPropertyChangeListeners();
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            return _module.getPropertyChangeListeners(propertyName);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            _module.removePropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            _module.removePropertyChangeListener(propertyName, listener);
        }
        
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGEditor.class);

}
