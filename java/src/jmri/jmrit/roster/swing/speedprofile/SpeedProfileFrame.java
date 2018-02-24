package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import javax.swing.JOptionPane;

/**
 * Frame Entry Exit Frames
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SpeedProfileFrame extends jmri.util.JmriJFrame {

    public SpeedProfileFrame() {
        super(false, true);
    }

    SpeedProfilePanel spPanel;

    @Override
    public void initComponents() {
        // the following code sets the frame's initial state

        spPanel = new SpeedProfilePanel();

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
            if (JOptionPane.showConfirmDialog(this,  Bundle.getMessage("SaveProfile"), 
                    Bundle.getMessage("SpeedProfile"), JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                spPanel.updateSpeedProfileWithResults();            
            }
        }
        if (spPanel != null && spPanel.table !=null) {
            spPanel.table.dispose();
        }
    }

}
