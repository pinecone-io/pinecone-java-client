package io.pinecone.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexMetaDatabase {
    private String name;

    private Integer dimension;

    private String metric;

    private Integer pods;

    private Integer replicas;

    private Integer shards;

    @JsonProperty("pod_type")
    private String podType;

    private IndexMetadataConfig metadataConfig;

    public IndexMetaDatabase () {

    }

    public String getName() {
        return name;
    }

    public IndexMetaDatabase withName(String name) {
        this.name = name;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public IndexMetaDatabase withDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getMetric() {
        return metric;
    }

    public IndexMetaDatabase withMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public Integer getPods() {
        return pods;
    }

    public IndexMetaDatabase withPods(Integer pods) {
        this.pods = pods;
        return this;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public IndexMetaDatabase withReplicas(Integer replicas) {
        this.replicas = replicas;
        return this;
    }

    public Integer getShards() {
        return shards;
    }

    public IndexMetaDatabase withShards(Integer shards) {
        this.shards = shards;
        return this;
    }

    public String getPodType() {
        return podType;
    }

    public IndexMetaDatabase withPodType(String podType) {
        this.podType = podType;
        return this;
    }

    public IndexMetadataConfig getMetadataConfig() {
        return metadataConfig;
    }

    public IndexMetaDatabase withMetadataConfig(IndexMetadataConfig metadataConfig) {
        this.metadataConfig = metadataConfig;
        return this;
    }

    @Override
    public String toString() {
        return "IndexMetaDatabase{" +
                "name='" + name + '\'' +
                ", dimension=" + dimension +
                ", metric='" + metric + '\'' +
                ", pods=" + pods +
                ", replicas=" + replicas +
                ", shards=" + shards +
                ", podType='" + podType + '\'' +
                ", metadataConfig=" + metadataConfig +
                '}';
    }
}
