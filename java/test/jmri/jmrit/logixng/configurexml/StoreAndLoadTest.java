package jmri.jmrit.logixng.configurexml;


import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.*;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.*;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 */
public class StoreAndLoadTest {

    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        CreateLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        // Store panels
        jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Unable to get default configure manager");
        } else {
            PrintTreeSettings printTreeSettings = new PrintTreeSettings();
            printTreeSettings._printDisplayName = true;

            FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
            File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG_temp.xml");
            File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG.xml");
            log.info("Temporary first file: %s%n", firstFile.getAbsoluteFile());
            log.info("Temporary second file: %s%n", secondFile.getAbsoluteFile());

            final String treeIndent = "   ";
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logixNG_Manager.printTree(
                    printTreeSettings,
                    Locale.ENGLISH,
                    printWriter,
                    treeIndent,
                    new MutableInt(0));
            final String originalTree = stringWriter.toString();

            boolean results = cm.storeUser(firstFile);
            log.debug(results ? "store was successful" : "store failed");
            if (!results) {
                log.error("Failed to store panel");
                throw new RuntimeException("Failed to store panel");
            }

            // Add the header comment to the xml file
            addHeader(firstFile, secondFile);


            //**********************************
            // Delete all the LogixNGs, ConditionalNGs, and so on before reading the file.
            //**********************************
            CreateLogixNGTreeScaffold.cleanup();

            LogixNG_Thread.stopAllLogixNGThreads();
            LogixNG_Thread.assertLogixNGThreadNotRunning();

/*
            audioManager.cleanup();
            JUnitUtil.waitFor(()->{return !audioManager.isInitialised();});

            audioManager = new jmri.jmrit.audio.DefaultAudioManager(
                    InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class));
            audioManager.init();
            JUnitUtil.waitFor(()->{return audioManager.isInitialised();});
*/

            //**********************************
            // Try to load file
            //**********************************

            results = cm.load(secondFile);
            log.debug(results ? "load was successful" : "store failed");
            if (results) {
                logixNG_Manager.setupAllLogixNGs();

                stringWriter = new StringWriter();
                printWriter = new PrintWriter(stringWriter);
                logixNG_Manager.printTree(
                        printTreeSettings,
                        Locale.ENGLISH,
                        printWriter,
                        treeIndent,
                        new MutableInt(0));

                String newTree = stringWriter.toString();
                if (!originalTree.equals(newTree)) {
                    log.error("--------------------------------------------");
                    log.error("Old tree:");
                    log.error("XXX"+originalTree+"XXX");
                    log.error("--------------------------------------------");
                    log.error("New tree:");
                    log.error("XXX"+stringWriter.toString()+"XXX");
                    log.error("--------------------------------------------");

                    System.out.println("--------------------------------------------");
                    System.out.println("Old tree:");
                    System.out.println("XXX"+originalTree+"XXX");
                    System.out.println("--------------------------------------------");
                    System.out.println("New tree:");
                    System.out.println("XXX"+stringWriter.toString()+"XXX");
                    System.out.println("--------------------------------------------");

//                    log.error(conditionalNGManager.getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());

                    String[] originalTreeLines = originalTree.split(System.lineSeparator());
                    String[] newTreeLines = newTree.split(System.lineSeparator());
                    int line=0;
                    for (; line < Math.min(originalTreeLines.length, newTreeLines.length); line++) {
                        if (!originalTreeLines[line].equals(newTreeLines[line])) {
                            System.out.format("Tree differs on line %d:%nOrig: %s%n New: %s%n", line+1, originalTreeLines[line], newTreeLines[line]);
                            break;
                        }
                    }
                    System.out.println("The tree has changed. The tree differs on line "+Integer.toString(line+1));
                    Assert.fail("The tree has changed. The tree differs on line "+Integer.toString(line+1));
//                    throw new RuntimeException("tree has changed");
                }
            } else {
                Assert.fail("Failed to load panel");
//                throw new RuntimeException("Failed to load panel");
            }
        }


//        for (LoggingEvent evt : JUnitAppender.getBacklog()) {
//            System.out.format("Log: %s, %s%n", evt.getLevel(), evt.getMessage());
//        }


        JUnitAppender.assertErrorMessage("systemName is already registered: IH1");
        JUnitAppender.assertErrorMessage("systemName is already registered: IH2");
    }


    private void addHeader(File inFile, File outFile) throws FileNotFoundException, IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8)))) {

            String line = reader.readLine();
            writer.println(line);

            writer.println("<!--");
            writer.println("*****************************************************************************");
            writer.println();
            writer.println("DO NOT EDIT THIS FILE!!!");
            writer.println();
            writer.println("This file is created by jmri.jmrit.logixng.configurexml.StoreAndLoadTest");
            writer.println("and put in the temp/temp folder. LogixNG uses both the standard JMRI load");
            writer.println("and store test, and a LogixNG specific store and load test.");
            writer.println();
            writer.println("After adding new stuff to StoreAndLoadTest, copy the file temp/temp/LogixNG.xml");
            writer.println("to the folder java/test/jmri/jmrit/logixng/configurexml/load");
            writer.println();
            writer.println("******************************************************************************");
            writer.println("-->");

            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        }
    }

    @Before
    public void setUp() {
        CreateLogixNGTreeScaffold.setUp();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        CreateLogixNGTreeScaffold.tearDown();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
