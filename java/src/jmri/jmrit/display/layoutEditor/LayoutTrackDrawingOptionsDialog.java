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

        // define the presets
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
        tempLTDO.setSideBallastColor(Color.decode("#B0B0B0"));
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
        tempLTDO.setMainBallastColor(Color.decode("#909090"));
        tempLTDO.setMainTieLength(11);
        tempLTDO.setMainTieWidth(2);
        tempLTDO.setMainTieGap(5);
        tempLTDO.setMainTieColor(Color.decode("#D5CFCC"));
        tempLTDO.setMainRailCount(2);
        tempLTDO.setMainRailWidth(2);
        tempLTDO.setMainRailGap(3);
        tempLTDO.setMainRailColor(Color.decode("#F0F0F0"));
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
        optionLabel = new javax.swing.JLabel();
        mainlineLabel = new javax.swing.JLabel();
        railCountLabel = new javax.swing.JLabel();
        mainRailCountSpinner = new javax.swing.JSpinner();
        sideRailCountSpinner = new javax.swing.JSpinner();
        railWidthLabel = new javax.swing.JLabel();
        mainRailWidthSpinner = new javax.swing.JSpinner();
        sideRailWidthSpinner = new javax.swing.JSpinner();
        railGapLabel = new javax.swing.JLabel();
        mainRailGapSpinner = new javax.swing.JSpinner();
        sideRailGapSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel railColorLabel = new javax.swing.JLabel();
        mainRailColorButton = new javax.swing.JButton();
        sideRailColorButton = new javax.swing.JButton();
        blockLineWidthLabel = new javax.swing.JLabel();
        mainBlockLineWidthSpinner = new javax.swing.JSpinner();
        sideBlockLineWidthSpinner = new javax.swing.JSpinner();
        ballastWidthLabel = new javax.swing.JLabel();
        mainBallastWidthSpinner = new javax.swing.JSpinner();
        sideBallastWidthSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel ballastColorLabel = new javax.swing.JLabel();
        mainBallastColorButton = new javax.swing.JButton();
        sideBallastColorButton = new javax.swing.JButton();
        tieLengthLabel = new javax.swing.JLabel();
        mainTieLengthSpinner = new javax.swing.JSpinner();
        sideTieLengthSpinner = new javax.swing.JSpinner();
        tieWidthLabel = new javax.swing.JLabel();
        mainTieWidthSpinner = new javax.swing.JSpinner();
        sideTieWidthSpinner = new javax.swing.JSpinner();
        tieGapLabel = new javax.swing.JLabel();
        mainTieGapSpinner = new javax.swing.JSpinner();
        sideTieGapSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel tieColorLabel = new javax.swing.JLabel();
        mainTieColorButton = new javax.swing.JButton();
        sideTieColorButton = new javax.swing.JButton();
        sideLineLabel = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        previewLayeredPane = new javax.swing.JLayeredPane();
        presetsComboBox = new javax.swing.JComboBox<>();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        presetsLabel = new javax.swing.JLabel();
        applyButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(512, 2147483647));
        setMinimumSize(new java.awt.Dimension(512, 512));

        OptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(ltdOptions.getName()));
        OptionsPanel.setMaximumSize(new java.awt.Dimension(800, 529));
        OptionsPanel.setMinimumSize(new java.awt.Dimension(800, 529));
        OptionsPanel.setPreferredSize(new java.awt.Dimension(800, 529));
        OptionsPanel.setSize(new java.awt.Dimension(800, 529));

        optionLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        optionLabel.setText("Option");
        optionLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        optionLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        optionLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        optionLabel.setSize(new java.awt.Dimension(256, 16));

        mainlineLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        mainlineLabel.setText("Mainline");
        mainlineLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        mainlineLabel.setMinimumSize(new java.awt.Dimension(64, 16));
        mainlineLabel.setPreferredSize(new java.awt.Dimension(64, 16));
        mainlineLabel.setSize(new java.awt.Dimension(64, 16));

        railCountLabel.setText("Rail Count:");
        railCountLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        railCountLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        railCountLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        railCountLabel.setSize(new java.awt.Dimension(256, 16));

        mainRailCountSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        mainRailCountSpinner.setToolTipText("Select the number of mainline rails (1...3)");
        mainRailCountSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainRailCountSpinner.setName(""); // NOI18N
        mainRailCountSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainRailCountSpinner.setSize(new java.awt.Dimension(64, 16));
        mainRailCountSpinner.setValue(ltdOptions.getMainRailCount());
        mainRailCountSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailCountSpinnerVetoableChange(evt);
            }
        });

        sideRailCountSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, 3, 1));
        sideRailCountSpinner.setToolTipText("Select the number of sideline rails (1...3)");
        sideRailCountSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideRailCountSpinner.setName(""); // NOI18N
        sideRailCountSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideRailCountSpinner.setSize(new java.awt.Dimension(64, 16));
        sideRailCountSpinner.setValue(ltdOptions.getSideRailCount());
        sideRailCountSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailCountSpinnerVetoableChange(evt);
            }
        });

        railWidthLabel.setText("Rail Width:");
        railWidthLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        railWidthLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        railWidthLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        railWidthLabel.setSize(new java.awt.Dimension(256, 16));

        mainRailWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainRailWidthSpinner.setToolTipText("Select the number of mainline rails (1...3)");
        mainRailWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainRailWidthSpinner.setName(""); // NOI18N
        mainRailWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainRailWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        mainRailWidthSpinner.setValue(ltdOptions.getMainRailWidth());
        mainRailWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailWidthSpinnerVetoableChange(evt);
            }
        });

        sideRailWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideRailWidthSpinner.setToolTipText("Select the width of the sideline rails");
        sideRailWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideRailWidthSpinner.setName(""); // NOI18N
        sideRailWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideRailWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        sideRailWidthSpinner.setValue(ltdOptions.getSideRailWidth());
        sideRailWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailWidthSpinnerVetoableChange(evt);
            }
        });

        railGapLabel.setText("Rail Gap:");
        railGapLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        railGapLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        railGapLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        railGapLabel.setSize(new java.awt.Dimension(256, 16));

        mainRailGapSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainRailGapSpinner.setToolTipText("Select the gap between the mainline rails");
        mainRailGapSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainRailGapSpinner.setName(""); // NOI18N
        mainRailGapSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainRailGapSpinner.setSize(new java.awt.Dimension(64, 16));
        mainRailGapSpinner.setValue(ltdOptions.getMainRailGap());
        mainRailGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainRailGapSpinnerVetoableChange(evt);
            }
        });

        sideRailGapSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideRailGapSpinner.setToolTipText("Select the gap between the sideline rails");
        sideRailGapSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideRailGapSpinner.setName(""); // NOI18N
        sideRailGapSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideRailGapSpinner.setSize(new java.awt.Dimension(64, 16));
        sideRailGapSpinner.setValue(ltdOptions.getSideRailGap());
        sideRailGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideRailGapSpinnerVetoableChange(evt);
            }
        });

        railColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        railColorLabel.setText("Rail Color:");
        railColorLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        railColorLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        railColorLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        railColorLabel.setSize(new java.awt.Dimension(256, 16));

        mainRailColorButton.setToolTipText("Set the color of the mainline ties");
        mainRailColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainRailColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        mainRailColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        mainRailColorButton.setOpaque(true);
        mainRailColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        mainRailColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        sideRailColorButton.setToolTipText("Set the color of the sideline ties");
        sideRailColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideRailColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        sideRailColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        sideRailColorButton.setOpaque(true);
        sideRailColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        sideRailColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        blockLineWidthLabel.setText("Block Line Width:");
        blockLineWidthLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        blockLineWidthLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        blockLineWidthLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        blockLineWidthLabel.setSize(new java.awt.Dimension(256, 16));

        mainBlockLineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        mainBlockLineWidthSpinner.setToolTipText("Select the width for the block highlight line");
        mainBlockLineWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainBlockLineWidthSpinner.setName(""); // NOI18N
        mainBlockLineWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainBlockLineWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        mainBlockLineWidthSpinner.setValue(ltdOptions.getMainBlockLineWidth());
        mainBlockLineWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainBlockLineWidthSpinnerVetoableChange(evt);
            }
        });

        sideBlockLineWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        sideBlockLineWidthSpinner.setToolTipText("Select the gap between the sideline rails");
        sideBlockLineWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideBlockLineWidthSpinner.setName(""); // NOI18N
        sideBlockLineWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideBlockLineWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        sideBlockLineWidthSpinner.setValue(ltdOptions.getSideBlockLineWidth());
        sideBlockLineWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideBlockLineWidthSpinnerVetoableChange(evt);
            }
        });

        ballastWidthLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        ballastWidthLabel.setText("Ballast Width:");
        ballastWidthLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        ballastWidthLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        ballastWidthLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        ballastWidthLabel.setSize(new java.awt.Dimension(256, 16));

        mainBallastWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainBallastWidthSpinner.setToolTipText("Set the width of the mainline ballast");
        mainBallastWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainBallastWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainBallastWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        mainBallastWidthSpinner.setValue(ltdOptions.getMainBallastWidth());
        mainBallastWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainBallastWidthSpinnerVetoableChange(evt);
            }
        });

        sideBallastWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideBallastWidthSpinner.setToolTipText("Set the width of the sideline ballast");
        sideBallastWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideBallastWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideBallastWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        sideBallastWidthSpinner.setValue(ltdOptions.getSideBallastWidth());
        sideBallastWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideBallastWidthSpinnerVetoableChange(evt);
            }
        });

        ballastColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        ballastColorLabel.setText("Ballast Color:");
        ballastColorLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        ballastColorLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        ballastColorLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        ballastColorLabel.setSize(new java.awt.Dimension(256, 16));

        mainBallastColorButton.setToolTipText("Set the color of the mainline ballast");
        mainBallastColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainBallastColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        mainBallastColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        mainBallastColorButton.setOpaque(true);
        mainBallastColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        mainBallastColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        sideBallastColorButton.setToolTipText("Set the color of the sideline ballast");
        sideBallastColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideBallastColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        sideBallastColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        sideBallastColorButton.setOpaque(true);
        sideBallastColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        sideBallastColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        tieLengthLabel.setText("Tie Length:");
        tieLengthLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        tieLengthLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        tieLengthLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        tieLengthLabel.setSize(new java.awt.Dimension(256, 16));

        mainTieLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieLengthSpinner.setToolTipText("Set the length of the mainline ties");
        mainTieLengthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainTieLengthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainTieLengthSpinner.setSize(new java.awt.Dimension(64, 16));
        mainTieLengthSpinner.setValue(ltdOptions.getMainTieLength());
        mainTieLengthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieLengthSpinnerVetoableChange(evt);
            }
        });

        sideTieLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieLengthSpinner.setToolTipText("Set the length of the sideline ties");
        sideTieLengthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideTieLengthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideTieLengthSpinner.setSize(new java.awt.Dimension(64, 16));
        sideTieLengthSpinner.setValue(ltdOptions.getSideTieLength());
        sideTieLengthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieLengthSpinnerVetoableChange(evt);
            }
        });

        tieWidthLabel.setText("Tie Width:");
        tieWidthLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        tieWidthLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        tieWidthLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        tieWidthLabel.setSize(new java.awt.Dimension(256, 16));

        mainTieWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieWidthSpinner.setToolTipText("Set the width of the mainline ties");
        mainTieWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainTieWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainTieWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        mainTieWidthSpinner.setValue(ltdOptions.getMainTieWidth());
        mainTieWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieWidthSpinnerVetoableChange(evt);
            }
        });

        sideTieWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieWidthSpinner.setToolTipText("Set the width of the sideline ties");
        sideTieWidthSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideTieWidthSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideTieWidthSpinner.setSize(new java.awt.Dimension(64, 16));
        sideTieWidthSpinner.setValue(ltdOptions.getSideTieWidth());
        sideTieWidthSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieWidthSpinnerVetoableChange(evt);
            }
        });

        tieGapLabel.setText("Tie Gap:");
        tieGapLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        tieGapLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        tieGapLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        tieGapLabel.setSize(new java.awt.Dimension(256, 16));

        mainTieGapSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        mainTieGapSpinner.setToolTipText("Set the gap between the mainline ties");
        mainTieGapSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        mainTieGapSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        mainTieGapSpinner.setSize(new java.awt.Dimension(64, 16));
        mainTieGapSpinner.setValue(ltdOptions.getMainTieGap());
        mainTieGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                mainTieGapSpinnerVetoableChange(evt);
            }
        });

        sideTieGapSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        sideTieGapSpinner.setToolTipText("Set the gap between the sideline ties");
        sideTieGapSpinner.setMinimumSize(new java.awt.Dimension(64, 16));
        sideTieGapSpinner.setPreferredSize(new java.awt.Dimension(64, 16));
        sideTieGapSpinner.setSize(new java.awt.Dimension(64, 16));
        sideTieGapSpinner.setValue(ltdOptions.getSideTieGap());
        sideTieGapSpinner.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                sideTieGapSpinnerVetoableChange(evt);
            }
        });

        tieColorLabel.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        tieColorLabel.setText("Tie Color:");
        tieColorLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        tieColorLabel.setMinimumSize(new java.awt.Dimension(256, 16));
        tieColorLabel.setPreferredSize(new java.awt.Dimension(256, 16));
        tieColorLabel.setSize(new java.awt.Dimension(256, 16));

        mainTieColorButton.setToolTipText("Set the color of the mainline ties");
        mainTieColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        mainTieColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        mainTieColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        mainTieColorButton.setOpaque(true);
        mainTieColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        mainTieColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        sideTieColorButton.setToolTipText("Set the color of the sideline ties");
        sideTieColorButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        sideTieColorButton.setMaximumSize(new java.awt.Dimension(32767, 32767));
        sideTieColorButton.setMinimumSize(new java.awt.Dimension(64, 16));
        sideTieColorButton.setOpaque(true);
        sideTieColorButton.setPreferredSize(new java.awt.Dimension(64, 16));
        sideTieColorButton.setSize(new java.awt.Dimension(64, 16));
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
            .addGap(0, 12, Short.MAX_VALUE)
        );

        sideLineLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        sideLineLabel.setText("Sideline");
        sideLineLabel.setMaximumSize(new java.awt.Dimension(32767, 32767));
        sideLineLabel.setMinimumSize(new java.awt.Dimension(64, 16));
        sideLineLabel.setPreferredSize(new java.awt.Dimension(64, 16));
        sideLineLabel.setSize(new java.awt.Dimension(64, 16));

        javax.swing.GroupLayout OptionsPanelLayout = new javax.swing.GroupLayout(OptionsPanel);
        OptionsPanel.setLayout(OptionsPanelLayout);
        OptionsPanelLayout.setHorizontalGroup(
            OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tieColorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tieGapLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tieWidthLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tieLengthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ballastColorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ballastWidthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(blockLineWidthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(railColorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(railGapLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(optionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(railWidthLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(railCountLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mainlineLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainRailCountSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainRailWidthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sideRailGapSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainRailColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainBlockLineWidthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainBallastWidthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainBallastColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainTieLengthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainTieWidthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainTieGapSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mainTieColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, OptionsPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(sideTieColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(OptionsPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sideRailCountSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideRailWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(mainRailGapSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideRailColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideBlockLineWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideBallastWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideBallastColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideTieLengthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideTieWidthSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideTieGapSpinner, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sideLineLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mainBallastColorButton, mainBallastWidthSpinner, mainBlockLineWidthSpinner, mainRailColorButton, mainRailCountSpinner, mainRailGapSpinner, mainRailWidthSpinner, mainTieColorButton, mainTieGapSpinner, mainTieLengthSpinner, mainTieWidthSpinner, mainlineLabel, sideBallastColorButton, sideBallastWidthSpinner, sideBlockLineWidthSpinner, sideLineLabel, sideRailColorButton, sideRailCountSpinner, sideRailGapSpinner, sideRailWidthSpinner, sideTieColorButton, sideTieGapSpinner, sideTieLengthSpinner, sideTieWidthSpinner});

        OptionsPanelLayout.setVerticalGroup(
            OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(OptionsPanelLayout.createSequentialGroup()
                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(OptionsPanelLayout.createSequentialGroup()
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(OptionsPanelLayout.createSequentialGroup()
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(sideLineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainlineLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(optionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(railCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainRailCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideRailCountSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(railWidthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainRailWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideRailWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(railGapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideRailGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainRailGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(railColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainRailColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideRailColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(blockLineWidthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainBlockLineWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideBlockLineWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(ballastWidthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(mainBallastWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideBallastWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ballastColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sideBallastColorButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(mainBallastColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tieLengthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mainTieLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sideTieLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tieWidthLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mainTieWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sideTieWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tieGapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mainTieGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sideTieGapSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(OptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tieColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sideTieColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(mainTieColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        OptionsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {ballastColorLabel, ballastWidthLabel, blockLineWidthLabel, mainBallastColorButton, mainBallastWidthSpinner, mainBlockLineWidthSpinner, mainRailColorButton, mainRailCountSpinner, mainRailGapSpinner, mainRailWidthSpinner, mainTieColorButton, mainTieGapSpinner, mainTieLengthSpinner, mainTieWidthSpinner, mainlineLabel, optionLabel, railColorLabel, railCountLabel, railGapLabel, railWidthLabel, sideBallastColorButton, sideBallastWidthSpinner, sideBlockLineWidthSpinner, sideLineLabel, sideRailColorButton, sideRailCountSpinner, sideRailGapSpinner, sideRailWidthSpinner, sideTieColorButton, sideTieGapSpinner, sideTieLengthSpinner, sideTieWidthSpinner, tieColorLabel, tieGapLabel, tieLengthLabel, tieWidthLabel});

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));

        javax.swing.GroupLayout previewLayeredPaneLayout = new javax.swing.GroupLayout(previewLayeredPane);
        previewLayeredPane.setLayout(previewLayeredPaneLayout);
        previewLayeredPaneLayout.setHorizontalGroup(
            previewLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
        );
        previewLayeredPaneLayout.setVerticalGroup(
            previewLayeredPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
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
            .addComponent(previewLayeredPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );

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

        presetsLabel.setText("Presets:");

        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(presetsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(presetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(applyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(OptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 506, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(OptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(presetsLabel)
                    .addComponent(presetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(applyButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setupControls() {
        mainBallastColorButton.setBackground(ltdOptions.getMainBallastColor());
        mainBallastWidthSpinner.setValue(ltdOptions.getMainBallastWidth());
        mainBlockLineWidthSpinner.setValue(ltdOptions.getMainBlockLineWidth());
        mainRailColorButton.setBackground(ltdOptions.getMainRailColor());
        mainRailCountSpinner.setValue(ltdOptions.getMainRailCount());
        mainRailGapSpinner.setValue(ltdOptions.getMainRailGap());
        mainRailWidthSpinner.setValue(ltdOptions.getMainRailWidth());
        mainTieColorButton.setBackground(ltdOptions.getMainTieColor());
        mainTieGapSpinner.setValue(ltdOptions.getMainTieGap());
        mainTieLengthSpinner.setValue(ltdOptions.getMainTieLength());
        mainTieWidthSpinner.setValue(ltdOptions.getMainTieWidth());
        sideBallastColorButton.setBackground(ltdOptions.getSideBallastColor());
        sideBallastWidthSpinner.setValue(ltdOptions.getSideBallastWidth());
        sideBlockLineWidthSpinner.setValue(ltdOptions.getSideBlockLineWidth());
        sideRailColorButton.setBackground(ltdOptions.getSideRailColor());
        sideRailCountSpinner.setValue(ltdOptions.getSideRailCount());
        sideRailGapSpinner.setValue(ltdOptions.getSideRailGap());
        sideRailWidthSpinner.setValue(ltdOptions.getSideRailWidth());
        sideTieColorButton.setBackground(ltdOptions.getSideTieColor());
        sideTieGapSpinner.setValue(ltdOptions.getSideTieGap());
        sideTieLengthSpinner.setValue(ltdOptions.getSideTieLength());
        sideTieWidthSpinner.setValue(ltdOptions.getSideTieWidth());
    }   // setupControls

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
                if (!ltdOptions.getName().equals(ltdo.getName())) {
                    ltdOptions = ltdo;
                    OptionsPanel.setBorder(BorderFactory.createTitledBorder((String) presetName));
                    setupControls();
                }
                break;
            }
        }
    }//GEN-LAST:event_presetsComboBoxActionPerformed

    private void cancelButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        log.info("cancelButtonActionPerformed({}", evt);
        this.setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void sideTieColorButtonMouseClicked(MouseEvent evt) {
        log.info("sideTieColorButtonMouseClicked()");
    }

    private void demoButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_demoButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_demoButtonActionPerformed

    private void mainRailColorButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainRailColorButtonMouseClicked
        log.info("mainRailColorButtonMouseClicked()");
    }//GEN-LAST:event_mainRailColorButtonMouseClicked

    private void mainTieColorButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainTieColorButtonMouseClicked
        log.info("mainTieColorButtonMouseClicked()");
    }//GEN-LAST:event_mainTieColorButtonMouseClicked

    private void sideTieGapSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideTieGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieGapSpinnerVetoableChange

    private void mainTieGapSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainTieGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieGapSpinnerVetoableChange

    private void sideTieWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideTieWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieWidthSpinnerVetoableChange

    private void mainTieWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainTieWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieWidthSpinnerVetoableChange

    private void sideTieLengthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideTieLengthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideTieLengthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideTieLengthSpinnerVetoableChange

    private void mainTieLengthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainTieLengthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainTieLengthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainTieLengthSpinnerVetoableChange

    private void sideBallastColorButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sideBallastColorButtonMouseClicked
        log.info("sideBallastColorButtonMouseClicked()");
    }//GEN-LAST:event_sideBallastColorButtonMouseClicked

    private void mainBallastColorButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mainBallastColorButtonMouseClicked
        log.info("mainBallastColorButtonMouseClicked()");
    }//GEN-LAST:event_mainBallastColorButtonMouseClicked

    private void sideBallastWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideBallastWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideBallastWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideBallastWidthSpinnerVetoableChange

    private void mainBallastWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainBallastWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainBallastWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainBallastWidthSpinnerVetoableChange

    private void sideBlockLineWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideBlockLineWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideBlockLineWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideBlockLineWidthSpinnerVetoableChange

    private void mainBlockLineWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainBlockLineWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainBlockLineWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainBlockLineWidthSpinnerVetoableChange

    private void sideRailColorButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sideRailColorButtonMouseClicked
        log.info("sideRailColorButtonMouseClicked()");
    }//GEN-LAST:event_sideRailColorButtonMouseClicked

    private void sideRailGapSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideRailGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailGapSpinnerVetoableChange

    private void mainRailGapSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainRailGapSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailGapSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailGapSpinnerVetoableChange

    private void sideRailWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideRailWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailWidthSpinnerVetoableChange

    private void mainRailWidthSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainRailWidthSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailWidthSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailWidthSpinnerVetoableChange

    private void sideRailCountSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_sideRailCountSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("sideRailCountSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_sideRailCountSpinnerVetoableChange

    private void mainRailCountSpinnerVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_mainRailCountSpinnerVetoableChange
        JSpinner spinner = (JSpinner) evt.getSource();
        Object value = spinner.getValue();
        log.info("mainRailCountSpinnerVetoableChange({})", value);
    }//GEN-LAST:event_mainRailCountSpinnerVetoableChange

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        layoutEditor.
    }//GEN-LAST:event_applyButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel OptionsPanel;
    private javax.swing.JButton applyButton;
    private javax.swing.JLabel ballastWidthLabel;
    private javax.swing.JLabel blockLineWidthLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton mainBallastColorButton;
    private javax.swing.JSpinner mainBallastWidthSpinner;
    private javax.swing.JSpinner mainBlockLineWidthSpinner;
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
