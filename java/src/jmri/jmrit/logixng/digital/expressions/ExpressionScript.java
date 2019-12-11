package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.DigitalExpression;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.script.JmriScriptEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates a script.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class ExpressionScript extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private String _scriptText;
    private AbstractScriptDigitalExpression _scriptClass;
    private boolean _listenersAreRegistered = false;

    public ExpressionScript(@Nonnull String sys, @CheckForNull String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    private void loadScript() {
        try {
            jmri.script.JmriScriptEngineManager scriptEngineManager = jmri.script.JmriScriptEngineManager.getDefault();

            Bindings bindings = new SimpleBindings();
            ScriptParams params = new ScriptParams(this);
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
            log.error("script has not initialized params._scriptClass");
        }
    }
    
    public void setScript(@Nonnull String script) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setScript must not be called when listeners are registered");
            log.error("setScript must not be called when listeners are registered", e);
            throw e;
        }
        _scriptText = script;
        loadScript();
    }
    
    public String getScriptText() {
        return _scriptText;
    }
    
    @Override
    public void vetoableChange(@Nonnull java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        _scriptClass.vetoableChange(evt);
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
    public boolean evaluate() {
        return _scriptClass.evaluate();
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        _scriptClass.setup();
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
    public void propertyChange(@Nonnull PropertyChangeEvent evt) {
        getConditionalNG().execute();
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
    
    
    private final static Logger log = LoggerFactory.getLogger(ExpressionScript.class);
    
}
