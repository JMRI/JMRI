<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: DecoderID.xsl,v 1.1 2003-12-28 19:30:13 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI decoder definition index and -->
<!-- definition files into an HTML page on decoder ID -->

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
<xsl:template match='decoderIndex-config'>

<html>
	<head>
		<title>JMRI decoder identification</title>
	</head>
	
	<body>
		<h2>JMRI decoder selection identification</h2>

                <xsl:apply-templates/>

	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>

<!-- Index through manufacturers -->
<xsl:template match="decoderIndex-config/decoderIndex/mfgList/manufacturer">
<xsl:if test="not( @mfg = 'NMRA' )" >
<h3><xsl:value-of select="@mfg"/> CV8=<xsl:value-of select="@mfgID"/></h3>
        <xsl:call-template name="familyTable">
                <xsl:with-param name="mfgname" select="@mfg"/>
        </xsl:call-template>
</xsl:if>
</xsl:template>

<!-- template to create the table for a specific mfg -->
<!-- needs two improvements:  dont put out a line if the versionCV is present and -->
<!--   handle versions specified at several levels -->
<xsl:template name="familyTable">
        <xsl:param name="mfgname"/>
		<!-- define table and fill -->
		<table border="0" cellspacing="1" cellpadding="1">
		<tr>
			<th bgcolor="#cccccc">Model</th>
			<th bgcolor="#cccccc">Family</th>
			<th bgcolor="#cccccc">Min CV7 value</th>
			<th bgcolor="#cccccc">Max CV7 value</th>
		</tr>

		<xsl:for-each select="/decoderIndex-config/decoderIndex/familyList/family">
		  <xsl:if test="( @mfg = $mfgname )" >
			<xsl:for-each select="document(@file)/decoder-config/decoder/family/model">
			
			<!-- display model as row in table -->
			<tr>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@model"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@name"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center">
<xsl:if test="( @lowVersionID = '' )" ><xsl:value-of select="../@lowVersionID"/></xsl:if>
<xsl:value-of select="@lowVersionID"/>
</td>
				<td bgcolor="#eeeeee" valign="top" align="center">
<xsl:value-of select="../@highVersionID"/>
<xsl:value-of select="@highVersionID"/>
</td>
			</tr>
                        <xsl:apply-templates/>
			</xsl:for-each>
		  </xsl:if>
		</xsl:for-each>

		</table>
</xsl:template>

<!-- Index through versionCV elements in a model  -->
<xsl:template match="versionCV">
                        <tr>
                                <td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@model"/></td>
                                <td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../../@name"/></td>
                                <td bgcolor="#eeeeee" valign="top" align="center">
<xsl:value-of select="@lowVersionID"/>
</td>
                                <td bgcolor="#eeeeee" valign="top" align="center">
<xsl:value-of select="@highVersionID"/>
</td>
                        </tr>
</xsl:template>


</xsl:stylesheet>
