<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI ID Tag XML file into displayable HTML     -->

<!-- Used by default when the id tag file is displayed in a web browser     -->

<!-- This is just a basic implementation for debugging purposes, without    -->
<!-- any real attempt at formatting                                         -->

<!-- This file is part of JMRI.  Copyright 2011-2018.                       -->
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

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2022')" />

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
<xsl:template match='idtagtable'>
<html>
<head>
<title>JMRI ID Tag File</title>
</head>

<body>
<h2>JMRI ID Tag File</h2>
<xsl:apply-templates/>
<hr/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
</body>
</html>
</xsl:template>

<!-- Display Configuration -->
<xsl:template match="configuration">
<h4>Configuration</h4>
Store State = "<xsl:value-of select="storeState"/>"<br/>
Use Fast Clock = "<xsl:value-of select="useFastClock"/>"
</xsl:template>

<!-- Display ID Tags -->
<xsl:template match="idtags">
<h4>ID Tags</h4>
<table border='1'>
<tr>
    <th>System<br/>Name</th>
    <th>User<br/>Name</th>
    <th>Comment</th>
    <th>Where<br/>Last<br/>Seen</th>
    <th>When<br/>Last<br/>Seen</th>
</tr>

<xsl:apply-templates/>
</table>
</xsl:template>

<xsl:template match="idtag">
<tr>
<td><xsl:value-of select="systemName"/></td>
<td><xsl:if test="userName">
    <xsl:value-of select="userName"/>
    </xsl:if></td>
<td><xsl:if test="comment">
    <xsl:value-of select="comment"/>
    </xsl:if></td>
<td><xsl:if test="whereLastSeen">
    <xsl:value-of select="whereLastSeen"/>
    </xsl:if></td>
<td><xsl:if test="whenLastSeen">
    <xsl:value-of select="whenLastSeen"/>
    </xsl:if></td>
</tr>
</xsl:template>

</xsl:stylesheet>
