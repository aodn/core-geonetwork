package org.fao.geonet.kernel.search.facet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;

import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.search.Dimension;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;

public class ItemBuilderTest {

	@Test
	public void testBuild() throws JDOMException, IOException {
		ItemConfig itemConfig = mockItemConfig();
		ServiceContext context = mockServiceContext();
		FacetResult facetResult = mockFacetResult();

		ItemBuilder builder = new ItemBuilder(context, itemConfig);
		Element summary = builder.build(facetResult, "eng");

		Element expectedResult = loadExpectedResult();
		assertEquals(Xml.getString(expectedResult), Xml.getString(summary));
	}

	private ServiceContext mockServiceContext() {
		return mock(ServiceContext.class);
	}

	private ItemConfig mockItemConfig() throws JDOMException, IOException {
		URL url = this.getClass().getResource("/org/fao/geonet/kernel/search/config-summary.xml");
		Element facetConfig = Xml.loadFile(url);
		
		Element dimension = Xml.selectElement(facetConfig, "dimensions/dimension[@name='Keyword']");
		HashMap<String, Dimension> dimensions = new HashMap<String, Dimension>();
		dimensions.put("Keyword", new Dimension(dimension));

		Element item = Xml.selectElement(facetConfig, "def/keyword/item");
		ItemConfig itemConfig= new ItemConfig(item, dimensions);
		return itemConfig;
	}

	private FacetResult mockFacetResult() {
		Iterator iterator = mock(Iterator.class);

		when(iterator.hasNext()).thenReturn(true, true, true, false);

		FacetResultNode test1 = mockNode("test1", 2, 0);
		FacetResultNode test2 = mockNode("test2", 1, 0);
		FacetResultNode test3 = mockNode("test3", 5, 0);
		
		when(iterator.next()).thenReturn(test1)
			.thenReturn(test2)
			.thenReturn(test3);

		Collection nodes = mock(Collection.class);
		when(nodes.iterator()).thenReturn(iterator);
		
		FacetResultNode node = mockNode("Keywords", 7.0, 3);
		when(node.getSubResults()).thenReturn(nodes);

		FacetResult facetResult = new FacetResult(null, node, 3);
		return facetResult;
	}

	private Element loadExpectedResult() throws IOException, JDOMException {
		URL url = this.getClass().getResource("/results/org/fao/geonet/kernel/search/facet/ItemBuilderTest/testBuild.xml");
		Element expectedResult = Xml.loadFile(url);
		return expectedResult;
	}

	private FacetResultNode mockNode(String path, double value, int subResults) {
		FacetResultNode resultNode = mock(FacetResultNode.class);
		when(resultNode.getLabel()).thenReturn(new CategoryPath(path, '/'));
		when(resultNode.getValue()).thenReturn(value);
		when(resultNode.getNumSubResults()).thenReturn(subResults);
		return resultNode;
	}

}
