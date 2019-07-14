package jmri.jmrit.operations.locations;

import javax.swing.BorderFactory;

import jmri.jmrit.operations.locations.tools.ChangeTrackTypeAction;
import jmri.jmrit.operations.locations.tools.IgnoreUsedTrackAction;
import jmri.jmrit.operations.locations.tools.ShowCarsByLocationAction;
import jmri.jmrit.operations.locations.tools.ShowTrainsServingLocationAction;
import jmri.jmrit.operations.locations.tools.TrackDestinationEditAction;

/**
 * Frame for user edit of a classification/interchange track. Adds two panels to
 * TrackEditFrame for train/route car drops and pulls.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012
 */
public class InterchangeEditFrame extends TrackEditFrame {

    public InterchangeEditFrame() {
        super();
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.INTERCHANGE;

        super.initComponents(location, track);

        _toolMenu.add(new IgnoreUsedTrackAction(this));
        _toolMenu.add(new TrackDestinationEditAction(this));
        _toolMenu.add(new ChangeTrackTypeAction(this));
        _toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
        _toolMenu.add(new ShowCarsByLocationAction(false, _location, _track));
        addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N

        // override text strings for tracks
        // panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainInterchange")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesInterchange")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteInterchange"));
        addTrackButton.setText(Bundle.getMessage("AddInterchange"));
        saveTrackButton.setText(Bundle.getMessage("SaveInterchange"));

        // finish
        pack();
        setVisible(true);
    }

//    private final static Logger log = LoggerFactory.getLogger(InterchangeEditFrame.class);
}
