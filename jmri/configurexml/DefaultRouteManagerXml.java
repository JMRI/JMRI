// DefaultRouteManagerConfigXML.java

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.DefaultRouteManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the functionality for
 * configuring RouteManagers
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2004
 * @version $Revision: 1.3 $
 */
public class DefaultRouteManagerXml implements XmlAdapter {

    public DefaultRouteManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * RouteManager
     * @param o Object to store, of type RouteManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element routes = new Element("routes");
        setStoreElementClass(routes);
        RouteManager tm = (RouteManager) o;
        if (tm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                Route r = tm.getBySystemName(sname);
                String uname = r.getUserName();
                String cTurnout = r.getControlTurnout();
                Element elem = new Element("route")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                if (cTurnout!=null) {
                    elem.addAttribute("controlTurnout", cTurnout);
                    int state = r.getControlTurnoutState();
                    if (state == jmri.Turnout.THROWN) {
                        elem.addAttribute("controlTurnoutState","THROWN");
                    }
                    else {
                        elem.addAttribute("controlTurnoutState","CLOSED");
                    }
                }
                // add route Turnouts, if any
                int index = 0;
                String rTurnout = null;
                while ( (rTurnout = r.getRouteTurnoutByIndex(index)) != null) {
                    Element rElem = new Element("routeTurnout")
                                    .addAttribute("systemName", rTurnout);
                    String sState = "CLOSED";
                    if (r.getTurnoutSetState(rTurnout)==jmri.Turnout.THROWN) {
                        sState = "THROWN";
                    }
                    rElem.addAttribute("state", sState);
                    elem.addContent(rElem);
                    index ++;
                }
                // add route control Sensors, if any
                index = 0;
                String rSensor = null;
                while ( (rSensor = r.getRouteSensor(index)) != null) {
                    Element rsElem = new Element("routeSensor")
                                    .addAttribute("systemName", rSensor);
                    elem.addContent(rsElem);
                    index ++;
                }
                log.debug("store route "+sname+":"+uname);
                routes.addContent(elem);
            }
        }
        return routes;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param routes The top-level element being created
     */
    public void setStoreElementClass(Element routes) {
        routes.addAttribute("class","jmri.configurexml.DefaultRouteManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a RouteManager object of the correct class, then
     * register and fill it.
     * @param routes Top level Element to unpack.
     */
    public void load(Element routes) {
        // create the master object
        DefaultRouteManager mgr = DefaultRouteManager.instance();
        // load individual routes
        loadRoutes(routes);
    }


    /**
     * Utility method to load the individual Route objects.
     * If there's no additional info needed for a specific route type,
     * invoke this with the parent of the set of Route elements.
     * @param routes Element containing the Route elements to load.
     */
    public void loadRoutes(Element routes) {
        List routeList = routes.getChildren("route");
        if (log.isDebugEnabled()) log.debug("Found "+routeList.size()+" routes");
        RouteManager tm = InstanceManager.routeManagerInstance();

        for (int i=0; i<routeList.size(); i++) {
            if ( ((Element)(routeList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(routeList.get(i)))+" "+
                                                        ((Element)(routeList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(routeList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            String cTurnout = null;
            String cTurnoutState = null;
            if ( ((Element)(routeList.get(i))).getAttribute("userName") != null)
                userName = ((Element)(routeList.get(i))).getAttribute("userName").getValue();
            if ( ((Element)(routeList.get(i))).getAttribute("controlTurnout") != null)
                cTurnout = ((Element)(routeList.get(i))).getAttribute("controlTurnout").getValue();
            if ( ((Element)(routeList.get(i))).getAttribute("controlTurnoutState") != null)
                cTurnoutState = ((Element)(routeList.get(i))).getAttribute("controlTurnoutState").getValue();
            if (log.isDebugEnabled()) log.debug("create route: ("+sysName+")("+
                                                            (userName==null?"<null>":userName)+")");
            Route r = tm.createNewRoute(sysName, userName);
            if (r!=null) {
                // add control turnout if there is one
                if (cTurnout != null) {
                    r.setControlTurnout(cTurnout);
                    if ( cTurnoutState.equals("THROWN") ) {
                        r.setControlTurnoutState(jmri.Turnout.THROWN);
                    }
                    else {
                        r.setControlTurnoutState(jmri.Turnout.CLOSED);
                    }
                }
                // load route turnouts if there are any
                List routeTurnoutList = ((Element)(routeList.get(i))).getChildren("routeTurnout");
                if (routeTurnoutList.size() > 0) {
                    // This route has turnouts
                    for (int k=0; k<routeTurnoutList.size(); k++) {
                        if ( ((Element)(routeTurnoutList.get(k))).getAttribute("systemName") == null) {
                            log.warn("unexpected null in systemName "+((Element)(routeTurnoutList.get(k)))+
                                                " "+((Element)(routeTurnoutList.get(k))).getAttributes());
                            break;
                        }
                        String tSysName = ((Element)(routeTurnoutList.get(k)))
                                                            .getAttribute("systemName").getValue();
                        String rState = ((Element)(routeTurnoutList.get(k)))
                                                            .getAttribute("state").getValue();
                        int tSetState = jmri.Turnout.CLOSED;
                        if (rState.equals("THROWN")) {
                            tSetState = jmri.Turnout.THROWN;
                        }
                        // Add turnout to route
                        r.addTurnoutToRoute(tSysName, tSetState);
                    }
                }
                // load route control sensors, if there are any
                List routeSensorList = ((Element)(routeList.get(i))).getChildren("routeSensor");
                if (routeSensorList.size() > 0) {
                    // This route has sensors
                    for (int k=0; k<routeSensorList.size(); k++) {
                        if ( ((Element)(routeSensorList.get(k))).getAttribute("systemName") == null) {
                            log.warn("unexpected null in systemName "+((Element)(routeSensorList.get(k)))+
                                                " "+((Element)(routeSensorList.get(k))).getAttributes());
                            break;
                        }
                        // Add Sensor to route
                        r.addSensorToRoute(((Element)(routeSensorList.get(k)))
                                                        .getAttribute("systemName").getValue());
                    }
                }
            // and start it working
            r.activateRoute();
            
            }
            else {
                log.error ("failed to create Route: "+sysName);
            }
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultRouteManagerXml.class.getName());
}