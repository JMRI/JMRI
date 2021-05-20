package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.jmrit.logixng.FemaleSocket;
import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * Editor of ConditionalNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class ConditionalNGEditor extends TreeEditor {

    protected final ConditionalNG _conditionalNG;
    
    
    /**
     * Maintain a list of listeners -- normally only one.
     */
    private final List<ConditionalNGEventListener> listenerList = new ArrayList<>();
    
    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    final HashMap<String, String> logixNGData = new HashMap<>();
    
    /**
     * Construct a ConditionalEditor.
     * <p>
     * This is used by JmriUserPreferencesManager since it tries to create an
     * instance of this class.
     */
    public ConditionalNGEditor() {
        super(InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(null, new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }
        }, "A"),
                EnableClipboard.EnableClipboard,
                EnableRootRemoveCutCopy.EnableRootRemoveCutCopy,
                EnableRootPopup.EnableRootPopup
        );
        
        _conditionalNG = null;
    }
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param conditionalNG the ConditionalNG to be edited
     */
    public ConditionalNGEditor(@Nonnull ConditionalNG conditionalNG) {
        super(conditionalNG.getFemaleSocket(),
                EnableClipboard.EnableClipboard,
                EnableRootRemoveCutCopy.EnableRootRemoveCutCopy,
                EnableRootPopup.EnableRootPopup
        );
        
        _conditionalNG = conditionalNG;
        
        if (_conditionalNG.getUserName() == null) {
            ConditionalNGEditor.this.setTitle(
                    Bundle.getMessage("TitleEditConditionalNG",
                            _conditionalNG.getSystemName()));
        } else {
            ConditionalNGEditor.this.setTitle(
                    Bundle.getMessage("TitleEditConditionalNG2", 
                            _conditionalNG.getSystemName(),
                            _conditionalNG.getUserName()));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        logixNGData.clear();
        logixNGData.put("Finish", _conditionalNG.getSystemName());  // NOI18N
        fireLogixNGEvent();
    }
    
    public void addLogixNGEventListener(ConditionalNGEventListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Notify the listeners to check for new data.
     */
    void fireLogixNGEvent() {
        for (ConditionalNGEventListener l : listenerList) {
            l.conditionalNGEventOccurred();
        }
    }
    
    
    public interface ConditionalNGEventListener extends EventListener {
        
        public void conditionalNGEventOccurred();
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGEditor.class);

}
