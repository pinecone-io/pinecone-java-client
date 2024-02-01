# Use the official Maven image with Java
FROM maven:3.8.4-openjdk-17

# Set the working directory in the container
WORKDIR /app

# Copy the Java project into the container at /app
# Adjust this to copy only the necessary files
COPY . /app

# Compile the entire project. You can customize this if needed
RUN mvn clean package -Dmaven.repo.local=repository

# The command to run the examples. Replace 'com.example.MainClass' with your main class
# You can repeat this pattern for different examples, or use script/commands to handle multiple examples
CMD ["mvn", "package", "exec:java", "-Dexec.mainClass=pineconeexamples.CreateIndexAndCollectionExample", "-Dpinecone.apiKey=b56c33c1-ba11-4f93-9333-4235ffdaac43", "-Dpinecone.indexName=poddy2", "-Dpinecone.environment=us-west1-gcp", "-Dpinecone.projectName=1390950"]