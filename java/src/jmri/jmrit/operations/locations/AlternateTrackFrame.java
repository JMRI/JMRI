//AlternateTrackAction.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to launch selection of alternate track.
 *
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
class AlternateTrackFrame extends OperationsFrame {

    /**
     *
     */
    private static final long serialVersionUID = -9027155799954540567L;

    // combo boxes
    JComboBox<Track> trackBox = new JComboBox<>();

    // radio buttons
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("Save"));

    Track _track;

    public AlternateTrackFrame(TrackEditFrame tef) {
        super();

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track = tef._track;

        // load the panel
        // row 2
        JPanel pAlternate = new JPanel();
        pAlternate.setLayout(new GridBagLayout());
        pAlternate.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pAlternate, trackBox, 0, 0);

        _track.getLocation().updateComboBox(trackBox);
        trackBox.removeItem(_track);	// remove this track from consideration
        trackBox.setSelectedItem(_track.getAlternateTrack());

        JPanel pControls = new JPanel();
        pControls.add(saveButton);

        // button action
        addButtonAction(saveButton);

        getContentPane().add(pAlternate);
        getContentPane().add(pControls);

        setTitle(Bundle.getMessage("AlternateTrack"));
        pack();
        setMinimumSize(new Dimension(Control.panelWidth300, Control.panelHeight100));
        setVisible(true);
    }

    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            _track.setAlternateTrack((Track) trackBox.getSelectedItem());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    static Logger log = LoggerFactory.getLogger(TrackEditFrame.class.getName());
}
