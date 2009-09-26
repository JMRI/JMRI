package jmri.jmrit.roster;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.util.DnDImagePanel;

/**
 * A media pane for roster configuration tool, contains:
 *      + a selector for roster image (a large image for throttle background...)
 *      + a selector for roster icon (a small image for list displays...)
 *      + a selector for roster URL (link to wikipedia page about prototype...)
 *
 * @author Lionel Jeanson - Copyright 2009
 */
public class RosterMediaPane extends javax.swing.JPanel {

	private static final long serialVersionUID = 2420617780437463773L;
	JLabel _imageFPlabel = new JLabel();
    DnDImagePanel _imageFilePath;
    JLabel _iconFPlabel = new JLabel();
    DnDImagePanel _iconFilePath;
    JLabel _URLlabel = new JLabel();
    JTextField _URL = new JTextField(30);

    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    public RosterMediaPane(RosterEntry r) {
        _imageFilePath = new DnDImagePanel(r.getImagePath(), 320, 240);
        _imageFilePath.setToolTipText(rb.getString("MediaRosterImageToolTip"));
        _imageFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _imageFPlabel.setText(rb.getString("MediaRosterImageLabel"));

        _iconFilePath = new DnDImagePanel(r.getIconPath(), 160, 120);
        _iconFilePath.setToolTipText(rb.getString("MediaRosterIconToolTip"));
        _iconFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _iconFPlabel.setText(rb.getString("MediaRosterIconLabel"));

        _URL.setText(r.getURL());
        _URL.setToolTipText(rb.getString("MediaRosterURLToolTip"));
        _URLlabel.setText(rb.getString("MediaRosterURLLabel"));

        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(150,20);
        Dimension minImageDim = new Dimension(320,200);
        setLayout(gbLayout);

        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets (0,0,0,15);
        gbLayout.setConstraints( _imageFPlabel,cL);
        add( _imageFPlabel);

        cR.gridy = 1;
        cR.anchor = GridBagConstraints.WEST;
        _imageFilePath.setMinimumSize(minImageDim);
        gbLayout.setConstraints( _imageFilePath,cR);
        add( _imageFilePath);


        cL.gridy = 2;
        gbLayout.setConstraints( _iconFPlabel,cL);
        add( _iconFPlabel);

        cR.gridy = 3;
        _iconFilePath.setMinimumSize(minImageDim);
        gbLayout.setConstraints( _iconFilePath,cR);
        add( _iconFilePath);

        cL.gridy = 4;
        gbLayout.setConstraints(_URLlabel,cL);
        add(_URLlabel);

        cR.gridy = 5;
        _URL.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(_URL,cR);
        add(_URL);

    }

    public boolean guiChanged(RosterEntry r) {
        if (!r.getURL().equals(_URL.getText())) return true;
        if (!r.getImagePath().equals(_imageFilePath.getImagePath())) return true;
        if (!r.getIconPath().equals(_iconFilePath.getImagePath())) return true;
        return false;
    }

    public void update(RosterEntry r) {
        r.setURL(_URL.getText()) ;
        r.setImagePath(_imageFilePath.getImagePath()) ;
        r.setIconPath(_iconFilePath.getImagePath()) ;
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterMediaPane.class.getName());
}
