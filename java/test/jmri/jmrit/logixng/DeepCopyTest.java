package jmri.jmrit.logixng;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.Base.PrintTreeSettings;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.*;

/**
 * Do a deep copy of all the LogixNG actions and expressions.
 */
public class DeepCopyTest {

    public final String TREE_INDENT = "     ";

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

    private String getItem(MaleSocket baseMaleSocket, String systemName) {
        AtomicReference<Base> ar = new AtomicReference<>();
        baseMaleSocket.forEntireTree((Base b) -> {
            if (b.getSystemName().equals(systemName)) {
                ar.set(b);
            }
        });

        Base base = ar.get();
        while (base instanceof MaleSocket) {
            base = ((MaleSocket)base).getObject();
        }
        return base.getClass().getName();
    }

    private void testCopySocket(MaleSocket baseMaleSocket) throws JmriException {
        Map<String, String> systemNames = new HashMap<>();
        Map<String, String> userNames = new HashMap<>();
        Map<String, String> comments = new HashMap<>();

        // The copy is not a male socket so it will not get the local variables
        baseMaleSocket.clearLocalVariables();

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));

        Base base = baseMaleSocket.getObject();
        while (base instanceof MaleSocket) {
            base = ((MaleSocket)base).getObject();
        }
        Base copy = base.getDeepCopy(systemNames, userNames);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        Assert.assertTrue(copy != null);

        copy.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));


        // Test that we can give the copied items new system names and user names

        List<Base> originalList = new ArrayList<>();
        baseMaleSocket.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                b.setComment(RandomStringUtils.randomAlphabetic(10));

                originalList.add(b);

                // A system name with a dollar sign after the sub system prefix
                // can have any character after the dollar sign.
                String newSystemName =
                        ((MaleSocket)b).getManager()
                                .getSubSystemNamePrefix() + "$" + RandomStringUtils.randomAlphabetic(10);
                String newUserName = RandomStringUtils.randomAlphabetic(20);

                systemNames.put(b.getSystemName(), newSystemName);
                userNames.put(b.getSystemName(), newUserName);
                comments.put(b.getSystemName(), b.getComment());
            }
        });

        copy = base.getDeepCopy(systemNames, userNames);

        List<Base> copyList = new ArrayList<>();
        copy.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                copyList.add(b);
            }
        });

        for (int i=0; i < originalList.size(); i++) {
            Assert.assertEquals(copyList.get(i).getSystemName(),
                    systemNames.get(originalList.get(i).getSystemName()));

            if (!copyList.get(i).getUserName().equals(userNames.get(originalList.get(i).getSystemName()))) {
                Assert.fail(String.format("Username is wrong for item %s",
                        getItem(baseMaleSocket, originalList.get(i).getSystemName())));
            }
            Assert.assertEquals(copyList.get(i).getUserName(),
                    userNames.get(originalList.get(i).getSystemName()));

            String origComment = comments.get(originalList.get(i).getSystemName());
            String copyComment = copyList.get(i).getComment();
            if ( (origComment != null && copyComment == null)
                    || (origComment == null && copyComment != null)
                    || (origComment != null && copyComment != null && !origComment.equals(copyComment))
                    ) {
                Assert.fail(String.format("Comment is wrong for item %s",
                        getItem(baseMaleSocket, originalList.get(i).getSystemName())));
            }
            Assert.assertEquals(copyList.get(i).getComment(),
                    comments.get(originalList.get(i).getSystemName()));
        }
    }

    @Test
    public void testGetDeepCopy() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        createLogixNGTreeScaffold.createLogixNGTree();

        java.util.Set<LogixNG> newLogixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
        for (LogixNG aLogixNG : newLogixNG_Set) {
            for (int i=0; i < aLogixNG.getNumConditionalNGs(); i++) {
                FemaleSocket femaleSocket = aLogixNG.getConditionalNG(i).getFemaleSocket();

                if (femaleSocket.isConnected()) {
                    femaleSocket.getConnectedSocket().forEntireTreeWithException((Base b) -> {
                        if (b instanceof MaleSocket) {
                            testCopySocket((MaleSocket) b);
                        }
                    });
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
