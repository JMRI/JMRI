package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;
import jmri.Route;
import jmri.Sensor;
import jmri.Turnout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Edit frame for the Route Table.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
public class RouteEditFrame extends AbstractRouteAddEditFrame {

    private final String systemName;

    public RouteEditFrame(String systemName) {
        this(Bundle.getMessage("TitleEditRoute"), systemName);
    }

    public RouteEditFrame(String name, String systemName) {
        this(name,false,true, systemName);
    }

    public RouteEditFrame(String name, boolean saveSize, boolean savePosition, String systemName) {
        super(name, saveSize, savePosition);
        this.systemName = systemName;
        initComponents();
    }

    @Override
    public final void initComponents() {
        super.initComponents();
        _systemName.setText(systemName);
        // identify the Route with this name if it already exists
        String sName = _systemName.getText();
        Route g = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(sName);
        if (g == null) {
            sName = _userName.getText();
            g = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(sName);
            if (g == null) {
                // Route does not exist, so cannot be edited
                status1.setText(Bundle.getMessage("RouteAddStatusErrorNotFound"));
                return;
            }
        }
        // Route was found, make its system name not changeable
        curRoute = g;
        _systemName.setVisible(true);
        _systemName.setText(sName);
        _systemName.setEnabled(false);
        nameLabel.setEnabled(true);
        _autoSystemName.setVisible(false);
        // deactivate this Route
        curRoute.deActivateRoute();
        // get information for this route
        _userName.setText(g.getUserName());
        // set up Turnout list for this route
        int setRow = 0;
        for (int i = _turnoutList.size() - 1; i >= 0; i--) {
            RouteTurnout turnout = _turnoutList.get(i);
            String tSysName = turnout.getSysName();
            if (g.isOutputTurnoutIncluded(tSysName)) {
                turnout.setIncluded(true);
                turnout.setState(g.getOutputTurnoutSetState(tSysName));
                setRow = i;
            } else {
                turnout.setIncluded(false);
                turnout.setState(Turnout.CLOSED);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeTurnoutScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _routeTurnoutModel.fireTableDataChanged();
        // set up Sensor list for this route
        for (int i = _sensorList.size() - 1; i >= 0; i--) {
            RouteSensor sensor = _sensorList.get(i);
            String tSysName = sensor.getSysName();
            if (g.isOutputSensorIncluded(tSysName)) {
                sensor.setIncluded(true);
                sensor.setState(g.getOutputSensorSetState(tSysName));
                setRow = i;
            } else {
                sensor.setIncluded(false);
                sensor.setState(Sensor.INACTIVE);
            }
        }
        setRow -= 1;
        if (setRow < 0) {
            setRow = 0;
        }
        _routeSensorScrollPane.getVerticalScrollBar().setValue(setRow * ROW_HEIGHT);
        _routeSensorModel.fireTableDataChanged();
        // get Sound and  Script file names
        scriptFile.setText(g.getOutputScriptName());
        soundFile.setText(g.getOutputSoundName());

        // get Turnout Aligned sensor
        turnoutsAlignedSensor.setSelectedItem(g.getTurnoutsAlgdSensor());

        // set up Sensors if there are any
        Sensor[] temNames = new Sensor[Route.MAX_CONTROL_SENSORS];
        int[] temModes = new int[Route.MAX_CONTROL_SENSORS];
        for (int k = 0; k < Route.MAX_CONTROL_SENSORS; k++) {
            temNames[k] = g.getRouteSensor(k);
            temModes[k] = g.getRouteSensorMode(k);
        }
        sensor1.setSelectedItem(temNames[0]);
        setSensorModeBox(temModes[0], sensor1mode);

        sensor2.setSelectedItem(temNames[1]);
        setSensorModeBox(temModes[1], sensor2mode);

        sensor3.setSelectedItem(temNames[2]);
        setSensorModeBox(temModes[2], sensor3mode);

        // set up Control Turnout if there is one
        cTurnout.setSelectedItem(g.getCtlTurnout());

        setTurnoutModeBox(g.getControlTurnoutState(), cTurnoutStateBox);

        // set up Lock Control Turnout if there is one
        cLockTurnout.setSelectedItem(g.getLockCtlTurnout());

        setTurnoutModeBox(g.getLockControlTurnoutState(), cLockTurnoutStateBox);

        // set up additional route specific Delay
        timeDelay.setValue(g.getRouteCommandDelay());
        // begin with showing all Turnouts
        // set up buttons and notes
        status1.setText(Bundle.getMessage("RouteAddStatusInitial3", Bundle.getMessage("ButtonUpdate")));
        status2.setText(Bundle.getMessage("RouteAddStatusInitial4", Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit"))));
        status2.setVisible(true);
        setTitle(Bundle.getMessage("TitleEditRoute"));
        editMode = true;
    }

    @Override
    protected JPanel getButtonPanel() {
        final JButton cancelEditButton = new JButton(Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit"))); // I18N for word sequence "Cancel Edit"
        final JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete") + " " + Bundle.getMessage("BeanNameRoute")); // I18N "Delete Route"
        final JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        final JButton exportButton = new JButton(Bundle.getMessage("ButtonExport"));
        // add Buttons panel
        JPanel pb = new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // CancelEdit button
        pb.add(cancelEditButton);
        cancelEditButton.addActionListener(this::cancelPressed);
        cancelEditButton.setToolTipText(Bundle.getMessage("TooltipCancelRoute"));
        // Delete Route button
        pb.add(deleteButton);
        deleteButton.addActionListener(this::deletePressed);
        deleteButton.setToolTipText(Bundle.getMessage("TooltipDeleteRoute"));
        // Update Route button
        pb.add(updateButton);
        updateButton.addActionListener((ActionEvent e1) -> updatePressed(false));
        updateButton.setToolTipText(Bundle.getMessage("TooltipUpdateRoute"));
        // Export button
        pb.add(exportButton);
        exportButton.addActionListener(this::exportButtonPressed);
        exportButton.setToolTipText(Bundle.getMessage("TooltipExportRoute"));

        // Show the initial buttons, and hide the others
        deleteButton.setVisible(true);
        cancelEditButton.setVisible(true);
        updateButton.setVisible(true);
        exportButton.setVisible(true);
        return pb;
    }

    /**
     * Respond to the export button.
     *
     * @param e the action event
     */
    private void exportButtonPressed(ActionEvent e){
        new RouteExportToLogix(_systemName.getText()).export();
        status1.setText(Bundle.getMessage("BeanNameRoute")
                + "\"" + _systemName.getText() + "\" " +
                Bundle.getMessage("RouteAddStatusExported") + " ("
                + get_includedTurnoutList().size() +
                Bundle.getMessage("Turnouts") + ", " +
                get_includedSensorList().size() + " " + Bundle.getMessage("Sensors") + ")");
        finishUpdate();
        closeFrame();
    }

    /**
     * Respond to the CancelEdit button.
     *
     * @param e the action event
     */
    private void cancelPressed(ActionEvent e) {
        cancelEdit();
    }

    /**
     * Respond to the Delete button.
     *
     * @param e the action event
     */
    private void deletePressed(ActionEvent e) {
        // route is already deactivated, just delete it
        routeManager.deleteRoute(curRoute);

        curRoute = null;
        finishUpdate();
        closeFrame();
    }

}
