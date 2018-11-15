package au.org.emii.classifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.search.facet.CategoryHelper;

/**
 * This class is used to determine the category paths (facets) to be indexed for an AODN vocabulary term.
 *
 * The category paths are determined by following SKOS broader relationships in the vocabulary thesaurus,
 * SKOS relatedMatch and broadMatch relationships to terms in the classification scheme and broader
 * relationships in the classification scheme until no broader terms can be found.  Terms in the vocabulary
 * scheme with no related terms in the classification scheme are not added as only terms that lead to top
 * level terms in the classification scheme are to be displayed in the portal
 */

public class AodnTermClassifier {

    private final AodnThesaurus vocabularyThesaurus;  // vocabulary in which the term is defined
    private final AodnThesaurus classificationThesaurus;  // Classification scheme defining the portal facet hierarchy for
                                                          // terms in this vocabulary.

    public AodnTermClassifier(AodnThesaurus vocabularyThesaurus, AodnThesaurus classificationThesaurus) {
        this.vocabularyThesaurus = vocabularyThesaurus;
        this.classificationThesaurus = classificationThesaurus;
    }

    /**
     * Determine the category paths to be indexed for the passed term reference (a term label or uri)
     *
     * @param term term to be classified
     * @return a list of all the category paths to top level terms in the classification scheme found for this term
     */

    public List<CategoryPath> classify(AodnTerm term) {
        CategoryPath termPath = new CategoryPath(getCategoryLabel(term));  // Category path for this term
        return getPathsForVocabularyTerm(term, termPath);
    }

    /**
     * Get the category paths to top level terms in the classifications scheme for the term being classified
     * resulting from following broader/related relationships for the passed vocabulary term with the category
     * path termPath.
     *
     * @param term vocabulary term being examined for broader/broadMatch/relatedMatch relationships
     * @param termPath the category path from the term being classified to this term
     * @return a list of all the category paths of top level terms in the classification scheme sourced for this term
     */

    private List<CategoryPath> getPathsForVocabularyTerm(AodnTerm term, CategoryPath termPath) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        // Add all top level category paths for broader terms in the vocabulary thesaurus

        for (AodnTerm broaderTerm : vocabularyThesaurus.getRelatedTerms(term, SkosRelation.BROADER)) {
            CategoryPath broaderTermPath = CategoryHelper.addParentCategory(getCategoryLabel(broaderTerm), termPath);
            result.addAll(getPathsForVocabularyTerm(broaderTerm, broaderTermPath));
        }

        // Add all top level category paths for broader match terms in the classification scheme

        for (AodnTerm broaderTerm: classificationThesaurus.getRelatedTerms(term, SkosRelation.BROADER_MATCH)) {
            CategoryPath broaderTermPath = CategoryHelper.addParentCategory(getCategoryLabel(broaderTerm), termPath);
            result.addAll(getPathsForClassificationSchemeTerm( broaderTerm, broaderTermPath));
        }

        // Add all top level category paths for related match terms in the classification scheme

        for (AodnTerm broaderTerm: classificationThesaurus.getRelatedTerms(term, SkosRelation.RELATED_MATCH)) {
            result.addAll(getPathsForClassificationSchemeTerm(broaderTerm, termPath)); // don't add related match term as its the same as the term
        }

        return result;
    }

    /**
     * Get the category paths to top level terms in the classifications scheme for the term being classified
     * resulting from following broader/related relationships for the passed classification term with category
     * path termPath.
     *
     * @param term vocabulary term being examined for broader/broadMatch/relatedMatch relationships
     * @param termPath the category path from the term being classified to this term
     * @return a list of all the category paths of top level terms in the classification scheme sourced for this term
     */

    private List<CategoryPath> getPathsForClassificationSchemeTerm(AodnTerm term, CategoryPath termPath) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        if (classificationThesaurus.hasRelatedTerms(term, SkosRelation.BROADER)) {
            // Add all top level category paths for broader terms
            for (AodnTerm broaderTerm : classificationThesaurus.getRelatedTerms(term, SkosRelation.BROADER)) {
                CategoryPath broaderTermPath = CategoryHelper.addParentCategory(getCategoryLabel(broaderTerm), termPath);
                result.addAll(getPathsForClassificationSchemeTerm(broaderTerm, broaderTermPath));
            }
        } else {
            // No broader terms so this is a top level category that needs to be added to the list
            result.add(termPath);
        }

        return result;
    }

    private String getCategoryLabel(AodnTerm term) {
        if (term.getDisplayLabel() != null) {
            return term.getDisplayLabel();
        } else {
            return term.getPrefLabel();
        }
    }
}
