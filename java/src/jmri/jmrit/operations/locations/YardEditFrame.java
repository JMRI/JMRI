package jmri.jmrit.operations.locations;

import javax.swing.BorderFactory;

import jmri.jmrit.operations.locations.tools.ChangeTrackTypeAction;
import jmri.jmrit.operations.locations.tools.IgnoreUsedTrackAction;
import jmri.jmrit.operations.locations.tools.ShowCarsByLocationAction;
import jmri.jmrit.operations.locations.tools.ShowTrainsServingLocationAction;

/**
 * Frame for user edit of a yard
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
public class YardEditFrame extends TrackEditFrame {

    public YardEditFrame() {
        super();
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.YARD;
        super.initComponents(location, track);

        _toolMenu.add(new IgnoreUsedTrackAction(this));
        _toolMenu.add(new ChangeTrackTypeAction(this));
        _toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
        _toolMenu.add(new ShowCarsByLocationAction(false, _location, _track));
        addHelpMenu("package.jmri.jmrit.operations.Operations_Yards", true); // NOI18N

        // override text strings for tracks
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainYard")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesYard")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteYard"));
        addTrackButton.setText(Bundle.getMessage("AddYard"));
        saveTrackButton.setText(Bundle.getMessage("SaveYard"));
        // finish
        dropPanel.setVisible(false); // don't show drop and pick up panel
        pickupPanel.setVisible(false);
        pack();
        setVisible(true);
    }

//    private final static Logger log = LoggerFactory.getLogger(YardEditFrame.class);
}
