package jmri.jmrit.logixng;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.*;

/**
 * Do a deep copy of all the LogixNG actions and expressions.
 */
public class DeepCopyTest {

    private CreateLogixNGTreeScaffold createLogixNGTreeScaffold;

    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        createLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        final String treeIndent = "   ";

        //**********************************
        // Test deep copy of all the LogixNGs since that we have
        // almost all actions and expressions with lots of data.
        //**********************************

        var systemNames = new HashMap<String, String>();
        var userNames = new HashMap<String, String>();

        PrintTreeSettings otherPrintTreeSettings = new PrintTreeSettings();
        otherPrintTreeSettings._printDisplayName = false;
        otherPrintTreeSettings._hideUserName = true;

        java.util.Set<LogixNG> newLogixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
        for (LogixNG aLogixNG : newLogixNG_Set) {
            for (int i=0; i < aLogixNG.getNumConditionalNGs(); i++) {
                FemaleSocket originSocket = aLogixNG.getConditionalNG(i).getFemaleSocket();

                if (originSocket.isConnected()) {
                    Base origin = originSocket.getConnectedSocket();

                    Base copy = origin.getDeepCopy(systemNames, userNames);

                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    origin.printTree(
                            otherPrintTreeSettings,
                            Locale.ENGLISH,
                            printWriter,
                            treeIndent,
                            new MutableInt(0));

                    String originStr = stringWriter.toString();

                    stringWriter = new StringWriter();
                    printWriter = new PrintWriter(stringWriter);
                    copy.printTree(
                            otherPrintTreeSettings,
                            Locale.ENGLISH,
                            printWriter,
                            treeIndent,
                            new MutableInt(0));

                    String copyStr = stringWriter.toString();

//                        System.out.format("%n%n%n------------------------------------%n%n%s%n%n------------------------------------%n%n%n", originStr);
//                        System.out.format("%n%n%n------------------------------------%n%n%s%n%n------------------------------------%n%n%n", copyStr);

                    Assert.assertEquals(originStr, copyStr);
                }
            }
        }
    }


    @Before
    public void setUp() {
        createLogixNGTreeScaffold = new CreateLogixNGTreeScaffold();
        createLogixNGTreeScaffold.setUp();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        createLogixNGTreeScaffold.tearDown();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeepCopyTest.class);

}
