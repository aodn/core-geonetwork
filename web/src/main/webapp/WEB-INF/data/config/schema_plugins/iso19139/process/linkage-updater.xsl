<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:mcp="http://bluenet3.antcrc.utas.edu.au/mcp"
    xmlns:geonet="http://www.fao.org/geonetwork"
    exclude-result-prefixes="mcp geonet"
    version="2.0">
    
    <xsl:output indent="yes"/>

    <xsl:param name="pattern"/>
    <xsl:param name="replacement"/>
    <xsl:param name="pot_url"/> <!-- point-of-truth url to add (templated ${uuid} is replaced witch actual uuid)-->

    <xsl:variable name="metadata-uuid" select="//gmd:fileIdentifier/*/text()"/>

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

    <!-- Add point of truth online resource element to the first transferOptions -->
    <!-- element in the MD_Distribution section if pot_url provided and it       -->
    <!-- doesn't exist already             -->

    <xsl:variable name="has-pot" select="//gmd:MD_Distribution//gmd:protocol/*/text()[.='WWW:LINK-1.0-http--metadata-URL']"/>

    <xsl:variable name="add-pot" select="$pot_url and not($has-pot)"/>

    <xsl:template match="gmd:MD_Distribution[$add-pot]/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions[1]">
        <xsl:copy>
            <xsl:apply-templates select="node()"/>
            <gmd:onLine>
                <gmd:CI_OnlineResource>
                    <gmd:linkage>
                        <gmd:URL><xsl:value-of select="replace($pot_url, '\$\{uuid\}', $metadata-uuid)"/></gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                        <gco:CharacterString>WWW:LINK-1.0-http--metadata-URL</gco:CharacterString>
                    </gmd:protocol>
                    <gmd:description>
                        <gco:CharacterString>Point of truth URL of this metadata record</gco:CharacterString>
                    </gmd:description>
                </gmd:CI_OnlineResource>
            </gmd:onLine>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
