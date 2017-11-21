/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.layoutEditor;

import java.awt.Color;

/**
 *
 * @author geowar
 */
public class LayoutTrackDrawingOptions {

    private int sideBallastWidth = 13;   // defaults to zero (off)

    protected int getSideBallastWidth() {
        return sideBallastWidth;
    }

    protected void setSideBallastWidth(int val) {
        sideBallastWidth = val;
    }

    private Color sideBallastColor = Color.decode("#AEACAD");

    protected Color getSideBallastColor() {
        return sideBallastColor;
    }

    protected void setSideBallastColor(Color val) {
        sideBallastColor = val;
    }

    private int sideTieLength = 9;

    protected int getSideTieLength() {
        return sideTieLength;
    }

    protected void setSideTieLength(int val) {
        sideTieLength = val;
    }

    private Color sideTieColor = Color.decode("#391E16");

    protected Color getSideTieColor() {
        return sideTieColor;
    }

    protected void setSideTieColor(Color val) {
        sideTieColor = val;
    }

    private int sideTieWidth = 3;

    protected int getSideTieWidth() {
        return sideTieWidth;
    }

    protected void setSideTieWidth(int val) {
        sideTieWidth = val;
    }

    private int sideTieGap = 4;

    protected int getSideTieGap() {
        return sideTieGap;
    }

    protected void setSideTieGap(int val) {
        sideTieGap = val;
    }

    private int sideRailCount = 2;

    protected int getSideRailCount() {
        return sideRailCount;
    }

    protected void setSideRailCount(int val) {
        sideRailCount = val;
    }

    private int sideRailWidth = 1;

    protected int getSideRailWidth() {
        return sideRailWidth;
    }

    protected void setSideRailWidth(int val) {
        sideRailWidth = val;
    }

    private int sideRailGap = 3;

    protected int getSideRailGap() {
        return sideRailGap;
    }

    protected void setSideRailGap(int val) {
        sideRailGap = val;
    }

    private Color sideRailColor = Color.decode("#9B705E");

    protected Color getSideRailColor() {
        return sideRailColor;
    }

    protected void setSideRailColor(Color val) {
        sideRailColor = val;
    }

    private int sideBlockLineWidth = 3;

    protected int getSideBlockLineWidth() {
        return sideBlockLineWidth;
    }

    protected void setSideBlockLineWidth(int val) {
        sideBlockLineWidth = val;
    }

    private int mainBallastWidth = 15;   // defaults to zero (off)

    protected int getMainBallastWidth() {
        return mainBallastWidth;
    }

    protected void setMainBallastWidth(int val) {
        mainBallastWidth = val;
    }

    private Color mainBallastColor = Color.decode("#9E9C9D");

    protected Color getMainBallastColor() {
        return mainBallastColor;
    }

    protected void setMainBallastColor(Color val) {
        mainBallastColor = val;
    }

    private int mainTieLength = 11;

    protected int getMainTieLength() {
        return mainTieLength;
    }

    protected void setMainTieLength(int val) {
        mainTieLength = val;
    }

    private Color mainTieColor = Color.decode("#D5CFCC");

    protected Color getMainTieColor() {
        return mainTieColor;
    }

    protected void setMainTieColor(Color val) {
        mainTieColor = val;
    }

    private int mainTieWidth = 2;

    protected int getMainTieWidth() {
        return mainTieWidth;
    }

    protected void setMainTieWidth(int val) {
        mainTieWidth = val;
    }

    private int mainTieGap = 5;

    protected int getMainTieGap() {
        return mainTieGap;
    }

    protected void setMainTieGap(int val) {
        mainTieGap = val;
    }

    private int mainRailCount = 2;

    protected int getMainRailCount() {
        return mainRailCount;
    }

    protected void setMainRailCount(int val) {
        mainRailCount = val;
    }

    private int mainRailWidth = 2;

    protected int getMainRailWidth() {
        return mainRailWidth;
    }

    protected void setMainRailWidth(int val) {
        mainRailWidth = val;
    }

    private int mainRailGap = 3;

    protected int getMainRailGap() {
        return mainRailGap;
    }

    protected void setMainRailGap(int val) {
        mainRailGap = val;
    }

    private Color mainRailColor = Color.decode("#C0BFBF");

    protected Color getMainRailColor() {
        return mainRailColor;
    }

    protected void setMainRailColor(Color val) {
        mainRailColor = val;
    }

    private int mainBlockLineWidth = 3;

    protected int getMainBlockLineWidth() {
        return mainBlockLineWidth;
    }

    protected void setMainBlockLineWidth(int val) {
        mainBlockLineWidth = val;
    }
}
