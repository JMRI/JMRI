package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.script.JmriScriptEngineManager;

/**
 * Executes a script.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSimpleScript extends AbstractDigitalAction {

    private String _scriptText;

    public ActionSimpleScript(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSimpleScript copy = new ActionSimpleScript(sysName, userName);
        copy.setComment(getComment());
        copy.setScript(_scriptText);
        return manager.registerAction(copy);
    }
    
    public void setScript(String script) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setScript must not be called when listeners are registered");
            log.error("setScript must not be called when listeners are registered", e);
            throw e;
        }
        _scriptText = script;
    }
    
    public String getScriptText() {
        return _scriptText;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        
        try {
            JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();
            
            Bindings bindings = new SimpleBindings();
            ScriptParams params = new ScriptParams(this);
            
            // this should agree with help/en/html/tools/scripting/Start.shtml - this link is wrong and should point to LogixNG documentation
            bindings.put("analogActions", InstanceManager.getNullableDefault(AnalogActionManager.class));
            bindings.put("analogExpressions", InstanceManager.getNullableDefault(AnalogExpressionManager.class));
            bindings.put("digitalActions", InstanceManager.getNullableDefault(DigitalActionManager.class));
            bindings.put("digitalBooleanActions", InstanceManager.getNullableDefault(DigitalBooleanActionManager.class));
            bindings.put("digitalExpressions", InstanceManager.getNullableDefault(DigitalExpressionManager.class));
            bindings.put("stringActions", InstanceManager.getNullableDefault(StringActionManager.class));
            bindings.put("stringExpressions", InstanceManager.getNullableDefault(StringExpressionManager.class));
            
            bindings.put("params", params);    // Give the script access to the local variable 'params'
            
            scriptEngineManager.getEngineByName(JmriScriptEngineManager.PYTHON)
                    .eval(_scriptText, bindings);
        } catch (ScriptException e) {
            log.warn("cannot execute script", e);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SimpleScript_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "SimpleScript_Long");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void firePropertyChange(String p, Object old, Object n) {
        super.firePropertyChange(p, old, n);
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        // Do nothing
    }
    
    
    public static class ScriptParams {
        
        public final AtomicReference<AbstractScriptDigitalAction> _scriptClass
                = new AtomicReference<>();
        
        public final DigitalAction _parentAction;
        
        public ScriptParams(DigitalAction parentExpression) {
            _parentAction = parentExpression;
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSimpleScript.class);
    
}
