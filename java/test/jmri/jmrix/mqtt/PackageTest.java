package jmri.jmrix.mqtt;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MqttAdapterTest.class,
        MqttConnectionConfigTest.class,
        MqttConnectionTypeListTest.class,
        MqttSystemConnectionMemoTest.class,
        MqttTurnoutManagerTest.class,
        MqttTurnoutTest.class,
        jmri.jmrix.mqtt.configurexml.PackageTest.class
})

/**
 * tests for the jmri.jmrix.mqtt package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest  {
}
