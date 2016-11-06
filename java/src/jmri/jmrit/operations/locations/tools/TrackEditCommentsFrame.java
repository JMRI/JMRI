package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainSwitchListEditFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackEditCommentsFrame extends OperationsFrame {

    // text areas
    JTextArea commentBothTextArea = new JTextArea(5, 100);
    JScrollPane commentBothScroller = new JScrollPane(commentBothTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JTextArea commentPickupTextArea = new JTextArea(5, 100);
    JScrollPane commentPickupScroller = new JScrollPane(commentPickupTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JTextArea commentSetoutTextArea = new JTextArea(5, 100);
    JScrollPane commentSetoutScroller = new JScrollPane(commentSetoutTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    Dimension minScrollerDim = new Dimension(1200, 300);

    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

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
        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel pCb = new JPanel();
        pCb.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentBoth")));
        pCb.setLayout(new GridBagLayout());
        commentBothScroller.setMinimumSize(minScrollerDim);
        addItem(pCb, commentBothScroller, 1, 0);

        JPanel pCp = new JPanel();
        pCp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentPickup")));
        pCp.setLayout(new GridBagLayout());
        commentPickupScroller.setMinimumSize(minScrollerDim);
        addItem(pCp, commentPickupScroller, 1, 0);

        JPanel pCs = new JPanel();
        pCs.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CommentSetout")));
        pCs.setLayout(new GridBagLayout());
        commentSetoutScroller.setMinimumSize(minScrollerDim);
        addItem(pCs, commentSetoutScroller, 1, 0);

        commentBothTextArea.setText(track.getCommentBoth());
        commentPickupTextArea.setText(track.getCommentPickup());
        commentSetoutTextArea.setText(track.getCommentSetout());

        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, saveButton, 0, 0);

        getContentPane().add(pCb);
        getContentPane().add(pCp);
        getContentPane().add(pCs);
        getContentPane().add(pB);

        addButtonAction(saveButton);

        setTitle(track.getName());
        initMinimumSize(new Dimension(Control.panelHeight400, Control.panelWidth600));
    }

    // Buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            _track.setCommentBoth(commentBothTextArea.getText());
            _track.setCommentPickup(commentPickupTextArea.getText());
            _track.setCommentSetout(commentSetoutTextArea.getText());
            // save location file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                super.dispose();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainSwitchListEditFrame.class
            .getName());
}
