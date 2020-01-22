package au.org.emii.classifier;

import org.apache.log4j.Logger;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.classifier.Classifier;

import java.util.ArrayList;
import java.util.List;
import java.net.*;

/**
 * This class is used to determine the category paths (facets) to be indexed for an AODN term given its URI
 */

public class UriLabelClassifier implements Classifier  {
    private static Logger logger = Logger.getLogger(UriLabelClassifier.class);

    private final AodnThesaurus vocabularyThesaurus; // vocabulary in which the term is defined
    private final AodnTermClassifier termClassifier; // classifier used to create CategoryPaths for a term
    private final String indexKey; // classifier indexKey
    private Classifier uriClassifier;
    private Classifier labelClassifier;

    public UriLabelClassifier(ThesaurusFinder thesaurusFinder, String vocabularyScheme, String classificationScheme, String indexKey) {
        vocabularyThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(vocabularyScheme));
        AodnThesaurus classificationThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(classificationScheme));
        termClassifier = new AodnTermClassifier(vocabularyThesaurus, classificationThesaurus);
        this.indexKey = indexKey;
        this.uriClassifier = new UriClassifier(thesaurusFinder, vocabularyScheme, classificationScheme, null);
        this.labelClassifier = new LabelClassifier(thesaurusFinder, vocabularyScheme, classificationScheme, null);
    }

    @Override
    public List<CategoryPath> classify(String value) {
        try {
            URL url = new URL(value);
            return uriClassifier.classify(value);
        } catch (MalformedURLException e){
            return labelClassifier.classify(value);
        }
    }
}
