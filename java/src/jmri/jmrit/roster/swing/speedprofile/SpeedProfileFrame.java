package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;

import jmri.util.swing.JmriJOptionPane;

/**
 * Frame Entry Exit Frames
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SpeedProfileFrame extends jmri.util.JmriJFrame {

    public SpeedProfileFrame() {
        super(false, true);
        spPanel = new SpeedProfilePanel();
    }

    SpeedProfilePanel spPanel;

    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        setTitle(Bundle.getMessage("SpeedProfile"));
        getContentPane().setLayout(new BorderLayout(15,15));

        getContentPane().add(spPanel);

        addHelpMenu("package.jmri.jmrit.roster.swing.speedprofile.SpeedProfileFrame", true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                windowClosingEvent();
            }
        });
        // pack for display
        pack();
    }
    
    public void windowClosingEvent() {
        spPanel.cancelButton();
        if (spPanel.save) {
            if (JmriJOptionPane.showConfirmDialog(this,  Bundle.getMessage("SaveProfile"), 
                    Bundle.getMessage("SpeedProfile"), JmriJOptionPane.YES_NO_OPTION, 
                    JmriJOptionPane.QUESTION_MESSAGE) == JmriJOptionPane.YES_OPTION) {
                spPanel.updateSpeedProfileWithResults();            
            }
        }
        if (spPanel.table !=null) {
            spPanel.table.dispose();
        }
    }

}
