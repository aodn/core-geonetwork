package au.org.emii.classifier;

import org.apache.log4j.Logger;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.classifier.Classifier;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to determine the category paths (facets) to be indexed for an AODN term given its prefLabel or atlLabel
 */

public class LabelClassifier implements Classifier {
    private static Logger logger = Logger.getLogger(LabelClassifier.class);

    private final AodnThesaurus vocabularyThesaurus; // vocabulary in which the term is defined
    private final AodnTermClassifier termClassifier; // classifier used to create CategoryPaths for a term
    private final String indexKey; // classifier indexKey

    public LabelClassifier(ThesaurusFinder thesaurusFinder, String vocabularyScheme, String classificationScheme, String indexKey) {
        vocabularyThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(vocabularyScheme));
        AodnThesaurus classificationThesaurus = new AodnThesaurus(thesaurusFinder.getThesaurusByConceptScheme(classificationScheme));
        termClassifier = new AodnTermClassifier(vocabularyThesaurus, classificationThesaurus);
        this.indexKey = indexKey;
    }

    @Override
    public List<CategoryPath> classify(String value) {
        List<AodnTerm> matchingTerms = vocabularyThesaurus.getTermWithLabel(value);

        if (matchingTerms.isEmpty()) {
            logger.warn(String.format("Could not find term with label='%s' in vocabulary='%s' discoveryParams='%s=%s'",
                    value, vocabularyThesaurus.getThesaurusTitle(), indexKey, value));
            return new ArrayList<CategoryPath>();
        }

        AodnTerm term = getBestMatch(matchingTerms, value);

        return termClassifier.classify(term);
    }

    private AodnTerm getBestMatch(List<AodnTerm> matchingTerms, String label) {
        // If there's only one return that

        if (matchingTerms.size() == 1) {
            return matchingTerms.get(0);
        }

        // Otherwise, return any term found which has not been replaced by another term

        for (AodnTerm aodnTerm: matchingTerms) {
            if (aodnTerm.getReplaces() != null && aodnTerm.getReplacedBy() == null) {
                return aodnTerm;
            }
        }

        // Otherwise, log a warning and return the first

        AodnTerm term = matchingTerms.get(0);

        logger.warn("Multiple matching terms found for '" + label + "' in '"
            + vocabularyThesaurus.getThesaurusTitle() + "' returning '" + term.getPrefLabel() + "'");

        return term;

    }
}
