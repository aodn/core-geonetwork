package org.fao.geonet.kernel.search.facet;

import static org.fao.geonet.kernel.search.Translator.NULL_TRANSLATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ItemConfigTest {
    private static final String TEST_DIMENSION = "keywordToken";
    private Element testData;

    @Before
    public void loadTestData() throws IOException, JDOMException {
        URL url = this.getClass().getResource("/WEB-INF/config-summary.xml");
        testData = Xml.loadFile(url);
    }

    @Test
    public void testItemConfigDefaults() throws JDOMException {
        Element dimensionElement = Xml.selectElement(testData, "dimensions/dimension[@name='" + TEST_DIMENSION + "']");
        HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
        Dimension dimension = new Dimension(dimensionElement);
        dimensions.put(TEST_DIMENSION, dimension);

        Element item = Xml.selectElement(testData, "def/default_attributes/item");
        ItemConfig itemConfig= new ItemConfig(item, dimensions);

        assertEquals(dimension, itemConfig.getDimension());
        assertEquals(Facet.DEFAULT_MAX_KEYS, itemConfig.getMax());
        assertEquals(Facet.DEFAULT_DEPTH, itemConfig.getDepth());
        assertEquals(Facet.SortBy.COUNT, itemConfig.getSortBy());
        assertEquals(Facet.SortOrder.DESCENDING, itemConfig.getSortOrder());
        assertTrue(itemConfig.getFormatter() instanceof FacetNameFormatter);
        assertEquals(itemConfig.getTranslator(null), NULL_TRANSLATOR);
    }

    @Test
    public void testItemConfigAll() throws JDOMException {
        Element dimensionElement = Xml.selectElement(testData, "dimensions/dimension[@name='" + TEST_DIMENSION + "']");
        HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
        Dimension dimension = new Dimension(dimensionElement);
        dimensions.put(TEST_DIMENSION, dimension);

        Element item = Xml.selectElement(testData, "def/all_attributes/item");
        ItemConfig itemConfig= new ItemConfig(item, dimensions);

        assertEquals(dimension, itemConfig.getDimension());
        assertEquals(17, itemConfig.getMax());
        assertEquals(3, itemConfig.getDepth());
        assertEquals(Facet.SortBy.NUMVALUE, itemConfig.getSortBy());
        assertEquals(Facet.SortOrder.ASCENDING, itemConfig.getSortOrder());
        assertTrue(itemConfig.getFormatter() instanceof DimensionFormatter);
        // Can't test non-default getTranslator as implemented using static methods
    }
}
