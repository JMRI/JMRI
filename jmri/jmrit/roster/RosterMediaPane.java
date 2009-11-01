package jmri.jmrit.roster;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.util.ResizableImagePanel;

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
    ResizableImagePanel _imageFilePath;
    JLabel _iconFPlabel = new JLabel();
    ResizableImagePanel _iconFilePath;
    JLabel _URLlabel = new JLabel();
    JTextField _URL = new JTextField(30);
    JButton jbRemoveIcon = new JButton();
    JButton jbRemoveImage = new JButton();

    final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    public RosterMediaPane(RosterEntry r) {
        _imageFilePath = new ResizableImagePanel(r.getImagePath(), 320, 240);
        _imageFilePath.setDnd(true);
        _imageFilePath.setToolTipText(rb.getString("MediaRosterImageToolTip"));
        _imageFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _imageFPlabel.setText(rb.getString("MediaRosterImageLabel"));

        _iconFilePath = new ResizableImagePanel(r.getIconPath(), 160, 120);
        _iconFilePath.setDnd(true);
        _iconFilePath.setToolTipText(rb.getString("MediaRosterIconToolTip"));
        _iconFilePath.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        _iconFPlabel.setText(rb.getString("MediaRosterIconLabel"));

        _URL.setText(r.getURL());
        _URL.setToolTipText(rb.getString("MediaRosterURLToolTip"));
        _URLlabel.setText(rb.getString("MediaRosterURLLabel"));
        
        jbRemoveImage.setText(rb.getString("MediaRosterIconRemove"));
        jbRemoveIcon.setText(rb.getString("MediaRosterImageRemove"));
        
        jbRemoveImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_imageFilePath.setImagePath(null);
			}
        });
        jbRemoveIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_iconFilePath.setImagePath(null);
			}
        });
        
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(150,20);
        Dimension minImageDim = new Dimension(320,200);
        setLayout(gbLayout);

        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbLayout.setConstraints( _imageFPlabel,gbc);
        add( _imageFPlabel);

        gbc.gridx = 1;
        gbc.gridy = 0;
        _imageFilePath.setMinimumSize(minImageDim);
        gbLayout.setConstraints( _imageFilePath,gbc);
        add( _imageFilePath);
        
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbLayout.setConstraints( jbRemoveImage,gbc);
        add( jbRemoveImage);    

        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbLayout.setConstraints( _iconFPlabel,gbc);
        add( _iconFPlabel);

        gbc.gridx = 1;
        gbc.gridy = 2;
        _iconFilePath.setMinimumSize(minImageDim);
        gbLayout.setConstraints( _iconFilePath,gbc);
        add( _iconFilePath);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbLayout.setConstraints( jbRemoveIcon,gbc);
        add( jbRemoveIcon);    

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbLayout.setConstraints(_URLlabel,gbc);
        add(_URLlabel);

        gbc.gridx = 1;
        gbc.gridy = 4;
        _URL.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(_URL,gbc);
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
