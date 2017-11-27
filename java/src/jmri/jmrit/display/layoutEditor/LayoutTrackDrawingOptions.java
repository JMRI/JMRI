/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import javax.annotation.Nonnull;

/**
 *
 * @author geowar
 */
public class LayoutTrackDrawingOptions implements Cloneable {

    private String name;

    public LayoutTrackDrawingOptions(String name) {
        this.name = name;
    }

    public LayoutTrackDrawingOptions(LayoutTrackDrawingOptions ltdo) {
        name = ltdo.getName();
        sideBallastWidth = ltdo.getSideBallastWidth();
        sideBallastColor = ltdo.getSideBallastColor();
        sideTieLength = ltdo.getSideTieLength();
        sideTieWidth = ltdo.getSideTieWidth();
        sideTieColor = ltdo.getSideTieColor();
        sideTieGap = ltdo.getSideTieGap();
        sideRailCount = ltdo.getSideRailCount();
        sideRailWidth = ltdo.getSideRailWidth();
        sideRailGap = ltdo.getSideRailGap();
        sideRailColor = ltdo.getSideRailColor();
        sideBlockLineWidth = ltdo.getSideBlockLineWidth();
        mainBallastWidth = ltdo.getMainBallastWidth();
        mainBallastColor = ltdo.getMainBallastColor();
        mainTieLength = ltdo.getMainTieLength();
        mainTieWidth = ltdo.getMainTieWidth();
        mainTieGap = ltdo.getMainTieGap();
        mainTieColor = ltdo.getMainTieColor();
        mainRailCount = ltdo.getMainRailCount();
        mainRailWidth = ltdo.getMainRailWidth();
        mainRailGap = ltdo.getMainRailGap();
        mainRailColor = ltdo.getMainRailColor();
        mainBlockLineWidth = ltdo.getMainBlockLineWidth();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int sideBallastWidth = 13;   // defaults to zero (off)

    public int getSideBallastWidth() {
        return sideBallastWidth;
    }

    public void setSideBallastWidth(int val) {
        sideBallastWidth = val;
    }

    private Color sideBallastColor = Color.decode("#AEACAD");

    public Color getSideBallastColor() {
        return sideBallastColor;
    }

    public void setSideBallastColor(@Nonnull Color val) {
        sideBallastColor = val;
    }

    private int sideTieLength = 9;

    public int getSideTieLength() {
        return sideTieLength;
    }

    public void setSideTieLength(int val) {
        sideTieLength = val;
    }

    private Color sideTieColor = Color.decode("#391E16");

    public Color getSideTieColor() {
        return sideTieColor;
    }

    public void setSideTieColor(@Nonnull Color val) {
        sideTieColor = val;
    }

    private int sideTieWidth = 3;

    public int getSideTieWidth() {
        return sideTieWidth;
    }

    public void setSideTieWidth(int val) {
        sideTieWidth = val;
    }

    private int sideTieGap = 4;

    public int getSideTieGap() {
        return sideTieGap;
    }

    public void setSideTieGap(int val) {
        sideTieGap = val;
    }

    private int sideRailCount = 2;

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

    private int sideRailGap = 3;

    public int getSideRailGap() {
        return sideRailGap;
    }

    public void setSideRailGap(int val) {
        sideRailGap = val;
    }

    private Color sideRailColor = Color.decode("#9B705E");

    public Color getSideRailColor() {
        return sideRailColor;
    }

    public void setSideRailColor(@Nonnull Color val) {
        sideRailColor = val;
    }

    private int sideBlockLineWidth = 3;

    public int getSideBlockLineWidth() {
        return sideBlockLineWidth;
    }

    public void setSideBlockLineWidth(int val) {
        sideBlockLineWidth = val;
    }

    private int mainBallastWidth = 15;   // defaults to zero (off)

    public int getMainBallastWidth() {
        return mainBallastWidth;
    }

    public void setMainBallastWidth(int val) {
        mainBallastWidth = val;
    }

    private Color mainBallastColor = Color.decode("#9E9C9D");

    public Color getMainBallastColor() {
        return mainBallastColor;
    }

    public void setMainBallastColor(@Nonnull Color val) {
        mainBallastColor = val;
    }

    private int mainTieLength = 11;

    public int getMainTieLength() {
        return mainTieLength;
    }

    public void setMainTieLength(int val) {
        mainTieLength = val;
    }

    private Color mainTieColor = Color.decode("#D5CFCC");

    public Color getMainTieColor() {
        return mainTieColor;
    }

    public void setMainTieColor(@Nonnull Color val) {
        mainTieColor = val;
    }

    private int mainTieWidth = 2;

    public int getMainTieWidth() {
        return mainTieWidth;
    }

    public void setMainTieWidth(int val) {
        mainTieWidth = val;
    }

    private int mainTieGap = 5;

    public int getMainTieGap() {
        return mainTieGap;
    }

    public void setMainTieGap(int val) {
        mainTieGap = val;
    }

    private int mainRailCount = 2;

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

    private int mainRailGap = 3;

    public int getMainRailGap() {
        return mainRailGap;
    }

    public void setMainRailGap(int val) {
        mainRailGap = val;
    }

    private Color mainRailColor = Color.decode("#C0BFBF");

    public Color getMainRailColor() {
        return mainRailColor;
    }

    public void setMainRailColor(@Nonnull Color val) {
        mainRailColor = val;
    }

    private int mainBlockLineWidth = 3;

    public int getMainBlockLineWidth() {
        return mainBlockLineWidth;
    }

    public void setMainBlockLineWidth(int val) {
        mainBlockLineWidth = val;
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
                    if (mainBlockLineWidth != ltdo.getMainBlockLineWidth()) {
                        break;
                    }
                } while (false);
                result = true;
            }
        }
        return result;
    }

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
        result = (37 * result) + mainBlockLineWidth;

        return result;
    }

}
