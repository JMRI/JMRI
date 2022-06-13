package jmri.jmrit.logixng;

import javax.script.Bindings;

import jmri.InstanceManager;

/**
 * Script bindings for LogixNG.
 *
 * @author Daniel Bergqvist 2022
 */
public class LogixNG_ScriptBindings {

    // This class should never be instanciated.
    private LogixNG_ScriptBindings() {}

    public static void addScriptBindings(Bindings bindings) {
        // this should agree with help/en/html/tools/scripting/Start.shtml - this link is wrong and should point to LogixNG documentation
        bindings.put("logixngs", InstanceManager.getNullableDefault(LogixNG_Manager.class));
        bindings.put("conditionalngs", InstanceManager.getNullableDefault(ConditionalNG_Manager.class));
        bindings.put("globalVariables", InstanceManager.getNullableDefault(GlobalVariableManager.class));
        bindings.put("logixngModules", InstanceManager.getNullableDefault(ModuleManager.class));
        bindings.put("logixngTables", InstanceManager.getNullableDefault(NamedTableManager.class));
        bindings.put("analogActions", InstanceManager.getNullableDefault(AnalogActionManager.class));
        bindings.put("analogExpressions", InstanceManager.getNullableDefault(AnalogExpressionManager.class));
        bindings.put("digitalActions", InstanceManager.getNullableDefault(DigitalActionManager.class));
        bindings.put("digitalBooleanActions", InstanceManager.getNullableDefault(DigitalBooleanActionManager.class));
        bindings.put("digitalExpressions", InstanceManager.getNullableDefault(DigitalExpressionManager.class));
        bindings.put("stringActions", InstanceManager.getNullableDefault(StringActionManager.class));
        bindings.put("stringExpressions", InstanceManager.getNullableDefault(StringExpressionManager.class));
    }

}
