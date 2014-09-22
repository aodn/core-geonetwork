package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;
import static org.fao.geonet.test.CategoryTestHelper.createCategoryPathList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.junit.Test;

public class DimensionTest {

	@Test
	public void testClassify() {
		Classifier mockClassifier = mockClassifier("a-b/a-c", "a/b", "a/c");
		Dimension dimensionClassifier = new Dimension("v", mockClassifier);

		List<CategoryPath> result = dimensionClassifier.classify("a-b/a-c");

		assertCategoryListEquals(result, "v/a/b", "v/a/c");
	}

	private Classifier mockClassifier(String value, String... returnCategoryPaths) {
		Classifier mockClassifier = mock(Classifier.class);
		List<CategoryPath> returnCategoryPathList = createCategoryPathList(returnCategoryPaths);
		when(mockClassifier.classify(value)).thenReturn(returnCategoryPathList);
		return mockClassifier;
	}

}
