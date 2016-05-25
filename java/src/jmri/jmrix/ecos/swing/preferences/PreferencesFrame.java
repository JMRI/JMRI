// PreferencesFrame.java
package jmri.jmrix.ecos.swing.preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * Frame for ECoS preferences
 *
 * @author	Kevin Dickerson Copyright (C) 2009
 * @version $Revision$
 */
public class PreferencesFrame extends jmri.util.JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -8594656805050589881L;

    public PreferencesFrame() {
        super();
    }

    JButton sendButton;
    PreferencesPane preferencesPane;

    public void initComponents(EcosSystemConnectionMemo adaptermemo) throws Exception {
        // the following code sets the frame's initial state

        preferencesPane = new PreferencesPane(adaptermemo.getPreferenceManager());

        setTitle("ECoS User Preferences");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(preferencesPane);

        addHelpMenu("package.jmri.jmrix.ecos.swing.preferencesframe.PreferencesFrame", true);

        pack();
    }

}
