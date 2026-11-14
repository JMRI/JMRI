package jmri.jmrit.trainrecord.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean.DisplayOptions;
import jmri.Path;
import jmri.TrainMakeupSource;
import jmri.TrainRecord;
import jmri.TrainRecord.TrailEntry;
import jmri.TrainRecordManager;
import jmri.jmrit.trainrecord.PermissionsTrainTracking;
import jmri.jmrit.trainrecord.TrainCorrections;
import jmri.jmrit.trainrecord.TrainRecordReasons;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * The Block dialog: the trains a Block holds, how sure JMRI is of each, why,
 * and what has happened in the Block. It is opened from the popup menu of a
 * Block's track on a Layout Editor panel in operating mode, and from the
 * popup menu of a Block Contents icon.
 * <p>
 * It only shows. "Edit Value..." edits the Block's value, as a double-click on
 * a Block Contents icon does.
 * <p>
 * One dialog is kept per Block: opening it again brings it to the front.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class BlockDialog extends JmriJFrame {

    private static final Map<Block, BlockDialog> OPEN = new HashMap<>();

    private final Block block;
    private final boolean valueEditable;
    private final boolean controls;
    private final JComboBox<Choice> trainHere = new JComboBox<>();
    private final JButton confirm = new JButton(Bundle.getMessage("BlockDialogConfirm"));
    private final JButton clear = new JButton(Bundle.getMessage("BlockDialogClear"));
    private final JPanel holds = new JPanel();
    private final JTextArea why = new JTextArea(4, 40);
    private final HistoryModel historyModel = new HistoryModel();
    private final List<TrainRecord> listenedTo = new ArrayList<>();
    private final PropertyChangeListener changed = e -> refresh();
    private final PropertyChangeListener recordsChanged = this::recordsChanged;

    /**
     * Open the dialog for a Block, or bring it to the front if it is open.
     *
     * @param block         the Block
     * @param valueEditable false if the Block's value isn't to be edited
     *                      from here, as for a Block Contents icon whose
     *                      value edit is disabled
     * @return the dialog
     */
    @Nonnull
    public static BlockDialog open(@Nonnull Block block, boolean valueEditable) {
        return open(block, valueEditable, true);
    }

    /**
     * Open the dialog for a Block, or bring it to the front if it is open.
     *
     * @param block         the Block
     * @param valueEditable false if the Block's value isn't to be edited
     *                      from here, as for a Block Contents icon whose
     *                      value edit is disabled
     * @param controls      false if it is opened from a panel or icon that
     *                      doesn't control the layout: it then only shows,
     *                      and offers no corrections
     * @return the dialog
     */
    @Nonnull
    public static BlockDialog open(@Nonnull Block block, boolean valueEditable, boolean controls) {
        BlockDialog d = OPEN.get(block);
        if (d != null && d.controls != controls) {
            d.dispose();
            d = null;
        }
        if (d == null) {
            d = new BlockDialog(block, valueEditable, controls);
            OPEN.put(block, d);
            d.initComponents();
            d.pack();
            d.setVisible(true);
        } else {
            d.setVisible(true);
            d.toFront();
        }
        return d;
    }

    /**
     * @param block a Block
     * @return the text of the menu item that opens its dialog
     */
    @Nonnull
    public static String menuItemText(@Nonnull Block block) {
        return Bundle.getMessage("MenuItemBlockDialog", block.getDisplayName());
    }

    BlockDialog(@Nonnull Block block, boolean valueEditable, boolean controls) {
        super(Bundle.getMessage("BlockDialogTitle", block.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME)),
                true, true);
        this.block = block;
        this.valueEditable = valueEditable;
        this.controls = controls;
    }

    /** {@inheritDoc} */
    @Override
    public void initComponents() {
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        main.add(heading(Bundle.getMessage("BlockDialogHolds")));
        holds.setLayout(new BoxLayout(holds, BoxLayout.Y_AXIS));
        holds.setAlignmentX(LEFT_ALIGNMENT);
        main.add(holds);
        main.add(Box.createVerticalStrut(8));

        main.add(heading(Bundle.getMessage("BlockDialogWhy")));
        why.setEditable(false);
        why.setLineWrap(true);
        why.setWrapStyleWord(true);
        JScrollPane whyPane = new JScrollPane(why);
        whyPane.setAlignmentX(LEFT_ALIGNMENT);
        main.add(whyPane);
        main.add(Box.createVerticalStrut(8));

        main.add(heading(Bundle.getMessage("BlockDialogHistory", TrainRecordManager.BLOCK_HISTORY_LENGTH)));
        JTable table = new JTable(historyModel);
        table.setAutoCreateRowSorter(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(420);
        JScrollPane historyPane = new JScrollPane(table);
        historyPane.setPreferredSize(new java.awt.Dimension(610, 160));
        historyPane.setAlignmentX(LEFT_ALIGNMENT);
        main.add(historyPane);
        main.add(Box.createVerticalStrut(8));

        main.add(heading(Bundle.getMessage("BlockDialogTrainHere")));
        trainHere.setAlignmentX(LEFT_ALIGNMENT);
        trainHere.setToolTipText(Bundle.getMessage("BlockDialogTrainHereHint"));
        main.add(trainHere);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        clear.setToolTipText(Bundle.getMessage("BlockDialogClearHint"));
        clear.addActionListener(e -> jmri.util.ThreadingUtil.runOnLayout(() -> TrainCorrections.blockIsEmpty(block)));
        confirm.setToolTipText(Bundle.getMessage("BlockDialogConfirmHint"));
        confirm.addActionListener(e -> {
            Choice c = (Choice) trainHere.getSelectedItem();
            if (c != null) {
                jmri.util.ThreadingUtil.runOnLayout(() -> TrainCorrections.trainIsHere(c.record, block));
            }
        });
        boolean mayCorrect = controls && PermissionsTrainTracking.mayCorrect();
        trainHere.setEnabled(mayCorrect);
        confirm.setEnabled(mayCorrect);
        clear.setEnabled(mayCorrect);
        buttons.add(clear);
        buttons.add(confirm);
        JButton edit = new JButton(Bundle.getMessage("BlockDialogEditValue"));
        edit.setToolTipText(Bundle.getMessage("BlockDialogEditValueHint"));
        edit.setEnabled(valueEditable);
        edit.addActionListener(e -> editValue());
        JButton close = new JButton(Bundle.getMessage("BlockDialogClose"));
        close.addActionListener(e -> dispose());
        buttons.add(edit);
        buttons.add(close);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        addHelpMenu("package.jmri.jmrit.trainrecord.TrainTracking", true);

        block.addPropertyChangeListener(changed);
        if (InstanceManager.containsDefault(TrainRecordManager.class)) {
            InstanceManager.getDefault(TrainRecordManager.class)
                    .addPropertyChangeListener(TrainRecordManager.PROPERTY_RECORDS, recordsChanged);
        }
        refresh();
    }

    private static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /**
     * @return the Block shown
     */
    @Nonnull
    public Block getBlock() {
        return block;
    }

    private void recordsChanged(PropertyChangeEvent e) {
        refresh();
    }

    /**
     * Show the Block as it is now, and listen to every train, so one that
     * enters it or is placed in it is shown.
     */
    final void refresh() {
        jmri.util.ThreadingUtil.runOnGUIEventually(this::refreshNow);
    }

    private void refreshNow() {
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        List<TrainRecord> trains = manager.getRecordsIn(block);
        // listen to every train, so one placed in or entering the Block is shown
        for (TrainRecord r : listenedTo) {
            r.removePropertyChangeListener(changed);
        }
        listenedTo.clear();
        listenedTo.addAll(manager.getRecords());
        for (TrainRecord r : listenedTo) {
            r.addPropertyChangeListener(changed);
        }

        holds.removeAll();
        StringBuilder whyText = new StringBuilder();
        if (trains.isEmpty()) {
            holds.add(new JLabel(Bundle.getMessage("BlockDialogNoTrain")));
            whyText.append(Bundle.getMessage("BlockDialogNoTrainWhy"));
        }
        for (TrainRecord r : trains) {
            JLabel name = new JLabel(r.getName() + "    "
                    + TrainRecordReasons.describe(r.getConfidence()));
            name.setFont(name.getFont().deriveFont(java.awt.Font.BOLD, name.getFont().getSize2D() * 1.3f));
            holds.add(name);
            String position = position(r);
            if (!position.isEmpty()) {
                holds.add(new JLabel(position));
            }
            String work = work(r);
            if (!work.isEmpty()) {
                holds.add(new JLabel(work));
            }
            if (whyText.length() > 0) {
                whyText.append("\n");
            }
            whyText.append(TrainRecordReasons.why(r, block));
        }
        why.setText(whyText.toString());
        why.setCaretPosition(0);
        historyModel.setEntries(manager.getHistory(block));
        Choice selected = (Choice) trainHere.getSelectedItem();
        trainHere.removeAllItems();
        for (TrainRecord r : trains) {
            trainHere.addItem(new Choice(r, Bundle.getMessage("BlockDialogChoiceHere", r.getName())));
        }
        for (TrainRecord r : TrainCorrections.suggestions(block)) {
            trainHere.addItem(new Choice(r, TrainCorrections.isNextTo(r, block)
                    ? Bundle.getMessage("BlockDialogChoiceNextDoor", r.getName()) : r.getName()));
        }
        if (selected != null) {
            for (int i = 0; i < trainHere.getItemCount(); i++) {
                if (trainHere.getItemAt(i).record == selected.record) {
                    trainHere.setSelectedIndex(i);
                }
            }
        }
        holds.revalidate();
        holds.repaint();
    }

    /**
     * Where the train is heading from this Block, if that is known.
     */
    /**
     * @param r a train record
     * @return the train's work at its next stop, from the source that knows
     *         what it is made up of (Operations, say), or "" if none is known
     */
    @Nonnull
    static String work(@Nonnull TrainRecord r) {
        TrainMakeupSource.TrainMakeup m = TrainMakeupSource.find(r.getName());
        if (m == null || m.getWork().isEmpty()) {
            return "";
        }
        TrainMakeupSource.Work w = m.getWork().get(0);
        return Bundle.getMessage("BlockDialogNextStopWork", w.getLocation(), w.getSetOuts(), w.getPickUps());
    }

    private String position(TrainRecord r) {
        String towards = towards(r);
        return towards == null ? "" : Bundle.getMessage("BlockDialogHeadingTowards", towards);
    }

    /**
     * @return the one Block the train's leading end can go on to from this
     *         Block along paths that are set, or null
     */
    @CheckForNull
    private String towards(TrainRecord r) {
        List<TrailEntry> trail = r.getTrail();
        if (trail.isEmpty() || r.getLeadingEnd() == null) {
            return null;
        }
        TrailEntry last = trail.get(trail.size() - 1);
        if (last.getBlock() != block || last.getFrom() == null) {
            return null;
        }
        Block next = null;
        for (Path p : block.getPaths()) {
            Block b = p.getBlock();
            if (b == null || b == block || b == last.getFrom() || !p.checkPathSet()) {
                continue;
            }
            if (next != null && next != b) {
                return null;
            }
            next = b;
        }
        return next == null ? null : next.getDisplayName();
    }

    private void editValue() {
        Object value = block.getValue();
        String reply = JmriJOptionPane.showInputDialog(this,
                Bundle.getMessage("BlockDialogEditValuePrompt", block.getDisplayName()),
                value == null ? "" : value.toString());
        if (reply != null) {
            block.setValue(reply);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        OPEN.remove(block);
        block.removePropertyChangeListener(changed);
        for (TrainRecord r : listenedTo) {
            r.removePropertyChangeListener(changed);
        }
        listenedTo.clear();
        if (InstanceManager.containsDefault(TrainRecordManager.class)) {
            InstanceManager.getDefault(TrainRecordManager.class)
                    .removePropertyChangeListener(TrainRecordManager.PROPERTY_RECORDS, recordsChanged);
        }
        super.dispose();
    }

    /**
     * A train offered in "Train here".
     */
    private static final class Choice {

        private final TrainRecord record;
        private final String text;

        Choice(TrainRecord record, String text) {
            this.record = record;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private static final class HistoryModel extends AbstractTableModel {

        private List<TrainRecordManager.BlockEvent> entries = new ArrayList<>();

        void setEntries(List<TrainRecordManager.BlockEvent> entries) {
            this.entries = entries;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return Bundle.getMessage("BlockDialogTime");
                case 1:
                    return Bundle.getMessage("BlockDialogTrain");
                default:
                    return Bundle.getMessage("BlockDialogWhat");
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            TrainRecordManager.BlockEvent e = entries.get(row);
            switch (column) {
                case 0:
                    return TrainRecordReasons.time(e.getReason().getTime());
                case 1:
                    return e.getTrain();
                default:
                    return TrainRecordReasons.describe(e.getReason());
            }
        }
    }
}
