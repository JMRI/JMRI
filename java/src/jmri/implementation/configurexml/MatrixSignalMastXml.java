// MatrixSignalMastXml.java
package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.implementation.MatrixSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for a DefaultSignalMastManager object.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @author Egbert Broerse Copyright: Copyright (c) 2016
 */
public class MatrixSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public MatrixSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalMastManager
     *
     * @param o Object to store, of type MatrixSignalMast
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        MatrixSignalMast p = (MatrixSignalMast) o;
        Element e = new Element("signalmast");
        e.setAttribute("class", this.getClass().getName());
        e.addContent(new Element("systemName").addContent(p.getSystemName()));
        storeCommon(p, e);

        // mast properties:
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);

        Element bitnum = new Element("bitnum");
        bitnum.setAttribute("bits", "5");  // add a method to MatrixMast to read value
        // number of bits in  matrix; default = 1, max. = 5
        // (might be the same as number of turnouts stored)
        e.addContent(bitnum);

        Element outputs = new Element("outputs");
        bitnum.setAttribute("turnouts", "LT1, LT2, LT3, LT4, LT5"); // add a method in MatrixMast.java to read these
        // max 5 outputs (turnouts)
        e.addContent(outputs);

        List<String> disabledAspects = p.getDisabledAspects();
        if (disabledAspects != null) {
            Element el = new Element("disabledAspects");
            for (String aspect : disabledAspects) {
                Element ele = new Element("disabledAspect");
                ele.addContent(aspect);
                el.addContent(ele);
            }
            if (disabledAspects.size() != 0) {
                e.addContent(el);
            }
        }
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        SignalMast m;
        String sys = getSystemName(shared);
        try {
            m = InstanceManager.signalMastManagerInstance()
                    .provideSignalMast(sys);
        } catch (Exception e) {
            log.error("An error occured while trying to create the signal '" + sys + "' " + e.toString());
            return false;
        }
        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }

        loadCommon(m, shared);
        if (shared.getChild("unlit") != null) {
            Element unlit = shared.getChild("unlit");
            if (unlit.getAttribute("allowed") != null) {
                if (unlit.getAttribute("allowed").getValue().equals("no")) {
                    m.setAllowUnLit(false);
                } else {
                    m.setAllowUnLit(true);
                }
            }
        }
        Element e = shared.getChild("disabledAspects");
        if (e != null) {
            List<Element> list = e.getChildren("disabledAspect");
            for (Element aspect : list) {
                ((MatrixSignalMast) m).setAspectDisabled(aspect.getText());
            }
        }
        Element b = shared.getChild("bitnum");
        if (b != null) {
            int bitNum = -1;
            try {
                String value = b.getChild("bitnum").getValue();
                bitNum = parseInt(value); // Integer? EBR

            } catch (Exception ex) {
                log.error("failed to convert number of bits in matrix");
            }
            m.setBitNum(bitNum);
        }
        return true;
    }

    public void setBitNum(int value) {
        m.bitNum = value; // declare?
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(MatrixSignalMastXml.class.getName());
}
