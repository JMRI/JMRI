package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutTrackView;
import jmri.jmrit.logixng.LogixNG_Manager;

import org.jdom2.Element;

/**
 * Base class for Xml classes for classes that inherits LayoutTrackView.
 * @author Daniel Bergqvist (C) 2022
 */
public abstract class LayoutTrackViewXml extends AbstractXmlAdapter {

    public void storeLogixNG_Data(LayoutTrackView ltv, Element element) {
        if (ltv.getLogixNG() == null) return;

        // Don't save LogixNG data if we don't have any ConditionalNGs
        if (ltv.getLogixNG().getNumConditionalNGs() == 0) return;
        Element logixNG_Element = new Element("LogixNG");
        logixNG_Element.addContent(new Element("InlineLogixNG_SystemName").addContent(ltv.getLogixNG().getSystemName()));
        element.addContent(logixNG_Element);
    }

    public void loadLogixNG_Data(LayoutTrackView ltv, Element element) {
        Element logixNG_Element = element.getChild("LogixNG");
        if (logixNG_Element == null) return;
        Element inlineLogixNG = logixNG_Element.getChild("InlineLogixNG_SystemName");
        if (inlineLogixNG != null) {
            String systemName = inlineLogixNG.getTextTrim();
            ltv.setLogixNG_SystemName(systemName);
            InstanceManager.getDefault(LogixNG_Manager.class).registerSetupTask(() -> {
                ltv.setupLogixNG();
            });
        }
    }

}
