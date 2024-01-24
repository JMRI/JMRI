<?xml version="1.0" encoding="UTF-8"?>
<!-- Stylesheet to convert a JMRI throttle XML file into displayable HTML -->
<!-- Used by default when the throttle file is displayed in a web browser-->
<!-- This is just a basic implementation for debugging purposes, without -->
<!-- any real attempt at formatting -->
<!-- This file is part of JMRI.  Copyright 2007-2023.                       -->
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

  <xsl:output method="html" encoding="UTF-8"/>

  <!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->

  <xsl:param name="JmriCopyrightYear" select="concat('1997','-','2024')"/>

  <!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We can also pick some stuff out explicitly in the head section using
     value-of instructions.
-->

  <xsl:template match='throttle-layout-config'>
    <html>
      <head>
        <title>JMRI Throttle List File</title>
      </head>
      <body>
        <h1>JMRI Throttle List File
        <!-- For when we move to XSL 2.0: <xsl:value-of select="static-base-uri()" />-->
        </h1>
        <xsl:apply-templates/>
        <br/>
        <hr/>
This page was produced by <a href="https://www.jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear"/> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/>
        <a href="https://www.jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
      </body>
    </html>
  </xsl:template>

  <!-- Title each throttle frames -->
  <xsl:template match="ThrottlesListPanel">
    <h1>Throttles List Panel</h1>
     <xsl:call-template name="window"/>
     <xsl:apply-templates/>
  </xsl:template>
<!-- Title each throttle frames -->
  <xsl:template match="ThrottleWindow">
    <h2>Throttle Window</h2>
    <table border="border-width:thin" width="67%">
    <tr><td><xsl:call-template name="window"/><xsl:if test='@title != ""'><xsl:value-of select="@title"/></xsl:if></td></tr>
    <tr><td><xsl:if test='@titleType != ""'>Title Type: <xsl:value-of select="@titleType"/></xsl:if></td></tr>
    </table>
    <xsl:apply-templates/>
  </xsl:template>
<!-- Title each throttle frames -->
  <xsl:template match="ThrottleWindow/ThrottleFrame">
    <h3>Throttle Frame: "<xsl:value-of select="@ThrottleXMLFile"/>"</h3>
    <xsl:apply-templates/>
  </xsl:template>
<!-- Title each throttle frames -->
  <xsl:template match="ThrottleWindow/Jynstrument">
    <h3>Toolbar Jynstrument</h3>
    <table border="border-width:thin" width="67%">
          <tr><td>Jynstrument Folder</td><td><xsl:value-of select="@JynstrumentFolder"/></td></tr>
      <!-- Future proofing: ["USBThottle" is only element at current time 2022/03/25] -->
      <xsl:for-each select="./*">
        <tr>
          <td><xsl:value-of select="name()"/></td>
          <td><xsl:for-each select="@*"> <!-- Only one attribute at current time 2022/03/25 -->
            <xsl:value-of select="name()"/>: <xsl:value-of select="."/><br/></xsl:for-each>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>


<!-- All detail process templates in include -->
<xsl:include href="throttle-include.xsl"/>

</xsl:stylesheet>

