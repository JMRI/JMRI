// PollTablePane.java

package jmri.jmrix.rps.swing.polling;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.jmrix.rps.Engine;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane for user management of RPS polling.
 
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class PollTablePane extends javax.swing.JPanel {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");
		
    PollDataModel pollModel = null;

    /**
     * Constructor method
     */
    public PollTablePane() {
    	super();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        pollModel = new PollDataModel();

        JTable pollTable = jmri.util.JTableUtil.sortableDataModel(pollModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
                pollTable.setDefaultRenderer(JButton.class,buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
                pollTable.setDefaultEditor(JButton.class,buttonEditor);
        pollTable.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        pollTable.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        
        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter)pollTable.getModel());
            tmodel.setSortingStatus(pollModel.ADDRCOL, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {}  // if not a sortable table model
        pollTable.setRowSelectionAllowed(false);
        pollTable.setPreferredScrollableViewportSize(new java.awt.Dimension(580,80));

        JScrollPane scrollPane = new JScrollPane(pollTable);
        add(scrollPane);
        
        // status info on bottom
        JPanel p = new JPanel() {
            public Dimension getMaximumSize() { 
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height); }
        };
        p.setLayout(new FlowLayout());

        polling = new JCheckBox(rb.getString("LabelPoll"));
        p.add(polling);
        polling.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                    checkPolling();
            }
        });
        p.add(Box.createHorizontalGlue());
        p.add(new JLabel(rb.getString("LabelDelay")));
        delay = new JTextField(5);
        delay.setText(""+Engine.instance().getPollingInterval());
        p.add(delay);
        delay.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                    updateInterval();
            }
        });
        
        
        add(p);
    }
    
    JTextField delay;
    JCheckBox polling;
    
    /**
     * Start or stop the polling
     */
    void checkPolling() {
        Engine.instance().setPolling(polling.isSelected());
    }
    
    /**
     * The requested interval has changed, update it
     */
    void updateInterval() {
        int interval = Integer.parseInt(delay.getText());
        log.debug("set interval to "+interval);
        Engine.instance().setPollingInterval(interval);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollTablePane.class.getName());

}
