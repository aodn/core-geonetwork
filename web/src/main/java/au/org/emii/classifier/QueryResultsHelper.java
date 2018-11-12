package au.org.emii.classifier;

import org.openrdf.model.Value;
import org.openrdf.sesame.query.QueryResultsTable;

import java.util.Arrays;
import java.util.List;

public class QueryResultsHelper {
    public static String getColumnValue(QueryResultsTable resultsTable, int row, String columnName) {
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

    private static Integer getColumnIndex(QueryResultsTable resultsTable, String columnName) {
        List<String> columnNames = Arrays.asList(resultsTable.getColumnNames());
        return columnNames.indexOf(columnName);
    }
}
