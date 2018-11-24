package jmri.jmrit.decoderdefn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import jmri.jmrit.XmlFile;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks for duplicate Family-Model pairs in decoder files.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 */
public class DuplicateTest {

    @Test
    public void testForDuplicateModels() throws JDOMException, IOException {
        File dir = new File("xml/decoders/");
        File[] files = dir.listFiles();
        boolean failed = false;
        for (File file : files) {
            if (file.getName().endsWith("xml")) {
                failed = check(file) || failed;
            }
        }
        log.debug("checked total of{}", models.size());
        if (failed) {
            Assert.fail("test failed, see System.err");
        }
    }

    ArrayList<String> models = new ArrayList<>();

    boolean check(File file) throws JDOMException, IOException {
        Element root = readFile(file);

        // check to see if there's a decoder element
        if (root.getChild("decoder") == null) {
            log.warn("Does not appear to be a decoder file");
            return false;
        }

        String family = root.getChild("decoder").getChild("family").getAttributeValue("name") + "][";
        Iterator<Element> iter = root.getChild("decoder").getChild("family")
                .getDescendants(new ElementFilter("model"));

        boolean failed = false;
        while (iter.hasNext()) {
            String model = iter.next().getAttributeValue("model");
            if (models.contains(family + model)) {
                System.err.println("found duplicate for " + family + model);
                failed = true;
            }
            models.add(family + model);
        }
        return failed;
    }

    Element readFile(File file) throws JDOMException, IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromFile(file);

    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();

    }

    private final static Logger log = LoggerFactory.getLogger(DuplicateTest.class);
}
