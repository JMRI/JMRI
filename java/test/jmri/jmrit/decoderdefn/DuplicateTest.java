// DuplicateTest.java

package jmri.jmrit.decoderdefn;

import org.apache.log4j.Logger;

import junit.framework.*;

import java.util.*;
import java.io.*;

import org.jdom.*;
import org.jdom.filter.*;

import jmri.jmrit.XmlFile;

/**
 * Checks for duplicate Family-Model pairs in decoder files
 * 
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 * @version $Revision$
 */
public class DuplicateTest extends TestCase {

    public void testForDuplicateModels() throws JDOMException, IOException {
        File dir = new File("xml/decoders/");
        File[] files = dir.listFiles();
        boolean failed = false;
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith("xml")) {
                failed = check(files[i]) || failed;
            }
        }
        System.out.println("checked total of "+models.size());
        if (failed) Assert.fail("test failed, see System.err");
    }
    
    ArrayList<String> models = new ArrayList<String>();
    
    @SuppressWarnings("unchecked")
    boolean check(File file) throws JDOMException, IOException {
        Element root = readFile(file);
        
        // check to see if there's a decoder element
        if (root.getChild("decoder")==null) {
            log.warn("Does not appear to be a decoder file");
            return false;
        }

        String family = root.getChild("decoder").getChild("family").getAttributeValue("name")+"][";
        Iterator<Element> iter = root.getChild("decoder").getChild("family")
                                    .getDescendants(new ElementFilter("model"));

        boolean failed = false;
        while (iter.hasNext()) {
            String model = iter.next().getAttributeValue("model");
            if (models.contains(family+model)) {
                System.err.println("found duplicate for "+family+model);
                failed = true;
            }
            models.add(family+model);
        }
        return failed;
    }
    
    Element readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromFile(file);
        
    }
    
    // from here down is testing infrastructure

    public DuplicateTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", DuplicateTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DuplicateTest.class);
        return suite;
    }

    static Logger log = Logger.getLogger(DuplicateTest.class.getName());
}
