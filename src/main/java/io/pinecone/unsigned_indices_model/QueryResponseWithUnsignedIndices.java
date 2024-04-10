package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.Usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a query response with unsigned indices, allowing manipulation and conversion of scored vectors.
 * This class is designed to work with ScoredVectorWithUnsignedIndices objects.
 */
public class QueryResponseWithUnsignedIndices {

    private List<ScoredVectorWithUnsignedIndices> matches;
    private String namespace;
    private Usage usage;

    /**
     * Constructs a QueryResponseWithUnsignedIndices object based on the provided QueryResponse.
     * @param queryResponse The QueryResponse object.
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
     * Retrieves the list of scored vectors with unsigned indices.
     * @return The list of scored vectors.
     */
    public List<ScoredVectorWithUnsignedIndices> getMatchesList() {
        return matches;
    }

    /**
     * Retrieves the scored vector with unsigned indices at the specified index.
     * @param index The index of the scored vector to retrieve.
     * @return The scored vector at the specified index.
     */
    public ScoredVectorWithUnsignedIndices getMatches(int index) {
        return matches.get(index);
    }

    /**
     * Converts a list of ScoredVector objects to a list of ScoredVectorWithUnsignedIndices objects.
     * @param matches The list of ScoredVector objects to convert.
     * @return The list of ScoredVectorWithUnsignedIndices objects.
     * @throws IllegalArgumentException if the matches list is null.
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
     * Sets the list of scored vectors with unsigned indices.
     * @param matches The list of scored vectors to set.
     */
    public void setMatches(List<ScoredVectorWithUnsignedIndices> matches) {
        this.matches = matches;
    }

    /**
     * Retrieves the namespace.
     * @return The namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     * @param namespace The namespace to set.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Retrieves the usage.
     * @return The usage.
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * Sets the usage.
     * @param usage The usage to set.
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * Converts the object to a string with each line indented by 4 spaces (except the first line).
     * @param o The object to convert to string.
     * @return The indented string representation of the object.
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Generates a string representation of the QueryResponseWithUnsignedIndices object.
     * @return The string representation of the object.
     */
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
