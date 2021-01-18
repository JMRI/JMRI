package jmri.jmrit.logixng.tools.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
public class ConditionalNGDebugger extends JmriJFrame implements PropertyChangeListener {

    private static final int panelWidth = 700;
    private static final int panelHeight = 500;
    
    private final Debugger _debugger = InstanceManager.getDefault(Debugger.class);
    private final TreePane _treePane;
    private final JMenuItem _stepOverItem;
    private final JMenuItem _stepIntoItem;
    protected final ConditionalNG _conditionalNG;
    private MaleSocket _currentMaleSocket;
    private State _currentState = State.None;
    
    private final Object _lock = new Object();
    
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
        
        _conditionalNG = conditionalNG;
        
        _treePane = new TreePane(conditionalNG.getFemaleSocket());
        _treePane.initComponents((FemaleSocket femaleSocket, JPanel panel) -> {
            if (_currentMaleSocket != null) {
                Base parent = _currentMaleSocket.getParent();
                while ((parent != null) && (!(parent instanceof FemaleSocket))) {
                    parent = parent.getParent();
                }
                if (parent == femaleSocket) {
                    JPanel newPanel = new JPanel();
                    switch (_currentState) {
                        case Before:
                            newPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 1, 1, Color.red));
                            break;
                        case After:
                            newPanel.setBorder(BorderFactory.createMatteBorder(1, 5, 5, 1, Color.red));
                            break;
                        default:
                    }
                    newPanel.add(panel);
                    return newPanel;
                }
            }
            return panel;
        });
        
        // build menu
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem closeWindowItem = new JMenuItem(Bundle.getMessage("CloseWindow"));
        closeWindowItem.addActionListener((ActionEvent e) -> {
            dispose();
        });
        fileMenu.add(closeWindowItem);
        menuBar.add(fileMenu);
        
        JMenu debugMenu = new JMenu(Bundle.getMessage("Debug_MenuDebug"));
        
        _stepOverItem = new JMenuItem(Bundle.getMessage("Debug_MenuItem_StepOver"));
        _stepIntoItem = new JMenuItem(Bundle.getMessage("Debug_MenuItem_StepInto"));
        _stepOverItem.setEnabled(false);
        _stepIntoItem.setEnabled(false);
        
        _stepOverItem.addActionListener((ActionEvent e) -> {
            MaleSocket maleSocket = _currentMaleSocket;
            _currentMaleSocket = null;
            _currentState = State.None;
            _stepOverItem.setEnabled(false);
            _stepIntoItem.setEnabled(false);
            _treePane.updateTree(maleSocket);
            synchronized(_lock) {
                _lock.notify();
            }
        });
        debugMenu.add(_stepOverItem);
        
        _stepIntoItem.addActionListener((ActionEvent e) -> {
            MaleSocket maleSocket = _currentMaleSocket;
            _currentMaleSocket = null;
            _currentState = State.None;
            _stepOverItem.setEnabled(false);
            _stepIntoItem.setEnabled(false);
            _treePane.updateTree(maleSocket);
            synchronized(_lock) {
                _lock.notify();
            }
        });
        debugMenu.add(_stepIntoItem);
        
        menuBar.add(debugMenu);
        
        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N
        
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
        
        _debugger.addPropertyChangeListener(this);
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Debugger.STEP_BEFORE.equals(evt.getPropertyName())
                || Debugger.STEP_AFTER.equals(evt.getPropertyName())) {
            
            jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
                _currentMaleSocket = (MaleSocket) evt.getNewValue();
                switch (evt.getPropertyName()) {
                    case Debugger.STEP_BEFORE:
                        _currentState = State.Before;
                        break;
                    case Debugger.STEP_AFTER:
                        _currentState = State.After;
                        break;
                    default:
                        _currentState = State.None;
                }
                _stepOverItem.setEnabled(true);
                _stepIntoItem.setEnabled(true);
                _treePane.updateTree(_currentMaleSocket);
            });
            
            try {
                synchronized(_lock) {
                    _lock.wait();
                }
            } catch (InterruptedException e) {
                log.error("LogixNG thread was interrupted: {}", _conditionalNG.getCurrentThread().getThreadName());
                Thread.currentThread().interrupt();
            }
        }
    }
    
    
    public interface ConditionalNGEventListener extends EventListener {
        
        public void conditionalNGEventOccurred();
    }
    
    
    private static enum State {
        None,
        Before,
        After,
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConditionalNGDebugger.class);

}
