package jmri.server.json;

import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.POST;
import static jmri.server.json.JSON.PUT;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.ReporterManager;
import jmri.Manager.NameValidity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of JsonSocketService with specific support for
 * {@link jmri.NamedBean} objects. Note that services requiring support for
 * multiple classes of NamedBean cannot extend this class.
 *
 * @author Randall Wood (C) 2019
 * @param <T> the NamedBean class supported by this service
 * @param <H> the supporting JsonNamedBeanHttpService class
 */
public class JsonNamedBeanSocketService<T extends NamedBean, H extends JsonNamedBeanHttpService<T>> extends JsonSocketService<H> {

    protected final HashMap<String, NamedBeanListener> beanListeners = new HashMap<>();
    protected final ManagerListener managerListener = new ManagerListener();
    private final static Logger log = LoggerFactory.getLogger(JsonNamedBeanSocketService.class);

    public JsonNamedBeanSocketService(JsonConnection connection, H service) {
        super(connection, service);
        service.getManager().addPropertyChangeListener(managerListener);
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale)
            throws IOException, JmriException, JsonException {
        setLocale(locale);
        String name = data.path(NAME).asText();
        // protect against a request made with a user name instead of a system name
        if (!method.equals(PUT) && service.getManager().validSystemNameFormat(name) != NameValidity.VALID) {
            T bean = service.getManager().getBeanBySystemName(name);
            if (bean == null) {
                bean = service.getManager().getBeanByUserName(name);
                if (bean != null) {
                    // set to warn so users can provide specific feedback to developers of JSON clients
                    log.warn("{} request for {} made with user name \"{}\"; should use system name", method, type, name);
                    name = bean.getSystemName();
                } // service will throw appropriate error to client later if bean is still null
            }
        }
        switch (method) {
            case PUT:
                connection.sendMessage(service.doPut(type, name, data, locale));
                break;
            case GET:
                connection.sendMessage(service.doGet(type, name, data, locale));
                break;
            case POST:
            default:
                connection.sendMessage(service.doPost(type, name, data, locale));
        }
        if (!this.beanListeners.containsKey(name)) {
            addListenerToBean(name);
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        setLocale(locale);
        connection.sendMessage(service.doGetList(type, data, locale));
    }

    @Override
    public void onClose() {
        beanListeners.values().stream().forEach((listener) -> {
            listener.bean.removePropertyChangeListener(listener);
        });
        beanListeners.clear();
        service.getManager().removePropertyChangeListener(managerListener);
    }

    protected void addListenerToBean(String name) {
        addListenerToBean(service.getManager().getBeanBySystemName(name), name);
    }

    protected void addListenerToBean(T bean, String name) {
        if (bean != null) {
            NamedBeanListener listener = new NamedBeanListener(bean);
            bean.addPropertyChangeListener(listener);
            this.beanListeners.put(name, listener);
        }
    }

    protected void removeListenersFromRemovedBeans() {
        for (String name : new HashSet<>(beanListeners.keySet())) {
            if (service.getManager().getBeanBySystemName(name) == null) {
                beanListeners.remove(name);
            }
        }
    }

    protected class NamedBeanListener implements PropertyChangeListener {

        public final T bean;

        public NamedBeanListener(T bean) {
            this.bean = bean;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                connection.sendMessage(service.doGet(this.bean, this.bean.getSystemName(), service.getType(), getLocale()));
            } catch (
                    IOException |
                    JsonException ex) {
                // if we get an error, unregister as listener
                this.bean.removePropertyChangeListener(this);
                beanListeners.remove(this.bean.getSystemName());
            }
        }
    }

    protected class ManagerListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            try {
                try {
                    // send the new list
                    connection.sendMessage(service.doGetList(service.getType(),
                            service.getObjectMapper().createObjectNode(), getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        removeListenersFromRemovedBeans();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending {}: {}", service.getType(), ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, unregister as listener
                log.debug("deregistering reportersListener due to IOException");
                InstanceManager.getDefault(ReporterManager.class).removePropertyChangeListener(this);
            }
        }

    }
}
