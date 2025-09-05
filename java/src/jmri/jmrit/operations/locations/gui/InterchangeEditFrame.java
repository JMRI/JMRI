package jmri.jmrit.operations.locations.gui;

import java.awt.GridBagLayout;

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

    JCheckBox quickServiceCheckBox = new JCheckBox(Bundle.getMessage("QuickService"));

    JPanel panelQuickService = panelOpt4;

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

        // setup the optional panel with quick service checkbox
        panelQuickService.setLayout(new GridBagLayout());
        panelQuickService.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("QuickService")));
        addItem(panelQuickService, quickServiceCheckBox, 0, 0);
        quickServiceCheckBox.setToolTipText(Bundle.getMessage("QuickServiceTip"));

        super.initComponents(location, track);

        _toolMenu.insert(new TrackPriorityAction(_track), 0);
        _toolMenu.insert(new TrackDestinationEditAction(this), 1);
        _toolMenu.insert(new ChangeTrackTypeAction(this), TOOL_MENU_OFFSET + 2);
        addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N

        // override text strings for tracks
        // panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainInterchange")));
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesInterchange")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteInterchange"));
        addTrackButton.setText(Bundle.getMessage("AddInterchange"));
        saveTrackButton.setText(Bundle.getMessage("SaveInterchange"));

        // setup the check boxes
        if (track != null) {
            quickServiceCheckBox.setSelected(track.isQuickServiceEnabled());
        }

        // finish
        pack();
        setVisible(true);
    }

    @Override
    protected void enableButtons(boolean enabled) {
        quickServiceCheckBox.setEnabled(enabled);
        super.enableButtons(enabled);
    }

    @Override
    protected void saveTrack(Track track) {
        track.setQuickServiceEnabled(quickServiceCheckBox.isSelected());
        super.saveTrack(track);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Track.LOAD_OPTIONS_CHANGED_PROPERTY)) {
            quickServiceCheckBox.setSelected(_track.isQuickServiceEnabled());
        }
        super.propertyChange(e);
    }

//    private final static Logger log = LoggerFactory.getLogger(InterchangeEditFrame.class);
}
