package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

/**
 * A very specific dialog for editing the properties of a ThrottleFrame
 * object.
 * @author		Original Unknown
 * @author		Ken Cameron, copyright 2008
 * @version     $Revision: 1.5 $
 */
public class ThrottleFramePropertyEditor extends JDialog
{
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.throttle.ThrottleBundle");
    
    private ThrottleFrame frame;
	
	private JTextField titleField;
	
	private JList titleType;

    private String [] titleTextTypes = {"address", "text", "textAddress", "addressText"};
    private String [] titleTextTypeNames = {
    		rb.getString("SelectTitleTypeADDRESS"),
    		rb.getString("SelectTitleTypeTEXT"),
    		rb.getString("SelectTitleTypeTEXTADDRESS"),
    		rb.getString("SelectTitleTypeADDRESSTEXT")
    };
	
    /**
     * Constructor. Create it and pack it.
     */
    public ThrottleFramePropertyEditor()
    {
//        initGUI();
//		//this.setModal(true);
//        pack();
    }

    /**
     * Create, initilize, and place the GUI objects.
     */
    private void initGUI()
    {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.setTitle(rb.getString("EditThrottleFrameTitle"));
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());

        JPanel propertyPanel = new JPanel();
        propertyPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        titleField = new JTextField();
        titleField.setColumns(24);
        propertyPanel.add(new JLabel(rb.getString("FrameTitlePrompt")), constraints);

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridx++;
        propertyPanel.add(titleField, constraints);

        titleType = new JList(titleTextTypeNames);
        titleType.setVisibleRowCount(titleTextTypeNames.length);
        titleType.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < titleTextTypes.length; i++) {
        	if (titleTextTypes[i] == frame.titleTextType)
            	titleType.setSelectedIndex(i);
        }
        constraints.gridy++;
        constraints.gridx = 0;
        propertyPanel.add(new JLabel(rb.getString("SelectTitleTypePrompt")), constraints);
        constraints.gridx++;
        propertyPanel.add(titleType, constraints);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 4, 4));

        JButton saveButton = new JButton(rb.getString("ButtonOk"));
        saveButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveProperties();
            }
        });


        JButton cancelButton = new JButton(rb.getString("ButtonCancel"));
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                finishEdit();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(propertyPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	
    /**
     * Set the ThrottleFrame used here. Does some initialization of the
     * Frame.
     */
    public void setThrottleFrame(ThrottleFrame f)
    {
        this.frame = f;
        initGUI();
        pack();
        titleField.setText(frame.titleText);
		titleField.selectAll();
    }

    /**
     * Save the user-modified properties back to the ThrottleFrame.
     */
    private void saveProperties()
    {
        if (isDataValid())
        {
        	frame.titleText = titleField.getText();
            frame.titleTextType = titleTextTypes[titleType.getSelectedIndex()];
            frame.setFrameTitle();
            finishEdit();
        }
    }

    /**
     * Finish the editing process. Hide the dialog.
     */
    private void finishEdit()
    {
        this.setVisible(false);
    }

    /**
     * Verify the data on the dialog. If invalid, notify user of errors.
     */
    private boolean isDataValid()
    {
        StringBuffer errors = new StringBuffer();
        int errorNumber = 0;


        if (errorNumber > 0)
        {
            JOptionPane.showMessageDialog(this, errors,
                    rb.getString("ErrorOnPage"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
	
}

