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

public class FacetNameFormatterTest {

	private FacetNameFormatter formatter; 

	@Before
	public void loadTestData() throws IOException, JDOMException {
		Dimension mockDimension = mock(Dimension.class);
		when(mockDimension.getName()).thenReturn("keyword");
		when(mockDimension.getLabel()).thenReturn("keywords");
		
		Translator mockTranslator = mock(Translator.class);
		when(mockTranslator.translate("oceans")).thenReturn("Oceans");

		ItemConfig mockConfig = mock(ItemConfig.class);
		when(mockConfig.getDimension()).thenReturn(mockDimension);
		when(mockConfig.getTranslator(null, "eng")).thenReturn(mockTranslator);

		formatter = new FacetNameFormatter(null, mockConfig);
	}

	@Test
	public void testBuildDimensionTag() throws JDOMException {
		Element dimensionTag = formatter.buildDimensionTag("Keywords", "6", "eng");

		assertEquals("keywords", dimensionTag.getName());
		assertEquals(0, dimensionTag.getContent().size());
		assertEquals(0, dimensionTag.getAttributes().size());
	}

	@Test
	public void testBuildCategoryTag() {
		Element categoryTag = formatter.buildCategoryTag("oceans", "3", "eng");

		assertEquals("keyword", categoryTag.getName());
		assertEquals(0, categoryTag.getContent().size());
		assertEquals(3, categoryTag.getAttributes().size());
		assertEquals("oceans", categoryTag.getAttributeValue("name"));
		assertEquals("Oceans", categoryTag.getAttributeValue("label"));
		assertEquals("3", categoryTag.getAttributeValue("count"));
	}
}
