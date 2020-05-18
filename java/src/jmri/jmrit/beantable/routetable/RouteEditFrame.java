package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;
import jmri.Route;
import jmri.Sensor;
import jmri.Turnout;

public class RouteEditFrame extends RouteAddFrame {

    private final String systemName;

    public RouteEditFrame(String systemName) {
        this(Bundle.getMessage("TitleEditRoute"),systemName);
    }

    public RouteEditFrame(String name, String systemName) {
        this(name,false,true,systemName);
    }

    public RouteEditFrame(String name, boolean saveSize, boolean savePosition, String systemName) {
        super(name, saveSize, savePosition);
        this.systemName = systemName;
    }

    @Override
    public void initComponents() {
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
        _autoSystemName.setVisible(false);
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
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
        status1.setText(updateInst);
        status2.setText(cancelInst);
        status2.setVisible(true);
        deleteButton.setVisible(true);
        cancelButton.setVisible(false);
        cancelEditButton.setVisible(true);
        updateButton.setVisible(true);
        exportButton.setVisible(true);
        editButton.setVisible(false);
        createButton.setVisible(false);
        fixedSystemName.setVisible(true);
        _systemName.setVisible(false);
        setTitle(Bundle.getMessage("TitleEditRoute"));
        editMode = true;
    }

}
