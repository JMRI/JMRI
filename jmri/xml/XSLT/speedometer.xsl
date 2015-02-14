<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI Speedometer XML file into displayable     -->
<!-- HTML                                                                   -->

<!-- Used by default when the speedometer file is displayed in a web        -->
<!-- browser                                                                -->

<!-- This is just a basic implementation for debugging purposes, without    -->
<!-- any real attempt at formatting                                         -->

<!-- This file is part of JMRI.  Copyright 2011.                            -->
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
      <xsl:param name="JmriCopyrightYear"/>

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We can also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='speedometer-config'>
<html>
<head>
<title>JMRI Speedometer File</title>
</head>

<body>
<h2>JMRI Speedometer File</h2>
<xsl:apply-templates/>
<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
<a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </a>
</body>
</html>
</xsl:template>

<!-- Display Configuration -->
<xsl:template match="configuration">
<h4>Configuration</h4>
Use Metric Units= "<xsl:value-of select="useMetric"/>"
</xsl:template>

<!-- Display Sensors -->
<xsl:template match="sensors">
<h4>Sensors</h4>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="sensor">
Sensor
sensorName="<xsl:value-of select="sensorName"/>"
type="<xsl:value-of select="type"/>"
trigger="<xsl:value-of select="trigger"/>"
<xsl:if test="distance">
distance="<xsl:value-of select="distance"/>"
</xsl:if>
<br/>
</xsl:template>

</xsl:stylesheet>
