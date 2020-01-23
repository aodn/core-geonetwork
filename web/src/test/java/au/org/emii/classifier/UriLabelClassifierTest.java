package au.org.emii.classifier;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.classifier.Classifier;
import org.fao.geonet.test.ThesaurusDirectoryLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.fao.geonet.test.CategoryTestHelper.assertCategoryListEquals;
import static org.junit.Assert.assertEquals;

public class UriLabelClassifierTest {

    private static final String VOCABULARY_SCHEME = "http://www.my.com/test_vocabulary";
    private static final String CLASSIFICATION_SCHEME = "http://www.my.com/test_classification";
    private final String indexKey = "";

    private static ThesaurusFinder thesaurusFinder;

    private Classifier uriLabelClassifier;

    @BeforeClass
    static public void loadThesauri() {
        URL thesauriDirectory = UriLabelClassifier.class.getResource("/thesauri");
        thesaurusFinder = new ThesaurusDirectoryLoader(thesauriDirectory.getFile());
    }

    @Before
    public void setup() {
        uriLabelClassifier = new UriLabelClassifier(thesaurusFinder, VOCABULARY_SCHEME, CLASSIFICATION_SCHEME, indexKey);
    }

    @Test
    public void testClassifyHierarchyWithBroaderTerms() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#sea_surface_temperature");
        assertCategoryListEquals(result, "ocean/ocean temperature/sea surface temperature");
    }

    @Test
    public void testClassifyTermWithMultipleBroaderTerms() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#air_sea_flux");
        assertCategoryListEquals(result, "physical - water/air sea flux", "physical - air/air sea flux");
    }

    @Test
    public void testClassifyTermWithNoBroaderTerms() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#longitude");
        assertEquals(0, result.size());
    }

    @Test
    public void testClassifyTermDoesNotExist() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#unknown_term");
        assertEquals(0, result.size());
    }

    @Test
    public void testDeepDrilldownFacet() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#hubble");
        assertCategoryListEquals(result, "space junk/satellite/orbitting satellite/hubble telescope");
    }

    // Organisation facets tests

    @Test
    public void testRelatedMatch() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#aatams");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

    @Test
    public void testRelatedMatchTermOnly() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#imos");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)");
    }

    @Test
    public void testDisplayLabelTerm() {
        List<CategoryPath> result = uriLabelClassifier.classify("http://www.my.com/test_vocabulary/#aatams");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

    @Test
    public void testPreferredLabelLookup() {
        List<CategoryPath> result = uriLabelClassifier.classify("Animal Tracking Facility, Integrated Marine Observing System (IMOS)");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

    @Test
    public void testAlternateLabelLookup() {
        List<CategoryPath> result = uriLabelClassifier.classify("AATAMS");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Animal Tracking Facility");
    }

    @Test
    public void testMultipleAlternateLabelLookup() {
        List<CategoryPath> result = uriLabelClassifier.classify("SOOP");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Ships of Opportunity Facility (SOOP)");
    }

    @Test
    public void testLabelNotFoundLookup() {
        List<CategoryPath> result = uriLabelClassifier.classify("MISSING");
        assertEquals(0, result.size());
    }

    @Test
    public void testMultipleReplacedBysFound() {
        List<CategoryPath> result = uriLabelClassifier.classify("Australian Institute of Marine Science (AIMS)");
        assertCategoryListEquals(result, "Australian Institute of Marine Science (AIMS) 3");
    }

    @Test
    public void testOneReplacedByFound() {
        List<CategoryPath> result = uriLabelClassifier.classify("Australian Institute of Marine Science (AIMS), Department of Industry, Innovation and Science, Australian Government");
        assertCategoryListEquals(result, "Australian Institute of Marine Science (AIMS) 2");
    }

    @Test
    public void testUsesAltLabelIfPresent() {
        List<CategoryPath> result = uriLabelClassifier.classify("Argo Floats Facility");
        assertCategoryListEquals(result, "Integrated Marine Observing System (IMOS)/Argo Floats Facility");
    }
}
