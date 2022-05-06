package jmri.jmrix.mqtt.logixng;

import jmri.jmrix.mqtt.MqttSystemConnectionMemo;

import java.util.List;

import jmri.jmrit.logixng.Category;

/**
 * Defines the category MQTT
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public final class CategoryMqtt extends Category {

    /**
     * A item on the layout, for example turnout, sensor and signal mast.
     */
    public static final CategoryMqtt MQTT = new CategoryMqtt();


    public CategoryMqtt() {
        super("MQTT", Bundle.getMessage("MenuMQTT"), 300);
    }

    public static void registerCategory() {
        // We don't want to add these classes if we don't have a MQTT connection
        if (hasMQTT() && !Category.values().contains(MQTT)) {
            Category.registerCategory(MQTT);
        }
    }

    /**
     * Do we have a MQTT connection?
     * @return true if we have MQTT, false otherwise
     */
    public static boolean hasMQTT() {
        List<MqttSystemConnectionMemo> list = jmri.InstanceManager.getList(MqttSystemConnectionMemo.class);

        // We have at least one LocoNet connection if the list is not empty
        return !list.isEmpty();
    }

}
