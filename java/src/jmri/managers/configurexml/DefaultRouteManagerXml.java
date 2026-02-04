package jmri.managers.configurexml;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.SortedSet;

import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.managers.DefaultRouteManager;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring RouteManagers.
 *
 * @author Dave Duchamp Copyright (c) 2004
 * @author Daniel Boudreau Copyright (c) 2007
 * @author Simon Reader Copyright (C) 2008
 */
public class DefaultRouteManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    private static final String STR_ROUTE = "route";
    private static final String STR_ROUTES = "routes";

    private static final String STR_CONTROL_TURNOUT = "controlTurnout";
    private static final String STR_CONTROL_TURNOUT_FEEDBACK = "controlTurnoutFeedback";
    private static final String STR_CONTROL_TURNOUT_STATE = "controlTurnoutState";
    private static final String STR_CONTROL_LOCK_TURNOUT = "controlLockTurnout";
    private static final String STR_CONTROL_LOCK_TURNOUT_STATE = "controlLockTurnoutState";

    private static final String STR_ACTIVE = "ACTIVE";
    private static final String STR_INACTIVE = "INACTIVE";
    private static final String STR_ON_ACTIVE = "onActive";
    private static final String STR_ON_INACTIVE = "onInactive";
    private static final String STR_ON_CHANGE = "onChange";
    private static final String STR_VETO_ACTIVE = "vetoActive";
    private static final String STR_VETO_INACTIVE = "vetoInactive";

    private static final String STR_THROWN = "THROWN";
    private static final String STR_CLOSED = "CLOSED";
    private static final String STR_CHANGE = "CHANGE";
    private static final String STR_TOGGLE = "TOGGLE";
    private static final String STR_VETO_CLOSED = "VETOCLOSED";
    private static final String STR_VETO_THROWN = "VETOTHROWN";
    private static final String STR_ADDED_DELAY = "addedDelay";

    private static final String STR_ROUTE_LOCKED = "routeLocked";
    private static final String STR_ROUTE_SENSOR = "routeSensor";
    private static final String STR_ROUTE_OUTPUT_SENSOR = "routeOutputSensor";
    private static final String STR_ROUTE_OUTPUT_TURNOUT = "routeOutputTurnout";
    private static final String STR_STATE = "state";
    private static final String STR_MODE = "mode";
    private static final String STR_NAME = "name";

    private static final String STR_ROUTE_SOUND_FILE = "routeSoundFile";
    private static final String STR_ROUTE_SCRIPT_FILE = "routeScriptFile";
    private static final String STR_TURNOUTS_ALIGNED_SENSOR = "turnoutsAlignedSensor";

    public DefaultRouteManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a RouteManager.
     *
     * @param o Object to store, of type RouteManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element routes = new Element(STR_ROUTES);
        setStoreElementClass(routes);
        RouteManager rm = (RouteManager) o;
        if (rm != null) {
            SortedSet<Route> routeList = rm.getNamedBeanSet();
            // don't return an element if there are no routes to include
            if (routeList.isEmpty()) {
                return null;
            }
            for (Route r : routeList) {
                // store the routes
                String rName = r.getSystemName();
                log.debug("system name is {}", rName);

                String cTurnout = r.getControlTurnout();
                int addedDelay = r.getRouteCommandDelay();
                String cLockTurnout = r.getLockControlTurnout();

                Element elem = new Element(STR_ROUTE);
                elem.addContent(new Element(STR_SYSTEM_NAME).addContent(rName));

                // As a work-around for backward compatibility, store systemName and userName as attribute.
                // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                String uName = r.getUserName();
                if (uName != null && !uName.isEmpty()) {
                    elem.setAttribute(STR_USER_NAME, uName);
                }

                // store common parts
                storeCommon(r, elem);

                if (cTurnout != null && !cTurnout.isEmpty()) {
                    elem.setAttribute(STR_CONTROL_TURNOUT, cTurnout);
                    int state = r.getControlTurnoutState();
                    switch (state) {
                        case Route.ONTHROWN:
                            elem.setAttribute(STR_CONTROL_TURNOUT_STATE, STR_THROWN);
                            break;
                        case Route.ONCHANGE:
                            elem.setAttribute(STR_CONTROL_TURNOUT_STATE, STR_CHANGE);
                            break;
                        case Route.VETOCLOSED:
                            elem.setAttribute(STR_CONTROL_TURNOUT_STATE, STR_VETO_CLOSED);
                            break;
                        case Route.VETOTHROWN:
                            elem.setAttribute(STR_CONTROL_TURNOUT_STATE, STR_VETO_THROWN);
                            break;
                        default:
                            elem.setAttribute(STR_CONTROL_TURNOUT_STATE, STR_CLOSED);
                            break;
                    }
                    
                    if (r.getControlTurnoutFeedback()) {
                        elem.setAttribute(STR_CONTROL_TURNOUT_FEEDBACK, STR_TRUE);
                    } // don't write if not set, accept default
                }
                if (cLockTurnout != null && !cLockTurnout.isEmpty()) {
                    elem.setAttribute(STR_CONTROL_LOCK_TURNOUT, cLockTurnout);
                    int state = r.getLockControlTurnoutState();
                    switch (state) {
                        case Route.ONTHROWN:
                            elem.setAttribute(STR_CONTROL_LOCK_TURNOUT_STATE, STR_THROWN);
                            break;
                        case Route.ONCHANGE:
                            elem.setAttribute(STR_CONTROL_LOCK_TURNOUT_STATE, STR_CHANGE);
                            break;
                        default:
                            elem.setAttribute(STR_CONTROL_LOCK_TURNOUT_STATE, STR_CLOSED);
                            break;
                    }
                }
                if (addedDelay > 0) {
                    elem.setAttribute(STR_ADDED_DELAY, Integer.toString(addedDelay));
                }

                if ( r.getLocked() ) {
                    // TODO - For consistency in class, convert True to true
                    elem.setAttribute(STR_ROUTE_LOCKED, "True");
                }
                // add route output Turnouts, if any
                int index = 0;
                String rTurnout;
                while ((rTurnout = r.getOutputTurnoutByIndex(index)) != null) {
                    Element rElem = new Element(STR_ROUTE_OUTPUT_TURNOUT)
                            .setAttribute(STR_SYSTEM_NAME, rTurnout);
                    String sState = STR_CLOSED;
                    if (r.getOutputTurnoutSetState(rTurnout) == Turnout.THROWN) {
                        sState = STR_THROWN;
                    } else if (r.getOutputTurnoutSetState(rTurnout) == Route.TOGGLE) {
                        sState = STR_TOGGLE;
                    }
                    rElem.setAttribute(STR_STATE, sState);
                    elem.addContent(rElem);
                    index++;
                }
                // add route output Sensors, if any
                index = 0;
                String rSensor;
                while ((rSensor = r.getOutputSensorByIndex(index)) != null) {
                    Element rElem = new Element(STR_ROUTE_OUTPUT_SENSOR)
                            .setAttribute(STR_SYSTEM_NAME, rSensor);
                    String sState = STR_INACTIVE;
                    if (r.getOutputSensorSetState(rSensor) == Sensor.ACTIVE) {
                        sState = STR_ACTIVE;
                    } else if (r.getOutputSensorSetState(rSensor) == Route.TOGGLE) {
                        sState = STR_TOGGLE;
                    }
                    rElem.setAttribute(STR_STATE, sState);
                    elem.addContent(rElem);
                    index++;
                }
                // add route control Sensors, if any
                index = 0;
                while ((rSensor = r.getRouteSensorName(index)) != null) {
                    Element rsElem = new Element(STR_ROUTE_SENSOR)
                            .setAttribute(STR_SYSTEM_NAME, rSensor);
                    int mode = r.getRouteSensorMode(index);
                    String modeName;
                    switch (mode) {
                        case Route.ONACTIVE:
                            modeName = STR_ON_ACTIVE;
                            break;
                        case Route.ONINACTIVE:
                            modeName = STR_ON_INACTIVE;
                            break;
                        case Route.ONCHANGE:
                            modeName = STR_ON_CHANGE;
                            break;
                        case Route.VETOACTIVE:
                            modeName = STR_VETO_ACTIVE;
                            break;
                        case Route.VETOINACTIVE:
                            modeName = STR_VETO_INACTIVE;
                            break;
                        default:
                            modeName = null;
                    }
                    if (modeName != null) {
                        rsElem.setAttribute(STR_MODE, modeName);
                    }
                    elem.addContent(rsElem);
                    index++;
                }
                // add sound and script file elements if needed
                String osn = r.getOutputSoundName();
                if ( osn != null && !osn.isEmpty()) {
                    Element rsElem = new Element(STR_ROUTE_SOUND_FILE)
                        .setAttribute(STR_NAME,
                            jmri.util.FileUtil.getPortableFilename(
                                new java.io.File(osn))
                        );
                    elem.addContent(rsElem);
                }
                osn = r.getOutputScriptName();
                if ( osn != null && !osn.isEmpty()) {
                    Element rsElem = new Element(STR_ROUTE_SCRIPT_FILE)
                        .setAttribute(STR_NAME,
                            jmri.util.FileUtil.getPortableFilename(
                                new java.io.File(osn))
                        );
                    elem.addContent(rsElem);
                }

                // add turnouts aligned sensor if there is one
                osn = r.getTurnoutsAlignedSensor();
                if ( osn != null && !osn.isEmpty()) {
                    Element rsElem = new Element(STR_TURNOUTS_ALIGNED_SENSOR)
                        .setAttribute(STR_NAME, osn);
                    elem.addContent(rsElem);
                }

                log.debug("store Route {}", rName);
                routes.addContent(elem);
            }
        }
        return routes;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param routes The top-level element being created
     */
    public void setStoreElementClass(Element routes) {
        routes.setAttribute(STR_CLASS, this.getClass().getName());
    }

    /**
     * Create a RouteManager object of the correct class, then register and fill
     * it.
     *
     * @param sharedRoutes Top level Element to unpack.
     * @param perNodeRoutes unused.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedRoutes, Element perNodeRoutes) {
        // create the master object
        replaceRouteManager();
        // load individual sharedRoutes
        loadRoutes(sharedRoutes);
        return true;
    }

    /**
     * Utility method to load the individual Route objects. If there's no
     * additional info needed for a specific route type, invoke this with the
     * parent of the set of Route elements.
     *
     * @param routes Element containing the Route elements to load.
     */
    public void loadRoutes(Element routes) {
        List<Element> routeList = routes.getChildren(STR_ROUTE);
        log.debug("Found {} routes", routeList.size());
        RouteManager tm = InstanceManager.getDefault(RouteManager.class);
        int namesChanged = 0;

        for (Element el : routeList) {

            String sysName = getSystemName(el);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", el);
                break;
            }
            // convert typeLetter from R to tm.typeLetter()
            if (sysName.startsWith(tm.getSystemPrefix() + 'R')) {
                String old = sysName;
                sysName = tm.getSystemNamePrefix() + sysName.substring(tm.getSystemNamePrefix().length());
                log.warn("Converting route system name {} to {}", old, sysName);
                namesChanged++;
            }
            // prepend systemNamePrefix if missing
            if (!sysName.startsWith(tm.getSystemNamePrefix())) {
                String old = sysName;
                sysName = tm.getSystemNamePrefix() + sysName;
                log.warn("Converting route system name {} to {}", old, sysName);
                namesChanged++;
            }

            String userName = getUserName(el);
            String cTurnout = null;
            String cTurnoutState = null;
            boolean cTurnoutFeedback = false;
            String routeLockedTxt = null;
            String cLockTurnout = null;
            String cLockTurnoutState = null;
            int addedDelay = 0;

            if (el.getAttribute(STR_CONTROL_TURNOUT) != null) {
                cTurnout = el.getAttribute(STR_CONTROL_TURNOUT).getValue();
            }
            if (el.getAttribute(STR_CONTROL_TURNOUT_STATE) != null) {
                cTurnoutState = el.getAttribute(STR_CONTROL_TURNOUT_STATE).getValue();
            }
            if (el.getAttribute(STR_CONTROL_TURNOUT_FEEDBACK) != null) {
                cTurnoutFeedback = el.getAttribute(STR_CONTROL_TURNOUT_FEEDBACK).getValue().equals(STR_TRUE);
            }
            if (el.getAttribute(STR_CONTROL_LOCK_TURNOUT) != null) {
                cLockTurnout = el.getAttribute(STR_CONTROL_LOCK_TURNOUT).getValue();
            }
            if (el.getAttribute(STR_CONTROL_LOCK_TURNOUT_STATE) != null) {
                cLockTurnoutState = el.getAttribute(STR_CONTROL_LOCK_TURNOUT_STATE).getValue();
            }
            if (el.getAttribute(STR_ADDED_DELAY) != null) {
                String addedDelayTxt = el.getAttribute(STR_ADDED_DELAY).getValue();
                if (addedDelayTxt != null) {
                    addedDelay = Integer.parseInt(addedDelayTxt);
                }
            }
            if (el.getAttribute(STR_ROUTE_LOCKED) != null) {
                routeLockedTxt = el.getAttribute(STR_ROUTE_LOCKED).getValue();
            }

            log.debug("create route: ({})({})", sysName, (userName == null ? "<null>" : userName));
            
            Route r;
            try {
                r = tm.provideRoute(sysName, userName);
            } catch (IllegalArgumentException ex) {
                log.error("failed to create Route: {}", sysName);
                return;
            }

            // load common parts
            loadCommon(r, el);

            // add control turnout if there is one
            if (cTurnout != null) {
                r.setControlTurnout(cTurnout);
                if (cTurnoutState != null) {
                    switch (cTurnoutState) {
                        case STR_THROWN:
                            r.setControlTurnoutState(Route.ONTHROWN);
                            break;
                        case STR_CHANGE:
                            r.setControlTurnoutState(Route.ONCHANGE);
                            break;
                        case STR_VETO_CLOSED:
                            r.setControlTurnoutState(Route.VETOCLOSED);
                            break;
                        case STR_VETO_THROWN:
                            r.setControlTurnoutState(Route.VETOTHROWN);
                            break;
                        default:
                            r.setControlTurnoutState(Route.ONCLOSED);
                    }
                } else {
                    log.error("cTurnoutState was null!");
                }
                r.setControlTurnoutFeedback(cTurnoutFeedback);
            }
            // set added delay
            r.setRouteCommandDelay(addedDelay);

            // determine if route locked
            if (routeLockedTxt != null && routeLockedTxt.equals("True")) {
                r.setLocked(true);
            }

            // add lock control turout if there is one
            if (cLockTurnout != null) {
                r.setLockControlTurnout(cLockTurnout);
                if (cLockTurnoutState != null) {
                    switch (cLockTurnoutState) {
                        case STR_THROWN:
                            r.setLockControlTurnoutState(Route.ONTHROWN);
                            break;
                        case STR_CHANGE:
                            r.setLockControlTurnoutState(Route.ONCHANGE);
                            break;
                        default:
                            r.setLockControlTurnoutState(Route.ONCLOSED);
                            break;
                    }
                } else {
                    log.error("cLockTurnoutState was null!");
                }
            }

            // load output turnouts if there are any - old format first (1.7.6 and before)
            List<Element> routeTurnoutList = el.getChildren("routeTurnout");
            if (!routeTurnoutList.isEmpty()) {
                // This route has turnouts
                for (Element element : routeTurnoutList) {
                    if (element.getAttribute(STR_SYSTEM_NAME) == null) {
                        log.warn("unexpected null in route turnout systemName {} {}", element, element.getAttributes());
                        break;
                    }
                    String tSysName = element.getAttribute(STR_SYSTEM_NAME).getValue();
                    String rState = element.getAttribute(STR_STATE).getValue();
                    int tSetState = Turnout.CLOSED;
                    if (rState.equals(STR_THROWN)) {
                        tSetState = Turnout.THROWN;
                    } else if (rState.equals(STR_TOGGLE)) {
                        tSetState = Route.TOGGLE;
                    }
                    // Add turnout to route
                    r.addOutputTurnout(tSysName, tSetState);
                }
            }
            // load output turnouts if there are any - new format
            routeTurnoutList = el.getChildren(STR_ROUTE_OUTPUT_TURNOUT);
            if (!routeTurnoutList.isEmpty()) {
                // This route has turnouts
                for (int k = 0; k < routeTurnoutList.size(); k++) { // index k is required later to get Locked state
                    if (routeTurnoutList.get(k).getAttribute(STR_SYSTEM_NAME) == null) {
                        log.warn("unexpected null in route turnout systemName {} {}", routeTurnoutList.get(k),
                                routeTurnoutList.get(k).getAttributes());
                        break;
                    }
                    String tSysName = routeTurnoutList.get(k)
                            .getAttribute(STR_SYSTEM_NAME).getValue();
                    String rState = routeTurnoutList.get(k)
                            .getAttribute(STR_STATE).getValue();
                    int tSetState = Turnout.CLOSED;
                    if (rState.equals(STR_THROWN)) {
                        tSetState = Turnout.THROWN;
                    } else if (rState.equals(STR_TOGGLE)) {
                        tSetState = Route.TOGGLE;
                    }
                    // If the Turnout has already been added to the route and is the same as that loaded, 
                    // we will not re add the turnout.
                    if (!r.isOutputTurnoutIncluded(tSysName)) {

                        // Add turnout to route
                        r.addOutputTurnout(tSysName, tSetState);

                        // determine if turnout should be locked
                        Turnout t = r.getOutputTurnout(k);
                        if ( t == null ) {
                            log.error("could not find output Turnout {}", tSysName);
                        } else if (r.getLocked()) {
                            t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                    }
                }
            }
            // load output sensors if there are any - new format
            List<Element> routeSensorList = el.getChildren(STR_ROUTE_OUTPUT_SENSOR);
            for (Element sen : routeSensorList) { // this route has output sensors
                if (sen.getAttribute(STR_SYSTEM_NAME) == null) {
                    log.warn("unexpected null in systemName {} {}", sen, sen.getAttributes());
                    break;
                }
                String tSysName = sen.getAttribute(STR_SYSTEM_NAME).getValue();
                String rState = sen.getAttribute(STR_STATE).getValue();
                int tSetState = Sensor.INACTIVE;
                if (rState.equals(STR_ACTIVE)) {
                    tSetState = Sensor.ACTIVE;
                } else if (rState.equals(STR_TOGGLE)) {
                    tSetState = Route.TOGGLE;
                }
                // If the Turnout has already been added to the route and is the same as that loaded,
                // we will not re add the turnout.
                if (r.isOutputSensorIncluded(tSysName)) {
                    break;
                }
                // Add turnout to route
                r.addOutputSensor(tSysName, tSetState);
            }
            // load sound, script files if present
            Element fileElement = el.getChild(STR_ROUTE_SOUND_FILE);
            if (fileElement != null) {
                // convert to absolute path name
                r.setOutputSoundName(
                        jmri.util.FileUtil.getExternalFilename(fileElement.getAttribute(STR_NAME).getValue())
                );
            }
            fileElement = el.getChild(STR_ROUTE_SCRIPT_FILE);
            if (fileElement != null) {
                r.setOutputScriptName(
                        jmri.util.FileUtil.getExternalFilename(fileElement.getAttribute(STR_NAME).getValue())
                );
            }
            // load turnouts aligned sensor if there is one
            fileElement = el.getChild(STR_TURNOUTS_ALIGNED_SENSOR);
            if (fileElement != null) {
                r.setTurnoutsAlignedSensor(fileElement.getAttribute(STR_NAME).getValue());
            }

            // load route control sensors, if there are any
            routeSensorList = el.getChildren(STR_ROUTE_SENSOR);
            for (Element sen : routeSensorList) { // this route has sensors
                if (sen.getAttribute(STR_SYSTEM_NAME) == null) {
                    log.warn("unexpected null in systemName {} {}", sen, sen.getAttributes());
                    break;
                } else {
                int mode = Route.ONACTIVE;  // default mode
                if (sen.getAttribute(STR_MODE) == null) {
                    break;
                }
                String sm = sen.getAttribute(STR_MODE).getValue();
                switch (sm) {
                    case STR_ON_ACTIVE:
                        mode = Route.ONACTIVE;
                        break;
                    case STR_ON_INACTIVE:
                        mode = Route.ONINACTIVE;
                        break;
                    case STR_ON_CHANGE:
                        mode = Route.ONCHANGE;
                        break;
                    case STR_VETO_ACTIVE:
                        mode = Route.VETOACTIVE;
                        break;
                    case STR_VETO_INACTIVE:
                        mode = Route.VETOINACTIVE;
                        break;
                    default:
                        log.warn("unexpected sensor mode in route {} was {}", sysName, sm);
                }
                // Add Sensor to route
                r.addSensorToRoute(sen.getAttribute(STR_SYSTEM_NAME).getValue(), mode);
                }
            }
            // and start it working
            r.activateRoute();
        }
        if (namesChanged > 0) {
            // TODO: replace the System property check with an in-application mechanism
            // for notifying users of multiple changes that can be silenced as part of
            // normal operations
            if (!GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("jmri.test.no-dialogs")) {
                JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage(namesChanged > 1 ? "RouteManager.SystemNamesChanged.Message" :
                        "RouteManager.SystemNameChanged.Message", namesChanged),
                    Bundle.getMessage("Manager.SystemNamesChanged.Title",
                        namesChanged, tm.getBeanTypeHandled(namesChanged > 1)),
                    JmriJOptionPane.WARNING_MESSAGE);
            }
            log.warn("System names for {} Routes changed; this may have operational impacts.", namesChanged); 
        }
    }

    /**
     * Replace the current RouteManager, if there is one, with one newly created
     * during a load operation. This is skipped if the present one is already of
     * the right type.
     */
    protected void replaceRouteManager() {
        RouteManager current = InstanceManager.getNullableDefault(RouteManager.class);
        if (current != null && current.getClass().getName()
                .equals(DefaultRouteManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (current != null) {
            InstanceManager.getDefault(jmri.ConfigureManager.class).deregister(current);
            InstanceManager.deregister(current, RouteManager.class);
        }

        // register new one with InstanceManager
        DefaultRouteManager pManager = InstanceManager.getDefault(DefaultRouteManager.class);
        InstanceManager.store(pManager, RouteManager.class);
        // register new one for configuration
        InstanceManager.getDefault(jmri.ConfigureManager.class).registerConfig(pManager, jmri.Manager.ROUTES);
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(RouteManager.class).getXMLOrder();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultRouteManagerXml.class);

}
