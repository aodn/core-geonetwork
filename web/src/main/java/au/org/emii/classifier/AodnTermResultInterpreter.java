package au.org.emii.classifier;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.rdf.ResultInterpreter;
import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.Arrays;
import java.util.List;

/**
 * Converts a thesaurus query result row containing a term uri (id), prefLabel and displayLabel into an
 * AODN vocab term
 */

public class AodnTermResultInterpreter extends ResultInterpreter<AodnTerm> {
    @Override
    public AodnTerm createFromRow(Thesaurus thesaurus, QueryResultsTable resultsTable, int row) {
        String uri = getColumnValue(resultsTable, row, "id");
        String altLabel = getColumnValue(resultsTable, row, "altLabel");
        String prefLabel = getColumnValue(resultsTable, row, "prefLabel");
        String displayLabel = getColumnValue(resultsTable, row, "displayLabel");
        return new AodnTerm(uri, prefLabel, altLabel, displayLabel);
    }

    private String getColumnValue(QueryResultsTable resultsTable, int row, String columnName) {
        String result = null;
        Integer columnIdx = getColumnIndex(resultsTable, columnName);

        if (columnIdx != -1) {
            Value value = resultsTable.getValue(row, columnIdx);

            if (value != null) {
                result = value.toString();
            }
        }

        return result;
    }

    private Integer getColumnIndex(QueryResultsTable resultsTable, String columnName) {
        List<String> columnNames = Arrays.asList(resultsTable.getColumnNames());
        return columnNames.indexOf(columnName);
    }
}
