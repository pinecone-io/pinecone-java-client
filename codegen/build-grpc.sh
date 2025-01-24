#!/bin/bash

set -eux -o pipefail
version=$1 # e.g. 2024-07

update_apis_repo() {
	echo "Updating apis repo"
	pushd codegen/apis
		git fetch
		git checkout main
		git pull
		just build
	popd
}

update_buf_config() {
    pushd codegen
        # Update buf config to find correct proto version
        sed -i '' "s/[0-9][0-9][0-9][0-9]-[0-1][0-9]/${version}/g" buf.yaml

        # Clean before building
        rm -rf gen

        # Ensure path valid by running the buf build command
        buf build
    popd
}

buf_generate() {
    pushd codegen
        # Generate the java code
        buf generate
    popd
}

update_apis_repo
update_buf_config
buf_generate

dest="src/main/java/io/pinecone/proto/"

# Remove existing files in dest
rm -rf "${dest}*.java"

# Copy the new generated files to dest directory
cp codegen/gen/java/io/pinecone/proto/*.java ${dest}

# Cleanup the intermediate files that were generated
rm -rf codegen/gen