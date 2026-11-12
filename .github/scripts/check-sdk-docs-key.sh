#!/usr/bin/env bash
# Checks that the SSH_DEPLOY_KEY environment variable holds a key that can push to pinecone-io/sdk-docs.
# It opens git-receive-pack, which GitHub refuses unless the key has write access, and sends no commands,
# so nothing is pushed. A read check would not do: sdk-docs is public. The key is never printed.
set -euo pipefail

if [ -z "${SSH_DEPLOY_KEY:-}" ]; then
  echo "::error::The SSH_DEPLOY_KEY secret is empty or not set. See 'Docs deploy key' in RELEASING.md."
  exit 1
fi

dir=$(mktemp -d)
trap 'rm -rf "$dir"' EXIT
printf '%s\n' "$SSH_DEPLOY_KEY" > "$dir/key"
chmod 600 "$dir/key"
ssh-keyscan -t ed25519,rsa github.com > "$dir/known_hosts" 2> /dev/null

if ! ssh -F /dev/null -i "$dir/key" -o IdentitiesOnly=yes -o IdentityAgent=none -o BatchMode=yes \
    -o UserKnownHostsFile="$dir/known_hosts" \
    git@github.com "git-receive-pack 'pinecone-io/sdk-docs.git'" < /dev/null > /dev/null; then
  echo "::error::SSH_DEPLOY_KEY cannot push to pinecone-io/sdk-docs (see the ssh error above). Its public half must be a deploy key with write access on sdk-docs. See 'Docs deploy key' in RELEASING.md."
  exit 1
fi
echo "SSH_DEPLOY_KEY has write access to pinecone-io/sdk-docs."
