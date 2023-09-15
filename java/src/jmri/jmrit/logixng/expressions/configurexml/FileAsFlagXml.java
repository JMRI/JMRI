package jmri.jmrit.logixng.expressions.configurexml;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.FileAsFlag;
import jmri.jmrit.logixng.expressions.FileAsFlag.DeleteOrKeep;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectStringXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for FileAsFlag objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class FileAsFlagXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public FileAsFlagXml() {
    }

    /**
     * Default implementation for storing the contents of a SE8cSignalMast
     *
     * @param o Object to store, of type TripleLightSignalMast
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        FileAsFlag p = (FileAsFlag) o;

        var selectFilenameXml = new LogixNG_SelectStringXml();
        var selectDeleteOrKeepXml = new LogixNG_SelectEnumXml<DeleteOrKeep>();

        Element element = new Element("FileAsFlag");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectFilenameXml.store(p.getSelectFilename(), "filename"));
        element.addContent(selectDeleteOrKeepXml.store(p.getSelectDeleteOrKeep(), "deleteOrKeep"));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        FileAsFlag h = new FileAsFlag(sys, uname);

        var selectFilenameXml = new LogixNG_SelectStringXml();
        var selectDeleteOrKeepXml = new LogixNG_SelectEnumXml<DeleteOrKeep>();

        loadCommon(h, shared);

        selectFilenameXml.load(shared.getChild("filename"), h.getSelectFilename());
        selectDeleteOrKeepXml.load(shared.getChild("deleteOrKeep"), h.getSelectDeleteOrKeep());

        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileAsFlagXml.class);
}
