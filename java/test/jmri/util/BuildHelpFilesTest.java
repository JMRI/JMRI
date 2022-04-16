package jmri.util;

import java.io.*;
import java.util.Properties;
import java.util.TreeSet;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Assume;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Builds the help index page for the JMRI help pages that's accessed as files
 * by the user's web browser.
 *
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class BuildHelpFilesTest {

    private String _lang;
    private PrintWriter _mapJhmWriter;
    private TreeSet<String> _helpKeys;
    private Properties _alternateMap;


    // The main() method is used when this class is run directly from ant
    static public void main(String[] args) throws IOException, JDOMException {
        boolean enResult = new BuildHelpFilesTest().buildIndex("en");
        boolean frResult = new BuildHelpFilesTest().buildIndex("fr");
        if (enResult && frResult) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }


    private class XmlFile extends jmri.jmrit.XmlFile {
    }

    private void parseElement(Element e) throws IOException {
        String helpKey = e.getAttributeValue("target");
        if (helpKey != null) _helpKeys.add(helpKey);

        for (Element child : e.getChildren()) {
            parseElement(child);
        }
    }

    private boolean buildIndex(String lang) throws JDOMException, IOException {
        boolean result = true;

        _lang = lang;

        _alternateMap = new Properties();
        try (InputStream input = new FileInputStream(FileUtil.getProgramPath() + "help/" + _lang + "/local/alternate_map.txt")) {
            _alternateMap.load(input);
        }

        _helpKeys = new TreeSet<>();

        XmlFile xmlFile = new XmlFile();
        Assert.assertNotNull(xmlFile);
        Element e = xmlFile.rootFromName(FileUtil.getProgramPath()
                + "help/" + _lang + "/JmriHelp_" + _lang + "TOC.xml");
        Assert.assertNotNull(e);
        parseElement(e);

        xmlFile = new XmlFile();
        Assert.assertNotNull(xmlFile);
        e = xmlFile.rootFromName(FileUtil.getProgramPath()
                + "help/" + _lang + "/JmriHelp_" + _lang + "Index.xml");
        Assert.assertNotNull(e);
        parseElement(e);

        // Generate jmri_map.xml
        FileWriter fileWriter = new FileWriter(FileUtil.getProgramPath() + "help/" + _lang + "/local/jmri_map.xml");
        _mapJhmWriter = new PrintWriter(fileWriter);

        _mapJhmWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _mapJhmWriter.println("<map xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/help-map.xsd\">");

        for (String helpKey : _helpKeys) {
            String expandedHelpKey;
            if (_alternateMap.containsKey(helpKey)) {
                expandedHelpKey = _alternateMap.getProperty(helpKey);
            } else {
                expandedHelpKey = helpKey.replace(".", "/");
                int pos = expandedHelpKey.indexOf('_');
                if (pos == -1) {
                    expandedHelpKey = expandedHelpKey + ".shtml";
                } else {
                    expandedHelpKey = expandedHelpKey.substring(0, pos) + ".shtml"
                            + "#" + expandedHelpKey.substring(pos+1);
                }
            }
            _mapJhmWriter.format("<mapID target=\"%s\" url=\"%s\"/>%n", helpKey, expandedHelpKey);
        }

        _mapJhmWriter.println("</map>");
        _mapJhmWriter.close();

        return result;
    }


    @Test
    public void testBuildIndex() throws JDOMException, IOException {
        Assume.assumeFalse("Ignoring BuildHelpFilesTest", Boolean.getBoolean("jmri.skipBuildHelpFilesTest"));
        buildIndex("en");
        buildIndex("fr");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BuildHelpFilesTest.class);
}
