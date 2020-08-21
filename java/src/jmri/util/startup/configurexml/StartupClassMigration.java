package jmri.util.startup.configurexml;

import java.util.HashMap;
import java.util.Map;

import jmri.configurexml.ClassMigration;

import org.openide.util.lookup.ServiceProvider;

/**
 * Migrate startup actions in XML from the {@code apps} and {@code apps.startup}
 * packages to the {@code jmri.util.startup} package.
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = ClassMigration.class)
public class StartupClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> map = new HashMap<>();
        map.put("apps.configurexml.PerformActionModelXml", "jmri.util.startup.configurexml.PerformActionModelXml");
        map.put("apps.configurexml.PerformFileModelXml", "jmri.util.startup.configurexml.PerformFileModelXml");
        map.put("apps.configurexml.PerformScriptModelXml", "jmri.util.startup.configurexml.PerformScriptModelXml");
        return map;
    }

}
