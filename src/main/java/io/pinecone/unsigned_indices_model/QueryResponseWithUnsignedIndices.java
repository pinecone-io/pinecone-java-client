package io.pinecone.unsigned_indices_model;

import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResponseWithUnsignedIndices {

    private List<ScoredVectorWithUnsignedIndices> matches;
    private String namespace;

    public QueryResponseWithUnsignedIndices(QueryResponse queryResponse) {
        if (queryResponse == null) {
            this.matches = Collections.emptyList();
            this.namespace = "";
        } else {
            this.matches = convertToScoredVectorWithUnsignedIndices(queryResponse.getMatchesList());
            this.namespace = queryResponse.getNamespace();
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
}
