#!/usr/bin/env bash
# Signing steps shared by the dry-run and publish jobs of release.yml. Run from the repository root with
# the ORG_GRADLE_PROJECT_* secrets and SIGNING_KEY_FINGERPRINT in the environment:
#
#   .github/scripts/release-signing.sh check-secrets | sign | verify
#
# Secret values are never printed.
set -euo pipefail

check_secrets() {
  local missing=() name
  for name in ORG_GRADLE_PROJECT_SIGNINGKEY ORG_GRADLE_PROJECT_SIGNINGPASSWORD \
              ORG_GRADLE_PROJECT_SONATYPEUSERNAME ORG_GRADLE_PROJECT_SONATYPEPASSWORD; do
    [ -n "${!name:-}" ] || missing+=("$name")
  done
  if [ "${#missing[@]}" -gt 0 ]; then
    echo "::error::Empty or missing Actions secrets: ${missing[*]}"
    return 1
  fi
  # Gradle accepts only the 8-hex-digit short form, and silently skips signing when a well-formed ID is
  # not in the key.
  local key_id=${ORG_GRADLE_PROJECT_SIGNINGKEYID:-}
  if [ -z "$key_id" ]; then
    echo "ORG_GRADLE_PROJECT_SIGNINGKEYID is not set: signing uses the primary key."
  elif ! [[ "$key_id" =~ ^(0x)?[0-9A-Fa-f]{8}$ ]]; then
    echo "::error::ORG_GRADLE_PROJECT_SIGNINGKEYID is not an 8-hex-digit short key ID. Delete the secret to sign with the primary key, or set the short ID (the last 8 hex digits of the fingerprint)."
    return 1
  fi
}

# The sign task builds the artifacts and signs them in memory; it has no upload in its task graph.
sign() {
  if ! ./gradlew signPineconeClientMavenPublication; then
    echo "::error::Signing failed. Check ORG_GRADLE_PROJECT_SIGNINGKEY (ASCII-armored private key), ORG_GRADLE_PROJECT_SIGNINGPASSWORD (its passphrase) and ORG_GRADLE_PROJECT_SIGNINGKEYID (deleted, or an 8-hex-digit short ID)."
    return 1
  fi
}

# Maven Central checks signatures against public keyservers, so check them the same way. Signing itself
# does not look at key expiry; only a good signature by the release key, verified against its published
# copy, passes.
verify() {
  local expected=${SIGNING_KEY_FINGERPRINT:?} sigs sig status key_id
  GNUPGHOME=$(mktemp -d)
  export GNUPGHOME
  mapfile -t sigs < <(find build/libs build/publications -name '*.asc' | sort)
  if [ "${#sigs[@]}" -ne 6 ]; then
    echo "::error::Expected 6 signatures (jar, -all, -sources, -javadoc, POM, module), found ${#sigs[@]}: ${sigs[*]}"
    return 1
  fi
  key_id=$(gpg --list-packets "${sigs[0]}" | sed -n 's/.*keyid \([0-9A-F]*\).*/\1/p' | head -n1)
  [ -z "${GITHUB_OUTPUT:-}" ] || echo "key_id=$key_id" >> "$GITHUB_OUTPUT"
  if ! curl -sf -o "$GNUPGHOME/public-key.asc" "https://keyserver.ubuntu.com/pks/lookup?op=get&options=mr&search=0x$expected"; then
    echo "::error::Release key $expected is not on keyserver.ubuntu.com, so Maven Central would reject the signatures."
    return 1
  fi
  gpg --batch --quiet --import "$GNUPGHOME/public-key.asc"
  for sig in "${sigs[@]}"; do
    status=$(gpg --batch --status-fd 1 --verify "$sig" "${sig%.asc}" 2> /dev/null || true)
    if grep -q '^\[GNUPG:\] GOODSIG ' <<< "$status" &&
       [ "$(awk '$2 == "VALIDSIG" { print $NF }' <<< "$status")" = "$expected" ]; then
      continue
    elif grep -q '^\[GNUPG:\] EXPKEYSIG ' <<< "$status"; then
      echo "::error::Release key $expected has expired on keyserver.ubuntu.com. Extend its expiry and upload the public key again (RELEASING.md), or Maven Central will reject the signatures."
    elif grep -q '^\[GNUPG:\] REVKEYSIG ' <<< "$status"; then
      echo "::error::Release key $expected is revoked on keyserver.ubuntu.com."
    elif grep -q '^\[GNUPG:\] NO_PUBKEY ' <<< "$status"; then
      echo "::error::${sig%.asc} is signed by key $key_id, not the release key $expected. ORG_GRADLE_PROJECT_SIGNINGKEY holds a different key. If the key was rotated on purpose, update SIGNING_KEY_FINGERPRINT in release.yml and RELEASING.md."
    else
      echo "::error::The signature on ${sig%.asc} does not verify against release key $expected."
    fi
    return 1
  done
  echo "All ${#sigs[@]} signatures verify against release key $expected."
}

case "${1:-}" in
  check-secrets) check_secrets ;;
  sign) sign ;;
  verify) verify ;;
  *) echo "usage: $0 check-secrets|sign|verify" >&2; exit 2 ;;
esac
