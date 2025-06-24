package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;

/**
 * Runs an engine.
 * This action reads an analog expression with the loco address and sets its
 * speed according to an alaog expression and the direction according to a
 * digital expression.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionLightIntensity extends AbstractDigitalAction
        implements FemaleSocketListener, PropertyChangeListener {

    public static final int INTENSITY_SOCKET = 0;

    private final LogixNG_SelectNamedBean<VariableLight> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, VariableLight.class, InstanceManager.getDefault(VariableLightManager.class), this);

    private String _intensitySocketSystemName;
    private final FemaleAnalogExpressionSocket _intensitySocket;


    public ActionLightIntensity(String sys, String user) {
        super(sys, user);
        _intensitySocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(this, this, Bundle.getMessage("ActionLightIntensity_SocketName"));
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionLightIntensity copy = new ActionLightIntensity(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<VariableLight> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        VariableLight light = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (light == null) {
//            log.warn("light is null");
            return;
        }

        double intensity = 0.0;

        if (_intensitySocket.isConnected()) {
            intensity =
                    ((MaleAnalogExpressionSocket)_intensitySocket.getConnectedSocket())
                            .evaluate();
        }

        if (intensity < 0.0) intensity = 0.0;
        if (intensity > 100.0) intensity = 100.0;

        light.setTargetIntensity(intensity/100.0);
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        switch (index) {
            case INTENSITY_SOCKET:
                return _intensitySocket;

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
        if (socket == _intensitySocket) {
            _intensitySocketSystemName = socket.getConnectedSocket().getSystemName();
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        if (socket == _intensitySocket) {
            _intensitySocketSystemName = null;
        } else {
            throw new IllegalArgumentException("unkown socket");
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionLightIntensity_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);

        return Bundle.getMessage(locale, "ActionLightIntensity_Long", namedBean);
    }

    public FemaleAnalogExpressionSocket getIntensitySocket() {
        return _intensitySocket;
    }

    public String getIntensitySocketSystemName() {
        return _intensitySocketSystemName;
    }

    public void setIntensitySystemName(String systemName) {
        _intensitySocketSystemName = systemName;
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        try {
            if ( !_intensitySocket.isConnected()
                    || !_intensitySocket.getConnectedSocket().getSystemName()
                            .equals(_intensitySocketSystemName)) {

                String socketSystemName = _intensitySocketSystemName;
                _intensitySocket.disconnect();
                if (socketSystemName != null) {
                    MaleSocket maleSocket =
                            InstanceManager.getDefault(AnalogExpressionManager.class)
                                    .getBySystemName(socketSystemName);
                    _intensitySocket.disconnect();
                    if (maleSocket != null) {
                        _intensitySocket.connect(maleSocket);
                        maleSocket.setup();
                    } else {
                        log.error("cannot load analog expression {}", socketSystemName);
                    }
                }
            } else {
                _intensitySocket.getConnectedSocket().setup();
            }
        } catch (SocketAlreadyConnectedException ex) {
            // This shouldn't happen and is a runtime error if it does.
            throw new RuntimeException("socket is already connected");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectNamedBean.registerListeners();
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLightIntensity.class);

}
