<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
    <xsl:template match="/">
        kaptCode,kaptName
        <xsl:for-each select="//item">
            <xsl:value-of select="concat(kaptCode,',',kaptName,'&#xA;')"/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>