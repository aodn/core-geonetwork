package org.fao.geonet.kernel.search.facet;

public class Facet {
    /**
     * Default number of values for a facet
     */
    public static final int DEFAULT_MAX_KEYS = 10;
    /**
     * Max number of values for a facet
     */
    public static final int MAX_SUMMARY_KEY = 1000;
    /**
     * Define the sorting order of a facet.
     */
    public enum SortBy {
        /**
         * Use a text comparator for sorting values
         */
        VALUE, 
        /**
         * Use a numeric compartor for sorting values
         */
        NUMVALUE, 
        /**
         * Sort by count
         */
        COUNT
    }

    public enum SortOrder {
        ASCENDIND, DESCENDING
    }
}