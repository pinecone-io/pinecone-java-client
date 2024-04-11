package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.Usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the response to a query, where the `ScoredVector` objects in the `matches` list
 * contain `SparseValuesWithUnsignedIndices` instead of the standard `SparseValues`.
 * <p>
 * The `QueryResponseWithUnsignedIndices` class contains the following fields:
 * - `matches`: a list of `ScoredVectorWithUnsignedIndices` objects, representing the matching vectors
 * - `namespace`: the namespace of the query
 * - `usage`: the usage information for the query
 * <p>
 * The class provides a constructor that takes a `QueryResponse` object and converts it to a
 * `QueryResponseWithUnsignedIndices` object, as well as getter and setter methods for each of the fields.
 * It also includes a method to convert a list of `ScoredVector` objects to a list of
 * `ScoredVectorWithUnsignedIndices` objects.
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
     * Constructs a `QueryResponseWithUnsignedIndices` object from a `QueryResponse` object, converting the
     * `ScoredVector` objects to `ScoredVectorWithUnsignedIndices` objects.
     *
     * @param queryResponse the `QueryResponse` object to convert
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
     * Returns the list of matching vectors, where each vector contains sparse values with unsigned 32-bit integer indices.
     *
     * @return the list of `ScoredVectorWithUnsignedIndices` objects
     */
    public List<ScoredVectorWithUnsignedIndices> getMatchesList() {
        return matches;
    }

    /**
     * Returns the `ScoredVectorWithUnsignedIndices` object at the specified index in the `matches` list.
     *
     * @param index the index of the `ScoredVectorWithUnsignedIndices` object to return
     * @return the `ScoredVectorWithUnsignedIndices` object at the specified index
     */
    public ScoredVectorWithUnsignedIndices getMatches(int index) {
        return matches.get(index);
    }

    /**
     * Converts a list of `ScoredVector` objects to a list of `ScoredVectorWithUnsignedIndices` objects.
     *
     * @param matches the list of `ScoredVector` objects to convert
     * @return the list of `ScoredVectorWithUnsignedIndices` objects
     * @throws IllegalArgumentException if the `matches` list is null
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
     * Sets the list of matching vectors, where each vector contains sparse values with unsigned 32-bit integer indices.
     *
     * @param matches the new list of `ScoredVectorWithUnsignedIndices` objects
     */
    public void setMatches(List<ScoredVectorWithUnsignedIndices> matches) {
        this.matches = matches;
    }

    /**
     * Returns the namespace of the query.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace of the query.
     *
     * @param namespace the new namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns the usage information for the query.
     *
     * @return the usage
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * Sets the usage information for the query.
     *
     * @param usage the new usage
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * Converts the given object to a string with each line indented by 4 spaces (except the first line).
     *
     * @param o the object to convert to a string
     * @return the indented string representation of the object
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
