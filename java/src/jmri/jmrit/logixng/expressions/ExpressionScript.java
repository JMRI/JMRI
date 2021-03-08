package jmri.jmrit.logixng.expressions;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.script.JmriScriptEngineManager;

/**
 * Evaluates a script.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ExpressionScript extends AbstractDigitalExpression {

    private String _scriptText;
    private AbstractScriptDigitalExpression _scriptClass;

    public ExpressionScript(@Nonnull String sys, @CheckForNull String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionScript copy = new ExpressionScript(sysName, userName);
        copy.setComment(getComment());
        copy.setScript(_scriptText);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    private void loadScript() {
        try {
            jmri.script.JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

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
            
            _scriptClass = params._scriptClass.get();
        } catch (ScriptException e) {
            log.error("cannot load script", e);
            _scriptText = null;
            _scriptClass = null;
            return;
        }
        
        if (_scriptClass == null) {
            log.warn("script has not initialized params._scriptClass");
        }
    }
    
    public void setScript(String script) {
        assertListenersAreNotRegistered(log, "setScript");
        _scriptText = script;
        if (_scriptText != null) {
            loadScript();
        } else {
            _scriptClass = null;
        }
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
    public boolean evaluate() throws JmriException {
        if (_scriptClass != null) {
            return _scriptClass.evaluate();
        } else {
            return false;
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        if (_scriptClass != null) {
            return _scriptClass.getChild(index);
        } else {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    @Override
    public int getChildCount() {
        if (_scriptClass != null) {
            return _scriptClass.getChildCount();
        } else {
            return 0;
        }
    }

    @Override
    public String getShortDescription(@Nonnull Locale locale) {
        return Bundle.getMessage(locale, "Script_Short");
    }

    @Override
    public String getLongDescription(@Nonnull Locale locale) {
        return Bundle.getMessage(locale, "Script_Long");
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        if (_scriptClass != null) _scriptClass.setup();
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_scriptClass != null)) {
            _scriptClass.registerListeners();
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _scriptClass.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_scriptClass != null) _scriptClass.dispose();
    }
    
    
    public static class ScriptParams {
        
        public final AtomicReference<AbstractScriptDigitalExpression> _scriptClass
                = new AtomicReference<>();
        
        public final DigitalExpression _parentExpression;
        
        public ScriptParams(@Nonnull DigitalExpression parentExpression) {
            _parentExpression  = parentExpression;
        }
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionScript.class);
    
}
