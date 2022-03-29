# java-basic-mvn
To build and run (Java and Maven required):
```
# Either this (basic example):
mvn package exec:java -Dexec.mainClass="pineconeexamples.MinimalUpsertAndQueryExample" -Dpinecone.apikey=<your-api-key> -Dpinecone.indexName=<your-index-name> -Dpinecone.environment=<your-pinecone-environment> -Dpinecone.projectName=<your-project-id>

# Or (concurrent example)
mvn package exec:java -Dexec.mainClass="pineconeexamples.UpsertsAndQueriesConcurrentExample" -Dpinecone.apikey=<your-api-key> -Dpinecone.indexName=<your-index-name> -Dpinecone.environment=<your-pinecone-environment> -Dpinecone.projectName=<your-project-id>
```