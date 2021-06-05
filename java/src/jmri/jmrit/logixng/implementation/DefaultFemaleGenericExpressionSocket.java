package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.CheckForNull;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Default implementation of the FemaleGenericExpressionSocket
 */
public class DefaultFemaleGenericExpressionSocket
        extends AbstractFemaleSocket
        implements FemaleGenericExpressionSocket, FemaleSocketListener {

    private SocketType _socketType;             // The type of the socket the user has selected
    private SocketType _currentSocketType;      // The current type of the socket.
    private FemaleSocket _currentActiveSocket;  // The socket that is currently in use, if any. Null otherwise.
    private final FemaleAnalogExpressionSocket _analogSocket = new DefaultFemaleAnalogExpressionSocket(this, this, "A");
    private final FemaleDigitalExpressionSocket _digitalSocket = new DefaultFemaleDigitalExpressionSocket(this, this, "D");
    private final FemaleStringExpressionSocket _stringSocket = new DefaultFemaleStringExpressionSocket(this, this, "S");
    private boolean _do_i18n;
    
    public DefaultFemaleGenericExpressionSocket(
            SocketType socketType,
            Base parent,
            FemaleSocketListener listener,
            String name) {
        
        super(parent, listener, name);
        
        _socketType = socketType;
        _currentSocketType = socketType;
        
        switch (_socketType) {
            case ANALOG:
                _currentActiveSocket = _analogSocket;
                break;
                
            case DIGITAL:
                _currentActiveSocket = _digitalSocket;
                break;
                
            case STRING:
                _currentActiveSocket = _stringSocket;
                break;
                
            case GENERIC:
                _currentActiveSocket = null;
                break;
                
            default:
                throw new RuntimeException("_socketType has invalid value: "+socketType.name());
        }
    }
    
    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getCurrentActiveSocket() {
        return _currentActiveSocket;
    }
    
    
    /** {@inheritDoc} */
    @Override
    public boolean isCompatible(MaleSocket socket) {
        return (socket instanceof MaleAnalogExpressionSocket)
                || (socket instanceof MaleDigitalExpressionSocket)
                || (socket instanceof MaleStringExpressionSocket);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setSocketType(SocketType socketType)
            throws SocketAlreadyConnectedException {
        
        if (socketType == _socketType) {
            return;
        }
        
        if ((_currentActiveSocket != null) && (_currentActiveSocket.isConnected())) {
            throw new SocketAlreadyConnectedException("Socket is already connected");
        }
        
        switch (socketType) {
            case DIGITAL:
                _socketType = SocketType.DIGITAL;
                _currentSocketType = SocketType.DIGITAL;
                _currentActiveSocket = _digitalSocket;
                break;
                
            case ANALOG:
                _socketType = SocketType.ANALOG;
                _currentSocketType = SocketType.ANALOG;
                _currentActiveSocket = _analogSocket;
                break;
                
            case STRING:
                _socketType = SocketType.STRING;
                _currentSocketType = SocketType.STRING;
                _currentActiveSocket = _stringSocket;
                break;
                
            case GENERIC:
                _socketType = SocketType.GENERIC;
                _currentSocketType = SocketType.GENERIC;
                _currentActiveSocket = null;
                break;
                
            default:
                throw new RuntimeException("socketType has invalid value: "+socketType.name());
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public SocketType getSocketType() {
        return _socketType;
    }
    
    public void setDoI18N(boolean do_i18n) {
        _do_i18n = do_i18n;
    }
    
    public boolean getDoI18N() {
        return _do_i18n;
    }
    
    @Override
    @CheckForNull
    public Object evaluateGeneric() throws JmriException {
        if (isConnected()) {
            switch (_currentSocketType) {
                case DIGITAL:
                    return ((MaleDigitalExpressionSocket)getConnectedSocket())
                            .evaluate();
                    
                case ANALOG:
                    return ((MaleAnalogExpressionSocket)getConnectedSocket())
                            .evaluate();
                    
                case STRING:
                    return ((MaleStringExpressionSocket)getConnectedSocket())
                            .evaluate();
                    
                default:
                    throw new RuntimeException("_currentSocketType has invalid value: "+_currentSocketType.name());
            }
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleGenericExpressionSocket_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "DefaultFemaleGenericExpressionSocket_Long", getName());
    }

    private void addClassesToMap(
            Map<Category, List<Class<? extends Base>>> destinationClasses,
            Map<Category, List<Class<? extends Base>>> sourceClasses) {
        
        for (Category category : Category.values()) {
            // Some categories might not have any expression.
            if (sourceClasses.get(category) == null) continue;
            
            for (Class<? extends Base> clazz : sourceClasses.get(category)) {
                destinationClasses.get(category).add(clazz);
            }
        }
    }
    
    @Override
    public Map<Category, List<Class<? extends Base>>> getConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> classes = new HashMap<>();
        
        for (Category category : Category.values()) {
            classes.put(category, new ArrayList<>());
        }
        
        addClassesToMap(classes, InstanceManager.getDefault(AnalogExpressionManager.class).getExpressionClasses());
        addClassesToMap(classes, InstanceManager.getDefault(DigitalExpressionManager.class).getExpressionClasses());
        addClassesToMap(classes, InstanceManager.getDefault(StringExpressionManager.class).getExpressionClasses());
        
        return classes;
    }
    
    /** {@inheritDoc} */
    @Override
    public void connect(MaleSocket socket) throws SocketAlreadyConnectedException {
        
        if (socket == null) {
            throw new NullPointerException("socket cannot be null");
        }
        
        // If _currentActiveSocket is not null, the socket is either connected
        // or locked to a particular type.
        if (_currentActiveSocket != null) {
            if (_currentActiveSocket.isConnected()) {
                throw new SocketAlreadyConnectedException("Socket is already connected");
            } else {
                _currentActiveSocket.connect(socket);
                _listener.connected(this);
                return;
            }
        }
        
        // If we are here, the socket is not connected and is not locked to a
        // particular type.
        
        if (_digitalSocket.isCompatible(socket)) {
            _currentSocketType = SocketType.DIGITAL;
            _currentActiveSocket = _digitalSocket;
        } else if (_analogSocket.isCompatible(socket)) {
            _currentSocketType = SocketType.ANALOG;
            _currentActiveSocket = _analogSocket;
        } else if (_stringSocket.isCompatible(socket)) {
            _currentSocketType = SocketType.STRING;
            _currentActiveSocket = _stringSocket;
        } else {
            throw new IllegalArgumentException("Socket is not compatible");
        }
        _currentActiveSocket.connect(socket);
        _listener.connected(this);
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect() {
        if ((_currentActiveSocket != null)
                && _currentActiveSocket.isConnected()) {
            
            _currentActiveSocket.disconnect();
            _listener.disconnected(this);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket getConnectedSocket() {
        if (_currentActiveSocket != null) {
            return _currentActiveSocket.getConnectedSocket();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConnected() {
        return (_currentActiveSocket != null) && _currentActiveSocket.isConnected();
    }

    @Override
    public void connected(FemaleSocket socket) {
        // Do nothing
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (_socketType == SocketType.GENERIC) {
            _currentActiveSocket = null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }
    
}
