package jmri.jmrit.display.logixng;

// import java.beans.PropertyChangeEvent;
// import java.beans.PropertyVetoException;
import java.awt.Frame;
import java.beans.*;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action acts on a Window.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class WindowManagement extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private String _jmriJFrameTitle;
    private JmriJFrame _jmriJFrame;
    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private boolean _ignoreWindowNotFound = false;

    private final LogixNG_SelectEnum<HideOrShow> _selectEnumHideOrShow =
            new LogixNG_SelectEnum<>(this, HideOrShow.values(), HideOrShow.DoNothing, this);

    private final LogixNG_SelectEnum<MaximizeMinimizeNormalize> _selectEnumMaximizeMinimizeNormalize =
            new LogixNG_SelectEnum<>(this, MaximizeMinimizeNormalize.values(), MaximizeMinimizeNormalize.DoNothing, this);

    private final LogixNG_SelectEnum<BringToFrontOrBack> _selectEnumBringToFrontOrBack =
            new LogixNG_SelectEnum<>(this, BringToFrontOrBack.values(), BringToFrontOrBack.DoNothing, this);


    public WindowManagement(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        WindowManagement copy = new WindowManagement(sysName, userName);
        copy.setComment(getComment());
        copy.setJmriJFrame(_jmriJFrameTitle);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        copy._ignoreWindowNotFound = _ignoreWindowNotFound;
        _selectEnumHideOrShow.copy(copy._selectEnumHideOrShow);
        _selectEnumMaximizeMinimizeNormalize.copy(copy._selectEnumMaximizeMinimizeNormalize);
        _selectEnumBringToFrontOrBack.copy(copy._selectEnumBringToFrontOrBack);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<HideOrShow> getSelectEnumHideOrShow() {
        return _selectEnumHideOrShow;
    }

    public LogixNG_SelectEnum<MaximizeMinimizeNormalize> getSelectEnumMaximizeMinimizeNormalize() {
        return _selectEnumMaximizeMinimizeNormalize;
    }

    public LogixNG_SelectEnum<BringToFrontOrBack> getSelectEnumBringToFrontOrBack() {
        return _selectEnumBringToFrontOrBack;
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

    public void setIgnoreWindowNotFound(boolean ignoreWindowNotFound) {
        _ignoreWindowNotFound = ignoreWindowNotFound;
    }

    public boolean isIgnoreWindowNotFound() {
        return _ignoreWindowNotFound;
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
    public LogixNG_Category getCategory() {
        return CategoryDisplay.DISPLAY;
    }

    private void throwErrorJmriJFrameDoesNotExists() throws JmriException {
        var lng = getLogixNG();
        var cng = getConditionalNG();
        var m = getModule();
        String errorMessage;
        if (m != null) {
            errorMessage = Bundle.getMessage(
                    "WindowManagement_ErrorNoJmriJFrame_Module",
                    getLongDescription(), m.getDisplayName(), getSystemName());
        } else {
            errorMessage = Bundle.getMessage(
                    "WindowManagement_ErrorNoJmriJFrame_LogixNG",
                    getLongDescription(), lng.getDisplayName(), cng.getDisplayName(), getSystemName());
        }
        List<String> list = Arrays.asList(errorMessage.split("\n"));
        throw new JmriException(Bundle.getMessage("WindowManagement_ErrorNoJmriJFrame"), list);
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();

        JmriJFrame jmriJFrame;

//        System.out.format("WindowToFront.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                jmriJFrame = this._jmriJFrame;
                if (jmriJFrame == null && (_jmriJFrameTitle != null && !_jmriJFrameTitle.isBlank())) {
                    jmriJFrame = JmriJFrame.getFrame(_jmriJFrameTitle);
                    if (jmriJFrame == null) {
                        if (_ignoreWindowNotFound) {
                            log.debug("Window is not found");
                            return;
                        } else {
                            throwErrorJmriJFrameDoesNotExists();
                        }
                    }
                }
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _reference);
                jmriJFrame = JmriJFrame.getFrame(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                jmriJFrame = JmriJFrame.getFrame(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                jmriJFrame = _expressionNode != null ?
                        JmriJFrame.getFrame(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                conditionalNG.getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("WindowToFront.execute: positionable: %s%n", positionable);

        if (jmriJFrame == null) {
            log.error("Window is null");
            return;
        }

        HideOrShow hideOrShow =
                _selectEnumHideOrShow.evaluateEnum(conditionalNG);
        MaximizeMinimizeNormalize maximizeMinimizeNormalize =
                _selectEnumMaximizeMinimizeNormalize.evaluateEnum(conditionalNG);
        BringToFrontOrBack bringToFrontOrBack =
                _selectEnumBringToFrontOrBack.evaluateEnum(conditionalNG);

        JmriJFrame frame = jmriJFrame;

        ThreadingUtil.runOnGUI(() -> {
            hideOrShow.run(frame);
            maximizeMinimizeNormalize.run(frame);
            bringToFrontOrBack.run(frame);
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
        return Bundle.getMessage(locale, "WindowManagement_Short");
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

        List<Object> strings = new ArrayList<>();
        strings.add(jmriJFrameName);
        if (!_selectEnumHideOrShow.isEnum(HideOrShow.DoNothing)) {
            strings.add(_selectEnumHideOrShow.getDescription(locale));
        }
        if (!_selectEnumMaximizeMinimizeNormalize.isEnum(MaximizeMinimizeNormalize.DoNothing)) {
            strings.add(_selectEnumMaximizeMinimizeNormalize.getDescription(locale));
        }
        if (!_selectEnumBringToFrontOrBack.isEnum(BringToFrontOrBack.DoNothing)) {
            strings.add(_selectEnumBringToFrontOrBack.getDescription(locale));
        }

        if (_ignoreWindowNotFound) {
            strings.add(Bundle.getMessage("WindowManagement_IgnoreWindowNotFound_Descr",
                    Bundle.getMessage("WindowManagement_IgnoreWindowNotFound")));
        } else {
            strings.add("");
        }

        return Bundle.getMessage(locale, "WindowManagement_Long_"+Integer.toString(strings.size()),
                strings.toArray());
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
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
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

    private interface FrameAction {
        void run(JmriJFrame f);
    }

    public enum HideOrShow {
        DoNothing(Bundle.getMessage("WindowManagement_HideOrShow_DoNothing"), (f) -> {}),
        Show(Bundle.getMessage("WindowManagement_HideOrShow_Show"), (f) -> { f.setVisible(true); }),
        Hide(Bundle.getMessage("WindowManagement_HideOrShow_Hide"), (f) -> { f.setVisible(false); });

        private final String _text;
        private final FrameAction _action;

        private HideOrShow(String text, FrameAction action) {
            this._text = text;
            this._action = action;
        }

        public void run(JmriJFrame f) {
            _action.run(f);
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    public enum MaximizeMinimizeNormalize {
        DoNothing(Bundle.getMessage("WindowManagement_MaximizeMinimizeNormalize_DoNothing"), (f) -> {}),
        Minimize(Bundle.getMessage("WindowManagement_MaximizeMinimizeNormalize_Minimize"), (f) -> { f.setExtendedState(Frame.ICONIFIED); }),
        Normalize(Bundle.getMessage("WindowManagement_MaximizeMinimizeNormalize_Normalize"), (f) -> { f.setExtendedState(Frame.NORMAL); }),
        Maximize(Bundle.getMessage("WindowManagement_MaximizeMinimizeNormalize_Maximize"), (f) -> { f.setExtendedState(Frame.MAXIMIZED_BOTH); });

        private final String _text;
        private final FrameAction _action;

        private MaximizeMinimizeNormalize(String text, FrameAction action) {
            this._text = text;
            this._action = action;
        }

        public void run(JmriJFrame f) {
            _action.run(f);
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    public enum BringToFrontOrBack {
        DoNothing(Bundle.getMessage("WindowManagement_BringToFrontOrBack_DoNothing"), (f) -> {}),
        Front(Bundle.getMessage("WindowManagement_BringToFrontOrBack_Front"), (f) -> { f.toFront(); }),
        Back(Bundle.getMessage("WindowManagement_BringToFrontOrBack_Back"), (f) -> { f.toBack(); });

        private final String _text;
        private final FrameAction _action;

        private BringToFrontOrBack(String text, FrameAction action) {
            this._text = text;
            this._action = action;
        }

        public void run(JmriJFrame f) {
            _action.run(f);
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowManagement.class);

}
