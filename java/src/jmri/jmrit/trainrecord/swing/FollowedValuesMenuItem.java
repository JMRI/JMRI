package jmri.jmrit.trainrecord.swing;

import java.beans.PropertyChangeListener;

import javax.swing.JCheckBoxMenuItem;

import jmri.InstanceManager;
import jmri.TrainRecordManager;
import jmri.UserPreferencesManager;
import jmri.managers.DefaultTrainRecordManager;
import jmri.util.ThreadingUtil;

/**
 * The Train Tracking menu's check box for
 * {@link TrainRecordManager#isFollowedValuesUsed()}: whether a Block value
 * that the Block passed along itself, from a neighbour as a train entered,
 * places a train or says which train entered a Block. Off unless turned on.
 * <p>
 * Making the menu doesn't start train tracking: the setting is read from the
 * user's preferences, and the {@link TrainRecordManager} is only used once
 * the check box is changed, or if it is already running.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class FollowedValuesMenuItem extends JCheckBoxMenuItem {

    private final transient PropertyChangeListener listener = e ->
            ThreadingUtil.runOnGUIEventually(this::showSetting);
    private transient TrainRecordManager listenedTo = null;

    /**
     * Make the check box.
     */
    public FollowedValuesMenuItem() {
        super(Bundle.getMessage("MenuItemFollowedValues"));
        setToolTipText(Bundle.getMessage("MenuItemFollowedValuesHint"));
        showSetting();
        addActionListener(e -> {
            boolean use = isSelected();
            ThreadingUtil.runOnLayout(() -> InstanceManager.getDefault(TrainRecordManager.class)
                    .setFollowedValuesUsed(use));
            listen();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void addNotify() {
        super.addNotify();
        listen();
        showSetting();
    }

    /** {@inheritDoc} */
    @Override
    public void removeNotify() {
        if (listenedTo != null) {
            listenedTo.removePropertyChangeListener(TrainRecordManager.PROPERTY_FOLLOWED_VALUES_USED, listener);
            listenedTo = null;
        }
        super.removeNotify();
    }

    /**
     * Follow changes made elsewhere, if train tracking is running.
     */
    private void listen() {
        if (listenedTo == null && InstanceManager.containsDefault(TrainRecordManager.class)) {
            listenedTo = InstanceManager.getDefault(TrainRecordManager.class);
            listenedTo.addPropertyChangeListener(TrainRecordManager.PROPERTY_FOLLOWED_VALUES_USED, listener);
        }
    }

    private void showSetting() {
        setSelected(InstanceManager.getOptionalDefault(UserPreferencesManager.class)
                .map(p -> p.getSimplePreferenceState(DefaultTrainRecordManager.FOLLOWED_VALUES_PREFERENCE))
                .orElse(false));
    }
}
