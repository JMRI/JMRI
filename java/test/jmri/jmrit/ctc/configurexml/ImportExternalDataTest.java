package jmri.jmrit.ctc.configurexml;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import jmri.InstanceManager;
import jmri.jmrit.ctc.CTCFiles;
import jmri.profile.NullProfile;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/*
* Test the external file import process
* @author  Dave Sand   Copyright (C) 2020
*/
public class ImportExternalDataTest {

    @Test
    public void testExternalDataImport() throws Exception    {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Load the test panel and initialize Logix and advanced block routing
        java.io.File f = new java.io.File("java/test/jmri/jmrit/ctc/configurexml/setup/CTC_Test_Import.xml");  // NOI18N
        InstanceManager.getDefault(jmri.ConfigureManager.class).load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
        InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).initializeLayoutBlockPaths();
        JUnitUtil.waitFor(5000);     // Wait for block routing and SML initialization

        createTestFiles();

        ImportExternalData.loadExternalData();
    }

    public void createTestFiles() {
        // Copy ProgramProperties
        final File source = new File("java/test/jmri/jmrit/ctc/configurexml/setup/");
        final String props = "ProgramProperties.xml";
        final String system = "CTCSystem.xml";

        File propsFile = new File(source, props);
        File systemFile = new File(source, system);
        try {
            log.debug("Copying from {} to {}", propsFile, CTCFiles.getFile(props));
            Files.copy(propsFile.toPath(), CTCFiles.getFile(props).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Copy CTC Properties demo file failed", ex);  // NOI18N
        }
        try {
            log.debug("Copying from {} to {}", systemFile, CTCFiles.getFile(system));
            Files.copy(systemFile.toPath(), CTCFiles.getFile(system).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Copy CTC System demo file failed", ex);  // NOI18N
        }
    }

    @BeforeEach
    public void setUp(@TempDir File folder) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new NullProfile(folder));
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportExternalDataTest.class);
}
