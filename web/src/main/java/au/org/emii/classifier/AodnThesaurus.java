package au.org.emii.classifier;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.*;
import org.jdom.Namespace;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide AODN specific lookup methods methods on a wrapped GeoNetwork thesaurus returning AODN terms
 * instead of KeywordBeans
 */

public class AodnThesaurus {
    private static final String LANG_CODE = "en";
    private static final Namespace SKOS_NAMESPACE = Namespace.getNamespace("skos","http://www.w3.org/2004/02/skos/core#");
    private static final Namespace DCTERMS_NAMESPACE = Namespace.getNamespace("dcterms","http://purl.org/dc/terms/");

    private final Thesaurus thesaurus;

    public AodnThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getThesaurusTitle() {
        return thesaurus.getTitle();
    }

    public AodnTerm getTerm(String uri) {
        Query<AodnTerm> query = QueryBuilder.builder()
            .distinct(true)
            .select(Selectors.ID, true)
            .select(PREF_LABEL_SELECTOR, false)
            .select(DISPLAY_LABEL_SELECTOR, false)
            .where(idEquals(uri))
            .interpreter(new AodnTermResultInterpreter())
            .build();

        List<AodnTerm> aodnTerms = getTerms(query);

        if (aodnTerms.isEmpty()) {
            return null;
        }

        return aodnTerms.get(0);
    }

    public List<AodnTerm> getTermWithLabel(String label) {
        try {
            Query<AodnTerm> query = QueryBuilder.builder()
                .distinct(true)
                .select(Selectors.ID, true)
                .select(PREF_LABEL_SELECTOR, false)
                .select(ALT_LABEL_SELECTOR, false)
                .select(DISPLAY_LABEL_SELECTOR, false)
                .select(REPLACES_SELECTOR, false)
                .select(REPLACED_BY_SELECTOR, false)
                .where(prefLabelEquals(label).or(altLabelEquals(label)))
                .interpreter(new AodnTermResultInterpreter())
                .build();

            return getTerms(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasRelatedTerms(AodnTerm aodnTerm, SkosRelation relation) {
        return getRelatedTerms(aodnTerm, relation).size() > 0;
    }

    public List<AodnTerm> getRelatedTerms(AodnTerm aodnTerm, SkosRelation relationshipType) {
        Query<AodnTerm> query = QueryBuilder.builder()
            .distinct(true)
            .select(Selectors.ID, true)
            .select(relatedToTermSelector(aodnTerm, relationshipType), true)
            .select(PREF_LABEL_SELECTOR, false)
            .select(DISPLAY_LABEL_SELECTOR, false)
            .interpreter(new AodnTermResultInterpreter())
            .build();

        return getTerms(query);
    }

    private List<AodnTerm> getTerms(Query<AodnTerm> query) {
        try {
            List<AodnTerm> result = new ArrayList<AodnTerm>();

            // Add alternate labels to terms

            for (AodnTerm term: query.execute(thesaurus)) {
                List<String> altLabels = getAltLabels(term.getUri());
                term.setAltLabels(altLabels);
                result.add(term);
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getAltLabels(String uri) throws java.io.IOException, org.openrdf.sesame.query.MalformedQueryException, org.openrdf.sesame.query.QueryEvaluationException, org.openrdf.sesame.config.AccessDeniedException {
        Query<String> altLabelQuery = QueryBuilder.builder()
            .distinct(true)
            .select(Selectors.ID, true)
            .select(ALT_LABEL_SELECTOR, false)
            .where(idEquals(uri).and(ALT_LABEL_BOUND))
            .interpreter(new AltLabelResultInterpreter())
            .build();

        return altLabelQuery.execute(thesaurus);
    }

    /* SERQL selectors */

    private static final Selector PREF_LABEL_SELECTOR =
        new Selector("prefLabel", "{id} skos:prefLabel {prefLabel}", SKOS_NAMESPACE)
              .where(Wheres.ilike("lang(prefLabel)", LANG_CODE));

    private static final Selector DISPLAY_LABEL_SELECTOR =
        new Selector("displayLabel", "{id} DisplayLabelRelation {displayLabel}")
              .where(Wheres.ilike("localName(DisplayLabelRelation)", "displayLabel"));

    private static final Selector REPLACED_BY_SELECTOR =
        new Selector("replacedBy", "{id} dcterms:isReplacedBy {replacedBy}", DCTERMS_NAMESPACE);

    private static final Selector REPLACES_SELECTOR =
        new Selector("replaces", "{id} dcterms:replaces {replaces}", DCTERMS_NAMESPACE);

    private static final Selector ALT_LABEL_SELECTOR =
        new Selector("altLabel", "{id} skos:altLabel {altLabel}", SKOS_NAMESPACE)
          .where(Wheres.ilike("lang(altLabel)", LANG_CODE));

    private static Selector relatedToTermSelector(AodnTerm term, SkosRelation relationType) {
        // Return any terms referencing this term with the opposite relationType e.g. to find
        // broader terms look for terms referencing this term as a narrower term
        return new Selector("\"relatedConcept\"", "{id} skos:" + relationType.opposite()
            + " {<" + term.getUri() + ">}", SKOS_NAMESPACE);
    }

    /* SERQL where clauses */

    private static final Where ALT_LABEL_BOUND = bound("altLabel");

    private static Where idEquals(String uri) {
        return Wheres.ID(uri);
    }

    private static Where altLabelEquals(String label) {
        return Wheres.like("altLabel", label);
    }

    private Where prefLabelEquals(String label) {
        return Wheres.like("prefLabel", label);
    }

    private static Where bound(String columnName) {
        return new WhereClause(columnName + " != null");
    }

}
