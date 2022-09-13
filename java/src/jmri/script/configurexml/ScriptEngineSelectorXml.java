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
     * @param scriptEngineSelector the LogixNG_SelectTable object
     * @param tagName the name of the element
     * @return Element containing the complete info
     */
    public Element store(ScriptEngineSelector scriptEngineSelector, String tagName) {
        Element scriptTypeElement = new Element(tagName);

        ScriptEngineSelector.Engine engine = scriptEngineSelector.getSelectedEngine();
        if (engine != null) {
            scriptTypeElement.addContent(new Element("language").addContent(engine.getLanguageName()));
        }

        return scriptTypeElement;
    }

    public void load(Element scriptTypeElement, ScriptEngineSelector scriptEngineSelector) {

        if (scriptTypeElement != null) {
            Element elem = scriptTypeElement.getChild("language");
            if (elem != null) {
                scriptEngineSelector.setSelectedEngine(elem.getTextTrim());
            }
        }
    }

}
