package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test WebRequest
 *
 * @author Daniel Bergqvist 2023
 */
public class WebRequestTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private WebRequest webRequest;
//    private Light light;


    @Test
    public void testWebRequest() throws JmriException {
        webRequest.getSelectUrl().setValue("https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php");
//        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest.getParameters().add(new WebRequest.Parameter("address", SymbolTable.InitialValueType.String, "Adenravagen 2"));
        conditionalNG.execute();
    }

    @Test
    public void testWebRequest2() throws JmriException {
        webRequest.getSelectUrl().setValue("https://www.modulsyd.se/forum_3.3/index.php");
//        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest.getParameters().add(new WebRequest.Parameter("address", SymbolTable.InitialValueType.String, "Adenravagen 2"));
        conditionalNG.execute();
    }

    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Web request for https://www.jmri.org/ ::: Use default%n" +
                "   ! Execute%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Web request for https://www.jmri.org/ ::: Use default%n" +
                "            ! Execute%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new WebRequest(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();

        _category = Category.ITEM;
        _isExternal = true;

//        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
//        light.setCommandedState(Light.OFF);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        webRequest = new WebRequest(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        webRequest.setUseThread(false);
        webRequest.getSelectUrl().setValue("https://www.jmri.org/");
//        actionWebRequest.getSelectNamedBean().setNamedBean(light);
//        actionWebRequest.getSelectEnum().setEnum(ActionLight.LightState.On);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        conditionalNG.getChild(0).connect(socket);

        _base = webRequest;
        _baseMaleSocket = socket;

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
