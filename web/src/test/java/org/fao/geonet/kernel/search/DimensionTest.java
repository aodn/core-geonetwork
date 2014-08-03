package org.fao.geonet.kernel.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import jeeves.utils.Xml;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class DimensionTest {
	private Element testData;

	@Before
	public void loadTestData() throws IOException, JDOMException {
		URL url = this.getClass().getResource("/org/fao/geonet/kernel/search/config-summary.xml");
		Element configSummary = Xml.loadFile(url);
		testData = Xml.selectElement(configSummary, "dimensions");
	}

	@Test
	public void testDimension() throws JDOMException {
		Element pointOfContact = Xml.selectElement(testData, "dimension[@name='metadataPOC']");
		Dimension dimension = new Dimension(pointOfContact);
		assertEquals("metadataPOC", dimension.getName());
		assertEquals("metadataPOC", dimension.getIndexKey());
		assertEquals("metadataPOCs", dimension.getLabel());
		assertEquals("org.fao.geonet.kernel.search.classifier.Value", dimension.getClassifier());
		assertEquals(dimension.getParams().size(), 0);
	}

	@Test
	public void testGetClassifier() throws JDOMException {
		Element keywords = Xml.selectElement(testData, "dimension[@name='Keyword']");
		Dimension dimension = new Dimension(keywords);
		assertEquals("org.fao.geonet.kernel.search.classifier.Split", dimension.getClassifier());
	}

	@Test
	public void testGetParams() throws JDOMException {
		Element keywords = Xml.selectElement(testData, "dimension[@name='Keyword']");
		Dimension dimension = new Dimension(keywords);
		assertEquals(dimension.getParams().size(), 1);
		assertEquals(dimension.getParams().get(0).getAttributeValue("name"), "regex");
	}

}
