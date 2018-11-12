package au.org.emii.classifier;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.ResultInterpreter;
import org.openrdf.sesame.query.QueryResultsTable;

import static au.org.emii.classifier.QueryResultsHelper.getColumnValue;

/**
 * Converts a thesaurus query result row containing a term uri (id), prefLabel and displayLabel into an
 * AODN vocab term
 */

public class AodnTermResultInterpreter extends ResultInterpreter<AodnTerm> {
    @Override
    public AodnTerm createFromRow(Thesaurus thesaurus, QueryResultsTable resultsTable, int row) {
        String uri = getColumnValue(resultsTable, row, "id");
        String prefLabel = getColumnValue(resultsTable, row, "prefLabel");
        String displayLabel = getColumnValue(resultsTable, row, "displayLabel");
        String replaces = getColumnValue(resultsTable, row, "replaces");
        String replacedBy = getColumnValue(resultsTable, row, "replacedBy");

        AodnTerm term = new AodnTerm(uri, prefLabel);

        term.setDisplayLabel(displayLabel);
        term.setReplaces(replaces);
        term.setReplacedBy(replacedBy);

        return term;
    }

}
