package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.Usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of a query on {@link io.pinecone.clients.Index} or {@link io.pinecone.clients.AsyncIndex}: the
 * matching vectors, best match first, plus the namespace searched and the read units used.
 *
 * <p>Returned by the {@code query}, {@code queryByVector} and {@code queryByVectorId} methods (wrapped in a
 * {@code ListenableFuture} on {@code AsyncIndex}); you do not normally construct one. It is the same as the
 * generated {@link QueryResponse} class except that sparse indices in the matches are {@code Long}, so they hold the
 * full unsigned 32-bit range.
 *
 * <pre>{@code
 * QueryResponseWithUnsignedIndices response =
 *     index.queryByVector(5, Arrays.asList(0.12f, -0.03f, 0.48f), "articles-en", false, true);
 * for (ScoredVectorWithUnsignedIndices match : response.getMatchesList()) {
 *     System.out.println(match.getId() + " " + match.getScore());
 * }
 * System.out.println("Read units: " + response.getUsage().getReadUnits());
 * }</pre>
 */
public class QueryResponseWithUnsignedIndices {

    /**
     * The list of matching vectors, where each vector contains sparse values with unsigned 32-bit integer indices.
     */
    private List<ScoredVectorWithUnsignedIndices> matches;

    /**
     * The namespace of the query.
     */
    private String namespace;

    /**
     * The usage information for the query.
     */
    private Usage usage;

    /**
     * Creates a result from the generated {@link QueryResponse} class, converting the sparse indices in each
     * match to unsigned values. The query methods call this for you.
     *
     * <pre>{@code
     * QueryResponseWithUnsignedIndices response = new QueryResponseWithUnsignedIndices(queryResponse);
     * }</pre>
     *
     * @param queryResponse a query response read from gRPC; null gives no matches, an empty namespace and a null
     *                      usage
     */
    public QueryResponseWithUnsignedIndices(QueryResponse queryResponse) {
        if (queryResponse == null) {
            this.matches = Collections.emptyList();
            this.namespace = "";
            this.usage = null;
        } else {
            this.matches = convertToScoredVectorWithUnsignedIndices(queryResponse.getMatchesList());
            this.namespace = queryResponse.getNamespace();
            this.usage = queryResponse.getUsage();
        }
    }

    /**
     * The matching vectors, best match first, at most {@code topK} of them.
     *
     * @return the matches; empty if nothing matched
     */
    public List<ScoredVectorWithUnsignedIndices> getMatchesList() {
        return matches;
    }

    /**
     * The match at a position in {@link #getMatchesList()}; {@code getMatches(0)} is the best match.
     *
     * @param index the position, starting at 0
     * @return the match at that position
     * @throws IndexOutOfBoundsException if there are not that many matches
     */
    public ScoredVectorWithUnsignedIndices getMatches(int index) {
        return matches.get(index);
    }

    /**
     * Converts matches from the generated {@link ScoredVector} class, as the constructor does. Callers do not
     * normally need it.
     *
     * <pre>{@code
     * List<ScoredVectorWithUnsignedIndices> matches =
     *     response.convertToScoredVectorWithUnsignedIndices(queryResponse.getMatchesList());
     * }</pre>
     *
     * @param matches matches read from a gRPC query response
     * @return the converted matches, in the same order
     * @throws IllegalArgumentException if {@code matches} is null
     */
    public List<ScoredVectorWithUnsignedIndices> convertToScoredVectorWithUnsignedIndices(List<ScoredVector> matches) {
        if (matches == null) {
            throw new IllegalArgumentException("Matches list cannot be null.");
        }
        List<ScoredVectorWithUnsignedIndices> scoredVectorList = new ArrayList<>(matches.size());
        for (ScoredVector scoredVector : matches) {
            scoredVectorList.add(new ScoredVectorWithUnsignedIndices(scoredVector));
        }
        return scoredVectorList;
    }

    /**
     * Replaces the matches, for example in test data.
     *
     * @param matches the matches, best match first
     */
    public void setMatches(List<ScoredVectorWithUnsignedIndices> matches) {
        this.matches = matches;
    }

    /**
     * The namespace the query searched.
     *
     * @return the namespace name, such as {@code "articles-en"}
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Replaces the namespace, for example in test data.
     *
     * @param namespace the namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * What the query cost; {@link Usage#getReadUnits()} gives the read units it used.
     *
     * @return the usage, or null if the result was created from a null response
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * Replaces the usage, for example in test data.
     *
     * @param usage the usage
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
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
        sb.append("class QueryResponseWithUnsignedIndices {").append("\n");
        sb.append("    matches: ").append(toIndentedString(matches)).append("\n");
        sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
        sb.append("    usage: ").append(usage).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
