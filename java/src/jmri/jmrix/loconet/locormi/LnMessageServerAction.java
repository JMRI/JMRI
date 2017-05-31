package jmri.jmrix.loconet.locormi;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Call LnTcpServerAction and migrate preferences to use that server.
 *
 * @author Alex Shepherd (C) 2002
 * @author Randall Wood (C) 2017
 */
public class LnMessageServerAction extends AbstractAction {

    private final static Logger log = LoggerFactory.getLogger(LnMessageServerAction.class);

    public LnMessageServerAction(String s) {
        super(s);
    }

    public LnMessageServerAction() {
        super("Start LocoNet server");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // start an LnTcpServer if not running already
        new LnTcpServerAction().actionPerformed(e);
        InstanceManager.getOptionalDefault(StartupActionsManager.class).ifPresent((manager) -> {
            if (manager.getActions(PerformActionModel.class).stream().anyMatch((model) -> (this.getClass().getName().equals(model.getClassName())))) {
                // notify user of migration
                log.error("The LocoNet Server is no longer supported.");
                log.error("This server will be converted to a LocoNetOverTCP server.");
                log.error("See the JMRI 4.7.5 release notes for more information.");
                if (!GraphicsEnvironment.isHeadless()) {
                    // avoid the class overhead of the standard Bundle construct
                    ResourceBundle bundle = ResourceBundle.getBundle("jmri.jmrix.loconet.locormi.Bundle");
                    JOptionPane.showMessageDialog(null,
                            bundle.getString("NoLocoRmiServerMessage"),
                            bundle.getString("NoLocoRmiServerTitle"),
                            JOptionPane.ERROR_MESSAGE);
                }
                // migrate
                new Thread(() -> {
                    Profile profile = ProfileManager.getDefault().getActiveProfile();
                    while (!manager.isInitialized(profile)) {
                        try {
                            Thread.currentThread().wait(100);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    if (manager.isInitialized(profile)) {
                        boolean hasTcpServer = manager.getActions(PerformActionModel.class).stream().anyMatch((model) -> (LnTcpServerAction.class.getName().equals(model.getClassName())));
                        List<PerformActionModel> models = new ArrayList<>();
                        manager.getActions(PerformActionModel.class).stream().filter((model) -> (this.getClass().getName().equals(model.getClassName()))).forEach((model) -> {
                            models.add(model);
                        });
                        for (PerformActionModel model : models) {
                            int startupActionPosition = Arrays.asList(manager.getActions()).indexOf(model);
                            manager.removeAction(model);
                            if (!hasTcpServer) {
                                PerformActionModel newModel = new PerformActionModel();
                                newModel.setClassName(LnTcpServerAction.class.getName());
                                manager.setActions(startupActionPosition, newModel);
                                hasTcpServer = true;
                            }
                        }
                    }
                }, "LocoRMI migration").start();
            }
        });
    }

}
