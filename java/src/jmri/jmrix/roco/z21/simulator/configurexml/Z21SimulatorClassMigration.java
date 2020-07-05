package jmri.jmrix.roco.z21.simulator.configurexml;

import java.util.HashMap;
import java.util.Map;
import jmri.configurexml.ClassMigration;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Handle changes in persistence class names for the Z21 simulator.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = ClassMigration.class)
@API(status = EXPERIMENTAL)
public class Z21SimulatorClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> map = new HashMap<>();
        map.put("jmri.jmrix.roco.z21.simulator.configurexml.ConnectionConfigXml", "jmri.jmrix.roco.z21.simulator.configurexml.Z21SimulatorConnectionConfigXml");
        return map;
    }

}
