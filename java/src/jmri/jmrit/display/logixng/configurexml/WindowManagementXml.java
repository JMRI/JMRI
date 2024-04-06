package jmri.jmrit.display.logixng.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.jmrit.display.logixng.WindowManagement;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for WindowManagement objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class WindowManagementXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public WindowManagementXml() {
    }

    /**
     * Default implementation for storing the contents of a WindowManagement
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        WindowManagement p = (WindowManagement) o;

        var selectEnumHideOrShowXml = new LogixNG_SelectEnumXml<WindowManagement.HideOrShow>();
        var selectEnumMaximizeMinimizeNormalizeXml = new LogixNG_SelectEnumXml<WindowManagement.MaximizeMinimizeNormalize>();
        var selectEnumBringToFrontOrBackXml = new LogixNG_SelectEnumXml<WindowManagement.BringToFrontOrBack>();

        Element element = new Element("DisplayActionWindowManagement");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        String jmriJFrameTitle = p.getJmriJFrameTitle();
        if (jmriJFrameTitle != null) {
            element.addContent(new Element("jmriJFrameTitle").addContent(jmriJFrameTitle));
        }

        element.addContent(new Element("addressing").addContent(p.getAddressing().name()));
        element.addContent(new Element("reference").addContent(p.getReference()));
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("formula").addContent(p.getFormula()));

        element.addContent(selectEnumHideOrShowXml.store(
                p.getSelectEnumHideOrShow(), "hideOrShow"));

        element.addContent(selectEnumMaximizeMinimizeNormalizeXml.store(
                p.getSelectEnumMaximizeMinimizeNormalize(), "maximizeMinimizeNormalize"));

        element.addContent(selectEnumBringToFrontOrBackXml.store(
                p.getSelectEnumBringToFrontOrBack(), "bringToFrontOrBack"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {     // Test class that inherits this class throws exception
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        WindowManagement h = new WindowManagement(sys, uname);

        var selectEnumHideOrShowXml = new LogixNG_SelectEnumXml<WindowManagement.HideOrShow>();
        var selectEnumMaximizeMinimizeNormalizeXml = new LogixNG_SelectEnumXml<WindowManagement.MaximizeMinimizeNormalize>();
        var selectEnumBringToFrontOrBackXml = new LogixNG_SelectEnumXml<WindowManagement.BringToFrontOrBack>();

        loadCommon(h, shared);

        Element elem = shared.getChild("jmriJFrameTitle");
        if (elem != null) {
            h.setJmriJFrame(elem.getTextTrim());
        }

        try {
            elem = shared.getChild("addressing");
            if (elem != null) {
                h.setAddressing(NamedBeanAddressing.valueOf(elem.getTextTrim()));
            }

            elem = shared.getChild("reference");
            if (elem != null) h.setReference(elem.getTextTrim());

            elem = shared.getChild("localVariable");
            if (elem != null) h.setLocalVariable(elem.getTextTrim());

            elem = shared.getChild("formula");
            if (elem != null) h.setFormula(elem.getTextTrim());

        } catch (ParserException e) {
            throw new JmriConfigureXmlException(e);
        }

        selectEnumHideOrShowXml.load(shared.getChild("hideOrShow"),
                h.getSelectEnumHideOrShow());

        selectEnumMaximizeMinimizeNormalizeXml.load(
                shared.getChild("maximizeMinimizeNormalize"),
                h.getSelectEnumMaximizeMinimizeNormalize());

        Element bringToFrontOrBack = shared.getChild("bringToFrontOrBack");
        if (bringToFrontOrBack != null) {
            selectEnumBringToFrontOrBackXml.load(bringToFrontOrBack,
                    h.getSelectEnumBringToFrontOrBack());
        } else {
            // Handle pre JMRI 5.7.6
            h.getSelectEnumBringToFrontOrBack().setEnum(
                    WindowManagement.BringToFrontOrBack.Front);
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowToFrontXml.class);
}
