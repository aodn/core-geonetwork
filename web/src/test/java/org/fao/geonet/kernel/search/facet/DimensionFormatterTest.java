package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.fao.geonet.kernel.search.Dimension;
import org.fao.geonet.kernel.search.Translator;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class DimensionFormatterTest {

	private DimensionFormatter formatter; 

	@Before
	public void loadTestData() throws IOException, JDOMException {
		Dimension mockDimension = mock(Dimension.class);
		when(mockDimension.getName()).thenReturn("keyword");

		ItemConfig mockConfig = mock(ItemConfig.class);
		when(mockConfig.getDimension()).thenReturn(mockDimension);
		when(mockConfig.getTranslator(null, "eng")).thenReturn(Translator.NULL_TRANSLATOR);

		formatter = new DimensionFormatter(null, mockConfig);
	}

	@Test
	public void testBuildDimensionTag() throws JDOMException {
		Element dimensionTag = formatter.buildDimensionTag("Keywords", "6", "eng");

		assertEquals("dimension", dimensionTag.getName());
		assertEquals(0, dimensionTag.getContent().size());
		assertEquals(2, dimensionTag.getAttributes().size());
		assertEquals("Keywords", dimensionTag.getAttributeValue("value"));
		assertEquals("6", dimensionTag.getAttributeValue("count"));
	}

	@Test
	public void testBuildCategoryTag() {
		Element categoryTag = formatter.buildCategoryTag("oceans", "3", "eng");

		assertEquals("category", categoryTag.getName());
		assertEquals(0, categoryTag.getContent().size());
		assertEquals(2, categoryTag.getAttributes().size());
		assertEquals("oceans", categoryTag.getAttributeValue("value"));
		assertEquals("3", categoryTag.getAttributeValue("count"));
	}

}
