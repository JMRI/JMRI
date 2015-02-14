<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI ID Tag XML file into displayable HTML     -->

<!-- Used by default when the id tag file is displayed in a web browser     -->

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
Store State = "<xsl:value-of select="storeState"/>"<br/>
Use Fast Clock = "<xsl:value-of select="useFastClock"/>"
</xsl:template>

<!-- Display ID Tags -->
<xsl:template match="idtags">
<h4>ID Tags</h4>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="idtag">
ID Tag
systemName="<xsl:value-of select="systemName"/>"
<xsl:if test="userName">
userName="<xsl:value-of select="userName"/>"
</xsl:if>
<xsl:if test="comment">
comment="<xsl:value-of select="comment"/>"
</xsl:if>
<xsl:if test="whereLastSeen">
whereLastSeen="<xsl:value-of select="whereLastSeen"/>"
</xsl:if>
<xsl:if test="whenLastSeen">
whenLastSeen="<xsl:value-of select="whenLastSeen"/>"
</xsl:if>
<br/>
</xsl:template>

</xsl:stylesheet>
