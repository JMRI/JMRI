package jmri.jmrit.logixng.tools.swing;

import java.awt.Dimension;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.logixng.FemaleSocket;
import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.tools.debugger.Debugger;
import jmri.util.JmriJFrame;

/**
 * Editor of ConditionalNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class ConditionalNGDebugger extends JmriJFrame {

    private static final int panelWidth = 700;
    private static final int panelHeight = 500;
    
    private final Debugger _debugger = InstanceManager.getDefault(Debugger.class);
    
    private final TreePane _treePane;
    
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
    
    /*.*
     * Construct a ConditionalEditor.
     * <p>
     * This is used by JmriUserPreferencesManager since it tries to create an
     * instance of this class.
     *./
    public ConditionalNGDebugger() {
        _treePane = new TreePane(InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(null, new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }, "A"));
        _conditionalNG = null;
    }
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param conditionalNG the ConditionalNG to be edited
     */
    public ConditionalNGDebugger(@Nonnull ConditionalNG conditionalNG) {
        _treePane = new TreePane(conditionalNG.getFemaleSocket());
        _treePane.initComponents();
        
        _conditionalNG = conditionalNG;
        
        if (_conditionalNG.getUserName() == null) {
            setTitle(Bundle.getMessage("TitleEditConditionalNG", _conditionalNG.getSystemName()));
        } else {
            setTitle(Bundle.getMessage("TitleEditConditionalNG2", _conditionalNG.getSystemName(), _conditionalNG.getUserName()));
        }
        
        JPanel variablePanel = new JPanel();
        JScrollPane variableScrollPane = new JScrollPane(variablePanel);
        
        JPanel watchPanel = new JPanel();
        JScrollPane watchScrollPane = new JScrollPane(watchPanel);
        
        JSplitPane watchSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           variableScrollPane, watchScrollPane);
        watchSplitPane.setOneTouchExpandable(true);
        watchSplitPane.setDividerLocation(150);
        
        // Provide minimum sizes for the two components in the split pane
        Dimension minimumWatchSize = new Dimension(100, 50);
        variableScrollPane.setMinimumSize(minimumWatchSize);
        watchScrollPane.setMinimumSize(minimumWatchSize);
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           _treePane, watchSplitPane);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setDividerLocation(150);
        
        // Provide minimum sizes for the two components in the split pane
        Dimension minimumMainSize = new Dimension(100, 50);
        _treePane.setMinimumSize(minimumMainSize);
        watchSplitPane.setMinimumSize(minimumMainSize);
        
        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(mainSplitPane);
        
        initMinimumSize(new Dimension(panelWidth, panelHeight));
        
        _debugger.activateDebugger(conditionalNG);
    }
    
    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        _debugger.deActivateDebugger();
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
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGDebugger.class);

}
