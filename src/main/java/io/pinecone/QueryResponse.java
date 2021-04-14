package io.pinecone;

import io.pinecone.proto.Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryResponse extends PineconeResponse {

    public static class SingleQueryResults {
        private final float[] query;

        /**
         * Vector ids of the item results of the query.
         */
        private final List<String> ids;

        /**
         * Corresponding pairwise scores between the query to each item.
         */
        private final List<Float> scores;

        /**
         * Corresponding data for all of these items, if requested. E.g. data[0] gives the first
         * vector as a float[].
         */
        private final float[][] data;

        SingleQueryResults(float[] query, List<String> ids,
                           List<Float> scores, float[][] data) {
            this.query = query;
            this.ids = ids;
            this.scores = scores;
            this.data = data;
        }

        /**
         * @return See {@link SingleQueryResults#query}.
         */
        public float[] getQuery() {
            return query;
        }

        /**
         * @return See {@link SingleQueryResults#ids}.
         */
        public List<String> getIds() {
            return ids;
        }

        /**
         * @return See {@link SingleQueryResults#scores}.
         */
        public List<Float> getScores() {
            return scores;
        }

        /**
         * @return See {@link SingleQueryResults#data}.
         */
        public float[][] getData() {
            return data;
        }

        @Override
        public String toString() {
            return "SingleQueryResults("
                    + "query=" + Arrays.toString(getQuery())
                    + ", ids=" + getIds()
                    + ", scores=" + getScores()
                    + ", data=" + Arrays.deepToString(getData())
                    + ")";
        }
    }

    /**
     * A list of query results with one {@link SingleQueryResults} per query vector in the same
     * order as submitted in the {@link QueryRequest}.
     */
    private List<SingleQueryResults> queryResults;

    static QueryResponse from(Core.Request response, PineconeTranslator translator) {
        QueryResponse queryResponse = new QueryResponse();
        Core.QueryRequest queryContainer = response.getQuery();
        float[][] data = translator.translate(queryContainer.getData());

        queryResponse.queryResults = new ArrayList<>(data.length);

        List<Core.ScoredResults> matchesList = queryContainer.getMatchesList();
        for(int i = 0; i < matchesList.size(); i++) {
            Core.ScoredResults matches = matchesList.get(i);
            float[] query = data[i];
            SingleQueryResults singleQueryResults = new SingleQueryResults(
                    query,
                    matches.getIdsList(),
                    matches.getScoresList(),
                    translator.translate(matches.getData()));
            queryResponse.queryResults.add(singleQueryResults);
        }
        // TODO validate sizes look good
        return queryResponse;
    }

    public List<SingleQueryResults> getQueryResults() {
        return queryResults;
    }

    @Override
    public String toString() {
        return "QueryResponse("
                + "queryResults=" + getQueryResults()
                + ")";
    }
}
