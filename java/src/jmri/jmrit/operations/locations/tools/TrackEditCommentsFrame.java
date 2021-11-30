package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainCommon;

public class TrackEditCommentsFrame extends OperationsFrame {

    // text areas
    JTextArea commentBothTextArea = new JTextArea(5, 100);
    JTextArea commentPickupTextArea = new JTextArea(5, 100);
    JTextArea commentSetoutTextArea = new JTextArea(5, 100);
    
    // scrollers
    JScrollPane commentBothScroller = new JScrollPane(commentBothTextArea);
    JScrollPane commentPickupScroller = new JScrollPane(commentPickupTextArea);
    JScrollPane commentSetoutScroller = new JScrollPane(commentSetoutTextArea);
    
    // text color choosers
    JColorChooser commentColorChooserBoth = new JColorChooser();
    JColorChooser commentColorChooserPickup = new JColorChooser();
    JColorChooser commentColorChooserSetout = new JColorChooser();

    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    
    JCheckBox printManifest = new JCheckBox(Bundle.getMessage("PrintManifest"));
    JCheckBox printSwitchList = new JCheckBox(Bundle.getMessage("PrintSwitchList"));

    Track _track;

    public TrackEditCommentsFrame(Track track) {
        super();
        initComponents(track);
    }

    private void initComponents(Track track) {
        if (track == null) {
            log.debug("Track is null can't edit track comments");
            return;
        }
        _track = track;
        
        // Layout the panel by rows
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel panelComments = new JPanel();
        JScrollPane panelPane = new JScrollPane(panelComments);
        panelComments.setLayout(new BoxLayout(panelComments, BoxLayout.Y_AXIS));
        
        panelPane.setBorder(BorderFactory.createTitledBorder(""));

        JPanel pCb = new JPanel();
        pCb.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentBoth")));
        pCb.setLayout(new GridBagLayout());
        addItem(pCb, commentBothScroller, 1, 0);
        
        addItem(pCb, OperationsPanel.getColorChooserPanel(track.getCommentBoth(), commentColorChooserBoth), 2, 0);

        JPanel pCp = new JPanel();
        pCp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentPickup")));
        pCp.setLayout(new GridBagLayout());
        addItem(pCp, commentPickupScroller, 1, 0);
        
        addItem(pCp, OperationsPanel.getColorChooserPanel(track.getCommentPickup(), commentColorChooserPickup), 2, 0);

        JPanel pCs = new JPanel();
        pCs.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentSetout")));
        pCs.setLayout(new GridBagLayout());
        addItem(pCs, commentSetoutScroller, 1, 0);
        
        addItem(pCs, OperationsPanel.getColorChooserPanel(track.getCommentSetout(), commentColorChooserSetout), 2, 0);

        commentBothTextArea.setText(TrainCommon.getTextColorString(track.getCommentBoth()));
        commentPickupTextArea.setText(TrainCommon.getTextColorString(track.getCommentPickup()));
        commentSetoutTextArea.setText(TrainCommon.getTextColorString(track.getCommentSetout()));

        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, printManifest, 0, 0);
        addItem(pB, printSwitchList, 1, 0);
        addItem(pB, saveButton, 2, 0);
        
        printManifest.setSelected(track.isPrintManifestCommentEnabled());
        printSwitchList.setSelected(track.isPrintSwitchListCommentEnabled());

        panelComments.add(pCb);
        panelComments.add(pCp);
        panelComments.add(pCs);
        
        add(panelPane);
        add(pB);

        addButtonAction(saveButton);

        setTitle(track.getName());
        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight400));
    }

    // Buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            _track.setCommentBoth(TrainCommon.formatColorString(commentBothTextArea.getText(), commentColorChooserBoth.getColor()));
            _track.setCommentPickup(TrainCommon.formatColorString(commentPickupTextArea.getText(), commentColorChooserPickup.getColor()));
            _track.setCommentSetout(TrainCommon.formatColorString(commentSetoutTextArea.getText(), commentColorChooserSetout.getColor()));
            _track.setPrintManifestCommentEnabled(printManifest.isSelected());
            _track.setPrintSwitchListCommentEnabled(printSwitchList.isSelected());
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                super.dispose();
            }
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(TrackEditCommentsFrame.class
            .getName());
}
