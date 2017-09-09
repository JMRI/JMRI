package jmri.jmrit.display.switchboardEditor.configurexml;

import java.util.HashMap;
import java.util.Map;
import jmri.configurexml.ClassMigration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class migration for persistence classes for the Switchboard Editor.
 *
 * @author Randall Wood Copyright 2017
 */
@ServiceProvider(service = ClassMigration.class)
public class SwitchboardEditorClassMigration implements ClassMigration {

    @Override
    public Map<String, String> getMigrations() {
        Map<String, String> map = new HashMap<>();
        map.put("jmri.jmrit.display.switchboardEditor.configurexml.SwitchboardEditor$BeanSwitchXml", "jmri.jmrit.display.switchboardEditor.configurexml.BeanSwitchXml");
        return map;
    }

}
