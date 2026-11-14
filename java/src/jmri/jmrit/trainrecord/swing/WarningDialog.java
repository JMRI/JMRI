package jmri.jmrit.trainrecord.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.UserPreferencesManager;
import jmri.jmrit.trainrecord.TrainRecordReasons;
import jmri.jmrit.trainrecord.TrainTrackingWarnings;
import jmri.jmrit.trainrecord.TrainTrackingWarnings.Warning;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;

/**
 * A window for one train tracking warning, with its answers as buttons.
 * Train tracking only warns: no train is stopped. The window closes by
 * itself when what caused the warning is over.
 * <p>
 * {@link #start()} opens one for each new warning about a train that has
 * stalled or is lost, unless that is turned off in Preferences &gt; Messages.
 * Occupancy that no train explains opens no window: it is listed in
 * Tools &gt; Train Tracking &gt; Warnings, where its answers are, since a
 * train JMRI isn't following, or a parked train on a detector that drops out,
 * would otherwise open one at every Block.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class WarningDialog extends JmriJFrame {

    /**
     * The preference item, in Preferences &gt; Messages, that stops a window
     * opening for a stalled or lost train.
     */
    public static final String HIDE_WINDOWS = "hideWarningWindows"; // NOI18N

    private static final Map<Warning, WarningDialog> OPEN = new HashMap<>();
    private static PropertyChangeListener notifier = null;
    private static TrainTrackingWarnings notifierSource = null;

    private final transient Warning warning;
    private final transient PropertyChangeListener warningsChanged = e -> closeIfOver();

    /**
     * Open a window for each new train tracking warning from now on. Calling
     * it again does nothing more.
     */
    public static void start() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }
        InstanceManager.getOptionalDefault(UserPreferencesManager.class)
                .ifPresent(p -> p.setClassDescription(Preferences.class.getName()));
        // done now, not queued: queued work could run after its caller has
        // gone, and make the warnings in the next InstanceManager
        ThreadingUtil.runOnGUI(() -> {
            TrainTrackingWarnings warnings = InstanceManager.getDefault(TrainTrackingWarnings.class);
            if (notifier != null && notifierSource == warnings) {
                return;
            }
            if (notifier != null) {
                notifierSource.removePropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, notifier);
                SEEN.clear();
            }
            notifierSource = warnings;
            notifier = e -> ThreadingUtil.runOnGUIEventually(() -> {
                for (Warning w : warnings.getWarnings()) {
                    if (!OPEN.containsKey(w) && !SEEN.containsKey(w)) {
                        SEEN.put(w, Boolean.TRUE);
                        if (opensWindow(w)) {
                            open(w);
                        }
                    }
                }
                SEEN.keySet().retainAll(warnings.getWarnings());
            });
            warnings.addPropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, notifier);
        });
    }

    private static final Map<Warning, Boolean> SEEN = new HashMap<>();

    /**
     * Whether a new warning opens a window by itself.
     *
     * @param w the warning
     * @return true for a stalled or lost train, unless windows are turned off
     *         in Preferences &gt; Messages; false for unexplained occupancy
     */
    static boolean opensWindow(@Nonnull Warning w) {
        if (w.getKind() == Warning.Kind.UNEXPLAINED) {
            return false;
        }
        UserPreferencesManager p = InstanceManager.getNullableDefault(UserPreferencesManager.class);
        return p == null || !p.getPreferenceState(Preferences.class.getName(), HIDE_WINDOWS);
    }

    /**
     * Open the window for a warning, or bring it to the front.
     *
     * @param w the warning
     * @return the window
     */
    @Nonnull
    public static WarningDialog open(@Nonnull Warning w) {
        WarningDialog d = OPEN.get(w);
        if (d == null) {
            d = new WarningDialog(w);
            OPEN.put(w, d);
            d.initComponents();
            d.pack();
            d.setVisible(true);
        } else {
            d.toFront();
        }
        return d;
    }

    WarningDialog(@Nonnull Warning warning) {
        super(Bundle.getMessage("WarningDialogTitle"), false, false);
        this.warning = warning;
    }

    /** {@inheritDoc} */
    @Override
    public void initComponents() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel title = new JLabel(warning.getTitle());
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, title.getFont().getSize2D() * 1.25f));
        title.setAlignmentX(LEFT_ALIGNMENT);
        main.add(title);
        JLabel time = new JLabel(TrainRecordReasons.time(warning.getTime()));
        time.setAlignmentX(LEFT_ALIGNMENT);
        main.add(time);
        main.add(Box.createVerticalStrut(8));
        TrainRecord r = warning.getRecord();
        main.add(text(Bundle.getMessage("Warning" + warning.getKind().name() + "Text",
                warning.getBlock().getDisplayName(), r == null ? "" : r.getName())));
        main.add(Box.createVerticalStrut(6));
        main.add(text(Bundle.getMessage("WarningTrains")));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        if (warning.getKind() == Warning.Kind.UNEXPLAINED) {
            JButton stuck = new JButton(Bundle.getMessage("WarningDetectorStuck"));
            stuck.setToolTipText(Bundle.getMessage("WarningDetectorStuckHint"));
            stuck.addActionListener(e -> {
                InstanceManager.getDefault(TrainTrackingWarnings.class).detectorStuck(warning.getBlock());
                dispose();
            });
            buttons.add(stuck);
        }
        JButton show = new JButton(Bundle.getMessage("WarningShowMe"));
        show.setToolTipText(Bundle.getMessage("WarningShowMeHint"));
        show.addActionListener(e -> BlockDialog.open(warning.getBlock(), true));
        buttons.add(show);
        JButton later = new JButton(Bundle.getMessage("WarningLater"));
        later.setToolTipText(Bundle.getMessage("WarningLaterHint"));
        later.addActionListener(e -> dispose());
        buttons.add(later);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        InstanceManager.getDefault(TrainTrackingWarnings.class)
                .addPropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, warningsChanged);
    }

    private static JTextArea text(String s) {
        JTextArea t = new JTextArea(s, 0, 40);
        t.setEditable(false);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setOpaque(false);
        t.setAlignmentX(LEFT_ALIGNMENT);
        // give it its width before the window is packed, so its height is that of the wrapped lines
        t.setSize(new java.awt.Dimension(t.getPreferredSize().width, Short.MAX_VALUE));
        return t;
    }

    private void closeIfOver() {
        ThreadingUtil.runOnGUIEventually(() -> {
            if (!InstanceManager.getDefault(TrainTrackingWarnings.class).getWarnings().contains(warning)) {
                dispose();
            }
        });
    }

    /**
     * @return the warning shown
     */
    @Nonnull
    public Warning getWarning() {
        return warning;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        OPEN.remove(warning);
        InstanceManager.getDefault(TrainTrackingWarnings.class)
                .removePropertyChangeListener(TrainTrackingWarnings.PROPERTY_WARNINGS, warningsChanged);
        super.dispose();
    }

    /**
     * The warnings' entry in Preferences &gt; Messages. JMRI's user
     * preferences find it by name.
     */
    public static class Preferences {

        /**
         * A constructor without parameters, which the user preferences
         * manager needs.
         */
        public Preferences() {
            // nothing to set up
        }

        /**
         * @return the heading in Preferences &gt; Messages
         */
        public String getClassDescription() {
            return Bundle.getMessage("WarningPreferences");
        }

        /**
         * Name the warnings' one setting.
         */
        public void setMessagePreferencesDetails() {
            InstanceManager.getDefault(UserPreferencesManager.class).setPreferenceItemDetails(
                    Preferences.class.getName(), HIDE_WINDOWS, Bundle.getMessage("WarningHideWindows"));
        }
    }
}
