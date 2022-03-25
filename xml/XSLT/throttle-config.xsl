<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Stylesheet to convert a JMRI throttle XML file into displayable HTML -->
<!-- Used by default when the throttle file is displayed in a web browser-->
<!-- This is just a basic implementation for debugging purposes, without -->
<!-- any real attempt at formatting -->
<!-- This file is part of JMRI.  Copyright 2007-2022.                       -->
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

  <!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->

  <xsl:param name="JmriCopyrightYear" select="concat('1997','-','2022')"/>

  <!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We can also pick some stuff out explicitly in the head section using
     value-of instructions.
-->

  <xsl:template match='throttle-config'>
    <html>
      <head>
        <title>JMRI Throttle File</title>
      </head>
      <body>
        <h2>JMRI Throttle File
        <!-- For when we move to XSL 2.0: <xsl:value-of select="static-base-uri()" />-->
        </h2>
        <xsl:apply-templates/>
        <br/>
        <hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear"/> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/>
        <a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
      </body>
    </html>
  </xsl:template>

  <!-- Title each throttle frames -->
  <xsl:template match="throttle-config/ThrottleFrame">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Display position-->
  <xsl:template name="window">
    <xsl:if test='window/@isVisible = "true"'>(Visible)</xsl:if>
    <xsl:if test='window/@isIconified = "true"'>(Iconified)</xsl:if>
    [XYPosition:  (<xsl:value-of select="window/@x"/>,<xsl:value-of select="window/@y"/>)
     Height/Width:  (<xsl:value-of select="window/@height"/>/<xsl:value-of select="window/@width"/>)]
  </xsl:template>

  <!-- Display control panel subwindow -->
  <xsl:template match="ControlPanel">
    <h4>Control Panel   <xsl:call-template name="window"/></h4>
    <xsl:apply-templates/>
    <table border="border-width:thin" width="50%">
      <tr><th>Attribute</th><th>Value</th></tr>      
      <xsl:for-each select="@*"> 
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td>
          <xsl:choose>
            <xsl:when test='name() ="displaySpeedSlider"'>              
              <xsl:choose>
                <xsl:when test='. = 0'>0 (Percentage)</xsl:when>
                <xsl:when test='. = 1'>1 (Speed Steps)</xsl:when>
                <xsl:when test='. = 2'>2 (Shunting)</xsl:when>
                <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <xsl:otherwise>        
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
          </td>
        </tr>
       </xsl:for-each>
    </table>
  </xsl:template>


  <!-- Display function panel subwindow -->
  <xsl:template match="FunctionPanel">
    <h4>Function Panel  <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="100%">
      <tr>
        <th>Button ID</th>
        <th>Text</th>
        <th>Lockable</th>
        <th>Visible</th>
        <th>fontSize</th>
        <th>Icon</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="FunctionButton">
    <tr>
     <td style="text-align:center"><xsl:value-of select="@id"/></td>
     <td><xsl:value-of select="@text"/></td>
     <td style="text-align:center"><xsl:value-of select="@isLockable"/></td>
     <td style="text-align:center"><xsl:value-of select="@isVisible"/></td>
     <td style="text-align:center"><xsl:value-of select="@fontSize"/></td>
     <td>ImageSize: <xsl:value-of select="@buttonImageSize"/> 
       <xsl:if test='@iconPath != ""'><br/>"Off" Icon: <xsl:value-of select="@iconPath"/></xsl:if> 
       <xsl:if test='@selectedIconPath != ""'><br/>"On" Icon: <xsl:value-of select="@selectedIconPath" /></xsl:if>
     </td>
   </tr>
  </xsl:template>

  <!-- Display address panel subwindow -->
  <xsl:template match="AddressPanel">
    <h4>Address Panel   <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="33%">
      <tr>
        <th>Loco Number</th>
        <th>Attributes</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="locoaddress">
    <xsl:if test='number > 0'>
      <tr>
        <td>
          <xsl:if test='number > 0'>
            <xsl:value-of select="number"/>
          </xsl:if>
        </td>
        <td>
          <xsl:if test='dcclocoaddress/@number > 0'>DCC</xsl:if>
          <xsl:if test='dcclocoaddress/@longaddress = "yes"'>[Long]</xsl:if>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- Display speed panel subwindow -->
  <xsl:template match="SpeedPanel">
    <h4>Speed Panel     <xsl:call-template name="window"/></h4>
    <xsl:apply-templates/>
  </xsl:template>

  <!-- Display Jynstrument panel subwindow -->
  <xsl:template match="Jynstrument">
    <h4>Jynstrument Panel     <xsl:call-template name="window"/></h4>
    <table border="border-width:thin" width="75%">
      <xsl:for-each select="@*"> 
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="./*"> 
       <xsl:if test='name() != "window"'>
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td><xsl:for-each select="@*">
            <xsl:value-of select="name()"/>: <xsl:value-of select="."/><br/></xsl:for-each>
          </td>
        </tr>
       </xsl:if>
      </xsl:for-each>
    </table>
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
