package jmri.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for ConfigXmlManager.
 * <p>
 * Uses the local preferences for test files.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class ConfigXmlManagerTest {

    private boolean innerFlag;

    @Test
    public void testRegisterOK() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager();

        Object o1 = new jmri.implementation.TripleTurnoutSignalHead("", "", null, null, null);
        configxmlmanager.registerConfig(o1);
        assertEquals(1, configxmlmanager.clist.size(), "stored in clist");
        configxmlmanager.deregister(o1);
        assertTrue(configxmlmanager.clist.isEmpty(), "removed from clist");
    }

    @Test
    public void testLogErrorOnStore() {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager();
        innerFlag = false;
        ConfigXmlManager.setErrorHandler(new ErrorHandler() {
            @Override
            public void handle(ErrorMemo e) {
                innerFlag = true;
            }
        });

        Object o1 = new jmri.ConfigXmlHandle();
        configxmlmanager.registerUser(o1);

        assertFalse(
            configxmlmanager.storeUser(new File(FileUtil.getUserFilesPath(), "none")));

        assertTrue(innerFlag, "the handler was invoked");
    }

    @Test
    public void testFind() throws ClassNotFoundException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager();
        Object o1 = new jmri.implementation.TripleTurnoutSignalHead("SH1", "", null, null, null);
        Object o2 = new jmri.implementation.TripleTurnoutSignalHead("SH2", "", null, null, null);
        Object o3 = new jmri.implementation.TripleTurnoutSignalHead("SH3", "", null, null, null);
        innerFlag = false;
        configxmlmanager.registerConfig(o1, jmri.Manager.SIGNALHEADS);
        assertSame(o1, configxmlmanager.findInstance(o1.getClass(), 0), "find found it");
        assertNull(configxmlmanager.findInstance(o1.getClass(), 1), "find only one so far");
        configxmlmanager.deregister(o1);
        assertNull(configxmlmanager.findInstance(o1.getClass(), 0), "find none");
        configxmlmanager.registerConfig(o1, jmri.Manager.SIGNALHEADS);
        configxmlmanager.registerConfig(o2, jmri.Manager.SIGNALHEADS);
        configxmlmanager.registerConfig(o3, jmri.Manager.SIGNALHEADS);
        Object ot = configxmlmanager.findInstance(o1.getClass(), 1);
        assertNotNull(ot, "findInstance(class, 1) not null");
        assertEquals(o2, ot, "findInstance(class, 1) equals o2");
        assertSame(o2, configxmlmanager.findInstance(o1.getClass(), 1), "find found 2nd");
        assertSame(o2, configxmlmanager.findInstance(Class.forName("jmri.SignalHead"), 1),
            "find found subclass");

    }

    @Test
    @Disabled("Test requires further development")
    public void testDeregister() {
    }

    @Test
    public void testAdapterName() {
        //ConfigXmlManager c = new ConfigXmlManager();
        assertEquals("java.lang.configurexml.StringXml",
            ConfigXmlManager.adapterName(""), "String class adapter");
    }

    @Test
    public void testCurrentClassName() {
        assertEquals( "jmri.managers.configurexml.DccSignalHeadXml",
                ConfigXmlManager.currentClassName("jmri.managers.configurexml.DccSignalHeadXml"),
                "unmigrated");
        assertEquals("jmri.managers.configurexml.DccSignalHeadXml",
                ConfigXmlManager.currentClassName("jmri.configurexml.DccSignalHeadXml"),
                "migrated");
    }

    @Test
    public void testFindFile() throws FileNotFoundException, IOException {
        ConfigXmlManager configxmlmanager = new ConfigXmlManager() {
            @Override
            void locateClassFailed(Throwable ex, String adapterName, Object o) {
                innerFlag = true;
            }

            @Override
            void locateFileFailed(String f) {
                // suppress warning during testing
            }
        };
        URL result = configxmlmanager.find("foo.biff");
        assertNull(result, "dont find foo.biff");

        // make sure no test file exists in "layout"
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "layout");
        File f = new File(FileUtil.getUserFilesPath() + "layout" + File.separator + "testConfigXmlManagerTest.xml");
        if (f.exists()) {
            assertTrue(f.delete()); // remove it if its there
        }

        // if file is at top level, remove that too
        f = new File("testConfigXmlManagerTest.xml");
        if (f.exists()) {
            assertTrue(f.delete());
        }

        // check for not found if doesn't exist
        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        assertNull(result, "should not find testConfigXmlManagerTest.xml");

        // put file back and find
        PrintStream p = new PrintStream(new FileOutputStream(f));
        p.println("stuff"); // load a new one
        p.close();

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        assertNotNull(result, "should find testConfigXmlManagerTest.xml");
        assertTrue(f.delete(), "file deleted 146");  // make sure it's gone again

        // check file in the current app dir
        f = new File("testConfigXmlManagerTest.xml");
        assertFalse(f.delete(), "file NOT deleted as already deleted");  // remove it if its there
        p = new PrintStream(new FileOutputStream(f));
        p.println("stuff"); // load a new one
        p.close();

        result = configxmlmanager.find("testConfigXmlManagerTest.xml");
        assertNotNull(result, "should find testConfigXmlManagerTest.xml in app dir");
        assertTrue(f.delete());  // make sure it's gone again
    }

    @BeforeEach
    public void setUp(@TempDir Path tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
