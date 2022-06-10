
//Copyright (c) 2020-2021 Pinecone Systems Inc. All right reserved.


package io.pinecone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PineconePackageInfo {

    public static final Package INFO = PineconePackageInfo.class.getPackage();

    private static final Logger logger = LoggerFactory.getLogger(PineconePackageInfo.class);
    static {
        logger.debug("loaded pinecone package info: {}", INFO);
    }

    private static final String UNKOWN = "unknown";

    public static String clientVersion() {
        String version = INFO.getImplementationVersion();
        return version != null ? version : UNKOWN;
    }
}
