package jmri.jmrit.display.logixng;

// import java.beans.PropertyChangeEvent;
// import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action brings a JFrame to front.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class WindowToFront extends AbstractDigitalAction implements VetoableChangeListener {

    private String _jmriJFrameTitle;
    private JmriJFrame _jmriJFrame;
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    public WindowToFront(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        WindowToFront copy = new WindowToFront(sysName, userName);
        copy.setComment(getComment());
        copy.setJmriJFrame(_jmriJFrameTitle);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        return manager.registerAction(copy);
    }

    public void setJmriJFrame(@CheckForNull String jmriJFrameTitle) {
        assertListenersAreNotRegistered(log, "setJmriJFrame");
        _jmriJFrameTitle = jmriJFrameTitle;
        _jmriJFrame = null;
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public void setJmriJFrame(@CheckForNull JmriJFrame jmriJFrame) {
        assertListenersAreNotRegistered(log, "setJmriJFrame");
        _jmriJFrame = jmriJFrame;
        _jmriJFrameTitle = jmriJFrame != null ? jmriJFrame.getTitle() : "";
//        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public JmriJFrame getJmriJFrame() {
        return _jmriJFrame;
    }

    public String getJmriJFrameTitle() {
        return _jmriJFrameTitle;
    }

    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
/*
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Turnout_TurnoutInUseTurnoutExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Turnout) {
                if (evt.getOldValue().equals(getTurnout().getBean())) {
                    removeTurnout();
                }
            }
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return CategoryDisplay.DISPLAY;
    }

    private void throwErrorJmriJFrameDoesNotExists() throws JmriException {
        var lng = getConditionalNG();
        var cng = getConditionalNG();
        var m = getModule();
        String errorMessage;
        if (m != null) {
            errorMessage = Bundle.getMessage(
                    "WindowToFront_ErrorNoJmriJFrame_Module",
                    getLongDescription(), m.getDisplayName(), getSystemName());
        } else {
            errorMessage = Bundle.getMessage(
                    "WindowToFront_ErrorNoJmriJFrame_LogixNG",
                    getLongDescription(), lng.getDisplayName(), cng.getDisplayName(), getSystemName());
        }
        List<String> list = Arrays.asList(errorMessage.split("\n"));
        throw new JmriException(Bundle.getMessage("WindowToFront_ErrorNoJmriJFrame"), list);
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        JmriJFrame jmriJFrame;

//        System.out.format("WindowToFront.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                jmriJFrame = this._jmriJFrame;
                if (jmriJFrame == null && (_jmriJFrameTitle != null && !_jmriJFrameTitle.isBlank())) {
                    jmriJFrame = JmriJFrame.getFrame(_jmriJFrameTitle);
                    if (jmriJFrame == null) {
                        log.error("ddd");
                        throwErrorJmriJFrameDoesNotExists();
                    }
                }
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                jmriJFrame = JmriJFrame.getFrame(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                jmriJFrame = JmriJFrame.getFrame(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                jmriJFrame = _expressionNode != null ?
                        JmriJFrame.getFrame(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("WindowToFront.execute: positionable: %s%n", positionable);

        if (jmriJFrame == null) {
            log.error("Frame is null");
            return;
        }

        JmriJFrame frame = jmriJFrame;
        ThreadingUtil.runOnGUI(() -> {
            frame.toFront();
        });
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
        return Bundle.getMessage(locale, "WindowToFront_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String jmriJFrameName;

        switch (_addressing) {
            case Direct:
                if (this._jmriJFrameTitle != null) {
                    jmriJFrameName = this._jmriJFrameTitle;
                } else {
                    jmriJFrameName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                jmriJFrameName = Bundle.getMessage(locale, "AddressByDirect", jmriJFrameName);
                break;

            case Reference:
                jmriJFrameName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                jmriJFrameName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                jmriJFrameName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        return Bundle.getMessage(locale, "WindowToFront_Long", jmriJFrameName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        if ((_jmriJFrameTitle != null) && (_jmriJFrame == null)) {
            setJmriJFrame(_jmriJFrameTitle);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowToFront.class);

}
