package jmri.jmrix.mqtt.logixng.configurexml;

import jmri.jmrit.logixng.actions.IfThenElse;

import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.jmrit.logixng.expressions.And;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.jmrix.mqtt.logixng.Publish;
import jmri.jmrix.mqtt.logixng.Subscribe;
import jmri.util.*;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Creates a LogixNG with all actions and expressions to test store and load.
 * <P>
 * It uses the Base.printTree(PrintWriter writer, String indent) method to
 * compare the LogixNGs before and after store and load.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class StoreAndLoadTest {

    private MqttSystemConnectionMemo memo1;
//    private MqttSystemConnectionMemo memo2;

    @DisabledIfSystemProperty(named ="java.awt.headless", matches = "true")
    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        ConditionalNG_Manager conditionalNGManager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);


        LogixNG logixNG = logixNG_Manager.createLogixNG("A logixNG");
        ConditionalNG conditionalNG =
                conditionalNGManager.createConditionalNG(logixNG, "A conditionalNG");
        logixNG.setEnabled(false);
        conditionalNG.setEnabled(true);

        FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();

        IfThenElse ifThenElse = new IfThenElse(digitalActionManager.getAutoSystemName(), null);
        MaleSocket maleSocket = digitalActionManager.registerAction(ifThenElse);
        femaleSocket.connect(maleSocket);

        And and = new And(digitalExpressionManager.getAutoSystemName(), null);
        and.setComment("A comment");
        maleSocket = digitalExpressionManager.registerExpression(and);
        ifThenElse.getChild(0).connect(maleSocket);

        DigitalMany many = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        many.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(many);
        ifThenElse.getChild(1).connect(maleSocket);

        Publish publish = new Publish(digitalActionManager.getAutoSystemName(), null, memo1);
        publish.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(publish);
        many.getChild(0).connect(maleSocket);

        publish = new Publish(digitalActionManager.getAutoSystemName(), null, memo1);
        publish.setComment("A comment");
        publish.getSelectTopic().setAddressing(NamedBeanAddressing.Direct);
        publish.getSelectTopic().setValue("TheTopic");
        publish.getSelectMessage().setAddressing(NamedBeanAddressing.Direct);
        publish.getSelectMessage().setValue("TheMessage");
        maleSocket = digitalActionManager.registerAction(publish);
        many.getChild(1).connect(maleSocket);

        Subscribe subscribe = new Subscribe(digitalActionManager.getAutoSystemName(), null, memo1);
        subscribe.setComment("A comment");
        maleSocket = digitalActionManager.registerAction(subscribe);
        many.getChild(2).connect(maleSocket);

        subscribe = new Subscribe(digitalActionManager.getAutoSystemName(), null, memo1);
        subscribe.setComment("A comment");
        subscribe.setSubscribeToTopic("TheTopic");
        subscribe.setLastTopicLocalVariable("TopicVariable");
        subscribe.setLastMessageLocalVariable("MessageVariable");
        maleSocket = digitalActionManager.registerAction(subscribe);
        many.getChild(3).connect(maleSocket);

/*
        if (1==1) {
            final String treeIndent = "   ";
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent);

            System.out.println("--------------------------------------------");
            System.out.println("The current tree:");
            System.out.println("XXX"+stringWriter.toString()+"XXX");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");
            System.out.println("--------------------------------------------");

            log.error("--------------------------------------------");
            log.error("The current tree:");
            log.error("XXX"+stringWriter.toString()+"XXX");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
            log.error("--------------------------------------------");
//            return;
        }
*/



        // Store panels
        jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Unable to get default configure manager");
        } else {
            FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
            File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG_temp.xml");
            File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "LogixNG.xml");
            log.info("Temporary first file: %s%n", firstFile.getAbsoluteFile());
            log.info("Temporary second file: %s%n", secondFile.getAbsoluteFile());

            final String treeIndent = "   ";
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent, new MutableInt(0));
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

            java.util.Set<LogixNG> logixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
            for (LogixNG aLogixNG : logixNG_Set) {
                logixNG_Manager.deleteLogixNG(aLogixNG);
            }

            java.util.Set<ConditionalNG> conditionalNGSet = new java.util.HashSet<>(conditionalNGManager.getNamedBeanSet());
            for (ConditionalNG aConditionalNG : conditionalNGSet) {
                conditionalNGManager.deleteConditionalNG(aConditionalNG);
            }

            java.util.Set<MaleDigitalActionSocket> digitalActionSet = new java.util.HashSet<>(digitalActionManager.getNamedBeanSet());
            for (MaleDigitalActionSocket aDigitalActionSocket : digitalActionSet) {
                digitalActionManager.deleteDigitalAction(aDigitalActionSocket);
            }

            java.util.Set<MaleDigitalExpressionSocket> digitalExpressionSet = new java.util.HashSet<>(digitalExpressionManager.getNamedBeanSet());
            for (MaleDigitalExpressionSocket aDigitalExpression : digitalExpressionSet) {
                digitalExpressionManager.deleteDigitalExpression(aDigitalExpression);
            }

            Assert.assertEquals(0, logixNG_Manager.getNamedBeanSet().size());
            Assert.assertEquals(0, conditionalNGManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalActionManager.getNamedBeanSet().size());
            Assert.assertEquals(0, digitalExpressionManager.getNamedBeanSet().size());

            LogixNG_Thread.stopAllLogixNGThreads();
            LogixNG_Thread.assertLogixNGThreadNotRunning();


            //**********************************
            // Try to load file
            //**********************************

            java.util.Set<ConditionalNG> conditionalNG_Set =
                    new java.util.HashSet<>(conditionalNGManager.getNamedBeanSet());
            for (ConditionalNG aConditionalNG : conditionalNG_Set) {
                conditionalNGManager.deleteConditionalNG(aConditionalNG);
            }
            java.util.SortedSet<MaleDigitalActionSocket> set3 = digitalActionManager.getNamedBeanSet();
            List<MaleSocket> l = new ArrayList<>(set3);
            for (MaleSocket x3 : l) {
                digitalActionManager.deleteBean((MaleDigitalActionSocket)x3, "DoDelete");
            }
            java.util.SortedSet<MaleDigitalExpressionSocket> set4 = digitalExpressionManager.getNamedBeanSet();
            l = new ArrayList<>(set4);
            for (MaleSocket x4 : l) {
                digitalExpressionManager.deleteBean((MaleDigitalExpressionSocket)x4, "DoDelete");
            }

            results = cm.load(secondFile);
            log.debug(results ? "load was successful" : "store failed");
            if (results) {
                logixNG_Manager.setupAllLogixNGs();

                stringWriter = new StringWriter();
                printWriter = new PrintWriter(stringWriter);
                logixNG_Manager.printTree(Locale.ENGLISH, printWriter, treeIndent, new MutableInt(0));

                if (!originalTree.equals(stringWriter.toString())) {
                    log.error("--------------------------------------------");
                    log.error("Old tree:");
                    log.error("XXX"+originalTree+"XXX");
                    log.error("--------------------------------------------");
                    log.error("New tree:");
                    log.error("XXX"+stringWriter.toString()+"XXX");
                    log.error("--------------------------------------------");
/*
                    System.out.println("--------------------------------------------");
                    System.out.println("Old tree:");
                    System.out.println("XXX"+originalTree+"XXX");
                    System.out.println("--------------------------------------------");
                    System.out.println("New tree:");
                    System.out.println("XXX"+stringWriter.toString()+"XXX");
                    System.out.println("--------------------------------------------");
*/
//                    log.error(conditionalNGManager.getBySystemName(originalTree).getChild(0).getConnectedSocket().getSystemName());

                    Assert.fail("tree has changed");
//                    throw new RuntimeException("tree has changed");
                }
            } else {
                Assert.fail("Failed to load panel");
//                throw new RuntimeException("Failed to load panel");
            }
        }
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
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();

        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();

        // The class under test uses MQTT connections that it pulls from the InstanceManager.
        memo1 = new MqttSystemConnectionMemo();
//        jmri.InstanceManager.store(memo1, jmri.jmrix.mqtt.MqttSystemConnectionMemo.class);

        // The class under test uses MQTT connections that it pulls from the InstanceManager.
//        memo2 = new MqttSystemConnectionMemo();
//        jmri.InstanceManager.store(memo2, MqttSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        // JUnitAppender.clearBacklog();    // REMOVE THIS!!!

//        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
//        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");
//        JUnitUtil.removeMatchingThreads("LocoNetThrottledTransmitter");

        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
//        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreAndLoadTest.class);

}
