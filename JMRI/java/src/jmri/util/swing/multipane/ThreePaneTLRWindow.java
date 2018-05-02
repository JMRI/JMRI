package jmri.util.swing.multipane;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import jmri.util.swing.JMenuUtil;
import jmri.util.swing.JToolBarUtil;
import jmri.util.swing.WindowInterface;

/**
 * MultiPane JMRI window with a "top" area over "left" and "right" lower panes,
 * optional toolbar and menu.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public class ThreePaneTLRWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     *
     * @param name        the name and title of the window
     * @param menubarFile path to the XML file for the menubar
     * @param toolbarFile path to the XML file for the toolbar
     */
    public ThreePaneTLRWindow(String name, String menubarFile, String toolbarFile) {
        super(name);
        buildGUI(menubarFile, toolbarFile);
        pack();
    }

    JSplitPane upDownSplitPane;
    JSplitPane leftRightSplitPane;

    JPanel top = new JPanel();

    JPanel left = new JPanel();
    JPanel right = new JPanel();

    public JComponent getTop() {
        return top;
    }

    public JComponent getRight() {
        return right;
    }

    public JComponent getLeft() {
        return left;
    }

    WindowInterface rightTopWI;

    protected void buildGUI(String menubarFile, String toolbarFile) {
        configureFrame();
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
    }

    protected void configureFrame() {

        rightTopWI = new jmri.util.swing.sdi.JmriJFrameInterface();  // TODO figure out what WI is used here

        //rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        right.setLayout(new FlowLayout());
        left.setLayout(new FlowLayout());

        leftRightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        leftRightSplitPane.setOneTouchExpandable(true);
        leftRightSplitPane.setResizeWeight(0.0);  // emphasize right part

        upDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                top,
                leftRightSplitPane);
        upDownSplitPane.setOneTouchExpandable(true);
        upDownSplitPane.setResizeWeight(1.0);  // emphasize top part

        add(upDownSplitPane, BorderLayout.CENTER);
    }

    public void resetRightToPreferredSizes() {
        leftRightSplitPane.resetToPreferredSizes();
    }

    protected void addMainMenuBar(String menuFile) {
        if (menuFile == null) {
            return;
        }
        JMenuBar menuBar = new JMenuBar();

        JMenu[] menus = JMenuUtil.loadMenu(menuFile, rightTopWI, null);
        for (JMenu j : menus) {
            menuBar.add(j);
        }

        setJMenuBar(menuBar);
    }

    protected void addMainToolBar(String toolBarFile) {
        if (toolBarFile == null) {
            return;
        }

        JToolBar toolBar = JToolBarUtil.loadToolBar(toolBarFile, rightTopWI, null);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }

    /**
     * Only close frame, etc, dispose() disposes of all cached panes
     */
    @Override
    public void dispose() {
        rightTopWI.dispose();
        super.dispose();
    }

}
