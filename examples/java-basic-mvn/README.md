# java-basic-mvn
Upserts one 3-dimensional vector into the namespace `test-ns` of an existing index, then queries by its ID. The index must have dimension 3.

To build and run (Java and Maven required):
```
mvn package exec:java -Dexec.mainClass="pineconeexamples.MinimalUpsertAndQueryExample" -Dpinecone.apikey=<your-api-key> -Dpinecone.indexName=<your-index-name>
```
