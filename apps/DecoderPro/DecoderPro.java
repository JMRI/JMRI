// DecoderPro.java

package apps.DecoderPro;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.util.oreilly.*;

/**
 * DecoderPro application.
 *
 * @author                      Bob Jacobsen
 * @version                     $Revision: 1.25 $
 */
public class DecoderPro extends JPanel {
    public DecoderPro() {

        super(true);

        // create basic GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a menu bar and give it a bevel border
        menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));

        // load preferences
        DecoderProConfigAction prefs
            = new DecoderProConfigAction("Preferences...");

        // populate GUI

        // create actions with side-effects if you need to reference them more than once
        Action serviceprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction("Use programming track ...");
        Action opsprog = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction("Program on main track ...");
        Action quit = new AbstractAction("Quit"){
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };

        // Create menu categories and add to the menu bar, add actions to menus
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(serviceprog);
        fileMenu.add(opsprog);
        fileMenu.add(new JSeparator());
        fileMenu.add(quit);

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        editMenu.add(prefs);

        menuBar.add(new jmri.jmrit.roster.RosterMenu("Roster", jmri.jmrit.roster.RosterMenu.MAINMENU, this));

        JMenu toolMenu = new JMenu("Tools");
        menuBar.add(toolMenu);
        toolMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction("Single CV Programmer"));
        toolMenu.add(new jmri.jmrit.XmlFileCheckAction("Check XML File", this));
        toolMenu.add(new jmri.jmrit.decoderdefn.NameCheckAction("Check decoder names", this));
        toolMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction("Check programmer names", this));
        toolMenu.add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction("Create decoder index"));

        JMenu debugMenu = new JMenu("Debug");
        menuBar.add(debugMenu);
        debugMenu.add(new jmri.jmrix.easydcc.easydccmon.EasyDccMonAction("EasyDCC Command Monitor"));
        debugMenu.add(new jmri.jmrix.lenz.mon.XNetMonAction("Lenz/Atlas Command Monitor"));
        debugMenu.add(new jmri.jmrix.loconet.locomon.LocoMonAction("LocoNet Monitor"));
        debugMenu.add(new jmri.jmrix.nce.ncemon.NceMonAction("Nce Command Monitor"));
        debugMenu.add(new jmri.jmrit.MemoryFrameAction("Memory usage monitor"));

        // Label & text
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"),"Decoder Pro label"), JLabel.LEFT));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(" Decoder Pro "+jmri.Version.name()+", part of the JMRI project "));
        pane2.add(new JLabel("   http://jmri.sf.net/DecoderPro "));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(" Connected via "+prefs.getCurrentProtocolName()));
        pane2.add(new JLabel(" on port "+prefs.getCurrentPortName()));
        pane2.add(new JLabel(" "));
        pane2.add(new JLabel(" Java version "+System.getProperty("java.version","<unknown>")));
        pane1.add(pane2);
        add(pane1);

        // Buttons
        JButton b1 = new JButton("Use programming track ...");
        b1.addActionListener(serviceprog);
        b1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(b1);

        JButton m1 = new JButton("Program on main track ...");
        m1.addActionListener(opsprog);
        m1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        add(m1);
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
            !jmri.InstanceManager.programmerManagerInstance().isOpsModePossible()) {
            m1.setEnabled(false);
            m1.setToolTipText("This button is disabled because your command station can't do "
                            +"ops mode programming, or we don't yet have code to do "
                            +"it for that type of system");
        }

        JButton q1 = new JButton("Quit");
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
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.ERROR);
            }
        }
        catch (java.lang.NoSuchMethodError e) { System.out.println("Exception starting logging: "+e); }

        log.info("DecoderPro starts");

        // create the demo frame and menus
        DecoderPro pane = new DecoderPro();
        JFrame frame = new JFrame("Decoder Pro");
        frame.addWindowListener(new BasicWindowMonitor());
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderPro.class.getName());
}

