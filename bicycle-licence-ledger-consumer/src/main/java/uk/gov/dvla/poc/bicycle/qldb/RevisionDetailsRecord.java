package uk.gov.dvla.poc.bicycle.qldb;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a revision record on the QLDB stream. A revision details record
 * represents a document revision that is committed to your ledger. The payload
 * contains all of the attributes from the committed view of the revision, along
 * with the associated table name and table ID.
 */
public final class RevisionDetailsRecord implements StreamRecord.StreamRecordPayload {
    private TableInfo tableInfo;
    private Revision revision;

    public RevisionDetailsRecord(@JsonProperty("tableInfo") TableInfo tableInfo, @JsonProperty("revision") Revision revision) {
        this.tableInfo = tableInfo;
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RevisionDetailsRecord that = (RevisionDetailsRecord) o;

        if (!Objects.equals(tableInfo, that.tableInfo)) {
            return false;
        }
        return Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        int result = tableInfo != null ? tableInfo.hashCode() : 0;
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RevisionDetailsRecord{" +
                "tableInfo=" + tableInfo +
                ", revision=" + revision +
                '}';
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public Revision getRevision() {
        return revision;
    }
}
