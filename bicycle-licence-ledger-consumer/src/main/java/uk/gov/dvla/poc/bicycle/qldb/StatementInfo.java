package uk.gov.dvla.poc.bicycle.qldb;

import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.ion.IonTimestampSerializers;

/**
 * Contains information about an individual statement run as part of a transaction.
 */
public class StatementInfo {

    private String statement;
    @JsonSerialize(using = IonTimestampSerializers.IonTimestampJavaDateSerializer.class)
    private Date startTime;
    private byte[] statementDigest;

    @JsonCreator
    public StatementInfo(@JsonProperty("statement") final String statement,
                         @JsonProperty("startTime") final Date startTime,
                         @JsonProperty("statementDigest") final byte[] statementDigest) {
        this.statement = statement;
        this.startTime = startTime;
        this.statementDigest = statementDigest;
    }

    public String getStatement() {
        return statement;
    }

    public Date getStartTime() {
        return startTime;
    }

    public byte[] getStatementDigest() {
        return statementDigest;
    }

    @Override
    public String toString() {
        return "StatementInfo{"
                + "statement='" + statement + '\''
                + ", startTime=" + startTime
                + ", statementDigest=" + Arrays.toString(statementDigest)
                + '}';
    }
}