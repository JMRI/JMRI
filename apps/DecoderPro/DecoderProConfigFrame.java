// DecoderProConfigFrame.java

package apps.DecoderPro;

import apps.DecoderPro.DecoderProConfigFile;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * DecoderProConfigFrame provides startup configuration, a GUI for setting
 * config/preferences, and read/write support.  Its specific to DecoderPro
 * but should eventually be generalized.  Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.20 $
 */
public class DecoderProConfigFrame extends apps.AbstractConfigFrame {

    public DecoderProConfigFrame(String name) {
        super(name);
    }
    
    /**
     * DecoderPro only supports the DCC protocols, so the parent member is overloaded
     */
    public String[] availableProtocols() {
        return  new String[] {"(None selected)",
                              "EasyDCC", "Lenz XPressNet",
                              "LocoNet LocoBuffer","LocoNet MS100",
                              "NCE", "SPROG"
        };
    }
    /**
     * Abstract method to save the data
     */
    public void saveContents() {
        DecoderProConfigFile f = new DecoderProConfigFile();
        f.makeBackupFile(f.defaultConfigFilename());
        f.writeFile(f.defaultConfigFilename(), this);
    }
    
}
