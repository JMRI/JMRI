/*
 * CbusEventFilterFrame.java
 *
 */

package jmri.jmrix.can.cbus;

import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.event.*;

import jmri.util.JmriJFrame;
import jmri.jmrix.can.cbus.swing.console.CbusConsoleFrame;

/**
 * Frame to control an instance of CBUS filter to filter events
 *
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class CbusEventFilterFrame extends JmriJFrame {
    
    // Fields to enter Node Number range
    protected JRadioButton nnEnButton = new JRadioButton();
    protected JTextField nnLowField = new JTextField("", 5);
    protected JTextField nnHighField = new JTextField("", 5);
    // Fields to enter Event range
    protected JRadioButton evEnButton = new JRadioButton();
    protected JTextField evLowField = new JTextField("", 5);
    protected JTextField evHighField = new JTextField("", 5);
    // Buttons to select event type
    protected JRadioButton onButton = new JRadioButton();
    protected JRadioButton offButton = new JRadioButton();
    protected JRadioButton eitherButton = new JRadioButton();
    protected ButtonGroup eventGroup = new ButtonGroup();
    // Color chooser for filter highlighting
    protected JColorChooser fcc = new JColorChooser();
    
    // member to hold reference to my filter
    private CbusEventFilter _filter = null;
    private CbusConsoleFrame _console = null;
    
    /** Creates a new instance of CbusFilterFrame */
    public CbusEventFilterFrame(CbusConsoleFrame console) {
        super();
        log.debug("CbusEventFilterFrame() ctor called");
        _console = console;
    }
    
    public CbusEventFilterFrame(CbusConsoleFrame console, CbusEventFilter f) {
        super();
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor called");
        _filter = f;
        _console = console;
    }
    
    protected String title() { return "CBUS EventFilter"; }
    
    protected void init() {
    }
    
    public void dispose() {
        super.dispose();
        _console.filterClosed();
    }
    
    public void initComponents() throws Exception {
        log.debug("CbusEventFilterFrame initComponents() called");
        setTitle(title());
        // Panels will be added downwards
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add items to GUI
        
        // Pane to hold node number
        JPanel nnPane = new JPanel();
        nnPane.setLayout(new BoxLayout(nnPane, BoxLayout.X_AXIS));
        nnPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Node Number"));
        
        // define contents
        nnEnButton.setText("Enable");
        nnEnButton.setVisible(true);
        nnEnButton.setSelected(true);
        _filter.setNnEnable(true);
        nnEnButton.setToolTipText("Select to enable filtering on Node Number");
        nnPane.add(nnEnButton);
        
        nnLowField.setToolTipText("Enter the Node Number to be filtered");
        nnPane.add(nnLowField);
        
        getContentPane().add(nnPane);

        // Pane to hold Event
        JPanel evPane = new JPanel();
        evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
        evPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Event"));
        
        // define contents
        evEnButton.setText("Enable");
        evEnButton.setVisible(true);
        _filter.setEvEnable(false);
        evEnButton.setToolTipText("Select to enable filtering on Event");
        evPane.add(evEnButton);
        
        evLowField.setToolTipText("Enter the Event to be filtered");
        evPane.add(evLowField);
        
        getContentPane().add(evPane);

        // Pane to hold event type
        JPanel eventPane = new JPanel();
        eventPane.setLayout(new BoxLayout(eventPane, BoxLayout.X_AXIS));
        eventPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Event type"));
        
        // define contents
        onButton.setText("ON");
        onButton.setVisible(true);
        onButton.setToolTipText("Look for ON events");
        eventPane.add(onButton);
        
        offButton.setText("OFF");
        offButton.setVisible(true);
        offButton.setToolTipText("Look for OFF events");
        eventPane.add(offButton);
       
        eitherButton.setText("Either");
        eitherButton.setVisible(true);
        eitherButton.setSelected(true);
        _filter.setType(CbusConstants.EVENT_EITHER);
        eitherButton.setToolTipText("Look for ON or OFF events");
        eventPane.add(eitherButton);
      
        // Add to group to make one-hot
        eventGroup.add(onButton);
        eventGroup.add(offButton);
        eventGroup.add(eitherButton);
       
        getContentPane().add(eventPane);

	// Add color chooser
	fcc.setColor(Color.RED);
//	fcc.getSelectionModel().addChangeListener(this);

//	getContentPane().add(fcc);
        
        // connect actions to buttons
        nnEnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                _filter.setNnEnable(nnEnButton.isSelected());
            }
        });
        evEnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                _filter.setEvEnable(evEnButton.isSelected());
            }
        });
        onButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                _filter.setType(CbusConstants.EVENT_ON);
            }
        });
        offButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                _filter.setType(CbusConstants.EVENT_OFF);
            }
        });
        eitherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                _filter.setType(CbusConstants.EVENT_EITHER);
            }
        });
        nnLowField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                nnLowFieldActionPerformed(e);
            }
        });
        evLowField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                evLowFieldActionPerformed(e);
            }
        });
        
        // connect to data source
        init();
        
        // add help menu to window
        addHelpMenu();
        
        // prevent button areas from expanding
        pack();
        eventPane.setMaximumSize(eventPane.getSize());
        pack();
        
    }
    
    public void nnLowFieldActionPerformed(java.awt.event.ActionEvent e) {
        int nn;
        try {
            nn = Integer.parseInt(nnLowField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid Node Number Entered\n",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            nn = 0;
            return;
        }
        if ((nn > 65535) || (nn < 0)) {
            JOptionPane.showMessageDialog(null, "Invalid Node Number Entered\n",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            nn = 0;
            return;
        }
        _filter.setNn(nn);
    }
    
    public void evLowFieldActionPerformed(java.awt.event.ActionEvent e) {
        int ev;
        try {
            ev = Integer.parseInt(evLowField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid Event Entered\n",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            ev = 0;
            return;
        }
        if ((ev > 65535) || (ev < 0)) {
            JOptionPane.showMessageDialog(null, "Invalid Event Entered\n",
                    "CBUS Console", JOptionPane.ERROR_MESSAGE);
            ev = 0;
            return;
        }
        _filter.setEv(ev);
    }

//    public void stateChanged(ChangeEvent e) {
//        _filter.setColor(fcc.getColor());
//    }
    
    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page
     * that covers general features.  Specific
     * implementations can override this to 
     * show their own help page if desired.
     */
    protected void addHelpMenu() {
        
        // *** TO DO
//    	addHelpMenu("package.jmri.jmrix.can.cbus.CbusEventFilterFrame", true);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusEventFilterFrame.class.getName());
}
