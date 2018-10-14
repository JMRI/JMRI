package jmri.jmrit.picker;

import javax.swing.JMenuBar;
import jmri.util.JmriJFrame;

public class PickFrame extends JmriJFrame {

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
            PickListModel.entryExitPickModelInstance(),
            PickListModel.logixPickModelInstance()};
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
}
