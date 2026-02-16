package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.swing.JmriMouseEvent;
import jmri.util.swing.JmriMouseListener;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        throttleFrames.setDragEnabled(true);
        throttleFrames.setDropMode(DropMode.INSERT_ROWS);
        throttleFrames.setTransferHandler(new ThrottlesTableTransferHandler(throttleFrames)); 
        throttleFrames.setDefaultRenderer(Object.class, new ThrottlesTableCellRenderer());
        throttleFrames.addMouseListener(JmriMouseListener.adapt(new JmriMouseListener() {
            @Override
            public void mouseClicked(JmriMouseEvent e) {                
                int ntw = throttleFrames.columnAtPoint(e.getPoint());
                int ntf = throttleFrames.rowAtPoint(e.getPoint());
                log.debug("Click in table at row {} (frame) / col {} (window)", ntf, ntw);
                ThrottleFrame tf = ((ThrottleFrame) throttleFramesLM.getValueAt(ntf, ntw));
                if (tf != null) {
                    tf.toFront();
                }
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
            ThrottleControllerUI tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleController();
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
    
    private final static Logger log = LoggerFactory.getLogger(ThrottlesListPanel.class);    
}
