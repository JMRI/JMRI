package jmri.jmrix.ecos.swing.preferences;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PreferencesFrame object
 *
 * @author	Kevin Dickerson Copyright (C) 2009
  */
public class PreferencesFrameAction extends AbstractAction {

    public PreferencesFrameAction(String s, EcosSystemConnectionMemo memo) {
        super(s);
        adaptermemo = memo;
    }

    EcosSystemConnectionMemo adaptermemo;

    public void actionPerformed(ActionEvent e) {
        PreferencesFrame f = new PreferencesFrame();
        try {
            f.initComponents(adaptermemo);
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
            ex.printStackTrace();
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(PreferencesFrameAction.class.getName());
}
