package au.org.emii.classifier;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.classifier.Classifier;
import org.fao.geonet.test.ThesaurusDirectoryLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UriClassifierTest {

    private static final String VOCABULARY_SCHEME = "http://www.my.com/test_vocabulary";
    private static final String CLASSIFICATION_SCHEME = "http://www.my.com/test_classification";
    
    private static ThesaurusFinder thesaurusFinder;

    private Classifier uriClassifier;

    @BeforeClass
    static public void loadThesauri() {
        URL thesauriDirectory = UriClassifier.class.getResource("/thesauri");
        thesaurusFinder = new ThesaurusDirectoryLoader(thesauriDirectory.getFile());
    }
    
    @Before
    public void setup() {
        uriClassifier = new UriClassifier(thesaurusFinder, VOCABULARY_SCHEME, CLASSIFICATION_SCHEME);
    }

    @Test
    public void testClassifyHierarchyWithBroaderTerms() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#sea_surface_temperature");
        assertCategoryListEquals(result, "ocean/ocean temperature/sea surface temperature");
    }

    @Test
    public void testClassifyTermWithMultipleBroaderTerms() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#air_sea_flux");
        assertCategoryListEquals(result, "physical - water/air sea flux", "physical - air/air sea flux");
    }

    @Test
    public void testClassifyTermWithNoBroaderTerms() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#longitude");
        assertEquals(0, result.size());
    }

    @Test
    public void testClassifyTermDoesNotExist() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#unknown_term");
        assertEquals(0, result.size());
    }

    @Test
    public void testDeepDrilldownFacet() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#hubble");
        assertCategoryListEquals(result, "space junk/satellite/orbitting satellite/hubble telescope");
    }

    // Organisation facets tests

    @Test
    public void testRelatedMatch() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#aatams");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

    @Test
    public void testRelatedMatchTermOnly() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#imos");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)");
    }

    @Test
    public void testDisplayLabelTerm() {
        List<CategoryPath> result = uriClassifier.classify("http://www.my.com/test_vocabulary/#aatams");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

}
