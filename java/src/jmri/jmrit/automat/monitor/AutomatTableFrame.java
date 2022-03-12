package jmri.jmrit.automat.monitor;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 * Frame providing a table of Automat instances
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class AutomatTableFrame extends jmri.util.JmriJFrame {

    AutomatTableDataModel dataModel;
    JTable dataTable;
    JScrollPane dataScroll;

    public AutomatTableFrame(AutomatTableDataModel model) {

        super();
        dataModel = model;

        dataTable = new JTable(dataModel);
        dataTable.setRowSorter(new TableRowSorter<>(dataModel));
        dataScroll = new JScrollPane(dataTable);

        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        getContentPane().add(dataScroll);
        pack();
        pane1.setMaximumSize(pane1.getSize());

        setTitle(Bundle.getMessage("TitleAutomatTable"));

        addHelpMenu("package.jmri.jmrit.automat.monitor.AutomatTableFrame", true);

        pack();
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
        super.dispose();
    }
}
