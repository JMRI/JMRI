/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.jmrit.catalog package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.jmrit.catalog;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.catalog");   // no tests in this class itself
        suite.addTest(CatalogTreeFSTest.suite());
        suite.addTest(CatalogTreeIndexTest.suite());
        suite.addTest(ImageIndexEditorTest.suite());        
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.catalog.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CatalogPaneTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CatalogPanelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CatalogTreeModelTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DefaultCatalogTreeManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(CatalogTreeNodeTest.class));
        return suite;
    }

}
