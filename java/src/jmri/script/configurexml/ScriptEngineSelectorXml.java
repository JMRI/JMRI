package jmri.script.configurexml;

import jmri.script.ScriptEngineSelector;

import org.jdom2.Element;

/**
 * Xml class for jmri.script.ScriptEngineSelector.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ScriptEngineSelectorXml {

    /**
     * Default implementation for storing the contents of a ScriptEngineSelector
     *
     * @param ScriptEngineSelector the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(ScriptEngineSelector ScriptEngineSelector, String tagName) {
        Element scriptTypeElement = new Element(tagName);

        if (ScriptEngineSelector.getSelectedEngine() != null) {
            scriptTypeElement.addContent(new Element("language").addContent(ScriptEngineSelector.getSelectedEngine().getLanguageName()));
        }

        return scriptTypeElement;
    }

    public void load(Element scriptTypeElement, ScriptEngineSelector ScriptEngineSelector) {

        if (scriptTypeElement != null) {
            Element elem = scriptTypeElement.getChild("language");
            if (elem != null) {
                ScriptEngineSelector.setSelectedEngine(elem.getTextTrim());
            }
        }
    }

}
