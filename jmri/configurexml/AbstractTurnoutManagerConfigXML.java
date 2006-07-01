package jmri.configurexml;

import jmri.InstanceManager;
import jmri.TurnoutManager;
import com.sun.java.util.collections.List;
import org.jdom.Element;

/**
 * Provides the abstract base and store functionality for
 * configuring TurnoutManagers, working with
 * AbstractTurnoutManagers.
 * <P>
 * Typically, a subclass will just implement the load(Element turnouts)
 * class, relying on implementation here to load the individual turnouts.
 * Note that these are stored explicitly, so the
 * resolution mechanism doesn't need to see *Xml classes for each
 * specific Turnout or AbstractTurnout subclass at store time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.9 $
 */
public abstract class AbstractTurnoutManagerConfigXML implements XmlAdapter {

    public AbstractTurnoutManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutManager
     * @param o Object to store, of type TurnoutManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element turnouts = new Element("turnouts");
        setStoreElementClass(turnouts);
        TurnoutManager tm = (TurnoutManager) o;
        if (tm!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    tm.getSystemNameList().iterator();

            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during store");
                log.debug("system name is "+sname);
                String uname = tm.getBySystemName(sname).getUserName();
                Element elem = new Element("turnout")
                            .addAttribute("systemName", sname);
                if (uname!=null) elem.addAttribute("userName", uname);
                log.debug("store turnout "+sname+":"+uname);
                turnouts.addContent(elem);

            }
        }
        return turnouts;
    }

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param turnouts The top-level element being created
     */
    abstract public void setStoreElementClass(Element turnouts);

    /**
     * Create a TurnoutManager object of the correct class, then
     * register and fill it.
     * @param turnouts Top level Element to unpack.
     */
    abstract public void load(Element turnouts);

    /**
     * Utility method to load the individual Turnout objects.
     * If there's no additional info needed for a specific turnout type,
     * invoke this with the parent of the set of Turnout elements.
     * @param turnouts Element containing the Turnout elements to load.
     */
    public void loadTurnouts(Element turnouts) {
        List turnoutList = turnouts.getChildren("turnout");
        if (log.isDebugEnabled()) log.debug("Found "+turnoutList.size()+" turnouts");
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();

        for (int i=0; i<turnoutList.size(); i++) {
            if ( ((Element)(turnoutList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+((Element)(turnoutList.get(i)))+" "+((Element)(turnoutList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(turnoutList.get(i))).getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(turnoutList.get(i))).getAttribute("userName") != null)
            userName = ((Element)(turnoutList.get(i))).getAttribute("userName").getValue();
            if (log.isDebugEnabled()) log.debug("create turnout: ("+sysName+")("+(userName==null?"<null>":userName)+")");
            tm.newTurnout(sysName, userName);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractTurnoutManagerConfigXML.class.getName());
}