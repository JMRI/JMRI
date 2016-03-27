package jmri.layout;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

public class LayoutManagerTest extends JFrame implements LayoutEventListener {

    JPanel contentPane;
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenu jMenuFile = new JMenu();
    JMenuItem jMenuFileExit = new JMenuItem();
    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JLabel statusBar = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    JMenu jMenu1 = new JMenu();

    Layout mLayout = new Layout("LocalHost");

    JMenuItem jMenuItem1 = new JMenuItem();
    JSplitPane jSplitPane1 = new JSplitPane();
    JTree mLayoutElementTree = new JTree(mLayout.getLayoutTree());
    JTextArea mLogTextBox = new JTextArea();

    /**
     * Construct the frame
     */
    public LayoutManagerTest() {
        mLayout.addEventListener(this);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Component initialization
     */
    private void jbInit() throws Exception {
        //setIconImage(Toolkit.getDefaultToolkit().createImage(LayoutManagerTest.class.getResource("[Your Icon]")));
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(borderLayout1);
        this.setSize(new Dimension(800, 600));
        this.setTitle("Layout Manager Test Application");
        statusBar.setText(" ");
        jMenuFile.setText("File");
        jMenuFileExit.setText("Exit");
        jMenuFileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuFileExit_actionPerformed(e);
            }
        });
        jMenuHelp.setText("Help");
        jMenuHelpAbout.setText("About");
        jMenuHelpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuHelpAbout_actionPerformed(e);
            }
        });
        jMenu1.setText("Test");
        jMenuItem1.setText("Create Elements");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createLayoutElements_actionPerformed(e);
            }
        });
        mLogTextBox.setText("jTextArea1");

        // The following lines are commented out to get Java 1.1.8 compilation.
        // They cause some convenient-but-not-vital window resizing to occur
        //    contentPane.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsAdapter() {
        //        public void ancestorResized(HierarchyEvent e) {
        //            contentPane_ancestorResized(e);
        //        }
        //    });
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setPreferredSize(new Dimension(395, 19));
        jMenuFile.add(jMenuFileExit);
        jMenuHelp.add(jMenuHelpAbout);
        jMenuBar1.add(jMenuFile);
        jMenuBar1.add(jMenu1);
        jMenuBar1.add(jMenuHelp);
        this.setJMenuBar(jMenuBar1);
        contentPane.add(statusBar, BorderLayout.SOUTH);
        contentPane.add(jSplitPane1, BorderLayout.WEST);
        jSplitPane1.add(mLayoutElementTree, JSplitPane.TOP);
        jSplitPane1.add(mLogTextBox, JSplitPane.BOTTOM);
        jMenu1.add(jMenuItem1);
        jSplitPane1.setDividerLocation(200);
    }

    // The following lines are commented out to get Java 1.1.8 compilation.
    // They cause some convenient-but-not-vital window resizing to occur
    //    void contentPane_ancestorResized(HierarchyEvent e)
    //    {
    //        Dimension d = this.getSize();
    //        jSplitPane1.setSize( d.width, d.height - 30 );
    //    }
    /**
     * File | Exit action performed
     */
    public void jMenuFileExit_actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    /**
     * Help | About action performed
     */
    public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
        LayoutManagerTest_AboutBox dlg = new LayoutManagerTest_AboutBox(this);
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.setVisible(true);
    }

    /**
     * Overridden so we can exit when window is closed
     */
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            jMenuFileExit_actionPerformed(null);
        }
    }

    void createLayoutElements_actionPerformed(ActionEvent e) {
        String[] vLayoutNames = {"LocoNet_COM1", "EasyDCC_COM2", "NCE_COM3", "CMRI_COM4"};

        LayoutEventData vData;
        java.util.Random vRandom = new java.util.Random(System.currentTimeMillis());
        for (int vIndex = 0; vIndex < 20; vIndex++) {
            int vRandomNumber = vRandom.nextInt();
            if (vRandomNumber < 0) {
                vRandomNumber = 0 - vRandomNumber;
            }

            String vLayoutName = vLayoutNames[vRandomNumber % vLayoutNames.length];
            int vType = vRandomNumber % LayoutAddress.ELEMENT_TYPE_LAST;
            int vOffset = vRandomNumber % 1000;

            String vState;
            switch (vType) {
                case LayoutAddress.ELEMENT_TYPE_SENSOR:
                    vState = (vRandomNumber % 2) == 0 ? "Open" : "Closed";
                    break;

                case LayoutAddress.ELEMENT_TYPE_TURNOUT:
                    vState = (vRandomNumber % 2) == 0 ? "Thrown" : "Closed";
                    break;

                default:
                    vState = "Speed: " + Integer.toString(vRandomNumber % 128)
                            + " Dir: " + ((vRandomNumber % 2) == 0 ? "Fwd" : "Rev");
            }

            vData = new LayoutEventData(vLayoutName, vType, vOffset, vState);
            mLayout.message(vData);
        }
    }

    public static void main(String[] Args) {
        LayoutManagerTest vLayoutManagerTest = new LayoutManagerTest();
        vLayoutManagerTest.setVisible(true);
    }

    public void message(LayoutEventData pLayoutEvent) {
        log(pLayoutEvent.toString());
    }

    private void log(String pMessage) {
        mLogTextBox.append(pMessage + "\n");
    }
}
