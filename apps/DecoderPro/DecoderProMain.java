// DecoderProMain.java

package apps.DecoderPro;

import jmri.util.oreilly.BasicWindowMonitor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

/**
 * DecoderPro application main class.
 *
 * @author                      Bob Jacobsen
 * @version                     $Revision: 1.5 $
 */
public class DecoderProMain extends JPanel {
    public DecoderProMain(JFrame frame) {

        super(true);

        ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

        // create basic GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

        // load preferences
        DecoderProConfigAction prefs
            = new DecoderProConfigAction(rb.getString("MenuItemPreferences"));

        // populate GUI

        // create actions with side-effects if you need to reference them more than once
        Action serviceprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("DpButtonUseProgrammingTrack"));
        Action opsprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("DpButtonProgramOnMainTrack"));
        Action quit = new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

        // Create menu categories and add to the menu bar, add actions to menus
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(serviceprog);
        fileMenu.add(opsprog);
        fileMenu.add(new JSeparator());
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(frame));
        fileMenu.add(quit);

        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(prefs);

        menuBar.add(new jmri.jmrit.roster.RosterMenu(rb.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        menuBar.add(new jmri.jmrit.ToolsMenu());

        JMenu debugMenu = new jmri.jmrit.DebugMenu(this);
        menuBar.add(debugMenu);
        debugMenu.add(new JSeparator());
        debugMenu.add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction(rb.getString("MenuItemEasyDCCCommandMonitor")));
        debugMenu.add(new jmri.jmrix.lenz.mon.XNetMonAction(rb.getString("MenuItemLenzAtlasCommandMonitor")));
        debugMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction(rb.getString("MenuItemLocoNetMonitor")));
        debugMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction(rb.getString("MenuItemNCECommandMonitor")));
        debugMenu.add(new jmri.jmrix.sprog.sprogmon.SprogMonAction(rb.getString("MenuItemSPROGCommandMonitor")));
        debugMenu.add(new JSeparator());
        debugMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction(rb.getString("MenuItemSerialPortSensors")));

        // Label & text
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"),"Decoder Pro label"), JLabel.LEFT));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("DpCredit1"),
                                new String[]{jmri.Version.name()}
                            )));
        pane2.add(new JLabel("http://jmri.sf.net/DecoderPro "));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("DpCredit2"),
                                new String[]{prefs.getCurrentProtocolName()}
                            )));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("DpCredit3"),
                                new String[]{prefs.getCurrentPortName()}
                            )));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(MessageFormat.format(rb.getString("DpCredit4"),
                                new String[]{System.getProperty("java.version","<unknown>"),
                                            Locale.getDefault().toString()}
                            )));
        pane1.add(pane2);
        add(pane1);

        // Buttons
        JButton b1 = new JButton(rb.getString("DpButtonUseProgrammingTrack"));
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(b1);

        JButton m1 = new JButton(rb.getString("DpButtonProgramOnMainTrack"));
        m1.addActionListener(opsprog);
        m1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(m1);
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isOpsModePossible()) {
            m1.setEnabled(false);
            m1.setToolTipText(rb.getString("MsgOpsButtonDisabled"));
        }

        JButton q1 = new JButton(rb.getString("ButtonQuit"));
        q1.addActionListener(quit);
        q1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(q1);
    }

    // Main entry point
    public static void main(String s[]) {

        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("DecoderPro starts");

        // create the primary frame
        JFrame frame = new JFrame("DecoderPro");
        DecoderProMain pane = new DecoderProMain(frame);
        frame.addWindowListener(new BasicWindowMonitor());
        // insert contents
        frame.setJMenuBar(pane.menuBar);
        frame.getContentPane().add(pane);
        // pack and center this frame
        frame.pack();
        Dimension screen = pane.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);

        frame.setVisible(true);
    }

    // GUI members
    private JMenuBar menuBar;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProMain.class.getName());
}

