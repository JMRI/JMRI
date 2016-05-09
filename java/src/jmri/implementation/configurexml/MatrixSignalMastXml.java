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

        storeCommon(p, e); // username, comment & properties

        // mast properties:
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);

        List<String> outputs = p.getOutputs();
        // max. 5 outputs (either: turnouts (bean names) or DCC addresses (numbers)
        if (outputs != null) {
            Element outps = new Element("outputs");
            int i = 1;
            for (String _output : outputs) {
                String key = ("bit" + i);
                Element outp = new Element("output");
                outp.setAttribute("defines", key);
                outp.addContent(p.getTurnoutName(key)); // get beanname
                outps.addContent(outp);
                i++;
            }
            if (outputs.size() != 0) {
                e.addContent(outps);
            }
        }

        List<String> bitStrings = p.getBitStrings();
        // string of max. 5 chars "00101" describing matrix row per aspect
        if (bitStrings != null) {
            Element bss = new Element("bitStrings");
            int i = 1;
            for (String _bitstring : bitStrings) {
                String key = aspect;
                Element bs = new Element("bitString");
                outp.setAttribute("aspect", key);
                bs.addContent(_bitstring);
                bss.addContent(bs);
            }
            if (bitStrings.size() != 0) {
                e.addContent(bss);
            }
        }
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

        loadCommon(m, shared); // username & comment

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

        Element outps = shared.getChild("outputs"); // multiple
        if (outps != null) {
            List<Element> list = outps.getChildren("output"); // singular
            for (Element output : list) {
                ((MatrixSignalMast) m).setOutputs(output.getText());
            }
        }

        Element e = shared.getChild("disabledAspects"); // multiple
        if (e != null) {
            List<Element> list = e.getChildren("disabledAspect"); // singular
            for (Element aspect : list) {
                ((MatrixSignalMast) m).setAspectDisabled(aspect.getText());
            }
        }
        Element bss = shared.getChild("bitStrings"); // multiple
        if (bss != null) {
            List<Element> list = bss.getChildren("bitString"); // singular
            for (Element aspect : list) {
                ((MatrixSignalMast) m).setAspectBitstring(aspect.getText());
            }
        }
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(MatrixSignalMastXml.class.getName());
}
