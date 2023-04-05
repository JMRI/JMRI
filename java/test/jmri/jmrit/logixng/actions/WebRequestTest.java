package jmri.jmrit.logixng.actions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import jmri.*;
import static jmri.configurexml.StoreAndCompare.checkFile;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionLight;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.LineEnding;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.FileUtil;
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

    private static final String WEB_REQUEST_URL = "https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php";

    private LogixNG _logixNG;
    private ConditionalNG _conditionalNG;
    private GlobalVariable _responseCodeVariable;
    private GlobalVariable _replyVariable;
    private GlobalVariable _cookiesVariable;


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
                "   ConditionalNG: Throw turnouts%n" +
                "      ! A%n" +
                "         For each value, set variable \"bean\" and execute action A. Values from Turnouts ::: Use default%n" +
                "            ::: Local variable \"bean\", init to None \"null\"%n" +
                "            ::: Local variable \"turnout\", init to None \"null\"%n" +
                "            ! A%n" +
                "               Listen on the bean in the local variable \"bean\" of type Turnout ::: Use default%n" +
                "                  ! Execute%n" +
                "                     Web request for %s ::: Use default%n" +
                "                        ! Execute%n" +
                "                           Many ::: Use default%n" +
                "                              ! A1%n" +
                "                                 Log local variables ::: Use default%n" +
                "                              ! A2%n" +
                "                                 Socket not connected%n" +
                "   ConditionalNG: Test cookies%n" +
                "      ! A%n" +
                "         For each value, set variable \"bean\" and execute action A. Values from Sensors ::: Use default%n" +
                "            ::: Local variable \"bean\", init to None \"null\"%n" +
                "            ::: Local variable \"sensor\", init to None \"null\"%n" +
                "            ! A%n" +
                "               Listen on the bean in the local variable \"bean\" of type Sensor ::: Use default%n" +
                "                  ! Execute%n" +
                "                     Web request for %s ::: Use default%n" +
                "                        ! Execute%n" +
                "                           Many ::: Use default%n" +
                "                              ! A1%n" +
                "                                 Log local variables ::: Use default%n" +
                "                              ! A2%n" +
                "                                 Socket not connected%n" +
                "   ConditionalNG: Test post request%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Light TestPostRequestLight is On ::: Use default%n" +
                "            ! Then%n" +
                "               Web request for %s ::: Use default%n" +
                "                  ! Execute%n" +
                "                     Many ::: Use default%n" +
                "                        ! A1%n" +
                "                           Log local variables ::: Use default%n" +
                "                        ! A2%n" +
                "                           Socket not connected%n" +
                "            ! Else%n" +
                "               Socket not connected%n",
                WEB_REQUEST_URL,
                WEB_REQUEST_URL,
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
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Chicago32");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "MiamiWest");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT3", "TorontoFirst");

        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "Throw turnouts");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _logixNG.addConditionalNG(_conditionalNG);
        _conditionalNG.setRunDelayed(false);
        _conditionalNG.setEnabled(true);

        ForEach forEach = new ForEach("IQDA101", null);
        forEach.setLocalVariableName("bean");
        forEach.setUseCommonSource(true);
        forEach.setCommonManager(CommonManager.Turnouts);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(forEach);
        maleSocket.addLocalVariable("bean", SymbolTable.InitialValueType.None, null);
        maleSocket.addLocalVariable("turnout", SymbolTable.InitialValueType.None, null);
        _conditionalNG.getChild(0).connect(maleSocket);

        ActionListenOnBeansLocalVariable listenOnBeans = new ActionListenOnBeansLocalVariable("IQDA102", null);
        listenOnBeans.setNamedBeanType(NamedBeanType.Turnout);
        listenOnBeans.setLocalVariableBeanToListenOn("bean");
        listenOnBeans.setLocalVariableNamedBean("turnout");
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        forEach.getChild(0).connect(maleSocket);

        WebRequest webRequest = new WebRequest("IQDA103", null);
        webRequest.getSelectUrl().setValue(WEB_REQUEST_URL);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest.getParameters().add(new WebRequest.Parameter("action", SymbolTable.InitialValueType.String, "throw"));
        webRequest.getParameters().add(new WebRequest.Parameter("turnout", SymbolTable.InitialValueType.LocalVariable, "turnout"));
        webRequest.setLocalVariableForResponseCode("responseCode");
        webRequest.setLocalVariableForReplyContent("reply");
        webRequest.getSelectReplyType().setEnum(WebRequest.ReplyType.String);
//        webRequest.getSelectLineEnding().setEnum(LineEnding.MacLinuxLf);
        webRequest.getSelectLineEnding().setEnum(LineEnding.Space);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        listenOnBeans.getChild(0).connect(maleSocket);
        // These are used by super class for its testing
        _base = webRequest;
        _baseMaleSocket = maleSocket;

        DigitalMany many = new DigitalMany("IQDA104", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        webRequest.getChild(0).connect(maleSocket);

        LogLocalVariables log = new LogLocalVariables("IQDA105", null);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(log);
        many.getChild(0).connect(maleSocket);
    }

    @Test
    public void testCookies() throws JmriException {
        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        _cookiesVariable.setValue(null);
        InstanceManager.getDefault(SensorManager.class).getByUserName("Green").setState(Sensor.ACTIVE);
//        InstanceManager.getDefault(TurnoutManager.class).getByUserName("Chicago32").setState(Turnout.THROWN);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Cookie Green is set. Cookies from client: ", _replyVariable.getValue());
        String cookies = _cookiesVariable.getValue().toString();
        cookies = cookies.replaceAll("expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d", "expires=???, ??-???-???? ??:??:??");
        Assert.assertEquals("{Green=Green=GreenGreen%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000}", cookies);

        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(SensorManager.class).getByUserName("Yellow").setState(Sensor.ACTIVE);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Cookie Yellow is set. Cookies from client: Green=GreenGreen!", _replyVariable.getValue());
        cookies = _cookiesVariable.getValue().toString();
        cookies = cookies.replaceAll("expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d", "expires=???, ??-???-???? ??:??:??");
        Assert.assertEquals("{Yellow=Yellow=YellowYellow%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000, Green=Green=GreenGreen%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000}", cookies);

        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(SensorManager.class).getByUserName("Blue").setState(Sensor.ACTIVE);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Cookie Blue is set. Cookies from client: Yellow=YellowYellow!, Green=GreenGreen!", _replyVariable.getValue());
        cookies = _cookiesVariable.getValue().toString();
        cookies = cookies.replaceAll("expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d", "expires=???, ??-???-???? ??:??:??");
        Assert.assertEquals("{Yellow=Yellow=YellowYellow%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000, Blue=Blue=BlueBlue%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000, Green=Green=GreenGreen%21; expires=???, ??-???-???? ??:??:?? GMT; Max-Age=1296000}", cookies);
    }

    private void setupCookiesConditionalNG() throws SocketAlreadyConnectedException, ParserException {

        _cookiesVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("cookies");

        InstanceManager.getDefault(SensorManager.class).newSensor("IS1", "Blue");
        InstanceManager.getDefault(SensorManager.class).newSensor("IS2", "Green");
        InstanceManager.getDefault(SensorManager.class).newSensor("IS3", "Yellow");

        _conditionalNG = new DefaultConditionalNGScaffold("IQC2", "Test cookies");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _logixNG.addConditionalNG(_conditionalNG);
        _conditionalNG.setRunDelayed(false);
        _conditionalNG.setEnabled(true);

        ForEach forEach = new ForEach("IQDA201", null);
        forEach.setLocalVariableName("bean");
        forEach.setUseCommonSource(true);
        forEach.setCommonManager(CommonManager.Sensors);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(forEach);
        maleSocket.addLocalVariable("bean", SymbolTable.InitialValueType.None, null);
        maleSocket.addLocalVariable("sensor", SymbolTable.InitialValueType.None, null);
        _conditionalNG.getChild(0).connect(maleSocket);

        ActionListenOnBeansLocalVariable listenOnBeans = new ActionListenOnBeansLocalVariable("IQDA202", null);
        listenOnBeans.setNamedBeanType(NamedBeanType.Sensor);
        listenOnBeans.setLocalVariableBeanToListenOn("bean");
        listenOnBeans.setLocalVariableNamedBean("sensor");
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(listenOnBeans);
        forEach.getChild(0).connect(maleSocket);

        WebRequest webRequest = new WebRequest("IQDA203", null);
        webRequest.getSelectUrl().setValue(WEB_REQUEST_URL);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Get);
        webRequest.getParameters().add(new WebRequest.Parameter("action", SymbolTable.InitialValueType.String, "cookies"));
        webRequest.getParameters().add(new WebRequest.Parameter("cookie", SymbolTable.InitialValueType.LocalVariable, "sensor"));
        webRequest.setLocalVariableForResponseCode("responseCode");
        webRequest.setLocalVariableForReplyContent("reply");
        webRequest.setLocalVariableForCookies("cookies");
        webRequest.getSelectReplyType().setEnum(WebRequest.ReplyType.String);
//        webRequest.getSelectLineEnding().setEnum(LineEnding.MacLinuxLf);
        webRequest.getSelectLineEnding().setEnum(LineEnding.Space);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        listenOnBeans.getChild(0).connect(maleSocket);
        // These are used by super class for its testing
        _base = webRequest;
        _baseMaleSocket = maleSocket;

        DigitalMany many = new DigitalMany("IQDA204", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        webRequest.getChild(0).connect(maleSocket);

        LogLocalVariables log = new LogLocalVariables("IQDA205", null);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(log);
        many.getChild(0).connect(maleSocket);
    }

    @Test
    public void testPostRequest() throws JmriException {
        _responseCodeVariable.setValue(null);
        _replyVariable.setValue(null);
        InstanceManager.getDefault(LightManager.class).getByUserName("TestPostRequestLight").setState(Light.ON);
        Assert.assertEquals(200, (int)_responseCodeVariable.getValue());
        Assert.assertEquals("Logged in. First name: Green, last name: Tomato", _replyVariable.getValue());
    }

    private void setupPostRequestConditionalNG() throws SocketAlreadyConnectedException, ParserException {

        Light testPostRequestLight = InstanceManager.getDefault(LightManager.class).newLight("IL1", "TestPostRequestLight");

        _conditionalNG = new DefaultConditionalNGScaffold("IQC3", "Test post request");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(_conditionalNG);
        _logixNG.addConditionalNG(_conditionalNG);
        _conditionalNG.setRunDelayed(false);
        _conditionalNG.setEnabled(true);

        IfThenElse ifThenElse = new IfThenElse("IQDA301", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        _conditionalNG.getChild(0).connect(maleSocket);

        ExpressionLight expressionLight = new ExpressionLight("IQDE301", null);
        expressionLight.getSelectNamedBean().setNamedBean(testPostRequestLight);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setStateAddressing(NamedBeanAddressing.Direct);
        expressionLight.setBeanState(ExpressionLight.LightState.On);
        maleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
        ifThenElse.getChild(0).connect(maleSocket);

        WebRequest webRequest = new WebRequest("IQDA302", null);
        webRequest.getSelectUrl().setValue(WEB_REQUEST_URL);
        webRequest.getSelectRequestMethod().setEnum(WebRequest.RequestMethodType.Post);
        webRequest.getParameters().add(new WebRequest.Parameter("action", SymbolTable.InitialValueType.String, "login"));
        webRequest.getParameters().add(new WebRequest.Parameter("fname", SymbolTable.InitialValueType.String, "Green"));
        webRequest.getParameters().add(new WebRequest.Parameter("lname", SymbolTable.InitialValueType.String, "Tomato"));
        webRequest.setLocalVariableForResponseCode("responseCode");
        webRequest.setLocalVariableForReplyContent("reply");
        webRequest.getSelectReplyType().setEnum(WebRequest.ReplyType.String);
//        webRequest.getSelectLineEnding().setEnum(LineEnding.MacLinuxLf);
        webRequest.getSelectLineEnding().setEnum(LineEnding.Space);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(webRequest);
        ifThenElse.getChild(1).connect(maleSocket);
        // These are used by super class for its testing
        _base = webRequest;
        _baseMaleSocket = maleSocket;

        DigitalMany many = new DigitalMany("IQDA303", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(many);
        webRequest.getChild(0).connect(maleSocket);

        LogLocalVariables log = new LogLocalVariables(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(log);
        many.getChild(0).connect(maleSocket);
    }

    @Test
    public void testStoreFile() throws IOException {
        // This test only updates the xml file in the LogixNG documentation
        storeXmlFile();
    }


    private void storeXmlFile() throws IOException {

        jmri.ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm == null) {
            log.error("Unable to get default configure manager");
        } else {
            FileUtil.createDirectory(FileUtil.getUserFilesPath() + "temp");
            File firstFile = new File(FileUtil.getUserFilesPath() + "temp/" + "WebRequest_temp.xml");
            File secondFile = new File(FileUtil.getUserFilesPath() + "temp/" + "WebRequest.xml");
            log.info("Temporary first file: {}", firstFile.getAbsoluteFile());
            log.info("Temporary second file: {}", secondFile.getAbsoluteFile());
            System.out.format("Temporary first file: %s%n", firstFile.getAbsoluteFile());
            System.out.format("Temporary second file: %s%n", secondFile.getAbsoluteFile());

            boolean results = cm.storeUser(firstFile);
            log.debug(results ? "store was successful" : "store failed");
            if (!results) {
                log.error("Failed to store panel");
                throw new RuntimeException("Failed to store panel");
            }

            // Add the header comment to the xml file
            addHeader(firstFile, secondFile);




            try {
                boolean dataHasChanged = true;

                File file1 = new File(FileUtil.getProgramPath() + "help/en/html/tools/logixng/reference/WebRequestExample/" + "WebRequest.xml");

                try {
                    dataHasChanged = checkFile(file1, secondFile);
                } catch (FileNotFoundException e) {
                    // Ignore this. If this happens, just copy the new file to the documentation folder
                    System.out.format("File not found!!! %s%n", e.getMessage());
                }

                if (dataHasChanged) {
                    java.nio.file.Files.copy(secondFile.toPath(), file1.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.format("File copied from %s to %s%n", secondFile, file1);
                }

                System.out.format("File compare %s with %s resulted in: %b%n", file1, secondFile, dataHasChanged);
            } catch (Exception ex) {
                log.debug("checkFile exception: ", ex);
                throw new RuntimeException("An exception occurred", ex);
            }



        }
    }


    private void addHeader(File inFile, File outFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8)))) {

            String line = reader.readLine();
            writer.println(line);

            writer.println("<!--");
            writer.println("*****************************************************************************");
            writer.println();
            writer.println("DO NOT EDIT THIS FILE!!!");
            writer.println();
            writer.println("This file is created by jmri.jmrit.logixng.actions.WebRequestTest");
            writer.println();
            writer.println("******************************************************************************");
            writer.println("-->");

            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
        }
    }


    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, ParserException, IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
        jmri.jmrit.logixng.actions.NamedBeanType.reset();

        _category = Category.ITEM;
        _isExternal = true;

        _logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");

        _responseCodeVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("responseCode");
        _replyVariable = InstanceManager.getDefault(GlobalVariableManager.class).createGlobalVariable("reply");

        setupThrowTurnoutsConditionalNG();
        setupCookiesConditionalNG();
        setupPostRequestConditionalNG();

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


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebRequestTest.class);
}
