package jmri.server.json.route;

import jmri.Route;
import jmri.Sensor;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonNamedBeanSocketService;

/**
 * JSON socket service provider for managing {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
public class JsonRouteSocketService extends JsonNamedBeanSocketService<Route, JsonRouteHttpService> {

    public JsonRouteSocketService(JsonConnection connection) {
        super(connection, new JsonRouteHttpService(connection.getObjectMapper()));
    }
    
    @Override
    protected void addListenerToBean(Route bean) {
        if (bean != null) {
            NamedBeanListener listener = new NamedBeanListener(bean);
            bean.addPropertyChangeListener(listener);
            Sensor sensor = bean.getTurnoutsAlgdSensor();
            if (sensor != null) {
                sensor.addPropertyChangeListener(listener);
            }
            this.beanListeners.put(bean, listener);
        }
    }

    @Override
    public void onClose() {
        beanListeners.values().stream().forEach((listener) -> {
            Sensor sensor = listener.bean.getTurnoutsAlgdSensor();
            if (sensor != null) {
                sensor.removePropertyChangeListener(listener);
            }
        });
        super.onClose();
    }
}
