package io.pinecone.unsigned_indices_model;

import com.google.protobuf.Struct;

import java.util.List;

/**
 * A vector to upsert with {@link io.pinecone.clients.Index#upsert(List, String)} or the same method on
 * {@link io.pinecone.clients.AsyncIndex}: an id, dense values, and optional metadata and sparse values.
 *
 * <p>Sparse indices are {@code Long} so they can hold the full unsigned 32-bit range, 0 to 4294967295. Use this
 * class to upsert many vectors in one request; to upsert one vector, the {@code upsert} overloads that take the
 * fields directly are shorter. {@link io.pinecone.commons.IndexInterface#buildUpsertVectorWithUnsignedIndices buildUpsertVectorWithUnsignedIndices}
 * builds one and checks its fields in the same call.
 *
 * <pre>{@code
 * List<VectorWithUnsignedIndices> vectors = new ArrayList<>();
 * vectors.add(new VectorWithUnsignedIndices("doc-1", Arrays.asList(0.12f, -0.03f, 0.48f)));
 * vectors.add(new VectorWithUnsignedIndices("doc-2", Arrays.asList(0.05f, 0.21f, -0.17f)));
 * index.upsert(vectors, "articles-en");
 * }</pre>
 */
public class VectorWithUnsignedIndices {

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
     * Creates a vector with every field unset; set at least the id and values before you upsert it.
     */
    public VectorWithUnsignedIndices() {}

    /**
     * Creates a vector with an id and dense values, and no metadata or sparse values.
     *
     * <pre>{@code
     * VectorWithUnsignedIndices vector =
     *     new VectorWithUnsignedIndices("doc-1", Arrays.asList(0.12f, -0.03f, 0.48f));
     * }</pre>
     *
     * @param id     the vector's id, unique within its namespace, such as {@code "doc-1"}; upserting an existing id
     *               overwrites that vector
     * @param values the dense values, one per dimension of the index; for a vector in a sparse index pass an
     *               empty list, not null
     */
    public VectorWithUnsignedIndices(String id, List<Float> values) {
        this.id = id;
        this.values = values;
    }

    /**
     * Creates a vector with an id, dense values, metadata and sparse values.
     *
     * <pre>{@code
     * Struct metadata = Struct.newBuilder()
     *     .putFields("genre", Value.newBuilder().setStringValue("documentary").build())
     *     .build();
     * SparseValuesWithUnsignedIndices sparse = new SparseValuesWithUnsignedIndices(
     *     Arrays.asList(102L, 4017L), Arrays.asList(0.62f, 0.31f));
     * VectorWithUnsignedIndices vector = new VectorWithUnsignedIndices(
     *     "doc-1", Arrays.asList(0.12f, -0.03f, 0.48f), metadata, sparse);
     * }</pre>
     *
     * @param id                              the vector's id, unique within its namespace, such as {@code "doc-1"}
     * @param values                          the dense values, one per dimension of the index; for a vector in a
     *                                        sparse index pass an empty list, not null
     * @param metadata                        key-value pairs you can filter queries on, such as a genre or year;
     *                                        null for none
     * @param sparseValuesWithUnsignedIndices the sparse values, such as output of a sparse embedding model; null for none
     */
    public VectorWithUnsignedIndices(String id, List<Float> values, Struct metadata, SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }

    /**
     * The vector's id, unique within its namespace.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the vector's id; upsert rejects a null or empty id with a
     * {@link io.pinecone.exceptions.PineconeValidationException}.
     *
     * @param id the id, such as {@code "doc-1"}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The dense values, one per dimension of the index.
     *
     * @return the dense values, or null if never set
     */
    public List<Float> getValuesList() {
        return values;
    }

    /**
     * Sets the dense values.
     *
     * @param values one value per dimension of the index; for a vector in a sparse index pass an empty list,
     *               not null
     */
    public void setValues(List<Float> values) {
        this.values = values;
    }

    /**
     * The key-value pairs stored with the vector, which queries can filter on.
     *
     * @return the metadata, or null if none was set
     */
    public Struct getMetadata() {
        return metadata;
    }

    /**
     * Sets the key-value pairs stored with the vector.
     *
     * @param metadata the metadata, such as a genre or year; null for none
     */
    public void setMetadata(Struct metadata) {
        this.metadata = metadata;
    }

    /**
     * The sparse values stored with the vector.
     *
     * @return the sparse values, or null if none were set
     */
    public SparseValuesWithUnsignedIndices getSparseValuesWithUnsignedIndices() {
        return sparseValuesWithUnsignedIndices;
    }

    /**
     * Sets the sparse values stored with the vector.
     *
     * @param sparseValuesWithUnsignedIndices the sparse values; null for none
     */
    public void setSparseValuesWithUnsignedIndices(SparseValuesWithUnsignedIndices sparseValuesWithUnsignedIndices) {
        this.sparseValuesWithUnsignedIndices = sparseValuesWithUnsignedIndices;
    }
}
