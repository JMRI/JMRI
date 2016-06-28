package jmri.jmrit.roster;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.Icon;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the decoder definitions in the roster
 *
 * @author	Bob Jacobsen Copyright (C) 2013
 * @see jmri.jmrit.XmlFile
 */
public class UpdateDecoderDefinitionAction extends JmriAbstractAction {

    public UpdateDecoderDefinitionAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public UpdateDecoderDefinitionAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public UpdateDecoderDefinitionAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<RosterEntry> list = Roster.getDefault().matchingList(null, null, null, null, null, null, null);

        for (RosterEntry entry : list) {
            String family = entry.getDecoderFamily();
            String model = entry.getDecoderModel();

            // check if replaced
            List<DecoderFile> decoders = DecoderIndexFile.instance().matchingDecoderList(null, family, null, null, null, model);
            log.info("Found " + decoders.size() + " decoders matching family \"" + family + "\" model \"" + model + "\" from roster entry \"" + entry.getId() + "\"");

            String replacementFamily = null;
            String replacementModel = null;

            for (DecoderFile decoder : decoders) {

                if (decoder.getReplacementFamily() != null || decoder.getReplacementModel() != null) {
                    log.info("   Recommended replacement is family \"" + decoder.getReplacementFamily() + "\" model \"" + decoder.getReplacementModel() + "\"");
                }
                replacementFamily = (decoder.getReplacementFamily() != null) ? decoder.getReplacementFamily() : replacementFamily;
                replacementModel = (decoder.getReplacementModel() != null) ? decoder.getReplacementModel() : replacementModel;
            }

            if (replacementModel != null && replacementFamily != null) {
                log.info("   *** Will update");

                // change the roster entry
                entry.setDecoderFamily(replacementFamily);
                entry.setDecoderModel(replacementModel);

                // write it out (not bothering to do backup?)
                entry.updateFile();
            }
        }

        // write updated roster
        Roster.getDefault().makeBackupFile(Roster.getDefault().getRosterIndexPath());
        try {
            Roster.getDefault().writeFile(Roster.getDefault().getRosterIndexPath());
        } catch (IOException ex) {
            log.error("Exception while writing the new roster file, may not be complete: " + ex);
        }
        // use the new one
        Roster.getDefault().reloadRosterFile();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(UpdateDecoderDefinitionAction.class.getName());
}
