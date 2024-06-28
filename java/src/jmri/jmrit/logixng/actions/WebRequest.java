package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.ParserException;
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

    // Note that it's valid if the url has parameters as well, like https://www.mysite.org/somepage.php?name=Jim&city=Boston
    // The parameters are the string after the question mark.
    private final LogixNG_SelectString _selectUrl =
            new LogixNG_SelectString(this, this);

    private final LogixNG_SelectCharset _selectCharset =
            new LogixNG_SelectCharset(this, this);

    private final LogixNG_SelectEnum<RequestMethodType> _selectRequestMethod =
            new LogixNG_SelectEnum<>(this, RequestMethodType.values(), RequestMethodType.Get, this);

    private final LogixNG_SelectString _selectUserAgent =
            new LogixNG_SelectString(this, DEFAULT_USER_AGENT, this);

    private final LogixNG_SelectEnum<ReplyType> _selectReplyType =
            new LogixNG_SelectEnum<>(this, ReplyType.values(), ReplyType.String, this);

    private final LogixNG_SelectEnum<LineEnding> _selectLineEnding =
            new LogixNG_SelectEnum<>(this, LineEnding.values(), LineEnding.System, this);

    private final List<Parameter> _parameters = new ArrayList<>();

    private String _socketSystemName;
    private final FemaleDigitalActionSocket _socket;
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
        getSelectCharset().copy(copy._selectCharset);
        getSelectRequestMethod().copy(copy._selectRequestMethod);
        getSelectUserAgent().copy(copy._selectUserAgent);
        copy._parameters.addAll(_parameters);
//        getSelectMime().copy(copy._selectMime);
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

    public LogixNG_SelectCharset getSelectCharset() {
        return _selectCharset;
    }

    public LogixNG_SelectEnum<RequestMethodType> getSelectRequestMethod() {
        return _selectRequestMethod;
    }

    public LogixNG_SelectString getSelectUserAgent() {
        return _selectUserAgent;
    }

    public LogixNG_SelectEnum<ReplyType> getSelectReplyType() {
        return _selectReplyType;
    }

    public LogixNG_SelectEnum<LineEnding> getSelectLineEnding() {
        return _selectLineEnding;
    }

    public List<Parameter> getParameters() {
        return _parameters;
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

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws JmriException {

        final ConditionalNG conditionalNG = getConditionalNG();
        final DefaultSymbolTable newSymbolTable = new DefaultSymbolTable(conditionalNG.getSymbolTable());
        final boolean useThread = conditionalNG.getRunDelayed();

        String urlString = _selectUrl.evaluateValue(conditionalNG);
        Charset charset = _selectCharset.evaluateCharset(conditionalNG);
        String userAgent = _selectUserAgent.evaluateValue(conditionalNG);
        RequestMethodType requestMethodType = _selectRequestMethod.evaluateEnum(conditionalNG);
        ReplyType replyType = _selectReplyType.evaluateEnum(conditionalNG);
        LineEnding lineEnding = _selectLineEnding.evaluateEnum(conditionalNG);

        URL url;
        StringBuilder paramString = new StringBuilder();

        try {
            for (Parameter parameter : _parameters) {

                Object v = SymbolTable.getInitialValue(
                        SymbolTable.Type.Parameter,
                        parameter._name,
                        parameter._type,
                        parameter._data,
                        newSymbolTable,
                        newSymbolTable.getSymbols());

                String value;
                if (v != null) value = v.toString();
                else value = "";
                paramString.append(URLEncoder.encode(parameter._name, charset));
                paramString.append("=");
                paramString.append(URLEncoder.encode(value, charset));
                paramString.append("&");
            }

            if (paramString.length() > 0) {
                paramString.deleteCharAt(paramString.length() - 1);
            }

            if (requestMethodType == RequestMethodType.Get) {
                if (urlString.contains("?")) {
                    urlString += "&";
                } else {
                    urlString += "?";
                }
                urlString +=  paramString.toString();
//                System.out.format("Param string: \"%s\". URL: \"%s\"%n", paramString, urlString);
            }

            url = new URI(urlString).toURL();
//            System.out.format("URL: %s, query: %s, userInfo: %s%n", url.toString(), url.getQuery(), url.getUserInfo());
//            if (!urlString.contains("LogixNG_WebRequest_Test.php") && !urlString.contains("https://www.modulsyd.se/")) return;
//            if (!urlString.contains("LogixNG_WebRequest_Test.php")) return;
//            if (!urlString.contains("https://www.modulsyd.se/")) return;
        } catch (MalformedURLException | URISyntaxException ex) {
            throw new JmriException(ex.getMessage(), ex);
        }

        boolean useHttps = urlString.toLowerCase().startsWith("https://");

        Runnable runnable = () -> {
//            String https_url = "https://www.google.com/";
//            String https_url = "https://jmri.bergqvist.se/LogixNG_WebRequest_Test.php";
            try {

//                long startTime = System.currentTimeMillis();

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

                Map<String,String> cookiesMap = null;

                if (!_localVariableForCookies.isEmpty()) {
                    StringBuilder cookies = new StringBuilder();

                    Object cookiesObject = newSymbolTable.getValue(_localVariableForCookies);
                    if (cookiesObject != null) {
                        if (!(cookiesObject instanceof Map)) {
                            throw new IllegalArgumentException(String.format("The value of the local variable '%s' must be a Map", _localVariableForCookies));
                        }
                        cookiesMap = (Map<String,String>)cookiesObject;
//                        System.out.format("Set cookies to connection. Count: %d%n", ((List<Object>)cookiesObject).size());
                        for (Map.Entry<String,String> entry : cookiesMap.entrySet()) {
                            if (cookies.length() > 0) {
                                cookies.append("; ");
                            }
                            String[] cookieParts = entry.getValue().split("; ");
                            cookies.append(cookieParts[0]);
//                            System.out.format("Set cookie to connection: '%s=%s'%n", entry.getKey(), entry.getValue());
                        }
                        if (cookies.length() > 0) {
//                            System.out.format("Set cookie to connection: '%s'%n", cookies.toString());
                            con.setRequestProperty("Cookie", cookies.toString());
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


//                System.out.println("Response Code: " + con.getResponseCode());
/*
                System.out.println("Header fields:");
                for (var entry : con.getHeaderFields().entrySet()) {
                    for (String value : entry.getValue()) {
                        System.out.format("Header: %s, value: %s%n", entry.getKey(), value);
                    }
                }
*/
                //dump all the content
//DANIEL                print_content(con);

                Object reply;

                if (replyType == ReplyType.Bytes) {
                    reply = con.getInputStream().readAllBytes();
                } else if (replyType == ReplyType.String || replyType == ReplyType.ListOfStrings) {
                    List<String> list = new ArrayList<>();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String input;
                        while ((input = br.readLine()) != null) {
    //                        System.out.println(input);
                            list.add(input);
                        }
    //                } catch (IOException e) {
    //                    e.printStackTrace();
                    }

                    if (replyType == ReplyType.String) {
                        reply = String.join(lineEnding.getLineEnding(), list);
                    } else {
                        reply = list;
                    }
                } else {
                    throw new IllegalArgumentException("replyType has unknown value: " + replyType.name());
                }




                if (cookiesMap == null) {
                    cookiesMap = new HashMap<>();
                }
                for (var entry : con.getHeaderFields().entrySet()) {
                    if ("Set-Cookie".equals(entry.getKey())) {
                        for (String value : entry.getValue()) {
                            String[] parts = value.split("=");
                            cookiesMap.put(parts[0], value);
                        }
                    }
                }


//                long time = System.currentTimeMillis() - startTime;

//                System.out.format("Total time: %d%n", time);

                synchronized (WebRequest.this) {
                    _internalSocket._conditionalNG = conditionalNG;
                    _internalSocket._newSymbolTable = newSymbolTable;
                    _internalSocket._cookies = cookiesMap;
                    _internalSocket._responseCode = con.getResponseCode();
                    _internalSocket._reply = reply;

                    if (useThread) {
                        conditionalNG.execute(_internalSocket);
                    } else {
                        _internalSocket.execute();
                    }
                }

//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JmriException ex) {
            } catch (IOException | IllegalArgumentException | JmriException ex) {
                log.error("An exception has occurred: {}", ex, ex);
            }
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


    public enum ReplyType {
        String(Bundle.getMessage("WebRequest_ReplyType_String")),
        ListOfStrings(Bundle.getMessage("WebRequest_ReplyType_ListOfStrings")),
        Bytes(Bundle.getMessage("WebRequest_ReplyType_Bytes"));

        private final String _text;

        private ReplyType(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    public static class Parameter {

        public String _name;
        public InitialValueType _type;
        public String _data;

        public Parameter(String name, InitialValueType type, String data) {
            this._name = name;
            this._type = type;
            this._data = data;
        }

        public void setName(String name) { _name = name; }
        public String getName() { return _name; }

        public void setType(InitialValueType dataType) { _type = dataType; }
        public InitialValueType getType() { return _type; }

        public void setData(String valueData) { _data = valueData; }
        public String getData() { return _data; }

    }


    private class InternalFemaleSocket extends jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket {

        private ConditionalNG _conditionalNG;
        private SymbolTable _newSymbolTable;
        private int _responseCode;
        private Map<String,String> _cookies;
        private Object _reply;

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
//                        System.out.format("Set cookies:%n");
//                        for (String s : _cookies) {
//                            System.out.format("Set cookies: '%s'%n", s);
//                        }
                        if (!_cookies.isEmpty()) {
                           _newSymbolTable.setValue(_localVariableForCookies, _cookies);
                        }
//                    } else {
//                        System.out.format("Local variable for cookies is empty!!!%n");
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
