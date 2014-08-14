package org.fao.geonet.kernel.search.facet;

import static org.fao.geonet.kernel.search.Translator.NULL_TRANSLATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.fao.geonet.kernel.search.Dimension;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ItemConfigTest {
	private Element testData;

	@Before
	public void loadTestData() throws IOException, JDOMException {
		URL url = this.getClass().getResource("/org/fao/geonet/kernel/search/config-summary.xml");
		testData = Xml.loadFile(url);
	}

	@Test
	public void testItemConfigDefaults() throws JDOMException {
		Element dimensionElement = Xml.selectElement(testData, "dimensions/dimension[@name='Keyword']");
		HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
		Dimension dimension = new Dimension(dimensionElement);
		dimensions.put("Keyword", dimension);

		Element item = Xml.selectElement(testData, "def/default_attributes/item");
		ItemConfig itemConfig= new ItemConfig(item, dimensions);

		assertEquals(dimension, itemConfig.getDimension());
		assertEquals(Facet.DEFAULT_MAX_KEYS, itemConfig.getMax());
		assertEquals(Facet.DEFAULT_DEPTH, itemConfig.getDepth());
		assertEquals(Facet.SortBy.COUNT, itemConfig.getSortBy());
		assertEquals(Facet.SortOrder.DESCENDING, itemConfig.getSortOrder());
		assertTrue(itemConfig.getFormatter(null) instanceof FacetNameFormatter);
		assertEquals(itemConfig.getTranslator(mock(ServiceContext.class), null), NULL_TRANSLATOR);
	}

	@Test
	public void testItemConfigAll() throws JDOMException {
		Element dimensionElement = Xml.selectElement(testData, "dimensions/dimension[@name='Keyword']");
		HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
		Dimension dimension = new Dimension(dimensionElement);
		dimensions.put("Keyword", dimension);

		Element item = Xml.selectElement(testData, "def/all_attributes/item");
		ItemConfig itemConfig= new ItemConfig(item, dimensions);

		assertEquals(dimension, itemConfig.getDimension());
		assertEquals(17, itemConfig.getMax());
		assertEquals(3, itemConfig.getDepth());
		assertEquals(Facet.SortBy.NUMVALUE, itemConfig.getSortBy());
		assertEquals(Facet.SortOrder.ASCENDIND, itemConfig.getSortOrder());
		assertTrue(itemConfig.getFormatter(null) instanceof DimensionFormatter);
		// Can't test non-default getTranslator as implemented using static methods
	}
}
