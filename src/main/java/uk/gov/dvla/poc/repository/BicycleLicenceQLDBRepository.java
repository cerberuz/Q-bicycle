package uk.gov.dvla.poc.repository;

import com.amazon.ion.*;
import com.amazon.ion.system.IonReaderBuilder;
import com.amazon.ion.system.IonSystemBuilder;
import com.amazon.ion.system.IonTextWriterBuilder;
import com.amazonaws.services.qldb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.ion.IonObjectMapper;
import com.fasterxml.jackson.dataformat.ion.ionvalue.IonValueMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import software.amazon.qldb.PooledQldbDriver;
import software.amazon.qldb.QldbSession;
import software.amazon.qldb.Result;
import uk.gov.dvla.poc.model.BicycleLicence;
import uk.gov.dvla.poc.model.HistoryResult;
import uk.gov.dvla.poc.model.IntegrityInfo;
import uk.gov.dvla.poc.service.Verifier;
import uk.gov.dvla.poc.util.QLDBStringUtils;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BicycleLicenceQLDBRepository implements CrudRepository<BicycleLicence, String> {

    private IonValueMapper MAPPER = new IonValueMapper(IonSystemBuilder.standard().build());
    private ObjectMapper mapper = new ObjectMapper();

    private PooledQldbDriver pooledQldbDriver;
    private QldbSession qldbSession;

    {
        pooledQldbDriver = LedgerConnection.createPooledQldbDriver();
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public <S extends BicycleLicence> S save(S s) {
        qldbSession = LedgerConnection.createQldbSession();
        qldbSession.execute(txn -> {
            log.info("Inserting licence document in the {} table...", "licence");
            try {
                final String query = String.format("INSERT INTO %s ?", "licence");
                final IonValue ionDocument = (IonValue) MAPPER.writeValueAsIonValue(s);
                final List<IonValue> parameters = Collections.singletonList(ionDocument);
                Result result = txn.execute(query, parameters);
                final Iterator<IonValue> itr = result.iterator();
                final IonValue idResult = itr.next();
                System.out.println(idResult);
                System.out.println(idResult.toPrettyString());
                String docID = getDocId(idResult.toString());
                s.setId(docID);
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        });
        return s;
    }

    public <S extends BicycleLicence> S update(S s) {
        qldbSession = LedgerConnection.createQldbSession();
        qldbSession.execute(txn -> {
            log.info("Update licence document in the {} table...", "licence");
            try {
                final String query = String.format("UPDATE %s as l set l.name = ?, l.telephone = ?, l.penaltyPoints = ?, l.events=? where l.email = ?", "licence");
                final List<IonValue> parameters = new ArrayList<>();
                parameters.add(MAPPER.writeValueAsIonValue(s.getName()));
                parameters.add(MAPPER.writeValueAsIonValue(s.getTelephone()));
                parameters.add(MAPPER.writeValueAsIonValue(s.getPenaltyPoints()));
                parameters.add(MAPPER.writeValueAsIonValue(s.getEvents()));
                parameters.add(MAPPER.writeValueAsIonValue(s.getEmail()));

                Result result = txn.execute(query, parameters);
                final Iterator<IonValue> itr = result.iterator();
                final IonValue idResult = itr.next();
                String docID = getDocId(idResult.toString());
                s.setId(docID);
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        });
        return s;
    }

    @Override
    public <S extends BicycleLicence> Iterable<S> saveAll(Iterable<S> iterable) {
        return null;
    }

    @Override
    public Optional<BicycleLicence> findById(String s) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String s) {
        return false;
    }

    @Override
    public Iterable<BicycleLicence> findAll() {
        List<BicycleLicence> licences = new ArrayList<>();
        qldbSession = LedgerConnection.createQldbSession();
        qldbSession.execute(txn -> {
            final String query = String.format("SELECT * FROM %s", "licence");
            List<IonStruct> documents = toIonStructs(txn.execute(query));
            log.info("Got all records");
            for(IonStruct struct : documents) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    try (IonWriter jsonWriter = IonTextWriterBuilder.json().withPrettyPrinting().build(stringBuilder)) {
                        rewrite(struct.toString(), jsonWriter);
                    }
                    log.info(stringBuilder.toString());
                    BicycleLicence licence = mapper.readValue(stringBuilder.toString(), BicycleLicence.class);
                    licences.add(licence);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return licences;
    }

    public BicycleLicence findByEmail(String email) {
        List<BicycleLicence> licences = new ArrayList<>();
        qldbSession = LedgerConnection.createQldbSession();
        qldbSession.execute(txn -> {
            final String query = String.format("SELECT * FROM %s where email = ?", "licence");
            List<IonValue> parameters = null;
            try {
                parameters = Collections.singletonList(MAPPER.writeValueAsIonValue(email));
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<IonStruct> documents = toIonStructs(txn.execute(query, parameters));
            log.info("Got all records by email");
            for(IonStruct struct : documents) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    try (IonWriter jsonWriter = IonTextWriterBuilder.json().withPrettyPrinting().build(stringBuilder)) {
                        rewrite(struct.toString(), jsonWriter);
                    }
                    log.info(stringBuilder.toString());
                    BicycleLicence licence = mapper.readValue(stringBuilder.toString(), BicycleLicence.class);
                    licences.add(licence);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        if(licences.size() == 0) {
            return null;
        }
        else {
            return licences.get(0);
        }
    }

    @Override
    public Iterable<BicycleLicence> findAllById(Iterable<String> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(String s) {

    }

    @Override
    public void delete(BicycleLicence licence) {

    }

    @Override
    public void deleteAll(Iterable<? extends BicycleLicence> iterable) {

    }

    @Override
    public void deleteAll() {

    }

    private String getDocId(String value) {
        String docId = "";
        try {
            String[] split = value.toString().split(":");
            docId = split[1];
            docId = docId.substring(1, docId.lastIndexOf("\""));
        }
        catch(Exception e) {

        }
        return docId;
    }

    /**
     * Convert the result set into a list of {@link IonStruct}.
     *
     * @param result
     *              {@link Result} from executing a query.
     * @return a list of documents in IonStruct.
     */
    public static List<IonStruct> toIonStructs(final Result result) {
        final List<IonStruct> documentList = new ArrayList<>();
        result.iterator().forEachRemaining(row -> documentList.add((IonStruct) row));
        return documentList;
    }

    public void rewrite(String textIon, IonWriter writer) throws IOException {
        IonReader reader = IonReaderBuilder.standard().build(textIon);
        writer.writeValues(reader);
    }

    private IonValue getDocumentId (final String email) {
        log.info("Getting document id ----");
        final String query = "select metadata.id as docId from _ql_committed_licence where data.email= ? ";
        log.info("Getting document id for " + email + query);
        qldbSession = LedgerConnection.createQldbSession();
        IonValue documentId = qldbSession.execute(txn -> {
            try {

                final List<IonValue> parameters = Collections.singletonList(MAPPER.writeValueAsIonValue(email));
                final Result result = txn.execute(query, parameters);

                log.info("Document Id result " + result);
                List<IonStruct> docList = new ArrayList<>();
                result.iterator().forEachRemaining(row -> {
                    log.info("Ion struct " + row);
                    docList.add((IonStruct) row);
                });
                return docList.get(0).get("docId");
            }
            catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        });
        return documentId;
    }

    public List<HistoryResult> findAllRevisionsByEmail(final String email) {
        final IonValue documentId = getDocumentId(email);
        log.info("Getting revisions for document ", documentId.toPrettyString());
        return findAllRevisions(documentId);
    }

    public List<HistoryResult> findAllRevisionsByDocumentId(final String documentId) {
        final IonValue documentIdAsIon;
        try {
            documentIdAsIon = MAPPER.writeValueAsIonValue(documentId);
            log.info("Getting revisions for document ", documentIdAsIon.toPrettyString());
            return findAllRevisions(documentIdAsIon);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Query the registration history for the document id.
     */
    private List<HistoryResult> findAllRevisions(final IonValue documentId) {

        final String query = String.format("SELECT * FROM history(%s) WHERE metadata.id = ?", "licence");
        List<HistoryResult> list;
        qldbSession = LedgerConnection.createQldbSession();
        list = qldbSession.execute(txn -> {
            final List<IonValue> parameters = Collections.singletonList(documentId);
            final Result result = txn.execute(query, parameters);
            final List<IonStruct> structList = toIonStructs(result);
            List<HistoryResult> historyResults = new ArrayList<>();
            for(IonStruct struct : structList) {
                HistoryResult historyResult = new HistoryResult();
                try {
                    IonStruct blockStruct = MAPPER.parse(struct.get("blockAddress"), IonStruct.class);
                    historyResult.setBlockAddress(blockStruct);
                    IonValue strandId = MAPPER.parse(blockStruct.get("strandId"), IonValue.class);
                    String strandAsString = MAPPER.parse(strandId, String.class);
                    IonValue sequenceNo = MAPPER.parse(blockStruct.get("sequenceNo"), IonValue.class);
                    int sequenceInt = MAPPER.parse(sequenceNo, int.class);
                    IonBlob hashBlob = (IonBlob) struct.get("hash");

                    IntegrityInfo info = new IntegrityInfo();
                    info.setStrandId(strandAsString);
                    info.setSequenceNo(sequenceInt);
                    info.setHash(hashBlob);
                    historyResult.setIntegrityInfo(info);

                    IonStruct metadataStruct = MAPPER.parse(struct.get("metadata"), IonStruct.class);
                    IonValue transactionId = MAPPER.parse(metadataStruct.get("txId"), IonValue.class);
                    Timestamp transactionTime = MAPPER.parse(metadataStruct.get("txTime"), Timestamp.class);
                    IonValue version = MAPPER.parse(metadataStruct.get("version"), IonValue.class);
                    IonValue id = MAPPER.parse(metadataStruct.get("id"), IonValue.class);
                    historyResult.setTransactionId(MAPPER.parse(transactionId, String.class));
                    historyResult.setTransactionTime(transactionTime.toString());
                    historyResult.setVersion(MAPPER.parse(version, int.class));
                    historyResult.setDocumentId(MAPPER.parse(id, String.class));

                    IonStruct dataStruct = MAPPER.parse(struct.get("data"), IonStruct.class);
                    if(dataStruct != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        try (IonWriter jsonWriter = IonTextWriterBuilder.json().withPrettyPrinting().build(stringBuilder)) {
                            rewrite(dataStruct.toString(), jsonWriter);
                        }
                        log.info(stringBuilder.toString());
                        BicycleLicence licence = mapper.readValue(stringBuilder.toString(), BicycleLicence.class);

                        historyResult.setLicence(licence);
                    }
                    else {
                        BicycleLicence licence = new BicycleLicence();
                        licence.setName("DELETED");
                        licence.setPenaltyPoints(0);
                        licence.setTelephone("DELETED");
                        licence.setEmail("DELETED");
                        historyResult.setLicence(licence);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
                historyResults.add(historyResult);
            }
            return historyResults;

        });
        return list;
    }

    public boolean verifyRevision(String documentId, IonStruct blockAddress, IonBlob hash) {

        GetDigestResult digest = getDigest();
        GetRevisionRequest request = new GetRevisionRequest()
                .withName(LedgerConstants.LEDGER_NAME)
                .withDigestTipAddress(digest.getDigestTipAddress())
                .withBlockAddress(new ValueHolder().withIonText(blockAddress.toPrettyString()))
                .withDocumentId(documentId);

        GetRevisionResult result = LedgerConnection.createQldbClient().getRevision(request);

        Proof proof = Proof.fromBlob(result.getProof().getIonText());
        byte[] digestBytes = Verifier.convertByteBufferToByteArray(digest.getDigest());

        byte[] candidateDigest = Verifier.buildCandidateDigest(proof, hash.getBytes());

        return Arrays.equals(digestBytes, candidateDigest);


    }

    public static GetDigestResult getDigest() {
        log.info("Let's get the current digest of the ledger named {}.", LedgerConstants.LEDGER_NAME);
        GetDigestRequest request = new GetDigestRequest()
                .withName(LedgerConstants.LEDGER_NAME);
        GetDigestResult result = LedgerConnection.createQldbClient().getDigest(request);
        log.info("Success. LedgerDigest: {}.", QLDBStringUtils.toUnredactedString(result));
        return result;
    }

    /**
     * Build the candidate digest representing the entire ledger from the internal hashes of the {@link Proof}.
     *
     * @param proof
     *              A Java representation of {@link Proof}
     *              returned from {@link com.amazonaws.services.qldb.AmazonQLDB#getRevision}.
     * @param leafHash
     *              Leaf hash to build the candidate digest with.
     * @return a byte array of the candidate digest.
     */
    private static byte[] buildCandidateDigest(final Proof proof, final byte[] leafHash) {
        return calculateRootHashFromInternalHashes(proof.getInternalHashes(), leafHash);
    }

    /**
     * Starting with the provided {@code leafHash} combined with the provided {@code internalHashes}
     * pairwise until only the root hash remains.
     *
     * @param internalHashes
     *              Internal hashes of Merkle tree.
     * @param leafHash
     *              Leaf hashes of Merkle tree.
     * @return the root hash.
     */
    public static byte[] calculateRootHashFromInternalHashes(final List<byte[]> internalHashes, final byte[] leafHash) {
        return internalHashes.stream().reduce(leafHash, Verifier::joinHashesPairwise);
    }
}
