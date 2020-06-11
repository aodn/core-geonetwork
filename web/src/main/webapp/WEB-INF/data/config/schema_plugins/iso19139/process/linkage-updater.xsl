<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:mcp="http://bluenet3.antcrc.utas.edu.au/mcp"
    xmlns:geonet="http://www.fao.org/geonetwork"
    exclude-result-prefixes="mcp geonet"
    version="2.0">

    <xsl:preserve-space elements="*"/>
    <xsl:param name="pattern"/>
    <xsl:param name="replacement"/>

    <!-- default action is to copy -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Always remove geonet:* elements. -->
    <xsl:template match="geonet:*"/>

    <xsl:template match="gmd:URL[normalize-space($pattern) != '' and matches(text(), $pattern)]">
        <xsl:copy>
            <xsl:value-of select="replace(text(), $pattern, $replacement)"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="gmd:URL[../../gmd:protocol/*/text()='OGC:WMS-1.1.1-http-get-map']" priority="100">
        <xsl:copy><xsl:value-of select="replace(., concat($pattern,'/geoserver'), 'https://tilecache.aodn.org.au/geowebcache/service')"/></xsl:copy>
    </xsl:template>

    <xsl:template match="gmd:URL[../../gmd:protocol/*/text()='OGC:WMS-1.3.0-http-get-map']" priority="100">
        <xsl:copy><xsl:value-of select="replace(., concat($pattern,'/geoserver'), 'https://tilecache.aodn.org.au/geowebcache/service')"/></xsl:copy>
    </xsl:template>

</xsl:stylesheet>
