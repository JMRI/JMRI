package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.managers.DefaultRouteManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for configuring RouteManagers.
 *
 * @author Dave Duchamp Copyright (c) 2004
 * @author Daniel Boudreau Copyright (c) 2007
 * @author Simon Reader Copyright (C) 2008
 */
public class DefaultRouteManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

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
        Element routes = new Element("routes");
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
                boolean routeLocked = r.getLocked();
                String cLockTurnout = r.getLockControlTurnout();

                Element elem = new Element("route");
                elem.addContent(new Element("systemName").addContent(rName));

                // As a work-around for backward compatibility, store systemName and userName as attribute.
                // TODO Remove this in e.g. JMRI 4.11.1 and then update all the loadref comparison files
                String uName = r.getUserName();
                if (uName != null && !uName.equals("")) {
                    elem.setAttribute("userName", uName);
                }

                // store common parts
                storeCommon(r, elem);

                if (cTurnout != null && !cTurnout.equals("")) {
                    elem.setAttribute("controlTurnout", cTurnout);
                    int state = r.getControlTurnoutState();
                    if (state == Route.ONTHROWN) {
                        elem.setAttribute("controlTurnoutState", "THROWN");
                    } else if (state == Route.ONCHANGE) {
                        elem.setAttribute("controlTurnoutState", "CHANGE");
                    } else if (state == Route.VETOCLOSED) {
                        elem.setAttribute("controlTurnoutState", "VETOCLOSED");
                    } else if (state == Route.VETOTHROWN) {
                        elem.setAttribute("controlTurnoutState", "VETOTHROWN");
                    } else {
                        elem.setAttribute("controlTurnoutState", "CLOSED");
                    }
                }
                if (cLockTurnout != null && !cLockTurnout.equals("")) {
                    elem.setAttribute("controlLockTurnout", cLockTurnout);
                    int state = r.getLockControlTurnoutState();
                    if (state == Route.ONTHROWN) {
                        elem.setAttribute("controlLockTurnoutState", "THROWN");
                    } else if (state == Route.ONCHANGE) {
                        elem.setAttribute("controlLockTurnoutState", "CHANGE");
                    } else {
                        elem.setAttribute("controlLockTurnoutState", "CLOSED");
                    }
                }
                if (addedDelay > 0) {
                    elem.setAttribute("addedDelay", Integer.toString(addedDelay));
                }

                if (routeLocked) {
                    elem.setAttribute("routeLocked", "True");
                }
                // add route output Turnouts, if any
                int index = 0;
                String rTurnout = null;
                while ((rTurnout = r.getOutputTurnoutByIndex(index)) != null) {
                    Element rElem = new Element("routeOutputTurnout")
                            .setAttribute("systemName", rTurnout);
                    String sState = "CLOSED";
                    if (r.getOutputTurnoutSetState(rTurnout) == Turnout.THROWN) {
                        sState = "THROWN";
                    } else if (r.getOutputTurnoutSetState(rTurnout) == Route.TOGGLE) {
                        sState = "TOGGLE";
                    }
                    rElem.setAttribute("state", sState);
                    elem.addContent(rElem);
                    index++;
                }
                // add route output Sensors, if any
                index = 0;
                String rSensor = null;
                while ((rSensor = r.getOutputSensorByIndex(index)) != null) {
                    Element rElem = new Element("routeOutputSensor")
                            .setAttribute("systemName", rSensor);
                    String sState = "INACTIVE";
                    if (r.getOutputSensorSetState(rSensor) == Sensor.ACTIVE) {
                        sState = "ACTIVE";
                    } else if (r.getOutputSensorSetState(rSensor) == Route.TOGGLE) {
                        sState = "TOGGLE";
                    }
                    rElem.setAttribute("state", sState);
                    elem.addContent(rElem);
                    index++;
                }
                // add route control Sensors, if any
                index = 0;
                while ((rSensor = r.getRouteSensorName(index)) != null) {
                    Element rsElem = new Element("routeSensor")
                            .setAttribute("systemName", rSensor);
                    int mode = r.getRouteSensorMode(index);
                    String modeName;
                    switch (mode) {
                        case Route.ONACTIVE:
                            modeName = "onActive";
                            break;
                        case Route.ONINACTIVE:
                            modeName = "onInactive";
                            break;
                        case Route.ONCHANGE:
                            modeName = "onChange";
                            break;
                        case Route.VETOACTIVE:
                            modeName = "vetoActive";
                            break;
                        case Route.VETOINACTIVE:
                            modeName = "vetoInactive";
                            break;
                        default:
                            modeName = null;
                    }
                    if (modeName != null) {
                        rsElem.setAttribute("mode", modeName);
                    }
                    elem.addContent(rsElem);
                    index++;
                }
                // add sound and script file elements if needed
                if (r.getOutputSoundName() != null && !r.getOutputSoundName().equals("")) {
                    Element rsElem = new Element("routeSoundFile")
                            .setAttribute("name",
                                    jmri.util.FileUtil.getPortableFilename(
                                            new java.io.File(r.getOutputSoundName()))
                            );
                    elem.addContent(rsElem);
                }
                if (r.getOutputScriptName() != null && !r.getOutputScriptName().equals("")) {
                    Element rsElem = new Element("routeScriptFile")
                            .setAttribute("name",
                                    jmri.util.FileUtil.getPortableFilename(
                                            new java.io.File(r.getOutputScriptName()))
                            );
                    elem.addContent(rsElem);
                }

                // add turnouts aligned sensor if there is one
                if (!r.getTurnoutsAlignedSensor().equals("")) {
                    Element rsElem = new Element("turnoutsAlignedSensor")
                            .setAttribute("name", r.getTurnoutsAlignedSensor());
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
        routes.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a RouteManager object of the correct class, then register and fill
     * it.
     *
     * @param sharedRoutes Top level Element to unpack.
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
        List<Element> routeList = routes.getChildren("route");
        log.debug("Found {} routes", routeList.size());
        RouteManager tm = InstanceManager.getDefault(jmri.RouteManager.class);

        for (Element el : routeList) {

            String sysName = getSystemName(el);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", el);
                break;
            }

            String userName = getUserName(el);
            String cTurnout = null;
            String cTurnoutState = null;
            String addedDelayTxt = null;
            String routeLockedTxt = null;
            String cLockTurnout = null;
            String cLockTurnoutState = null;
            int addedDelay = 0;

            if (el.getAttribute("controlTurnout") != null) {
                cTurnout = el.getAttribute("controlTurnout").getValue();
            }
            if (el.getAttribute("controlTurnoutState") != null) {
                cTurnoutState = el.getAttribute("controlTurnoutState").getValue();
            }
            if (el.getAttribute("controlLockTurnout") != null) {
                cLockTurnout = el.getAttribute("controlLockTurnout").getValue();
            }
            if (el.getAttribute("controlLockTurnoutState") != null) {
                cLockTurnoutState = el.getAttribute("controlLockTurnoutState").getValue();
            }
            if (el.getAttribute("addedDelay") != null) {
                addedDelayTxt = el.getAttribute("addedDelay").getValue();
                if (addedDelayTxt != null) {
                    addedDelay = Integer.parseInt(addedDelayTxt);
                }
            }
            if (el.getAttribute("routeLocked") != null) {
                routeLockedTxt = el.getAttribute("routeLocked").getValue();
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
                        case "THROWN":
                            r.setControlTurnoutState(Route.ONTHROWN);
                            break;
                        case "CHANGE":
                            r.setControlTurnoutState(Route.ONCHANGE);
                            break;
                        case "VETOCLOSED":
                            r.setControlTurnoutState(Route.VETOCLOSED);
                            break;
                        case "VETOTHROWN":
                            r.setControlTurnoutState(Route.VETOTHROWN);
                            break;
                        default:
                            r.setControlTurnoutState(Route.ONCLOSED);
                    }
                } else {
                    log.error("cTurnoutState was null!");
                }
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
                    if (cLockTurnoutState.equals("THROWN")) {
                        r.setLockControlTurnoutState(Route.ONTHROWN);
                    } else if (cLockTurnoutState.equals("CHANGE")) {
                        r.setLockControlTurnoutState(Route.ONCHANGE);
                    } else {
                        r.setLockControlTurnoutState(Route.ONCLOSED);
                    }
                } else {
                    log.error("cLockTurnoutState was null!");
                }
            }

            // load output turnouts if there are any - old format first (1.7.6 and before)
            List<Element> routeTurnoutList = el.getChildren("routeTurnout");
            if (routeTurnoutList.size() > 0) {
                // This route has turnouts
                for (Element element : routeTurnoutList) {
                    if (element.getAttribute("systemName") == null) {
                        log.warn("unexpected null in systemName {} {}", element, element.getAttributes());
                        break;
                    }
                    String tSysName = element.getAttribute("systemName").getValue();
                    String rState = element.getAttribute("state").getValue();
                    int tSetState = Turnout.CLOSED;
                    if (rState.equals("THROWN")) {
                        tSetState = Turnout.THROWN;
                    } else if (rState.equals("TOGGLE")) {
                        tSetState = Route.TOGGLE;
                    }
                    // Add turnout to route
                    r.addOutputTurnout(tSysName, tSetState);
                }
            }
            // load output turnouts if there are any - new format
            routeTurnoutList = el.getChildren("routeOutputTurnout");
            if (routeTurnoutList.size() > 0) {
                // This route has turnouts
                for (int k = 0; k < routeTurnoutList.size(); k++) { // index k is required later to get Locked state
                    if (routeTurnoutList.get(k).getAttribute("systemName") == null) {
                        log.warn("unexpected null in systemName {} {}", routeTurnoutList.get(k),
                                routeTurnoutList.get(k).getAttributes());
                        break;
                    }
                    String tSysName = routeTurnoutList.get(k)
                            .getAttribute("systemName").getValue();
                    String rState = routeTurnoutList.get(k)
                            .getAttribute("state").getValue();
                    int tSetState = Turnout.CLOSED;
                    if (rState.equals("THROWN")) {
                        tSetState = Turnout.THROWN;
                    } else if (rState.equals("TOGGLE")) {
                        tSetState = Route.TOGGLE;
                    }
                    // If the Turnout has already been added to the route and is the same as that loaded, 
                    // we will not re add the turnout.
                    if (!r.isOutputTurnoutIncluded(tSysName)) {

                        // Add turnout to route
                        r.addOutputTurnout(tSysName, tSetState);

                        // determine if turnout should be locked
                        Turnout t = r.getOutputTurnout(k);
                        if (r.getLocked()) {
                            t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                        }
                    }
                }
            }
            // load output sensors if there are any - new format
            List<Element> routeSensorList = el.getChildren("routeOutputSensor");
            for (Element sen : routeSensorList) { // this route has output sensors
                if (sen.getAttribute("systemName") == null) {
                    log.warn("unexpected null in systemName {} {}", sen, sen.getAttributes());
                    break;
                }
                String tSysName = sen.getAttribute("systemName").getValue();
                String rState = sen.getAttribute("state").getValue();
                int tSetState = Sensor.INACTIVE;
                if (rState.equals("ACTIVE")) {
                    tSetState = Sensor.ACTIVE;
                } else if (rState.equals("TOGGLE")) {
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
            Element fileElement = el.getChild("routeSoundFile");
            if (fileElement != null) {
                // convert to absolute path name
                r.setOutputSoundName(
                        jmri.util.FileUtil.getExternalFilename(fileElement.getAttribute("name").getValue())
                );
            }
            fileElement = el.getChild("routeScriptFile");
            if (fileElement != null) {
                r.setOutputScriptName(
                        jmri.util.FileUtil.getExternalFilename(fileElement.getAttribute("name").getValue())
                );
            }
            // load turnouts aligned sensor if there is one
            fileElement = el.getChild("turnoutsAlignedSensor");
            if (fileElement != null) {
                r.setTurnoutsAlignedSensor(fileElement.getAttribute("name").getValue());
            }

            // load route control sensors, if there are any
            routeSensorList = el.getChildren("routeSensor");
            for (Element sen : routeSensorList) { // this route has sensors
                if (sen.getAttribute("systemName") == null) {
                    log.warn("unexpected null in systemName {} {}", sen, sen.getAttributes());
                    break;
                }
                int mode = Route.ONACTIVE;  // default mode
                if (sen.getAttribute("mode") == null) {
                    break;
                }
                String sm = sen.getAttribute("mode").getValue();
                switch (sm) {
                    case "onActive":
                        mode = Route.ONACTIVE;
                        break;
                    case "onInactive":
                        mode = Route.ONINACTIVE;
                        break;
                    case "onChange":
                        mode = Route.ONCHANGE;
                        break;
                    case "vetoActive":
                        mode = Route.VETOACTIVE;
                        break;
                    case "vetoInactive":
                        mode = Route.VETOINACTIVE;
                        break;
                    default:
                        log.warn("unexpected sensor mode in route {} was {}", sysName, sm);
                }
                // Add Sensor to route
                r.addSensorToRoute(sen.getAttribute("systemName").getValue(), mode);
            }
            // and start it working
            r.activateRoute();
        }
    }

    /**
     * Replace the current RouteManager, if there is one, with one newly created
     * during a load operation. This is skipped if the present one is already of
     * the right type.
     */
    protected void replaceRouteManager() {
        RouteManager current = InstanceManager.getNullableDefault(jmri.RouteManager.class);
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
        return InstanceManager.getDefault(jmri.RouteManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultRouteManagerXml.class);

}
