<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI VSDecoder config XML file into displayable HTML -->

<!-- Used by default when the config file is displayed in a web browser-->

<!-- This is just a basic implementation for debugging purposes, without -->
<!-- any real attempt at formatting -->

<!-- This file is part of JMRI.  Copyright 2007-2018.                            -->
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

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2019')" />

<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We can also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='/vsdecoder-config'>

<html>
	<head>
		<title>JMRI Virtual Sound Decoder Config File</title>
	</head>
	
	<body>
		<h1>VSd Config File</h1>

                <xsl:apply-templates/>

<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
      <a href="http://www.tagadab.com/">
      <img src="https://www.tagadab.com/sites/default/files/logo-tagadab-nostrap.png" height="28" width="103" border="0" alt="Tagadab logo"/></a>

	</body>
</html>
</xsl:template>

<xsl:template match="profile">
  <h2>Profile: <xsl:value-of select="@name"/></h2>
  <xsl:for-each select="default">
    (DEFAULT)
  </xsl:for-each>
  
  <h2>Sound Events</h2>
  <xsl:apply-templates select="sound-event"/>
  <br/>
  <hr/>
  <br/>
  <h2>Sounds</h2>
  <xsl:apply-templates select="sound"/>
  
</xsl:template>

<xsl:template match="sound">
  <hr/>
  <h3>Sound: <xsl:value-of select="@name"/></h3>
  Type = <xsl:value-of select="@type"/><br/>
  <xsl:choose>
    <xsl:when test="@type = 'configurable'">
      Start file: <xsl:value-of select="start-file"/><br/>
      Mid file: <xsl:value-of select="mid-file"/><br/>
      End file: <xsl:value-of select="end-file"/><br/>
      Short file: <xsl:value-of select="short-file"/><br/>
      Start sound duration: <xsl:value-of select="start-sound-duration"/><br/>
    </xsl:when>
    <xsl:when test="@type = 'diesel'">
      Start sound = <xsl:value-of select="start-sound/file"/><br/>
      Shutdown sound = <xsl:value-of select="shutdown-sound/file"/><br/>
      Notch-up sound = <xsl:value-of select="notch-up-sound/file"/><br/>
      # Notches = <xsl:value-of select="notches"/><br/>
      <xsl:for-each select="notch-sound">
	Notch sound ( <xsl:value-of select="notch"/> ) :
	File = <xsl:value-of select="file"/><br/>
	
      </xsl:for-each>

    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="sound-event">
  <hr/>
  <h3>SoundEvent: <xsl:value-of select="@name"/></h3>
  Label = <xsl:value-of select="@label"/><br/>
  Buttontype = <xsl:value-of select="@buttontype"/><br/>
  <xsl:for-each select="trigger">
    <h5>Trigger: <xsl:value-of select="@name"/></h5>
    Type = <xsl:value-of select="@type"/><br/>
    Event name = <xsl:value-of select="event-name"/><br/>
    Target name = <xsl:value-of select="target-name"/><br/>
    Match = <xsl:value-of select="match"/><br/>
    Action = <xsl:value-of select="action"/><br/>
    <xsl:choose>
      <xsl:when test="type = FLOAT">
	Compare type = <xsl:value-of select="compare-type"/><br/>
      </xsl:when>
      <xsl:when test="type = INT">
	Compare type = <xsl:value-of select="compare-type"/><br/>
      </xsl:when>
    </xsl:choose>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
