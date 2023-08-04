package jmri.jmrit.operations.routes.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame to show and edit which routes can service a location
 *
 * @author Dan Boudreau Copyright (C) 2023
 */
public class ShowRoutesServingLocationFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Location _location = null;

    // panels
    JPanel pRoutes = new JPanel();

    // combo boxes
    JComboBox<Location> locationComboBox = new JComboBox<>();

    public ShowRoutesServingLocationFrame() {
        super(Bundle.getMessage("TitleShowRoutes"));
    }

    public void initComponents(Location location) {

        _location = location;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pLocations = new JPanel();
        pLocations.setLayout(new GridBagLayout());
        pLocations.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocations.setMaximumSize(new Dimension(2000, 50));

        addItem(pLocations, locationComboBox, 0, 0);
        
        pRoutes.setLayout(new GridBagLayout());
        JScrollPane routesPane = new JScrollPane(pRoutes);
        routesPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        routesPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleRoutesTable")));

        getContentPane().add(pLocations);
        getContentPane().add(routesPane);

        // setup combo box
        addComboBoxAction(locationComboBox);
        updateLocationsComboBox();

        if (location != null) {
            location.addPropertyChangeListener(this);
        }
 
        addPropertyChangeAllRoutes();

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowRoutesServicingThisLocation", true); // NOI18N

        setPreferredSize(null);
        initMinimumSize();
    }

    private void updateRoutePane() {
        log.debug("Updating for location ({})", _location);
        pRoutes.removeAll();
        int y = 0;
        for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByNameList()) {
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (_location != null && rl.getName().equals(_location.getName())) {
                    JButton button = new JButton(route.getName());
                    addButtonAction(button);
                    addItemLeft(pRoutes, button, 1, y++);
                    break;
                }
            }
        }
        pRoutes.repaint();
        pRoutes.revalidate();
        pack();
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource().equals(locationComboBox)) {
            _location = (Location) locationComboBox.getSelectedItem();
            updateRoutePane();
        }
    }
    
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        JButton button = (JButton) ae.getSource();
        log.debug("Edit route button ({})", button.getText());
        Route route = InstanceManager.getDefault(RouteManager.class).getRouteByName(button.getText());
        RouteEditFrame frame = new RouteEditFrame();
        frame.initComponents(route);
    }

    private void updateLocationsComboBox() {
        InstanceManager.getDefault(LocationManager.class).updateComboBox(locationComboBox);
        locationComboBox.setSelectedItem(_location);
    }

    @Override
    public void dispose() {
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        removePropertyChangeAllRoutes();
        super.dispose();
    }

    public void addPropertyChangeAllRoutes() {
        InstanceManager.getDefault(RouteManager.class).addPropertyChangeListener(this);
        for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByNameList()) {
            route.addPropertyChangeListener(this);
        }
    }

    public void removePropertyChangeAllRoutes() {
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
        for (Route route : InstanceManager.getDefault(RouteManager.class).getRoutesByNameList()) {
            route.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
            removePropertyChangeAllRoutes();
            addPropertyChangeAllRoutes();
        }
        if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Route.ROUTE_NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)) {
            updateRoutePane();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ShowRoutesServingLocationFrame.class);
}
