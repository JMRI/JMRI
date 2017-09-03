
package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.TrackEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Planned Pick ups.
 * Frame to allow a user to define how much used track space is to be ignored
 * by the program when placing new rolling stock onto a track.
 *
 * @author Daniel Boudreau Copyright (C) 2012, 2017
 * 
 */
class IgnoreUsedTrackFrame extends OperationsFrame {

    // radio buttons
    JRadioButton zeroPercent = new JRadioButton(Bundle.getMessage("Disabled"));
    JRadioButton twentyfivePercent = new JRadioButton("25%"); // NOI18N
    JRadioButton fiftyPercent = new JRadioButton("50%");  // NOI18N
    JRadioButton seventyfivePercent = new JRadioButton("75%"); // NOI18N
    JRadioButton hundredPercent = new JRadioButton("100%");  // NOI18N

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    private TrackEditFrame _tef;
    protected Track _track;

    public IgnoreUsedTrackFrame(TrackEditFrame tef) {
        super();

        setTitle(Bundle.getMessage("MenuItemPlannedPickups"));

        _tef = tef;
        _track = _tef._track;
        if (_track == null) {
            log.debug("track is null!");
            return;
        }

        // load the panel
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PrePlanedPickups")));

        p1.add(zeroPercent);
        p1.add(twentyfivePercent);
        p1.add(fiftyPercent);
        p1.add(seventyfivePercent);
        p1.add(hundredPercent);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(zeroPercent);
        buttonGroup.add(twentyfivePercent);
        buttonGroup.add(fiftyPercent);
        buttonGroup.add(seventyfivePercent);
        buttonGroup.add(hundredPercent);

        // select the correct radio button
        int percentage = _track.getIgnoreUsedLengthPercentage();
        zeroPercent.setSelected(percentage >= 0);
        twentyfivePercent.setSelected(percentage >= 25);
        fiftyPercent.setSelected(percentage >= 50);
        seventyfivePercent.setSelected(percentage >= 75);
        hundredPercent.setSelected(percentage >= 100);
        
        // warning text for planned pick ups.
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(new JLabel(Bundle.getMessage("PPWarningMessage")));
        p2.add(new JLabel(Bundle.getMessage("PPWarningMessage2")));
        
        JPanel pW = new JPanel();
        pW.setLayout(new GridBagLayout());
        addItem(pW, p2, 0, 1);
        addItem(pW, saveButton, 0, 2);

        getContentPane().add(p1);
        getContentPane().add(pW);

        addButtonAction(saveButton);
        
        addHelpMenu("package.jmri.jmrit.operations.Operations_PlannedPickUps", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // save percentage selected
            int percentage = 0;
            if (twentyfivePercent.isSelected()) {
                percentage = 25;
            } else if (fiftyPercent.isSelected()) {
                percentage = 50;
            } else if (seventyfivePercent.isSelected()) {
                percentage = 75;
            } else if (hundredPercent.isSelected()) {
                percentage = 100;
            }
            if (_track != null) {
                _track.setIgnoreUsedLengthPercentage(percentage);
            }
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(IgnoreUsedTrackFrame.class);
}
