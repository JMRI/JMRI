package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame to show which trains can service this location
 *
 * @author Dan Boudreau Copyright (C) 2014
 * 
 */
public class ShowTrainsServingLocationFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // location
    Location _location = null;
    Track _track = null;

    // panels
    JPanel pTrains = new JPanel();

    // radio buttons
    // for padding out panel
    // combo boxes
    JComboBox<String> typeComboBox = new JComboBox<>();

    // check boxes
    JCheckBox showAllTrainsCheckBox = new JCheckBox(Bundle.getMessage("ShowAllTrains"));

    // make show all trains consistent during a session
    private static boolean isShowAllTrains = true;

    public ShowTrainsServingLocationFrame() {
        super();
    }

    public void initComponents(Location location, Track track) {

        _location = location;
        _track = track;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pOptions = new JPanel();
        pOptions.setLayout(new GridBagLayout());
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));

        addItem(pOptions, showAllTrainsCheckBox, 0, 0);

        JPanel pCarType = new JPanel();
        pCarType.setLayout(new GridBagLayout());
        pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        pCarType.setMaximumSize(new Dimension(2000, 50));

        addItem(pCarType, typeComboBox, 0, 0);

        pTrains.setLayout(new GridBagLayout());
        JScrollPane trainsPane = new JScrollPane(pTrains);
        trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Trains")));

        getContentPane().add(pOptions);
        getContentPane().add(pCarType);
        getContentPane().add(trainsPane);

        // show all trains
        showAllTrainsCheckBox.setToolTipText(Bundle.getMessage("TipDeselectedShowAllTrains"));
        addCheckBoxAction(showAllTrainsCheckBox);
        showAllTrainsCheckBox.setSelected(isShowAllTrains);

        // setup combo box
        updateComboBox();
        typeComboBox.setSelectedItem(NONE);
        addComboBoxAction(typeComboBox);

        // increase width of combobox so large text names display properly
        Dimension boxsize = typeComboBox.getMinimumSize();
        if (boxsize != null) {
            boxsize.setSize(boxsize.width + 10, boxsize.height);
            typeComboBox.setMinimumSize(boxsize);
        }

        updateTrainPane();

        location.addPropertyChangeListener(this);
        addPropertyChangeAllTrains();

        if (_track != null) {
            _track.addPropertyChangeListener(this);
            setTitle(MessageFormat.format(Bundle.getMessage("TitleShowTrains"), new Object[]{_track.getName()}));
        } else {
            setTitle(MessageFormat.format(Bundle.getMessage("TitleShowTrains"), new Object[]{_location.getName()}));
        }
        
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowTrainsServicingThisLocation", true); // NOI18N

        setPreferredSize(null);
        initMinimumSize();
    }

    private void updateTrainPane() {
        pTrains.removeAll();
        int y = 0;
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            Route route = train.getRoute();
            if (route == null) {
                continue;
            }
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (rl.getName().equals(_location.getName())) {
                    boolean pickup = false;
                    boolean setout = false;
                    // monitor move count in the route for this location
                    train.getRoute().removePropertyChangeListener(this);
                    train.getRoute().addPropertyChangeListener(this);
                    if (rl.isPickUpAllowed()
                            && rl.getMaxCarMoves() > 0
                            && !train.skipsLocation(rl.getId())
                            && (typeComboBox.getSelectedItem() == null || typeComboBox.getSelectedItem().equals(NONE) || train
                            .acceptsTypeName((String) typeComboBox.getSelectedItem()))
                            && (train.isLocalSwitcher() || (rl.getTrainDirection() & _location.getTrainDirections()) != 0)
                            && (train.isLocalSwitcher() || _track == null || ((rl.getTrainDirection() & _track
                            .getTrainDirections()) != 0))
                            && (_track == null || _track.acceptsPickupTrain(train))) {
                        pickup = true;
                    }
                    if (rl.isDropAllowed()
                            && rl.getMaxCarMoves() > 0
                            && !train.skipsLocation(rl.getId())
                            && (typeComboBox.getSelectedItem() == null || typeComboBox.getSelectedItem().equals(NONE) || train
                            .acceptsTypeName((String) typeComboBox.getSelectedItem()))
                            && (train.isLocalSwitcher() || (rl.getTrainDirection() & _location.getTrainDirections()) != 0)
                            && (train.isLocalSwitcher() || _track == null || ((rl.getTrainDirection() & _track
                            .getTrainDirections()) != 0)) 
                            && (_track == null || _track.acceptsDropTrain(train))) {
                        setout = true;
                    }
                    // now display results
                    if (showAllTrainsCheckBox.isSelected() || pickup || setout) {
                        addItemLeft(pTrains, new JLabel(train.getName()), 0, y);
                        // train direction when servicing this location
                        addItem(pTrains, new JLabel(rl.getTrainDirectionString()), 1, y);
                        if (pickup) {
                            addItem(pTrains, new JLabel(Bundle.getMessage("OkayPickUp")), 2, y);
                        } else {
                            addItem(pTrains, new JLabel(Bundle.getMessage("NoPickUp")), 2, y);
                        }
                        if (setout) {
                            addItem(pTrains, new JLabel(Bundle.getMessage("OkaySetOut")), 3, y);
                        } else {
                            addItem(pTrains, new JLabel(Bundle.getMessage("NoSetOut")), 3, y);
                        }
                    }
                    y++;
                }
            }
        }
        pTrains.repaint();
        pTrains.revalidate();
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("check box action");
        isShowAllTrains = showAllTrainsCheckBox.isSelected();
        updateTrainPane();
    }

    private String comboBoxSelect;

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("combo box action");
        if (typeComboBox.isEnabled() && ae.getSource().equals(typeComboBox)) {
            updateTrainPane();
            if (typeComboBox.getSelectedItem() != null) {
                comboBoxSelect = (String) typeComboBox.getSelectedItem();
            }
        }
    }

    private void updateComboBox() {
        log.debug("update combobox");
        typeComboBox.setEnabled(false);
        InstanceManager.getDefault(CarTypes.class).updateComboBox(typeComboBox);
        // remove car types not serviced by this location and track
        for (int i = typeComboBox.getItemCount() - 1; i >= 0; i--) {
            String type = typeComboBox.getItemAt(i);
            if (_location != null && !_location.acceptsTypeName(type)) {
                typeComboBox.removeItem(type);
            }
            if (_track != null && !_track.acceptsTypeName(type)) {
                typeComboBox.removeItem(type);
            }
        }
        typeComboBox.insertItemAt(NONE, 0);

        if (comboBoxSelect == null) {
            typeComboBox.setSelectedItem(NONE);
        } else {
            typeComboBox.setSelectedItem(comboBoxSelect);
            if (typeComboBox.getSelectedItem() != null && !typeComboBox.getSelectedItem().equals(comboBoxSelect)) {
                typeComboBox.setSelectedItem(NONE); // selected object has been removed
            }
            updateTrainPane();
        }
        typeComboBox.setEnabled(true);
    }

    @Override
    public void dispose() {
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        removePropertyChangeAllTrains();
        super.dispose();
    }

    public void addPropertyChangeAllTrains() {
        InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(this);
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            train.addPropertyChangeListener(this);
        }
    }

    public void removePropertyChangeAllTrains() {
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            train.removePropertyChangeListener(this);
            if (train.getRoute() != null) {
                train.getRoute().removePropertyChangeListener(this);
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateComboBox();
        }
        if (e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.TRAINDIRECTION_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.DROP_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Track.PICKUP_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.TRAIN_ROUTE_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.STOPS_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)) {
            updateTrainPane();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            removePropertyChangeAllTrains();
            addPropertyChangeAllTrains();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationFrame.class);
}
