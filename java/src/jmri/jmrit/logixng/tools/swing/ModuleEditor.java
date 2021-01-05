package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;

/**
 * Editor of Module
 * 
 * @author Daniel Bergqvist 2020
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
        
        super(setupRootSocket(new Root(), sName), true, true);
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
        
        ((Root)_femaleRootSocket.getParent())._femaleSocket = _femaleRootSocket;
//        makeEditLogixNGWindow();
    }
    
    private static FemaleSocket setupRootSocket(Base parent, String sName) {
        FemaleSocket socket = new RootSocket(parent, new FemaleSocketListener() {
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
    
    
    
    private static class Root extends AbstractBase {

        private FemaleSocket _femaleSocket;
        
        public Root() {
            super("");
        }
        
        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getParent() {
            return null;
        }

        @Override
        public void setParent(Base parent) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            if (index != 0) throw new IllegalArgumentException("invalid index: "+Integer.toString(index));
            return _femaleSocket;
        }

        @Override
        public int getChildCount() {
            return 1;
        }

        @Override
        public Category getCategory() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isExternal() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Lock getLock() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setLock(Lock lock) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }
    
}
