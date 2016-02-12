package jmri.jmrit.picker;

import javax.swing.JMenuBar;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PickFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -3288532933990677427L;

    public PickFrame(String title) {
        setTitle(title);
        /*
         addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent e) {
         dispose();
         }
         });
         */
        makeMenus();

        PickListModel[] models = {PickListModel.turnoutPickModelInstance(),
            PickListModel.sensorPickModelInstance(),
            PickListModel.signalHeadPickModelInstance(),
            PickListModel.signalMastPickModelInstance(),
            PickListModel.memoryPickModelInstance(),
            PickListModel.reporterPickModelInstance(),
            PickListModel.lightPickModelInstance(),
            PickListModel.warrantPickModelInstance(),
            PickListModel.oBlockPickModelInstance(),
            PickListModel.conditionalPickModelInstance(),
            PickListModel.entryExitPickModelInstance(),};
        setContentPane(new PickPanel(models));
        setVisible(true);
        pack();
    }

    private void makeMenus() {
        JMenuBar menuBar = new JMenuBar();
        //JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        //fileMenu.add(new jmri.configurexml.SaveMenu());
        //menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.picker.PickTables", true);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PickFrame.class.getName());
}
