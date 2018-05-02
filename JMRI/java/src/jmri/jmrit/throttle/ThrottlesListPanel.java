package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import org.jdom2.Element;

public class ThrottlesListPanel extends JPanel {

    private ThrottlesTableModel throttleFramesLM;
    private JTable throttleFrames;

    public ThrottlesListPanel() {
        super();
        throttleFramesLM = new ThrottlesTableModel();
        initGUI();
    }

    public ThrottlesTableModel getTableModel() {
        return throttleFramesLM;
    }

    private void initGUI() {
        throttleFrames = new JTable(throttleFramesLM);
        throttleFrames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        throttleFrames.setRowHeight(ThrottlesTableCellRenderer.height);
        throttleFrames.setTableHeader(null);
        throttleFrames.setDefaultRenderer(Object.class, new ThrottlesTableCellRenderer());
        throttleFrames.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = throttleFrames.rowAtPoint(e.getPoint());
                throttleFrames.getSelectionModel().setSelectionInterval(row, row);
                ((ThrottleFrame) throttleFramesLM.getValueAt(row, 0)).toFront();
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
            }

            @Override
            public void mousePressed(MouseEvent arg0) {
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
            }
        });

        JScrollPane scrollPane1 = new JScrollPane(throttleFrames);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 200));

        JToolBar throttleToolBar = new JToolBar("Throttles list toolbar");
        JButton jbNew = new JButton();
        jbNew.setIcon(new NamedIcon("resources/icons/throttles/new.png", "resources/icons/throttles/new.png"));
        jbNew.setToolTipText(Bundle.getMessage("ThrottleToolBarNewWindowToolTip"));
        jbNew.setVerticalTextPosition(JButton.BOTTOM);
        jbNew.setHorizontalTextPosition(JButton.CENTER);
        jbNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
                tf.toFront();
            }
        });
        throttleToolBar.add(jbNew);

        throttleToolBar.addSeparator();
        throttleToolBar.add(new StopAllButton());
        throttleToolBar.add(new LargePowerManagerButton());

        add(throttleToolBar, BorderLayout.PAGE_START);
        add(scrollPane1, BorderLayout.CENTER);

        throttleToolBar.addSeparator();
        JButton jbPreferences = new JButton();
        jbPreferences.setIcon(new NamedIcon("resources/icons/throttles/preferences.png", "resources/icons/throttles/Preferences24.png"));
        jbPreferences.setToolTipText(Bundle.getMessage("ThrottleToolBarPreferencesToolTip"));
        jbPreferences.setVerticalTextPosition(JButton.BOTTOM);
        jbPreferences.setHorizontalTextPosition(JButton.CENTER);
        jbPreferences.addActionListener(new ThrottlesPreferencesAction());
        throttleToolBar.add(jbPreferences);
    }

    public Element getXml() {
        Element me = new Element("ThrottlesListPanel");
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
        children.add(WindowPreferences.getPreferences(this.getTopLevelAncestor()));
        me.setContent(children);
        return me;
    }

    public void setXml(Element tlp) {
        Element window = tlp.getChild("window");
        if (window != null) {
            WindowPreferences.setPreferences(this.getTopLevelAncestor(), window);
        }
    }
}
