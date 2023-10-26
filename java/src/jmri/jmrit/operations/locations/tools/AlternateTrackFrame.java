package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.TrackEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame that allows user to select alternate track and options.
 *
 * @author Daniel Boudreau Copyright (C) 2011, 2015
 */
class AlternateTrackFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // combo boxes
    JComboBox<Track> trackBox = new JComboBox<>();

    // radio buttons
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    Track _track;

    public AlternateTrackFrame(TrackEditFrame tef) {
        super(Bundle.getMessage("AlternateTrack"));

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track = tef._track;

        // load the panel
        // row 2
        JPanel pAlternate = new JPanel();
        pAlternate.setLayout(new GridBagLayout());
        pAlternate.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pAlternate, trackBox, 0, 0);

        if (_track != null) {
            updateTrackCombobox();
            _track.getLocation().addPropertyChangeListener(this);
        }

        JPanel pControls = new JPanel();
        pControls.add(saveButton);
        saveButton.setEnabled(_track != null);

        // button action
        addButtonAction(saveButton);

        getContentPane().add(pAlternate);
        getContentPane().add(pControls);
        
        addHelpMenu("package.jmri.jmrit.operations.Operations_AlternateTrack", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight200));
    }

    private void updateTrackCombobox() {
        _track.getLocation().updateComboBox(trackBox);
        trackBox.removeItem(_track); // remove this track from consideration
        trackBox.setSelectedItem(_track.getAlternateTrack());
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // warn user that planned pickups and alternate track don't work
            // together
            if (trackBox.getSelectedItem() != null && _track.getIgnoreUsedLengthPercentage() > Track.IGNORE_0) {
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("PPWarningAlternate"),
                        Bundle.getMessage("PPWarningConfiguration"),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
            _track.setAlternateTrack((Track) trackBox.getSelectedItem());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
            updateTrackCombobox();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AlternateTrackFrame.class);
}
