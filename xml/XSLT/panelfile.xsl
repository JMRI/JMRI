<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: panelfile.xsl,v 1.9 2008-05-04 22:47:57 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI panel file into an HTML page -->

<!-- Used by default when the panel file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007.                            -->
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
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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

	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>

<!-- Index through turnouts elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/turnouts">
<h3>Turnouts</h3>
    <table border="1">
    <tr>
      <th>System Name</th>
      <th>User Name</th>
      <th>Feedback</th>
      <th>Invert?</th>
      <th>Locked?</th>
      <th>Automate?</th>
    </tr>
    <!-- index through individual turnout elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<xsl:template match="turnout">
<tr> 
  <td><xsl:value-of select="@systemName"/></td>
  <td><xsl:value-of select="@userName"/></td>
  <td> <xsl:value-of select="@feedback"/> </td>
  <td><xsl:if test="( @inverted = 'true' )" >Yes</xsl:if></td>
  <td><xsl:if test="( @locked = 'true' )" >Yes</xsl:if></td>
  <td><xsl:if test="( @automate != 'Default' )" >Yes</xsl:if></td>
</tr>
</xsl:template>

<!-- Index through signalheads elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/signalheads">
<h3>Signal Heads</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal signalhead elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through sensors elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/sensors">
<h3>Sensors</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal sensor elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through memories elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/memories">
<h3>Memories</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal memory elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<!-- Index through reporters elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/reporters">
<h3>Reporters</h3>
    <table border="1">
    <tr><td>System Name</td><td>User Name</td></tr>
    <!-- index through individal reporter elements -->
    <xsl:apply-templates/>
    </table>
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

<xsl:template match="layout-config/blocks">
    <xsl:if test="@class = 'jmri.jmrit.blockboss.configurexml.BlockBossLogicXml'" >
        <!-- Index through SSL elements -->
        <!-- each one becomes a table -->
        <h3>Simple Signal Logic</h3>
            <table border="1">
            <tr><td>Controlled Signal</td><td>Watched Signal</td></tr>
            <!-- index through individal block elements -->
            <xsl:for-each select="block">
                <tr>
                    <td><xsl:value-of select="@signal"/></td>
                    <td><xsl:value-of select="@watchedsignal1"/></td>
                </tr>
            </xsl:for-each>
            </table>
    </xsl:if>
    
    <xsl:if test="@class = 'jmri.configurexml.BlockManagerXml'" >
        <!-- Index through blocks elements -->
        <!-- each one becomes a table -->
        <h3>Blocks</h3>
            <table border="1">
            <tr>
                <td>System Name</td>
                <td>User Name</td>
                <td>Sensor</td>
                <td>Paths</td>
            </tr>
            <!-- index through individal block elements -->
            <xsl:for-each select="block">
                <tr><xsl:element name="a"><xsl:attribute name="id">Block-<xsl:value-of select="@systemName"/></xsl:attribute></xsl:element>
                    <td><xsl:value-of select="@systemName"/></td>
                    <td><xsl:value-of select="@userName"/></td>
                    <td><xsl:for-each select="sensor">
                        <xsl:value-of select="@systemName"/><br/>
                        </xsl:for-each></td>
                    <td><table><xsl:for-each select="path">
                        <tr>
                            <td><xsl:choose>
                                <xsl:when test="( @todir = 16 )" >North</xsl:when>
                                <xsl:when test="( @todir = 32 )" >South</xsl:when>
                                <xsl:when test="( @todir = 64 )" >East</xsl:when>
                                <xsl:when test="( @todir = 128 )" >West</xsl:when>
                                <xsl:when test="( @todir = 256 )" >CW</xsl:when>
                                <xsl:when test="( @todir = 512 )" >CCW</xsl:when>
                                <xsl:when test="( @todir = 1024 )" >Left</xsl:when>
                                <xsl:when test="( @todir = 2048 )" >Right</xsl:when>
                                <xsl:when test="( @todir = 4096 )" >Up</xsl:when>
                                <xsl:when test="( @todir = 8192 )" >Down</xsl:when>
                                <xsl:otherwise><xsl:value-of select="@todir"/></xsl:otherwise>
                                </xsl:choose></td>
                            <td>to 
                                <xsl:element name="a"><xsl:attribute name="href">#Block-<xsl:value-of select="@block"/></xsl:attribute><xsl:value-of select="@block"/></xsl:element>,
                                </td>
                            <td><xsl:choose>
                                <xsl:when test="( @fromdir = 16 )" >North</xsl:when>
                                <xsl:when test="( @fromdir = 32 )" >South</xsl:when>
                                <xsl:when test="( @fromdir = 64 )" >East</xsl:when>
                                <xsl:when test="( @fromdir = 128 )" >West</xsl:when>
                                <xsl:when test="( @fromdir = 256 )" >CW</xsl:when>
                                <xsl:when test="( @fromdir = 512 )" >CCW</xsl:when>
                                <xsl:when test="( @fromdir = 1024 )" >Left</xsl:when>
                                <xsl:when test="( @fromdir = 2048 )" >Right</xsl:when>
                                <xsl:when test="( @fromdir = 4096 )" >Up</xsl:when>
                                <xsl:when test="( @fromdir = 8192 )" >Down</xsl:when>
                                <xsl:otherwise><xsl:value-of select="@fromdir"/></xsl:otherwise>
                                </xsl:choose></td>
                            <td>from</td>
                            <xsl:for-each select="beansetting">
                                <td>; when <xsl:value-of select="turnout/@systemName"/>
                                <xsl:choose>
                                <xsl:when test="( @setting = 2 )" > is Closed</xsl:when>
                                <xsl:when test="( @setting = 4 )" > is Thrown</xsl:when>
                                <xsl:otherwise> is <xsl:value-of select="@setting"/></xsl:otherwise>
                                </xsl:choose>
                                </td>
                                </xsl:for-each>
                        </tr>
                        </xsl:for-each></table></td>
                </tr>
            </xsl:for-each>
            </table>
    </xsl:if>
</xsl:template>


<!-- Index through layoutblock elements -->
<!-- each one becomes a table -->
<xsl:template match="layout-config/layoutblocks">
<h3>Layout Blocks</h3>
    <table border="1">
    <tr>
        <td>System Name</td>
        <td>User Name</td>
        <td>Occupancy Sensor</td>
        <td>Memory</td>
    </tr>
    <!-- index through individal turnout elements -->
    <xsl:apply-templates/>
    </table>
</xsl:template>

<xsl:template match="layoutblock">
<tr>
    <td><xsl:value-of select="@systemName"/></td>
    <td><xsl:value-of select="@userName"/></td>
    <td><xsl:value-of select="@occupancysensor"/></td>
    <td><xsl:value-of select="@memory"/></td>
</tr>
</xsl:template>

<xsl:template match="signalhead">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="sensor">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="memory">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
</xsl:template>

<xsl:template match="reporter">
<tr><td><xsl:value-of select="@systemName"/></td><td><xsl:value-of select="@userName"/></td></tr>
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
		    <xsl:for-each select="conditionalStateVariable">
		        <xsl:call-template name="conditionalStateVariable"/>
		    </xsl:for-each>
		    <xsl:for-each select="conditionalAction">
		        <xsl:call-template name="conditionalAction"/>
		    </xsl:for-each>
        </xsl:if>        
    </xsl:for-each>
</xsl:template>

<xsl:template name="conditionalStateVariable">
<!-- decode operator -->
<xsl:choose>
  <xsl:when test="( @operator = 1 )" >And </xsl:when>
  <xsl:when test="( @operator = 2 )" >Not </xsl:when>
  <xsl:when test="( @operator = 3 )" >And not </xsl:when>
  <xsl:when test="( @operator = 4 )" ></xsl:when>  <!-- None -->
  <xsl:otherwise>(operator="<xsl:value-of select="@operator"/>") </xsl:otherwise>
</xsl:choose>
<!-- decode type -->
<xsl:choose>
  <xsl:when test="( @type = 1 )" >Sensor Active</xsl:when>
  <xsl:when test="( @type = 2 )" >Sensor Inactive</xsl:when>
  <xsl:when test="( @type = 3 )" >Turnout Thrown</xsl:when>
  <xsl:when test="( @type = 4 )" >Turnout Closed</xsl:when>
  <xsl:when test="( @type = 5 )" >Conditional True</xsl:when>
  <xsl:when test="( @type = 6 )" >Conditional False</xsl:when>
  <xsl:when test="( @type = 7 )" >Light On</xsl:when>
  <xsl:when test="( @type = 8 )" >Light Off</xsl:when>
  <xsl:when test="( @type = 9 )" >Memory Equal</xsl:when>
  <xsl:when test="( @type = 10 )" >Fast Clock Range"</xsl:when>
  <xsl:when test="( @type = 11 )" >Signal Red</xsl:when>
  <xsl:when test="( @type = 12 )" >Signal Yellow</xsl:when>
  <xsl:when test="( @type = 13 )" >Signal Green</xsl:when>
  <xsl:when test="( @type = 14 )" >SIgnal Dark</xsl:when>
  <xsl:when test="( @type = 15 )" >Signal Flashing Red</xsl:when>
  <xsl:when test="( @type = 16 )" >Signal Flashing Yellow</xsl:when>
  <xsl:when test="( @type = 17 )" >Signal Flashing Green</xsl:when>
  <xsl:when test="( @type = 18 )" >Signal Head Lit</xsl:when>
  <xsl:when test="( @type = 19 )" >Signal Head Held</xsl:when>
  <xsl:otherwise>(type="<xsl:value-of select="@type"/>")</xsl:otherwise>
</xsl:choose>
systemName="<xsl:value-of select="@systemName"/>"
num1="<xsl:value-of select="@num1"/>"
num2="<xsl:value-of select="@num2"/>"
triggerCalc="<xsl:value-of select="@triggerCalc"/>"
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
