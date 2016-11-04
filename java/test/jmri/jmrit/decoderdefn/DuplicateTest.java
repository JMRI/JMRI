package jmri.jmrit.decoderdefn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import jmri.jmrit.XmlFile;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;

/**
 * Checks for duplicate Family-Model pairs in decoder files
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 */
public class DuplicateTest extends TestCase {

    public void testForDuplicateModels() throws JDOMException, IOException {
        File dir = new File("xml/decoders/");
        File[] files = dir.listFiles();
        boolean failed = false;
        for (File file : files) {
            if (file.getName().endsWith("xml")) {
                failed = check(file) || failed;
            }
        }
        System.out.println("checked total of " + models.size());
        System.out.println("skipped due to migration total of " + skipped);
        if (failed) {
            Assert.fail("test failed, see System.err");
        }
    }

    ArrayList<String> models = new ArrayList<>();
    int skipped = 0;

    @SuppressWarnings("unchecked")
    boolean check(File file) throws JDOMException, IOException {
        Element root = readFile(file);

        // check to see if there's a decoder element
        if (root.getChild("decoder") == null) {
            log.warn("Does not appear to be a decoder file");
            return false;
        }

        String family = "[" + root.getChild("decoder").getChild("family").getAttributeValue("name") + "][";
        Iterator<Element> iter = root.getChild("decoder").getChild("family")
                .getDescendants(new ElementFilter("model"));

        boolean failed = false;
        while (iter.hasNext()) {
            Element e = iter.next();
            String model = e.getAttributeValue("model") + "][";
            String productID = e.getAttributeValue("productID")+ "]";
            if (models.contains(family + model + productID)) {
                System.err.println("found duplicate for " + family + model + productID);
                failed = true;
            }
            // Check if this decoder definition has a replacement model and
            // family defined meaning that it's been migrated.
            // If so, skip adding it to the models list as this can otherwise
            // result in an erroneous duplicate
            if (e.getAttributeValue("replacementModel") != null && e.getAttributeValue("replacementFamily") != null) {
                skipped += 1;
                System.out.println("skipping due to migration " + family + model + productID);
            } else {
                models.add(family + model + productID);
            }
        }
        return failed;
    }

    Element readFile(File file) throws org.jdom2.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromFile(file);

    }

    // from here down is testing infrastructure
    public DuplicateTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DuplicateTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DuplicateTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(DuplicateTest.class.getName());
}
