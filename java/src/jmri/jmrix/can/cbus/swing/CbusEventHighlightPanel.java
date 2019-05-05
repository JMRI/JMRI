package jmri.jmrix.can.cbus.swing;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.cbus.CbusConstants;

/**
 *
 * @author Andrew Crosland
 * @author Steve Young Copyright (C) 2018
 */
public class CbusEventHighlightPanel extends JPanel {
    
    private int _index;
    private CbusEventHighlightFrame _highlightFrame;
    private Boolean _isActive;
    
    protected JCheckBox evEnButton = new JCheckBox();
    protected JCheckBox nnEnButton = new JCheckBox();
    protected JSpinner eventnumberspinner = new JSpinner(new SpinnerNumberModel(1,1, 65535, 1));
    protected JSpinner nodenumberspinner = new JSpinner(new SpinnerNumberModel(0,0, 65535, 1));

    // Buttons to select event type
    protected JCheckBox onButton = new JCheckBox();
    protected JCheckBox offButton = new JCheckBox();
    
    protected JCheckBox inButton = new JCheckBox();
    protected JCheckBox outButton = new JCheckBox();

    /**
     * Create a new instance of CbusEventHighlightPanel.
     */
    public CbusEventHighlightPanel(CbusEventHighlightFrame highlightFrame, int index) {
        super();
        _index = index;
        _highlightFrame = highlightFrame;
    }

    protected CbusEventHighlightPanel() {
        super();
    }

    public void initComponents(int index) {
        _isActive = false;
        // Panels will be added across
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Pane to hold Event
        JPanel evPane = new JPanel();
        evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
        evPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("EventCol")));

        // define contents
        evEnButton.setText(Bundle.getMessage("ButtonEnable"));
        evEnButton.setVisible(true);
        evEnButton.setSelected(true);
        evEnButton.setToolTipText(Bundle.getMessage("EnableEventTooltip"));
        evPane.add(evEnButton);
        
        eventnumberspinner.setToolTipText(Bundle.getMessage("EvLowfieldTooltip"));
        evPane.add(eventnumberspinner);
        
        JComponent compEv = eventnumberspinner.getEditor();
        JFormattedTextField fieldEv = (JFormattedTextField) compEv.getComponent(0);
        DefaultFormatter formatterEv = (DefaultFormatter) fieldEv.getFormatter();
        fieldEv.setColumns(2);
        formatterEv.setCommitsOnValidEdit(true);
        
        this.add(evPane);

        // Pane to hold node number
        JPanel nnPane = new JPanel();
        nnPane.setLayout(new BoxLayout(nnPane, BoxLayout.X_AXIS));
        nnPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("NodeNumberTitle")));

        // define contents
        nnEnButton.setText(Bundle.getMessage("ButtonEnable"));
        nnEnButton.setVisible(true);
        nnEnButton.setToolTipText(Bundle.getMessage("EnableNodeTooltip"));
        nnPane.add(nnEnButton);

        JComponent compNd = nodenumberspinner.getEditor();
        JFormattedTextField fieldNd = (JFormattedTextField) compNd.getComponent(0);
        DefaultFormatter formatterNd = (DefaultFormatter) fieldNd.getFormatter();
        fieldNd.setColumns(2);
        formatterNd.setCommitsOnValidEdit(true); 
        
        nodenumberspinner.setToolTipText(Bundle.getMessage("NnLowfieldTooltip"));
        nnPane.add(nodenumberspinner);

        this.add(nnPane);

        // Pane to hold event type
        JPanel eventPane = new JPanel();
        eventPane.setLayout(new BoxLayout(eventPane, BoxLayout.X_AXIS));
        eventPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("EventTypeTitle")));

        // define contents
        onButton.setText(Bundle.getMessage("CbusEventOn"));
        onButton.setVisible(true);
        onButton.setToolTipText(Bundle.getMessage("OnEventsTooltip"));
        eventPane.add(onButton);

        offButton.setText(Bundle.getMessage("CbusEventOff"));
        offButton.setVisible(true);
        offButton.setToolTipText(Bundle.getMessage("OffEventsTooltip"));
        eventPane.add(offButton);

        this.add(eventPane);

        // Pane to hold event type
        JPanel directionPane = new JPanel();
        directionPane.setLayout(new BoxLayout(directionPane, BoxLayout.X_AXIS));
        directionPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("EventdirectionTitle")));

        // define contents
        inButton.setText(Bundle.getMessage("InEventsButton"));
        inButton.setVisible(true);
        inButton.setToolTipText(Bundle.getMessage("InEventsTooltip"));
        directionPane.add(inButton);

        outButton.setText(Bundle.getMessage("OutEventsButton"));
        outButton.setVisible(true);
        outButton.setToolTipText(Bundle.getMessage("OutEventsTooltip"));
        directionPane.add(outButton);

        this.add(directionPane);
        
        eventnumberspinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( evEnButton.isSelected() ) {
                    setoptions();
                }
            }
        });
        
        nodenumberspinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if ( nnEnButton.isSelected() ) {
                    setoptions();
                }
            }
        });
        
        // update if already active
        nnEnButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });
        
        // update if already active
        evEnButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });

        // update if already active
        onButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });
        
        // update if already active
        offButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });
        
        // update if already active
        inButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });
        
        // update if already active
        outButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setoptions();
            }
        });

    }
        
    private void setoptions(){    
        
        int nn = 0;
        int ev = 0;
        int ty = CbusConstants.EVENT_NEITHER;
        int dr = CbusConstants.EVENT_DIR_UNSET;
        if (nnEnButton.isSelected()) {
            nn = (Integer) nodenumberspinner.getValue();
        }

        if (evEnButton.isSelected()) {
            ev = (Integer) eventnumberspinner.getValue();
        }

        if ( onButton.isSelected() ) {
            ty = CbusConstants.EVENT_ON;
        }
        if ( offButton.isSelected() ) {
            ty = CbusConstants.EVENT_OFF;
        }
        
        if ( onButton.isSelected() && offButton.isSelected() ) {
            ty = CbusConstants.EVENT_EITHER;
        }
        
        if ( inButton.isSelected()) {
            dr = dr + CbusConstants.EVENT_DIR_IN;
        }
        if ( outButton.isSelected()) {
            dr = dr + CbusConstants.EVENT_DIR_OUT;
        }
        
        if (( dr > 0 ) && ( _isActive ) && ( ty !=CbusConstants.EVENT_NEITHER ) ) {
            _highlightFrame.enable(_index, nn, nnEnButton.isSelected(), ev, evEnButton.isSelected(), ty, dr);
        }
        if (( dr > 0 ) && ( !_isActive ) && ( ty !=CbusConstants.EVENT_NEITHER ) ) {
            _highlightFrame.enable(_index, nn, nnEnButton.isSelected(), ev, evEnButton.isSelected(), ty, dr);
            _isActive=true;
        }        
        
        if ( ( ( dr == 0 ) || ( ty == CbusConstants.EVENT_NEITHER ) ) && ( _isActive ) ) {
            _highlightFrame.disable(_index);
            _isActive=false;
        }
    }
}
