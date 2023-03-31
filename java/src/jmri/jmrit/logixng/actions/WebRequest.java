package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.*;
import jmri.util.ThreadingUtil;

/**
 * This action sends a web request.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class WebRequest extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener, VetoableChangeListener {

    private static final ResourceBundle rbx =
            ResourceBundle.getBundle("jmri.jmrit.logixng.implementation.ImplementationBundle");

    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0";

    private boolean _useThread = true;

    // Note that it's valid if the url has parameters as well, like https://www.mysite.org/somepage.php?name=Jim&city=Boston
    // The parameters are the string after the question mark.
    private final LogixNG_SelectString _selectUrl =
            new LogixNG_SelectString(this, this);

    private final LogixNG_SelectEnum<RequestMethodType> _selectRequestMethod =
            new LogixNG_SelectEnum<>(this, RequestMethodType.values(), RequestMethodType.Get, this);

    private final LogixNG_SelectString _selectUserAgent =
            new LogixNG_SelectString(this, DEFAULT_USER_AGENT, this);

    private final List<Parameter> _parameters = new ArrayList<>();

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
    private String _localVariableForPostContent = "";
    private String _localVariableForResponseCode = "";
    private String _localVariableForReplyContent = "";
    private String _localVariableForCookies = "";

    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();


    public WebRequest(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _socket = InstanceManager.getDefault(DigitalActionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ShowDialog_SocketExecute"));
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames)
            throws ParserException, JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        WebRequest copy = new WebRequest(sysName, userName);
        copy.setComment(getComment());
        getSelectUrl().copy(copy._selectUrl);
        getSelectRequestMethod().copy(copy._selectRequestMethod);
        getSelectUserAgent().copy(copy._selectUserAgent);
        copy._parameters.addAll(_parameters);
//        getSelectMime().copy(copy._selectMime);
        copy.setLocalVariableForPostContent(_localVariableForPostContent);
        copy.setLocalVariableForResponseCode(_localVariableForResponseCode);
        copy.setLocalVariableForReplyContent(_localVariableForReplyContent);
        copy.setLocalVariableForCookies(_localVariableForCookies);
//        copy.setModal(_modal);
//        copy.setMultiLine(_multiLine);
//        copy.setFormat(_format);
//        copy.setFormatType(_formatType);
//        for (Data data : _dataList) {
//            copy.getDataList().add(new Data(data));
//        }
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectString getSelectUrl() {
        return _selectUrl;
    }

    public LogixNG_SelectEnum<RequestMethodType> getSelectRequestMethod() {
        return _selectRequestMethod;
    }

    public LogixNG_SelectString getSelectUserAgent() {
        return _selectUserAgent;
    }

    public List<Parameter> getParameters() {
        return _parameters;
    }

//    public LogixNG_SelectEnum<MimeType> getSelectMime() {
//        return _selectMime;
//    }

    public void setUseThread(boolean value) {
        _useThread = value;
    }

    public void setLocalVariableForPostContent(String localVariable) {
        _localVariableForPostContent = localVariable;
    }

    public String getLocalVariableForPostContent() {
        return _localVariableForPostContent;
    }

    public void setLocalVariableForResponseCode(String localVariable) {
        _localVariableForResponseCode = localVariable;
    }

    public String getLocalVariableForResponseCode() {
        return _localVariableForResponseCode;
    }

    public void setLocalVariableForReplyContent(String localVariable) {
        _localVariableForReplyContent = localVariable;
    }

    public String getLocalVariableForReplyContent() {
        return _localVariableForReplyContent;
    }

    public void setLocalVariableForCookies(String localVariable) {
        _localVariableForCookies = localVariable;
    }

    public String getLocalVariableForCookies() {
        return _localVariableForCookies;
    }
/*
    public void setFormatType(FormatType formatType) {
        _formatType = formatType;
    }

    public FormatType getFormatType() {
        return _formatType;
    }

    public void setFormat(String format) {
        _format = format;
    }

    public String getFormat() {
        return _format;
    }

    public List<Data> getDataList() {
        return _dataList;
    }
*/
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
/*
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }
/*
    private List<Object> getDataValues() throws JmriException {
        List<Object> values = new ArrayList<>();
        for (Data _data : _dataList) {
            switch (_data._dataType) {
                case LocalVariable:
                    values.add(getConditionalNG().getSymbolTable().getValue(_data._data));
                    break;

                case Memory:
                    MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
                    Memory memory = memoryManager.getMemory(_data._data);
                    if (memory == null) throw new IllegalArgumentException("Memory '" + _data._data + "' not found");
                    values.add(memory.getValue());
                    break;

                case Reference:
                    values.add(ReferenceUtil.getReference(
                            getConditionalNG().getSymbolTable(), _data._data));
                    break;

                case Formula:
                    if (_data._expressionNode != null) {
                        values.add(_data._expressionNode.calculate(getConditionalNG().getSymbolTable()));
                    }

                    break;

                default:
                    throw new IllegalArgumentException("_formatType has invalid value: "+_formatType.name());
            }
        }
        return values;
    }
*/





    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public void execute() throws JmriException {

        final boolean useThread = this._useThread;

        final ConditionalNG conditionalNG = getConditionalNG();
        final DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        String urlString = _selectUrl.evaluateValue(conditionalNG);
        String userAgent = _selectUserAgent.evaluateValue(conditionalNG);
        RequestMethodType requestMethodType = _selectRequestMethod.evaluateEnum(conditionalNG);

        URL url;
        StringBuilder paramString = new StringBuilder();

        try {
            for (Parameter parameter : _parameters) {

                Object v = SymbolTable.getInitialValue(
                        parameter._valueType,
                        parameter._valueData,
                        newSymbolTable,
                        newSymbolTable.getSymbols());

                String value;
                if (v != null) value = v.toString();
                else value = "";
                paramString.append(URLEncoder.encode(parameter._name, "UTF-8"));
                paramString.append("=");
                paramString.append(URLEncoder.encode(value, "UTF-8"));
                paramString.append("&");
            }

            if (paramString.length() > 0) {
                paramString.deleteCharAt(paramString.length() - 1);
            }

            if (requestMethodType == RequestMethodType.Get) {
                if (!urlString.contains("?")) urlString += "?";
                urlString +=  paramString.toString();
            }

            url = new URL(urlString);
            System.out.format("URL: %s, query: %s, userInfo: %s%n", url.toString(), url.getQuery(), url.getUserInfo());
//            if (!urlString.contains("LogixNG_WebRequest_Test.php") && !urlString.contains("https://www.modulsyd.se/")) return;
//            if (!urlString.contains("LogixNG_WebRequest_Test.php")) return;
//            if (!urlString.contains("https://www.modulsyd.se/")) return;
        } catch (UnsupportedEncodingException | MalformedURLException ex) {
            throw new JmriException(ex.getMessage(), ex);
        }

        boolean useHttps = urlString.toLowerCase().startsWith("https://");

        Runnable runnable = () -> {
            System.out.format("Runnable.start%n");
//            String https_url = "https://www.google.com/";
//            String https_url = "https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php";
            try {

                long startTime = System.currentTimeMillis();

//                if (!_localVariableForPostContent.isEmpty()) {
//                    postContent = newSymbolTable.getValue(_localVariableForPostContent);
//                }

                HttpURLConnection con;
                if (useHttps) {
                    con = (HttpsURLConnection) url.openConnection();
                } else {
                    con = (HttpURLConnection) url.openConnection();
                }

                con.setRequestMethod(requestMethodType._identifier);
                con.setRequestProperty("User-Agent", userAgent);


//                con.setRequestProperty("Cookie", "phpbb3_tm7zs_sid=5b33176e78318082f439a0a302fa4c25; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
//                con.setRequestProperty("Cookie", "Daniel=Hej; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
//                con.setRequestProperty("Cookie", "DanielAA=Hej; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
//                con.setRequestProperty("Cookie", "DanielBB=Hej; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
//                con.setRequestProperty("Cookie", "Aaa=Abb; Abb=Add; Acc=Aff");

                if (!_localVariableForCookies.isEmpty()) {
                    StringBuilder cookies = new StringBuilder();

                    Object cookiesObject = newSymbolTable.getValue(_localVariableForCookies);
                    if (cookiesObject instanceof List) {
                        if (!(cookiesObject instanceof List)) {
                            throw new IllegalArgumentException(String.format("The value of the local variable '%s' must be a List", _localVariableForCookies));
                        }
                        System.out.format("Set cookies to connection. Count: %d%n", ((List<Object>)cookiesObject).size());
                        for (Object o : ((List<Object>)cookiesObject)) {
                            if (!(o instanceof String)) {
                                throw new JmriException(String.format("The local variable \"%s\" has List but the item \"%s\" is not a string", _localVariableForCookies, o));
                            }
                            String c = o.toString();
                            if (c.contains(";")) c = c.substring(0, c.indexOf(";"));
                            if (cookies.length() > 0) {
                                cookies.append("; ");
                            }
                            cookies.append(c);
//                            System.out.format("Set cookie to connection: '%s'%n", o.toString());
//                            con.setRequestProperty("Cookie", o.toString());
//                            con.setRequestProperty("Cookie", "phpbb3_tm7zs_sid=5b33176e78318082f439a0a302fa4c25; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
//                            con.setRequestProperty("Cookie", "Daniel=Hej; expires=Fri, 29-Mar-2024 18:22:48 GMT; path=/; domain=.modulsyd.se; secure; HttpOnly");
                        }
                        if (cookies.length() > 0) {
                            System.out.format("Set cookie to connection: '%s'%n", cookies.toString());
                            con.setRequestProperty("Cookie", cookies.toString());
                        } else {
                            System.out.format("Set cookie to connection: 'NULL' - ERROR ERROR ERROR ERROR ERROR ERROR ERROR%n");
                        }
                    } else {
                        System.out.format("The local variable \"%s\" is \"%s\"%n", _localVariableForCookies, cookiesObject);
                        if ((cookiesObject != null)
                                && !((cookiesObject instanceof String) && cookiesObject.toString().isBlank())) {
                            System.out.format("ERROR: The local variable \"%s\" doesn't contain a List%n", _localVariableForCookies);
                            throw new JmriException(String.format("The local variable \"%s\" doesn't contain a List", _localVariableForCookies));
                        }
                    }
                }






////DANIEL                con.setRequestProperty("Content-Type", "text/html");
//                con.setRequestProperty("Content-Type", mime);

//                con.setRequestProperty("Content-Type", "application/json");
//                con.setRequestProperty("Content-Type", "application/html");
//                con.setRequestProperty("Content-Type", "text/html");
//                con.setRequestProperty("Content-Type", "text/plain");
//                con.setRequestProperty("Content-Type", "text/csv");
//                con.setRequestProperty("Content-Type", "text/markdown");

                if (requestMethodType == RequestMethodType.Post) {
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                        out.writeBytes(paramString.toString());
                        out.flush();
                    }
                }







                //dumpl all cert info
//                print_https_cert(con);


                System.out.println("Response Code: " + con.getResponseCode());

                System.out.println("Header fields:");
                for (var entry : con.getHeaderFields().entrySet()) {
                    for (String value : entry.getValue()) {
                        System.out.format("Header: %s, value: %s%n", entry.getKey(), value);
                    }
                }

                //dump all the content
//DANIEL                print_content(con);

                List<String> reply = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String input;
                    while ((input = br.readLine()) != null) {
                        System.out.println(input);
                        reply.add(input);
                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
                }



                List<String> cookies = new ArrayList<>();
                for (var entry : con.getHeaderFields().entrySet()) {
                    if ("Set-Cookie".equals(entry.getKey())) {
                        for (String value : entry.getValue()) {
                            cookies.add(value);
                        }
                    }
                }


                long time = System.currentTimeMillis() - startTime;

                System.out.format("Total time: %d%n", time);

                if (useThread) {
                    synchronized (WebRequest.this) {
                        _internalSocket._conditionalNG = conditionalNG;
                        _internalSocket._newSymbolTable = newSymbolTable;
                        _internalSocket._cookies = cookies;
                        _internalSocket._responseCode = con.getResponseCode();
                        _internalSocket._reply = reply;
//                        _internalSocket.selectedButton = button._value;
//                        _internalSocket.inputValue = textField.getText();
                        conditionalNG.execute(_internalSocket);
                    }
                } else {
                    synchronized (WebRequest.this) {
                        _internalSocket._conditionalNG = conditionalNG;
                        _internalSocket._newSymbolTable = newSymbolTable;
                        _internalSocket._cookies = cookies;
                        _internalSocket.execute();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JmriException ex) {
                log.error("An exception has occurred: {}", ex, ex);
            }
            System.out.format("Runnable.end%n");
        };

        if (useThread) {
            ThreadingUtil.newThread(runnable, "LogixNG action WebRequest").start();
        } else {
            runnable.run();
        }
    }
/*
    private void print_content(HttpURLConnection con) {
        if (con != null) {

            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

                System.out.println("****** Content of the URL ********");

                String input;
                while ((input = br.readLine()) != null) {
                    System.out.println(input);
                }
                br.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
*/




    /** {@inheritDoc} *./
    @Override
    public void execute() throws JmriException {

        String str;
        String strMultiLine;

        switch (_formatType) {
            case OnlyText:
                str = _format;
                break;

            case CommaSeparatedList:
                StringBuilder sb = new StringBuilder();
                for (Object value : getDataValues()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(value != null ? value.toString() : "null");
                }
                str = sb.toString();
                break;

            case StringFormat:
                str = String.format(_format, getDataValues().toArray());
                break;

            default:
                throw new IllegalArgumentException("_formatType has invalid value: "+_formatType.name());
        }

        final ConditionalNG conditionalNG = getConditionalNG();
        final DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        if (_multiLine) strMultiLine = "<html>" + str + "</html>";
        else strMultiLine = str;

        Object value = null;
        if (!_localVariableForInputString.isEmpty()) {
           value = newSymbolTable.getValue(_localVariableForInputString);
        }
        final Object currentValue = value;

        ThreadingUtil.runOnGUIEventually(() -> {

            if (_dialog != null) _dialog.dispose();

            _dialog = new JDialog(
                    (JFrame)null,
                    Bundle.getMessage("ShowDialog_Title"),
                    _modal);

            _dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    _dialog = null;
                }
            });

            JPanel panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            _dialog.getContentPane().add(panel);

            panel.add(new JLabel(strMultiLine));

            JTextField textField = new JTextField(20);
            if (!_localVariableForInputString.isEmpty()) {
                if (currentValue != null) {
                    String strValue = TypeConversionUtil.convertToString(currentValue, false);
                    textField.setText(strValue);
                }
                panel.add(textField);
            }

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout());

            for (Button button : Button.values()) {
                if (_enabledButtons.contains(button)) {
                    JButton jbutton = new JButton(button._text);
                    jbutton.addActionListener((ActionEvent e) -> {
                        synchronized(WebRequest.this) {
                            _internalSocket.conditionalNG = conditionalNG;
                            _internalSocket.newSymbolTable = newSymbolTable;
                            _internalSocket.selectedButton = button._value;
                            _internalSocket.inputValue = textField.getText();
                            conditionalNG.execute(_internalSocket);
                        }
                    });
                    buttonPanel.add(jbutton);
                }
            }
            panel.add(buttonPanel);

            _dialog.pack();
            _dialog.setLocationRelativeTo(null);
            _dialog.setVisible(true);
        });
    }
*/
    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _socket;

            default:
                throw new IllegalArgumentException(
                        String.format("index has invalid value: %d", index));
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public void connected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _socket) {
            _socketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "WebRequest_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage("WebRequest_Long", _selectUrl.getDescription(locale));
/*
        String bundleKey;
        switch (_formatType) {
            case OnlyText:
                bundleKey = "ShowDialog_Long_TextOnly";
                break;
            case CommaSeparatedList:
                bundleKey = "ShowDialog_Long_CommaSeparatedList";
                break;
            case StringFormat:
                bundleKey = "ShowDialog_Long_StringFormat";
                break;
            default:
                throw new RuntimeException("_formatType has unknown value: "+_formatType.name());
        }
        return Bundle.getMessage(locale, bundleKey, _format);
*/
    }

    public FemaleDigitalActionSocket getSocket() {
        return _socket;
    }

    public String getSocketSystemName() {
        return _socketSystemName;
    }

    public void setSocketSystemName(String systemName) {
        _socketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_socket.isConnected()
                    || !_socket.getConnectedSocket().getSystemName()
                            .equals(_socketSystemName)) {

                String socketSystemName = _socketSystemName;

                _socket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _socket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _socket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
/*
        log.debug("getUsageReport :: ShowDialog: bean = {}, report = {}", cdl, report);
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                if (bean.equals(namedBeanReference._handle.getBean())) {
                    report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
                }
            }
        }
*/
    }


    public enum RequestMethodType {
        Get("WebRequest_GetPostType_Get", "GET"),        // "GET" should not be i11n
        Post("WebRequest_GetPostType_Post", "POST");     // "POST" should not be i11n

        private final String _text;
        private final String _identifier;

        private RequestMethodType(String text, String identifier) {
            this._text = Bundle.getMessage(text, identifier);
            this._identifier = identifier;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public static class Parameter {

        public String _name;
        public InitialValueType _valueType;
        public String _valueData;

        public Parameter(String name, InitialValueType valueType, String valueData) {
            this._name = name;
            this._valueType = valueType;
            this._valueData = valueData;
        }
    }




/*
    public enum DataType {
        LocalVariable(Bundle.getMessage("ShowDialog_Operation_LocalVariable")),
        Memory(Bundle.getMessage("ShowDialog_Operation_Memory")),
        Reference(Bundle.getMessage("ShowDialog_Operation_Reference")),
        Formula(Bundle.getMessage("ShowDialog_Operation_Formula"));

        private final String _text;

        private DataType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public static class Data {

        private DataType _dataType = DataType.LocalVariable;
        private String _data = "";
        private ExpressionNode _expressionNode;

        public Data(Data data) throws ParserException {
            _dataType = data._dataType;
            _data = data._data;
            calculateFormula();
        }

        public Data(DataType dataType, String data) throws ParserException {
            if (dataType != null) {
                _dataType = dataType;
            } else {
                // Sometimes data entered in a JTable is not updated correctly
                log.warn("dataType is null");
            }
            _data = data;
            calculateFormula();
        }

        private void calculateFormula() throws ParserException {
            if (_dataType == DataType.Formula) {
                Map<String, Variable> variables = new HashMap<>();
                RecursiveDescentParser parser = new RecursiveDescentParser(variables);
                _expressionNode = parser.parseExpression(_data);
            } else {
                _expressionNode = null;
            }
        }

        public void setDataType(DataType dataType) {
            if (dataType != null) {
                _dataType = dataType;
            } else {
                // Sometimes data entered in a JTable is not updated correctly
                log.warn("dataType is null");
            }
        }

        public DataType getDataType() { return _dataType; }

        public void setData(String data) { _data = data; }
        public String getData() { return _data; }

    }
*/

    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG _conditionalNG;
        private SymbolTable _newSymbolTable;
        private int _responseCode;
        private List<String> _cookies;
        private List<String> _reply;

        public InternalFemaleSocket() {
            super(null, new FemaleSocketListener(){
                @Override
                public void connected(FemaleSocket socket) {
                    // Do nothing
                }

                @Override
                public void disconnected(FemaleSocket socket) {
                    // Do nothing
                }
            }, "A");
        }

        @Override
        public void execute() throws JmriException {
            if (_socket != null) {
                MaleSocket maleSocket = (MaleSocket)WebRequest.this.getParent();
                try {
                    SymbolTable oldSymbolTable = _conditionalNG.getSymbolTable();
                    _conditionalNG.setSymbolTable(_newSymbolTable);
                    if (!_localVariableForResponseCode.isEmpty()) {
                        _newSymbolTable.setValue(_localVariableForResponseCode, _responseCode);
                    }
                    if (!_localVariableForReplyContent.isEmpty()) {
                        _newSymbolTable.setValue(_localVariableForReplyContent, _reply);
                    }
                    if (!_localVariableForCookies.isEmpty()) {
                        System.out.format("Set cookies:%n");
                        for (String s : _cookies) {
                            System.out.format("Set cookies: '%s'%n", s);
                        }
                        if (!_cookies.isEmpty()) {
                           _newSymbolTable.setValue(_localVariableForCookies, _cookies);
                        }
                    } else {
                        System.out.format("Local variable for cookies is empty!!!%n");
                    }
                    _socket.execute();
                    _conditionalNG.setSymbolTable(oldSymbolTable);
                } catch (JmriException e) {
                    if (e.getErrors() != null) {
                        maleSocket.handleError(WebRequest.this, rbx.getString("ExceptionExecuteMulti"), e.getErrors(), e, log);
                    } else {
                        maleSocket.handleError(WebRequest.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                    }
                } catch (RuntimeException e) {
                    maleSocket.handleError(WebRequest.this, Bundle.formatMessage(rbx.getString("ExceptionExecuteAction"), e.getLocalizedMessage()), e, log);
                }
            }
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebRequest.class);






















/*

        https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php


        Class HttpURLConnection
        https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html


        Class HttpsURLConnection
        https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/HttpsURLConnection.html


        Do a Simple HTTP Request in Java
        https://www.baeldung.com/java-http-request



        Java HttpsURLConnection example
        https://mkyong.com/java/java-https-client-httpsurlconnection-example/

        HttpsURLConnection - Send POST request
        https://stackoverflow.com/questions/43352000/httpsurlconnection-send-post-request

        HttpsURLConnection
        https://developer.android.com/reference/javax/net/ssl/HttpsURLConnection




        MIME types (IANA media types)
        https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types

        Why is it `text/html` but `application/json` in media types?
        https://stackoverflow.com/questions/51191184/why-is-it-text-html-but-application-json-in-media-types


        How To Use Java HttpURLConnection for HTTP GET and POST Requests
        https://www.digitalocean.com/community/tutorials/java-httpurlconnection-example-java-http-request-get-post


        Making a JSON POST Request With HttpURLConnection
        https://www.baeldung.com/httpurlconnection-post

        https://www.jmri.org/JavaDoc/doc/jmri/server/json/JSON.html


*/

}
