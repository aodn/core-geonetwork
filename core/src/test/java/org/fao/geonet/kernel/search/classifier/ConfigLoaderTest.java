package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.search.classifier.Split;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ConfigLoaderTest {
    private Element testDimensions;

    @Before
    public void loadTestData() throws IOException, JDOMException {
        URL url = this.getClass().getResource("/WEB-INF/config-summary.xml");
        Element configSummary = Xml.loadFile(url);
        testDimensions = Xml.selectElement(configSummary, "dimensions");
    }

    @Test
    public void testNewInstance() throws Exception {
        Element keywordTokenElement = Xml.selectElement(testDimensions, "dimension[@name='keywordToken']");
        @SuppressWarnings("unchecked")
        List<Element> keywordTokenParams = (List <Element>) keywordTokenElement.getChildren();
        ConfigLoader loader = new ConfigLoader(null);

        Split splitClassifier = (Split) loader.newInstance(
            "org.fao.geonet.kernel.search.classifier.Split",
            keywordTokenParams
        );

        List<CategoryPath> categories = splitClassifier.classify("one-two");

        assertCategoryListEquals(categories, "one/two");
    }

}
