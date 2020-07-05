package jmri.jmrit.mailreport;

import javax.swing.BoxLayout;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Frame for uploading debugging information
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
@API(status = MAINTAINED)
public class ReportFrame extends jmri.util.JmriJFrame {

    public ReportFrame() {
        super(false, true);
    }

    @Override
    public void initComponents() {

        setTitle(java.util.ResourceBundle.getBundle("jmri.jmrit.mailreport.ReportBundle").getString("Title"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(new ReportPanel());

        addHelpMenu("package.jmri.jmrit.mailreport.Report", true);

        pack();
    }
}
