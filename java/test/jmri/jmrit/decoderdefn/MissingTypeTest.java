package jmri.jmrit.decoderdefn;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;

import jmri.jmrit.XmlFile;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

import org.junit.jupiter.api.*;

/**
 * Checks for missing content in a <variable> element
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 */
public class MissingTypeTest {

    @Test
    public void testForMissingTypeElement() throws JDOMException, IOException {
        File dir = new File("xml/decoders/");
        File[] files = dir.listFiles();
        boolean failed = false;
        for (File file : files) {
            if (file.getName().endsWith("xml")) {
                failed = check(file) || failed;
            }
        }
        log.debug("checked total of{}", files.length);
        if (failed) {
            Assertions.fail("test failed, see System.err");
        }
    }

    // private final ArrayList<String> models = new ArrayList<>();

    boolean check(File file) throws JDOMException, IOException {
        Element root = readFile(file);

        // check to see if there's a decoder element
        if (root.getChild("decoder") == null) {
            log.warn("Does not appear to be a decoder file");
            return false;
        }

        Iterator<Element> iter = root.getChild("decoder").getChild("variables")
                .getDescendants(new ElementFilter("variable"));

        boolean failed = false;
        while (iter.hasNext()) {
            Element e = iter.next().clone();
            
            // ignore everything _except_ the type element
            e.removeContent(new ElementFilter("label"));
            e.removeContent(new ElementFilter("qualifier"));
            e.removeContent(new ElementFilter("comment"));
            e.removeContent(new ElementFilter("tooltip"));
            e.removeContent(new ElementFilter("defaultItem"));
            
            if (e.getChildren().size() != 1) {
                failed = true;
                log.error("Test failed in file: {}", file);
                log.error("  Element's remaining children {}", e.getChildren());
                for (Attribute a: e.getAttributes()) {
                    log.error("     Attr {}", a);
                }
            }            
        }
        return failed;
    }

    Element readFile(File file) throws JDOMException, IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromFile(file);

    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MissingTypeTest.class);
}
