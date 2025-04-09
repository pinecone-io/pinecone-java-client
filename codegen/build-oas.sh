#!/bin/bash

set -eux -o pipefail

version=$1 # e.g. 2024-07
modules=("db_control" "db_data" "inference")

destination="src/main/java/org/openapitools"
build_dir="gen"

update_apis_repo() {
	echo "Updating apis repo"
	pushd codegen/apis
		git fetch
		git checkout main
		git pull
		just build
	popd
}

verify_spec_version() {
	local version=$1
	echo "Verifying spec version $version exists in apis repo"
	if [ -z "$version" ]; then
		echo "Version is required"
		exit 1
	fi

	verify_directory_exists "codegen/apis/_build/${version}"
}

verify_file_exists() {
	local filename=$1
	if [ ! -f "$filename" ]; then
		echo "File does not exist at $filename"
		exit 1
	fi
}

verify_directory_exists() {
	local directory=$1
	if [ ! -d "$directory" ]; then
		echo "Directory does not exist at $directory"
		exit 1
	fi
}

generate_client() {
	local module_name=$1

	oas_file="codegen/apis/_build/${version}/${module_name}_${version}.oas.yaml"
	
	verify_file_exists $oas_file

	# Cleanup previous build files
	echo "Cleaning up previous build files"
	rm -rf "${build_dir}"

	# Generate client module
	docker run --rm -v $(pwd):/workspace openapitools/openapi-generator-cli:v7.0.1 generate \
		--input-spec "/workspace/$oas_file" \
		--generator-name java \
		--additional-properties=dateLibrary='java8',disallowAdditionalPropertiesIfNotPresent=false \
		--output "/workspace/${build_dir}"

	# Copy the generated module to the correct location
	rm -rf "${destination}/${module_name}"
	mkdir -p "${destination}/${module_name}"

	path_to_copy="${build_dir}/src/main/java/org/openapitools/client"
	cp -r $path_to_copy "${destination}/${module_name}"

	# Adjust package names in generated file
  find "${destination}/${module_name}" -name "*.java" | while IFS= read -r file; do
    sed -i '' "s/org\.openapitools\.client/org\.openapitools\.${module_name}\.client/g" "$file"
  done

  # Add NDJSON block to ApiClient.java in the db_data module
  if [ "$module_name" == "db_data" ]; then
      echo "Adding NDJSON handler block to ApiClient.java"

      # Use sed to insert the NDJSON block into ApiClient.java
      sed -i '' '/return RequestBody.create((File) obj, MediaType.parse(contentType));/a \
          } else if ("application/x-ndjson".equals(contentType)) { \
              // Handle NDJSON (Newline Delimited JSON) \
              if (obj instanceof Iterable) { \
                  StringBuilder ndjsonContent = new StringBuilder(); \
                  for (Object item : (Iterable<?>) obj) { \
                      String json = JSON.serialize(item); \
                      ndjsonContent.append(json).append("\\n"); \
                  } \
                  return RequestBody.create(ndjsonContent.toString(), MediaType.parse(contentType)); \
              } else { \
                  throw new ApiException("NDJSON content requires a collection of objects."); \
              } \
          ' src/main/java/org/openapitools/db_data/client/ApiClient.java
  fi
}

update_apis_repo
verify_spec_version $version

rm -rf "${destination}"
mkdir -p "${destination}"

for module in "${modules[@]}"; do
	generate_client $module
done
