package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalAppearanceMap;
import jmri.implementation.MatrixSignalMast;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for MatrixSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: (C) 2009
 * @author Egbert Broerse Copyright: (C) 2016, 2017
 */
public class MatrixSignalMastXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public MatrixSignalMastXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MatrixSignalMastManager.
     *
     * @param o Object to store, of type MatrixSignalMast
     * @return e Element containing the complete info
     */
    @Override
    public Element store(Object o) { // from mast p to XML
        MatrixSignalMast p = (MatrixSignalMast) o;

        Element e = new Element("matrixsignalmast");
        e.setAttribute("class", this.getClass().getName());

        // include content
        e.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, e); // username, comment & properties

        // mast properties:
        Element unlit = new Element("unlit");
        if (p.allowUnLit()) {
            unlit.setAttribute("allowed", "yes");
            unlit.addContent(new Element("bitString").addContent(p.getUnLitChars()));
        } else {
            unlit.setAttribute("allowed", "no");
        }
        e.addContent(unlit);

        // store mast-specific delay, since 4.15.7
        Element delay = new Element("delay");
        if (p.getMatrixMastCommandDelay() > 0) {
            delay.setAttribute("duration", Integer.toString(p.getMatrixMastCommandDelay()));
        } else {
            delay.setAttribute("duration", "0");
        }
        e.addContent(delay);

        List<String> outputs = p.getOutputs();
        // convert char[] to xml-storable simple String
        // max. 5 outputs (either: turnouts (bean names) [or ToDo: DCC addresses (numbers)]
        // spotted by SpotBugs as to never be null (check on creation of MatrixMast)
        Element outps = new Element("outputs");
        int i = 1;
        for (String _output : outputs) {
            log.debug("   handling {}", _output);
            String key = ("output" + i);
            Element outp = new Element("output");
            outp.setAttribute("matrixCol", key);
            outp.addContent(p.getOutputName(i)); // get name (Turnout)
            outps.addContent(outp);
            i++;
        }
        if (outputs.size() != 0) {
            e.addContent(outps);
        }

        // string of max. 6 chars "001010" describing matrix row per aspect
        SignalAppearanceMap appMap = p.getAppearanceMap();
        if (appMap != null) {
            Element bss = new Element("bitStrings");
            java.util.Enumeration<String> aspects = appMap.getAspects();
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement();
                Element bs = new Element("bitString");
                bs.setAttribute("aspect", key);
                bs.addContent(p.getBitstring(key));
                bss.addContent(bs);
            }
                e.addContent(bss);

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
    public boolean load(Element shared, Element perNode) { // from XML to mast m
        MatrixSignalMast m;
        String sys = getSystemName(shared);
        try {
            m = (MatrixSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class)
                    .provideCustomSignalMast(sys, MatrixSignalMast.class);
        } catch (JmriException e) {
            log.error("Failed to load MatrixSignalMast {}: {}", sys, e);
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
                    String bits = unlit.getChild("bitString").getText();
                    m.setUnLitBits(bits);
                }
            }
        }

        if (shared.getChild("delay") != null) { // load mast-specific delay, since 4.15.7
            Element delay = shared.getChild("delay");
            if (delay.getAttribute("duration") != null) {
                m.setMatrixMastCommandDelay(Integer.parseInt(delay.getAttribute("duration").getValue()));
            }
        }

        Element outps = shared.getChild("outputs"); // multiple
        if (outps != null) {
            List<Element> list = outps.getChildren("output"); // singular
            m.setBitNum(list.size()); // set char[] size before creating outputs
            for (Element outp : list) {
                String outputname = outp.getAttribute("matrixCol").getValue();
                String turnoutname = outp.getText();
                m.setOutput(outputname, turnoutname);
            }
        }

        Element bss = shared.getChild("bitStrings"); // multiple
        if (bss != null) {
            List<Element> list = bss.getChildren("bitString"); // singular
            for (Element bs : list) {
                m.setBitstring(bs.getAttribute("aspect").getValue(), bs.getText()); // OK if value is null
            }
        }

        Element disabled = shared.getChild("disabledAspects"); // multiple
        if (disabled != null) {
            List<Element> list = disabled.getChildren("disabledAspect"); // singular
            for (Element asp : list) {
                m.setAspectDisabled(asp.getText());
            }
        }
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(MatrixSignalMastXml.class);

}
