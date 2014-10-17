package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.LabelAndValue;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

public class ItemBuilderTest {

    private static final String DIMENSION_NAME = "keywordToken";
    private static final String SUMMARY_TYPE = "keyword_token";
    private static final int MAX_RESULTS = 100;

    @Test
    public void testItemBuild() throws JDOMException, IOException {
        ItemConfig itemConfig = loadTestItemConfig();
        Facets facets = buildFacets();

        ItemBuilder builder = new ItemBuilder(itemConfig, "eng", facets);
        Element summary = builder.build();

        assertEquals(loadExpectedResult("itemBuild.xml"), Xml.getString(summary));
    }

    private ItemConfig loadTestItemConfig() throws JDOMException, IOException {
        URL url = this.getClass().getResource("/WEB-INF/config-summary.xml");
        Element facetConfig = Xml.loadFile(url);
        Element item = Xml.selectElement(facetConfig, "def/" + SUMMARY_TYPE + "/item");

        Element dimension = Xml.selectElement(facetConfig, "dimensions/dimension[@name='" + DIMENSION_NAME + "']");
        HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
        dimensions.put(DIMENSION_NAME, new Dimension(dimension));

        return new ItemConfig(item, dimensions);
    }

    private Facets buildFacets() throws IOException {
        Facets mockFacets = mock(Facets.class);

        LabelAndValue facet1 = new LabelAndValue("test1", 2);
        LabelAndValue facet2 = new LabelAndValue("test2", 1);
        LabelAndValue facet3 = new LabelAndValue("test3", 5);

        when(mockFacets.getTopChildren(MAX_RESULTS, DIMENSION_NAME, path())).thenReturn(buildFacetResult(7, path(), facet1, facet2, facet3));
        when(mockFacets.getTopChildren(MAX_RESULTS, DIMENSION_NAME, path(facet1.label))).thenReturn(buildFacetResult(facet1, path(facet1.label)));
        when(mockFacets.getTopChildren(MAX_RESULTS, DIMENSION_NAME, path(facet2.label))).thenReturn(buildFacetResult(facet2, path(facet2.label)));
        when(mockFacets.getTopChildren(MAX_RESULTS, DIMENSION_NAME, path(facet3.label))).thenReturn(buildFacetResult(facet3, path(facet3.label)));

        return mockFacets;
    }

    private FacetResult buildFacetResult(LabelAndValue facet, String[] path, LabelAndValue... labelAndValues) {
        return buildFacetResult(new Integer(facet.value.intValue()), path, labelAndValues);
    }

    private FacetResult buildFacetResult(int count, String[] path, LabelAndValue... labelAndValues) {
        return new FacetResult(DIMENSION_NAME, path, new Integer(count), labelAndValues, labelAndValues.length);
    }

    private String[] path(String... components) {
        return components;
    }

    private String loadExpectedResult(String expectedResultFileName) throws IOException, JDOMException {
        URL url = this.getClass().getResource("expected_result/" + expectedResultFileName);
        Element expectedResult = Xml.loadFile(url);
        return Xml.getString(expectedResult);
    }

}
