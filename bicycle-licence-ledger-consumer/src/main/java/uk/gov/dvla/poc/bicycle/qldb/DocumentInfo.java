package uk.gov.dvla.poc.bicycle.qldb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about an individual document in a table.
 */
public class DocumentInfo {

    private String tableName;
    private String tableId;
    private List<Integer> statementIndexList;

    @JsonCreator
    public DocumentInfo(@JsonProperty("tableName") final String tableName,
                        @JsonProperty("tableId") final String tableId,
                        @JsonProperty("statements") final List<Integer> statementIndexList) {
        this.tableName = tableName;
        this.tableId = tableId;
        this.statementIndexList = statementIndexList;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableId() {
        return tableId;
    }

    public List<Integer> getStatementIndexList() {
        return statementIndexList;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentInfo)) {
            return false;
        }

        final DocumentInfo that = (DocumentInfo) o;

        if (!getTableName().equals(that.getTableName())) {
            return false;
        }
        if (!getTableId().equals(that.getTableId())) {
            return false;
        }
        return getStatementIndexList().equals(that.getStatementIndexList());
    }

    @Override
    public int hashCode() {
        int result = getTableName().hashCode();
        result = 31 * result + getTableId().hashCode();
        result = 31 * result + getStatementIndexList().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DocumentInfo{"
                + "tableName='" + tableName + '\''
                + ", tableId='" + tableId + '\''
                + ", statementIndexList=" + statementIndexList + '}';
    }
}

