package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jmri.jmrit.operations.locations.tools.ShowCarsByLocationAction;
import jmri.jmrit.operations.locations.tools.ShowTrainsServingLocationAction;
import jmri.jmrit.operations.locations.tools.TrackDestinationEditAction;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.trains.Train;

/**
 * Frame for user edit of a staging track
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011
 */
public class StagingEditFrame extends TrackEditFrame {

    // check boxes
    JCheckBox swapLoadsCheckBox = new JCheckBox(Bundle.getMessage("SwapCarLoads"));
    JCheckBox emptyCheckBox = new JCheckBox(Bundle.getMessage("EmptyDefaultCarLoads"));
    JCheckBox emptyCustomCheckBox = new JCheckBox(Bundle.getMessage("EmptyCarLoads"));
    JCheckBox loadCheckBox = new JCheckBox(Bundle.getMessage("LoadCarLoads"));
    JCheckBox loadAnyCheckBox = new JCheckBox(Bundle.getMessage("LoadAnyCarLoads"));
    JCheckBox loadAnyStagingCheckBox = new JCheckBox(Bundle.getMessage("LoadsStaging"));
    JCheckBox blockCarsCheckBox = new JCheckBox(Bundle.getMessage("BlockCars"));

    JPanel panelLoad = panelOpt4;

    public StagingEditFrame() {
        super();
    }

    @Override
    public void initComponents(Location location, Track track) {
        _type = Track.STAGING;

        // setup the optional panel with staging stuff
        panelLoad.setLayout(new BoxLayout(panelLoad, BoxLayout.X_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("OptionalLoads")));
        addItemLeft(p1, swapLoadsCheckBox, 0, 0);
        addItemLeft(p1, emptyCheckBox, 0, 1);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("OptionalCustomLoads")));
        addItemLeft(p2, emptyCustomCheckBox, 0, 0);
        addItemLeft(p2, loadCheckBox, 0, 1);
        addItemLeft(p2, loadAnyCheckBox, 0, 2);
        addItemLeft(p2, loadAnyStagingCheckBox, 0, 3);

        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("OptionalBlocking")));
        addItemLeft(p3, blockCarsCheckBox, 0, 0);

        // load tool tips
        loadCheckBox.setToolTipText(Bundle.getMessage("TipIgnoresAlternate"));
        blockCarsCheckBox.setToolTipText(Bundle.getMessage("TipBlockByPickUp"));

        panelLoad.add(p1);
        panelLoad.add(p2);
        panelLoad.add(p3);

        super.initComponents(location, track);

        _toolMenu.add(new TrackDestinationEditAction(this));
        _toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
        _toolMenu.add(new ShowCarsByLocationAction(false, _location, _track));
        addHelpMenu("package.jmri.jmrit.operations.Operations_Staging", true); // NOI18N

        // override text strings for tracks
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainStaging")));
        paneCheckBoxes
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesStaging")));
        dropPanel
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectTrainArrival")));
        pickupPanel.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("SelectTrainDeparture")));
        deleteTrackButton.setText(Bundle.getMessage("DeleteStaging"));
        addTrackButton.setText(Bundle.getMessage("AddStaging"));
        saveTrackButton.setText(Bundle.getMessage("SaveStaging"));

        // setup the check boxes
        if (_track != null) {
            swapLoadsCheckBox.setSelected(_track.isLoadSwapEnabled());
            emptyCheckBox.setSelected(_track.isLoadEmptyEnabled());
            emptyCustomCheckBox.setSelected(_track.isRemoveCustomLoadsEnabled());
            loadCheckBox.setSelected(_track.isAddCustomLoadsEnabled());
            loadAnyCheckBox.setSelected(_track.isAddCustomLoadsAnySpurEnabled());
            loadAnyStagingCheckBox.setSelected(_track.isAddCustomLoadsAnyStagingTrackEnabled());
            blockCarsCheckBox.setSelected(_track.isBlockCarsEnabled());
            if (loadCheckBox.isSelected() || loadAnyCheckBox.isSelected()
                    || loadAnyStagingCheckBox.isSelected()) {
                blockCarsCheckBox.setSelected(false);
            }
        }

        addCheckBoxAction(swapLoadsCheckBox);
        addCheckBoxAction(emptyCheckBox);
        addCheckBoxAction(loadCheckBox);
        addCheckBoxAction(loadAnyCheckBox);
        addCheckBoxAction(loadAnyStagingCheckBox);

        // finish
        panelOrder.setVisible(false); // Car order out of staging isn't necessary
        pack();
        setVisible(true);
    }

    @Override
    protected void saveTrack(Track track) {
        track.setLoadSwapEnabled(swapLoadsCheckBox.isSelected());
        track.setLoadEmptyEnabled(emptyCheckBox.isSelected());
        track.setRemoveCustomLoadsEnabled(emptyCustomCheckBox.isSelected());
        track.setAddCustomLoadsEnabled(loadCheckBox.isSelected());
        track.setAddCustomLoadsAnySpurEnabled(loadAnyCheckBox.isSelected());
        track.setAddCustomLoadsAnyStagingTrackEnabled(loadAnyStagingCheckBox.isSelected());
        track.setBlockCarsEnabled(blockCarsCheckBox.isSelected());
        super.saveTrack(track);
    }

    @Override
    protected void enableButtons(boolean enabled) {
        swapLoadsCheckBox.setEnabled(enabled);
        emptyCheckBox.setEnabled(enabled);
        emptyCustomCheckBox.setEnabled(enabled);
        loadCheckBox.setEnabled(enabled);
        loadAnyCheckBox.setEnabled(enabled);
        loadAnyStagingCheckBox.setEnabled(enabled);
        blockCarsCheckBox.setEnabled(_track != null && !_track.isAddCustomLoadsEnabled() && !_track.isAddCustomLoadsAnySpurEnabled()
                && !_track.isAddCustomLoadsAnyStagingTrackEnabled() && enabled);
        // show ship loads
        pShipLoadOption.setVisible(_track == null || _track.isAddCustomLoadsEnabled()
                || _track.isAddCustomLoadsAnySpurEnabled() || _track.isAddCustomLoadsAnyStagingTrackEnabled()
                || !_track.getShipLoadOption().equals(Track.ALL_LOADS));
        super.enableButtons(enabled);
    }
    
    @Override
    protected void updateTrainComboBox() {
        super.updateTrainComboBox();
        // only show trains that depart from this staging location
        if (autoPickupCheckBox.isSelected()) {
            for (int i = 1; i < comboBoxPickupTrains.getItemCount(); i++) {
                Train train = comboBoxPickupTrains.getItemAt(i);
                if (!train.getTrainDepartsName().equals(_location.getName())) {
                    comboBoxPickupTrains.removeItemAt(i--);
                }
            }
        }
        // only show trains that terminate into this staging location
        if (autoDropCheckBox.isSelected()) {
            for (int i = 1; i < comboBoxDropTrains.getItemCount(); i++) {
                Train train = comboBoxDropTrains.getItemAt(i);
                if (!train.getTrainTerminatesName().equals(_location.getName())) {
                    comboBoxDropTrains.removeItemAt(i--);
                }
            }
        }
    }
    
    @Override
    protected void updateRouteComboBox() {
        super.updateRouteComboBox();
        // only show routes that depart from this staging location
        if (autoPickupCheckBox.isSelected()) {
            for (int i = 1; i < comboBoxPickupRoutes.getItemCount(); i++) {
                Route route = comboBoxPickupRoutes.getItemAt(i);
                if (route.getLocationsBySequenceList().get(0).getLocation() != _location) {
                    comboBoxPickupRoutes.removeItemAt(i--);
                }
            }
        }
        // only show routes that terminate into this staging location
        if (autoDropCheckBox.isSelected()) {
            for (int i = 1; i < comboBoxDropRoutes.getItemCount(); i++) {
                Route route = comboBoxDropRoutes.getItemAt(i);
                if (route.getLocationsBySequenceList().get(route.getLocationsBySequenceList().size()-1).getLocation() != _location) {
                    comboBoxDropRoutes.removeItemAt(i--);
                }
            }
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == swapLoadsCheckBox) {
            if (swapLoadsCheckBox.isSelected()) {
                emptyCheckBox.setSelected(false);
            }
        } else if (ae.getSource() == emptyCheckBox) {
            if (emptyCheckBox.isSelected()) {
                swapLoadsCheckBox.setSelected(false);
            }
        } else if (ae.getSource() == loadCheckBox) {
            if (loadCheckBox.isSelected()) {
                loadAnyCheckBox.setSelected(false);
                blockCarsCheckBox.setSelected(false);
                blockCarsCheckBox.setEnabled(false);
            } else if (!loadAnyCheckBox.isSelected() && !loadAnyStagingCheckBox.isSelected()) {
                blockCarsCheckBox.setEnabled(true);
            }
        } else if (ae.getSource() == loadAnyCheckBox) {
            if (loadAnyCheckBox.isSelected()) {
                loadCheckBox.setSelected(false);
                blockCarsCheckBox.setSelected(false);
                blockCarsCheckBox.setEnabled(false);
            } else if (!loadCheckBox.isSelected() && !loadAnyStagingCheckBox.isSelected()) {
                blockCarsCheckBox.setEnabled(true);
            }
        } else if (ae.getSource() == loadAnyStagingCheckBox) {
            if (loadAnyStagingCheckBox.isSelected()) {
                blockCarsCheckBox.setSelected(false);
                blockCarsCheckBox.setEnabled(false);
            } else if (!loadCheckBox.isSelected() && !loadAnyCheckBox.isSelected()) {
                blockCarsCheckBox.setEnabled(true);
            }
        } else {
            super.checkBoxActionPerformed(ae);
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(StagingEditFrame.class);
}
