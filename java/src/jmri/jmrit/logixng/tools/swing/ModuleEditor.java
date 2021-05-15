package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.jmrit.beantable.BeanTableFrame;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
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
        
        super(setupRootSocket(null, sName), true, true, false);
        
        this.beanTableDataModel = m;
        
        if (!_treePane._femaleRootSocket.isConnected()) {
            // This should never happen
            throw new RuntimeException("Module is not connected");
        }
        if (!(_treePane._femaleRootSocket.getConnectedSocket().getObject() instanceof Module)) {
            // This should never happen
            throw new RuntimeException("Connected socket is not a Module");
        }
        _module = (Module) _treePane._femaleRootSocket.getConnectedSocket().getObject();
        
        if (_module.getUserName() == null) {
            setTitle(Bundle.getMessage("TitleEditModule", _module.getSystemName()));
        } else {
            setTitle(Bundle.getMessage("TitleEditModule2", _module.getSystemName(), _module.getUserName()));
        }
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
    
}
