/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import javax.annotation.Nonnull;
import jmri.util.swing.JmriColorChooser;

/******************************************************************************* 
 * LayoutTrackDrawingOptions.
 * since 4.15.6 blockDefaultColor, blockOccupiedColor and blockAlternativeColor added here 
 * 
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTrackDrawingOptions {

    private String name;

    public LayoutTrackDrawingOptions(String name) {
        this.name = name;
    }

    public LayoutTrackDrawingOptions(LayoutTrackDrawingOptions ltdo) {
        name = ltdo.getName();
        mainBallastColor = ltdo.getMainBallastColor();
        mainBallastWidth = ltdo.getMainBallastWidth();
        mainBlockLineDashPercentageX10 = ltdo.getMainBlockLineDashPercentageX10();
        mainBlockLineWidth = ltdo.getMainBlockLineWidth();
        mainRailColor = ltdo.getMainRailColor();
        mainRailCount = ltdo.getMainRailCount();
        mainRailGap = ltdo.getMainRailGap();
        mainRailWidth = ltdo.getMainRailWidth();
        mainTieColor = ltdo.getMainTieColor();
        mainTieGap = ltdo.getMainTieGap();
        mainTieLength = ltdo.getMainTieLength();
        mainTieWidth = ltdo.getMainTieWidth();
        sideBallastColor = ltdo.getSideBallastColor();
        sideBallastWidth = ltdo.getSideBallastWidth();
        sideBlockLineDashPercentageX10 = ltdo.getSideBlockLineDashPercentageX10();
        sideBlockLineWidth = ltdo.getSideBlockLineWidth();
        sideRailColor = ltdo.getSideRailColor();
        sideRailCount = ltdo.getSideRailCount();
        sideRailGap = ltdo.getSideRailGap();
        sideRailWidth = ltdo.getSideRailWidth();
        sideTieColor = ltdo.getSideTieColor();
        sideTieGap = ltdo.getSideTieGap();
        sideTieLength = ltdo.getSideTieLength();
        sideTieWidth = ltdo.getSideTieWidth();
        blockDefaultColor = ltdo.getBlockDefaultColor();
        blockOccupiedColor = ltdo.getBlockOccupiedColor();
        blockAlternativeColor = ltdo.getBlockAlternativeColor();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int sideBallastWidth = 0;   // defaults to zero (off)

    public int getSideBallastWidth() {
        return sideBallastWidth;
    }

    public void setSideBallastWidth(int val) {
        sideBallastWidth = val;
    }

    private Color sideBallastColor = Color.BLACK;

    public Color getSideBallastColor() {
        return sideBallastColor;
    }

    public void setSideBallastColor(@Nonnull Color val) {
        sideBallastColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int sideTieLength = 0;

    public int getSideTieLength() {
        return sideTieLength;
    }

    public void setSideTieLength(int val) {
        sideTieLength = val;
    }

    private Color sideTieColor = Color.BLACK;

    public Color getSideTieColor() {
        return sideTieColor;
    }

    public void setSideTieColor(@Nonnull Color val) {
        sideTieColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int sideTieWidth = 0;

    public int getSideTieWidth() {
        return sideTieWidth;
    }

    public void setSideTieWidth(int val) {
        sideTieWidth = val;
    }

    private int sideTieGap = 0;

    public int getSideTieGap() {
        return sideTieGap;
    }

    public void setSideTieGap(int val) {
        sideTieGap = val;
    }

    private int sideRailCount = 1;

    public int getSideRailCount() {
        return sideRailCount;
    }

    public void setSideRailCount(int val) {
        sideRailCount = val;
    }

    private int sideRailWidth = 1;

    public int getSideRailWidth() {
        return sideRailWidth;
    }

    public void setSideRailWidth(int val) {
        sideRailWidth = val;
    }

    private int sideRailGap = 0;

    public int getSideRailGap() {
        return sideRailGap;
    }

    public void setSideRailGap(int val) {
        sideRailGap = val;
    }

    private Color sideRailColor = Color.GRAY;

    public Color getSideRailColor() {
        return sideRailColor;
    }

    public void setSideRailColor(@Nonnull Color val) {
        sideRailColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int sideBlockLineDashPercentageX10 = 0;

    public int getSideBlockLineDashPercentageX10() {
        return sideBlockLineDashPercentageX10;
    }

    public void setSideBlockLineDashPercentageX10(int val) {
        sideBlockLineDashPercentageX10 = val;
    }

    private int sideBlockLineWidth = 2;

    public int getSideBlockLineWidth() {
        return sideBlockLineWidth;
    }

    public void setSideBlockLineWidth(int val) {
        sideBlockLineWidth = val;
    }

    private int mainBallastWidth = 0;   // defaults to zero (off)

    public int getMainBallastWidth() {
        return mainBallastWidth;
    }

    public void setMainBallastWidth(int val) {
        mainBallastWidth = val;
    }

    private Color mainBallastColor = Color.BLACK;

    public Color getMainBallastColor() {
        return mainBallastColor;
    }

    public void setMainBallastColor(@Nonnull Color val) {
        mainBallastColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int mainTieLength = 0;

    public int getMainTieLength() {
        return mainTieLength;
    }

    public void setMainTieLength(int val) {
        mainTieLength = val;
    }

    private Color mainTieColor = Color.BLACK;

    public Color getMainTieColor() {
        return mainTieColor;
    }

    public void setMainTieColor(@Nonnull Color val) {
        mainTieColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int mainTieWidth = 0;

    public int getMainTieWidth() {
        return mainTieWidth;
    }

    public void setMainTieWidth(int val) {
        mainTieWidth = val;
    }

    private int mainTieGap = 0;

    public int getMainTieGap() {
        return mainTieGap;
    }

    public void setMainTieGap(int val) {
        mainTieGap = val;
    }

    private int mainRailCount = 1;

    public int getMainRailCount() {
        return mainRailCount;
    }

    public void setMainRailCount(int val) {
        mainRailCount = val;
    }

    private int mainRailWidth = 2;

    public int getMainRailWidth() {
        return mainRailWidth;
    }

    public void setMainRailWidth(int val) {
        mainRailWidth = val;
    }

    private int mainRailGap = 0;

    public int getMainRailGap() {
        return mainRailGap;
    }

    public void setMainRailGap(int val) {
        mainRailGap = val;
    }

    private Color mainRailColor = Color.GRAY;

    public Color getMainRailColor() {
        return mainRailColor;
    }

    public void setMainRailColor(@Nonnull Color val) {
        mainRailColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private int mainBlockLineDashPercentageX10 = 0;

    public int getMainBlockLineDashPercentageX10() {
        return mainBlockLineDashPercentageX10;
    }

    public void setMainBlockLineDashPercentageX10(int val) {
        mainBlockLineDashPercentageX10 = val;
    }

    private int mainBlockLineWidth = 4;

    public int getMainBlockLineWidth() {
        return mainBlockLineWidth;
    }

    public void setMainBlockLineWidth(int val) {
        mainBlockLineWidth = val;
    }

    private Color blockDefaultColor = Color.GRAY;

    public Color getBlockDefaultColor() {
        return blockDefaultColor;
    }

    public void setBlockDefaultColor(@Nonnull Color val) {
        blockDefaultColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private Color blockOccupiedColor = Color.red;

    public Color getBlockOccupiedColor() {
        return blockOccupiedColor;
    }

    public void setBlockOccupiedColor(@Nonnull Color val) {
        blockOccupiedColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    private Color blockAlternativeColor = Color.GRAY;

    public Color getBlockAlternativeColor() {
        return blockAlternativeColor;
    }

    public void setBlockAlternativeColor(@Nonnull Color val) {
        blockAlternativeColor = val;
        JmriColorChooser.addRecentColor(val);
    }

    //
    public boolean equalsAllButName(@Nonnull LayoutTrackDrawingOptions ltdo) {
        boolean result = true;  // assume success (optimist!)
        if (this != ltdo) {
            result = false; // assume failure (pessimist!)
            if (ltdo != null) {
                String tempName = name;
                name = ltdo.getName();
                result = this.equals(ltdo);
                name = tempName;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = true;  // assume success (optimist!)
        if (obj != this) {
            result = false; // assume failure (pessimist!)
            if ((obj != null) && (getClass() == obj.getClass())) {
                LayoutTrackDrawingOptions ltdo = (LayoutTrackDrawingOptions) obj;

                do {
                    if (!name.equals(ltdo.getName())) {
                        break;
                    }
                    if (sideBallastWidth != ltdo.getSideBallastWidth()) {
                        break;
                    }
                    if (!sideBallastColor.equals(ltdo.getSideBallastColor())) {
                        break;
                    }
                    if (sideTieLength != ltdo.getSideTieLength()) {
                        break;
                    }
                    if (!sideTieColor.equals(ltdo.getSideTieColor())) {
                        break;
                    }

                    if (sideTieGap != ltdo.getSideTieGap()) {
                        break;
                    }
                    if (sideRailCount != ltdo.getSideRailCount()) {
                        break;
                    }
                    if (sideRailWidth != ltdo.getSideRailWidth()) {
                        break;
                    }
                    if (sideRailGap != ltdo.getSideRailGap()) {
                        break;
                    }
                    if (!sideRailColor.equals(ltdo.getSideRailColor())) {
                        break;
                    }

                    if (sideBlockLineDashPercentageX10 != ltdo.getSideBlockLineDashPercentageX10()) {
                        break;
                    }

                    if (sideBlockLineWidth != ltdo.getSideBlockLineWidth()) {
                        break;
                    }

                    if (mainBallastWidth != ltdo.getMainBallastWidth()) {
                        break;
                    }
                    if (!mainBallastColor.equals(ltdo.getMainBallastColor())) {
                        break;
                    }

                    if (mainTieLength != ltdo.getMainTieLength()) {
                        break;
                    }
                    if (!mainTieColor.equals(ltdo.getMainTieColor())) {
                        break;
                    }

                    if (mainTieWidth != ltdo.getMainTieWidth()) {
                        break;
                    }
                    if (mainTieGap != ltdo.getMainTieGap()) {
                        break;
                    }
                    if (mainRailCount != ltdo.getMainRailCount()) {
                        break;
                    }
                    if (mainRailWidth != ltdo.getMainRailWidth()) {
                        break;
                    }
                    if (mainRailGap != ltdo.getMainRailGap()) {
                        break;
                    }
                    if (!mainRailColor.equals(ltdo.getMainRailColor())) {
                        break;
                    }
                    if (mainBlockLineDashPercentageX10 != ltdo.getMainBlockLineDashPercentageX10()) {
                        break;
                    }
                    if (mainBlockLineWidth != ltdo.getMainBlockLineWidth()) {
                        break;
                    }
                    if (!blockDefaultColor.equals(ltdo.getBlockDefaultColor())) {
                        break;
                    }
                    if (!blockOccupiedColor.equals(ltdo.getBlockOccupiedColor())) {
                        break;
                    }
                    if (!blockAlternativeColor.equals(ltdo.getBlockAlternativeColor())) {
                        break;
                    }
                    result = true;
                } while (false);
            }
        }
        return result;
    }   // equals

    /**
     * Hash on the header
     */
    @Override
    public int hashCode() {
        int result = 7;
        result = (37 * result) + (name != null ? name.hashCode() : 0);

        // sideline values
        result = (37 * result) + sideBallastWidth;
        result = (37 * result) + (sideBallastColor == null ? 0 : sideBallastColor.hashCode());
        result = (37 * result) + sideTieLength;
        result = (37 * result) + (sideTieColor == null ? 0 : sideTieColor.hashCode());
        result = (37 * result) + sideTieGap;
        result = (37 * result) + sideRailCount;
        result = (37 * result) + sideRailWidth;
        result = (37 * result) + sideRailGap;
        result = (37 * result) + (sideRailColor == null ? 0 : sideRailColor.hashCode());
        result = (37 * result) + sideBlockLineDashPercentageX10;
        result = (37 * result) + sideBlockLineWidth;

        // mainline values
        result = (37 * result) + mainBallastWidth;
        result = (37 * result) + (mainBallastColor == null ? 0 : mainBallastColor.hashCode());
        result = (37 * result) + mainTieLength;
        result = (37 * result) + (mainTieColor == null ? 0 : mainTieColor.hashCode());
        result = (37 * result) + mainTieWidth;
        result = (37 * result) + mainTieGap;
        result = (37 * result) + mainRailCount;
        result = (37 * result) + mainRailWidth;
        result = (37 * result) + mainRailGap;
        result = (37 * result) + (mainRailColor == null ? 0 : mainRailColor.hashCode());
        result = (37 * result) + mainBlockLineDashPercentageX10;
        result = (37 * result) + mainBlockLineWidth;

        // block
        result = (37 * result) + (blockDefaultColor == null ? 0 : blockDefaultColor.hashCode());
        result = (37 * result) + (blockOccupiedColor == null ? 0 : blockOccupiedColor.hashCode());
        result = (37 * result) + (blockAlternativeColor == null ? 0 : blockAlternativeColor.hashCode());

        return result;
    }
}
