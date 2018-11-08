package au.org.emii.classifier;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selector;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.WhereClause;
import org.fao.geonet.kernel.rdf.Wheres;
import org.jdom.Namespace;

import java.util.List;

/**
 * Wraps access to the underlying GeoNetwork Thesauri returning AODN vocabulary terms instead of KeywordBeans (to include
 * displayLabels) and provides AODN specific lookup methods
 */

public class AodnThesaurus {
    private static final String LANG_CODE = "en";
    private static final Namespace SKOS_NAMESPACE = Namespace.getNamespace("skos","http://www.w3.org/2004/02/skos/core#");
    private static final Namespace DCTERMS_NAMESPACE = Namespace.getNamespace("dcterms","http://purl.org/dc/terms/");

    private final Thesaurus thesaurus;

    public AodnThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public AodnTerm getTerm(String uri) {
        try {
            List<AodnTerm> aodnTerms;

            Query<AodnTerm> query = QueryBuilder.builder()
                .distinct(true)
                .select(Selectors.ID, true)
                .select(PREF_LABEL_SELECTOR, false)
                .select(DISPLAY_LABEL_SELECTOR, false)
                .select(REPLACED_BY_SELECTOR, false)
                .where(notReplaced().and(idEquals(uri)))
                .interpreter(new AodnTermResultInterpreter())
                .build();

            aodnTerms = query.execute(thesaurus);

            if (aodnTerms.isEmpty()) {
                return null;
            }

            return aodnTerms.get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AodnTerm getTermWithLabel(String label) {
        try {
            List<AodnTerm> aodnTerms;

            Query<AodnTerm> query = QueryBuilder.builder()
                .distinct(true)
                .select(Selectors.ID, true)
                .select(PREF_LABEL_SELECTOR, false)
                .select(DISPLAY_LABEL_SELECTOR, false)
                .select(REPLACED_BY_SELECTOR, false)
                .select(ALT_LABEL_SELECTOR, false)
                .where(notReplaced().and(prefLabelEquals(label).or(altLabelEquals(label))))
                .interpreter(new AodnTermResultInterpreter())
                .build();

            aodnTerms = query.execute(thesaurus);

            if (aodnTerms.isEmpty()) {
                return null;
            }

            return aodnTerms.get(0);
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
            .select(REPLACED_BY_SELECTOR, false)
            .where(notReplaced())
            .interpreter(new AodnTermResultInterpreter())
            .build();

        try {
            return query.execute(thesaurus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getThesaurusTitle() {
        return thesaurus.getTitle();
    }

    private static final Selector PREF_LABEL_SELECTOR =
        new Selector("prefLabel", "{id} skos:prefLabel {prefLabel}", SKOS_NAMESPACE)
              .where(Wheres.ilike("lang(prefLabel)", LANG_CODE));

    private static final Selector DISPLAY_LABEL_SELECTOR =
        new Selector("displayLabel", "{id} DisplayLabelRelation {displayLabel}")
            .where(Wheres.ilike("localName(DisplayLabelRelation)", "displayLabel"));

    private static final Selector REPLACED_BY_SELECTOR =
        new Selector("replacedBy", "{id} dcterms:isReplacedBy {replacedBy}", DCTERMS_NAMESPACE);

    private static final Selector ALT_LABEL_SELECTOR =
        new Selector("altLabel", "{id} skos:altLabel {altLabel}", SKOS_NAMESPACE)
              .where(Wheres.ilike("lang(altLabel)", LANG_CODE));

    private static Selector relatedToTermSelector(AodnTerm term, SkosRelation relationType) {
        // Return any terms referencing this term with the opposite relationType e.g. to find
        // broader terms look for terms referencing this term as a narrower term
        return new Selector("\"relatedConcept\"", "{id} skos:" + relationType.opposite() + " {<" + term.getUri() + ">}", SKOS_NAMESPACE);
    }

    private static Where notReplaced() {
        return new WhereClause("replacedBy = null");
    }

    private static Where idEquals(String uri) {
        return Wheres.ID(uri);
    }

    private static Where altLabelEquals(String label) {
        return Wheres.like("altLabel", label);
    }

    private Where prefLabelEquals(String label) {
        return Wheres.like("prefLabel", label);
    }

}
