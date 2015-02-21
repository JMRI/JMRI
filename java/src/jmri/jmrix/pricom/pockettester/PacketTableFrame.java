// PacketTableFrame.java
package jmri.jmrix.pricom.pockettester;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import jmri.util.JTableUtil;

/**
 * Frame providing survey of DCC contents
 *
 * @author	Bob Jacobsen Copyright (C) 2005
 * @version	$Revision$
 */
public class PacketTableFrame extends jmri.util.JmriJFrame implements DataListener {

    /**
     *
     */
    private static final long serialVersionUID = 219225062863225988L;
    PacketDataModel model = new PacketDataModel();
    JTable table;
    JScrollPane scroll;

    static java.util.ResourceBundle rb
            = java.util.ResourceBundle.getBundle("jmri.jmrix.pricom.pockettester.TesterBundle");

    public void initComponents() {

        table = JTableUtil.sortableDataModel(model);
        scroll = new JScrollPane(table);

        model.configureTable(table);

        // general GUI config
        setTitle("Packet Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(scroll);

        JPanel p1 = new JPanel();
        JButton b = new JButton(rb.getString("ButtonClear"));
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                model.reset();
            }
        });
        p1.add(b);
        getContentPane().add(p1);

        pack();

    }

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

    public void asciiFormattedMessage(String m) {
        model.asciiFormattedMessage(m);
    }

}
