package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.TrackEditFrame;
import jmri.jmrit.operations.setup.Control;

/**
 * Action to change the type of track. Track types are Spurs, Yards,
 * Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
class ChangeTrackFrame extends OperationsFrame {

    // radio buttons
    JRadioButton spurRadioButton = new JRadioButton(Bundle.getMessage("Spur"));
    JRadioButton yardRadioButton = new JRadioButton(Bundle.getMessage("Yard"));
    JRadioButton interchangeRadioButton = new JRadioButton(Bundle.getMessage("Interchange"));
    ButtonGroup group = new ButtonGroup();

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    private Track _track;
    private TrackEditFrame _tef;

    public ChangeTrackFrame(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _tef = tef;
        _track = tef._track;
        if (_track == null) {
            log.debug("track is null, change track not possible");
            return;
        }
        String trackName = _track.getName();

        // load the panel
        // row 1a
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(Bundle.getMessage("TrackType"), new Object[]{trackName})));
        addItem(p1, spurRadioButton, 0, 0);
        addItem(p1, yardRadioButton, 1, 0);
        addItem(p1, interchangeRadioButton, 2, 0);
        
        JPanel p2 = new JPanel();
        p2.add(saveButton);

        // group and set current track type
        group.add(spurRadioButton);
        group.add(yardRadioButton);
        group.add(interchangeRadioButton);

        spurRadioButton.setSelected(_track.isSpur());
        yardRadioButton.setSelected(_track.isYard());
        interchangeRadioButton.setSelected(_track.isInterchange());

        // Can not change staging tracks!
        saveButton.setEnabled(!_track.isStaging());

        // button action
        addButtonAction(saveButton);

        getContentPane().add(p1);
        getContentPane().add(p2);
        
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ChangeTrackType", true); // NOI18N
        
        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // check to see if button has changed
            if (spurRadioButton.isSelected() && !_track.isSpur()) {
                changeTrack(Track.SPUR);
            } else if (yardRadioButton.isSelected() && !_track.isYard()) {
                changeTrack(Track.YARD);
            } else if (interchangeRadioButton.isSelected() && !_track.isInterchange()) {
                changeTrack(Track.INTERCHANGE);
            }
        }
    }

    private void changeTrack(String type) {
        log.debug("change track to {}", type);
        _track.setTrackType(type);
        OperationsXml.save();
        _tef.dispose();
        dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ChangeTrackFrame.class);
}
