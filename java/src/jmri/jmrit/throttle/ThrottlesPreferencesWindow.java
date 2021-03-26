package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.util.JmriJFrame;

/**
 * A frame to display and edit Throttles preferences
 * 
 * @author Lionel Jeanson - 2021
 * 
 */
public final class ThrottlesPreferencesWindow extends JmriJFrame {
    
    private JButton jbApply;
    private JButton jbCancel;
    private JButton jbSave;
    private ThrottlesPreferencesPane tpP;
    
    ThrottlesPreferencesWindow(String title) {
        super(title);
        initComponents();
    }
    
    @Override
    public void initComponents() {
        super.initComponents();
        
        tpP = new ThrottlesPreferencesPane();
        
        jbCancel = new JButton();
        jbSave = new JButton();
        jbApply = new JButton();

        jbSave.setText(Bundle.getMessage("ButtonSave"));
        jbSave.addActionListener((ActionEvent e) -> {
            tpP.savePreferences();
            setVisible(false);
        });

        jbCancel.setText(Bundle.getMessage("ButtonCancel"));
        jbCancel.addActionListener((ActionEvent e) -> {
            setVisible(false);
        });

        jbApply.setText(Bundle.getMessage("ButtonApply"));
        jbApply.addActionListener((ActionEvent e) -> {
            tpP.applyPreferences();
        });

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setLayout(new GridLayout(1, 2, 4, 4));
        buttonsPanel.add(jbCancel);
        buttonsPanel.add(jbApply);
        buttonsPanel.add(jbSave);
        
        add(tpP, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);        
    }
    
    public void resetComponents() {
        tpP.resetComponents();
    }
    
}
