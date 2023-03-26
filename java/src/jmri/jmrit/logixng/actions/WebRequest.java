package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.*;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sends a web request.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class WebRequest extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener, VetoableChangeListener {

    private static final ResourceBundle rbx =
            ResourceBundle.getBundle("jmri.jmrit.logixng.implementation.ImplementationBundle");

    private boolean _useThread = true;

    private GetPostType _getPostType = GetPostType.Get;
//    private GetPostType _getPostType = GetPostType.Post;

    private final LogixNG_SelectString _selectUrl =
            new LogixNG_SelectString(this, this);

//    private final LogixNG_SelectEnum<MimeType> _selectMime =
//            new LogixNG_SelectEnum<>(this, MimeType.values(), MimeType.TextHtml, this);

    private String _executeSocketSystemName;
    private final FemaleDigitalActionSocket _executeSocket;
    private String _localVariableForPostContent = "";
    private String _localVariableForResponseCode = "";
    private String _localVariableForReplyContent = "";
    private final InternalFemaleSocket _internalSocket = new InternalFemaleSocket();


    public WebRequest(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _executeSocket = InstanceManager.getDefault(DigitalActionManager.class)
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
        copy.setGetPostType(_getPostType);
        getSelectUrl().copy(copy._selectUrl);
//        getSelectMime().copy(copy._selectMime);
        copy.setLocalVariableForPostContent(_localVariableForPostContent);
        copy.setLocalVariableForResponseCode(_localVariableForResponseCode);
        copy.setLocalVariableForReplyContent(_localVariableForReplyContent);
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

//    public LogixNG_SelectEnum<MimeType> getSelectMime() {
//        return _selectMime;
//    }

    public void setUseThread(boolean value) {
        _useThread = value;
    }

    public void setGetPostType(GetPostType value) {
        _getPostType = value;
    }

    public GetPostType getGetPostType() {
        return _getPostType;
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
    public void execute() throws JmriException {

        final boolean useThread = this._useThread;

        final ConditionalNG conditionalNG = getConditionalNG();
        final DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());

        String urlString = _selectUrl.evaluateValue(conditionalNG);
//        String mime = _selectMime.evaluateEnum(conditionalNG)._mime;

        Map<String, String> parameters = new HashMap<>();
        parameters.put("firstName", "Daniel");
        parameters.put("lastName", "Bergqvist");
        parameters.put("address", "Åbenråvägen 2");

        String parameters11;
        URL url;
        try {
            parameters11 = ParameterStringBuilder.getParamsString(parameters);
            if (_getPostType == GetPostType.Get) {
                urlString += "?" + parameters11;
//                urlString += "?name=Daniel&sur=Bergqvist";
            }
//            urlString += "?name=Daniel&sur=Bergqvist&some=thing";
            url = new URL(urlString);
            System.out.format("URL: %s, query: %s, userInfo: %s%n", url.toString(), url.getQuery(), url.getUserInfo());
//            System.out.format("Host: %s, Port: %d, DefaultPort: %d, File: %s, Protocol: %s, Authority: %s, Path: %s, Query: %s, Ref: %s, UserInfo: %s%n", url.getHost(), url.getPort(), url.getDefaultPort(), url.getFile(), url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), url.getRef(), url.getUserInfo());
//            if (1==1) return;
            if (!urlString.contains("LogixNG_WebRequest_Test.php")) return;
        } catch (UnsupportedEncodingException | MalformedURLException ex) {
            throw new JmriException(ex.getMessage(), ex);
        }

        boolean useHttps = urlString.toLowerCase().startsWith("https://");

        Runnable runnable = () -> {
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


////DANIEL                con.setRequestProperty("Content-Type", "text/html");
//                con.setRequestProperty("Content-Type", mime);

//                con.setRequestProperty("Content-Type", "application/json");
//                con.setRequestProperty("Content-Type", "application/html");
//                con.setRequestProperty("Content-Type", "text/html");
//                con.setRequestProperty("Content-Type", "text/plain");
//                con.setRequestProperty("Content-Type", "text/csv");
//                con.setRequestProperty("Content-Type", "text/markdown");

                switch (_getPostType) {
                    case Get:
                        con.setRequestMethod("GET");
/*
                        con.setDoOutput(true);
                        try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                            out.writeBytes(parameters11);
                            out.flush();
                        }
*/
                        break;

                    case Post:
                        con.setRequestMethod("POST");
                        con.setDoOutput(true);
                        try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                            out.writeBytes(parameters11);
                            out.flush();
                        }
                        break;

                    default:
                        throw new IllegalArgumentException("_getPostType has unknown value: "+_getPostType.name());
                }







                //dumpl all cert info
//                print_https_cert(con);
                //dump all the content
                print_content(con);

                System.out.println("Response Code : " + con.getResponseCode());

                long time = System.currentTimeMillis() - startTime;

                System.out.format("Total time: %d%n", time);

                if (useThread) {
                    synchronized (WebRequest.this) {
                        _internalSocket.conditionalNG = conditionalNG;
                        _internalSocket.newSymbolTable = newSymbolTable;
//                        _internalSocket.selectedButton = button._value;
//                        _internalSocket.inputValue = textField.getText();
                        conditionalNG.execute(_internalSocket);
                    }
                } else {
                    synchronized (WebRequest.this) {
                        _internalSocket.conditionalNG = conditionalNG;
                        _internalSocket.newSymbolTable = newSymbolTable;
                        _internalSocket.execute();
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JmriException ex) {
                log.error("An exception has occurred: {}", ex.getMessage(), ex);
            }
        };

        if (useThread) {
            ThreadingUtil.newThread(runnable, "LogixNG action WebRequest").start();
        } else {
            runnable.run();
        }
    }

    public static class ParameterStringBuilder {

        public static String getParamsString(Map<String, String> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }

    private void print_content(HttpURLConnection con) {
        if (con != null) {

            try {

                System.out.println("****** Content of the URL ********");
                BufferedReader br
                        = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));

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
                return _executeSocket;

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
        if (socket == _executeSocket) {
            _executeSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _executeSocket) {
            _executeSocketSystemName = null;
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

    public FemaleDigitalActionSocket getExecuteSocket() {
        return _executeSocket;
    }

    public String getExecuteSocketSystemName() {
        return _executeSocketSystemName;
    }

    public void setExecuteSocketSystemName(String systemName) {
        _executeSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_executeSocket.isConnected()
                    || !_executeSocket.getConnectedSocket().getSystemName()
                            .equals(_executeSocketSystemName)) {

                String socketSystemName = _executeSocketSystemName;

                _executeSocket.disconnect();

                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(DigitalActionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _executeSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load digital action {}", socketSystemName);
                    }
                }
            } else {
                _executeSocket.getConnectedSocket().setup();
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


    public enum GetPostType {
        Get(Bundle.getMessage("WebRequest_GetPostType_Get", "GET")),        // "GET" should not be i11n
        Post(Bundle.getMessage("WebRequest_GetPostType_Post", "POST"));     // "POST" should not be i11n

        private final String _text;

        private GetPostType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }




/*
    public enum FormatType {
        OnlyText(Bundle.getMessage("ShowDialog_FormatType_TextOnly"), true, false),
        CommaSeparatedList(Bundle.getMessage("ShowDialog_FormatType_CommaSeparatedList"), false, true),
        StringFormat(Bundle.getMessage("ShowDialog_FormatType_StringFormat"), true, true);

        private final String _text;
        private final boolean _useFormat;
        private final boolean _useData;

        private FormatType(String text, boolean useFormat, boolean useData) {
            this._text = text;
            this._useFormat = useFormat;
            this._useData = useData;
        }

        @Override
        public String toString() {
            return _text;
        }

        public boolean getUseFormat() {
            return _useFormat;
        }

        public boolean getUseData() {
            return _useData;
        }

    }

/*
    public enum Button {
        Ok(1, Bundle.getMessage("ButtonOK")),
        Cancel(2, Bundle.getMessage("ButtonCancel")),
        Yes(3, Bundle.getMessage("ButtonYes")),
        No(4, Bundle.getMessage("ButtonNo"));

        private final int _value;
        private final String _text;

        private Button(int value, String text) {
            this._value = value;
            this._text = text;
        }

        public int getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return _text;
        }

    }
*/
/*
    public enum MimeType {
        TextPlain(Bundle.getMessage("WebRequest_MimeType_TextPlain"), "text/plain"),
        TextHtml(Bundle.getMessage("WebRequest_MimeType_TextHtml"), "text/html"),
        ApplicationJson(Bundle.getMessage("WebRequest_MimeType_ApplicationJson"), "application/json");

        private final String _text;
        private final String _mime;

        private MimeType(String text, String mime) {
            this._text = text;
            this._mime = mime;
        }

        public String getMime() {
            return _mime;
        }

        @Override
        public String toString() {
            return _text;
        }

    }
*/
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

        private ConditionalNG conditionalNG;
        private SymbolTable newSymbolTable;
        private int responseCode;
        private String inputValue;

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
            if (_executeSocket != null) {
                MaleSocket maleSocket = (MaleSocket)WebRequest.this.getParent();
                try {
                    SymbolTable oldSymbolTable = conditionalNG.getSymbolTable();
                    conditionalNG.setSymbolTable(newSymbolTable);
                    if (!_localVariableForResponseCode.isEmpty()) {
                        newSymbolTable.setValue(_localVariableForResponseCode, responseCode);
                    }
                    if (!_localVariableForReplyContent.isEmpty()) {
                        newSymbolTable.setValue(_localVariableForReplyContent, inputValue);
                    }
                    _executeSocket.execute();
                    conditionalNG.setSymbolTable(oldSymbolTable);
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
