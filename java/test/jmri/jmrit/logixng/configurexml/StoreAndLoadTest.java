package jmri.jmrit.logixng.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.jupiter.api.*;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 */
public class StoreAndLoadTest {

    private CreateLogixNGTreeScaffold createLogixNGTreeScaffold;

    @Test
    @DisabledIfHeadless
    public void testLogixNGs() throws PropertyVetoException, IOException, JmriException {

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        createLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        // Store panels
        jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        assertNotNull(cm, "Unable to get default configure manager");

        PrintTreeSettings printTreeSettings = new PrintTreeSettings();
        printTreeSettings._printDisplayName = true;

        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
        File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG_temp.xml");
        File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG.xml");
        log.info("Temporary first file: {}", firstFile.getAbsoluteFile());
        log.info("Temporary second file: {}", secondFile.getAbsoluteFile());

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
        assertTrue(results, "Failed to store panel");

        // Add the header comment to the xml file
        addHeader(firstFile, secondFile);


        //**********************************
        // Delete all the LogixNGs, ConditionalNGs, and so on before reading the file.
        //**********************************
        createLogixNGTreeScaffold.cleanup();

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
        assertTrue(results, "Failed to load panel");

        JUnitAppender.assertErrorMessage("systemName is already registered: IH1");
        JUnitAppender.assertErrorMessage("systemName is already registered: IH2");
        JUnitAppender.assertErrorMessage("systemName is already registered: IHTransitScaffold");
        JUnitAppender.assertWarnMessage("Warrant \"Test Warrant\" already exists.");
        JUnitAppender.assertWarnMessage("No state variables found for conditional IX1C1");

        logixNG_Manager.setupAllLogixNGs();
        logixNG_Manager.activateAllLogixNGs(false, false);

        for (SymbolTable.InitialValueType type : SymbolTable.InitialValueType.values()) {
            assertWarningsFromActivate(type);
        }

        StringWriter stringWriter2 = new StringWriter();
        printWriter = new PrintWriter(stringWriter2);
        logixNG_Manager.printTree(
                printTreeSettings,
                Locale.ENGLISH,
                printWriter,
                treeIndent,
                new MutableInt(0));

        String newTree = stringWriter2.toString();
        assertEquals( originalTree, newTree, () ->
            getCompareTreeFailText(originalTree, newTree));


        // for (LoggingEvent evt : JUnitAppender.getBacklog()) {
        // System.out.format("Log: %s, %s%n", evt.getLevel(), evt.getMessage());
    }

    public static String getCompareTreeFailText( final String originalTree, final String newTree) {
        StringBuilder toReturn = new StringBuilder();
        toReturn.append(System.lineSeparator()).append("--------------------------------------------")
        .append(System.lineSeparator()).append("Old tree:")
        .append(System.lineSeparator()).append("XXX").append(originalTree).append("XXX")
        .append(System.lineSeparator()).append("--------------------------------------------")
        .append(System.lineSeparator()).append("New tree:")
        .append(System.lineSeparator()).append("XXX").append(newTree).append("XXX")
        .append(System.lineSeparator()).append("--------------------------------------------");

        // log.error(conditionalNGManager.getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());

        String[] originalTreeLines = originalTree.split(System.lineSeparator());
        String[] newTreeLines = newTree.split(System.lineSeparator());
        int line=0;
        for (; line < Math.min(originalTreeLines.length, newTreeLines.length); line++) {
            if (!originalTreeLines[line].equals(newTreeLines[line])) {
                toReturn.append(System.lineSeparator()).append("Tree differs on line ").append(line+1).append(":")
                    .append(System.lineSeparator()).append("Orig: ").append(originalTreeLines[line])
                    .append(System.lineSeparator()).append(" New: ").append(newTreeLines[line]);
                break;
            }
        }
        toReturn.append("The tree has changed. The tree differs on line ").append(Integer.toString(line+1));
        return toReturn.toString();
    }

    private void assertWarningsFromActivate( SymbolTable.InitialValueType type ){
        switch( type){
            case None:
            case String:
            case Array:
            case Map:
            case Object:
                return;
            case LogixNG_Table:
            case LocalVariable:
            case Memory:
            case Reference:
            case Formula:
            case ScriptExpression:
            case ScriptFile:
                assertInitialUnhandled(type);
                JUnitAppender.assertWarnMessage(String.format("Variable %s%s%s could not be initialized",
                    "TestVariable_", type.name(), "_3"));
                return;
            default:
                assertInitialUnhandled(type);
        }
    }

    private void assertInitialUnhandled( @javax.annotation.Nonnull SymbolTable.InitialValueType type ){
        JUnitAppender.assertWarnMessage(String.format("Variable %s%s could not be initialized", "TestVariable_", type.name()));
        JUnitAppender.assertWarnMessage(String.format("Variable %s%s%s could not be initialized", "TestVariable_", type.name(), "_2"));
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        createLogixNGTreeScaffold = new CreateLogixNGTreeScaffold();
        createLogixNGTreeScaffold.setUpScaffold();
    }

    @AfterEach
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        createLogixNGTreeScaffold.tearDownScaffold();
        JUnitUtil.tearDown();
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
