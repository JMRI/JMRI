package jmri.jmrit.trainrecord.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.NamedBean;
import jmri.TrainRecord;
import jmri.jmrit.trainrecord.TrainTrackingChecks;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.util.JmriJFrame;

/**
 * Lists the set-up problems that make following trains less sure, as
 * {@link TrainTrackingChecks} finds them: one row for each, naming the train,
 * Block or sensor it is about and saying what is wrong in the user's terms.
 * The checks run when the window opens and again on Check Again; they only
 * read, and nothing is changed.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public class SetUpChecksFrame extends JmriJFrame {

    private final FindingsModel model = new FindingsModel();
    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel();

    /**
     * The menu item that opens the window.
     */
    public static class Action extends AbstractAction {

        /**
         * Make the menu item.
         */
        public Action() {
            super(Bundle.getMessage("MenuItemSetUpChecks"));
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            SetUpChecksFrame f = InstanceManager.getOptionalDefault(SetUpChecksFrame.class).orElse(null);
            if (f == null || !f.isDisplayable()) {
                f = new SetUpChecksFrame();
                InstanceManager.setDefault(SetUpChecksFrame.class, f);
                f.initComponents();
                f.pack();
            } else {
                f.check();
            }
            f.setVisible(true);
            f.toFront();
        }
    }

    /**
     * @return the text of the Train Tracking menu, which holds the menu item
     *         for this window
     */
    @Nonnull
    public static String menuText() {
        return Bundle.getMessage("MenuTrainTracking");
    }

    /**
     * Make the window. Call {@link #initComponents()} to fill it.
     */
    public SetUpChecksFrame() {
        super(Bundle.getMessage("SetUpChecksTitle"));
    }

    @Override
    @InvokeOnGuiThread
    public void initComponents() {
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(650);
        table.getColumnModel().getColumn(1).setCellRenderer(new WholeTextRenderer());
        JButton again = new JButton(Bundle.getMessage("SetUpChecksAgain"));
        again.addActionListener(e -> check());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(summary);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(again);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        getContentPane().add(p);
        check();
    }

    /**
     * Run the checks again and show what they find.
     */
    @InvokeOnGuiThread
    public void check() {
        List<Finding> found = TrainTrackingChecks.run();
        model.setFindings(found);
        summary.setText(found.isEmpty()
                ? Bundle.getMessage("SetUpChecksNone")
                : Bundle.getMessage("SetUpChecksFound", found.size()));
    }

    /**
     * @return the table of findings, for tests
     */
    JTable getTable() {
        return table;
    }

    /**
     * @return the line above the table, for tests
     */
    String getSummary() {
        return summary.getText();
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(SetUpChecksFrame.class).ifPresent(f -> {
            if (f == this) {
                InstanceManager.deregister(this, SetUpChecksFrame.class);
            }
        });
        super.dispose();
    }

    /**
     * Shows the whole of a long finding as the cell's tool tip.
     */
    private static final class WholeTextRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            super.setValue(value);
            setToolTipText(value == null ? null : value.toString());
        }
    }

    private static final class FindingsModel extends AbstractTableModel {

        private List<Finding> findings = new ArrayList<>();

        void setFindings(@Nonnull List<Finding> findings) {
            this.findings = new ArrayList<>(findings);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return findings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int col) {
            return Bundle.getMessage(col == 0 ? "SetUpChecksColumnFor" : "SetUpChecksColumnFinding");
        }

        @Override
        public Object getValueAt(int row, int col) {
            Finding f = findings.get(row);
            if (col == 1) {
                return f.getMessage();
            }
            // a finding is about a train record or a named bean
            Object s = f.getSubject();
            return s instanceof TrainRecord ? ((TrainRecord) s).getName()
                    : s instanceof NamedBean ? ((NamedBean) s).getDisplayName() : s.toString();
        }
    }
}
