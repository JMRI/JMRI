package jmri.jmrix.pi.configurexml;

import java.util.HashMap;
import java.util.Map;
import jmri.configurexml.ClassMigration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Raspberry Pi class migrations for the
 * {@link jmri.configurexml.ConfigXmlManager} to use.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = ClassMigration.class)
public class RaspberryPiClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> migrations = new HashMap<>();
        migrations.put("jmri.jmrix.pi.configurexml.ConnectionConfigXml", "jmri.jmrix.pi.configurexml.RaspberryPiConnectionConfigXml");
        return migrations;
    }

}
