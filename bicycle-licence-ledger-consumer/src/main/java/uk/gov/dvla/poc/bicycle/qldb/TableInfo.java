package uk.gov.dvla.poc.bicycle.qldb;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents the table information that goes inside the {@link
 * RevisionDetailsRecord}. It allows the users to deserialize the {@link
 * Revision#data} appropriate to the underlying table.
 */
public final class TableInfo {
    private String tableId;
    private String tableName;

    @JsonCreator
    public TableInfo(@JsonProperty("tableId") String tableId, @JsonProperty("tableName") String tableName) {
        this.tableId = tableId;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableId() {
        return tableId;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "tableId='" + tableId + '\'' +
                ", tableName='" + tableName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableInfo tableInfo = (TableInfo) o;

        if (!Objects.equals(tableId, tableInfo.tableId)) {
            return false;
        }
        return Objects.equals(tableName, tableInfo.tableName);
    }

    @Override
    public int hashCode() {
        int result = tableId != null ? tableId.hashCode() : 0;
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        return result;
    }
}