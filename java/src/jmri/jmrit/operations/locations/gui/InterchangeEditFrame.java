package jmri.jmrit.operations.locations.gui;

import javax.swing.*;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.tools.*;

/**
 * Frame for user edit of a classification/interchange track.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012, 2025
 */
public class InterchangeEditFrame extends TrackEditFrame {

    public InterchangeEditFrame() {
        super(Bundle.getMessage("AddInterchange"));
    }
    
    @Override
    public void initComponents(Track track) {
        setTitle(Bundle.getMessage("EditInterchange", track.getLocation().getName()));
        initComponents(track.getLocation(), track);
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.INTERCHANGE;

        super.initComponents(location, track);

        _toolMenu.insert(new TrackPriorityAction(_track), 0);
        _toolMenu.insert(new TrackDestinationEditAction(this), 1);
        _toolMenu.insert(new ChangeTrackTypeAction(this), TOOL_MENU_OFFSET + 2);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N
        
        panelQuickService.setVisible(true);

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
