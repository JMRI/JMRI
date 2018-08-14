package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationEditFrame;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to change all of tracks at a location to the same type of track. Track
 * types are Spurs, Yards, Interchanges and Staging.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * 
 */
class ChangeTracksFrame extends OperationsFrame {

    // radio buttons
    JRadioButton spurRadioButton = new JRadioButton(Bundle.getMessage("Spur"));
    JRadioButton yardRadioButton = new JRadioButton(Bundle.getMessage("Yard"));
    JRadioButton interchangeRadioButton = new JRadioButton(Bundle.getMessage("Interchange"));
    JRadioButton stagingRadioButton = new JRadioButton(Bundle.getMessage("Staging"));

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    private LocationEditFrame _lef;
    private Location _location;

    public ChangeTracksFrame(LocationEditFrame lef) {
        super(Bundle.getMessage("MenuItemChangeTrackType"));

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _lef = lef;
        if (_lef._location == null) {
            log.debug("location is null, change location track types not possible");
            return;
        }
        _location = _lef._location;

        // load the panel
        // row 1a
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(Bundle.getMessage("TrackType"),
                new Object[]{_location.getName()})));
        addItem(p1, spurRadioButton, 0, 0);
        addItem(p1, yardRadioButton, 1, 0);
        addItem(p1, interchangeRadioButton, 2, 0);
        addItem(p1, stagingRadioButton, 3, 0);
        
        JPanel p2 = new JPanel();
        p2.add(saveButton);

        // group and set current track type
        ButtonGroup group = new ButtonGroup();
        group.add(spurRadioButton);
        group.add(yardRadioButton);
        group.add(interchangeRadioButton);
        group.add(stagingRadioButton);
        
        stagingRadioButton.setSelected(_location.isStaging());

        // button action
        addButtonAction(saveButton);

        getContentPane().add(p1);
        getContentPane().add(p2);
        
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ChangeTrackTypeLocation", true); // NOI18N
        
        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // check to see if button has changed
            if (spurRadioButton.isSelected()) {
                changeTracks(Track.SPUR);
            } else if (yardRadioButton.isSelected()) {
                changeTracks(Track.YARD);
            } else if (interchangeRadioButton.isSelected()) {
                changeTracks(Track.INTERCHANGE);
            } else if (stagingRadioButton.isSelected()) {
                changeTracks(Track.STAGING);
            }
        }
    }

    private void changeTracks(String type) {
        log.debug("change tracks to {}", type);
        List<Track> tracks = _location.getTrackByNameList(null);
        for (Track track : tracks) {
            track.setTrackType(type);
        }
        if (type.equals(Track.STAGING)) {
            _location.setLocationOps(Location.STAGING);
        } else {
            _location.setLocationOps(Location.NORMAL);
        }
        OperationsXml.save();
        _lef.dispose();
        dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(ChangeTracksFrame.class);
}
