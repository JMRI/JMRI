package jmri.jmrit.ussctc;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Route;
import jmri.RouteManager;
import jmri.Turnout;
import jmri.implementation.DefaultRoute;

/**
 * Provide bean-like access to the collection of Logix, Routes, Memories, etc
 * that make up a Follower.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class Follower implements Constants {

    final static String namePrefix = commonNamePrefix + "FOLLOWER" + commonNameSuffix; // NOI18N

    /**
     * Nobody can build anonymous object
     */
    //private Follower() {}
    /**
     * Create one from scratch
     *
     * @param output Output turnout to be driven
     * @param sensor Sensor checking for OS occupancy
     * @param veto   veto Sensor, or ""
     */
    public Follower(String output, String sensor, boolean invert, String veto) {
        this.veto = veto;
        this.sensor = sensor;
        this.invert = invert;
        this.output = output;
    }

    /**
     * Create the underlying objects that implement this
     */
    public void instantiate() {
        String nameT = namePrefix + "T" + nameDivider + output;
        String nameC = namePrefix + "C" + nameDivider + output;

        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);

        Route rt = rm.getBySystemName(nameT);
        // if an old one exists, remove it
        if (rt != null) {
            rt.deActivateRoute();
            rm.deleteRoute(rt);
        }
        Route rc = rm.getBySystemName(nameC);
        // if an old one exists, remove it
        if (rc != null) {
            rc.deActivateRoute();
            rm.deleteRoute(rc);
        }

        // create a new one
        rt = new DefaultRoute(nameT);
        rc = new DefaultRoute(nameC);

        // add trigger Sensor
        rt.addSensorToRoute(sensor, invert ? Route.ONINACTIVE : Route.ONACTIVE);
        rc.addSensorToRoute(sensor, !invert ? Route.ONINACTIVE : Route.ONACTIVE);

        // optionally, add veto
        if (!veto.isEmpty()) {
            rt.addSensorToRoute(veto, Route.VETOACTIVE);
            rc.addSensorToRoute(veto, Route.VETOACTIVE);
        }

        // add output
        rt.addOutputTurnout(output, Turnout.THROWN);
        rc.addOutputTurnout(output, Turnout.CLOSED);

        // and put Route into operation
        rt.activateRoute();
        rc.activateRoute();
        rm.register(rt);
        rm.register(rc);

    }

    /**
     * Create an object to represent an existing Follower.
     *
     * @param outputName name of output Turnout that drives the indicator
     * @throws JmriException if no such Follower exists, or some problem found
     */
    public Follower(String outputName) throws jmri.JmriException {
        this.output = outputName;

        // find existing thrown route to get info
        String nameT = namePrefix + "T" + nameDivider + output;        

        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        Route r = rm.getBySystemName(nameT);
        if (r == null) {
            throw new jmri.JmriException("Route does not exist");   // NOI18N
        }

        // and load internals from the route
        sensor = r.getRouteSensorName(0);
        invert = (r.getRouteSensorMode(0) == Route.ONINACTIVE);
        veto = r.getRouteSensorName(1);
        if (veto == null) {
            veto = "";
        }

    }

    public String getOutputName() {
        return output;
    }

    public String getSensorName() {
        return sensor;
    }

    public boolean getInvert() {
        return invert;
    }

    public String getVetoName() {
        return veto;
    }

    String output;
    String sensor;
    String veto;
    boolean invert;

}
