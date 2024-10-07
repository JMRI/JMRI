package jmri.jmrit.logixng;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;

/**
 * A reference to a NamedBean.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class NamedBeanReference {

    private String _name;
    private NamedBeanType _type;
    private NamedBeanHandle<? extends NamedBean> _handle;
    private boolean _listenOnAllProperties = false;

    public NamedBeanReference(NamedBeanReference ref) {
        this(ref._handle, ref._type, ref._listenOnAllProperties);
    }

    public NamedBeanReference(String name, NamedBeanType type, boolean all) {
        _name = name;
        _type = type;
        _listenOnAllProperties = all;
        if (_type != null) {
            NamedBean bean = _type.getManager().getNamedBean(name);
            if (bean != null) {
                _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
            }
        }
    }

    public NamedBeanReference(NamedBeanHandle<? extends NamedBean> handle, NamedBeanType type, boolean all) {
        _name = handle != null ? handle.getName() : null;
        _type = type;
        _listenOnAllProperties = all;
        _handle = handle;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
        updateHandle();
    }

    public void setName(NamedBean bean) {
        if (bean != null) {
            _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(bean.getDisplayName(), bean);
            _name = _handle.getName();
        } else {
            _name = null;
            _handle = null;
        }
    }

    public void setName(NamedBeanHandle<? extends NamedBean> handle) {
        if (handle != null) {
            _handle = handle;
            _name = _handle.getName();
        } else {
            _name = null;
            _handle = null;
        }
    }

    public NamedBeanType getType() {
        return _type;
    }

    public void setType(NamedBeanType type) {
        if (type == null) {
            log.warn("type is null");
            type = NamedBeanType.Turnout;
        }
        _type = type;
        _handle = null;
    }

    public NamedBeanHandle<? extends NamedBean> getHandle() {
        return _handle;
    }

    private void updateHandle() {
        if (_type != null && _name != null && !_name.isEmpty()) {
            NamedBean bean = _type.getManager().getNamedBean(_name);
            if (bean != null) {
                _handle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(_name, bean);
            } else {
                log.warn("Cannot find named bean {} in manager for {}", _name, _type.getManager().getBeanTypeHandled());
                _handle = null;
            }
        } else {
            _handle = null;
        }
    }

    public boolean getListenOnAllProperties() {
        return _listenOnAllProperties;
    }

    public void setListenOnAllProperties(boolean listenOnAllProperties) {
        _listenOnAllProperties = listenOnAllProperties;
    }

    // This method is used by ListenOnBeansTableModel
    @Override
    public String toString() {
        if (_handle != null) {
            return _handle.getName();
        } else {
            return "";
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NamedBeanReference.class);

}
