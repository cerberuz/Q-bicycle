package uk.gov.dvla.poc.bicycle.qldb;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the revision summary that appears in the {@link
 * BlockSummaryRecord}. Some revisions might not have a documentId. These are
 * internal-only system revisions that don't contain user data. Only the
 * revisions that do have a document ID are published in separate revision
 * details record.
 */
public final class RevisionSummary {

    private String documentId;
    private byte[] hash;

    @JsonCreator
    public RevisionSummary(@JsonProperty("documentId") String documentId, @JsonProperty("hash") byte[] hash) {
        this.documentId = documentId;
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "RevisionSummary{" +
                "documentId='" + documentId + '\'' +
                ", hash=" + Arrays.toString(hash) +
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

        RevisionSummary that = (RevisionSummary) o;

        if (!Objects.equals(documentId, that.documentId)) {
            return false;
        }
        return Arrays.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        int result = documentId != null ? documentId.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(hash);
        return result;
    }

    public String getDocumentId() {
        return documentId;
    }

    public byte[] getHash() {
        return hash;
    }
}

