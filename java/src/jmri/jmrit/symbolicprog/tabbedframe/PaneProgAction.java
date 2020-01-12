package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CombinedLocoSelTreePane;
import jmri.jmrit.symbolicprog.SymbolicProgBundle;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a frame for selecting the information
 * needed to open a PaneProgFrame in service mode.
 * <p>
 * The name is a historical accident, and probably should have included
 * "ServiceMode" or something.
 * <p>
 * The resulting JFrame is constructed on the fly here, and has no specific
 * type.
 *
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class PaneProgAction extends AbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;
    jmri.jmrit.progsupport.ProgModeSelector modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox();

    public PaneProgAction() {
        this("DecoderPro service programmer");
    }

    public PaneProgAction(String s) {
        super(s);

        statusLabel = new JLabel(SymbolicProgBundle.getMessage("StateIdle"));

        // disable ourself if programming is not possible
        if (jmri.InstanceManager.getNullableDefault(jmri.GlobalProgrammerManager.class) == null
                || !jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).isGlobalProgrammerAvailable()) {
            setEnabled(false);
            // This needs to return, so we don't start the xmlThread
            return;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) {
            log.debug("Pane programmer requested");
        }

        // create the initial frame that steers
        final JmriJFrame f = new JmriJFrame(SymbolicProgBundle.getMessage("FrameServiceProgrammerSetup"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

        // ensure status line is cleared on close so it is normal if re-opened
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                statusLabel.setText(SymbolicProgBundle.getMessage("StateIdle"));
                f.windowClosing(we);
            }
        });

        // add the Roster menu
        JMenuBar menuBar = new JMenuBar();
        // menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
        JMenu j = new JMenu(SymbolicProgBundle.getMessage("MenuFile"));
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintDecoderDefinitions"), f, false));
        j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(SymbolicProgBundle.getMessage("MenuPrintPreviewDecoderDefinitions"), f, true));
        menuBar.add(j);
        menuBar.add(new jmri.jmrit.roster.swing.RosterMenu(SymbolicProgBundle.getMessage("MenuRoster"), jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f));
        f.setJMenuBar(menuBar);

        // new Loco on programming track
        JPanel pane1 = new CombinedLocoSelTreePane(statusLabel, modePane) {

            @Override
            protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                    String filename) {
                String title = java.text.MessageFormat.format(SymbolicProgBundle.getMessage("FrameServiceProgrammerTitle"),
                        new Object[]{"new decoder"});
                if (re != null) {
                    title = java.text.MessageFormat.format(SymbolicProgBundle.getMessage("FrameServiceProgrammerTitle"),
                            new Object[]{re.getId()});
                }
                JFrame p = new PaneServiceProgFrame(decoderFile, re,
                        title, "programmers" + File.separator + filename + ".xml",
                        modePane.getProgrammer());
                p.pack();
                p.setVisible(true);

                // f.setVisible(false);
                // f.dispose();
            }
        };

        // load primary frame
        JPanel tempPane = new JPanel();
        tempPane.add(modePane);
        f.getContentPane().add(tempPane);
        f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(pane1);
        f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

        statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        f.getContentPane().add(statusLabel);

        f.pack();
        if (log.isDebugEnabled()) {
            log.debug("Tab-Programmer setup created");
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(PaneProgAction.class);

}
