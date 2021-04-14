# java-basic-mvn
To build and run (Java and Maven required):
```
# Either this (basic example):
mvn package exec:java -Dexec.mainClass="pineconeexamples.MinimalUpsertAndQueryExample" -Dpinecone.apikey=<your-api-key> -Dpinecone.service.name=<service-name> -Dpinecone.service.url_authority=<service-url-authority>

# Or (concurrent example)
mvn package exec:java -Dexec.mainClass="pineconeexamples.UpsertsAndQueriesConcurrentExample" -Dpinecone.apikey=<your-api-key> -Dpinecone.service.name=<service-name> -Dpinecone.service.url_authority=<service-url-authority>
```