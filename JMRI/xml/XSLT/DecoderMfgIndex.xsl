<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI decoder definition index to a HTML page -->

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
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
      <xsl:param name="JmriCopyrightYear">
        2000 -
            <script language="JavaScript" type="text/javascript">
                now = new Date
                theYear=now.getFullYear()
                document.write(theYear)
            </script>
      </xsl:param>

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
<xsl:template match='decoderIndex-config'>

<html>
	<head>
		<title>Index by manufacturer ID number</title>
	</head>
	
	<body>
	<h2>NMRA manufacturer numbers, sorted by number</h2>	
		<xsl:apply-templates/>

<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
    <a href="http://sourceforge.net/projects/jmri">
    <img src="https://sourceforge.net/sflogo.php?type=13&amp;group_id=26788" border="0" alt="JMRI Model Railroad Interface at SourceForge.net"/></a>
	</body>
</html>

</xsl:template>


<!-- Descend into "decoderIndex" element -->
<xsl:template match='decoderIndex'>
   <xsl:apply-templates/>
</xsl:template>

<!-- Descend into "mfgList" element -->
<xsl:template match='mfgList'>
   <xsl:apply-templates>
        <xsl:sort select="@mfgID" data-type="number"/>
   </xsl:apply-templates>
</xsl:template>

<!-- List the family information. Output as H2 -->
<xsl:template match="manufacturer">
<xsl:value-of select="@mfgID"/> &#160; <xsl:value-of select="@mfg"/>
<BR/>
</xsl:template>

</xsl:stylesheet>
