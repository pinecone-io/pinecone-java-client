package io.pinecone.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigureIndexRequest {
    private Integer replicas = 1;

    private String podType = "p1.x1";

    public ConfigureIndexRequest() {
    }

    public ConfigureIndexRequest withReplicas(Integer replicas) {
        this.replicas = replicas;
        return this;
    }

    /**
     * The number of replicas. Replicas duplicate your index. They provide higher availability and throughput.
     * @return replicas
     **/
    public Integer getReplicas() {
        return replicas;
    }

    public ConfigureIndexRequest withPodType(String podType) {
        this.podType = podType;
        return this;
    }

    /**
     * The type of pod to use. One of s1, p1, or p2 appended with . and one of x1, x2, x4, or x8.
     * @return podType
     **/
    public String getPodType() {
        return podType;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.createObjectNode()
                .put("replicas", replicas)
                .put("pod_type", podType);

        return jsonNode.toString();
    }
}
