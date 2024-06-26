package uk.gov.dvla.poc.bicycle.qldb;


import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about the transaction. Contains all the statements executed as
 * part of the transaction and mapping between the documents to
 * tableName/tableId which were updated as part of the transaction.
 */
public class TransactionInfo {

    private List<StatementInfo> statements;
    private Map<String, DocumentInfo> documents;

    @JsonCreator
    public TransactionInfo(@JsonProperty("statements") final List<StatementInfo> statements,
                           @JsonProperty("documents") final Map<String, DocumentInfo> documents) {
        this.statements = statements;
        this.documents = documents;
    }

    public List<StatementInfo> getStatements() {
        return statements;
    }

    public Map<String, DocumentInfo> getDocuments() {
        return documents;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionInfo)) {
            return false;
        }

        final TransactionInfo that = (TransactionInfo) o;

        if (getStatements() != null ? !getStatements().equals(that.getStatements()) : that.getStatements() != null) {
            return false;
        }
        return getDocuments() != null ? getDocuments().equals(that.getDocuments()) : that.getDocuments() == null;
    }

    @Override
    public int hashCode() {
        int result = getStatements() != null ? getStatements().hashCode() : 0;
        result = 31 * result + (getDocuments() != null ? getDocuments().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TransactionInfo{"
                + "statements=" + statements
                + ", documents=" + documents
                + '}';
    }
}