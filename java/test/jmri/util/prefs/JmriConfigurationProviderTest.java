package jmri.util.prefs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author rhwood
 */
public class JmriConfigurationProviderTest extends TestCase {

    private Path workspace;
    private Document document;

    public JmriConfigurationProviderTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workspace = Files.createTempDirectory(this.getName());
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileUtil.delete(this.workspace.toFile());
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriConfigurationProviderTest.class);
        return suite;
    }

    /**
     * Test of findProvider method, of class JmriConfigurationProvider.
     *
     * @throws java.io.IOException
     */
    public void testFindProvider() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile p = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        JmriConfigurationProvider config = JmriConfigurationProvider.findProvider(p);
        assertNotNull(config);
        FileUtil.delete(p.getPath());
    }

    /**
     * Test of getConfiguration method, of class JmriConfigurationProvider.
     *
     * @throws java.io.IOException
     */
    public void testGetConfiguration() throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        AuxiliaryConfiguration config = JmriConfigurationProvider.getConfiguration(project);
        assertNotNull(config);
        FileUtil.delete(project.getPath());
    }

    public void testGetConfigurationFragment() throws IOException {
        String id = Long.toString((new Date()).getTime());
        String elementName = "test:testElement";
        String namespace = "test";
        Profile project = new Profile(this.getName(), id, new File(this.workspace.toFile(), id));
        AuxiliaryConfiguration config = JmriConfigurationProvider.getConfiguration(project);
        Element e = config.getConfigurationFragment(elementName, namespace, true);
        assertNull(e);
        e = document.createElementNS(namespace, elementName);
        config.putConfigurationFragment(e, true);
        FileUtil.delete(project.getPath());
    }
}
