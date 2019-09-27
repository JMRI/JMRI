package jmri.jmrix.pricom.pockettester;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 * Frame providing survey of DCC contents
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 */
public class PacketTableFrame extends jmri.util.JmriJFrame implements DataListener {

    PacketDataModel model = new PacketDataModel();
    JTable table;
    JScrollPane scroll;

    @Override
    public void initComponents() {

        table = new JTable(model);
        table.setRowSorter(new TableRowSorter<>(model));
        scroll = new JScrollPane(table);

        model.configureTable(table);

        // general GUI config
        setTitle("Packet Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(scroll);

        JPanel p1 = new JPanel();
        JButton b = new JButton(Bundle.getMessage("ButtonClear"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                model.reset();
            }
        });
        p1.add(b);
        getContentPane().add(p1);

        pack();

    }

    @Override
    public void dispose() {
        if (source != null) {
            source.removeListener(this);
        }
        model.dispose();
        model = null;
        table = null;
        scroll = null;
        super.dispose();
    }

    DataSource source;

    public void setSource(DataSource d) {
        source = d;
        model.setSource(d);
    }

    @Override
    public void asciiFormattedMessage(String m) {
        model.asciiFormattedMessage(m);
    }

}
