<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert the Logix in a JMRI panel file to code -->

<!-- Used by default when the panel file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007-2011.                       -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='layout-config'>

<html>
    <head>
        <title>JMRI panel file</title>
    </head>
    
    <body>
        <h2>JMRI panel file</h2>

                <xsl:apply-templates/>

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; 1997 - 2012 JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

    </body>
</html>

</xsl:template>

<!-- Index through routes elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/routes">
<h3>Routes</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal route elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through logixs elements -->
<!-- each one becomes a separate section -->
<xsl:template match="layout-config/logixs/logix">
<h3>Logix <xsl:value-of select="@systemName"/>
<xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
</h3>
    <!-- index through individual logix elements -->
        <xsl:call-template name="oneLogix"/>
</xsl:template>

<!-- Index through blocks (SSL) elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/blocks">
<h3>Blocks</h3>
    <table border="1">
    <tr><td>Controlled Signal</td><td>Watched Turnout</td></tr>
    <!-- index through individal turnout elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<xsl:template match="route">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="logix">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="block">
<tr><td><xsl:value-of select="@signal"/></td><td><xsl:value-of select="@watchedturnout"/></td></tr>
</xsl:template>

<!-- conditionals are not directly displayed -->
<xsl:template match="conditional">
</xsl:template>

<!-- template to show a particular logix -->
<xsl:template name="oneLogix">
    <!-- index is at the logix element here -->
    <xsl:for-each select="logixConditional">
        <xsl:call-template name="oneConditional">
                <xsl:with-param name="name" select="@systemName"/>
        </xsl:call-template>
    </xsl:for-each>
</xsl:template>

<!-- template to show a particular conditional -->
<xsl:template name="oneConditional">
        <xsl:param name="name"/>
    <!-- index through individual conditional elements, looking for match -->
    <xsl:for-each select="/layout-config/conditionals/conditional">
        <xsl:if test="( @systemName = $name )" >
        <!-- here have found correct conditional -->
            <h4>Conditional <xsl:value-of select="@systemName"/>
            <xsl:if test="string-length(@userName)!=0" > (<xsl:value-of select="@userName"/>)</xsl:if>
            </h4>

            <pre>
            {
                Conditional c = ???;
                int nCode = ???;
                int[] opern;
                int[] type;
                String[] name;
                String[] data;
                int[] num1;
                int[] num2;
                boolean[] triggersCalc;
                int numVariables;
                int i = 0;<br/>
            <xsl:for-each select="conditionalStateVariable">
                <xsl:call-template name="conditionalStateVariable"/>
                i++;
                
            </xsl:for-each>
            <xsl:for-each select="conditionalAction">
                <xsl:call-template name="conditionalAction"/>
            </xsl:for-each>
                
                int numVariables = i;<br/>
                c.setStateVariables(opern, type, name, data, 
                    num1, num2, triggersCalc, numVariables); 
            }
            </pre>
        </xsl:if>   
    </xsl:for-each>
</xsl:template>

<xsl:template name="conditionalStateVariable">
<!-- decode operator -->


<xsl:choose>
  <xsl:when test="( @operator = 1 )" >        opern[i] = Conditional.OPERATOR_AND;
</xsl:when>
  <xsl:when test="( @operator = 2 )" >        opern[i] = Conditional.OPERATOR_NOT;
</xsl:when>
  <xsl:when test="( @operator = 3 )" >        opern[i] = Conditional.OPERATOR_AND_NOT;
</xsl:when>
  <xsl:when test="( @operator = 4 )" >        opern[i] = Conditional.OPERATOR_NONE;
</xsl:when>  <!-- None -->
  <xsl:otherwise>        opern[i] = (operator="<xsl:value-of select="@operator"/>");
</xsl:otherwise>
</xsl:choose>
<!-- decode type -->
<xsl:choose>
  <xsl:when test="( @type = 1 )" >        type[i] = Conditional.TYPE_SENSOR_ACTIVE;
    </xsl:when>
  <xsl:when test="( @type = 2 )" >        type[i] = Conditional.TYPE_SENSOR_INACTIVE;
    </xsl:when>
  <xsl:when test="( @type = 3 )" >        type[i] = Conditional.TYPE_TURNOUT_THROWN;
    </xsl:when>
  <xsl:when test="( @type = 4 )" >        type[i] = Conditional.TYPE_TURNOUT_CLOSED;
    </xsl:when>
  <xsl:when test="( @type = 5 )" >        type[i] = Conditional.TYPE_CONDITIONAL_TRUE;
    </xsl:when>
  <xsl:when test="( @type = 6 )" >        type[i] = Conditional.TYPE_CONDITIONAL_FALSE;
    </xsl:when>
  <xsl:when test="( @type = 7 )" >        type[i] = Conditional.TYPE_LIGHT_ON;
    </xsl:when>
  <xsl:when test="( @type = 8 )" >        type[i] = Conditional.TYPE_LIGHT_OFF;
    </xsl:when>
  <xsl:when test="( @type = 9 )" >        type[i] = Conditional.TYPE_MEMORY_EQUALS;
    </xsl:when>
  <xsl:when test="( @type = 10 )" >        type[i] = Conditional.TYPE_FAST_CLOCK_RANGE;
    </xsl:when>
  <xsl:when test="( @type = 11 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_RED;
    </xsl:when>
  <xsl:when test="( @type = 12 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_YELLOW;
    </xsl:when>
  <xsl:when test="( @type = 13 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_GREEN;
    </xsl:when>
  <xsl:when test="( @type = 14 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_DARK;
    </xsl:when>
  <xsl:when test="( @type = 15 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_FLASHRED;
    </xsl:when>
  <xsl:when test="( @type = 16 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_FLASHYELLOW;
    </xsl:when>
  <xsl:when test="( @type = 17 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_FLASHGREEN;
    </xsl:when>
  <xsl:when test="( @type = 18 )" >        type[i] = Conditional.TYPE_SIGNAL_HEAD_LIT;
    </xsl:when>
  <xsl:when test="( @type = 19 )" >        ype[i] = Conditional.TYPE_SIGNAL_HEAD_HELD;
    </xsl:when>
  <xsl:otherwise>        type[i] = <xsl:value-of select="@type"/>
    </xsl:otherwise>
</xsl:choose>
name[i] = "<xsl:value-of select="@systemName"/>";
num1[i] = <xsl:value-of select="@num1"/>;
num2[i] = <xsl:value-of select="@num2"/>;
triggerCalc[i] = <xsl:value-of select="@triggerCalc"/>;
<br/>
</xsl:template>

<xsl:template name="conditionalAction">
<p/>
<!-- decode operator -->
<xsl:choose>
  <xsl:when test="( @option = 1 )" >On change to true: </xsl:when>
  <xsl:when test="( @option = 2 )" >On change to false: </xsl:when>
  <xsl:when test="( @option = 3 )" >On change: </xsl:when>
  <xsl:otherwise>(option="<xsl:value-of select="@option"/>") </xsl:otherwise>
</xsl:choose>
<!-- decode type -->
<xsl:choose>
  <xsl:when test="( @type = 1 )" >(none) </xsl:when>
  <xsl:when test="( @type = 2 )" >Set Turnout </xsl:when>
  <xsl:when test="( @type = 3 )" >Set Signal Appearance </xsl:when>
  <xsl:when test="( @type = 4 )" >Set Signal Held </xsl:when>
  <xsl:when test="( @type = 5 )" >Clear Signal Held </xsl:when>
  <xsl:when test="( @type = 6 )" >Set Signal Dark </xsl:when>
  <xsl:when test="( @type = 7 )" >Set Signal Lit </xsl:when>
  <xsl:when test="( @type = 8 )" >Trigger Route </xsl:when>
  <xsl:when test="( @type = 9 )" >Set Sensor </xsl:when>
  <xsl:when test="( @type = 10 )" >Delayed Set Sensor </xsl:when>
  <xsl:when test="( @type = 11 )" >Set Light </xsl:when>
  <xsl:when test="( @type = 12 )" >Set Memory </xsl:when>
  <xsl:when test="( @type = 13 )" >Enable Logix </xsl:when>
  <xsl:when test="( @type = 14 )" >Disable Logix </xsl:when>
  <xsl:when test="( @type = 15 )" >Play Sound File </xsl:when>
  <xsl:when test="( @type = 16 )" >Run Script </xsl:when>
  <xsl:when test="( @type = 17 )" >Delayed Set Turnout </xsl:when>
  <xsl:when test="( @type = 18 )" >Lock Turnout </xsl:when>
  <xsl:otherwise>(type="<xsl:value-of select="@type"/>") </xsl:otherwise>
</xsl:choose>
delay="<xsl:value-of select="@delay"/>"
systemName="<xsl:value-of select="@systemName"/>"
data="<xsl:value-of select="@data"/>"
string="<xsl:value-of select="@string"/>"
<br/>
</xsl:template>

<xsl:template match="paneleditor">
<h3>Panel: <xsl:value-of select="@name"/></h3>

    <!-- index through individal panel elements -->
    <xsl:apply-templates/>

</xsl:template>

<xsl:template match="signalheadicon">
Signalhead Icon <xsl:value-of select="@signalhead"/><br/>
</xsl:template>

<xsl:template match="turnouticon">
Turnout Icon <xsl:value-of select="@turnout"/><br/>
</xsl:template>

<xsl:template match="sensoricon">
Sensor Icon <xsl:value-of select="@sensor"/><br/>
</xsl:template>

<xsl:template match="positionablelabel">
Positionable Label <xsl:value-of select="@icon"/><br/>
</xsl:template>

</xsl:stylesheet>
