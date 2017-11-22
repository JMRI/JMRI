/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author geowar
 */
public class LayoutTrackDrawingOptionsDialog extends JDialog {

    private LayoutTrackDrawingOptions ltdOptions = null;

    private final List<LayoutTrackDrawingOptions> ltdoList = new ArrayList<>();

    /**
     * Creates new form LayoutTrackDrawingOptionsDialog
     */
    public LayoutTrackDrawingOptionsDialog(Frame parent, boolean modal,
            LayoutTrackDrawingOptions ltdOptions) {
        super(parent, modal);
        this.ltdOptions = new LayoutTrackDrawingOptions(ltdOptions);
        initComponents();

        LayoutTrackDrawingOptions tempLTDO = new LayoutTrackDrawingOptions("Classic JMRI");
        tempLTDO.setSideBallastWidth(0);
        tempLTDO.setSideBallastColor(null);
        tempLTDO.setSideTieLength(0);
        tempLTDO.setSideTieWidth(0);
        tempLTDO.setSideTieColor(null);
        tempLTDO.setSideTieGap(0);
        tempLTDO.setSideRailCount(1);
        tempLTDO.setSideRailWidth(2);
        tempLTDO.setSideRailGap(0);
        tempLTDO.setSideRailColor(Color.BLACK);
        tempLTDO.setSideBlockLineWidth(0);
        tempLTDO.setMainBallastWidth(0);
        tempLTDO.setMainBallastColor(null);
        tempLTDO.setMainTieLength(0);
        tempLTDO.setMainTieWidth(0);
        tempLTDO.setMainTieGap(0);
        tempLTDO.setMainTieColor(null);
        tempLTDO.setMainRailCount(1);
        tempLTDO.setMainRailWidth(4);
        tempLTDO.setMainRailGap(0);
        tempLTDO.setMainRailColor(Color.BLACK);
        tempLTDO.setMainBlockLineWidth(0);
        ltdoList.add(tempLTDO);

        tempLTDO = new LayoutTrackDrawingOptions("Drafting");
        tempLTDO.setSideBallastWidth(0);
        tempLTDO.setSideBallastColor(null);
        tempLTDO.setSideTieLength(0);
        tempLTDO.setSideTieWidth(0);
        tempLTDO.setSideTieColor(null);
        tempLTDO.setSideTieGap(0);
        tempLTDO.setSideRailCount(1);
        tempLTDO.setSideRailWidth(1);
        tempLTDO.setSideRailGap(0);
        tempLTDO.setSideRailColor(Color.DARK_GRAY);
        tempLTDO.setSideBlockLineWidth(0);
        tempLTDO.setMainBallastWidth(0);
        tempLTDO.setMainBallastColor(null);
        tempLTDO.setMainTieLength(0);
        tempLTDO.setMainTieWidth(0);
        tempLTDO.setMainTieGap(0);
        tempLTDO.setMainTieColor(null);
        tempLTDO.setMainRailCount(1);
        tempLTDO.setMainRailWidth(2);
        tempLTDO.setMainRailGap(0);
        tempLTDO.setMainRailColor(Color.DARK_GRAY);
        tempLTDO.setMainBlockLineWidth(0);
        ltdoList.add(tempLTDO);

        tempLTDO = new LayoutTrackDrawingOptions("Realistic");
        tempLTDO.setSideBallastWidth(13);
        tempLTDO.setSideBallastColor(Color.decode("#AEACAD"));
        tempLTDO.setSideTieLength(9);
        tempLTDO.setSideTieWidth(3);
        tempLTDO.setSideTieColor(Color.decode("#391E16"));
        tempLTDO.setSideTieGap(4);
        tempLTDO.setSideRailCount(2);
        tempLTDO.setSideRailWidth(1);
        tempLTDO.setSideRailGap(3);
        tempLTDO.setSideRailColor(Color.decode("#9B705E"));
        tempLTDO.setSideBlockLineWidth(3);
        tempLTDO.setMainBallastWidth(15);
        tempLTDO.setMainBallastColor(Color.decode("#9E9C9D"));
        tempLTDO.setMainTieLength(11);
        tempLTDO.setMainTieWidth(2);
        tempLTDO.setMainTieGap(5);
        tempLTDO.setMainTieColor(Color.decode("#D5CFCC"));
        tempLTDO.setMainRailCount(2);
        tempLTDO.setMainRailWidth(2);
        tempLTDO.setMainRailGap(3);
        tempLTDO.setMainRailColor(Color.decode("#C0BFBF"));
        tempLTDO.setMainBlockLineWidth(3);
        ltdoList.add(tempLTDO);

        tempLTDO = new LayoutTrackDrawingOptions("Garrish");
        tempLTDO.setSideBallastWidth(13);
        tempLTDO.setSideBallastColor(Color.decode("#CA0024"));
        tempLTDO.setSideTieLength(9);
        tempLTDO.setSideTieWidth(3);
        tempLTDO.setSideTieColor(Color.decode("#F26308"));
        tempLTDO.setSideTieGap(4);
        tempLTDO.setSideRailCount(2);
        tempLTDO.setSideRailWidth(1);
        tempLTDO.setSideRailGap(3);
        tempLTDO.setSideRailColor(Color.decode("#FDB3C2"));
        tempLTDO.setSideBlockLineWidth(3);
        tempLTDO.setMainBallastWidth(15);
        tempLTDO.setMainBallastColor(Color.decode("#B25A2B"));
        tempLTDO.setMainTieLength(11);
        tempLTDO.setMainTieWidth(2);
        tempLTDO.setMainTieGap(5);
        tempLTDO.setMainTieColor(Color.decode("#468FE3"));
        tempLTDO.setMainRailCount(2);
        tempLTDO.setMainRailWidth(2);
        tempLTDO.setMainRailGap(3);
        tempLTDO.setMainRailColor(Color.decode("#39FF12"));
        tempLTDO.setMainBlockLineWidth(3);
        ltdoList.add(tempLTDO);

        ltdoList.add(ltdOptions);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        OptionsPanel = new javax.swing.JPanel();
        ballastWidthLabel = new javax.swing.JLabel();
        mainBallastWidthSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel ballastColorLabel = new javax.swing.JLabel();
        mainBallastColorButton = new javax.swing.JButton();
        tieLengthLabel = new javax.swing.JLabel();
        mainTieLengthSpinner = new javax.swing.JSpinner();
        tieWidthLabel = new javax.swing.JLabel();
        mainTieWidthSpinner = new javax.swing.JSpinner();
        tieGapLabel = new javax.swing.JLabel();
        mainTieGapSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel tieColorLabel = new javax.swing.JLabel();
        mainTieColorButton = new javax.swing.JButton();
        railCountLabel = new javax.swing.JLabel();
        mainRailCountSpinner = new javax.swing.JSpinner();
        railWidthLabel = new javax.swing.JLabel();
        mainRailWidthSpinner = new javax.swing.JSpinner();
        railGapLabel = new javax.swing.JLabel();
        mainRailGapSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel railColorLabel = new javax.swing.JLabel();
        mainRailColorButton = new javax.swing.JButton();
        blockLineWidthLabel = new javax.swing.JLabel();
        mainBlockLineSpinner = new javax.swing.JSpinner();
        sideTieWidthSpinner = new javax.swing.JSpinner();
        sideRailColorButton = new javax.swing.JButton();
        sideTieGapSpinner = new javax.swing.JSpinner();
        sideBlockLineWidthSpinner = new javax.swing.JSpinner();
        sideTieColorButton = new javax.swing.JButton();
        sideBallastWidthSpinner = new javax.swing.JSpinner();
        sideRailCountSpinner = new javax.swing.JSpinner();
        sideBallastColorButton = new javax.swing.JButton();
        sideRailWidthSpinner = new javax.swing.JSpinner();
        sideTieLengthSpinner = new javax.swing.JSpinner();
        sideRailGapSpinner = new javax.swing.JSpinner();
        optionLabel = new javax.swing.JLabel();
        sideLineLabel = new javax.swing.JLabel();
        mainlineLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        previewLayeredPane = new javax.swing.JLayeredPane();
        presetsLabel = new javax.swing.JLabel();
        presetsComboBox = new javax.swing.JComboBox<>();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        OptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(ltdOptions.getName()));

        ballastWidthLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        ballastWidthLabel.setText("Ballast Width:");

        mainBallastWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainBallastWidthSpinner.setToolTipText("Set the width of the mainline ballast");
        mainBallastWidthSpinner.setValue(ltdOptions.getMainBallastWidth());
        mainBallastWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainBallastWidthSpinnerVetoableChange(evt);
            }
        });

        ballastColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        ballastColorLabel.setText("Ballast Color:");

        mainBallastColorButton.setToolTipText("Set the color of the mainline ballast");
        mainBallastColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainBallastColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainBallastColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainBallastColorButtonLayout = new javax.swing.GroupLayout(mainBallastColorButton);
        mainBallastColorButton.setLayout(mainBallastColorButtonLayout);
        mainBallastColorButtonLayout.setHorizontalGroup(
            mainBallastColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        mainBallastColorButtonLayout.setVerticalGroup(
            mainBallastColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        tieLengthLabel.setText("Tie Length:");

        mainTieLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieLengthSpinner.setToolTipText("Set the length of the mainline ties");
        mainTieLengthSpinner.setValue(ltdOptions.getMainTieLength());
        mainTieLengthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieLengthSpinnerVetoableChange(evt);
            }
        });

        tieWidthLabel.setText("Tie Width:");

        mainTieWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieWidthSpinner.setToolTipText("Set the width of the mainline ties");
        mainTieWidthSpinner.setValue(ltdOptions.getMainTieWidth());
        mainTieWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieWidthSpinnerVetoableChange(evt);
            }
        });

        tieGapLabel.setText("Tie Gap:");

        mainTieGapSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieGapSpinner.setToolTipText("Set the gap between the mainline ties");
        mainTieGapSpinner.setValue(ltdOptions.getMainTieGap());
        mainTieGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieGapSpinnerVetoableChange(evt);
            }
        });

        tieColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        tieColorLabel.setText("Tie Color:");

        mainTieColorButton.setToolTipText("Set the color of the mainline ties");
        mainTieColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainTieColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainTieColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainTieColorButtonLayout = new javax.swing.GroupLayout(mainTieColorButton);
        mainTieColorButton.setLayout(mainTieColorButtonLayout);
        mainTieColorButtonLayout.setHorizontalGroup(
            mainTieColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        mainTieColorButtonLayout.setVerticalGroup(
            mainTieColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        railCountLabel.setText("Rail Count:");

        mainRailCountSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        mainRailCountSpinner.setToolTipText("Select the number of mainline rails (1...3)");
        mainRailCountSpinner.setName(""); // NOI18N
        mainRailCountSpinner.setValue(ltdOptions.getMainRailCount());
        mainRailCountSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailCountSpinnerVetoableChange(evt);
            }
        });

        railWidthLabel.setText("Rail Width:");

        mainRailWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainRailWidthSpinner.setToolTipText("Select the number of mainline rails (1...3)");
        mainRailWidthSpinner.setName(""); // NOI18N
        mainRailWidthSpinner.setValue(ltdOptions.getMainRailWidth());
        mainRailWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailWidthSpinnerVetoableChange(evt);
            }
        });

        railGapLabel.setText("Rail Gap:");

        mainRailGapSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainRailGapSpinner.setToolTipText("Select the gap between the mainline rails");
        mainRailGapSpinner.setName(""); // NOI18N
        mainRailGapSpinner.setValue(ltdOptions.getMainRailGap());
        mainRailGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailGapSpinnerVetoableChange(evt);
            }
        });

        railColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        railColorLabel.setText("Rail Color:");

        mainRailColorButton.setToolTipText("Set the color of the mainline ties");
        mainRailColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainRailColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainRailColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout mainRailColorButtonLayout = new javax.swing.GroupLayout(mainRailColorButton);
        mainRailColorButton.setLayout(mainRailColorButtonLayout);
        mainRailColorButtonLayout.setHorizontalGroup(
            mainRailColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        mainRailColorButtonLayout.setVerticalGroup(
            mainRailColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        blockLineWidthLabel.setText("Block Line Width:");

        mainBlockLineSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainBlockLineSpinner.setToolTipText("Select the gap between the mainline rails");
        mainBlockLineSpinner.setName(""); // NOI18N
        mainBlockLineSpinner.setValue(ltdOptions.getMainRailGap());
        mainBlockLineSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainBlockLineSpinnerVetoableChange(evt);
            }
        });

        sideTieWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieWidthSpinner.setToolTipText("Set the width of the sideline ties");
        sideTieWidthSpinner.setValue(ltdOptions.getSideTieWidth());
        sideTieWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieWidthSpinnerVetoableChange(evt);
            }
        });

        sideRailColorButton.setToolTipText("Set the color of the sideline ties");
        sideRailColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideRailColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sideRailColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout sideRailColorButtonLayout = new javax.swing.GroupLayout(sideRailColorButton);
        sideRailColorButton.setLayout(sideRailColorButtonLayout);
        sideRailColorButtonLayout.setHorizontalGroup(
            sideRailColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        sideRailColorButtonLayout.setVerticalGroup(
            sideRailColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        sideTieGapSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieGapSpinner.setToolTipText("Set the gap between the sideline ties");
        sideTieGapSpinner.setValue(ltdOptions.getSideTieGap());
        sideTieGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieGapSpinnerVetoableChange(evt);
            }
        });

        sideBlockLineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideBlockLineWidthSpinner.setToolTipText("Select the gap between the sideline rails");
        sideBlockLineWidthSpinner.setName(""); // NOI18N
        sideBlockLineWidthSpinner.setValue(ltdOptions.getSideRailGap());
        sideBlockLineWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideBlockLineWidthSpinnerVetoableChange(evt);
            }
        });

        sideTieColorButton.setToolTipText("Set the color of the sideline ties");
        sideTieColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideTieColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mainRailColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout sideTieColorButtonLayout = new javax.swing.GroupLayout(sideTieColorButton);
        sideTieColorButton.setLayout(sideTieColorButtonLayout);
        sideTieColorButtonLayout.setHorizontalGroup(
            sideTieColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        sideTieColorButtonLayout.setVerticalGroup(
            sideTieColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        sideBallastWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideBallastWidthSpinner.setToolTipText("Set the width of the sideline ballast");
        sideBallastWidthSpinner.setValue(ltdOptions.getSideBallastWidth());
        sideBallastWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideBallastWidthSpinnerVetoableChange(evt);
            }
        });

        sideRailCountSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        sideRailCountSpinner.setToolTipText("Select the number of sideline rails (1...3)");
        sideRailCountSpinner.setName(""); // NOI18N
        sideRailCountSpinner.setValue(ltdOptions.getSideRailCount());
        sideRailCountSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailCountSpinnerVetoableChange(evt);
            }
        });

        sideBallastColorButton.setToolTipText("Set the color of the sideline ballast");
        sideBallastColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideBallastColorButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sideBallastColorButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout sideBallastColorButtonLayout = new javax.swing.GroupLayout(sideBallastColorButton);
        sideBallastColorButton.setLayout(sideBallastColorButtonLayout);
        sideBallastColorButtonLayout.setHorizontalGroup(
            sideBallastColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        sideBallastColorButtonLayout.setVerticalGroup(
            sideBallastColorButtonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 22, Short.MAX_VALUE)
        );

        sideRailWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideRailWidthSpinner.setToolTipText("Select the width of the sideline rails");
        sideRailWidthSpinner.setName(""); // NOI18N
        sideRailWidthSpinner.setValue(ltdOptions.getSideRailWidth());
        sideRailWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailWidthSpinnerVetoableChange(evt);
            }
        });

        sideTieLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieLengthSpinner.setToolTipText("Set the length of the sideline ties");
        sideTieLengthSpinner.setValue(ltdOptions.getSideTieLength());
        sideTieLengthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieLengthSpinnerVetoableChange(evt);
            }
        });

        sideRailGapSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideRailGapSpinner.setToolTipText("Select the gap between the sideline rails");
        sideRailGapSpinner.setName(""); // NOI18N
        sideRailGapSpinner.setValue(ltdOptions.getSideRailGap());
        sideRailGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailGapSpinnerVetoableChange(evt);
            }
        });

        optionLabel.setText("Option");

        sideLineLabel.setText("Sideline");

        mainlineLabel.setText("Mainline");

        javax.swing.GroupLayout OptionsPanelLayout = new javax.swing.GroupLayout(OptionsPanel);
        OptionsPanel.setLayout(OptionsPanelLayout);
        OptionsPanelLayout.setHorizontalGroup(
            OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tieGapLabel)
                    .addComponent(railWidthLabel)
                    .addComponent(tieLengthLabel)
                    .addComponent(tieColorLabel)
                    .addComponent(railGapLabel)
                    .addComponent(tieWidthLabel)
                    .addComponent(railCountLabel)
                    .addComponent(blockLineWidthLabel)
                    .addComponent(ballastColorLabel)
                    .addComponent(railColorLabel)
                    .addComponent(optionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ballastWidthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(mainTieWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainRailCountSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainBlockLineSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainRailWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainTieLengthSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainRailColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainTieGapSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainTieColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainRailGapSpinner, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(mainBallastColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mainBallastWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mainlineLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sideLineLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(sideRailWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideRailGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideTieLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideTieColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(sideTieWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideTieGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideRailCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sideBallastColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(sideRailColorButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sideBlockLineWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sideBallastWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {sideBallastColorButton, sideBallastWidthSpinner, sideBlockLineWidthSpinner, sideLineLabel, sideRailColorButton, sideRailCountSpinner, sideRailGapSpinner, sideRailWidthSpinner, sideTieColorButton, sideTieGapSpinner, sideTieLengthSpinner, sideTieWidthSpinner});

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {ballastColorLabel, ballastWidthLabel, blockLineWidthLabel, optionLabel, railColorLabel, railCountLabel, railGapLabel, railWidthLabel, tieColorLabel, tieGapLabel, tieLengthLabel, tieWidthLabel});

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mainBallastColorButton, mainBallastWidthSpinner, mainBlockLineSpinner, mainRailColorButton, mainRailCountSpinner, mainRailGapSpinner, mainRailWidthSpinner, mainTieColorButton, mainTieGapSpinner, mainTieLengthSpinner, mainTieWidthSpinner, mainlineLabel});

        OptionsPanelLayout.setVerticalGroup(
            OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(OptionsPanelLayout.createSequentialGroup()
                        .addComponent(mainlineLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainBallastWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainBallastColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainTieLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainTieWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mainTieGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(OptionsPanelLayout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addComponent(mainTieColorButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainRailCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mainRailWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainRailGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainRailColorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mainBlockLineSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(OptionsPanelLayout.createSequentialGroup()
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sideLineLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(optionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(OptionsPanelLayout.createSequentialGroup()
                                .addComponent(ballastWidthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ballastColorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tieLengthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tieWidthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tieGapLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tieColorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(railCountLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(railWidthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(railGapLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(railColorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(blockLineWidthLabel))
                            .addGroup(OptionsPanelLayout.createSequentialGroup()
                                .addComponent(sideBallastWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideBallastColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideTieLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideTieWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(sideTieGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(OptionsPanelLayout.createSequentialGroup()
                                        .addGap(32, 32, 32)
                                        .addComponent(sideTieColorButton)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideRailCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(sideRailWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideRailGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideRailColorButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sideBlockLineWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {sideBallastColorButton, sideBallastWidthSpinner, sideBlockLineWidthSpinner, sideLineLabel, sideRailColorButton, sideRailCountSpinner, sideRailGapSpinner, sideRailWidthSpinner, sideTieColorButton, sideTieGapSpinner, sideTieLengthSpinner, sideTieWidthSpinner});

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {ballastColorLabel, ballastWidthLabel, blockLineWidthLabel, mainBallastColorButton, mainBallastWidthSpinner, mainBlockLineSpinner, mainRailColorButton, mainRailCountSpinner, mainRailGapSpinner, mainRailWidthSpinner, mainTieColorButton, mainTieGapSpinner, mainTieLengthSpinner, mainTieWidthSpinner, mainlineLabel, railColorLabel, railCountLabel, railGapLabel, railWidthLabel, tieColorLabel, tieGapLabel, tieLengthLabel, tieWidthLabel});

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        javax.swing.GroupLayout previewLayeredPaneLayout = new javax.swing.GroupLayout(previewLayeredPane);
        previewLayeredPane.setLayout(previewLayeredPaneLayout);
        previewLayeredPaneLayout.setHorizontalGroup(
            previewLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        previewLayeredPaneLayout.setVerticalGroup(
            previewLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 116, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewLayeredPane)
                .addContainerGap())
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, previewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewLayeredPane)
                .addContainerGap())
        );

        presetsLabel.setText("Presets:");

        presetsComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Classic JMRI", "Drafting", "Realistic", "Garrish", "--", "Custom" }));
        presetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetsComboBoxActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setActionCommand("cancelActionCommand");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(presetsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(presetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cancelButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(okButton))))
                    .addComponent(OptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(OptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(presetsLabel)
                    .addComponent(presetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*==========================*\
    |* action performed methods *|
    \*==========================*/

    private void okButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        log.info("okButtonActionPerformed({}", evt);
        this.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void presetsComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_presetsComboBoxActionPerformed
        log.info("presetsComboBoxActionPerformed({}", evt);
        String presetName = (String) presetsComboBox.getSelectedItem();
        //TODO: load new preset;
        for (LayoutTrackDrawingOptions ltdo : ltdoList) {
            if (ltdo.getName().equals(presetName)) {
                ltdOptions = ltdo;
                OptionsPanel.setBorder(BorderFactory.createTitledBorder((String) presetName));
                break;
            }
        }
    }//GEN-LAST:event_presetsComboBoxActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        log.info("cancelButtonActionPerformed({}", evt);
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void mainBallastWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainBallastWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainBallastWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainBallastWidthSpinnerVetoableChange

    private void mainTieLengthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainTieLengthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieLengthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieLengthSpinnerVetoableChange

    private void mainTieWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainTieWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieWidthSpinnerVetoableChange

    private void mainTieGapSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainTieGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieGapSpinnerVetoableChange

    private void mainRailCountSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainRailCountSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailCountSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailCountSpinnerVetoableChange

    private void mainRailWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainRailWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailWidthSpinnerVetoableChange

    private void mainRailGapSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_mainRailGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailGapSpinnerVetoableChange

    private void sideBallastWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideBallastWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideBallastWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideBallastWidthSpinnerVetoableChange

    private void sideBallastColorButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_sideBallastColorButtonMouseClicked
        log.info("sideBallastColorButtonMouseClicked()");
    }//GEN-LAST:event_sideBallastColorButtonMouseClicked

    private void sideTieLengthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideTieLengthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieLengthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieLengthSpinnerVetoableChange

    private void sideTieWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideTieWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieWidthSpinnerVetoableChange

    private void sideTieGapSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideTieGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieGapSpinnerVetoableChange

    private void sideRailCountSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideRailCountSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailCountSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailCountSpinnerVetoableChange

    private void sideRailWidthSpinnerVetoableChange(PropertyChangeEvent evt) {//GEN-FIRST:event_sideRailWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailWidthSpinnerVetoableChange

    private void sideTieColorButtonMouseClicked(MouseEvent evt) {
        log.info("sideTieColorButtonMouseClicked()");
    }

    private void sideRailColorButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_sideRailColorButtonMouseClicked
        log.info("sideRailColorButtonMouseClicked()");
    }//GEN-LAST:event_sideRailColorButtonMouseClicked

    private void demoButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_demoButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_demoButtonActionPerformed

    private void mainBlockLineWidthSpinnerVetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {//GEN-FIRST:event_mainBlockLineWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainBlockLineWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainBlockLineWidthSpinnerVetoableChange

    private void sideRailGapSpinnerVetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {//GEN-FIRST:event_sideRailGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailGapSpinnerVetoableChange

    private void sideBlockLineWidthSpinnerVetoableChange(PropertyChangeEvent evt)throws PropertyVetoException {//GEN-FIRST:event_sideBlockLineWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideBlockLineWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideBlockLineWidthSpinnerVetoableChange

    private void mainRailColorButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_mainRailColorButtonMouseClicked
        log.info("mainRailColorButtonMouseClicked()");
    }//GEN-LAST:event_mainRailColorButtonMouseClicked

    private void mainTieColorButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_mainTieColorButtonMouseClicked
        log.info("mainTieColorButtonMouseClicked()");
    }//GEN-LAST:event_mainTieColorButtonMouseClicked

    private void mainBallastColorButtonMouseClicked(MouseEvent evt) {//GEN-FIRST:event_mainBallastColorButtonMouseClicked
        log.info("mainBallastColorButtonMouseClicked()");
    }//GEN-LAST:event_mainBallastColorButtonMouseClicked

    private void mainBlockLineSpinnerVetoableChange(PropertyChangeEvent evt)throws PropertyVetoException {//GEN-FIRST:event_mainBlockLineSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainBlockLineSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainBlockLineSpinnerVetoableChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionsPanel;
    private javax.swing.JLabel ballastWidthLabel;
    private javax.swing.JLabel blockLineWidthLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton mainBallastColorButton;
    private javax.swing.JSpinner mainBallastWidthSpinner;
    private javax.swing.JSpinner mainBlockLineSpinner;
    private javax.swing.JButton mainRailColorButton;
    private javax.swing.JSpinner mainRailCountSpinner;
    private javax.swing.JSpinner mainRailGapSpinner;
    private javax.swing.JSpinner mainRailWidthSpinner;
    private javax.swing.JButton mainTieColorButton;
    private javax.swing.JSpinner mainTieGapSpinner;
    private javax.swing.JSpinner mainTieLengthSpinner;
    private javax.swing.JSpinner mainTieWidthSpinner;
    private javax.swing.JLabel mainlineLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel optionLabel;
    private javax.swing.JComboBox<String> presetsComboBox;
    private javax.swing.JLabel presetsLabel;
    private javax.swing.JLayeredPane previewLayeredPane;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel railCountLabel;
    private javax.swing.JLabel railGapLabel;
    private javax.swing.JLabel railWidthLabel;
    private javax.swing.JButton sideBallastColorButton;
    private javax.swing.JSpinner sideBallastWidthSpinner;
    private javax.swing.JSpinner sideBlockLineWidthSpinner;
    private javax.swing.JLabel sideLineLabel;
    private javax.swing.JButton sideRailColorButton;
    private javax.swing.JSpinner sideRailCountSpinner;
    private javax.swing.JSpinner sideRailGapSpinner;
    private javax.swing.JSpinner sideRailWidthSpinner;
    private javax.swing.JButton sideTieColorButton;
    private javax.swing.JSpinner sideTieGapSpinner;
    private javax.swing.JSpinner sideTieLengthSpinner;
    private javax.swing.JSpinner sideTieWidthSpinner;
    private javax.swing.JLabel tieGapLabel;
    private javax.swing.JLabel tieLengthLabel;
    private javax.swing.JLabel tieWidthLabel;
    // End of variables declaration//GEN-END:variables

    /*====================*\
    |* initialize logging *|
    \*====================*/
    private transient final static Logger log
            = LoggerFactory.getLogger(LayoutTrackDrawingOptionsDialog.class);
}
