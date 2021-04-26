package jmri.util.prefs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jmri.profile.AuxiliaryConfiguration;
import jmri.profile.Profile;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JmriConfigurationProviderTest {

    private Path workspace;
    private Document document;

    @BeforeEach
    public void setUp() throws IOException, ParserConfigurationException {
        JUnitUtil.setUp();
        this.workspace = Files.createTempDirectory(this.getClass().getSimpleName());
        this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @AfterEach
    public void tearDown() {
        FileUtil.delete(this.workspace.toFile());
        JUnitUtil.tearDown();
    }

    /**
     * Test of findProvider method, of class JmriConfigurationProvider.
     *
     * @param info test information
     * @throws java.io.IOException
     */
    @Test
    public void testFindProvider(TestInfo info) throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile p = new Profile(info.getTestMethod().get().getName(), id, new File(this.workspace.toFile(), id));
        JmriConfigurationProvider config = JmriConfigurationProvider.findProvider(p);
        Assert.assertNotNull(config);
        FileUtil.delete(p.getPath());
    }

    /**
     * Test of getConfiguration method, of class JmriConfigurationProvider.
     *
     * @param info test information
     * @throws java.io.IOException
     */
    @Test
    public void testGetConfiguration(TestInfo info) throws IOException {
        String id = Long.toString((new Date()).getTime());
        Profile project = new Profile(info.getTestMethod().get().getName(), id, new File(this.workspace.toFile(), id));
        AuxiliaryConfiguration config = JmriConfigurationProvider.getConfiguration(project);
        Assert.assertNotNull(config);
        FileUtil.delete(project.getPath());
    }

    @Test
    public void testGetConfigurationFragment(TestInfo info) throws IOException {
        String id = Long.toString((new Date()).getTime());
        String elementName = "test:testElement";
        String namespace = "test";
        Profile project = new Profile(info.getTestMethod().get().getName(), id, new File(this.workspace.toFile(), id));
        AuxiliaryConfiguration config = JmriConfigurationProvider.getConfiguration(project);
        Element e = config.getConfigurationFragment(elementName, namespace, true);
        Assert.assertNull(e);
        e = document.createElementNS(namespace, elementName);
        config.putConfigurationFragment(e, true);
        FileUtil.delete(project.getPath());
    }
}
