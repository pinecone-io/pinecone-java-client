package io.pinecone;

import io.pinecone.proto.Core;

import java.util.concurrent.ThreadLocalRandom;

public abstract class PineconeRequest {

    protected static final String READ_PATH = "read";
    protected static final String WRITE_PATH = "write";

    public enum DTYPE {
        FLOAT32;

        public String dtypeName() {
            // e.g. "FLOAT32" -> "float32"
            return name().toLowerCase();
        }
    }

    protected PineconeTranslator translator;

    protected Integer serverSideTimeout;

    /**
     * Converts a request to a internal protobuf model instance. Primarily for internal use.
     * @return A Core.Request instance that can be sent over a gRPC stub.
     */
    Core.Request toRequest() {
        Core.Request.Builder builder = Core.Request.newBuilder();
        builder = customizeBaseRequest(builder)
                .setRequestId(generateRequestId())
                .setVersion(PineconePackageInfo.clientVersion());
        if (serverSideTimeout != null) builder.setTimeout(serverSideTimeout);
        return builder.build();
    }

    static <R extends PineconeRequest> R usingTranslator(PineconeTranslator translator, R request) {
        request.translator = translator;
        return request;
    }

    static <R extends PineconeRequest> R usingServerSideTimeout(int serverSideTimeout, R request) {
        request.serverSideTimeout = serverSideTimeout;
        return request;
    }

    protected abstract Core.Request.Builder customizeBaseRequest(Core.Request.Builder base);

    private long generateRequestId() {
        return ThreadLocalRandom.current().nextLong();
    }
}
