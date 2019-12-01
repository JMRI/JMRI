/**********************************************************************************************
 *  panel Servlet - Draw JMRI panels on browser screen
 *    Retrieves panel xml from JMRI and builds panel client-side from that xml, including
 *    click functions.  Sends and listens for changes to panel elements using the JSON WebSocket server.
 *    If no parm passed, page will list links to available panels.
 *  Approach:  Read panel's xml and create widget objects in the browser with all needed attributes.
 *    There are 3 "widgetFamily"s: text, icon and drawn.  States are handled by storing members
 *    iconX, textX, cssX where X is the state.  The corresponding members are "shown" whenever the state changes.
 *    CSS classes are used throughout to attach events to correct widgets, as well as control appearance.
 *    The JSON type is used to send changes to JSON server and to listen for changes made elsewhere.
 *    Drawn widgets are handled by drawing directly on the javascript "canvas" layer.
 *
 *  TODO: show error dialog while retrying connection
 *  TODO: add Cancel button to return to home page on errors (not found, etc.)
 *  TODO: handle "&" in usernames (see Indicator Demo 00.xml)
 *  TODO: update drawn track on color and width changes (would need to create system objects to reflect these chgs)
 *  TODO: research movement of locoicons ("promote" locoicon to system entity in JMRI?, add panel-level listeners?)
 *  TODO: deal with mouseleave, mouseout, touchout, etc. Slide off Stop button on rb1 for example.
 *  TODO: handle inputs/selection on various memory widgets
 *  TODO: alignment of memoryIcons without fixed width is very different.  Recommended workaround is to use fixed width.
 *  TODO:    ditto for sensorIcons with text
 *  TODO: add support for slipturnouticon (one2beros)
 *  TODO: handle (and test) disableWhenOccupied for layoutslip
 *
 *  DONE: draw dashed curves
 *  DONE: handle drawn ellipse (see LMRC APB)
 *  DONE: connect turnouts to layoutturntable rays and make clickable (see WhichWay)
 *  DONE: address color differences between java panel and javascript panel (e.g. lightGray)
 *  DONE: make turnout, levelXing occupancy work like LE panels (more than just checking A)
 *
 **********************************************************************************************/

//persistent (global) variables
var $gWidgets = {};         //array of all widget objects, key=CSSId
var $gPanelList = {};       //store list of available panels
var $gPanel = {};           //store overall panel info
var whereUsed = {};         //associative array of array of elements indexed by systemName or userName
var occupancyNames = {};    //associative array of array of elements indexed by occupancy sensor name
var $gPts = {};             //array of all points, key="pointname.pointtype" (used for layoutEditor panels)
var $gBlks = {};            //array of all blocks, key="blockname" (used for layoutEditor panels)
var $gCtx;                  //persistent context of canvas layer
var $gDashArray = [12, 12]; //on,off of dashed lines
var DOWNEVENT = 'touchstart mousedown';  //check both touch and mouse events
var UPEVENT = 'touchend mouseup';
var SIZE = 3;               //default factor for circles

var UNKNOWN = '0';          //constants to match JSON Server state names
var ACTIVE = '2';
var CLOSED = '2';
var INACTIVE = '4';
var THROWN = '4';
var INCONSISTENT = '8';

var PT_CEN = ".1";          //named constants for point types
var PT_A = ".2";
var PT_B = ".3";
var PT_C = ".4";
var PT_D = ".5";

var LEVEL_XING_A = ".6";
var LEVEL_XING_B = ".7";
var LEVEL_XING_C = ".8";
var LEVEL_XING_D = ".9";

var SLIP_A = ".21";
var SLIP_B = ".22";
var SLIP_C = ".23";
var SLIP_D = ".24";

var STATE_AC = 0x02;
var STATE_BD = 0x04;
var STATE_AD = 0x06;
var STATE_BC = 0x08;

var DARK = 0x00;  //named constants for signalhead states
var RED = 0x01;
var FLASHRED = 0x02;
var YELLOW = 0x04;
var FLASHYELLOW = 0x08;
var GREEN = 0x10;
var FLASHGREEN = 0x20;
var LUNAR = 0x40;
var FLASHLUNAR = 0x80;
var HELD = 0x0100;  //additional to deal with "Held" pseudo-state

var RH_TURNOUT = 1; //named constants for turnout types
var LH_TURNOUT = 2;
var WYE_TURNOUT = 3;
var DOUBLE_XOVER = 4;
var RH_XOVER = 5;
var LH_XOVER = 6;
var SINGLE_SLIP = 7;
var DOUBLE_SLIP = 8;

var jmri = null;

var jmri_logging = false;

//
//  debug functions
//

// log object properties
function $logProperties(obj, force = false) {
	if (jmri_logging || force) {
		var $propList = "";
		for (var $propName in obj) {
			if (typeof obj[$propName] !== "undefined") {
				$propList += ($propName + "='" + obj[$propName] + "', ");
			}
		}
		jmri.log("$logProperties(obj): " + $propList + ".");
	}
}

function isUndefined(x) {
    return (typeof x === "undefined");
}

function isDefined(x) {
    return (typeof x !== "undefined");
}

//
//  Decoration classes
//

class Decoration {
    constructor($widget) {
        //jmri.log("Decoration.constructor(...)");
        //$logProperties(this.$widget, true);
        this.$widget = $widget;
    }
    getEndPoints() {
        [this.ep1, this.ep2] = $getEndPoints(this.$widget);
        //jmri.log("ep1 = {" + this.ep1[0] + "," + this.ep1[1] + "}, ep2 = {" + this.ep2[0] + "," + this.ep2[1] + "}");
    }
    getAngles() {
        var $widget = this.$widget;
        if ($widget.bezier == "yes") {
            this.getBezierAngles();
        } else if ($widget.circle == "yes") {
            this.getCircleAngles();
        } else if ($widget.arc == "yes") {
            this.getArcAngles();
        } else {
            this.startAngleRAD = (Math.PI / 2) - $computeAngleRAD2(this.ep2, this.ep1);
            this.stopAngleRAD = this.startAngleRAD;
        }
        //jmri.log("startAngleDEG: " + $toDegrees(this.startAngleRAD) + ", stopAngleDEG: " + $toDegrees(this.stopAngleRAD) + ".");
    }
    getBezierAngles() {
        var $widget = this.$widget;
        var $cps = $widget.controlpoints;   // get the control points
        var $cp0 = $cps[0];
        var $cpN = $cps[$cps.length - 1];
        var cp0 = [Number($cp0.attributes.x.value), Number($cp0.attributes.y.value)];
        var cpN = [Number($cpN.attributes.x.value), Number($cpN.attributes.y.value)];
        this.startAngleRAD = (Math.PI / 2) - $computeAngleRAD2(cp0, this.ep1);
        this.stopAngleRAD = (Math.PI / 2) - $computeAngleRAD2(this.ep2, cpN);
    }
    getCircleAngles() {
        var $widget = this.$widget;
        var extentAngleDEG = $widget.angle;
        if (extentAngleDEG == 0) {
            extentAngleDEG = 90;
        }
        var startAngleRAD, stopAngleRAD;
        // Convert angle to radiants in order to speed up math
        var halfAngleRAD = $toRadians(extentAngleDEG) / 2;
        // Compute arc's chord
        var a = this.ep2[0] - this.ep1[0];
        var o = this.ep2[1] - this.ep1[1];
        var chord = Math.hypot(a, o);
        // Make sure chord is not null
        // In such a case (ep1 == ep2), there is no arc to draw
        if (chord > 0) {
            var midAngleRAD = Math.atan2(a, o);
            startAngleRAD = (Math.PI / 2) - (midAngleRAD + halfAngleRAD);
            stopAngleRAD = (Math.PI / 2) - (midAngleRAD - halfAngleRAD);
        }
        this.startAngleRAD = startAngleRAD; this.stopAngleRAD = stopAngleRAD;
    }
    getArcAngles() {
        var startAngleRAD, stopAngleRAD;
        if (this.ep1[0] < this.ep2[0]) {
            if (this.ep1[1] < this.ep2[1]) {    //jmri.log("#### QUAD ONE ####");
                startAngleRAD = 0; stopAngleRAD = Math.PI / 2;
            } else {                            //jmri.log("#### QUAD TWO ####");
                startAngleRAD = -Math.PI / 2; stopAngleRAD = 0;
            }
        } else {
            if (this.ep1[1] < this.ep2[1]) {    //jmri.log("#### QUAD THREE ####");
                startAngleRAD = Math.PI / 2; stopAngleRAD = Math.PI;
            } else {                            //jmri.log("#### QUAD FOUR ####");
                startAngleRAD = Math.PI; stopAngleRAD = -Math.PI / 2;
            }
        }
        this.startAngleRAD = startAngleRAD; this.stopAngleRAD = stopAngleRAD;
    }

    draw() {
        this.getEndPoints();
        this.getAngles();
    }

    getArcParams(rw, rh, tp1, tp2) {
        var x, y;
        if (rw < 0) {
            rw = -rw;
            if (rh < 0) {                   //jmri.log("**** QUAD ONE ****");
                x = tp1[0]; y = tp2[1];
                rh = -rh;
            } else {                        //jmri.log("**** QUAD TWO ****");
                x = tp2[0]; y = tp1[1];
            }
        } else {
            if (rh < 0) {                   //jmri.log("**** QUAD THREE ****");
                x = tp2[0]; y = tp1[1];
                rh = -rh;
            } else {                        //jmri.log("**** QUAD FOUR ****");
                x = tp1[0]; y = tp2[1];
            }
        }
        return [x, y, rw, rh];
    }
}   // class Decoration

class ArrowDecoration extends Decoration {
    constructor($widget, $arrow) {
        super($widget);
        //<arrow style="4" end="stop" direction="out" color="#000000" linewidth="4" length="16" gap="1" />
        this.style = Number($arrow.attr('style'));
        this.end = $arrow.attr('end');
        this.direction = $arrow.attr('direction');
        this.color = $arrow.attr('color');
        this.linewidth = Number($arrow.attr('linewidth'));
        this.length = Number($arrow.attr('length'));
        this.gap = Number($arrow.attr('gap'));
        //jmri.log("arrow: {end:" + this.end + ", dir: " + this.direction + "}");
    }
    draw() {
        super.draw();
        $gCtx.save();   // save current line width and color
        // set color and width
        $gCtx.strokeStyle = this.color;
        $gCtx.fillStyle = this.color;
        $gCtx.lineWidth = this.linewidth;
        this.drawArrowStart();
        this.drawArrowStop();
        $gCtx.restore();        // restore color and width back to default
    }
    drawArrowStart() {
        var angleRAD = this.startAngleRAD;
        if (this.$widget.flip == "yes") {
            angleRAD = this.stopAngleRAD;
        }
        this.offset = 1;        // draw the start arrows
        if ((this.end == "start") || (this.end == "both")) {
            if ((this.direction == "in") || (this.direction == "both")) {
                this.drawArrowIn(this.ep1, Math.PI + angleRAD);
            }
            if ((this.direction == "out") || (this.direction == "both")) {
                this.drawArrowOut(this.ep1, Math.PI + angleRAD);
            }
        }
    }
    drawArrowStop() {
        var angleRAD = this.stopAngleRAD;
        if (this.$widget.flip == "yes") {
            angleRAD = this.startAngleRAD;
        }
        this.offset = 1;        // draw the stop arrows
        if ((this.end == "stop") || (this.end == "both")) {
            if ((this.direction == "in") || (this.direction == "both")) {
                this.drawArrowIn(this.ep2, angleRAD);
            }
            if ((this.direction == "out") || (this.direction == "both")) {
                this.drawArrowOut(this.ep2, angleRAD);
            }
        }
    }
    drawArrowIn(ep, angleRAD) {
        $gCtx.save();
        $gCtx.translate(ep[0], ep[1]);
        $gCtx.rotate(angleRAD);

        switch (this.style) {
            default:
                this.style = 0;
            case 0:
                break;
            case 1:
                this.drawArrow1In();
                break;
            case 2:
                this.drawArrow2In();
                break;
            case 3:
                this.drawArrow3In();
                break;
            case 4:
                this.drawArrow4In();
                break;
            case 5:
                this.drawArrow5In();
        }
        $gCtx.restore();
    }   // drawArrowIn

    drawArrowOut(ep, angleRAD) {
        $gCtx.save();
        $gCtx.translate(ep[0], ep[1]);
        $gCtx.rotate(angleRAD);

        switch (this.style) {
            default:
                this.style = 0;
            case 0:
                break;
            case 1:
                this.drawArrow1Out();
                break;
            case 2:
                this.drawArrow2Out();
                break;
            case 3:
                this.drawArrow3Out();
                break;
            case 4:
                this.drawArrow4Out();
                break;
            case 5:
                this.drawArrow5Out();
        }
        $gCtx.restore();
    }   // drawArrowIn

    drawArrow1In() {
        var p1 = [this.offset + this.length, -this.length];
        var p2 = [this.offset, 0];
        var p3 = [this.offset + this.length, +this.length];

        $drawLineP(p1, p2);
        $drawLineP(p2, p3);
        this.offset += this.length + this.gap;
    }

    drawArrow1Out() {
        var p1 = [this.offset, -this.length];
        var p2 = [this.offset + this.length, 0];
        var p3 = [this.offset, +this.length];

        $drawLineP(p1, p2);
        $drawLineP(p2, p3);
        this.offset += this.length + this.gap;
    }

    drawArrow2In() {
        var p1 = [this.offset + this.length, -this.length];
        var p2 = [this.offset, 0];
        var p3 = [this.offset + this.length, +this.length];
        var p4 = [this.offset + this.linewidth + this.gap + this.length, -this.length];
        var p5 = [this.offset + this.linewidth + this.gap, 0];
        var p6 = [this.offset + this.linewidth + this.gap + this.length, +this.length];

        $drawLineP(p1, p2);
        $drawLineP(p2, p3);
        $drawLineP(p4, p5);
        $drawLineP(p5, p6);
        this.offset += this.length + (2 * (this.linewidth + this.gap));
    }

    drawArrow2Out() {
        var p1 = [this.offset, -this.length];
        var p2 = [this.offset + this.length, 0];
        var p3 = [this.offset, +this.length];
        var p4 = [this.offset + this.linewidth + this.gap, -this.length];
        var p5 = [this.offset + this.linewidth + this.gap + this.length, 0];
        var p6 = [this.offset + this.linewidth + this.gap, +this.length];

        $drawLineP(p1, p2);
        $drawLineP(p2, p3);
        $drawLineP(p4, p5);
        $drawLineP(p5, p6);
        this.offset += this.length + (2 * (this.linewidth + this.gap));
    }

    drawArrow3In() {
        var p1 = [this.offset + this.length, -this.length];
        var p2 = [this.offset, 0];
        var p3 = [this.offset + this.length, +this.length];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.lineTo(p3[0], p3[1]);
        $gCtx.closePath();
        if (this.linewidth > 1) {
            $gCtx.fill();
        } else {
            $gCtx.stroke();
        }
        this.offset += this.length + this.gap;
    }

    drawArrow3Out() {
        var p1 = [this.offset, -this.length];
        var p2 = [this.offset + this.length, 0];
        var p3 = [this.offset, +this.length];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.lineTo(p3[0], p3[1]);
        $gCtx.closePath();
        if (this.linewidth > 1) {
            $gCtx.fill();
        } else {
            $gCtx.stroke();
        }
        this.offset += this.length + this.gap;
    }

    drawArrow4In() {
        var p1 = [this.offset, 0];
        var p2 = [this.offset + (4 * this.length), -this.length];
        var p3 = [this.offset + (3 * this.length), 0];
        var p4 = [this.offset + (4 * this.length), +this.length];

        $drawLineP(p1, p3);
        $drawLineP(p2, p3);
        $drawLineP(p3, p4);
        this.offset += (3 * this.length) + this.gap;
    }

    drawArrow4Out() {
        var p1 = [this.offset, 0];
        var p2 = [this.offset + (2 * this.length), -this.length];
        var p3 = [this.offset + (3 * this.length), 0];
        var p4 = [this.offset + (2 * this.length), +this.length];

        $drawLineP(p1, p3);
        $drawLineP(p2, p3);
        $drawLineP(p3, p4);
        this.offset += (3 * this.length) + this.gap;
    }

    drawArrow5In() {
        var p1 = [this.offset, 0];
        var p2 = [this.offset + (4 * this.length), -this.length];
        var p3 = [this.offset + (3 * this.length), 0];
        var p4 = [this.offset + (4 * this.length), +this.length];

        $gCtx.beginPath();
        $gCtx.moveTo(p4[0], p4[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.lineTo(p3[0], p3[1]);
        $gCtx.closePath();
        if (this.linewidth > 1) {
            $gCtx.fill();
        } else {
            $gCtx.stroke();
        }
        $drawLineP(p1, p3);
        this.offset += (3 * this.length) + this.gap;
    }

    drawArrow5Out() {
        var p1 = [this.offset, 0];
        var p2 = [this.offset + (2 * this.length), -this.length];
        var p3 = [this.offset + (3 * this.length), 0];
        var p4 = [this.offset + (2 * this.length), +this.length];

        $gCtx.beginPath();
        $gCtx.moveTo(p4[0], p4[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.lineTo(p3[0], p3[1]);
        $gCtx.closePath();
        if (this.linewidth > 1) {
            $gCtx.fill();
        } else {
            $gCtx.stroke();
        }
        $drawLineP(p1, p3);
        this.offset += (3 * this.length) + this.gap;
    }
}   // class ArrowDecoration

class BridgeDecoration extends Decoration {
    constructor($widget, $bridge) {
        super($widget);
        //<bridge side="both" end="both" color="#000000" linewidth="1" approachwidth="8" deckwidth="10" />
        this.side = $bridge.attr('side');
        this.end = $bridge.attr('end');
        this.color = $bridge.attr('color');
        this.linewidth = Number($bridge.attr('linewidth'));
        this.approachwidth = Number($bridge.attr('approachwidth'));
        this.deckwidth = Number($bridge.attr('deckwidth'));
    }
    draw() {
        super.draw();
        var $widget = this.$widget;
        $gCtx.save();   // save current line width and color
        // set color and width
        $gCtx.strokeStyle = this.color;
        $gCtx.fillStyle = this.color;
        $gCtx.lineWidth = this.linewidth;
        if ($widget.circle == "yes") {
            this.drawBridgeCircle();
        } else if ($widget.arc == "yes") {
            this.drawBridgeArc();
        } else if ($widget.bezier == "yes") {
            this.drawBridgeBezier();
        } else {
            this.drawBridgeStrait();
        }
        this.drawBridgeEnds();
        $gCtx.restore();        // restore color and width back to default
    }   // draw()

    drawBridgeCircle() {
        var $widget = this.$widget;
        var halfWidth = this.deckwidth / 2;
        var ep1 = this.ep1, ep2 = this.ep2;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        var v = [0, +halfWidth];
        if ($widget.flip == "yes") {
            v = [0, -halfWidth];
            [startAngleRAD, stopAngleRAD] = [stopAngleRAD, startAngleRAD];
        }
        if ((this.side == "right") || (this.side == "both")) {
            var tp1 = $point_add(ep1, $point_rotate(v, startAngleRAD));
            var tp2 = $point_add(ep2, $point_rotate(v, stopAngleRAD));
            if ($widget.flip == "yes") {
                $drawArcP(tp2, tp1, $widget.angle);
            } else {
                $drawArcP(tp1, tp2, $widget.angle);
            }
        }
        if ((this.side == "left") || (this.side == "both")) {
            var tp1 = $point_subtract(ep1, $point_rotate(v, startAngleRAD));
            var tp2 = $point_subtract(ep2, $point_rotate(v, stopAngleRAD));
            if ($widget.flip == "yes") {
                $drawArcP(tp2, tp1, $widget.angle);
            } else {
                $drawArcP(tp1, tp2, $widget.angle);
            }
        }
    }
    drawBridgeArc() {   //draw arc of ellipse
        var $widget = this.$widget;
        var tp1 = this.ep1, tp2 = this.ep2;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        if ($widget.flip == "yes") {
            [tp1, tp2] = [tp2, tp1];
            startAngleRAD += Math.PI;
            stopAngleRAD += Math.PI;
        }
        var halfWidth = this.deckwidth / 2;
        var x, y;
        var rw = tp2[0] - tp1[0], rh = tp2[1] - tp1[1];
        [x, y, rw, rh] = this.getArcParams(rw, rh, tp1, tp2);

        rw -= halfWidth;    rh -= halfWidth;
        if ((this.side == "right") || (this.side == "both")) {
            $drawEllipse(x, y, rw, rh, Math.PI + stopAngleRAD, startAngleRAD);
        }
        rw += this.deckwidth;  rh += this.deckwidth;
        if ((this.side == "left") || (this.side == "both")) {
            $drawEllipse(x, y, rw, rh, Math.PI + stopAngleRAD, startAngleRAD);
        }
    }   // drawBridgeArc()

    drawBridgeBezier() {
        var $widget = this.$widget;
        var ep1 = this.ep1, ep2 = this.ep2;
        var points = [[ep1[0], ep1[1]]];    // first point
        var $cps = $widget.controlpoints;   // get the control points
        $cps.each(function( idx, elem ) {   // control points
            points.push([elem.attributes.x.value, elem.attributes.y.value]);
        });
        points.push([ep2[0], ep2[1]]);  // last point
        var halfWidth = this.deckwidth / 2;
        if (((this.side == "left") || (this.side == "both"))) {
            $drawBezier(points, this.color, this.linewidth, -halfWidth);
        }
        if ((this.side == "right") || (this.side == "both")) {
            $drawBezier(points, this.color, this.linewidth, +halfWidth);
        }
    }
    drawBridgeStrait() {
        var $widget = this.$widget;
        var ep1 = this.ep1, ep2 = this.ep2;
        var halfWidth = this.deckwidth / 2;
        var vector = $point_orthogonal($point_normalizeTo($point_subtract(ep2, ep1), halfWidth));
        if ((this.side == "right") || (this.side == "both")) {
            $drawLineP($point_add(ep1, vector), $point_add(ep2, vector));
        }
        if (((this.side == "left") || (this.side == "both"))) {
            $drawLineP($point_subtract(ep1, vector), $point_subtract(ep2, vector));
        }
    }
    drawBridgeEnds() {
        if ((this.end == "entry") || (this.end == "both")) {
            this.drawBridgeEntry();
        }
        if ((this.end == "exit") || (this.end == "both")) {
            this.drawBridgeExit();
       }
    }
    drawBridgeEntry() {
        var $widget = this.$widget;
        var ep1 = this.ep1;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        var halfWidth = this.deckwidth / 2;
        var isRight = ((this.side == "right") || (this.side == "both"));
        var isLeft = ((this.side == "left") || (this.side == "both"));
        if ($widget.flip == "yes") {
            [isRight, isLeft] = [isLeft, isRight];
            [startAngleRAD, stopAngleRAD] = [stopAngleRAD, startAngleRAD];
        }
        var p1, p2;
        if (isRight) {
            p1 = [-this.approachwidth, +this.approachwidth + halfWidth];
            p2 = [0, +halfWidth];
            p1 = $point_add($point_rotate(p1, startAngleRAD), ep1);
            p2 = $point_add($point_rotate(p2, startAngleRAD), ep1);
            $drawLineP(p1, p2);
        }
        if (isLeft) {
            p1 = [-this.approachwidth, -this.approachwidth - halfWidth];
            p2 = [0, -halfWidth];
            p1 = $point_add($point_rotate(p1, startAngleRAD), ep1);
            p2 = $point_add($point_rotate(p2, startAngleRAD), ep1);
            $drawLineP(p1, p2);
        }
    }
    drawBridgeExit() {
        var $widget = this.$widget;
        var ep2 = this.ep2;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        var halfWidth = this.deckwidth / 2;
        var isRight = ((this.side == "right") || (this.side == "both"));
        var isLeft = ((this.side == "left") || (this.side == "both"));
        if ($widget.flip == "yes") {
            [isRight, isLeft] = [isLeft, isRight];
            [startAngleRAD, stopAngleRAD] = [stopAngleRAD, startAngleRAD];
        }
        var p1, p2;
        if (isRight) {
            p1 = [+this.approachwidth, +this.approachwidth + halfWidth];
            p2 = [0, +halfWidth];
            p1 = $point_add($point_rotate(p1, stopAngleRAD), ep2);
            p2 = $point_add($point_rotate(p2, stopAngleRAD), ep2);
            $drawLineP(p1, p2);
        }
        if (isLeft) {
            p1 = [+this.approachwidth, -this.approachwidth - halfWidth];
            p2 = [0, -halfWidth];
            p1 = $point_add($point_rotate(p1, stopAngleRAD), ep2);
            p2 = $point_add($point_rotate(p2, stopAngleRAD), ep2);
            $drawLineP(p1, p2);
        }
    }
}   // BridgeDecoration

class BumperDecoration extends Decoration {
    constructor($widget, $bumper) {
        super($widget);
        //<bumper end="stop" color="#000000" linewidth="2" length="16" />
        this.end = $bumper.attr('end');
        this.color = $bumper.attr('color');
        this.linewidth = Number($bumper.attr('linewidth'));
        this.length = Number($bumper.attr('length'));
    }
    draw() {
        super.draw();
        $gCtx.save();   // save current line width and color
        // set color and width
        $gCtx.strokeStyle = this.color;
        $gCtx.fillStyle = this.color;
        $gCtx.lineWidth = this.linewidth;
        var $widget = this.$widget;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        if ($widget.flip == "yes") {
            [startAngleRAD, stopAngleRAD] = [stopAngleRAD, startAngleRAD];
        }
        var bumperLength = this.length;
        var halfLength = bumperLength / 2;
        // common points
        var p1 = [0, -halfLength], p2 = [0, +halfLength];
        if ((this.end == "start") || (this.end == "both")) {
            var p1 = $point_add($point_rotate(p1, startAngleRAD), this.ep1);
            var p2 = $point_add($point_rotate(p2, startAngleRAD), this.ep1);
            $drawLineP(p1, p2);   // draw cross tie
        }
        if ((this.end == "stop") || (this.end == "both")) {
            var p1 = $point_add($point_rotate(p1, stopAngleRAD), this.ep2);
            var p2 = $point_add($point_rotate(p2, stopAngleRAD), this.ep2);
            $drawLineP(p1, p2);   // draw cross tie
        }
        $gCtx.restore();        // restore color and width back to default
    }
}   //  class BumperDecoration

class TunnelDecoration extends Decoration {
    constructor($widget, $tunnel) {
        super($widget);
        //<tunnel side="right" end="both" color="#FF00FF" linewidth="2" entrancewidth="16" floorwidth="12" />
        this.side = $tunnel.attr('side');

        this.end = $tunnel.attr('end');
        this.color = $tunnel.attr('color');
        this.linewidth = Number($tunnel.attr('linewidth'));
        this.entrancewidth = Number($tunnel.attr('entrancewidth'));
        this.floorwidth = Number($tunnel.attr('floorwidth'));
    }
    draw() {
        super.draw();
        var $widget = this.$widget;
        $gCtx.save();   // save current line width and color
        // set color and width
        $gCtx.strokeStyle = this.color;
        $gCtx.fillStyle = this.color;
        $gCtx.lineWidth = this.linewidth;
        $gCtx.setLineDash([6, 4]);
        if ($widget.circle == "yes") {
            this.drawTunnelCircle();
        } else if ($widget.arc == "yes") {
            this.drawTunnelArc();
        } else if ($widget.bezier == "yes") {
            this.drawTunnelBezier();
        } else {
            this.drawTunnelStrait();
        }
        $gCtx.setLineDash([]);
        this.drawTunnelEnds();
        $gCtx.restore();        // restore color and width back to default
    }   // draw()

    drawTunnelCircle() {
        var $widget = this.$widget;
        var halfWidth = this.floorwidth / 2;
        var ep1 = this.ep1, ep2 = this.ep2;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        var v = [0, +halfWidth];
        if ($widget.flip == "yes") {
            v = [0, -halfWidth];
            [startAngleRAD, stopAngleRAD] = [stopAngleRAD, startAngleRAD];
        }
        if ((this.side == "right") || (this.side == "both")) {
            var tp1 = $point_add(ep1, $point_rotate(v, startAngleRAD));
            var tp2 = $point_add(ep2, $point_rotate(v, stopAngleRAD));
            if ($widget.flip == "yes") {
                $drawArcP(tp2, tp1, $widget.angle);
            } else {
                $drawArcP(tp1, tp2, $widget.angle);
            }
        }
        if ((this.side == "left") || (this.side == "both")) {
            var tp1 = $point_subtract(ep1, $point_rotate(v, startAngleRAD));
            var tp2 = $point_subtract(ep2, $point_rotate(v, stopAngleRAD));
            if ($widget.flip == "yes") {
                $drawArcP(tp2, tp1, $widget.angle);
            } else {
                $drawArcP(tp1, tp2, $widget.angle);
            }
        }
    }
    drawTunnelArc() {   //draw arc of ellipse
        var $widget = this.$widget;
        var tp1 = this.ep1, tp2 = this.ep2;
        var startAngleRAD = this.startAngleRAD, stopAngleRAD = this.stopAngleRAD;
        if ($widget.flip == "yes") {
            [tp1, tp2] = [tp2, tp1];
            startAngleRAD += Math.PI;
            stopAngleRAD += Math.PI;
        }
        var halfWidth = this.floorwidth / 2;
        var x, y;
        var rw = tp2[0] - tp1[0], rh = tp2[1] - tp1[1];
        [x, y, rw, rh] = this.getArcParams(rw, rh, tp1, tp2);

        rw -= halfWidth;    rh -= halfWidth;
        if ((this.side == "right") || (this.side == "both")) {
            $drawEllipse(x, y, rw, rh, Math.PI + stopAngleRAD, startAngleRAD);
        }
        rw += this.floorwidth;  rh += this.floorwidth;
        if ((this.side == "left") || (this.side == "both")) {
            $drawEllipse(x, y, rw, rh, Math.PI + stopAngleRAD, startAngleRAD);
        }
    }   // drawTunnelArc()

    drawTunnelBezier() {
        var $widget = this.$widget;
        var ep1 = this.ep1, ep2 = this.ep2;
        var points = [[ep1[0], ep1[1]]];    // first point
        var $cps = $widget.controlpoints;   // get the control points
        $cps.each(function( idx, elem ) {   // control points
            points.push([elem.attributes.x.value, elem.attributes.y.value]);
        });
        points.push([ep2[0], ep2[1]]);  // last point
        var halfWidth = this.floorwidth / 2;
        if (((this.side == "left") || (this.side == "both"))) {
            $drawBezier(points, this.color, this.linewidth, -halfWidth);
        }
        if ((this.side == "right") || (this.side == "both")) {
            $drawBezier(points, this.color, this.linewidth, +halfWidth);
        }
    }
    drawTunnelStrait() {
        var $widget = this.$widget;
        var ep1 = this.ep1, ep2 = this.ep2;
        var halfWidth = this.floorwidth / 2;
        var vector = $point_orthogonal($point_normalizeTo($point_subtract(ep2, ep1), halfWidth));
        if ((this.side == "right") || (this.side == "both")) {
            $drawLineP($point_add(ep1, vector), $point_add(ep2, vector));
        }
        if (((this.side == "left") || (this.side == "both"))) {
            $drawLineP($point_subtract(ep1, vector), $point_subtract(ep2, vector));
        }
    }
    drawTunnelEnds() {
        if ((this.end == "entry") || (this.end == "both")) {
            this.drawTunnelEntry();
        }
        if ((this.end == "exit") || (this.end == "both")) {
            this.drawTunnelExit();
       }
    }
    drawTunnelEntry() {
        var $widget = this.$widget;
        var ep1 = this.ep1;
        var angleRAD = this.startAngleRAD;
        var isRight = ((this.side == "right") || (this.side == "both"));
        var isLeft = ((this.side == "left") || (this.side == "both"));
        if ($widget.flip == "yes") {
            [isRight, isLeft] = [isLeft, isRight];  // swap left and right
            angleRAD = this.stopAngleRAD;
        }

        $gCtx.save();
        $gCtx.translate(ep1[0], ep1[1]);
        $gCtx.rotate(angleRAD);

        if (isRight) {
            this.drawTunnelEntryRight();
        }
        if (isLeft) {
            this.drawTunnelEntryLeft();
        }
        $gCtx.restore();
    }
    drawTunnelEntryRight() {
        var halfWidth = this.floorwidth / 2;
        var halfEntranceWidth = this.entrancewidth / 2;
        var halfFloorWidth = this.floorwidth / 2;
        var halfDiffWidth = halfEntranceWidth - halfFloorWidth;
        var p1, p2, p3, p4, p5, p6, p7;
        p1 = [0, 0];
        p2 = [0, +halfFloorWidth];
        p3 = [0, +halfEntranceWidth];
        p4 = [-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth];
        p5 = [-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth - halfDiffWidth];
        p6 = [-halfFloorWidth, +halfEntranceWidth - halfDiffWidth];
        p7 = [-halfDiffWidth, 0];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.quadraticCurveTo(p3[0], p3[1], p4[0], p4[1]);
        $gCtx.lineTo(p5[0], p5[1]);
        $gCtx.quadraticCurveTo(p6[0], p6[1], p7[0], p7[1]);
        $gCtx.closePath();
        $gCtx.stroke();
    }
    drawTunnelEntryLeft() {
        var halfWidth = this.floorwidth / 2;
        var halfEntranceWidth = this.entrancewidth / 2;
        var halfFloorWidth = this.floorwidth / 2;
        var halfDiffWidth = halfEntranceWidth - halfFloorWidth;
        var p1, p2, p3, p4, p5, p6, p7;
        p1 = [0, 0];
        p2 = [0, -halfFloorWidth];
        p3 = [0, -halfEntranceWidth];
        p4 = [-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth];
        p5 = [-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth + halfDiffWidth];
        p6 = [-halfFloorWidth, -halfEntranceWidth + halfDiffWidth];
        p7 = [-halfDiffWidth, 0];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.quadraticCurveTo(p3[0], p3[1], p4[0], p4[1]);
        $gCtx.lineTo(p5[0], p5[1]);
        $gCtx.quadraticCurveTo(p6[0], p6[1], p7[0], p7[1]);
        $gCtx.closePath();
        $gCtx.stroke();
    }
    drawTunnelExit() {
        var $widget = this.$widget;
        var ep2 = this.ep2;
        var angleRAD = this.stopAngleRAD;
        var isRight = ((this.side == "right") || (this.side == "both"));
        var isLeft = ((this.side == "left") || (this.side == "both"));
        if ($widget.flip == "yes") {
            [isRight, isLeft] = [isLeft, isRight];
            angleRAD = this.startAngleRAD;
        }

        var halfWidth = this.floorwidth / 2;
        var halfEntranceWidth = this.entrancewidth / 2;
        var halfFloorWidth = this.floorwidth / 2;
        var halfDiffWidth = halfEntranceWidth - halfFloorWidth;

        var p1, p2, p3, p4, p5, p6, p7;

        $gCtx.save();
        $gCtx.translate(ep2[0], ep2[1]);
        $gCtx.rotate(angleRAD);

        if (isRight) {
            this.drawTunnelExitRight();
        }
        if (isLeft) {
            this.drawTunnelExitLeft();
        }
        $gCtx.restore();
    }
    drawTunnelExitRight() {
        var halfWidth = this.floorwidth / 2;
        var halfEntranceWidth = this.entrancewidth / 2;
        var halfFloorWidth = this.floorwidth / 2;
        var halfDiffWidth = halfEntranceWidth - halfFloorWidth;
        var p1, p2, p3, p4, p5, p6, p7;
        p1 = [0, 0];
        p2 = [0, +halfFloorWidth];
        p3 = [0, +halfEntranceWidth];
        p4 = [halfEntranceWidth + halfFloorWidth, +halfEntranceWidth];
        p5 = [halfEntranceWidth + halfFloorWidth, +halfEntranceWidth - halfDiffWidth];
        p6 = [halfFloorWidth, +halfEntranceWidth - halfDiffWidth];
        p7 = [halfDiffWidth, 0];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.quadraticCurveTo(p3[0], p3[1], p4[0], p4[1]);
        $gCtx.lineTo(p5[0], p5[1]);
        $gCtx.quadraticCurveTo(p6[0], p6[1], p7[0], p7[1]);
        $gCtx.closePath();
        $gCtx.stroke();
    }
    drawTunnelExitLeft() {
        var halfWidth = this.floorwidth / 2;
        var halfEntranceWidth = this.entrancewidth / 2;
        var halfFloorWidth = this.floorwidth / 2;
        var halfDiffWidth = halfEntranceWidth - halfFloorWidth;
        var p1, p2, p3, p4, p5, p6, p7;
        p1 = [0, 0];
        p2 = [0, -halfFloorWidth];
        p3 = [0, -halfEntranceWidth];
        p4 = [halfEntranceWidth + halfFloorWidth, -halfEntranceWidth];
        p5 = [halfEntranceWidth + halfFloorWidth, -halfEntranceWidth + halfDiffWidth];
        p6 = [halfFloorWidth, -halfEntranceWidth + halfDiffWidth];
        p7 = [halfDiffWidth, 0];

        $gCtx.beginPath();
        $gCtx.moveTo(p1[0], p1[1]);
        $gCtx.lineTo(p2[0], p2[1]);
        $gCtx.quadraticCurveTo(p3[0], p3[1], p4[0], p4[1]);
        $gCtx.lineTo(p5[0], p5[1]);
        $gCtx.quadraticCurveTo(p6[0], p6[1], p7[0], p7[1]);
        $gCtx.closePath();
        $gCtx.stroke();
    }
}

//process the response returned for the requestPanelXML command
function processPanelXML($returnedData, $success, $xhr) {

    $('div#messageText').text("rendering panel from xml, please wait...");
    $("#activity-alert").addClass("show").removeClass("hidden");
    var $xml = $($returnedData);  //jQuery-ize returned data for easier access

    //remove whitespace
    $xml.xmlClean();

    //get the panel-level values from the xml
    var $panel = $xml.find('panel');
    $($panel[0].attributes).each(function() {
        $gPanel[this.name] = this.value;
    });
    $("#panel-area").width($gPanel.panelwidth);
    $("#panel-area").height($gPanel.panelheight);
    setTitle($gPanel["name"]);

    //insert the canvas layer and set up context used by layouteditor "drawn" objects, set some defaults
    if ($gPanel.paneltype == "LayoutPanel") {
        $("#panel-area").prepend("<canvas id='panelCanvas' width=" + $gPanel.panelwidth + "px height=" +
                $gPanel.panelheight + "px style='position:absolute;z-index:2;'>");
        var canvas = document.getElementById("panelCanvas");
        $gCtx = canvas.getContext("2d");
        $gCtx.strokeStyle = $gPanel.defaulttrackcolor;
        $gCtx.lineWidth = $gPanel.sidetrackwidth;

        //set background color from panel attribute
        $("#panel-area").css({backgroundColor: $gPanel.backgroundcolor});
    }

    //set up context used by switchboardeditor "beanswitch" objects, set some defaults
    if ($gPanel.paneltype == "Switchboard") {
        // TODO add contents
        //$("#panel-area").prepend("<canvas id='panelCanvas' width=95% height=95% style='position:absolute;z-index:2;'>");
        //set background color from panel attribute
        $("#panel-area").css({backgroundColor: $gPanel.backgroundcolor});

        //set short notice
        $("#panel-area").append("<div id=info class=show>Hello " + $gPanel.text + "</div>");
    }

    //process all elements in the panel xml, drawing them on screen, and building persistent array of widgets
    $panel.contents().each(
            function() {
                var $widget = new Array();
                $widget['widgetType'] = this.nodeName;
                $widget['scale'] = "1"; //default to no scale
                $widget['degrees'] = 0; //default to no rotation
                $widget['rotation'] = 0; // default to no rotation
                //convert attributes to an object array
                $(this.attributes).each(function() {
                    $widget[this.name] = this.value;
                });
                //default various css attributes to not-set, then set in later code as needed
                var $hoverText = "";

                // add and normalize the various type-unique values, from the various spots they are stored
                // icon names based on states returned from JSON server,
                $widget['state'] = UNKNOWN; //initial state is unknown
                $widget.jsonType = ""; //default to no JSON type (avoid undefined)

                if ((typeof $widget["systemName"] === "undefined") &&  //set systemName from id if missing
                	  (typeof $widget["id"] !== "undefined")) {
                	$widget.systemName = $widget["id"];
                }
                $widget["id"] = "widget-" + $gUnique(); //set id to a unique value (since same element can be in multiple widgets)
                $widget['widgetFamily'] = $getWidgetFamily($widget, this);
                var $jc = "";
                if (typeof($widget["class"]) !== "undefined") {
                    var $ta = $widget["class"].split('.'); //get last part of java class name for a css class
                    $jc = $ta[$ta.length - 1];
                }
                $widget['classes'] = $widget.widgetType + " " + $widget.widgetFamily + " rotatable " + $jc;
                if ($widget.momentary == "true") {
                    $widget.classes += " momentary ";
                }
                if ($widget.hidden == "yes") {
                    $widget.classes += " hidden ";
                }
                //set additional values in this widget
                switch ($widget.widgetFamily) {
                    case "icon" :
                        $widget['styles'] = $getTextCSSFromObj($widget);
                        switch ($widget.widgetType) {
                            case "positionablelabel" :
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget['rotation'] = $(this).find('icon').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icon').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                break;
                            case "linkinglabel" :
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget['rotation'] = $(this).find('icon').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icon').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                $url = $(this).find('url').text();
                                $widget['url'] = $url; //default to using url value as is
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                break;
                            case "indicatortrackicon" :
                                $widget['icon' + UNKNOWN] = $(this).find('iconmap').find('ClearTrack').attr('url');
                                $widget['icon2'] = $widget['icon' + UNKNOWN];
                                $widget['icon4'] = $widget['icon' + UNKNOWN];
                                $widget['icon8'] = $widget['icon' + UNKNOWN];
                                $widget['iconOccupied' + UNKNOWN] = $(this).find('iconmap').find('OccupiedTrack').attr('url');
                                $widget['iconOccupied2'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['iconOccupied4'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['iconOccupied8'] = $widget['iconOccupied' + UNKNOWN];
                                $widget['rotation'] = $(this).find('iconmap').find('ClearTrack').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('iconmap').find('ClearTrack').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('iconmap').find('ClearTrack').attr('scale');
                                if ($(this).find('occupancysensor')) {  //store the occupancy sensor name and state
                                    $widget['occupancysensor'] = $(this).find('occupancysensor').text();
                                    $widget['occupancystate'] = UNKNOWN;
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                }
                                break;
                            case "indicatorturnouticon" :
                                $widget['name'] = $(this).find('turnout').text();
                                ; //normalize name
                                $widget.jsonType = 'turnout'; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('url');
                                $widget['icon2'] = $(this).find('iconmaps').find('ClearTrack').find('TurnoutStateClosed').attr('url');
                                $widget['icon4'] = $(this).find('iconmaps').find('ClearTrack').find('TurnoutStateThrown').attr('url');
                                $widget['icon8'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateInconsistent').attr('url');
                                $widget['iconOccupied' + UNKNOWN] = $(this).find('iconmaps').find('OccupiedTrack').find('BeanStateUnknown').attr('url');
                                $widget['iconOccupied2'] = $(this).find('iconmaps').find('OccupiedTrack').find('TurnoutStateClosed').attr('url');
                                $widget['iconOccupied4'] = $(this).find('iconmaps').find('OccupiedTrack').find('TurnoutStateThrown').attr('url');
                                $widget['iconOccupied8'] = $(this).find('iconmaps').find('OccupiedTrack').find('BeanStateInconsistent').attr('url');
                                $widget['rotation'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('iconmaps').find('ClearTrack').find('BeanStateUnknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if ($(this).find('occupancysensor')) {  //store the occupancy sensor name and state
                                    $widget['occupancysensor'] = $(this).find('occupancysensor').text();
                                    $widget['occupancystate'] = UNKNOWN;
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes
                                }
                                jmri.getTurnout($widget["systemName"]);
                                break;
                            case "turnouticon" :
                                $widget['name'] = $widget.turnout; //normalize name
                                $widget.jsonType = "turnout"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('icons').find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('icons').find('closed').attr('url');
                                $widget['icon4'] = $(this).find('icons').find('thrown').attr('url');
                                $widget['icon8'] = $(this).find('icons').find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icons').find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                jmri.getTurnout($widget["systemName"]);
                                break;
                            case "sensoricon" :
                                $widget['name'] = $widget.sensor; //normalize name
                                $widget.jsonType = "sensor"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('active').attr('url');
                                $widget['icon4'] = $(this).find('inactive').attr('url');
                                $widget['icon8'] = $(this).find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "LightIcon" :
                                $widget['name'] = $widget.light; //normalize name
                                $widget.jsonType = "light"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('icons').find('unknown').attr('url');
                                $widget['icon2'] = $(this).find('icons').find('on').attr('url');
                                $widget['icon4'] = $(this).find('icons').find('off').attr('url');
                                $widget['icon8'] = $(this).find('icons').find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getLight($widget["systemName"]);
                                break;
                            case "signalheadicon" :
                                $widget['name'] = $widget.signalhead; //normalize name
                                $widget.jsonType = "signalHead"; // JSON object type
                                $widget['icon' + HELD] = $(this).find('icons').find('held').attr('url');
                                $widget['icon' + DARK] = $(this).find('icons').find('dark').attr('url');
                                $widget['icon' + RED] = $(this).find('icons').find('red').attr('url');
                                if (typeof $widget['icon' + RED] === "undefined") { //look for held if no red
                                    $widget['icon' + RED] = $(this).find('icons').find('held').attr('url');
                                }
                                $widget['icon' + YELLOW] = $(this).find('icons').find('yellow').attr('url');
                                $widget['icon' + GREEN] = $(this).find('icons').find('green').attr('url');
                                $widget['icon' + FLASHRED] = $(this).find('icons').find('flashred').attr('url');
                                $widget['icon' + FLASHYELLOW] = $(this).find('icons').find('flashyellow').attr('url');
                                $widget['icon' + FLASHGREEN] = $(this).find('icons').find('flashgreen').attr('url');
                                $widget['icon' + LUNAR] = $(this).find('icons').find('lunar').attr('url');
                                $widget['icon' + FLASHLUNAR] = $(this).find('icons').find('lunar').attr('url');
                                $widget['rotation'] = $(this).find('icons').find('dark').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('icons').find('dark').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('icons').find('dark').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                jmri.getSignalHead($widget["systemName"]);
                                break;
                            case "signalmasticon" :
                                $widget['name'] = $widget.signalmast; //normalize name
                                $widget.jsonType = "signalMast"; // JSON object type
                                var icons = $(this).find('icons').children(); //get array of icons
                                icons.each(function(i, item) {  //loop thru icons array and set all iconXX urls for widget
                                    $widget['icon' + $(item).attr('aspect')] = $(item).attr('url');
                                });
                                $widget['degrees'] = $(this).attr('degrees') * 1;
                                $widget['scale'] = $(this).attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["iconUnlit"] !== "undefined") {
                                    $widget['state'] = "Unlit"; //set the initial aspect to Unlit if defined
                                } else {
                                    $widget['state'] = "Unknown"; //else set to Unknown
                                }
                                jmri.getSignalMast($widget["systemName"]);
                                break;
                            case "multisensoricon" :
                                //create multiple widgets, 1st with all images, stack others with non-active states set to a clear image
                                //  set up siblings array so each widget can also set state of the others
                                $widget.jsonType = "sensor"; // JSON object type
                                $widget['icon' + UNKNOWN] = $(this).find('unknown').attr('url');
                                $widget['icon4'] = $(this).find('inactive').attr('url');
                                $widget['icon8'] = $(this).find('inconsistent').attr('url');
                                $widget['rotation'] = $(this).find('unknown').find('rotation').text() * 1;
                                $widget['degrees'] = ($(this).find('unknown').attr('degrees') * 1) - ($widget.rotation * 90);
                                $widget['scale'] = $(this).find('unknown').attr('scale');
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                $widget['siblings'] = new Array();  //array of related multisensors
                                $widget['hoverText'] = "";          //for override of hovertext
                                var actives = $(this).find('active'); //get array of actives used by this multisensor
                                var $id = $widget.id;
                                actives.each(function(i, item) {  //loop thru array once to set up siblings array, to be copied to all siblings
                                    $widget.siblings.push($id);
                                    $widget.hoverText += $(item).attr('sensor') + " "; //add sibling names to hovertext
                                    $id = "widget-" + $gUnique(); //set new id to a unique value for each sibling
                                });
                                actives.each(function(i, item) {  //loop thru array again to create each widget
                                    $widget['id'] = $widget.siblings[i];         // use id already set in sibling array
                                    $widget.name = $(item).attr('sensor');
                                    $widget['icon2'] = $(item).attr('url');
                                    if (i < actives.size() - 1) { //only save widget and make a new one if more than one active found
                                        $preloadWidgetImages($widget); //start loading all images
                                        $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                        $widget["systemName"] = $widget.name;
                                        $widget['id'] = $widget.ident;
                                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                                        $drawIcon($widget); //actually place and position the widget on the panel
                                        jmri.getSensor($widget["systemName"]);
                                        if (!($widget.systemName in whereUsed)) {  //set where-used for this new sensor
                                            whereUsed[$widget.systemName] = new Array();
                                        }
                                        whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;
                                        $widget = jQuery.extend(true, {}, $widget); //get a new copy of widget
                                        $widget['icon' + UNKNOWN] = "/web/images/transparent_1x1.png";
                                        $widget['icon4'] = "/web/images/transparent_1x1.png"; //set non-actives to transparent image
                                        $widget['icon8'] = "/web/images/transparent_1x1.png";
                                        $widget['state'] = ACTIVE; //to avoid sizing based on the transparent image
                                    }
                                });
                                $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "memoryicon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                var memorystates = $(this).find('memorystate');
                                memorystates.each(function(i, item) {  //get any memorystates defined
                                    //store icon url in "iconXX" where XX is the state to match
                                    $widget['icon' + item.attributes['value'].value] = item.attributes['icon'].value;
                                    $widget['state'] = item.attributes['value'].value; //use value for initial state
                                });
                                $widget['iconnull']="/web/images/transparent_19x16.png"; //transparent for null value
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                        }
                        $preloadWidgetImages($widget); //start loading all images
                        $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                        $widget['id'] = $widget.ident;
                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                        $drawIcon($widget); //actually place and position the widget on the panel
                        break;

                    case "text" :
                        $widget['styles'] = $getTextCSSFromObj($widget);
                        switch ($widget.widgetType) {
                            case "sensoricon" :
                                $widget['name'] = $widget.sensor; //normalize name
                                $widget.jsonType = "sensor"; // JSON object type
                                //set each state's text
                                $widget['text' + UNKNOWN] = $(this).find('unknownText').attr('text');
                                $widget['text2'] = $(this).find('activeText').attr('text');
                                $widget['text4'] = $(this).find('inactiveText').attr('text');
                                $widget['text8'] = $(this).find('inconsistentText').attr('text');
                                //set each state's css attribute array (text color, etc.)
                                $widget['css' + UNKNOWN] = $getTextCSSFromObj($getObjFromXML($(this).find('unknownText')[0]));
                                $widget['css2'] = $getTextCSSFromObj($getObjFromXML($(this).find('activeText')[0]));
                                $widget['css4'] = $getTextCSSFromObj($getObjFromXML($(this).find('inactiveText')[0]));
                                $widget['css8'] = $getTextCSSFromObj($getObjFromXML($(this).find('inconsistentText')[0]));
                                if (typeof $widget.name !== "undefined" && $widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getSensor($widget["systemName"]);
                                break;
                            case "locoicon" :
                            case "trainicon" :
                                //also set the background icon for this one (additional css in .html file)
                                $widget['icon' + UNKNOWN] = $(this).find('icon').attr('url');
                                $widget.styles['background-image'] = "url('" + $widget['icon' + UNKNOWN] + "')";
                                $widget['scale'] = $(this).find('icon').attr('scale');
                                if ($widget.scale != 1) {
                                    $widget.styles['background-size'] = $widget.scale * 100 + "%";
                                    $widget.styles['line-height'] = $widget.scale * 20 + "px";  //center vertically
                                }
                                break;
                            case "fastclock" :
                                jmri.getMemory("IMRATEFACTOR"); //enable updates for fast clock rate
                                $widget['name'] = 'IMCURRENTTIME';  // already defined in JMRI
                                $widget.jsonType = 'memory';
                                $widget.styles['width'] = "166px";  //hard-coded to match original size of clock image
                                $widget.styles['height'] = "166px";
                                $widget['scale'] = $(this).attr('scale');
                                if (typeof $widget.level === "undefined") {
                                    $widget['level'] = 10;  //if not included in xml
                                }
                                $widget['text'] = "00:00 AM";
                                $widget['state'] = "00:00 AM";
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);

                                break;
                            case "memoryicon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['text'] = $widget.memory; //use name for initial text
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                            case "reportericon" :
                                $widget['name'] = $widget.reporter; //normalize name
                                $widget.jsonType = "reporter"; // JSON object type
                                $widget['text'] = $widget.reporter; //use name for initial text
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getReporter($widget["systemName"]);
                                break;
                            case "BlockContentsIcon" :
                                $widget['name'] = $widget.systemName; //normalize name (id got stepped on)
                                $widget.jsonType = "block"; // JSON object type
                                $widget['text'] = $widget.name; //use name for initial text
                                $widget['state'] = $widget.name; //use name for initial state as well
                                jmri.getBlock($widget["systemName"]);
                                break;
                            case "memoryInputIcon" :
                            case "memoryComboIcon" :
                                $widget['name'] = $widget.memory; //normalize name
                                $widget.jsonType = "memory"; // JSON object type
                                $widget['text'] = $widget.memory; //use name for initial text
                                $widget['state'] = $widget.memory; //use name for initial state as well
                                $widget.styles['border'] = "1px solid black" //add border for looks (temporary)
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getMemory($widget["systemName"]);
                                break;
                            case "linkinglabel" :
                                $url = $(this).find('url').text();
                                $widget['url'] = $url; //just store url value in widget, for use in click handler
                                if ($widget.forcecontroloff != "true") {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }
                                break;
                            case "beanswitch" : // Switchboard BeanSwitch of shape "button"
                                $widget['name'] = $widget.label; // normalize name
                                $widget['text'] = $widget.label; // use label as initial button text
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                switch  ($widget["type"]) {
                                    case "T" :
                                        $widget.jsonType = "turnout"; // JSON object type
                                        jmri.getTurnout($widget["systemName"]); // have the html button immediately follow the state on the layout
                                        break;
                                    case "S" :
                                        $widget.jsonType = "sensor"; // JSON object type
                                        jmri.getSensor($widget["systemName"]);
                                        break;
                                    default :
                                        $widget.jsonType = "light"; // JSON object type
                                        jmri.getLight($widget["systemName"]);
                                }
                                // set each state's text (this is only applied when shape is button)
                                $widget['text' + UNKNOWN] = $(this).find('unknownText').attr('text'); // mimick java switchboard buttons
                                $widget['text2'] = $(this).find('activeText').attr('text');
                                $widget['text4'] = $(this).find('inactiveText').attr('text');
                                $widget['text8'] = $(this).find('inconsistentText').attr('text');
                                $widget['state'] = UNKNOWN; // use UNKNOWN for initial state
                                $widget.styles['border'] = "2px solid black" //add border for looks (temporary)
                                $widget.styles['border-radius'] = "8px" // mimick JButtons
                                $widget.styles['color'] = $widget.textcolor; // use jmri color
                                $widget.styles['width'] = (90/$widget.columns) + "%"; // use jmri column number, 90pc to fit on screen
                                // CSS properties
                                $("#panel-area>#" + $widget.id).css(
                                {position: 'relative', float: 'left'});
                                $widget.classes += "button ";
                                if ($widget.connected == "true") {
                                    $widget['text'] = $widget['text0']; // add UNKNOWN state to label of connected switches
                                    $widget.classes += $widget.jsonType + " clickable ";
                                    $widget.styles['background-color'] = "rgb(240,240,240)" // very light grey to mark connected buttons
                                }
                                break;
                        }
                        $widget['safeName'] = $safeName($widget.name);
                        switch ($widget['orientation']) { //use orientation instead of degrees if populated
                        	case "vertical_up"   : $widget.degrees = 270;
                        	case "vertical_down" : $widget.degrees = 90;
                        }
                        $widget['id'] = $widget.ident;
                        $gWidgets[$widget.id] = $widget; //store widget in persistent array
                        //put the text element on the page
                        $("#panel-area").append("<div id=" + $widget.id + " class='" + $widget.classes + "'>" + $widget.text + "</div>");
                        $("#panel-area>#" + $widget.id).css($widget.styles); //apply style array to widget
                        $setWidgetPosition($("#panel-area>#" + $widget.id));
                        break;

                    case "drawn" :
                        if (jmri_logging) {
                            jmri.log("case drawm " + $widget.widgetType);
                            $logProperties($widget);
                        }
                        switch ($widget.widgetType) {
                            case "positionablepoint" :
                                //jmri.log("#### Positionable Point ####");
                                //just store these points in persistent variable for use when drawing tracksegments and layoutturnouts
                                //id is ident plus ".type", e.g. "A4.2"
                                $gPts[$widget.ident + "." + $widget.type] = $widget;
                                //End bumpers and Connectors use wrong type, so also store type 1
                                if (($widget.ident.substring(0, 2) == "EB") ||
                                     ($widget.ident.substring(0, 2) == "EC")) {
                                        $gPts[$widget.ident + ".1"] = $widget;
                                }
                                break;
                            case "layoutblock" :
                                $widget['state'] = UNKNOWN;  //add a state member for this block
                                $widget["blockcolor"] = $widget.trackcolor; //init blockcolor to trackcolor
                                //store these blocks in a persistent var
                                $gBlks[$widget.systemName] = $widget;
                                //jmri.log("layoutblock:");
                                //$logProperties($widget, true);
                                //jmri.log("block[" + $widget.systemName + "].blockcolor: '" + $widget.trackcolor + "'.")
                                jmri.getLayoutBlock($widget.systemName);
                                break;
                            case "layoutturnout" :
                                $widget['id'] = $widget.ident;
                                $widget['name'] = $widget.turnoutname; //normalize name
                                $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                $widget.jsonType = "turnout"; // JSON object type
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                if ((typeof $widget.name !== "undefined") && ($widget.disabled !== "yes")) {
                                    $widget.classes += $widget.jsonType + " clickable "; //make it clickable (unless no turnout assigned)
                                }
                                //set widget occupancy sensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensorA'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystateA'] = $gBlks[$widget.blockname].state;
                                }
                                if (typeof $gBlks[$widget.blockbname] !== "undefined") {
                                    $widget['occupancysensorB'] = $gBlks[$widget.blockbname].occupancysensor;
                                    $widget['occupancystateB'] = $gBlks[$widget.blockbname].state;
                                }
                                if (typeof $gBlks[$widget.blockcname] !== "undefined") {
                                    $widget['occupancysensorC'] = $gBlks[$widget.blockcname].occupancysensor;
                                    $widget['occupancystateC'] = $gBlks[$widget.blockcname].state;
                                }
                                if (typeof $gBlks[$widget.blockdname] !== "undefined") {
                                    $widget['occupancysensorD'] = $gBlks[$widget.blockdname].occupancysensor;
                                    $widget['occupancystateD'] = $gBlks[$widget.blockdname].state;
                                }
                                $gWidgets[$widget.id] = $widget; //store widget in persistent array
                                $storeTurnoutPoints($widget); //also store the turnout's 3 end points for other connections
                                $drawTurnout($widget); //draw the turnout

                                //add an empty, but clickable, div to the panel and position it over the turnout circle
                                $hoverText = " title='" + $widget.name + "' alt='" + $widget.name + "'";
                                $("#panel-area").append("<div id=" + $widget.id + " class='" + $widget.classes + "' " + $hoverText + "></div>");
                                var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius
                                var $cd = $cr * 2;
                                $("#panel-area>#" + $widget.id).css(
                                    {position: 'absolute', left: ($widget.x - $cr) + 'px', top: ($widget.y - $cr) + 'px', zIndex: 3,
                                        width: $cd + 'px', height: $cd + 'px'});
                                if (typeof $widget["systemName"] === "undefined")
                                    $widget["systemName"] = $widget.name;
                                jmri.getTurnout($widget["systemName"]);
                                if ($widget["occupancysensorA"])
                                    jmri.getSensor($widget["occupancysensorA"]); //listen for occupancy changes
                                if ($widget["occupancysensorB"])
                                    jmri.getSensor($widget["occupancysensorB"]); //listen for occupancy changes
                                if ($widget["occupancysensorC"])
                                    jmri.getSensor($widget["occupancysensorC"]); //listen for occupancy changes
                                if ($widget["occupancysensorD"])
                                    jmri.getSensor($widget["occupancysensorD"]); //listen for occupancy changes
                                break;
                            case 'layoutSlip' :
                                $widget['id'] = $widget.ident;
                                $widget['name'] = $widget.ident;
                                $widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name
                                $widget.jsonType = "turnout"; // JSON object type

                                //save the slip state to turnout state information
                                $widget['turnout'] = $(this).find('turnout:first').text();
                                $widget['turnoutB'] = $(this).find('turnoutB:first').text();
                                $widget['stateA'] = UNKNOWN;
                                $widget['stateB'] = UNKNOWN;

                                //jmri.log("tA: " + $widget.turnout + ", tB: " + $widget.turnoutB);

                                $widget['turnoutA_AC'] = Number($(this).find('states').find('A-C').find('turnout').text());
                                $widget['turnoutA_AD'] = Number($(this).find('states').find('A-D').find('turnout').text());
                                $widget['turnoutA_BC'] = Number($(this).find('states').find('B-C').find('turnout').text());
                                $widget['turnoutA_BD'] = Number($(this).find('states').find('B-D').find('turnout').text());

                                $widget['turnoutB_AC'] = Number($(this).find('states').find('A-C').find('turnoutB').text());
                                $widget['turnoutB_AD'] = Number($(this).find('states').find('A-D').find('turnoutB').text());
                                $widget['turnoutB_BC'] = Number($(this).find('states').find('B-C').find('turnoutB').text());
                                $widget['turnoutB_BD'] = Number($(this).find('states').find('B-D').find('turnoutB').text());

                                // default to this state
                                $widget['state'] = UNKNOWN;

                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;

                                if (((typeof $widget.turnout !== "undefined") || (typeof $widget.turnoutB !== "undefined"))
                                		&& ($widget.disabled !== "yes")) {
                                    $widget.classes += $widget.jsonType + " clickable ";
                                }

                                //set widget occupancy sensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensorA'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystateA'] = $gBlks[$widget.blockname].state;
                                }
                                if (typeof $gBlks[$widget.blockbname] !== "undefined") {
                                    $widget['occupancysensorB'] = $gBlks[$widget.blockbname].occupancysensor;
                                    $widget['occupancystateB'] = $gBlks[$widget.blockbname].state;
                                }
                                if (typeof $gBlks[$widget.blockcname] !== "undefined") {
                                    $widget['occupancysensorC'] = $gBlks[$widget.blockcname].occupancysensor;
                                    $widget['occupancystateC'] = $gBlks[$widget.blockcname].state;
                                }
                                if (typeof $gBlks[$widget.blockdname] !== "undefined") {
                                    $widget['occupancysensorD'] = $gBlks[$widget.blockdname].occupancysensor;
                                    $widget['occupancystateD'] = $gBlks[$widget.blockdname].state;
                                }

                                $gWidgets[$widget.id] = $widget;    //store widget in persistent array
                                $storeSlipPoints($widget);          //also store the slip's 4 end points for other connections
                                $drawSlip($widget);                 //draw the slip

                                // convenience variables for points (A, B, C, D)
                                var a = $getPoint($widget.ident + SLIP_A);
                                var b = $getPoint($widget.ident + SLIP_B);
                                var c = $getPoint($widget.ident + SLIP_C);
                                var d = $getPoint($widget.ident + SLIP_D);

                                var $cr = $gPanel.turnoutcirclesize * SIZE; //turnout circle radius
                                var $cd = $cr * 2;                          //turnout circle diameter

                                // center
                                var cen = [$widget.xcen, $widget.ycen];
                                // left center
                                var lcen = $point_midpoint(a, b);
                                var ldelta = $point_subtract(cen, lcen);

                                // left fraction
                                var lf = $cr / Math.hypot(ldelta[0], ldelta[1]);
                                // left circle
                                var lcc = $point_lerp(cen, lcen, lf);

                                //add an empty, but clickable, div to the panel and position it over the left turnout circle
                                $hoverText = " title='" + $widget.turnout + "' alt='" + $widget.turnout + "'";
                                $("#panel-area").append("<div id=" + $widget.id + "l class='" + $widget.classes + "' " + $hoverText + "></div>");
                                $("#panel-area>#" + $widget.id + "l").css(
                                    {position: 'absolute', left: (lcc[0] - $cr) + 'px', top: (lcc[1] - $cr) + 'px', zIndex: 3,
                                        width: $cd + 'px', height: $cd + 'px'});

                                // right center
                                var rcen = $point_midpoint(c, d);
                                var rdelta = $point_subtract(cen, rcen);
                                // right fraction
                                var rf = $cr / Math.hypot(rdelta[0], rdelta[1]);
                                // right circle
                                var rcc = $point_lerp(cen, rcen, rf);

                                //add an empty, but clickable, div to the panel and position it over the right turnout circle
                                $hoverText = " title='" + $widget.turnoutB + "' alt='" + $widget.turnoutB + "'";
                                $("#panel-area").append("<div id=" + $widget.id + "r class='" + $widget.classes + "' " + $hoverText + "></div>");
                                $("#panel-area>#" + $widget.id + "r").css(
                                    {position: 'absolute', left: (rcc[0] - $cr) + 'px', top: (rcc[1] - $cr) + 'px', zIndex: 3,
                                        width: $cd + 'px', height: $cd + 'px'});

                                // setup notifications (?)
                                jmri.getTurnout($widget["turnout"]);
                                jmri.getTurnout($widget["turnoutB"]);

                                if ($widget["occupancysensorA"])
                                    jmri.getSensor($widget["occupancysensorA"]); //listen for occupancy changes
                                if ($widget["occupancysensorB"])
                                    jmri.getSensor($widget["occupancysensorB"]); //listen for occupancy changes
                                if ($widget["occupancysensorC"])
                                    jmri.getSensor($widget["occupancysensorC"]); //listen for occupancy changes
                                if ($widget["occupancysensorD"])
                                    jmri.getSensor($widget["occupancysensorD"]); //listen for occupancy changes

                                // NOTE: turnout & turnoutB may appear to be swapped here however this is intentional
                                // (since the left turnout controls the right points and vice-versa) and we want
                                // the slip circles to toggle the points (not the turnout) on the corresponding side.
                                //
                                // note: the <div> areas above have their titles & alts turnouts swapped (left <-> right) also

                                // add turnout to whereUsed array (as $widget + 'r')
                                if (!($widget.turnout in whereUsed)) {  //set where-used for this new turnout
                                   whereUsed[$widget.turnout] = new Array();
                                }
                                whereUsed[$widget.turnout][whereUsed[$widget.turnout].length] = $widget.id + "r";

                                // add turnoutB to whereUsed array (as $widget + 'l')
                                if (!($widget.turnoutB in whereUsed)) {  //set where-used for this new turnout
                                   whereUsed[$widget.turnoutB] = new Array();
                                }
                                whereUsed[$widget.turnoutB][whereUsed[$widget.turnoutB].length] = $widget.id + "l";
                                break;
                            case "tracksegment" :
                                //jmri.log("#### Track Segment ####");
                                //set widget occupancy sensor from block to speed affected changes later
                                if (typeof $gBlks[$widget.blockname] !== "undefined") {
                                    $widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor;
                                    $widget['occupancystate'] = $gBlks[$widget.blockname].state;
                                }
                                //store this widget in persistent array, with ident as key
                                $widget['id'] = $widget.ident;
                                $gWidgets[$widget.id] = $widget;

                                if ($widget.bezier == "yes") {
                                    $widget['controlpoints'] = $(this).find('controlpoint');
                                }

                                // find decorations
                                var $decorations = $(this).find('decorations');

                                //copy arrow decoration
                                //<arrow style="4" end="stop" direction="out" color="#000000" linewidth="4" length="16" gap="1" />
                                var $arrow = $decorations.find('arrow');
                                var $arrowstyle = $arrow.attr('style');
                                if (typeof $arrowstyle !== "undefined") {
                                    if (Number($arrowstyle) > 0) {
                                        $widget['arrow'] = new ArrowDecoration($widget, $arrow);
                                    }
                                }

                                //copy bridge decoration
                                //<bridge side="both" end="both" color="#000000" linewidth="1" approachwidth="8" deckwidth="10" />
                                var $bridge = $decorations.find('bridge');
                                var $bridgeside = $bridge.attr('side');
                                if (typeof $bridgeside !== "undefined") {
                                    $widget['bridge'] = new BridgeDecoration($widget, $bridge);
                                }

                                //copy bumper decoration
                                //<bumper end="stop" color="#000000" linewidth="2" length="16" />
                                var $bumper = $decorations.find('bumper');
                                var $bumperend = $bumper.attr('end');
                                if (typeof $bumperend !== "undefined") {
                                    $widget['bumper'] = new BumperDecoration($widget, $bumper);
                                }

                                //copy tunnel decoration
                                //<tunnel side="right" end="both" color="#FF00FF" linewidth="2" entrancewidth="16" floorwidth="12" />
                                var $tunnel = $decorations.find('tunnel');
                                var $tunnelside = $tunnel.attr('side');
                                if (typeof $tunnelside !== "undefined") {
                                    $widget['tunnel'] = new TunnelDecoration($widget, $tunnel);
                                }

                                if ($widget["occupancysensor"])
                                    jmri.getSensor($widget["occupancysensor"]); //listen for occupancy changes

                                //draw the tracksegment
                                $drawTrackSegment($widget);
                                break;
                            case "levelxing" :
                                $widget['x'] = $widget.xcen; //normalize x,y
                                $widget['y'] = $widget.ycen;
                                //set widget occupancy sensor from block to speed affected changes later
                                //TODO: handle BD block
                                if (typeof $gBlks[$widget.blocknameac] !== "undefined") {
                                    $widget['occupancysensorAC'] = $gBlks[$widget.blocknameac].occupancysensor;
                                    $widget['occupancystateAC'] = $gBlks[$widget.blocknameac].state;
                                }
                                if (typeof $gBlks[$widget.blocknamebd] !== "undefined") {
                                    $widget['occupancysensorBD'] = $gBlks[$widget.blocknamebd].occupancysensor;
                                    $widget['occupancystateBD'] = $gBlks[$widget.blocknamebd].state;
                                }
                                //store widget in persistent array
                                $widget['id'] = $widget.ident;
                                $gWidgets[$widget.id] = $widget;
                                //also store the xing's 4 end points for other connections
                                $storeLevelXingPoints($widget);
                                //draw the xing
                                $drawLevelXing($widget);

                                //listen for occupancy changes
                                if ($widget["occupancysensorAC"])
                                    jmri.getSensor($widget["occupancysensorAC"]);
                                if ($widget["occupancysensorBD"])
                                    jmri.getSensor($widget["occupancysensorBD"]);
                                break;
                            case "layoutturntable" :
                                //draw the turntable
                                $drawTurntable($widget);
                                break;
                            case "backgroundColor" :  //set background color of the panel itself
                                $("#panel-area").css({"background-color": "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ")"});
                                break;
                            case "layoutShape" :
                                //jmri.log("#### Layout Shape ####");
                                //store this widget in persistent array, with ident as key
                                $widget['id'] = $widget.ident;
                                $gWidgets[$widget.id] = $widget;

                                $widget['points'] = $(this).find('point');

                                //draw the LayoutShape
                                $drawLayoutShape($widget);
                                break;
                            default:
                                jmri.log("unknown $widget.widgetType: " + $widget.widgetType + ".");
                                break;
                        }
                        break;
                    default:
                        //log any unsupported widgets, listing childnodes as info
                        $("div#logArea").append("<br />Unsupported: " + $widget.widgetType + ":");
                        $(this.attributes).each(function() {
                            $("div#logArea").append(" " + this.name);
                        });
                        $("div#logArea").append(" | ");
                        $(this.childNodes).each(function() {
                            $("div#logArea").append(" " + this.nodeName);
                        });
                        break;
                }
                //add widgetid to whereused array to support updates
                if ($widget.systemName) {
                    if (!($widget.systemName in whereUsed)) {
                        whereUsed[$widget.systemName] = new Array();
                    }
                    whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;
                }
                //store occupancy sensors where-used
                if ($gWidgets[$widget.id]) {
                    $store_occupancysensor($widget.id, $widget.occupancysensor);
                    $store_occupancysensor($widget.id, $widget.occupancysensorA);
                    $store_occupancysensor($widget.id, $widget.occupancysensorB);
                    $store_occupancysensor($widget.id, $widget.occupancysensorC);
                    $store_occupancysensor($widget.id, $widget.occupancysensorD);
                    $store_occupancysensor($widget.id, $widget.occupancysensorAC);
                    $store_occupancysensor($widget.id, $widget.occupancysensorBD);
                }
            }  //end of function
    );  //end of each

    //hook up mouseup state toggle function to non-momentary clickable widgets, except for multisensor and linkinglabel
    $('.clickable:not(.momentary):not(.multisensoricon):not(.linkinglabel)').bind(UPEVENT, $handleClick);

    //hook up mouseup state change function to multisensor (special handling)
    $('.clickable.multisensoricon').bind('click', $handleMultiClick);

    //hook up mouseup function to linkinglabel (special handling)
    $('.clickable.linkinglabel').bind(UPEVENT, $handleLinkingLabelClick);

    //momentary widgets always go active on mousedown, and inactive on mouseup, current state is ignored
    $('.clickable.momentary').bind(DOWNEVENT, function(e) {
        e.stopPropagation();
        e.preventDefault(); //prevent double-firing (touch + click)
        sendElementChange($gWidgets[this.id].jsonType, $gWidgets[this.id].systemName, ACTIVE);  //send active on down
    }).bind(UPEVENT, function(e) {
        e.stopPropagation();
        e.preventDefault(); //prevent double-firing (touch + click)
        sendElementChange($gWidgets[this.id].jsonType, $gWidgets[this.id].systemName, INACTIVE);  //send inactive on up
    });

    $drawAllDrawnWidgets(); //draw all the drawn widgets once more, to address some bidirectional dependencies in the xml
    $("#activity-alert").addClass("hidden").removeClass("show");

}

function $store_occupancysensor(id, sensor) {
    if (id && sensor) {
        if (!(sensor in occupancyNames)) {
            occupancyNames[sensor] = new Array();
        }
        occupancyNames[sensor][occupancyNames[sensor].length] = id;
    }
}

//perform regular click-handling, bound to click event for clickable, non-momentary widgets, except for multisensor and linkinglabel.
function $handleClick(e) {
    if (jmri_logging) jmri.log("$handleClick()");

    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];

    // if (null == $widget) {
    //     $logProperties(this);
    // }

    // special case for layoutSlips
    if (this.className.startsWith('layoutSlip ')) {
        if (this.id.startsWith("SL") && (this.id.endsWith("r") || this.id.endsWith("l"))) {
            var slipID = this.id.slice(0, -1);
            $widget = $gWidgets[slipID];

            if (this.id.endsWith("l")) {
                $widget["side"] = "left";
            } else if (this.id.endsWith("r")) {
                $widget["side"] = "right";
            }
            //jmri_logging = true;
            if (jmri_logging) jmri.log("\nlayoutSlip-side:" + $widget.side);

            // convert current slip state to current turnout states
            var $oldStateA, $oldStateB;
            [$oldStateA, $oldStateB] = [$widget.stateA, $widget.stateB];

            // determine next slip state
            var $newState = getNextSlipState($widget);

            if (jmri_logging) {
                jmri.log("$handleClick:layoutSlip: change state from " +
                    slipStateToString($widget.state) + " to " + slipStateToString($newState) + ".");
            }

            // convert new slip state to new turnout states
            var $newStateA, $newStateB;
            [$newStateA, $newStateB] = getTurnoutStatesForSlipState($widget, $newState);

            if ($oldStateA != $newStateA) {
                sendElementChange($widget.jsonType, $widget.turnout, $newStateA);
            }
            if ($oldStateB != $newStateB) {
                sendElementChange($widget.jsonType, $widget.turnoutB, $newStateB);
            }
            //jmri_logging = false;
        } else {
            jmri.log("$handleClick(e): unknown slip widget " + this.id);
            $logProperties(this);
        }
    } else {
        var $newState = $getNextState($widget);  //determine next state from current state
        sendElementChange($widget.jsonType, $widget.systemName, $newState);
        //also send new state to related turnout
        if (typeof $widget.turnoutB !== "undefined") {
            sendElementChange($widget.jsonType, $widget.turnoutB, $newState);
        }
        //used for crossover, layoutTurnout type 5
        if (typeof $widget.secondturnoutname !== "undefined") {
        	//invert 2nd turnout if requested
        	if ($widget.secondturnoutinverted == "true") {
        		$newState = ($newState==CLOSED ? THROWN : CLOSED);
        	}
        	sendElementChange($widget.jsonType, $widget.secondturnoutname, $newState);
        }
    }
}

//perform multisensor click-handling, bound to click event for clickable multisensor widgets.
function $handleMultiClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var clickX = (e.offsetX || e.pageX - $(e.target).offset().left); //get click position on the widget
    var clickY = (e.offsetY || e.pageY - $(e.target).offset().top );
    jmri.log("handleMultiClick X,Y on WxH: " + clickX + "," + clickY + " on " + this.width + "x" + this.height);

    //increment or decrement based on where the click occurred on image
    var missed = true; //flag if click x,y outside image bounds, indicates we didn't get good values
    var dec = false;
    if ($widget.updown == "true") {
        if (clickY >= 0 && clickY <= this.height) missed = false;
        if (clickY > this.height / 2) dec = true;
    } else {
        if (clickX >= 0 && clickX <= this.width)  missed = false;
        if (clickX < this.width / 2)  dec = true;
    }
    var displaying = 0;
    for (i in $widget.siblings) {  //determine which is currently active
        if ($gWidgets[$widget.siblings[i]].state == ACTIVE) {
            displaying = i; //flag the current active sibling
        }
    }
    var next;  //determine which is the next one to be set active (loop around only if click outside object)
    if (dec) {
        next = displaying - 1;
        if (next < 0)
            if (missed)
                next = i;
            else
                next = 0;
    } else {
        next = displaying * 1 + 1;
        if (next > i)
            if (missed)
                next = 0;
            else
                next = i;
    }
    for (i in $widget.siblings) {  //loop through siblings and send changes as needed
        if (i == next) {
            if ($gWidgets[$widget.siblings[i]].state != ACTIVE) {
                sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, ACTIVE);  //set next sensor to active and send command to JMRI server
            }
        } else {
            if ($gWidgets[$widget.siblings[i]].state != INACTIVE) {
                sendElementChange('sensor', $gWidgets[$widget.siblings[i]].name, INACTIVE);  //set all other siblings to inactive if not already
            }
        }
    }
}

//perform click-handling of linkinglabel widgets (3 cases: complete url or frame:<name> where name is a panel or a frame)
function $handleLinkingLabelClick(e) {
    e.stopPropagation();
    e.preventDefault(); //prevent double-firing (touch + click)
    var $widget = $gWidgets[this.id];
    var $url = $widget.url;
    if ($url.toLowerCase().indexOf("frame:") == 0) {
        $frameName = $url.substring(6); //if "frame" found, remove it
        $frameUrl = $gPanelList[$frameName];  //find panel in panel list
        if (typeof $frameUrl === "undefined") {
            $url = "/frame/" + $frameName + ".html"; //not in list, open using frameserver
        } else {
            $url = "/panel/" + $frameUrl; //format for panel server
        }
    }
    window.location = $url;  //navigate to the specified url
}

//draw a Tracksegment (pass in widget)
function $drawTrackSegment($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    //$logProperties($widget, true);  //TODO: remove or comment out for production

    // if positional points have not been loaded...
    if (Object.keys($gPts).length == 0) {
        return; // ... don't try to draw anything yet
    }

    //get the endpoints by name
    var $ep1, $ep2;
    [$ep1, $ep2] = $getEndPoints$($widget);
    if (typeof $ep1 === "undefined") {
        jmri.log("can't draw tracksegment " + $widget.ident + ": connect1: " + $widget.connect1name + "." + $widget.type1 + " undefined.");
        return;
    }
    if (typeof $ep2 === "undefined") {
        jmri.log("can't draw tracksegment " + $widget.ident + ": connect2: " + $widget.connect2name + "." + $widget.type2 + " undefined.");
        return;
    }

    // if ($widget.ident == "T5") {
    //     //jmri.log("$widget.ident:" + $widget.ident);
    //     $logProperties($widget, true);
    // }

    $gCtx.save();   // save current line width and color

    //set trackcolor based on blockcolor
    var $color = $gPanel.defaulttrackcolor;
    var $blk = $gBlks[$widget.blockname];
    if (typeof $blk !== "undefined") {
        $color = $blk.blockcolor;
    }

    var $width = $gPanel.sidetrackwidth;
    if ($widget.mainline == "yes") {
        $width = $gPanel.mainlinetrackwidth;
    }

    // set color and width
    if (typeof $color !== "undefined") {
        $gCtx.strokeStyle = $color;
    }
    if (typeof $width !== "undefined") {
        $gCtx.lineWidth = $width;
    }

    if ($widget.dashed == "yes") {
        $gCtx.setLineDash([6, 4]);
    }

    if ($widget.bezier == "yes") {
        $drawTrackSegmentBezier($widget);
    } else if ($widget.circle == "yes") {
        $drawTrackSegmentCircle($widget);
    } else if ($widget.arc == "yes") {  //draw arc of ellipse
        $drawTrackSegmentArc($widget);
    } else {
        $drawLine($ep1.x, $ep1.y, $ep2.x, $ep2.y, $color, $width);
    }

    if ($widget.dashed == "yes") {
        $gCtx.setLineDash([]);
    }

    //draw its decorations
    $drawDecorations($widget);

    $gCtx.restore();        // restore color and width back to default
}   // $drawTrackSegment

function $drawTrackSegmentBezier($widget) {
    //get the endpoints by name
    var ep1, ep2;
    [ep1, ep2] = $getEndPoints($widget);
    var $cps = $widget.controlpoints;   // get the control points
    var points = [[ep1[0], ep1[1]]];    // first point
    $cps.each(function( idx, elem ) {   // control points
        points.push([elem.attributes.x.value, elem.attributes.y.value]);
    });
    points.push([ep2[0], ep2[1]]);  // last point

    //$point_log("points[0]", points[0]);

    $drawBezier(points);
}

function $drawTrackSegmentCircle($widget) {
    //get the endpoints by name
    var $ep1, $ep2;
    [$ep1, $ep2] = $getEndPoints$($widget);
    if ((typeof $widget.angle === "undefined") || ($widget.angle == 0)) {
        $widget['angle'] = "90";
    }
    //draw curved line
    if ($widget.flip == "yes") {
        $drawArc($ep2.x, $ep2.y, $ep1.x, $ep1.y, $widget.angle);
    } else {
        $drawArc($ep1.x, $ep1.y, $ep2.x, $ep2.y, $widget.angle);
    }
}

function $drawTrackSegmentArc($widget) {
    //get the endpoints by name
    var $ep1, $ep2;
    [$ep1, $ep2] = $getEndPoints$($widget);
    var ep1x = Number($ep1.x), ep1y = Number($ep1.y), ep2x = Number($ep2.x), ep2y = Number($ep2.y);
    if ($widget.flip == "yes") {
        [ep1x, ep1y, ep2x, ep2y] = [ep2x, ep2y, ep1x, ep1y];
    }

    var x, y;
    var rw = ep2x - ep1x, rh = ep2y - ep1y;

    var startAngleRAD, stopAngleRAD;
    if (rw < 0) {
        rw = -rw;
        if (rh < 0) {                       //jmri.log("**** QUAD ONE ****");
            x = ep1x; y = ep2y;
            rh = -rh;
            startAngleRAD = Math.PI / 2;
            stopAngleRAD = Math.PI;
        } else {                            //jmri.log("**** QUAD TWO ****");
            x = ep2x; y = ep1y;
            startAngleRAD = 0;
            stopAngleRAD = Math.PI / 2;
        }
    } else {
        if (rh < 0) {                       //jmri.log("**** QUAD THREE ****");
            x = ep2x; y = ep1y;
            rh = -rh;
            startAngleRAD = Math.PI;
            stopAngleRAD = -Math.PI / 2;
        } else {                            //jmri.log("**** QUAD FOUR ****");
            x = ep1x; y = ep2y;
            startAngleRAD = -Math.PI / 2;
            stopAngleRAD = 0;
        }
    }

    $drawEllipse(x, y, rw, rh, startAngleRAD, stopAngleRAD);
}

function $getEndPoints$($widget) {
    var $ep1 = $gPts[$widget.connect1name + "." + $widget.type1];
    var $ep2 = $gPts[$widget.connect2name + "." + $widget.type2];
    return [$ep1, $ep2];
}

function $getEndPoints($widget) {
    var $ep1, $ep2;
    [$ep1, $ep2] = $getEndPoints$($widget);
    var ep1 = [Number($ep1.x), Number($ep1.y)];
    var ep2 = [Number($ep2.x), Number($ep2.y)];
    return [ep1, ep2];
}

//
//draw decorations
//
function $drawDecorations($widget) {
    if (typeof $widget.arrow !== "undefined") {
        $widget.arrow.draw();
    }
    if (typeof $widget.bridge !== "undefined") {
        $widget.bridge.draw();
    }
    if (typeof $widget.bumper !== "undefined") {
        $widget.bumper.draw();
    }
    if (typeof $widget.tunnel !== "undefined") {
        $widget.tunnel.draw();
    }
}   // $drawDecorations

//draw the analog clock (pass in widget), called on each update of clock
function $drawClock($widget) {
    var $fs = $widget.scale * 100;  //scale percentage, used for text
    var $fcr = $gWidgets['IMRATEFACTOR'].state * 1; //get the fast clock rate factor from its widget
    var $h = "";
    $h += "<div class='clocktext' style='font-size:" + $fs + "%;' >" + $widget.state + "<br />" + $fcr + ":1</div>";  //add the text
    $h += "<img class='clockface' src='/web/images/clockface.png' />";              //add the clockface
    $h += "<img class='clockhourhand' src='/web/images/clockhourhand.png' />";      //add the hour hand
    $h += "<img class='clockminutehand' src='/web/images/clockminutehand.png' />";  //add the minute hand
    $("#panel-area>#" + $widget.id).html($h); //set the html for the widget

    var hours = $widget.state.split(':')[0];    //extract hours from format "H:MM AM"
    var mins = $widget.state.split(':')[1].split(' ')[0]; //extract minutes
    var hdegree = hours * 30 + (mins / 2);
    var hrotate = "rotate(" + hdegree + "deg)";
    $("div.fastclock>img.clockhourhand").css({"transform": hrotate}); //set rotation for hour hand
    var mdegree = mins * 6;
    var mrotate = "rotate(" + mdegree + "deg)";
    $("div.fastclock>img.clockminutehand").css({"transform": mrotate}); //set rotation for minute hand
}

//draw an icon-type widget (pass in widget)
function $drawIcon($widget) {
    var $hoverText = "";
    if (typeof $widget.hoverText !== "undefined") {
        $hoverText = " title='" + $widget.hoverText + "' alt='" + $widget.hoverText + "'";
    }
    if ($hoverText == "" && typeof $widget.name !== "undefined") { //if name available, use it as hover text if still blank
        $hoverText = " title='" + $widget.name + "' alt='" + $widget.name + "'";
    }

    //additional naming for indicator*icon widgets to reflect occupancy
    $indicator = ($widget.occupancysensor && $widget.occupancystate == ACTIVE ? "Occupied" : "");
    //add the image to the panel area, with appropriate css classes and id (skip any unsupported)
    if (typeof $widget['icon' + $indicator + $widget.state] !== "undefined") {
        $imgHtml = "<img id=" + $widget.id + " class='" + $widget.classes +
                "' src='" + $widget["icon" + $indicator + $widget['state']] + "' " + $hoverText + "/>"

        $("#panel-area").append($imgHtml);  //put the html in the panel

        $("#panel-area>#" + $widget.id).css($widget.styles); //apply style array to widget

        //add overlay text if specified, one layer above, and copy attributes (except background-color)
        if (typeof $widget.text !== "undefined") {
            $("#panel-area").append("<div id=" + $widget.id + "-overlay class='overlay'>" + $widget.text + "</div>");
			ovlCSS = {position:'absolute', left: $widget.x + 'px', top: $widget.y + 'px', zIndex: $widget.level*1 + 1, pointerEvents: 'none'};
			$.extend(ovlCSS, $widget.styles); //append the styles from the widget
			delete ovlCSS['background-color'];  //clear the background color
            $("#panel-area>#" + $widget.id + "-overlay").css(ovlCSS);
        }
    } else {
        jmri.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
    }
    $setWidgetPosition($("#panel-area #" + $widget.id));
}

//draw a turntable (pass in widget)
function $drawTurntable($widget) {
    //from jmri.jmrit.display.layoutEditor.layoutTurntable
    $drawCircle($widget.xcen, $widget.ycen, $widget.radius);
    var $raytracks = $(this).find('raytrack');
    var $txcen = $widget.xcen * 1;
    var $tycen = $widget.ycen * 1;
    $raytracks.each(function(i, item) {  //loop thru raytracks, calc and store end of ray point for each
        var $t = [];
        //note: .5 is due to TrackSegment.java TURNTABLE_RAY_OFFSET
        $t['ident'] = $widget.ident + ".5" + item.attributes['index'].value * 1;
        $angle = $toRadians(item.attributes['angle'].value);
        $t['x'] = $txcen + (($widget.radius*1.25)*Math.sin($angle)); //from getRayCoordsIndexed()
        $t['y'] = $tycen - (($widget.radius*1.25)*Math.cos($angle));
        $gPts[$t.ident] = $t; //store the endpoint of this ray
        //draw the line from ray endpoint to turntable edge
        var $t1 = [];
        $t1['x'] = $t.x - (($t.x - $txcen) * 0.2); //from drawTurntables()
        $t1['y'] = $t.y - (($t.y - $tycen) * 0.2);
        $drawLine($t1.x, $t1.y, $t.x, $t.y, $gPanel.defaulttrackcolor, $gPanel.sidetrackwidth);
    });
}

//draw a LevelXing (pass in widget)
function $drawLevelXing($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    //get track widths
    var $widthAC = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectaname] !== "undefined") {
        if ($gWidgets[$widget.connectaname].mainline == "yes") {
            $widthAC = $gPanel.mainlinetrackwidth;
        }
    }
    if (typeof $gWidgets[$widget.connectcname] !== "undefined") {
        if ($gWidgets[$widget.connectcname].mainline == "yes") {
            $widthAC = $gPanel.mainlinetrackwidth;
        }
    }

    var $widthBD = $gPanel.sidetrackwidth;
    if (typeof $gWidgets[$widget.connectbname] !== "undefined") {
        if ($gWidgets[$widget.connectbname].mainline == "yes") {
            $widthBD = $gPanel.mainlinetrackwidth;
        }
    }
    if (typeof $gWidgets[$widget.connectdname] !== "undefined") {
        if ($gWidgets[$widget.connectdname].mainline == "yes") {
            $widthBD = $gPanel.mainlinetrackwidth;
        }
    }

    //  set trackcolor based on block color
    var $colorAC = $gPanel.defaulttrackcolor;
    var $blkAC = $gBlks[$widget.blocknameac];
    if (typeof $blkAC !== "undefined") {
        $colorAC = $blkAC.blockcolor;
    }
    var $colorBD = $gPanel.defaulttrackcolor;
    var $blkBD = $gBlks[$widget.blocknamebd];
    if (typeof $blkBD !== "undefined") {
        $colorBD = $blkBD.blockcolor;
    }

    //retrieve the points
    var cen = [$widget.xcen, $widget.ycen];
    var a = $getPoint($widget.ident + LEVEL_XING_A);
    var b = $getPoint($widget.ident + LEVEL_XING_B);
    var c = $getPoint($widget.ident + LEVEL_XING_C);
    var d = $getPoint($widget.ident + LEVEL_XING_D);

    //levelxing   A
    //          D-+-B
    //            C
    $drawLineP(a, c, $colorAC, $widthAC); //A to C
    $drawLineP(b, d, $colorBD, $widthBD); //B to D
}

//draw a Turnout (pass in widget)
//  see LayoutTurnout.draw()
function $drawTurnout($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }

    //get widths
    var $sideWidth = $gPanel.sidetrackwidth;
    var $mainWidth = $gPanel.mainlinetrackwidth;
    var $widthA = $sideWidth;
    if (typeof $gWidgets[$widget.connectaname] !== "undefined") {
        if ($gWidgets[$widget.connectaname].mainline == "yes") {
            $widthA = $mainWidth;
        }
    }
    var $widthB = $sideWidth;
    if (typeof $gWidgets[$widget.connectbname] !== "undefined") {
        if ($gWidgets[$widget.connectbname].mainline == "yes") {
            $widthB = $mainWidth;
        }
    }
    var $widthC = $sideWidth;
    if (typeof $gWidgets[$widget.connectcname] !== "undefined") {
        if ($gWidgets[$widget.connectcname].mainline == "yes") {
            $widthC = $mainWidth;
        }
    }
    var $widthD = $sideWidth;
    if (typeof $gWidgets[$widget.connectdname] !== "undefined") {
        if ($gWidgets[$widget.connectdname].mainline == "yes") {
            $widthD = $mainWidth;
        }
    }

    //get colors
    var $eraseColor = $gPanel.backgroundcolor;
    var $trackColor = $gPanel.defaulttrackcolor;

    //set track colors based on block colors
    var $colorA = $trackColor;
    var $blkA = $gBlks[$widget.blockname];
    if (typeof $blkA !== "undefined") {
        $colorA = $blkA.blockcolor;
    }
    var $colorB = $colorA;
    var $blkB = $gBlks[$widget.blockbname];
    if (typeof $blkB !== "undefined") {
        $colorB = $blkB.blockcolor;
    }
    var $colorC = $colorA;
    var $blkC = $gBlks[$widget.blockcname];
    if (typeof $blkC !== "undefined") {
        $colorC = $blkC.blockcolor;
    }
    var $colorD = $colorA;
    var $blkD = $gBlks[$widget.blockdname];
    if (typeof $blkD !== "undefined") {
        $colorD = $blkD.blockcolor;
    }

    var cen = [$widget.xcen * 1, $widget.ycen * 1]
    var a = $getPoint($widget.ident + PT_A);
    var b = $getPoint($widget.ident + PT_B);
    var c = $getPoint($widget.ident + PT_C);

    var ab = $point_midpoint(a, b);

    //turnout A--+--B
    //            \-C
    if ($widget.type == LH_TURNOUT || $widget.type == RH_TURNOUT || $widget.type == WYE_TURNOUT) {
        //always draw from a to cen
        $drawLineP(a, cen, $colorA, $widthA); //a to cen

        //if closed or thrown, draw the selected leg and erase the other one
        if ($widget.state == CLOSED || $widget.state == THROWN) {
            if ($widget.state == $widget.continuing) {
                $drawLineP(cen, c, $eraseColor, $widthC); //erase center to C (diverging leg)
                if ($gPanel.turnoutdrawunselectedleg == 'yes') {
                    $drawLineP(c, $point_midpoint(cen, c), $colorB, $widthC); //C to midC (diverging leg)
                }
                $drawLineP(cen, b, $colorB, $widthB); //center to B (straight leg)
            } else {
                    $drawLineP(cen, b, $eraseColor, $widthB); //erase center to B (straight leg)
                if ($gPanel.turnoutdrawunselectedleg == 'yes') {
                    $drawLineP(b, $point_midpoint(cen, b), $colorC, $widthB); //B to midB (straight leg)
                }
                $drawLineP(cen, c, $colorC, $widthC); //center to C (diverging leg)
            }
        } else {  //if state is undefined, draw both legs
            $drawLineP(cen, b, $colorB, $widthB); //center to B (straight leg)
            $drawLineP(cen, c, $colorC, $widthC); //center to C (diverging leg)
        }
        // xover A--B
        //       D--C
    } else if ($widget.type == LH_XOVER || $widget.type == RH_XOVER || $widget.type == DOUBLE_XOVER) {
        var d = $getPoint($widget.ident + PT_D);

        var ab = $point_midpoint(a, b);
        var cd = $point_midpoint(c, d);

        if ($widget.state == CLOSED || $widget.state == THROWN) {
            $drawLineP(a, b, $eraseColor, $mainWidth);      //erase A to B
            $drawLineP(c, d, $eraseColor, $mainWidth);      //erase C to D
            $drawLineP(ab, cd, $eraseColor, $mainWidth);    //erase midAB to midDC
            $drawLineP(a, c, $eraseColor, $mainWidth);      //erase A to C
            $drawLineP(b, d, $eraseColor, $mainWidth);      //erase B to D
            if ($widget.state == $widget.continuing) {
                //draw closed legs
                $drawLineP(a, ab, $colorA, $widthA);    //A to mid ab
                $drawLineP(b, ab, $colorB, $widthB);    //B to mid ab
                $drawLineP(c, cd, $colorC, $widthC);    //C to mid cd
                $drawLineP(d, cd, $colorD, $widthD);    //D to mid cd
                //draw open legs
                if ($widget.type == DOUBLE_XOVER) {
                    var acen = $point_midpoint(a, cen);
                    var bcen = $point_midpoint(b, cen);
                    var ccen = $point_midpoint(c, cen);
                    var dcen = $point_midpoint(d, cen);
                    $drawLineP(acen, cen, $colorA, $widthA);    //mid a cen to cen
                    $drawLineP(bcen, cen, $colorB, $widthB);    //mid b cen to cen
                    $drawLineP(ccen, cen, $colorC, $widthC);    //mid c cen to cen
                    $drawLineP(dcen, cen, $colorD, $widthD);    //mid d cen to cen
                } else if ($widget.type == RH_XOVER) {
                    $drawLineP($point_midpoint(ab, cen), cen, $colorA, $widthA);
                    $drawLineP($point_midpoint(cd, cen), cen, $colorC, $widthC);
                } else if ($widget.type == LH_XOVER) {
                    $drawLineP($point_midpoint(ab, cen), cen, $colorB, $widthB);
                    $drawLineP($point_midpoint(cd, cen), cen, $colorD, $widthD);
                }
            } else {
                var aab = $point_midpoint(a, ab);
                var abb = $point_midpoint(ab, b);
                var ccd = $point_midpoint(c, cd);
                var cdd = $point_midpoint(cd, d);
                if ($widget.type == DOUBLE_XOVER) {
                    //draw open legs
                    $drawLineP(ab, aab, $colorA, $widthA);
                    $drawLineP(ab, abb, $colorB, $widthB);
                    $drawLineP(cd, ccd, $colorC, $widthC);
                    $drawLineP(cd, cdd, $colorD, $widthD);

                    //draw closed legs
                    $drawLineP(a, cen, $colorA, $widthA);
                    $drawLineP(b, cen, $colorB, $widthB);
                    $drawLineP(c, cen, $colorC, $widthC);   //C to cen
                    $drawLineP(d, cen, $colorD, $widthD);   //D to cen
                } else if ($widget.type == RH_XOVER) {
                    //draw open legs
                    $drawLineP(b, abb, $colorB, $widthB);
                    $drawLineP(d, cdd, $colorD, $widthD);

                    //draw closed legs
                    $drawLineP(a, ab, $colorA, $widthA);    //A to mid ab
                    $drawLineP(ab, cen, $colorA, $widthA);  //midAB to cen
                    $drawLineP(cen, cd, $colorC, $widthC);  //cen to midDC
                    $drawLineP(c, cd, $colorC, $widthC);    //C to mid cd
                } else {  //LH_XOVER
                    //draw open legs
                    $drawLineP(a, aab, $colorA, $widthA);
                    $drawLineP(c, ccd, $colorC, $widthC);

                    //draw closed legs
                    $drawLineP(b, ab, $colorB, $widthB);    //B to mid ab
                    $drawLineP(ab, cen, $colorB, $widthB);  //midAB to cen
                    $drawLineP(cen, cd, $colorD, $widthD);  //cen to midDC
                    $drawLineP(d, cd, $colorD, $widthD);    //D to mid cd
                }
            }
        } else {  //if state is undefined, draw all legs
            $drawLineP(a, ab, $colorA, $widthA);    //A to mid ab
            $drawLineP(b, ab, $colorB, $widthB);    //B to mid ab
            $drawLineP(c, cd, $colorC, $widthC);    //C to mid cd
            $drawLineP(d, cd, $colorD, $widthD);    //D to mid cd
            if ($widget.type == DOUBLE_XOVER) {
                $drawLineP(a, cen, $colorA, $widthA);   //A to cen
                $drawLineP(b, cen, $colorB, $widthB);   //B to cen
                $drawLineP(c, cen, $colorC, $widthC);   //C to cen
                $drawLineP(d, cen, $colorD, $widthD);   //D to cen
            } else if ($widget.type == RH_XOVER) {
                $drawLineP(ab, cen, $colorA, $widthA);  //midAB to cen
                $drawLineP(cen, cd, $colorC, $widthC);  //cen to midDC
            } else {  //LH_XOVER
                $drawLineP(ab, cen, $colorB, $widthB);  //midAB to cen
                $drawLineP(cen, cd, $colorD, $widthD);  //cen to midDC
            }
        }
    }

    // erase and draw turnout circles if enabled, including occupancy check
    if (($gPanel.turnoutcircles == "yes") && ($widget.disabled !== "yes")) {
    	$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $eraseColor, 1);
    	if  (($widget.disableWhenOccupied !== "yes") || ($widget.occupancystate != ACTIVE)) {
    	    var $color = $gPanel.turnoutcirclecolor;

            if (($widget.state != UNKNOWN) && ($widget.state != $widget.continuing)) {
                $color = $gPanel.turnoutcirclethrowncolor;
            }
            if ($gPanel.turnoutfillcontrolcircles == "yes") {
                $fillCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $color, 1);
            } else {
                $drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $color, 1);
            }
    	}
    	// if disableWhenOccupied requested, disable click if enabled and active
    	if  ($widget.disableWhenOccupied == "yes") {
    		if ($widget.occupancystate == ACTIVE) {
    			$('#'+$widget.id).removeClass("clickable");
    			$('#'+$widget.id).unbind(UPEVENT, $handleClick);
    		} else {
    			$('#'+$widget.id).addClass("clickable");
    			$('#'+$widget.id).bind(UPEVENT, $handleClick);
    		}
    	}
    }   // function $drawTurnout($widget)
}

//draw a Slip (pass in widget)
//  see LayoutSlip.draw()
function $drawSlip($widget) {
    //if set to hidden, don't draw anything
    if ($widget.hidden == "yes") {
        return;
    }
    if (jmri_logging) {
        jmri.log("$drawSlip(" + $widget.id + "): state: " + $widget.state);
    }

    var $mainWidth = $gPanel.mainlinetrackwidth;
    var $sideWidth = $gPanel.sidetrackwidth;

    var $widthA = $sideWidth;
    if (isDefined($gWidgets[$widget.connectaname])) {
        if ($gWidgets[$widget.connectaname].mainline == "yes") {
            $widthA = $mainWidth;
        }
    }

    var $widthB = $sideWidth;
    if (isDefined($gWidgets[$widget.connectbname])) {
        if ($gWidgets[$widget.connectbname].mainline == "yes") {
            $widthB = $mainWidth;
        }
    }

    var $widthC = $sideWidth;
    if (isDefined($gWidgets[$widget.connectcname])) {
        if ($gWidgets[$widget.connectcname].mainline == "yes") {
            $widthC = $mainWidth;
        }
    }

    var $widthD = $sideWidth;
    if (isDefined($gWidgets[$widget.connectdname])) {
        if ($gWidgets[$widget.connectdname].mainline == "yes") {
            $widthD = $mainWidth;
        }
    }

    var cen = [$widget.xcen * 1, $widget.ycen * 1]
    var a = $getPoint($widget.ident + SLIP_A);
    var b = $getPoint($widget.ident + SLIP_B);
    var c = $getPoint($widget.ident + SLIP_C);
    var d = $getPoint($widget.ident + SLIP_D);

    var $eraseColor = $gPanel.backgroundcolor;
    var $trackColor = $gPanel.defaulttrackcolor;

    var $blkA = $gBlks[$widget.blockname];
    var $colorA = isDefined($blkA) ? $blkA.blockcolor : $trackColor;
    var $colorAt = isDefined($blkA) ? $blkA.trackcolor : $trackColor;
    var $blkB = $gBlks[$widget.blockbname];
    var $colorB = isDefined($blkB) ? $blkB.blockcolor : $colorA;
    var $colorBt = isDefined($blkB) ? $blkB.trackcolor : $colorAt;
    var $blkC = $gBlks[$widget.blockcname];
    var $colorC = isDefined($blkC) ? $blkC.blockcolor : $colorA;
    var $colorCt = isDefined($blkC) ? $blkC.trackcolor : $colorAt;
    var $blkD = $gBlks[$widget.blockdname];
    var $colorD = isDefined($blkD) ? $blkD.blockcolor : $colorA;
    var $colorDt = isDefined($blkD) ? $blkD.trackcolor : $colorAt;

    //slip A==-==D
    //      \\ //
    //        X
    //      // \\
    //     B==-==C
    // var STATE_AC = 0x02;
    // var STATE_BD = 0x04;
    // var STATE_AD = 0x06;
    // var STATE_BC = 0x08;

    // ERASE EVERYTHING FIRST
    var acen3rd = $point_third(a, cen);
    var bcen3rd = $point_third(b, cen);
    var ccen3rd = $point_third(c, cen);
    var dcen3rd = $point_third(d, cen);
    var ad3rd = $point_midpoint(acen3rd, dcen3rd);
    var bc3rd = $point_midpoint(bcen3rd, ccen3rd);

    if ($widget.state != STATE_AC) {
        $drawLineP(a, acen3rd, $eraseColor, $mainWidth);
        $drawLineP(acen3rd, ccen3rd, $eraseColor, $mainWidth);      //erase AC
        $drawLineP(ccen3rd, c, $eraseColor, $mainWidth);
    }
    if ($widget.state != STATE_BD) {
        $drawLineP(b, bcen3rd, $eraseColor, $mainWidth);
        $drawLineP(bcen3rd, dcen3rd, $eraseColor, $mainWidth);      //erase BD
        $drawLineP(dcen3rd, d, $eraseColor, $mainWidth);
    }
    if ($widget.state != STATE_AD) {
        $drawLineP(a, acen3rd, $eraseColor, $mainWidth);
        $drawLineP(acen3rd, dcen3rd, $eraseColor, $mainWidth);      //erase AD
        $drawLineP(dcen3rd, d, $eraseColor, $mainWidth);
    }
    if ($widget.slipType == DOUBLE_SLIP) {
        if ($widget.state != STATE_BC) {
            $drawLineP(b, bcen3rd, $eraseColor, $mainWidth);
            $drawLineP(bcen3rd, ccen3rd, $eraseColor, $mainWidth);  //erase BC
            $drawLineP(ccen3rd, c, $eraseColor, $mainWidth);
        }
    }

    // THEN DRAW ROUTE
    var forceUnselected = false;
    if ($widget.state == STATE_AD) {
        // draw A<===>D
        $drawLineP(a, acen3rd, $colorA, $widthA);
        $drawLineP(acen3rd, ad3rd, $colorA, $widthA);
        $drawLineP(d, dcen3rd, $colorD, $widthD);
        $drawLineP(dcen3rd, ad3rd, $colorD, $widthD);
    } else if ($widget.state == STATE_AC) {
        // draw A<===>C
        $drawLineP(a, acen3rd, $colorA, $widthA);
        $drawLineP(acen3rd, cen, $colorA, $widthA);
        $drawLineP(c, ccen3rd, $colorC, $widthC);
        $drawLineP(ccen3rd, cen, $colorC, $widthC);
    } else if ($widget.state == STATE_BD) {
        // draw B<===>D
        $drawLineP(b, bcen3rd, $colorB, $widthB);
        $drawLineP(bcen3rd, cen, $colorB, $widthB);
        $drawLineP(d, dcen3rd, $colorD, $widthD);
        $drawLineP(dcen3rd, cen, $colorD, $widthD);
    } else if ($widget.state == STATE_BC) {
        if ($widget.slipType == DOUBLE_SLIP) {
            // draw B<===>C
            $drawLineP(b, bcen3rd, $colorB, $widthB);
            $drawLineP(bcen3rd, bc3rd, $colorB, $widthB);
            $drawLineP(c, ccen3rd, $colorC, $widthC);
            $drawLineP(ccen3rd, bc3rd, $colorC, $widthC);
        }   // DOUBLE_SLIP
    } else {
        forceUnselected = true; // if not valid state force drawing unselected
    }

    if (forceUnselected || ($gPanel.turnoutdrawunselectedleg == 'yes')) {
        if ($widget.state == STATE_AC) {
            $drawLineP(b, bcen3rd, $colorBt, $widthB);
            $drawLineP(d, dcen3rd, $colorDt, $widthD);
        } else if ($widget.state == STATE_BD) {
            $drawLineP(a, acen3rd, $colorAt, $widthA);
            $drawLineP(c, ccen3rd, $colorCt, $widthC);
        } else if ($widget.state == STATE_AD) {
            $drawLineP(b, bcen3rd, $colorBt, $widthB);
            $drawLineP(c, ccen3rd, $colorCt, $widthC);
        } else if ($widget.state == STATE_BC) {
            $drawLineP(a, acen3rd, $colorAt, $widthA);
            $drawLineP(d, dcen3rd, $colorDt, $widthD);
        } else {
            $drawLineP(a, acen3rd, $colorAt, $widthA);
            $drawLineP(b, bcen3rd, $colorBt, $widthB);
            $drawLineP(c, ccen3rd, $colorCt, $widthC);
            $drawLineP(d, dcen3rd, $colorDt, $widthD);
        }
    }

    if (($gPanel.turnoutcircles == "yes") && ($widget.disabled !== "yes")) {
        //draw the two control circles
        var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius

        // center
        var cen = [$widget.xcen, $widget.ycen];
        // left center
        var lcen = $point_midpoint(a, b);
        var ldelta = $point_subtract(cen, lcen);

        // left fraction
        var lf = $cr / Math.hypot(ldelta[0], ldelta[1]);
        // left circle
        var lcc = $point_lerp(cen, lcen, lf);

        $drawCircleP(lcc, $cr, $gPanel.turnoutcirclecolor, 1);

        // right center
        var rcen = $point_midpoint(c, d);
        var rdelta = $point_subtract(cen, rcen);
        // right fraction
        var rf = $cr / Math.hypot(rdelta[0], rdelta[1]);
        // right circle
        var rcc = $point_lerp(cen, rcen, rf);

        $drawCircleP(rcc, $cr, $gPanel.turnoutcirclecolor, 1);
    }
}   // function $drawSlip($widget)

function $drawLayoutShape($widget) {
    var $pts = $widget.points;   // get the points
    var len = $pts.length;
    if (len > 0) {
        $gCtx.save();   // save current line width and color

        if (typeof $widget.lineColor !== "undefined") {
            $gCtx.strokeStyle = $widget.lineColor;
        }
        if (typeof $widget.fillColor !== "undefined") {
            $gCtx.fillStyle = $widget.fillColor;
        }
        if (typeof $widget.linewidth !== "undefined") {
            $gCtx.lineWidth = $widget.linewidth;
        }

        $gCtx.beginPath();

        var shapeType = $widget.type;

        var cnt = $pts.length;
        $pts.each(function(idx, $lsp) {  //loop thru points
            // this point
            var p = $getLayoutShapePoint($lsp);

            // left point
            var idxL = $wrapValue(idx - 1, 0, cnt);
            var $lspL = $pts[idxL];
            var pL = $getLayoutShapePoint($lspL);
            var midL = $point_midpoint(pL, p);

            // right point
            var idxR = $wrapValue(idx + 1, 0, cnt);
            var $lspR = $pts[idxR];
            var pR = $getLayoutShapePoint($lspR);
            var midR = $point_midpoint(p, pR);

            var lspt = $lsp.attributes.type.value;  // Straight or Curve

            // if this is an open shape...
            if (shapeType == "eOpen") {
                // and this is first or last point...
                if ((idx == 0) || (idxR == 0)) {
                    // then force straight shape point type
                    lspt = "Straight";
                }
            }
            if (lspt == "Straight") {
                if (idx == 0) { // if this is the first point...
                    // ...and our shape is open...
                    if (shapeType == "Open") {
                        $gCtx.moveTo(p[0], p[1]);    // then start here
                    } else {    // otherwise
                        $gCtx.moveTo(midL[0], midL[1]);  //start here
                        $gCtx.lineTo(p[0], p[1]);        //draw to here
                    }
                } else {
                    $gCtx.lineTo(midL[0], midL[1]);  //start here
                    $gCtx.lineTo(p[0], p[1]);        //draw to here
                }
                // if this is not the last point...
                // ...or our shape isn't open
                if ((idxR != 0) || (shapeType == "Open")) {
                    $gCtx.lineTo(midR[0], midR[1]);      // draw to here
                }
            } else if (lspt == "Curve") {
                if (idx == 0) { // if this is the first point
                    $gCtx.moveTo(midL[0], midL[1]);  // then start here
                }
                $gCtx.quadraticCurveTo(p[0], p[1], midR[0], midR[1]);
            } else {
                jmri.log("ERROR: unexpected LayoutShape point type '" + lspt + "' for " + $widget.ide);
            }
        });   // $pts.each(function(idx, $lsp)

        if (shapeType == "Filled") {
            $gCtx.fill();
        }
        $gCtx.stroke();

        $gCtx.restore();        // restore color and width back to default
    }   // if (len > 0)
}

function $getLayoutShapePoint($lsp) {
    return [Number($lsp.attributes.x.value), Number($lsp.attributes.y.value)];
}
// wrap inValue around between minVal and maxVal
function $wrapValue(inValue, minVal, maxVal) {
    var range = maxVal - minVal;
    return ((inValue % range) + range) % range;
}

function $lerp(value1, value2, amount) {
    return ((1 - amount) * value1) + (amount * value2);
}

function $half(value1, value2) {
    return $lerp(value1, value2, 1 / 2);
}

function $third(value1, value2) {
    return $lerp(value1, value2, 1 / 3);
}

//store the various points defined with a Turnout (pass in widget)
//see jmri.jmrit.display.layoutEditor.LayoutTurnout.java for background
function $storeTurnoutPoints($widget) {
    var $t = [];
    $t['ident'] = $widget.ident + PT_B;  //store B endpoint
    $t['x'] = $widget.xb * 1;
    $t['y'] = $widget.yb * 1;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + PT_C;  //store C endpoint
    $t['x'] = $widget.xc * 1;
    $t['y'] = $widget.yc * 1;
    $gPts[$t.ident] = $t;

    if ($widget.type == LH_TURNOUT || $widget.type == RH_TURNOUT) {
        $t = [];
        $t['ident'] = $widget.ident + PT_A;  //calculate and store A endpoint (mirror of B for these)
        $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
        $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
        $gPts[$t.ident] = $t;
    } else if ($widget.type == WYE_TURNOUT) {
        $t = [];
        $t['ident'] = $widget.ident + PT_A;  //store A endpoint
        $t['x'] = $widget.xa * 1;
        $t['y'] = $widget.ya * 1;
        $gPts[$t.ident] = $t;
    } else if ($widget.type == LH_XOVER || $widget.type == RH_XOVER || $widget.type == DOUBLE_XOVER) {
        $t = [];
        $t['ident'] = $widget.ident + PT_A;  //calculate and store A endpoint (mirror of C for these)
        $t['x'] = $widget.xcen - ($widget.xc - $widget.xcen);
        $t['y'] = $widget.ycen - ($widget.yc - $widget.ycen);
        $gPts[$t.ident] = $t;
        $t = [];
        $t['ident'] = $widget.ident + PT_D;  //calculate and store D endpoint (mirror of B for these)
        $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
        $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
        $gPts[$t.ident] = $t;
    }
}

//store the various points defined with a Slip (pass in widget)
//see jmri.jmrit.display.layoutEditor.LayoutSlip.java for background
function $storeSlipPoints($widget) {
    var $t = [];
    $t['ident'] = $widget.ident + SLIP_A;  //store A endpoint
    $t['x'] = $widget.xa * 1;
    $t['y'] = $widget.ya * 1;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_B;  //store B endpoint
    $t['x'] = $widget.xb * 1;
    $t['y'] = $widget.yb * 1;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_C;  //calculate and store C endpoint (mirror of A for these)
    $t['x'] = $widget.xcen - ($widget.xa - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.ya - $widget.ycen);
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + SLIP_D;  //calculate and store D endpoint (mirror of B for these)
    $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
    $gPts[$t.ident] = $t;
}

//store the various points defined with a LevelXing (pass in widget)
//see jmri.jmrit.display.layoutEditor.LevelXing.java for background
function $storeLevelXingPoints($widget) {
    var $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_A;  //store A endpoint
    $t['x'] = $widget.xa * 1;
    $t['y'] = $widget.ya * 1;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_B;  //store B endpoint
    $t['x'] = $widget.xb * 1;
    $t['y'] = $widget.yb * 1;
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_C;  //calculate and store A endpoint (mirror of A for these)
    $t['x'] = $widget.xcen - ($widget.xa - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.ya - $widget.ycen);
    $gPts[$t.ident] = $t;

    $t = [];
    $t['ident'] = $widget.ident + LEVEL_XING_D;  //calculate and store D endpoint (mirror of B for these)
    $t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
    $t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
    $gPts[$t.ident] = $t;
}

//drawLine, passing in values from xml
function $drawLine($p1x, $p1y, $p2x, $p2y, $color, $width, dashArray) {
    $gCtx.save();   // save current line width and color

    if (typeof $color !== "undefined") {
        $gCtx.strokeStyle = $color;
    }
    if (typeof $width !== "undefined") {
        $gCtx.lineWidth = $width;
    }

    $gCtx.beginPath();

    if (typeof dashArray !== "undefined") {
        $gCtx.setLineDash(dashArray);
    }

    $gCtx.moveTo($p1x, $p1y);
    $gCtx.lineTo($p2x, $p2y);

    $gCtx.stroke();

    if (typeof dashArray !== "undefined") {
        $gCtx.setLineDash([]);
    }

    $gCtx.restore();        // restore color and width back to default
}

function $drawLineP($p1, $p2, $color, $width) {
    $drawLine($p1[0], $p1[1], $p2[0], $p2[1], $color, $width);
}

//drawLine, passing in values from xml
function $drawDashedLine($p1x, $p1y, $p2x, $p2y, $color, $width, dashArray) {
    $drawLine($p1x, $p1y, $p2x, $p2y, $color, $width, dashArray);
}

// function $drawDashedLineP($p1, $p2, $color, $width, dashArray) {
//     $drawDashedLine($p1[0], $p1[1], $p2[0], $p2[1], $color, $width, dashArray);
// }

//draw a Circle (color and width are optional)
function $drawCircleP($p, $radius, $color, $width) {
    $drawCircle($p[0], $p[1], $radius, $color, $width);
}
function $drawCircle($px, $py, $radius, $color, $width) {
    $gCtx.save();   // save current line width and color

    // set color and width
    if (typeof $color !== "undefined") {
        $gCtx.strokeStyle = $color;
    }
    if (typeof $width !== "undefined") {
        $gCtx.lineWidth = $width;
    }

    $gCtx.beginPath();
    $gCtx.arc($px, $py, $radius, 0, 2 * Math.PI, false);
    $gCtx.stroke();

    $gCtx.restore();        // restore color and width back to default
}   // $drawCircle

//draw a Circle (color and width are optional)
function $fillCircleP($p, $radius, $color, $width) {
    $fillCircle($p[0], $p[1], $radius, $color, $width);
}
function $fillCircle($px, $py, $radius, $color, $width) {
    $gCtx.save();   // save current line width and color

    // set color and width
    if (typeof $color !== "undefined") {
        $gCtx.fillStyle = $color;
    }
    if (typeof $width !== "undefined") {
        $gCtx.lineWidth = $width;
    }

    $gCtx.beginPath();
    $gCtx.arc($px, $py, $radius, 0, 2 * Math.PI, false);
    $gCtx.fill();

    $gCtx.restore();        // restore color and width back to default
}   // $fillCircle

//drawArc, passing in values from xml
function $drawArc(pt1x, pt1y, pt2x, pt2y, degrees, $color, $width) {
    // Compute arc's chord
    var a = pt2x - pt1x;
    var o = pt2y - pt1y;
    var chord = Math.hypot(a, o);   //in pixels
    if (chord > 0) {  //don't bother if no length
        $gCtx.save();   // save current line width and color

        // set color and width
        if (typeof $color !== "undefined") {
            $gCtx.strokeStyle = $color;
        }
        if (typeof $width !== "undefined") {
            $gCtx.lineWidth = $width;
        }

        var halfAngleRAD = $toRadians(degrees / 2);
        var radius = (chord / 2) / (Math.sin(halfAngleRAD));  //in pixels
        var startRAD = Math.atan2(a, o) - halfAngleRAD; //in radians

        // calculate center of circle
        var cx = (pt2x * 1.0) - Math.cos(startRAD) * radius;
        var cy = (pt2y * 1.0) + Math.sin(startRAD) * radius;

        //calculate start and end angle
        var startAngleRAD = Math.atan2(pt1y - cy, pt1x - cx); //in radians
        var endAngleRAD = Math.atan2(pt2y - cy, pt2x - cx); //in radians

        $gCtx.beginPath();
        $gCtx.arc(cx, cy, radius, startAngleRAD, endAngleRAD, false);
        $gCtx.stroke();

        $gCtx.restore();        // restore color and width back to default
    }
}   //$drawArc

function $drawArcP(pt1, pt2, degrees) {
    $drawArc(pt1[0], pt1[1], pt2[0], pt2[1], degrees);
}

function $drawEllipse(x, y, rw, rh, startAngleRAD, stopAngleRAD)
{
    $gCtx.beginPath();
    $gCtx.ellipse(x, y, rw, rh, 0, startAngleRAD, stopAngleRAD);
    $gCtx.stroke();
}

//
//  $drawBezier
//
var bezier1st = true;
function $drawBezier(points, $color, $width, displacement = 0) {
    $gCtx.save();   // save current line width and color

    // set color and width
    if (typeof $color !== "undefined") {
        $gCtx.strokeStyle = $color;
    }
    if (typeof $width !== "undefined") {
        $gCtx.lineWidth = $width;
    }

    try {
        bezier1st = true;
        $gCtx.beginPath();
        $plotBezier(points, 0, displacement);
        $gCtx.stroke();
    } catch (e) {
        if (jmri_logging) {
            jmri.log("$plotBezier exception: " + e);
            var vDebug = "";
            for (var prop in e) {
               vDebug += "      ["+ prop+ "]: '"+ e[prop]+ "'\n";
            }
            vDebug += "toString(): " + " value: [" + e.toString() + "]";
            jmri.log(vDebug);
        }
    }

    $gCtx.restore();        // restore color and width back to default
}   // $drawBezier

//
//plotBezier - recursive function to draw bezier curve
//
function $plotBezier(points, depth = 0, displacement = 0) {
    var len = points.length, idx, jdx;

    // calculate flatness to determine if we need to recurse...
    var outer_distance = 0;
    for (var idx = 1; idx < len; idx++) {
        outer_distance += $point_distance(points[idx - 1], points[idx]);
    }
    var inner_distance = $point_distance(points[0], points[len - 1]);
    var flatness = outer_distance / inner_distance;

    // depth prevents stack overflow
    // (I picked 12 because 2^12 = 2048 is larger than most monitors ;-)
    // the flatness comparison value is somewhat arbitrary.
    // (I just kept moving it closer to 1 until I got good results. ;-)
    if ((depth > 12) || (flatness <= 1.001)) {
        var p0 = points[0], pN = points[len - 1];

        var vO = $point_normalizeTo($point_orthogonal($point_subtract(pN, p0)), displacement);
        //$point_log("vO", vO);

        if (bezier1st) {
            var p0P = $point_add(p0, vO);
            //$point_log("p0P", p0P);
            $gCtx.moveTo(p0P[0], p0P[1]);
            bezier1st = false;
        }
        var pNP = $point_add(pN, vO);
        $gCtx.lineTo(pNP[0], pNP[1]);
    } else {
        // calculate (len - 1) order of points
        // (zero'th order are the input points)
        var orderPoints = [];
        for (idx = 0; idx < len - 1; idx++) {
            var nthOrderPoints = [];
            for (jdx = 0; jdx < len - 1 - idx; jdx++) {
                if (idx == 0) {
                    nthOrderPoints.push($point_midpoint(points[jdx], points[jdx + 1]));
                } else {
                    nthOrderPoints.push($point_midpoint(orderPoints[idx - 1][jdx], orderPoints[idx - 1][jdx + 1]));
                }
            }
            orderPoints.push(nthOrderPoints);
        }

        // collect left points
        var leftPoints = [];
        leftPoints.push(points[0]);
        for (idx = 0; idx < len - 1; idx++) {
            leftPoints.push(orderPoints[idx][0]);
        }
        // draw left side Bezier
        $plotBezier(leftPoints, depth + 1, displacement);

        // collect right points
        var rightPoints = [];
        for (idx = 0; idx < len - 1; idx++) {
            rightPoints.push(orderPoints[len - 2 - idx][idx]);
        }
        rightPoints.push(points[len - 1]);
        // draw right side Bezier
        $plotBezier(rightPoints, depth + 1, displacement);
    }
}

function $point_log(prefix, p) {
    jmri.log(prefix + ": {" + p[0] + ", " + p[1] + "}");
}

function $getPoint(name) {
    var point$ = $gPts[name];
    return [Number(point$.x), Number(point$.y)];
}


function $point_length(p) {
    var dx = p[0];
    var dy = p[1];
    return Math.hypot(dx, dy);
}

function $point_add(p1, p2) {
    return [p1[0] + p2[0], p1[1] + p2[1]];
}

function $point_subtract(p1, p2) {
    return [p1[0] - p2[0], p1[1] - p2[1]];
}

function $point_distance(p1, p2) {
    var delta = $point_subtract(p1, p2);
    return Math.hypot(p1, p2);
}

function $point_midpoint(p1, p2) {
    return [$half(p1[0], p2[0]), $half(p1[1], p2[1])];
}

function $point_normalizeTo(p, new_length = 1) {
    var m = new_length / $point_length(p);
    return [p[0] * m, p[1] * m];
}

function $point_orthogonal(p) {
    return [-p[1],p[0]];
}

function $computeAngleRAD(v) {
    return Math.atan2(v[0], v[1]);
}

function $computeAngleRAD2(p1, p2) {
    return $computeAngleRAD($point_subtract(p1, p2));
}

// Converts from degrees to radians.
function $toRadians(degrees) {
    return degrees * Math.PI / 180;
};

// Converts from radians to degrees.
function $toDegrees(radians) {
    return radians * 180 / Math.PI;
};

// rotate a point vector
function $point_rotate(point, angleRAD) {
    var sinA = Math.sin(angleRAD), cosA = Math.cos(angleRAD);
    var x = point[0], y = point[1];
    return [cosA * x - sinA * y, sinA * x + cosA * y];
}

function $point_lerp(p1, p2, amount) {
    return [$lerp(p1[0], p2[0], amount), $lerp(p1[1], p2[1], amount)]
}

function $point_third(p1, p2) {
    return $point_lerp(p1, p2, 1.0/3.0);
}

//set object attributes from xml attributes, returning object
var $getObjFromXML = function(e) {
    var $widget = {};
    $(e.attributes).each(function() {
        $widget[this.name] = this.value;
    });
    return $widget;
};

//build and return CSS array from attributes passed in
var $getTextCSSFromObj = function($widget) {
    var $retCSS = {};
    $retCSS['color'] = '';  //only clear attributes
    $retCSS['background-color'] = '';
    if (typeof $widget.red !== "undefined") {
        $retCSS['color'] = "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ") ";
    }
    //check for new hasBackground element, ignore background colors unless set to yes
    if (typeof $widget.hasBackground !== "undefined" && $widget.hasBackground == "yes") {
        $retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
    }
    if (typeof $widget.hasBackground === "undefined" && $widget.redBack !== "undefined") {
        $retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
    }
    if (typeof $widget.size !== "undefined") {
        $retCSS['font-size'] = $widget.size + "px ";
    }
    if (typeof $widget.fontname !== "undefined") {
        $retCSS['font-family'] = $widget.fontname;
    }
    if (typeof $widget.margin !== "undefined") {
        $retCSS['padding'] = $widget.margin + "px ";
    }
    if (typeof $widget.borderSize !== "undefined") {
        $retCSS['border-width'] = $widget.borderSize + "px ";
    }
    if (typeof $widget.redBorder !== "undefined") {
        $retCSS['border-color'] = "rgb(" + $widget.redBorder + "," + $widget.greenBorder + "," + $widget.blueBorder + ") ";
        $retCSS['border-style'] = 'solid';
    }
    if (typeof $widget.fixedWidth !== "undefined") {
        $retCSS['width'] = $widget.fixedWidth + "px ";
    }
    if (typeof $widget.fixedHeight !== "undefined") {
        $retCSS['height'] = $widget.fixedHeight + "px ";
    }
    if (typeof $widget.justification !== "undefined") {
        if ($widget.justification == "centre") {
            $retCSS['text-align'] = "center";
        } else {
            $retCSS['text-align'] = $widget.justification;
        }
    }
    if (typeof $widget.style !== "undefined") {
        switch ($widget.style) { //set font based on style attrib from xml
            case "1":
                $retCSS['font-weight'] = 'bold';
                break;
            case "2":
                $retCSS['font-style'] = 'italic';
                break;
            case "3":
                $retCSS['font-weight'] = 'bold';
                $retCSS['font-style'] = 'italic';
                break;
        }
    }

    return $retCSS;
};

//get width of an html element by wrapping a copy in a div, then getting width of div
function $getElementWidth($e) {
    o = $e.clone();
    o.wrap('<div></div>').css({'position': 'absolute', 'float': 'left', 'white-space': 'nowrap', 'visibility': 'hidden'}).appendTo($('body'));
    w = o.width();
    o.remove();
    return w;
}

//place widget in correct position, rotation, z-index and scale. (pass in dom element, to simplify calling from e.load())
var $setWidgetPosition = function(e) {

    var $id = e.attr('id');
    var $widget = $gWidgets[$id];  //look up the widget and get its panel properties

    if (typeof $widget !== "undefined" && $widget.widgetType != "beanswitch") {  //don't bother if widget not found or BeanSwitch

        var $height = 0;
        var $width  = 0;
        //use html5 original sizes if available
        if (typeof e[0].naturalHeight !== "undefined") {
            $height = e[0].naturalHeight * $widget.scale;
        } else {
            $height = e.height() * $widget.scale;
        }
        if (typeof e[0].naturalWidth !== "undefined") {
            $width = e[0].naturalWidth * $widget.scale;
        } else {
            $width = e.width() * $widget.scale;
        }
        if ($widget.widgetFamily == "text") {  //special handling to get width of free-floating text
            $width = $getElementWidth(e) * $widget.scale;
        }

        // calculate x and y adjustment needed to keep upper left of bounding box in the same spot
        // adapted to match JMRI's NamedIcon.rotate(). Note: transform-origin set in .css file
        var tx = 0;
        var ty = 0;

        if ($height > 0 && ($widget.degrees !== 0 || $widget.scale != 1)) { // only calc offset if needed

            var $rad = $toRadians($widget.degrees);

            if (0 <= $widget.degrees && $widget.degrees < 90
                    || -360 < $widget.degrees && $widget.degrees <= -270) {
                tx = $height * Math.sin($rad);
                ty = 0.0;
            } else if (90 <= $widget.degrees && $widget.degrees < 180
                    || -270 < $widget.degrees && $widget.degrees <= -180) {
                tx = $height * Math.sin($rad) - $width * Math.cos($rad);
                ty = -$height * Math.cos($rad);
            } else if (180 <= $widget.degrees && $widget.degrees < 270
                    || -180 < $widget.degrees && $widget.degrees <= -90) {
                tx = -$width * Math.cos($rad);
                ty = -$width * Math.sin($rad) - $height * Math.cos($rad);
            } else /* if (270<=$widget.degrees && $widget.degrees<360) */{
                tx = 0.0;
                ty = -$width * Math.sin($rad);
            }
        }
        // position widget to adjusted position, set z-index, then set rotation
        e.css({
            position : 'absolute',
            left : (parseInt($widget.x) + tx) + 'px',
            top : (parseInt($widget.y) + ty) + 'px',
            zIndex : $widget.level
        });
        if ($widget.degrees !== 0) {
            var $rot = "rotate(" + $widget.degrees + "deg)";
            e.css({
                "transform" : $rot
            });
        }
        // set new height and width if scale specified
        if ($widget.scale != 1 && $height > 0) {
            e.css({
                height : $height + 'px',
                width : $width + 'px'
            });
        }
        // if this is an image that's rotated or scaled, set callback to
        // reposition on every icon load, as the icons can be different sizes.
        if (e.is("img") && ($widget.degrees !== 0 || $widget.scale != 1.0)) {
            e.unbind('load');
            e.load(function() {
                $setWidgetPosition($(this));
            });
        }

    }
};

// reDraw an icon-based widget to reflect changes to state or occupancy
var $reDrawIcon = function($widget) {
    // additional naming for indicator*icon widgets to reflect occupancy
    $indicator = ($widget.occupancysensor && $widget.occupancystate == ACTIVE ? "Occupied" : "");
    // set image src to requested state's image, if defined
    if ($widget['icon' + $indicator + ($widget.state + "")]) {
        $('img#' + $widget.id).attr('src', $widget['icon' + $indicator + ($widget.state + "")]);
    } else if ($widget['defaulticon']) {  //if state icon not found, use default icon if provided
        $('img#' + $widget.id).attr('src', $widget['defaulticon']);
    } else {
        jmri.log("ERROR: image not defined for " + $widget.widgetType + " " + $widget.id + ", state=" + $widget.state + ", occ=" + $widget.occupancystate);
    }
};

// set new value for widget, showing proper icon, return widgets changed
var $setWidgetState = function($id, $newState) {
    var $widget = $gWidgets[$id];

    // if undefined widget this must be a slip
    if (typeof $widget === "undefined") {
        // does it have "l" or "r" suffix?
        if ($id.endsWith("l") || $id.endsWith("r")) {   // (yes!)
            //jmri_logging = true;
            if (jmri_logging) {
                jmri.log("\n#### INFO: clicked slip " + $id + " to state " + $newState);
            }

            // remove suffix
            var slipID = $id.slice(0, -1);
            // get the slip widget
            $widget = $gWidgets[slipID];

            // convert current slip state to current turnout states
            var $stateA, $stateB;
            //[$stateA, $stateB] = getTurnoutStatesForSlip($widget);
            //[$stateA, $stateB] = [$widget.turnout.state, $widget.turnoutB.state];
            [$stateA, $stateB] = [$widget.stateA, $widget.stateB];
            $widget.state = getSlipStateForTurnoutStates($widget, $stateA, $stateB);
            if (jmri_logging) {
                jmri.log("#### 3360 Slip " + $widget.name +
                    " before: " + slipStateToString($widget.state) +
                    ", stateA: " + turnoutStateToString($stateA) +
                    ", stateB: " + turnoutStateToString($stateB));
            }

            // change appropriate turnout state
            if ($id.endsWith("r")) {
                if ($stateA != $newState) {
                    if (jmri_logging) {
                        jmri.log("#### 3370 Changed r slip " + $widget.name +
                            " $stateA from " + turnoutStateToString($stateA) +
                            " to " + turnoutStateToString($newState));
                    }
                    $stateA = $newState;
                    $widget.stateA = $stateA;
                }
            } else if ($id.endsWith("l")) {
                if ($stateB != $newState) {
                    if (jmri_logging) {
                        jmri.log("#### 3379 Changed l slip " + $widget.name +
                            " $stateB from " + turnoutStateToString($stateB) +
                            " to " + turnoutStateToString($newState));
                    }
                    $stateB = $newState;
                    $widget.stateB = $stateB;
                }
            }

            // turn turnout states back into slip state
            $newState = getSlipStateForTurnoutStates($widget, $stateA, $stateB);
            if (jmri_logging) {
                jmri.log("#### 3390 Slip " + $widget.name +
                    " after: " + slipStateToString($newState) +
                    ", stateA: " + turnoutStateToString($stateA) +
                    ", stateB: " + turnoutStateToString($stateB));
            }

            if ($widget.state != $newState) {
               if (jmri_logging) {
                   jmri.log("#### 3398 Changing slip " + $widget.name + " from " + slipStateToString($widget.state) +
                       " to " + slipStateToString($newState));
               }
            }
            //jmri_logging = false;

            // set $id to slip id
            $id = slipID;
        } else {
            jmri.log("$setWidgetState unknown $id: '" + $id + "'.");
            return;
        }
    } else if ($widget.widgetType == 'layoutSlip') {
        if (jmri_logging) {
            jmri.log("#### $setWidgetState(slip " + $id + ", " + slipStateToString($newState) +
                "); (was " + slipStateToString($widget.state) + ")");
        }
        // JMRI doesn't send slip states… it sends slip turnout states…
        // so ignore this (incorrect) slip state change
        return;
    }

    if ($widget.state !== $newState) {  //don't bother if already this value
        if (jmri_logging) {
            jmri.log("JMRI changed " + $id + " (" + $widget.jsonType + " " + $widget.name + ") from state '" + $widget.state + "' to '" + $newState + "'.");
        }
        $widget.state = $newState;

        switch ($widget.widgetFamily) {
            case "icon" :
                $reDrawIcon($widget)
                break;
            case "text" :
                if ($widget.jsonType == "memory" || $widget.jsonType == "block" || $widget.jsonType == "reporter" ) {
                    if ($widget.widgetType == "fastclock") {
                        $drawClock($widget);
                    } else {  //set memory/block/reporter text or html to new value from server, clearing "null"
                        if ($newState == null) {
                            $('div#' + $id).text("");
                        } else if ($newState.startsWith("<html>")) {
                            $('div#' + $id).html($newState);
                        } else {
                            $('div#' + $id).text($newState);
                        }
                    }
                } else {
                    if (typeof $widget['text' + $newState] !== "undefined") {
                        $('div#' + $id).text($widget['text' + $newState]);  //set text to new state's text
                    }
                    if (typeof $widget['css' + $newState] !== "undefined") {
                        $('div#' + $id).css($widget['css' + $newState]); //set css to new state's css
                    }
                }
                break;
            case "drawn" :
                if ($widget.widgetType == "layoutturnout") {
                    $drawTurnout($widget);
                } else if ($widget.widgetType == 'layoutSlip') {
                    $drawSlip($widget);
                }
                break;
        }
        $gWidgets[$id].state = $newState;  //update the persistent widget to the new state
//    } else {
//        jmri.log("NOT setting " + $id + " for " + $widget.jsonType + " " + $widget.name + " --> " + $newState);
    }
};

//return a unique ID # when called
var $gUnique = function() {
    if (typeof $gUnique.id === 'undefined') {
        $gUnique.id = 0;
    }
    $gUnique.id++;
    return $gUnique.id;
};

//clean up a name, for example to use as an id
var $safeName = function($name) {
    if (typeof $name === "undefined") {
        return "unique-" + $gUnique();
    } else {
        return $name.replace(/:/g, "_").replace(/ /g, "_").replace(/%20/g, "_");
    }
};

//send request for state change
var sendElementChange = function(type, name, state) {
    //jmri.log("Sending JMRI " + type + " '" + name + "' state '" + state + "'.");
    jmri.setObject(type, name, state);
};

//show unexpected ajax errors
$(document).ajaxError(function(event, xhr, opt, exception) {
    if (xhr.statusText != "abort" && xhr.status != 0) {
        var $msg = "AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText;
        $('div#messageText').text($msg);
        $("#activity-alert").addClass("show").removeClass("hidden");
        $('dvi#workingMessage').position({within: "window"});
        jmri.log($msg);
        return;
    }
    if (xhr.statusText == "timeout") {
        var $msg = "AJAX timeout " + opt.url + ", status= " + xhr.status + " " + xhr.statusText + " resending list....";
        jmri.log($msg);
        // TODO: need to recover somehow
    }
});

//clear out whitespace from xml, function adapted from
//http://stackoverflow.com/questions/1539367/remove-whitespace-and-line-breaks-between-html-elements-using-jquery/3103269#3103269
jQuery.fn.xmlClean = function() {
    this.contents().filter(function() {
        if (this.nodeType != 3) {
            $(this).xmlClean();
            return false;
        }
        else {
            return !/\S/.test(this.nodeValue);
        }
    }).remove();
}
//handle the toggling (or whatever) of the "next" state for the passed-in widget
var $getNextState = function($widget) {
    var $nextState = undefined;
    //$logProperties($widget);
    if ($widget.widgetType == 'signalheadicon') { //special case for signalheadicons
        switch ($widget.clickmode * 1) {          //   logic based on SignalHeadIcon.java
            case 0 :
                switch ($widget.state * 1) {  // (* 1 is to insure numeric comparisons)
                    case RED:
                    case FLASHRED:
                        $nextState = YELLOW;
                        break;
                    case YELLOW:
                    case FLASHYELLOW:
                        $nextState = GREEN;
                        break;
                    default: //also catches GREEN and FLASHGREEN
                        $nextState = RED;
                        break;
                }
            case 1 :
                //TODO: handle lit/unlit toggle
                // getSignalHead().setLit(!getSignalHead().getLit());
                break;
            case 2 :
                // getSignalHead().setHeld(!getSignalHead().getHeld());
                $nextState = ($widget.state * 1 == HELD ? RED : HELD);  //toggle between red and held states
                break;
            case 3: //loop through all elements, finding iconX and get "next one", skipping special ones
                var $firstState = undefined;
                var $currentState = undefined;
                for (k in $widget) {
                    var s = k.substr(4) * 1; //extract the state from current icon var, insure it is treated as numeric
                    //get valid value, name starts with 'icon', but not the HELD or DARK ones
                    if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && k != 'icon' + HELD && k != 'icon' + DARK) {
                        if (typeof $firstState === "undefined")
                            $firstState = s;  //remember the first state (for last one)
                        if (typeof $currentState !== "undefined" && typeof $nextState === "undefined")
                            $nextState = s; //last one was the current, so this one must be next
                        if (s == $widget.state)
                            $currentState = s;
//                      jmri.log('key: '+k+" first="+$firstState+" current="+$currentState+" next="+$nextState);
                    }
                }
                if (typeof $nextState === "undefined")
                    $nextState = $firstState;  //if still not set, start over
        } //end of switch

    } else if ($widget.widgetType == 'signalmasticon') { //special case for signalmasticons
        //loop through all elements, finding iconXXX and get next iconXXX, skipping special ones
        switch ($widget.clickmode * 1) {          //   logic based on SignalMastIcon.java
        case 0 :
            var $firstState = undefined;
            var $currentState = undefined;
            for (k in $widget) {
                var s = k.substr(4); //extract the state from current icon var
                //look for next icon value, skipping Held, Dark and Unknown
                if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && s != 'Held' && s != 'Dark'
              && s !='Unlit' && s !=  'Unknown') {
                    if (typeof $firstState === "undefined")
                        $firstState = s;  //remember the first state (for last one)
                    if (typeof $currentState !== "undefined" && typeof $nextState === "undefined")
                        $nextState = s; //last one was the current, so this one must be next
                    if (s == $widget.state)
                        $currentState = s;
//                  jmri.log('key: ' + k + " first=" + $firstState);
                }
            };
            if (typeof $nextState === "undefined")
                $nextState = $firstState;  //if still not set, start over
            break;

        case 1 :
            //TODO: handle lit/unlit states
            break;

        case 2 :
            //toggle between stop and held state
            $nextState = ($widget.state == "Held" ? "Stop" : "Held");
            break;

        }; //end of switch clickmode
    } else {  //start with INACTIVE, then toggle to ACTIVE and back
        $nextState = ($widget.state == ACTIVE ? INACTIVE : ACTIVE);
    }

    if (typeof $nextState === "undefined")
        $nextState = $widget.state;  //default to no change
    return $nextState;
};

//request the panel xml from the server, and setup callback to process the response
var requestPanelXML = function(panelName) {
    $("#activity-alert").addClass("show").removeClass("hidden");
    $.ajax({
        type: "GET",
        url: "/panel/" + panelName + "?format=xml", //request proper url
        success: function(data, textStatus, jqXHR) {
            processPanelXML(data, textStatus, jqXHR);
        },
        error: function( jqXHR, textStatus, errorThrown) {
            alert("Error retrieving panel xml from server.  Please press OK to retry.\n\nDetails: " + textStatus + " - " + errorThrown);
            window.location = window.location.pathname;
        },
        async: true,
        timeout: 15000, // very long timeout, since this can be a slow process for complicated panels
        dataType: "xml"
    });
};

//preload all images referred to by the widget
var $preloadWidgetImages = function($widget) {
    for (k in $widget) {
        if (k.indexOf('icon') == 0 && typeof $widget[k] !== "undefined" && $widget[k] !== "yes") { //if attribute names starts with 'icon', it's an image, so preload it
            $("<img src='" + $widget[k] + "'/>");
        }
    }
};

//determine widget "family" for broadly grouping behaviors
//note: not-yet-supported widgets are commented out here so as to return undefined
var $getWidgetFamily = function($widget, $element) {

    if (($widget.widgetType == "positionablelabel" || $widget.widgetType == "linkinglabel")
            && typeof $widget.text !== "undefined") {
        return "text";  //special case to distinguish text vs. icon labels
    }
    if ($widget.widgetType == "sensoricon" && $widget.icon == "no") {
        return "text";  //special case to distinguish text vs. icon labels
    }
    if ($widget.widgetType == "memoryicon" && $($element).find('memorystate').length == 0) {
        return "text";  //if no memorystate icons, treat as text
    }
    switch ($widget.widgetType) {
        case "locoicon" :
        case "trainicon" :
        case "memoryComboIcon" :
        case "memoryInputIcon" :
        case "fastclock" :
        case "BlockContentsIcon" :
        case "reportericon" :
        case "beanswitch" :
            return "text";
            break;
        case "positionablelabel" :
        case "linkinglabel" :
        case "turnouticon" :
        case "sensoricon" :
        case "LightIcon" :
        case "multisensoricon" :
        case "signalheadicon" :
        case "signalmasticon" :
        case "indicatortrackicon" :
        case "indicatorturnouticon" :
        case "memoryicon" :
            return "icon";
            break;
        case 'layoutSlip' :
        case "layoutturnout" :
        case "tracksegment" :
        case "positionablepoint" :
        case "backgroundColor" :
        case "layoutblock" :
        case "levelxing" :
        case "layoutturntable" :
        case "layoutShape" :
            return "drawn";
            break;
    }
    jmri.log("unknown widget type of '" + $widget.widgetType +"'");
    return; //unrecognized widget returns undefined
};

//redraw all "drawn" elements for given block (called after color change)
function $redrawBlock(blockName) {
    //jmri.log("redrawing all tracks for block " + blockName);
    //loop thru widgets, if block matches, redraw widget by proper method
    jQuery.each($gWidgets, function($id, $widget) {
        ///jmri.log("  $id: " + $id);
        //$logProperties($widget);
        if (($widget.blockname == blockName)
        || ($widget.blocknameac == blockName)
        || ($widget.blocknamebd == blockName)
        || ($widget.blockbname == blockName)
        || ($widget.blockcname == blockName)
        || ($widget.blockdname == blockName)) {
            switch ($widget.widgetType) {
                case 'layoutturnout' :
                    $drawTurnout($widget);
                    break;
                case 'layoutSlip' :
                    $drawSlip($widget);
                    break;
                case 'tracksegment' :
                    $drawTrackSegment($widget);
                    break;
                case 'levelxing' :
                    $drawLevelXing($widget);
                    break;
            }
        }
        if ($widget.widgetType == 'layoutSlip') {
            if ((isDefined($widget.connectaname) && ($gWidgets[$widget.connectaname].blockname == blockName))
            || isDefined($widget.connectbname) && ($gWidgets[$widget.connectbname].blockname == blockName)
            || isDefined($widget.connectcname) && ($gWidgets[$widget.connectcname].blockname == blockName)
            || isDefined($widget.connectdname) && ($gWidgets[$widget.connectdname].blockname == blockName)){
                $drawSlip($widget);
            }
        }
    });
};

//redraw all "drawn" elements to overcome some bidirectional dependencies in the xml
var $drawAllDrawnWidgets = function() {
    //loop thru widgets, redrawing each visible widget by proper method
    jQuery.each($gWidgets, function($id, $widget) {
        switch ($widget.widgetType) {
            case 'layoutturnout' :
                $drawTurnout($widget);
                break;
            case 'layoutSlip' :
                $drawSlip($widget);
                break;
            case 'tracksegment' :
                $drawTrackSegment($widget);
                break;
            case 'levelxing' :
                $drawLevelXing($widget);
                break;
        }
    });
};

//redraw all "icon" elements.  Called after a delay to allow loading of images.
var $drawAllIconWidgets = function() {
    //loop thru widgets, repositioning each icon widget
    jQuery.each($gWidgets, function($id, $widget) {
        switch ($widget.widgetFamily) {
            case 'icon' :
                $setWidgetPosition($("#panel-area > #" + $widget.id));
                break;
        }
    });
};

function updateWidgets(name, state, data) {
    //update all widgets based on the element that changed, using systemname
    if (whereUsed[name]) {
        if (jmri_logging) jmri.log("updateWidgets(" + name + ", " + state + ", data);");
        $.each(whereUsed[name], function(index, widgetId) {
            $setWidgetState(widgetId, state);
        });
//    } else {
//      jmri.log("system name " + name + " not found, can't set state to " + state);
    }
    //update all widgets based on the element that changed, using username
    if (whereUsed[data.userName]) {
        if (jmri_logging) jmri.log("updateWidgets(" + data.userName + ", " + state + ", data);");
        $.each(whereUsed[data.userName], function(index, widgetId) {
            $setWidgetState(widgetId, state);
        });
//    } else {
//      jmri.log("userName " + name + " not found, can't set state to " + state);
    }
}

function updateOccupancy(sensorName, state, data) {
    //handle occupancy sensors by systemname
    if (occupancyNames[sensorName]) {
        updateOccupancySub(sensorName, state);
    }
    //handle occupancy sensors by username
    if (occupancyNames[data.userName]) {
        updateOccupancySub(data.userName, state);
    }
}

function updateOccupancySub(sensorName, state) {
    if (occupancyNames[sensorName]) {
        //jmri.log("setting occupancies for sensor " + sensorName + " to " + state);
        $.each(occupancyNames[sensorName], function(index, widgetId) {
            $widget = $gWidgets[widgetId];

            updateBlockSensorState($widget.blockname, sensorName, state);
            updateBlockSensorState($widget.blocknameac, sensorName, state);
            updateBlockSensorState($widget.blocknamebd, sensorName, state);
            updateBlockSensorState($widget.blockbname, sensorName, state);
            updateBlockSensorState($widget.blockcname, sensorName, state);
            updateBlockSensorState($widget.blockdname, sensorName, state);

            $widget.occupancystate = state; //set occupancy for the widget to the newstate

            switch ($widget.widgetType) {
                case 'indicatortrackicon' :
                case 'indicatorturnouticon' :
                    $reDrawIcon($widget);
                    break;
                case 'layoutturnout' :
                    $drawTurnout($widget);
                    break;
                case 'layoutSlip' :
                    $drawSlip($widget);
                    break;
            }
        });
    }
}

function updateBlockSensorState(blockName, sensorName, sensorState) {
    if (isDefined(blockName)) {
        var $blk = $gBlks[blockName];
        if (isDefined($blk)) {
//             jmri.log("blockName: " + blockName
//                 + ", sensorName: " + sensorName
//                 + ", sensorState: " + sensorState);
//             $logProperties($blk, true);
            if (isDefined($blk.occupancysensor)
                && ($blk.occupancysensor == sensorName)) {
                $blk.state = sensorState;
            }
        }
    }
}

function setBlockColor(blockName, newColor) {
    //jmri.log("setBlockColor(" + blockName + ", " + newColor + ");");
    var $blk = $gBlks[blockName];
    if (isDefined($blk)) {
        $gBlks[blockName].blockcolor = newColor;
    } else {
        jmri.log("ERROR: block " + blockName + " not found for color " + newColor);
    }
    $redrawBlock(blockName);
}

function listPanels(name) {
    $.ajax({
        url: "/panel/?format=json",
        data: {},
        success: function(data, textStatus, jqXHR) {
            if (data.length !== 0) {
                $.each(data, function(index, value) {
                    $gPanelList[value.data.userName] = value.data.name;
                });
            }
            if (name === null || typeof (panelName) === undefined) {
                if (data.length !== 0) {
                    $("#panel-list").empty();
                    $("#activity-alert").addClass("hidden").removeClass("show");
                    $("#panel-list").addClass("show").removeClass("hidden");
                    $.each(data, function(index, value) {
                        $("#panel-list").append("<div class=\"col-sm-6 col-md-4 col-lg-3\"><div class=\"thumbnail\"><a href=\"/panel/" + value.data.name + "\"><div class=\"thumbnail-image\"><img src=\"/panel/" + value.data.name + "?format=png\" style=\"width: 100%;\"></div><div class=\"caption\">" + value.data.userName + "</div></a></div></div>");
                        // (12 / col-lg-#) % index + 1
                        if (4 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-lg\"></div>");
                        }
                        // (12 / col-md-#) % index + 1
                        if (3 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-md\"></div>");
                        }
                        // (12 / col-sm-#) % index + 1
                        if (2 % (index + 1)) {
                            $("#panel-list").append("<div class=\"clearfix visible-sm\"></div>");
                        }
                    });
                    // resizeThumbnails(); // sometimes gets .thumbnail sizes too small under image. Why?
                } else {
                    $("#activity-alert").addClass("hidden").removeClass("show");
                    $("#warning-no-panels").addClass("show").removeClass("hidden");
                }
            }
        }
    });
}

function resizeThumbnails() {
    tallest = 0;
    $(".thumbnail-image").each(function() {
        thisHeight = $("img", this).height();
        if (thisHeight > tallest) {
            tallest = thisHeight;
        }
    });
    $(".thumbnail-image").each(function() {
        $(this).height(tallest);
    });
}

$(window).resize(function() {
    resizeThumbnails();
});

//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
    // get panel name if passed as a parameter
    var panelName = getParameterByName("name");
    // get panel name if part of the path
    if (panelName === null || typeof (panelName) === undefined) {
        var path = $(location).attr('pathname');
        path = path.split("/");
        if (path.length > 3) {
            panelName = path[path.length - 2] + "/" + path[path.length - 1];
        }
    }
    // setup the functional menu items
    $("#navbar-panel-reload > a").attr("href", location.href);
    $("#navbar-panel-xml > a").attr("href", location.href + "?format=xml");
    // show panel thumbnails if no panel name
    listPanels(panelName);
    if (panelName === null || typeof (panelName) === undefined) {
        $("#panel-list").addClass("show").removeClass("hidden");
        $("#panel-area").addClass("hidden").removeClass("show");
        // hide the Show XML menu when listing panels
        $("#navbar-panel-xml").addClass("hidden").removeClass("show");
    } else {
        jmri = $.JMRI({
            didReconnect: function() {
                // if a reconnect is triggered, reload the page - it is the
                // simplest method to refresh every object in the panel
                jmri.log("Reloading at reconnect");
                location.reload(false);
            },
            light: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            block: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            layoutBlock: function(name, value, data) {
                setBlockColor(name, data.blockColor);
            },
            memory: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            reporter: function(name, value, data) {
                updateWidgets(name, value, data);
            },
            route: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            sensor: function(name, state, data) {
                updateOccupancy(name, state, data);
                updateWidgets(name, state, data);
            },
            signalHead: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            signalMast: function(name, state, data) {
                updateWidgets(name, state, data);
            },
            turnout: function(name, state, data) {
                updateWidgets(name, state, data);
            }
        });
        $("#panel-list").addClass("hidden").removeClass("show");
        $("#panel-area").addClass("show").removeClass("hidden");

        // include name of panel in page title. Will be updated to userName later
        setTitle(panelName);

        // Add a widget to retrieve fastclock rate
        $widget = new Array();
        $widget.jsonType = "memory";
        $widget['name'] = "IMRATEFACTOR";  // already defined in JMRI
        $widget['id'] = $widget['name'];
        $widget['safeName'] = $widget['name'];
        $widget['systemName'] = $widget['name'];
        $widget['state'] = "1.0";
        $widget['id'] = $widget.ident;
        $gWidgets[$widget.id] = $widget;
        if (!($widget.systemName in whereUsed)) {  //set where-used for this new memory
            whereUsed[$widget.systemName] = new Array();
        }
        whereUsed[$widget.systemName][whereUsed[$widget.systemName].length] = $widget.id;

        // request actual xml of panel, and process it on return
        // uses setTimeout simply to not block other JavaScript since
        // requestPanelXML has a long timeout
        setTimeout(function() {
            requestPanelXML(panelName);
        }, 500);
    }
});

// convert turnout state to string
function turnoutStateToString(state) {
    result = "UKNONWN"
    switch (state) {
        case 2:
            result = "CLOSED";
            break;
        case 4:
            result = "THROWN";
            break;
        case 8:
            result = "INCONSISTENT";
            break;
    }
    return result;
}

// convert slip state to string
function slipStateToString(state) {
    result = "UNKNOWN";
    switch (state) {
        case STATE_AC:
            result = "STATE_AC";
            break;
        case STATE_AD:
            result = "STATE_AD";
            break;
        case STATE_BC:
            result = "STATE_BC";
            break;
        case STATE_BD:
            result = "STATE_BD";
            break;
    }
    return result;
}

function getTurnoutStatesForSlipState(slipWidget, slipState) {
    var results = [UNKNOWN, UNKNOWN];
    if (typeof slipWidget !== "undefined") {
        if (slipWidget.widgetType == 'layoutSlip') {
            switch (slipState) {
                case STATE_AC:
                    results = [slipWidget.turnoutA_AC, slipWidget.turnoutB_AC];
                    break;
                case STATE_AD:
                    results = [slipWidget.turnoutA_AD, slipWidget.turnoutB_AD];
                    break;
                case STATE_BC:
                    results = [slipWidget.turnoutA_BC, slipWidget.turnoutB_BC];
                    break;
                case STATE_BD:
                    results = [slipWidget.turnoutA_BD, slipWidget.turnoutB_BD];
                    break;
            }
        }
    }
    return results;
}   // function getTurnoutStatesForSlipState

function getTurnoutStatesForSlip(slipWidget) {
    return getTurnoutStatesForSlipState(slipWidget, slipWidget.state);
}

function getSlipStateForTurnoutStatesClosest(slipWidget, stateA, stateB, useClosest) {
    var result = UNKNOWN;
    if ((stateA == slipWidget.turnoutA_AC) && (stateB == slipWidget.turnoutB_AC)) {
        result = STATE_AC;
    } else if ((stateA == slipWidget.turnoutA_AD) && (stateB == slipWidget.turnoutB_AD)) {
        result = STATE_AD;
    } else if (($widget.slipType == DOUBLE_SLIP)
        && (stateA == slipWidget.turnoutA_BC) && (stateB == slipWidget.turnoutB_BC)) {
        result = STATE_BC;
    } else if ((stateA == slipWidget.turnoutA_BD) && (stateB == slipWidget.turnoutB_BD)) {
        result = STATE_BD;
    } else if (useClosest) {
        if ((stateA == slipWidget.turnoutA_AC) || (stateB == slipWidget.turnoutB_AC)) {
            result = STATE_AC;
        } else if ((stateA == slipWidget.turnoutA_AD) || (stateB == slipWidget.turnoutB_AD)) {
            result = STATE_AD;
        } else if (($widget.slipType == DOUBLE_SLIP)
            && (stateA == slipWidget.turnoutA_BC) || (stateB == slipWidget.turnoutB_BC)) {
            result = STATE_BC;
        } else if ((stateA == slipWidget.turnoutA_BD) || (stateB == slipWidget.turnoutB_BD)) {
            result = STATE_BD;
        } else {
            result = STATE_AD;
        }
    }
    return result;
}

function getSlipStateForTurnoutStates(slipWidget, stateA, stateB) {
    return getSlipStateForTurnoutStatesClosest(slipWidget, stateA, stateB, false)
}

//slip A==-==D
//      \\ //
//        X
//      // \\
//     B==-==C
// var STATE_AC = 0x02;
// var STATE_BD = 0x04;
// var STATE_AD = 0x06;
// var STATE_BC = 0x08;
// var CLOSED = '2';
// var THROWN = '4';

function getNextSlipState(slipWidget) {
    var result = UNKNOWN;

    // jmri.log("****************************");
    // jmri.log("slipWidget.side:" + slipWidget.side);
    // jmri.log("  slipWidget.state:" + slipWidget.state);
    switch (slipWidget.side) {
        case 'left': {
            switch (slipWidget.state) {
                case STATE_AC:
                    if (slipWidget.slipType == SINGLE_SLIP) {
                        result = STATE_BD;
                    } else {
                        result = STATE_BC;
                    }
                    break;
                case STATE_AD:
                    result = STATE_BD;
                    break;
                case STATE_BC:
                default:
                    result = STATE_AC;
                    break;
                case STATE_BD:
                    result = STATE_AD;
                    break;
            }
            break;
        }
        case 'right': {
            switch (slipWidget.state) {
                case STATE_AC:
                    result = STATE_AD;
                    break;
                case STATE_AD:
                    result = STATE_AC;
                    break;
                case STATE_BC:
                default:
                    result = STATE_BD;
                    break;
                case STATE_BD:
                    if (slipWidget.slipType == SINGLE_SLIP) {
                        result = STATE_AC;
                    } else {
                        result = STATE_BC;
                    }
                    break;
            }
            break;
        }
        default: {
            jmri.log("getNextSlipState($widget): unknown $widget.side: " + slipWidget.side);
            break;
        }
    }   // switch (slipWidget.side)
    //jmri.log("  result:" + result);
    return result;
}   // function getNextSlipState(slipWidget)
