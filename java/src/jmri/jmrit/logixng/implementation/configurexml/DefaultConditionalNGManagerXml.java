package jmri.jmrit.logixng.implementation.configurexml;

import java.util.List;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGManager;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.ThreadingUtil;

import org.jdom2.Element;

/**
 * Provides the functionality for configuring ConditionalNGManagers
 * <P>
 *
 * @author Dave Duchamp     Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2018
 * @author Dave Sand        Copyright (c) 2021
 */
public class DefaultConditionalNGManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultConditionalNGManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a ConditionalNG_Manager
     *
     * @param o Object to store, of type ConditionalNG_Manager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element conditionalNGs = new Element("LogixNGConditionalNGs");
        setStoreElementClass(conditionalNGs);
        ConditionalNG_Manager tm = (ConditionalNG_Manager) o;
        LogixNG_Manager lm = InstanceManager.getDefault(LogixNG_Manager.class);
        if (tm != null) {
            if (lm.getNamedBeanSet().isEmpty()) return null;
            for (LogixNG logixNG : lm.getNamedBeanSet()) {
                for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
                    ConditionalNG conditionalNG = logixNG.getConditionalNG(i);

                    log.debug("ConditionalNG system name is " + conditionalNG.getSystemName());  // NOI18N
                    boolean enabled = conditionalNG.isEnabled();
                    Element elem = new Element("ConditionalNG");  // NOI18N
                    elem.addContent(new Element("systemName").addContent(conditionalNG.getSystemName()));  // NOI18N

                    // store common part
                    storeCommon(conditionalNG, elem);

                    elem.addContent(new Element("thread").addContent(
                            Integer.toString(conditionalNG.getStartupThreadId())));  // NOI18N

                    Element e2 = new Element("Socket");
                    e2.addContent(new Element("socketName").addContent(conditionalNG.getChild(0).getName()));
                    MaleSocket socket = conditionalNG.getChild(0).getConnectedSocket();
                    String socketSystemName;
                    if (socket != null) {
                        socketSystemName = socket.getSystemName();
                    } else {
                        socketSystemName = ((DefaultConditionalNG)conditionalNG).getSocketSystemName();
                    }
                    if (socketSystemName != null) {
                        e2.addContent(new Element("systemName").addContent(socketSystemName));
                    }
                    elem.addContent(e2);

                    elem.setAttribute("enabled", enabled ? "yes" : "no");  // NOI18N

                    conditionalNGs.addContent(elem);
                }
            }
        }
        return (conditionalNGs);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param logixngs The top-level element being created
     */
    public void setStoreElementClass(Element logixngs) {
        logixngs.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    /**
     * Create a ConditionalNG_Manager object of the correct class, then register and
     * fill it.
     *
     * @param sharedConditionalNG  Shared top level Element to unpack.
     * @param perNodeConditionalNG Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedConditionalNG, Element perNodeConditionalNG) {
        // create the master object
        replaceConditionalNGManager();
        // load individual sharedLogix
        loadConditionalNGs(sharedConditionalNG);
        return true;
    }

    /**
     * Utility method to load the individual ConditionalNG objects. If there's no
     * additional info needed for a specific logixng type, invoke this with the
     * parent of the set of ConditionalNG elements.
     *
     * @param conditionalNGs Element containing the ConditionalNG elements to load.
     */
    public void loadConditionalNGs(Element conditionalNGs) {
        List<Element> conditionalNGList = conditionalNGs.getChildren("ConditionalNG");  // NOI18N
        log.debug("Found " + conditionalNGList.size() + " ConditionalNGs");  // NOI18N
        ConditionalNG_Manager tm = InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class);

        for (int i = 0; i < conditionalNGList.size(); i++) {

            Element conditionalNG_Element = conditionalNGList.get(i);

            String sysName = getSystemName(conditionalNG_Element);
            if (sysName == null) {
                log.warn("unexpected null in systemName " + conditionalNG_Element);  // NOI18N
                break;
            }

            String userName = getUserName(conditionalNG_Element);

            int threadId = LogixNG_Thread.DEFAULT_LOGIXNG_THREAD;
            Element threadElem = conditionalNG_Element.getChild("thread");
            if (threadElem != null) threadId = Integer.parseInt(threadElem.getTextTrim());

            String enabled = "";
            if (conditionalNG_Element.getAttribute("enabled") != null) {  // NOI18N
                enabled = conditionalNG_Element.getAttribute("enabled").getValue();  // NOI18N
            }
            log.debug("create ConditionalNG: (" + sysName + ")("  // NOI18N
                    + (userName == null ? "<null>" : userName) + ")");  // NOI18N

            // Create a new ConditionalNG but don't setup the initial tree.
            LogixNG logixNG = tm.getParentLogixNG(sysName);
            if (logixNG == null) {
                log.warn("unexpected null while finding parent logixNG for '{}'", conditionalNG_Element);  // NOI18N
                break;
            }

            DefaultConditionalNG conditionalNG =
                    (DefaultConditionalNG)tm.createConditionalNG(logixNG, sysName, userName, threadId);

            if (conditionalNG != null) {
                // load common part
                loadCommon(conditionalNG, conditionalNG_Element);

                Element socketName = conditionalNG_Element.getChild("Socket").getChild("socketName");
                if (socketName != null) {
                    conditionalNG.getFemaleSocket().setName(socketName.getTextTrim());
                }
                Element socketSystemName = conditionalNG_Element.getChild("Socket").getChild("systemName");
                if (socketSystemName != null) {
//                    log.warn("Socket system name: {}", socketSystemName.getTextTrim());
                    conditionalNG.setSocketSystemName(socketSystemName.getTextTrim());
                }

                // set enabled/disabled if attribute was present
                if ((enabled != null) && (!enabled.equals(""))) {
                    if (enabled.equals("yes")) {  // NOI18N
                        conditionalNG.setEnabled(true);
                    } else if (enabled.equals("no")) {  // NOI18N
                        conditionalNG.setEnabled(false);
                    }
                }
            }
        }
    }

    /**
     * Replace the current LogixManager, if there is one, with one newly created
     * during a load operation. This is skipped if they are of the same absolute
     * type.
     */
    protected void replaceConditionalNGManager() {
        if (InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class).getClass().getName()
                .equals(DefaultConditionalNGManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class));
            }

        }

        ThreadingUtil.runOnGUI(() -> {
            // register new one with InstanceManager
            DefaultConditionalNGManager pManager = DefaultConditionalNGManager.instance();
            InstanceManager.store(pManager, ConditionalNG_Manager.class);
            // register new one for configuration
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.registerConfig(pManager, jmri.Manager.LOGIXNGS);
            }
        });
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.jmrit.logixng.ConditionalNG_Manager.class).getXMLOrder();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNGManagerXml.class);
}
