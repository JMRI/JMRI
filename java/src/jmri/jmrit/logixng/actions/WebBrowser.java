package jmri.jmrit.logixng.actions;

import java.awt.Desktop;
import java.io.IOException;
import java.net.*;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Executes an string action with the result of an string expression.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class WebBrowser
        extends AbstractDigitalAction
        implements FemaleSocketListener {

    private String _urlExpressionSocketSystemName;
    private final FemaleStringExpressionSocket _urlExpressionSocket;
    
    public WebBrowser(String sys, String user) {
        super(sys, user);
        _urlExpressionSocket = InstanceManager.getDefault(StringExpressionManager.class)
                .createFemaleSocket(this, this, "E");
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        WebBrowser copy = new WebBrowser(sysName, userName);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        String url = _urlExpressionSocket.evaluate();
        
        try {
            URI uri = new URI(url);
            HttpURLConnection request = (HttpURLConnection) uri.toURL().openConnection();
            request.setRequestMethod("GET");
            request.connect();
            if (request.getResponseCode() != 200) {
                throw new JmriException(String.format(
                        "Failed to connect to web page: %d, %s",
                        request.getResponseCode(), request.getResponseMessage()));
            }
            if ( Desktop.getDesktop().isSupported( Desktop.Action.BROWSE) ) {
                // Open browser to URL with draft report
                Desktop.getDesktop().browse(uri);
            } else {
                throw new JmriException(String.format(
                        "Failed to connect to web page. java.awt.Desktop doesn't suppport Action.BROWSE"));
            }
        } catch (IOException | URISyntaxException e) {
            throw new JmriException(String.format(
                    "Failed to connect to web page. Exception thrown: %s",
                    e.getMessage()), e);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case 0:
                return _urlExpressionSocket;
                
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
        if (socket == _urlExpressionSocket) {
            _urlExpressionSocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _urlExpressionSocket) {
            _urlExpressionSocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "WebBrowser_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "WebBrowser_Long", _urlExpressionSocket.getName());
    }

    public FemaleStringExpressionSocket getStringExpressionSocket() {
        return _urlExpressionSocket;
    }

    public String getStringExpressionSocketSystemName() {
        return _urlExpressionSocketSystemName;
    }

    public void setStringExpressionSocketSystemName(String systemName) {
        _urlExpressionSocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if (!_urlExpressionSocket.isConnected()
                    || !_urlExpressionSocket.getConnectedSocket().getSystemName()
                            .equals(_urlExpressionSocketSystemName)) {
                
                String socketSystemName = _urlExpressionSocketSystemName;
                
                _urlExpressionSocket.disconnect();
                
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(StringExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    if (maleSocket != null) {
                        _urlExpressionSocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load string expression " + socketSystemName);
                    }
                }
            } else {
                _urlExpressionSocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebBrowser.class);
    
}
