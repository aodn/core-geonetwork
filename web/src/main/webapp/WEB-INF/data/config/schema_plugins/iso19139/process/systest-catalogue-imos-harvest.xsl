<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:mcp="http://bluenet3.antcrc.utas.edu.au/mcp"
    exclude-result-prefixes="mcp"
    version="2.0">

    <xsl:param name="pot_url"/> <!-- point-of-truth url to add (templated ${uuid} is replaced witch actual uuid)-->

    <xsl:variable name="metadata-uuid" select="//gmd:fileIdentifier/*/text()"/>

    <!-- url substitutions to be performed -->

    <xsl:variable name="urlSubstitutions">
        <substitution match="https?://geoserver-123.aodn.org.au(:443)?" replaceWith="http://geoserver-systest.aodn.org.au"/>
        <substitution match="https?://thredds.aodn.org.au(:443)?" replaceWith="http://thredds-systest.aodn.org.au"/>
    </xsl:variable>

    <xsl:variable name="urlSubstitutionSelector" select="string-join($urlSubstitutions/substitution/@match, '|')"/>

    <!-- default action is to copy -->

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- substitute production service endpoints with systest service endpoints -->

    <xsl:template match="gmd:URL[matches(., $urlSubstitutionSelector)]">
        <xsl:variable name="url" select="."/>

        <xsl:for-each select="$urlSubstitutions/substitution">
            <xsl:if test="matches(string($url), string(@match))">
                <gmd:URL><xsl:value-of select="replace($url, string(@match), string(@replaceWith))"/></gmd:URL>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

	<xsl:template match="gmd:URL[../../gmd:protocol/*/text()='OGC:WPS--gogoduck']">
        <gmd:URL><xsl:value-of select="replace(.,'https?://processes.aodn.org.au', 'https://processes-systest.aodn.org.au')"/></gmd:URL>
    </xsl:template>

    <xsl:template match="gmd:URL[../../gmd:protocol/*/text()='OGC:WMS-1.1.1-http-get-map']" priority="100">
        <gmd:URL><xsl:value-of select="replace(.,'https?://geoserver-123.aodn.org.au/geoserver', 'https://tilecache-systest.aodn.org.au/geowebcache/service')"/></gmd:URL>
    </xsl:template>

    <xsl:template match="gmd:URL[../../gmd:protocol/*/text()='OGC:WMS-1.3.0-http-get-map']" priority="100">
        <xsl:copy><xsl:value-of select="replace(., 'https?://geoserver-123.aodn.org.au/geoserver', 'https://tilecache-systest.aodn.org.au/geowebcache/service')"/></xsl:copy>
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
