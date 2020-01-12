package jmri.jmrix.rps.aligntable;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import javax.vecmath.Point3d;
import jmri.jmrix.rps.Algorithms;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Receiver;
import jmri.swing.RowSorterUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for user management of RPS alignment.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class AlignTablePane extends javax.swing.JPanel {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.aligntable.AlignTableBundle");

    /**
     * Constructor method
     */
    public AlignTablePane(jmri.ModifiedFlag flag) {
        super();
        this.flag = flag;
    }

    AlignModel alignModel = null;
    jmri.ModifiedFlag flag;

    /**
     * Initialize the window
     */
    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        alignModel = new AlignModel();

        JTable alignTable = new JTable(alignModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        alignTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        alignTable.setDefaultEditor(JButton.class, buttonEditor);

        TableRowSorter<AlignModel> sorter = new TableRowSorter<>(alignModel);
        RowSorterUtil.setSortOrder(sorter, AlignModel.NUMCOL, SortOrder.ASCENDING);
        alignTable.setRowSelectionAllowed(false);
        alignTable.setPreferredScrollableViewportSize(new java.awt.Dimension(580, 80));

        JScrollPane scrollPane = new JScrollPane(alignTable);
        add(scrollPane);

        // status info on bottom
        JPanel p = new JPanel() {

            @Override
            public Dimension getMaximumSize() {
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height);
            }
        };
        p.setLayout(new FlowLayout());

        p.add(new JLabel(rb.getString("LabelNumCol")));
        num.setText("" + Engine.instance().getMaxReceiverNumber());
        p.add(num);

        JButton b = new JButton(rb.getString("ButtonSet"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // set number of columns
                Engine.instance().setMaxReceiverNumber(
                        Integer.parseInt(num.getText()));
                // mark modification
                flag.setModifiedFlag(true);
                // resize table
                alignModel.fireTableStructureChanged();

            }
        });
        p.add(b);
        add(p);

        p = new JPanel() {

            @Override
            public Dimension getMaximumSize() {
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height);
            }
        };
        p.setLayout(new FlowLayout());

        p.add(new JLabel(rb.getString("LabelVSound")));
        vsound.setText("" + Engine.instance().getVSound());
        p.add(vsound);

        p.add(new JLabel(rb.getString("LabelOffset")));
        offset.setText("" + Engine.instance().getOffset());
        p.add(offset);

        p.add(new JLabel(rb.getString("LabelAlgorithm")));
        p.add(algorithmBox);

        b = new JButton(rb.getString("ButtonSet"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // set number of vsound, offset
                Engine.instance().setOffset(
                        Integer.parseInt(offset.getText()));
                Engine.instance().setVSound(
                        Double.parseDouble(vsound.getText()));
                Engine.instance().setAlgorithm(
                        (String) algorithmBox.getSelectedItem());
                // mark modification
                flag.setModifiedFlag(true);
            }
        });
        p.add(b);
        add(p);

        //
        add(loadStore = new jmri.jmrix.rps.swing.LoadStorePanel() {
            // make sure we redisplay if changed
            @Override
            public void load() {
                super.load();
                alignModel.fireTableStructureChanged();
                // modified (to force store of default after load new values)
                flag.setModifiedFlag(true);
            }

            @Override
            public void storeDefault() {
                super.storeDefault();
                // no longer modified after storeDefault
                flag.setModifiedFlag(false);
            }
        });

        // add sound listener
        Engine.instance().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("vSound")) {
                    // update sound display
                    vsound.setText("" + e.getNewValue());
                }
            }
        });
    }

    jmri.jmrix.rps.swing.LoadStorePanel loadStore;

    void storeDefault() {
        loadStore.storeDefault();
        // no longer modified after storeDefault
        flag.setModifiedFlag(false);
    }

    JTextField num = new JTextField(4);
    JTextField vsound = new JTextField(8);
    JTextField offset = new JTextField(4);
    JComboBox<String> algorithmBox = Algorithms.algorithmBox();

    /**
     * Set up table for showing individual receivers
     * <ol>
     * <li>Address
     * <li>Present Y/N
     * <li>Edit button
     * </ol>
     */
    public class AlignModel extends AbstractTableModel {

        static private final int NUMCOL = 0;
        static private final int XCOL = 1;
        static private final int YCOL = 2;
        static private final int ZCOL = 3;

        static private final int LASTTIMECOL = 4;

        static private final int ACTIVECOL = 5;

        static private final int MINTIMECOL = 6;
        static private final int MAXTIMECOL = 7;

        static private final int LAST = MAXTIMECOL;

        @Override
        public int getColumnCount() {
            return LAST + 1;
        }

        @Override
        public int getRowCount() {
            return Engine.instance().getMaxReceiverNumber();
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case NUMCOL:
                    return rb.getString("TitleColNum");
                case XCOL:
                    return rb.getString("TitleColX");
                case YCOL:
                    return rb.getString("TitleColY");
                case ZCOL:
                    return rb.getString("TitleColZ");
                case LASTTIMECOL:
                    return rb.getString("TitleColLast");
                case ACTIVECOL:
                    return rb.getString("TitleColActive");
                case MINTIMECOL:
                    return rb.getString("TitleColMin");
                case MAXTIMECOL:
                    return rb.getString("TitleColMax");
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == XCOL || c == YCOL || c == ZCOL) {
                return Double.class;
            } else if (c == NUMCOL) {
                return Integer.class;
            } else if (c == ACTIVECOL) {
                return Boolean.class;
            }
            if (c == MINTIMECOL || c == MAXTIMECOL || c == LASTTIMECOL) {
                return Integer.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == XCOL || c == YCOL || c == ZCOL || c == ACTIVECOL
                    || c == MINTIMECOL || c == MAXTIMECOL) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            // r is row number, from 0; receiver addresses start at 1
            Receiver rc;
            switch (c) {
                case NUMCOL:
                    return Integer.valueOf(r + 1);
                case XCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Double.valueOf(rc.getPosition().x);
                case YCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Double.valueOf(rc.getPosition().y);
                case ZCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Double.valueOf(rc.getPosition().z);
                case ACTIVECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Boolean.valueOf(rc.isActive());
                case LASTTIMECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Integer.valueOf(rc.getLastTime());
                case MINTIMECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Integer.valueOf(rc.getMinTime());
                case MAXTIMECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        return null;
                    }
                    return Integer.valueOf(rc.getMaxTime());
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object val, int r, int c) {
            // r is row number, from 0
            Receiver rc;
            Point3d p;
            switch (c) {
                case XCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    p = rc.getPosition();
                    p.x = ((Double) val).doubleValue();
                    Engine.instance().setReceiverPosition(r + 1, p);
                    flag.setModifiedFlag(true);
                    break;
                case YCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    p = rc.getPosition();
                    p.y = ((Double) val).doubleValue();
                    Engine.instance().setReceiverPosition(r + 1, p);
                    flag.setModifiedFlag(true);
                    break;
                case ZCOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    p = rc.getPosition();
                    p.z = ((Double) val).doubleValue();
                    Engine.instance().setReceiverPosition(r + 1, p);
                    flag.setModifiedFlag(true);
                    break;
                case ACTIVECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    rc.setActive(((Boolean) val).equals(Boolean.TRUE));
                    flag.setModifiedFlag(true);
                    break;
                case MINTIMECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    int min = ((Integer) val).intValue();
                    Engine.instance().getReceiver(r + 1).setMinTime(min);
                    flag.setModifiedFlag(true);
                    break;
                case MAXTIMECOL:
                    rc = Engine.instance().getReceiver(r + 1);
                    if (rc == null) {
                        rc = new Receiver(new Point3d(0., 0., 0.));
                        Engine.instance().setReceiver(r + 1, rc);
                    }
                    int max = ((Integer) val).intValue();
                    Engine.instance().getReceiver(r + 1).setMaxTime(max);
                    flag.setModifiedFlag(true);
                    break;
                default:
                    log.error("setValueAt of column " + c);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AlignTablePane.class);

}
