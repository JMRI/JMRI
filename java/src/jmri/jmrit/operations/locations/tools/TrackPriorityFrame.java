package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
class TrackPriorityFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    JRadioButton priorityHigh = new JRadioButton(Bundle.getMessage("High"));
    JRadioButton priorityMedium = new JRadioButton(Bundle.getMessage("Medium"));
    JRadioButton priorityNormal = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton priorityLow = new JRadioButton(Bundle.getMessage("Low"));

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    protected Track _track;

    public TrackPriorityFrame(Track track) {
        super(Bundle.getMessage("MenuItemTrackPriority"));
        _track = track;
        initComponents();
    }

    @Override
    public void initComponents() {
        if (_track == null) {
            return;
        }
        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track.addPropertyChangeListener(this);

        // load the panel
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));
        
        // row 1
        JPanel pt = new JPanel();
        pt.setLayout(new BoxLayout(pt, BoxLayout.X_AXIS));
        pt.setMaximumSize(new Dimension(2000, 250));

        // row 1a
        JPanel pTrackName = new JPanel();
        pTrackName.setLayout(new GridBagLayout());
        pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        addItem(pTrackName, new JLabel(_track.getName()), 0, 0);

        // row 1b
        JPanel pLocationName = new JPanel();
        pLocationName.setLayout(new GridBagLayout());
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLocationName, new JLabel(_track.getLocation().getName()), 0, 0);

        pt.add(pTrackName);
        pt.add(pLocationName);
        
        // radio buttons
        JPanel pRadioButtons = new JPanel();
        pRadioButtons.setLayout(new GridBagLayout());
        pRadioButtons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MenuItemTrackPriority")));
        addItem(pRadioButtons, priorityHigh, 0, 0);
        addItem(pRadioButtons, priorityMedium, 1, 0);
        addItem(pRadioButtons, priorityNormal, 2, 0);
        addItem(pRadioButtons, priorityLow, 3, 0);

        ButtonGroup group = new ButtonGroup();
        group.add(priorityHigh);
        group.add(priorityMedium);
        group.add(priorityNormal);
        group.add(priorityLow);

        updateRadioButtons();

        JPanel savePriority = new JPanel();
        savePriority.setLayout(new GridBagLayout());
        savePriority.setBorder(BorderFactory.createTitledBorder(""));
        addItem(savePriority, saveButton, 0, 0);
        addButtonAction(saveButton);

        p1.add(pt);
        p1.add(pRadioButtons);
        p1.add(savePriority);

        getContentPane().add(p1Pane);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrackPriority", true); // NOI18N
        
        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            save();
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }
    
    private void save() {
        if (priorityHigh.isSelected()) {
            _track.setTrackPriority(Track.PRIORITY_HIGH);
        } else if (priorityMedium.isSelected()) {
            _track.setTrackPriority(Track.PRIORITY_MEDIUM);
        } else if (priorityLow.isSelected()) {
            _track.setTrackPriority(Track.PRIORITY_LOW);
        } else {
            _track.setTrackPriority(Track.PRIORITY_NORMAL);
        }
    }
    
    private void updateRadioButtons() {
        priorityHigh.setSelected(_track.getTrackPriority().equals(Track.PRIORITY_HIGH));
        priorityMedium.setSelected(_track.getTrackPriority().equals(Track.PRIORITY_MEDIUM));
        priorityNormal.setSelected(_track.getTrackPriority().equals(Track.PRIORITY_NORMAL));
        priorityLow.setSelected(_track.getTrackPriority().equals(Track.PRIORITY_LOW));
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Track.PRIORITY_CHANGED_PROPERTY)) {
            updateRadioButtons();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackPriorityFrame.class);
}
