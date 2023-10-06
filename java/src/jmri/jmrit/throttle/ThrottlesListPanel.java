package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;

import org.jdom2.Element;

/**
 * A panel to display a list of active JMRI throttles
 *
 * @author Lionel Jeanson - 2009-2021
 *
 */

public class ThrottlesListPanel extends JPanel {

    private final ThrottlesTableModel throttleFramesLM;
    private JTable throttleFrames;

    public ThrottlesListPanel() {
        super();
        throttleFramesLM = new ThrottlesTableModel();
        ConsistManager consistManager = InstanceManager.getNullableDefault(jmri.ConsistManager.class);
        if (consistManager != null && consistManager.isEnabled()) {
            consistManager.addConsistListListener(throttleFramesLM);
        }
        initGUI();
    }

    public ThrottlesTableModel getTableModel() {
        return throttleFramesLM;
    }

    private void initGUI() {
        throttleFrames = new JTable(throttleFramesLM);
        throttleFrames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        throttleFrames.setRowHeight(ThrottlesTableCellRenderer.LINE_HEIGHT);
        throttleFrames.setTableHeader(null);
        throttleFrames.setDefaultRenderer(Object.class, new ThrottlesTableCellRenderer());
        throttleFrames.addMouseListener(JmriMouseListener.adapt(new JmriMouseListener() {
            @Override
            public void mouseClicked(JmriMouseEvent e) {
                int row = throttleFrames.rowAtPoint(e.getPoint());
                throttleFrames.getSelectionModel().setSelectionInterval(row, row);
                ((ThrottleFrame) throttleFramesLM.getValueAt(row, 0)).toFront();
            }

            @Override
            public void mouseEntered(JmriMouseEvent arg0) {
            }

            @Override
            public void mouseExited(JmriMouseEvent arg0) {
            }

            @Override
            public void mousePressed(JmriMouseEvent arg0) {
            }

            @Override
            public void mouseReleased(JmriMouseEvent arg0) {
            }
        }));

        JScrollPane scrollPane1 = new JScrollPane(throttleFrames);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(320, 200));

        JToolBar throttleToolBar = new JToolBar("Throttles list toolbar");
        JButton jbNew = new JButton();
        jbNew.setIcon(new NamedIcon("resources/icons/throttles/new.png", "resources/icons/throttles/new.png"));
        jbNew.setToolTipText(Bundle.getMessage("ThrottleToolBarNewWindowToolTip"));
        jbNew.setVerticalTextPosition(SwingConstants.BOTTOM);
        jbNew.setHorizontalTextPosition(SwingConstants.CENTER);
        jbNew.addActionListener((ActionEvent e) -> {
            ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
            tf.toFront();
        });
        throttleToolBar.add(jbNew);

        throttleToolBar.addSeparator();
        throttleToolBar.add(new StopAllButton());
        throttleToolBar.add(new LargePowerManagerButton(false));

        add(throttleToolBar, BorderLayout.PAGE_START);
        add(scrollPane1, BorderLayout.CENTER);

        throttleToolBar.addSeparator();
        JButton jbPreferences = new JButton();
        jbPreferences.setIcon(new NamedIcon("resources/icons/throttles/preferences.png", "resources/icons/throttles/Preferences24.png"));
        jbPreferences.setToolTipText(Bundle.getMessage("ThrottleToolBarPreferencesToolTip"));
        jbPreferences.setVerticalTextPosition(SwingConstants.BOTTOM);
        jbPreferences.setHorizontalTextPosition(SwingConstants.CENTER);
        jbPreferences.addActionListener(new ThrottlesPreferencesAction());
        throttleToolBar.add(jbPreferences);
    }

    public Element getXml() {
        Element me = new Element("ThrottlesListPanel");
        java.util.ArrayList<Element> children = new java.util.ArrayList<>(1);
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

    void applyPreferences() {
        repaint();
    }
}
