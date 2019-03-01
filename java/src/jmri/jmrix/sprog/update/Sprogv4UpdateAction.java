package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogIIUpdateFrame object
 *
 * @author	Andrew crosland Copyright (C) 2004
 * 
 * @deprecated since 4.11.1; supports uncommon Sprog versions that are confused with Sprog II versions.
 */
@Deprecated
public class Sprogv4UpdateAction extends SprogUpdateAction {

    public Sprogv4UpdateAction(String s,SprogSystemConnectionMemo memo) {
        super(s,memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object[] options = {Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonContinue")};
        if (1 == JOptionPane.showOptionDialog(null,
                Bundle.getMessage("SprogXUpdateDialogString", ""),
                Bundle.getMessage("SprogXFirmwareUpdate", " v3/v4"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
            // create a SprogIIUpdateFrame
            // create a SprogUpdateFrame
            Sprogv4UpdateFrame f = new Sprogv4UpdateFrame(_memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Sprogv4UpdateAction starting Sprogv4UpdateFrame: Exception: " + ex.toString());
            }
            f.setVisible(true);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Sprogv4UpdateAction.class);

}



