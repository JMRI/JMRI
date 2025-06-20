package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.Block;
import jmri.Sensor;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * This action triggers a block.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionBlock extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Block> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Block.class, InstanceManager.getDefault(BlockManager.class), this);

    private final LogixNG_SelectEnum<DirectOperation> _selectEnum =
            new LogixNG_SelectEnum<>(this, DirectOperation.values(), DirectOperation.SetOccupied, this);

    private final LogixNG_SelectString _selectBlockValue =
            new LogixNG_SelectString(this, this);


    public ActionBlock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionBlock copy = new ActionBlock(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        _selectBlockValue.copy(copy._selectBlockValue);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Block> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<DirectOperation> getSelectEnum() {
        return _selectEnum;
    }

    public LogixNG_SelectString getSelectBlockValue() {
        return _selectBlockValue;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();

        Block block = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (block == null) {
            return;
        }

        DirectOperation oper = _selectEnum.evaluateEnum(getConditionalNG());

        // Variables used in lambda must be effectively final
        DirectOperation theOper = oper;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            Sensor sensor;
            LayoutBlock lblk;

            switch (theOper) {
                case SetOccupied:
                    sensor = block.getSensor();
                    if (sensor != null) {
                        try {
                            sensor.setKnownState(Sensor.ACTIVE);
                        } catch (JmriException ex) {
                            log.debug("Exception setting sensor active");
                        }
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorSensor", block.getDisplayName()));
                    }
                    break;
                case SetNotOccupied:
                    sensor = block.getSensor();
                    if (sensor != null) {
                        try {
                            sensor.setKnownState(Sensor.INACTIVE);
                        } catch (JmriException ex) {
                            log.debug("Exception setting sensor inactive");
                        }
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorSensor", block.getDisplayName()));
                    }
                    break;
                case SetAltColorOn:
                    lblk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);
                    if (lblk != null) {
                        lblk.setUseExtraColor(true);
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorLayoutBlock", block.getDisplayName()));
                    }
                    break;
                case SetAltColorOff:
                    lblk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);
                    if (lblk != null) {
                        lblk.setUseExtraColor(false);
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorLayoutBlock", block.getDisplayName()));
                    }
                    break;
                case SetNullValue:
                    block.setValue(null);
                    break;
                case SetValue:
                    block.setValue(_selectBlockValue.evaluateValue(conditionalNG));
                    break;
                default:
                    throw new IllegalArgumentException("invalid oper state: " + theOper.name());
            }
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
        return Bundle.getMessage(locale, "ActionBlock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        if (_selectEnum.isDirectAddressing()) {
            if (_selectEnum.getEnum() == DirectOperation.SetValue) {
                String bundleKey = "ActionBlock_Long_Value";
                return Bundle.getMessage(locale, bundleKey, namedBean, _selectBlockValue.getDescription(locale));
            }
        }

        return Bundle.getMessage(locale, "ActionBlock_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectNamedBean.registerListeners();
        _selectEnum.registerListeners();
        _selectBlockValue.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
        _selectBlockValue.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum DirectOperation {
        SetOccupied(Bundle.getMessage("ActionBlock_SetOccupied")),
        SetNotOccupied(Bundle.getMessage("ActionBlock_SetNotOccupied")),
        SetAltColorOn(Bundle.getMessage("ActionBlock_SetAltColorOn")),
        SetAltColorOff(Bundle.getMessage("ActionBlock_SetAltColorOff")),
        SetNullValue(Bundle.getMessage("ActionBlock_SetNullValue")),
        SetValue(Bundle.getMessage("ActionBlock_SetValue"));

        private final String _text;

        private DirectOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlock.class);

}
