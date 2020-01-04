package jmri.jmrit.operations.setup;

import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManifestHeaderText;

/**
 * Frame for user edit of manifest header text strings
 *
 * @author Dan Boudreau Copyright (C) 2014
 * 
 */
public class EditManifestHeaderTextPanel extends OperationsPreferencesPanel {

//    private static final Logger log = LoggerFactory.getLogger(OperationsSetupPanel.class);

    //protected static final ResourceBundle rb = ResourceBundle
    //        .getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton resetButton = new JButton(Bundle.getMessage("Reset"));

    // car and engine attributes
    JTextField road_TextField = new JTextField(25);
    JTextField number_TextField = new JTextField(25);
    JTextField engineNumber_TextField = new JTextField(25);
    JTextField type_TextField = new JTextField(25);
    JTextField length_TextField = new JTextField(25);
    JTextField weight_TextField = new JTextField(25);
    JTextField owner_TextField = new JTextField(25);
    JTextField track_TextField = new JTextField(25);
    JTextField location_TextField = new JTextField(25);
    JTextField destination_TextField = new JTextField(25);
    JTextField dest_track_TextField = new JTextField(25);
    JTextField comment_TextField = new JTextField(25);
    // car attributes
    JTextField load_TextField = new JTextField(25);
    JTextField load_type_TextField = new JTextField(25);
    JTextField hazardous_TextField = new JTextField(25);
    JTextField color_TextField = new JTextField(25);
    JTextField kernel_TextField = new JTextField(25);
    JTextField final_dest_TextField = new JTextField(25);
    JTextField final_dest_track_TextField = new JTextField(25);
    JTextField drop_comment_TextField = new JTextField(25);
    JTextField pickup_comment_TextField = new JTextField(25);
    JTextField rwe_TextField = new JTextField(25);
    // engine attributes
    JTextField model_TextField = new JTextField(25);
    JTextField consist_TextField = new JTextField(25);

    public EditManifestHeaderTextPanel() {

        // the following code sets the frame's initial state
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // manifest text fields
        JPanel pManifest = new JPanel();
        JScrollPane pManifestPane = new JScrollPane(pManifest);
        pManifestPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutManifestHeader")));
        pManifest.setLayout(new BoxLayout(pManifest, BoxLayout.Y_AXIS));

        JPanel pRoad_TextField = new JPanel();
        pRoad_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Road")));
        pRoad_TextField.add(road_TextField);
        road_TextField.setText(TrainManifestHeaderText.getStringHeader_Road());
        pManifest.add(pRoad_TextField);

        JPanel pNumber_TextField = new JPanel();
        pNumber_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Number") + " ("
                + Bundle.getMessage("Car") + ")"));
        pNumber_TextField.add(number_TextField);
        number_TextField.setText(TrainManifestHeaderText.getStringHeader_Number());
        pManifest.add(pNumber_TextField);

        JPanel pEngineNumber_TextField = new JPanel();
        pEngineNumber_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Number") + " ("
                + Bundle.getMessage("Loco") + ")"));
        pEngineNumber_TextField.add(engineNumber_TextField);
        engineNumber_TextField.setText(TrainManifestHeaderText.getStringHeader_EngineNumber());
        pManifest.add(pEngineNumber_TextField);

        JPanel pType_TextField = new JPanel();
        pType_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        pType_TextField.add(type_TextField);
        type_TextField.setText(TrainManifestHeaderText.getStringHeader_Type());
        pManifest.add(pType_TextField);

        JPanel pLength_TextField = new JPanel();
        pLength_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Length")));
        pLength_TextField.add(length_TextField);
        length_TextField.setText(TrainManifestHeaderText.getStringHeader_Length());
        pManifest.add(pLength_TextField);
        
        JPanel pWeight_TextField = new JPanel();
        pWeight_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Weight")));
        pWeight_TextField.add(weight_TextField);
        weight_TextField.setText(TrainManifestHeaderText.getStringHeader_Weight());
        pManifest.add(pWeight_TextField);

        JPanel pOwner_TextField = new JPanel();
        pOwner_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Owner")));
        pOwner_TextField.add(owner_TextField);
        owner_TextField.setText(TrainManifestHeaderText.getStringHeader_Owner());
        pManifest.add(pOwner_TextField);

        JPanel pTrack_TextField = new JPanel();
        pTrack_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        pTrack_TextField.add(track_TextField);
        track_TextField.setText(TrainManifestHeaderText.getStringHeader_Track());
        pManifest.add(pTrack_TextField);

        JPanel pLocation_TextField = new JPanel();
        pLocation_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocation_TextField.add(location_TextField);
        location_TextField.setText(TrainManifestHeaderText.getStringHeader_Location());
        pManifest.add(pLocation_TextField);

        JPanel pDestination_TextField = new JPanel();
        pDestination_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Destination")));
        pDestination_TextField.add(destination_TextField);
        destination_TextField.setText(TrainManifestHeaderText.getStringHeader_Destination());
        pManifest.add(pDestination_TextField);

        JPanel pDest_Track_TextField = new JPanel();
        pDest_Track_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Dest&Track")));
        pDest_Track_TextField.add(dest_track_TextField);
        dest_track_TextField.setText(TrainManifestHeaderText.getStringHeader_Dest_Track());
        pManifest.add(pDest_Track_TextField);

        JPanel pComment_TextField = new JPanel();
        pComment_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        pComment_TextField.add(comment_TextField);
        comment_TextField.setText(TrainManifestHeaderText.getStringHeader_Comment());
        pManifest.add(pComment_TextField);

        // car attributes
        JPanel pLoad_TextField = new JPanel();
        pLoad_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
        pLoad_TextField.add(load_TextField);
        load_TextField.setText(TrainManifestHeaderText.getStringHeader_Load());
        pManifest.add(pLoad_TextField);
        
        JPanel pLoad_Type_TextField = new JPanel();
        pLoad_Type_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load_Type")));
        pLoad_Type_TextField.add(load_type_TextField);
        load_type_TextField.setText(TrainManifestHeaderText.getStringHeader_Load_Type());
        pManifest.add(pLoad_Type_TextField);

        JPanel pHazardous_TextField = new JPanel();
        pHazardous_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Hazardous")));
        pHazardous_TextField.add(hazardous_TextField);
        hazardous_TextField.setText(TrainManifestHeaderText.getStringHeader_Hazardous());
        pManifest.add(pHazardous_TextField);

        JPanel pColor_TextField = new JPanel();
        pColor_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Color")));
        pColor_TextField.add(color_TextField);
        color_TextField.setText(TrainManifestHeaderText.getStringHeader_Color());
        pManifest.add(pColor_TextField);

        JPanel pKernel_TextField = new JPanel();
        pKernel_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Kernel")));
        pKernel_TextField.add(kernel_TextField);
        kernel_TextField.setText(TrainManifestHeaderText.getStringHeader_Kernel());
        pManifest.add(pKernel_TextField);

        JPanel pFinal_Dest_TextField = new JPanel();
        pFinal_Dest_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Final_Dest")));
        pFinal_Dest_TextField.add(final_dest_TextField);
        final_dest_TextField.setText(TrainManifestHeaderText.getStringHeader_Final_Dest());
        pManifest.add(pFinal_Dest_TextField);

        JPanel pFinal_Dest_Track_TextField = new JPanel();
        pFinal_Dest_Track_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("FD&Track")));
        pFinal_Dest_Track_TextField.add(final_dest_track_TextField);
        final_dest_track_TextField.setText(TrainManifestHeaderText.getStringHeader_Final_Dest_Track());
        pManifest.add(pFinal_Dest_Track_TextField);

        JPanel pDrop_Comment_TextField = new JPanel();
        pDrop_Comment_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SetOut_Msg")));
        pDrop_Comment_TextField.add(drop_comment_TextField);
        drop_comment_TextField.setText(TrainManifestHeaderText.getStringHeader_Drop_Comment());
        pManifest.add(pDrop_Comment_TextField);

        JPanel pPickup_Comment_TextField = new JPanel();
        pPickup_Comment_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PickUp_Msg")));
        pPickup_Comment_TextField.add(pickup_comment_TextField);
        pickup_comment_TextField.setText(TrainManifestHeaderText.getStringHeader_Pickup_Comment());
        pManifest.add(pPickup_Comment_TextField);

        JPanel pRWE_TextField = new JPanel();
        pRWE_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RWELabel")));
        pRWE_TextField.add(rwe_TextField);
        rwe_TextField.setText(TrainManifestHeaderText.getStringHeader_RWE());
        pManifest.add(pRWE_TextField);

        // engine attributes
        JPanel pModel_TextField = new JPanel();
        pModel_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Model")));
        pModel_TextField.add(model_TextField);
        model_TextField.setText(TrainManifestHeaderText.getStringHeader_Model());
        pManifest.add(pModel_TextField);

        JPanel pConsist_TextField = new JPanel();
        pConsist_TextField.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Consist")));
        pConsist_TextField.add(consist_TextField);
        consist_TextField.setText(TrainManifestHeaderText.getStringHeader_Consist());
        pManifest.add(pConsist_TextField);

        // add tool tips
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));

        // row 11
        JPanel pControl = new JPanel();
        pControl.setBorder(BorderFactory.createTitledBorder(""));
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, resetButton, 0, 0);
        addItem(pControl, saveButton, 1, 0);

        add(pManifestPane);
        add(pControl);

        // set up buttons
        addButtonAction(resetButton);
        addButtonAction(saveButton);

        initMinimumSize();
    }

    // Save buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == resetButton) {
            road_TextField.setText(Bundle.getMessage("Road"));
            number_TextField.setText(Bundle.getMessage("Number"));
            engineNumber_TextField.setText(Bundle.getMessage("Number"));
            type_TextField.setText(Bundle.getMessage("Type"));
            length_TextField.setText(Bundle.getMessage("Length"));
            weight_TextField.setText(Bundle.getMessage("Weight"));
            owner_TextField.setText(Bundle.getMessage("Owner"));
            track_TextField.setText(Bundle.getMessage("Track"));
            location_TextField.setText(Bundle.getMessage("Location"));
            destination_TextField.setText(Bundle.getMessage("Destination"));
            dest_track_TextField.setText(Bundle.getMessage("Dest&Track"));
            comment_TextField.setText(Bundle.getMessage("Comment"));
            // car attributes
            load_TextField.setText(Bundle.getMessage("Load"));
            load_type_TextField.setText(Bundle.getMessage("Load_Type"));
            hazardous_TextField.setText(Bundle.getMessage("Hazardous"));
            color_TextField.setText(Bundle.getMessage("Color"));
            final_dest_TextField.setText(Bundle.getMessage("Final_Dest"));
            final_dest_track_TextField.setText(Bundle.getMessage("FD&Track"));
            drop_comment_TextField.setText(Bundle.getMessage("SetOut_Msg"));
            pickup_comment_TextField.setText(Bundle.getMessage("PickUp_Msg"));
            kernel_TextField.setText(Bundle.getMessage("Kernel"));
            rwe_TextField.setText(Bundle.getMessage("RWE"));
            // engine attributes
            model_TextField.setText(Bundle.getMessage("Model"));
            consist_TextField.setText(Bundle.getMessage("Consist"));
        }
        if (ae.getSource() == saveButton) {
            this.savePreferences();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleManifestHeaderText");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        // car and engine attributes
        TrainManifestHeaderText.setStringHeader_Road(road_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Number(number_TextField.getText());
        TrainManifestHeaderText.setStringHeader_EngineNumber(engineNumber_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Type(type_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Length(length_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Weight(weight_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Owner(owner_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Track(track_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Location(location_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Destination(destination_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Dest_Track(dest_track_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Comment(comment_TextField.getText());
        // car attributes
        TrainManifestHeaderText.setStringHeader_Load(load_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Load_Type(load_type_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Hazardous(hazardous_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Color(color_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Final_Dest(final_dest_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Final_Dest_Track(final_dest_track_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Drop_Comment(drop_comment_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Pickup_Comment(pickup_comment_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Kernel(kernel_TextField.getText());
        TrainManifestHeaderText.setStringHeader_RWE(rwe_TextField.getText());
        // engine attributes
        TrainManifestHeaderText.setStringHeader_Model(model_TextField.getText());
        TrainManifestHeaderText.setStringHeader_Consist(consist_TextField.getText());

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();

        // recreate all train manifests
        InstanceManager.getDefault(TrainManager.class).setTrainsModified();
    }

    @Override
    public boolean isDirty() {
        return (!TrainManifestHeaderText.getStringHeader_Road().equals(road_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Number().equals(number_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_EngineNumber().equals(engineNumber_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Type().equals(type_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Length().equals(length_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Weight().equals(weight_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Owner().equals(owner_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Track().equals(track_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Location().equals(location_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Destination().equals(destination_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Dest_Track().equals(dest_track_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Comment().equals(comment_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Load().equals(load_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Load_Type().equals(load_type_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Hazardous().equals(hazardous_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Color().equals(color_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Final_Dest().equals(final_dest_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Final_Dest_Track().equals(final_dest_track_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Drop_Comment().equals(drop_comment_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Pickup_Comment().equals(pickup_comment_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Kernel().equals(kernel_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_RWE().equals(rwe_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Model().equals(model_TextField.getText())
                || !TrainManifestHeaderText.getStringHeader_Consist().equals(consist_TextField.getText()));
    }
}
