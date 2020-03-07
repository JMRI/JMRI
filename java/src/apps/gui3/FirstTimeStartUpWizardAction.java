package apps.gui3;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

public class FirstTimeStartUpWizardAction extends jmri.util.swing.JmriAbstractAction {

    public FirstTimeStartUpWizardAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public FirstTimeStartUpWizardAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public FirstTimeStartUpWizardAction(String s) {
        super(s);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    static jmri.util.JmriJFrame f;

    apps.gui3.Apps3 app;

    public void setApp(apps.gui3.Apps3 app) {
        this.app = app;
    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        // create the frame
        if (f == null) {
            f = new jmri.util.JmriJFrame("DecoderPro Wizard", false, false);
            // Update the GUI Look and Feel
            // This is needed as certain controls are instantiated
            // prior to the setup of the Look and Feel
            SwingUtilities.updateComponentTreeUI(f);
        }
        FirstTimeStartUpWizard wiz = new FirstTimeStartUpWizard(f, app);
        f.setPreferredSize(new java.awt.Dimension(700, 400));
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.add(wiz.getPanel());
        f.pack();

        Dimension screenDim
                = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle winDim = f.getBounds();
        winDim.height = winDim.height + 10;
        winDim.width = winDim.width + 10;
        f.setLocation((screenDim.width - winDim.width) / 2,
                (screenDim.height - winDim.height) / 2);
        f.setSize(winDim.width, winDim.height);

        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed();
    }

}
