package jmri.jmrit.logixng.actions.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSignalMastFollow;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionSignalMastFollow object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionSignalMastFollowSwing extends AbstractDigitalActionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private LogixNG_SelectNamedBeanSwing<SignalMast> _selectPrimaryMast;
    private LogixNG_SelectNamedBeanSwing<SignalMast> _selectSecondaryMast;
    private ActionSignalMastFollowTableModel _aspectMappingTableModel;
    private JCheckBox _followLitUnlitCheckBox;
    private JCheckBox _followHeldUnheldCheckBox;


    public ActionSignalMastFollowSwing() {
    }

    public ActionSignalMastFollowSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionSignalMastFollow action = (ActionSignalMastFollow)object;

        _selectPrimaryMast = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SignalMastManager.class), getJDialog(), this);

        _selectSecondaryMast = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(SignalMastManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel _tabbedPanePrimaryMast;
        JPanel _tabbedPaneSecondaryMast;

        if (action != null) {
            _tabbedPanePrimaryMast = _selectPrimaryMast.createPanel(action.getSelectPrimaryMast());
            _tabbedPaneSecondaryMast = _selectSecondaryMast.createPanel(action.getSelectSecondaryMast());
        } else {
            _tabbedPanePrimaryMast = _selectPrimaryMast.createPanel(null);
            _tabbedPaneSecondaryMast = _selectSecondaryMast.createPanel(null);
        }

        JPanel primaryMastPanel = new JPanel();
        JPanel secondaryMastPanel = new JPanel();

        primaryMastPanel.add(new JLabel(Bundle.getMessage("ActionSignalMastFollowSwing_PrimaryMast")));
        primaryMastPanel.add(_tabbedPanePrimaryMast);

        secondaryMastPanel.add(new JLabel(Bundle.getMessage("ActionSignalMastFollowSwing_SecondaryMast")));
        secondaryMastPanel.add(_tabbedPaneSecondaryMast);

        JTable table = new JTable();
        _aspectMappingTableModel = new ActionSignalMastFollowTableModel(
                _selectPrimaryMast.getBean(),
                _selectSecondaryMast.getBean(),
                action != null ? action.getAspectMap() : null);
        table.setModel(_aspectMappingTableModel);
        table.setDefaultEditor(String.class,
                new ActionSignalMastFollowTableModel.DestAspectCellEditor(_aspectMappingTableModel));
        _aspectMappingTableModel.setColumnsForComboBoxes(table);
        JScrollPane mappingScrollpane = new JScrollPane(table);
        mappingScrollpane.setPreferredSize(new Dimension(400, 200));

        _selectPrimaryMast.getBeanSelectPanel().getBeanCombo().addActionListener(
                (e)->{
                    _aspectMappingTableModel.setPrimaryMast(
                            _selectPrimaryMast.getBeanSelectPanel().getNamedBean());
                });

        _selectSecondaryMast.getBeanSelectPanel().getBeanCombo().addActionListener(
                (e)->{
                    _aspectMappingTableModel.setSecondaryMast(
                            _selectSecondaryMast.getBeanSelectPanel().getNamedBean());
                });

        _followLitUnlitCheckBox = new JCheckBox(Bundle.getMessage("ActionSignalMastFollowSwing_FollowLitUnlit"));
        _followHeldUnheldCheckBox = new JCheckBox(Bundle.getMessage("ActionSignalMastFollowSwing_FollowHeldUnheld"));

        if (action != null) {
            _followLitUnlitCheckBox.setSelected(action.getFollowLitUnlit());
            _followHeldUnheldCheckBox.setSelected(action.getFollowHeldUnheld());
        }

        panel.add(primaryMastPanel);
        panel.add(secondaryMastPanel);
        panel.add(mappingScrollpane);
        panel.add(_followLitUnlitCheckBox);
        panel.add(_followHeldUnheldCheckBox);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionSignalMastFollow action = new ActionSignalMastFollow("IQDA1", null);

        _selectPrimaryMast.validate(action.getSelectPrimaryMast(), errorMessages);
        _selectSecondaryMast.validate(action.getSelectSecondaryMast(), errorMessages);

        SignalMast mast1 = _selectPrimaryMast.getBean();
        SignalMast mast2 = _selectSecondaryMast.getBean();

        if (mast1 != null && mast1 == mast2) {
            errorMessages.add(Bundle.getMessage(
                    "ActionSignalMastFollowSwing_ErrorMustBeTwoDifferentMasts"));
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionSignalMastFollow action = new ActionSignalMastFollow(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionSignalMastFollow)) {
            throw new IllegalArgumentException("object must be an ActionSignalMastFollow but is a: "+object.getClass().getName());
        }
        ActionSignalMastFollow action = (ActionSignalMastFollow)object;

        _selectPrimaryMast.updateObject(action.getSelectPrimaryMast());
        _selectSecondaryMast.updateObject(action.getSelectSecondaryMast());
        action.getAspectMap().clear();
        action.getAspectMap().putAll(_aspectMappingTableModel.getAspectMapping());
        action.setFollowLitUnlit(_followLitUnlitCheckBox.isSelected());
        action.setFollowHeldUnheld(_followHeldUnheldCheckBox.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SignalMastFollow_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastSwing.class);

}
