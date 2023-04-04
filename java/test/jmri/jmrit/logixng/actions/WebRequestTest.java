package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LineEnding;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test WebRequest
 *
 * @author Daniel Bergqvist 2023
 */
public class WebRequestTest extends AbstractDigitalActionTestBase {

    private static final String WEB_REQUEST_URL = "https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php";

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;

    private GlobalVariable _responseCodeVariable;
    private GlobalVariable _replyVariable;

//    private WebRequest webRequest;
//    private Light light;

/*
//    @Ignore
    @Test
    public void testWebRequest() throws JmriException {
        webRequest.getSelectUrl().setValue("https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php");
//        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest.getParameters().add(new WebRequest.Parameter("address", SymbolTable.InitialValueType.String, "Adenravagen 2"));
        conditionalNG.execute();
    }

//    @Ignore
    @Test
    public void testWebRequest2() throws JmriException {
        MaleSocket socketWebRequest1 = conditionalNG.getFemaleSocket().getConnectedSocket();
        socketWebRequest1.addLocalVariable("cookies", SymbolTable.InitialValueType.String, "");

        conditionalNG.unregisterListeners();

        WebRequest webRequest2 = new WebRequest("IQDA322", null);
        MaleSocket socketWebRequest2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest2);
        socketWebRequest1.getChild(0).connect(socketWebRequest2);

        WebRequest webRequest3 = new WebRequest("IQDA323", null);
        MaleSocket socketWebRequest3 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest3);
        socketWebRequest2.getChild(0).connect(socketWebRequest3);

        webRequest2.setUseThread(false);
        webRequest3.setUseThread(false);

        conditionalNG.registerListeners();

        webRequest.setLocalVariableForCookies("cookies");
        webRequest2.setLocalVariableForCookies("cookies");
        webRequest3.setLocalVariableForCookies("cookies");





        webRequest.getSelectUrl().setValue("https://www.modulsyd.se/forum_3.3/index.php?aaaa=1");
        webRequest2.getSelectUrl().setValue("https://www.modulsyd.se/forum_3.3/index.php?aaaa=2");
        webRequest3.getSelectUrl().setValue("https://www.modulsyd.se/forum_3.3/index.php?aaaa=3");
//        webRequest3.getSelectUrl().setValue("https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php");


        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest2.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest3.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
/*
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest2.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest3.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest2.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest3.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest2.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
        webRequest3.getParameters().add(new WebRequest.Parameter("name", SymbolTable.InitialValueType.String, "Daniel123"));
*./
//        logixNG.setEnabled(true);
        conditionalNG.execute();

//        webRequest.setLocalVariableForCookies("cookies");
//        conditionalNG.execute();
//        conditionalNG.execute();
    }

    @Test
    public void testCharsets() {
        System.out.format("%n%n%nList of charsets%n");
        var charsets = java.nio.charset.Charset.availableCharsets();
        for (var entry : charsets.entrySet()) {
            System.out.format("Charset: %s, %s%n", entry.getKey(), entry.getValue().displayName());
        }
        System.out.format("%n%n%n");
    }
*/
    @Override
    public ConditionalNG getConditionalNG() {
        return _conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return _logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Web request for %s ::: Use default%n" +
                "   ! Execute%n" +
                "      Many ::: Use default%n" +
                "         ! A1%n" +
                "            Log local variables ::: Use default%n" +
                "         ! A2%n" +
                "            Socket not connected%n",
                WEB_REQUEST_URL);
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         For each value, set variable \"bean\" and execute action A. Values from Turnouts ::: Use default%n" +
                "            ::: Local variable \"bean\", init to None \"null\"%n" +
                "            ::: Local variable \"turnout\", init to None \"null\"%n" +
                "            ! A%n" +
                "               Listen on the bean in the local variable \"bean\" of type Light ::: Use default%n" +
                "                  ! Execute%n" +
                "                     Web request for %s ::: Use default%n" +
                "                        ! Execute%n" +
                "                           Many ::: Use default%n" +
                "                              ! A1%n" +
                "                                 Log local variables ::: Use default%n" +
                "                              ! A2%n" +
                "                                 Socket not connected%n",
                WEB_REQUEST_URL);
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new WebRequest(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testThrowTurnouts() throws JmriException {
        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(TurnoutManager.class).getByUserName("MiamiWest").setState(Turnout.THROWN);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Turnout MiamiWest is thrown", _replyVariable.getValue());

        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(TurnoutManager.class).getByUserName("Chicago32").setState(Turnout.THROWN);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Turnout Chicago32 is thrown", _replyVariable.getValue());

        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(TurnoutManager.class).getByUserName("TorontoFirst").setState(Turnout.THROWN);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Turnout TorontoFirst is thrown", _replyVariable.getValue());
    }

    private void setupThrowTurnoutsConditionalNG() throws SocketAlreadyConnectedException, ParserException {

        _responseCodeVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("responseCode");
        _replyVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("reply");

        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Chicago32");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "MiamiWest");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT3", "TorontoFirst");

        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _logixNG.addConditionalNG(_conditionalNG);
        _conditionalNG.setRunDelayed(false);
        _conditionalNG.setEnabled(true);


//        DigitalMany many = new DigitalMany("IQDA001", null);
//        MaleSocket maleSocketRoot =
//                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
//        maleSocketRoot.addLocalVariable("turnout", SymbolTable.InitialValueType.None, null);
//        conditionalNG.getChild(0).connect(maleSocketRoot);

        ForEach forEach = new ForEach("IQDA002", null);
        forEach.setLocalVariableName("bean");
        forEach.setUseCommonSource(true);
        forEach.setCommonManager(ForEach.CommonManager.Turnouts);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(forEach);
        maleSocket.addLocalVariable("bean", SymbolTable.InitialValueType.None, null);
        maleSocket.addLocalVariable("turnout", SymbolTable.InitialValueType.None, null);
        _conditionalNG.getChild(0).connect(maleSocket);

        ActionListenOnBeansLocalVariable listenOnBeans = new ActionListenOnBeansLocalVariable("IQDA321", null);
        listenOnBeans.setLocalVariableBeanToListenOn("bean");
        listenOnBeans.setLocalVariableNamedBean("turnout");
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        forEach.getChild(0).connect(maleSocket);

        WebRequest webRequest = new WebRequest(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        webRequest.setUseThread(false);
        webRequest.getSelectUrl().setValue(WEB_REQUEST_URL);
        webRequest.getParameters().add(new WebRequest.Parameter("action", SymbolTable.InitialValueType.String, "throw"));
        webRequest.getParameters().add(new WebRequest.Parameter("turnout", SymbolTable.InitialValueType.LocalVariable, "turnout"));
        webRequest.setLocalVariableForResponseCode("responseCode");
        webRequest.setLocalVariableForReplyContent("reply");
        webRequest.getSelectReplyType().setEnum(WebRequest.ReplyType.String);
//        webRequest.getSelectLineEnding().setEnum(LineEnding.MacLinuxLf);
        webRequest.getSelectLineEnding().setEnum(LineEnding.Space);
//        actionWebRequest.getSelectNamedBean().setNamedBean(light);
//        actionWebRequest.getSelectEnum().setEnum(ActionLight.LightState.On);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        listenOnBeans.getChild(0).connect(maleSocket);
        // These are used by super class for its testing
        _base = webRequest;
        _baseMaleSocket = maleSocket;

        DigitalMany many = new DigitalMany("IQDA001", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
//        maleSocket.addLocalVariable("turnout", SymbolTable.InitialValueType.None, null);
        webRequest.getChild(0).connect(maleSocket);

//        DigitalFormula formula = new DigitalFormula(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
//        formula.setFormula("reply = reply.trim()");
//        formula.setFormula("");
//        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(formula);
//        many.getChild(0).connect(maleSocket);

        LogLocalVariables log = new LogLocalVariables(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(log);
        many.getChild(0).connect(maleSocket);




/*
        IfThenElse ifThenElse = new IfThenElse("IQDA003", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        many.getChild(1).connect(maleSocket);

        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE001", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);
*/
/*
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);





        WebRequest webRequest = new WebRequest(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        webRequest.setUseThread(false);
        webRequest.getSelectUrl().setValue(WEB_REQUEST_URL);
//        actionWebRequest.getSelectNamedBean().setNamedBean(light);
//        actionWebRequest.getSelectEnum().setEnum(ActionLight.LightState.On);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        ifThenElse.getChild(1).connect(socket);



        // These are used by super class for its testing
        _base = webRequest;
        _baseMaleSocket = socket;
*/




        // If sensor1 is active
        // WebRequest

/*
        ConditionalNG
            For all turnouts
                ListenOnBeans
                    WebRequest
                        Digital formula: response = response.trim()
                        Digital formula: expectedResponse = String.format("Turnout %s is thrown", turnout.getSystemName())
                        IfThenElse
                            LocalVariable response is expectedResponse


        // Test cookies
        // Test post


*/
    }







    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, ParserException {
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
        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");






        setupThrowTurnoutsConditionalNG();
//        setupTestCookiesConditionalNG();
//        setupTestPostConditionalNG();













/*




        webRequest = new WebRequest(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        webRequest.setUseThread(false);
        webRequest.getSelectUrl().setValue("https://www.jmri.org/");
//        actionWebRequest.getSelectNamedBean().setNamedBean(light);
//        actionWebRequest.getSelectEnum().setEnum(ActionLight.LightState.On);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        conditionalNG.getChild(0).connect(socket);

        _base = webRequest;
        _baseMaleSocket = socket;

*/

        if (! _logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        _logixNG.activate();
        _logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
