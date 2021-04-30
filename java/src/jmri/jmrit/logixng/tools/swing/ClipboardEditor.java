package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.util.*;
import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * Editor of the clipboard
 * 
 * @author Daniel Bergqvist 2020
 */
public class ClipboardEditor extends TreeEditor {

    /**
     * Maintain a list of listeners -- normally only one.
     */
    private final List<ClipboardEventListener> listenerList = new ArrayList<>();
    
    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    final HashMap<String, String> clipboardData = new HashMap<>();
    
    /**
     * Construct a ConditionalEditor.
     */
    public ClipboardEditor() {
        super(InstanceManager.getDefault(LogixNG_Manager.class).getClipboard().getFemaleSocket(), false, false);
        
        setTitle(Bundle.getMessage("TitleClipboardEditor"));
        
        setRootVisible(false);
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        clipboardData.clear();
        clipboardData.put("Finish", "Clipboard");  // NOI18N
        fireClipboardEvent();
    }
    
    public void addClipboardEventListener(ClipboardEventListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Notify the listeners to check for new data.
     */
    void fireClipboardEvent() {
        for (ClipboardEventListener l : listenerList) {
            l.clipboardEventOccurred();
        }
    }
    
    
    public interface ClipboardEventListener extends EventListener {
        
        public void clipboardEventOccurred();
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClipboardEditor.class);

}
