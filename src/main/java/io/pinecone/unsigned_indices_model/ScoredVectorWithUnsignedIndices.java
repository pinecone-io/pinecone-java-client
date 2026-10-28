package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;
import io.pinecone.proto.ScoredVector;

import java.util.Collections;
import java.util.List;

/**
 * One match from a query on {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.AsyncIndex}: the
 * vector's id, its similarity score, and its values and metadata if you asked for them.
 *
 * <p>The same as the generated {@link ScoredVector} class except that sparse indices are {@code Long}, so they
 * hold the full unsigned 32-bit range. You get these from
 * {@link QueryResponseWithUnsignedIndices#getMatchesList()}; you do not normally construct one.
 *
 * <pre>{@code
 * QueryResponseWithUnsignedIndices response =
 *     index.queryByVector(5, Arrays.asList(0.12f, -0.03f, 0.48f), "articles-en", false, true);
 * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
 *     System.out.println(match.getId() + " " + match.getScore() + " " + match.getMetadata());
 * }
 * }</pre>
 */
public class ScoredVectorWithUnsignedIndices {

    /**
     * The score associated with the vector.
     */
    private float score;

    /**
     * The identifier of the vector.
     */
    private String id;

    /**
     * The dense vector values.
     */
    private List<Float> values;

    /**
     * The metadata associated with the vector.
     */
    private Struct metadata;

    /**
     * The sparse values associated with the vector, using unsigned 32-bit integer indices.
     */
    private SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices;

    /**
     * Creates a match from the generated {@link ScoredVector} class, converting its sparse indices to
     * unsigned values. The query methods call this for you.
     *
     * <pre>{@code
     * ScoredVectorWithUnsignedIndices match = new ScoredVectorWithUnsignedIndices(scoredVector);
     * }</pre>
     *
     * @param scoredVector a match read from a gRPC query response; null gives a match with an empty id, a score
     *                     of 0 and empty values, metadata and sparse values
     */
    public ScoredVectorWithUnsignedIndices(ScoredVector scoredVector) {
        if (scoredVector == null) {
            this.score = 0F;
            this.id = "";
            this.values = Collections.emptyList();
            this.metadata = Struct.newBuilder().build();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices();
        } else {
            this.score = scoredVector.getScore();
            this.id = scoredVector.getId();
            this.values = scoredVector.getValuesList();
            this.metadata = scoredVector.getMetadata();
            this.sparseValuesWithUnsignedIndices = new SparseValuesWithUnsignedIndices(scoredVector.getSparseValues());
        }
    }

    /**
     * How similar the vector is to the query, computed with the index's metric. For cosine and dotproduct a
     * higher score is more similar; for euclidean a lower score is more similar.
     *
     * @return the similarity score
     */
    public float getScore() {
        return score;
    }

    /**
     * Replaces the score, for example in test data.
     *
     * @param score the similarity score
     */
    public void setScore(float score) {
        this.score = score;
    }

    /**
     * The id of the matching vector.
     *
     * @return the vector id, such as {@code "doc-42"}
     */
    public String getId() {
        return id;
    }

    /**
     * Replaces the id, for example in test data.
     *
     * @param id the vector id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The matching vector's dense values. The server returns them only when the query set
     * {@code includeValues} to true.
     *
     * @return the dense values, or an empty list if they were not requested
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Replaces the dense values, for example in test data.
     *
     * @param values the dense values
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * The matching vector's metadata. The server returns it only when the query set {@code includeMetadata} to
     * true.
     *
     * @return the metadata, or an empty {@link Struct} if it was not requested or the vector has none
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Replaces the metadata, for example in test data.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * The matching vector's sparse values. The server returns them only when the query set
     * {@code includeValues} to true.
     *
     * @return the sparse values; never null, with empty lists if they were not requested or the vector has none
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Replaces the sparse values, for example in test data.
     *
     * @param sparseValuesWithUnsignedIndices the sparse values
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * Converts the given object to a string with each line indented by 4 spaces (except the first line).
     *
     * @param o The object to convert to a string
     * @return The indented string representation of the object
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ScoredVectorWithUnsignedIndices {").append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    values: ").append(toIndentedString(values)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    sparseValuesWithUnsignedIndices: ").append(toIndentedString(sparseValuesWithUnsignedIndices)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
