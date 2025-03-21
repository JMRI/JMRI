package jmri.jmrit.beantable.routetable;

import java.util.List;

import jmri.InstanceManager;

/**
 * Set up table for selecting Sensors and Sensor State.
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
class RouteSensorModel extends RouteOutputModel {

    private final AbstractRouteAddEditFrame routeAddFrame;

    RouteSensorModel(AbstractRouteAddEditFrame routeAddFrame) {
        this.routeAddFrame = routeAddFrame;
        InstanceManager.sensorManagerInstance().addPropertyChangeListener(RouteSensorModel.this);
    }

    @Override
    public int getRowCount() {
        if (routeAddFrame.isShowAll()) {
            return routeAddFrame.get_sensorList().size();
        } else {
            return routeAddFrame.get_includedSensorList().size();
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        List<RouteSensor> sensorList;
        if (routeAddFrame.isShowAll()) {
            sensorList = routeAddFrame.get_sensorList();
        } else {
            sensorList = routeAddFrame.get_includedSensorList();
        }
        // some error checking
        if (r >= sensorList.size()) {
            log.debug("row is greater than turnout list size");
            return null;
        }
        switch (c) {
            case INCLUDE_COLUMN:
                return sensorList.get(r).isIncluded();
            case SNAME_COLUMN:  // slot number
                return sensorList.get(r).getSysName();
            case UNAME_COLUMN:  //
                return sensorList.get(r).getUserName();
            case STATE_COLUMN:  //
                return sensorList.get(r).getSetToState();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object type, int r, int c) {
        List<RouteSensor> sensorList;
        if (routeAddFrame.isShowAll()) {
            sensorList = routeAddFrame.get_sensorList();
        } else {
            sensorList = routeAddFrame.get_includedSensorList();
        }
        switch (c) {
            case INCLUDE_COLUMN:
                sensorList.get(r).setIncluded(((Boolean) type));
                break;
            case STATE_COLUMN:
                sensorList.get(r).setSetToState((String) type);
                break;
            default:
                log.error("RouteSensorModel.setValueAt should not be called on column {}", c);
        }
    }

    @Override
    protected void dispose() {
        InstanceManager.sensorManagerInstance().removePropertyChangeListener(this);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RouteSensorModel.class);

}
