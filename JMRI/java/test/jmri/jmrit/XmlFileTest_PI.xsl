<?xml version="1.0" encoding="utf-8"?>
<!-- test file for XmlFileTest.testProcessPI -->
<xsl:stylesheet	version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" encoding="utf-8"/>

    <!-- actual transform is contains -> content -->
    <xsl:template match="contains">
        <content>
            <xsl:apply-templates select="@*|node()"/>
        </content>
    </xsl:template>

    <!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
