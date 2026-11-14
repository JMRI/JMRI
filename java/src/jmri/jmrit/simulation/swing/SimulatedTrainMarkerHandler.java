package jmri.jmrit.simulation.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import jmri.Block;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.layoutEditor.TrackMarkerDropHandler;
import jmri.jmrit.simulation.SimulatedLayout;
import jmri.jmrit.simulation.SimulationCheck;
import jmri.jmrit.simulation.VirtualTrain;

import org.openide.util.lookup.ServiceProvider;

/**
 * In a profile whose connections are all simulators, a loco marker dropped on
 * a Block's track places a simulated train there, and the Block's menu takes
 * it off again. In any other profile this does nothing, and a dropped marker
 * is train tracking's alone.
 * <p>
 * Train tracking is told the train is there only by running what Layout
 * Editor hands over, when the operator leaves that ticked: the simulator
 * itself knows nothing of train records.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
@ServiceProvider(service = TrackMarkerDropHandler.class)
public class SimulatedTrainMarkerHandler implements TrackMarkerDropHandler {

    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return SimulationCheck.isFullySimulated();
    }

    /** {@inheritDoc} */
    @Override
    public void markerDropped(@Nonnull LocoIcon marker, @Nonnull Block block, @Nonnull Component parent,
            @Nonnull Runnable tellTrainTracking) {
        String text = marker.getText();
        if (text == null || text.isEmpty()) {
            text = marker.getUnRotatedText();
        }
        PlaceTrainPanel panel = new PlaceTrainPanel(new PlaceTrainModel(marker.getRosterEntry(), text, block));
        String title = Bundle.getMessage("PlaceTitle");
        while (true) {
            int answer = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                panel.place();
                break;
            } catch (IllegalArgumentException | IllegalStateException e) {
                String why = e.getMessage();
                JOptionPane.showMessageDialog(parent, why == null ? Bundle.getMessage("PlaceFailed") : why, title,
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        if (panel.isTellTrainTracking()) {
            tellTrainTracking.run();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addBlockMenuItems(@Nonnull JPopupMenu popup, @Nonnull Block block) {
        for (VirtualTrain train : PlaceTrainModel.trainsIn(block)) {
            popup.add(new AbstractAction(Bundle.getMessage("RemoveTrain", train.getName())) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SimulatedLayout.getDefault().remove(train);
                }
            });
        }
    }
}
