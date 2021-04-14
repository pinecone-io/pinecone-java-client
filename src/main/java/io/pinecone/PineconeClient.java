package io.pinecone;

/**
 * Top-level client for connecting and making calls to Pinecone services. One instance can
 * be used to connect to multiple services and shared across threads.
 */
public final class PineconeClient {

    private final PineconeClientConfig config;

    /**
     * Create a new instance. Throws {@link PineconeValidationException} if configuration is invalid.
     * @param config User-level configuration for the client.
     */
    public PineconeClient(PineconeClientConfig config) {
        config.validate();
        this.config = config;
    }

    /**
     * Create a new connection to the specified Pinecone service or router. Throws {@link PineconeValidationException} if configuration is invalid.
     *
     * @param serviceAuthority URL authority of the Pinecone service or router to access. E.g. "10.1.2.3:3456".
     * @param serviceName Service or router name to connect to.
     * @return A {@link PineconeConnection} for the service or router.
     */
    public PineconeConnection connect(String serviceAuthority, String serviceName) {
        return connect(new PineconeConnectionConfig()
                .withServiceAuthority(serviceAuthority)
                .withServiceName(serviceName));
    }

    /**
     * Create a new connection to the Pinecone service or router specified in the config. Throws {@link PineconeValidationException} if configuration is invalid.
     * @param connectionConfig Config for the connection to be opened.
     * @return A {@link PineconeConnection} for the service or router.
     */
    public PineconeConnection connect(PineconeConnectionConfig connectionConfig) {
        return new PineconeConnection(config, connectionConfig);
    }

    /**
     * Create a new info request. TODO: make this public when ready.
     * @return An InfoRequest.
     */
    private InfoRequest infoRequest() {
        InfoRequest request = PineconeRequest.usingTranslator(config.getTranslator(), new InfoRequest());
        return PineconeRequest.usingServerSideTimeout(config.getServerSideTimeoutSec(), request);
    }

    /**
     * Create a new upsert request.
     * @return An UpsertRequest.
     */
    public UpsertRequest upsertRequest() {
        UpsertRequest request = PineconeRequest.usingTranslator(config.getTranslator(), new UpsertRequest());
        return PineconeRequest.usingServerSideTimeout(config.getServerSideTimeoutSec(), request);
    }

    /**
     * Create a new fetch request. TODO: make this public when ready.
     * @return A FetchRequest.
     */
    private FetchRequest fetchRequest() {
        FetchRequest request = PineconeRequest.usingTranslator(config.getTranslator(), new FetchRequest());
        return PineconeRequest.usingServerSideTimeout(config.getServerSideTimeoutSec(), request);
    }

    /**
     * Create a new query request.
     * @return A QueryRequest
     */
    public QueryRequest queryRequest() {
        QueryRequest request = PineconeRequest.usingTranslator(config.getTranslator(), new QueryRequest());
        return PineconeRequest.usingServerSideTimeout(config.getServerSideTimeoutSec(), request);
    }

    /**
     * Create a new delete request.TODO: make this public when ready.
     * @return A DeleteRequest.
     */
    private DeleteRequest deleteRequest() {
        DeleteRequest request = PineconeRequest.usingTranslator(config.getTranslator(), new DeleteRequest());
        return PineconeRequest.usingServerSideTimeout(config.getServerSideTimeoutSec(), request);
    }
}