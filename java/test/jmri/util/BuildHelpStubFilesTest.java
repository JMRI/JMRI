package jmri.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Builds the help index page for the JMRI help pages that's accessed as files
 * by the user's web browser.
 * 
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class BuildHelpStubFilesTest {

    private final String template;
    
    
    public BuildHelpStubFilesTest() throws IOException {
        Path path = Path.of(FileUtil.getProgramPath() + "help/en/local/stub_template.html");
        template = Files.readString(path);
    }
    
    private class XmlFile extends jmri.jmrit.XmlFile {
    }
    
    private void generateStubFile(String helpKey) throws IOException {
        FileWriter fileWriter = new FileWriter(FileUtil.getProgramPath() + "help/en/local/stub/"+helpKey+".html");
        try (PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String contents = template.replaceFirst("<!--HELP_KEY-->", helpKey);
            printWriter.print(contents);
        }
    }
    
    private void parseElement(Element e) throws IOException {
        if ("tocitem".equals(e.getName())) {
            String helpKey = e.getAttributeValue("target");
            if (helpKey != null) {
                generateStubFile(helpKey);
            }
        }
        for (Element child : e.getChildren()) {
            parseElement(child);
        }
    }
    
    @Test
    public void testBuildIndex() throws JDOMException, IOException {
        XmlFile xmlFile = new XmlFile();
        Assert.assertNotNull(xmlFile);
        Element e = xmlFile.rootFromName(FileUtil.getProgramPath() + "help/en/JmriHelp_enTOC.xml");
        Assert.assertNotNull(e);
        parseElement(e);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
