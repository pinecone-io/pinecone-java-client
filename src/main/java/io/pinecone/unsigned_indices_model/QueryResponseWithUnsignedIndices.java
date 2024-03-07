package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import io.pinecone.proto.Usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.pinecone.utils.StringUtils.toIndentedString;

public class QueryResponseWithUnsignedIndices {

    private List<ScoredVectorWithUnsignedIndices> matches;
    private String namespace;
    private Usage usage;

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

    public List<ScoredVectorWithUnsignedIndices> getMatchesList() {
        return matches;
    }

    public ScoredVectorWithUnsignedIndices getMatches(int index) {
        return matches.get(index);
    }

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

    public void setMatches(List<ScoredVectorWithUnsignedIndices> matches) {
        this.matches = matches;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Usage getUsage() { return usage; }

    public void setUsage(Usage usage) { this.usage = usage; }

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
