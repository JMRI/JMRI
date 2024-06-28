package jmri.jmrit.logixng.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * This action decodes a Json string to a JsonNode.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class JsonDecode extends AbstractDigitalAction
        implements PropertyChangeListener {

    private String _jsonLocalVariable;
    private String _resultLocalVariable;


    public JsonDecode(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        JsonDecode copy = new JsonDecode(sysName, userName);
        copy.setComment(getComment());
        copy.setJsonLocalVariable(_jsonLocalVariable);
        copy.setResultLocalVariable(_resultLocalVariable);
        return manager.registerAction(copy);
    }

    public void setJsonLocalVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setJsonLocalVariable");   // No I18N
        _jsonLocalVariable = variableName;
    }

    public String getJsonLocalVariable() {
        return _jsonLocalVariable;
    }

    public void setResultLocalVariable(String variableName) {
        assertListenersAreNotRegistered(log, "setResultLocalVariable");   // No I18N
        _resultLocalVariable = variableName;
    }

    public String getResultLocalVariable() {
        return _resultLocalVariable;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (_jsonLocalVariable == null) return;
        if (_resultLocalVariable == null) return;

        final ConditionalNG conditionalNG = getConditionalNG();

        SymbolTable symbolTable = conditionalNG.getSymbolTable();

        String json = TypeConversionUtil.convertToString(
                symbolTable.getValue(_jsonLocalVariable), false);

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jsonNode = om.readTree(json);
            symbolTable.setValue(_resultLocalVariable, jsonNode);
        } catch (JsonProcessingException ex) {
            throw new JmriException(ex);
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
        return Bundle.getMessage(locale, "JsonDecode_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "JsonDecode_Long", _jsonLocalVariable, _resultLocalVariable);
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
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /*.* {@inheritDoc} *./
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: JsonDecode: bean = {}, report = {}", cdl, report);
    }
*/
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonDecode.class);

}
