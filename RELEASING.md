# Releasing pinecone-client

The library is published to Maven Central as `io.pinecone:pinecone-client` from the **public** repo,
[pinecone-io/pinecone-java-client](https://github.com/pinecone-io/pinecone-java-client), by the
[Release workflow](.github/workflows/release.yml). Development happens in this internal repo, which is
mirrored to public with `scripts/sync_to_public.sh`.

Before the first release, do the [one-time setup](#one-time-setup-public-repo), and create the `release`
environment before this workflow is synced to public. Then the whole flow, once per release:

1. Prep PR in this repo sets the version.
2. Sync main to public.
3. Dry run of the Release workflow on public.
4. Real run of the Release workflow. It uploads to Maven Central and creates a draft GitHub release.
5. Write the notes in the draft GitHub release and publish it. The `vX.Y.Z` tag does not exist until
   you do; publishing creates it.
6. Pull the tag back into this repo. This only works after step 5.

A Maven Central version can never be deleted or replaced. Once step 4 uploads, the only fix for a bad
release is a new version.

## One-time setup (public repo)

**Signing key.** 6.3.0 and later are signed with this key:

| | |
|---|---|
| Key ID | `A87EC316B9162471` (short form `B9162471`) |
| Fingerprint | `68D9DCB2AB93798113C29B8DA87EC316B9162471` |
| Type | RSA-4096, one sign-capable primary key, no subkeys |
| User ID | `Pinecone.io Ops <ops@pinecone.io>` |
| Created | 2026-10-27, with no expiry date |

6.2.0 and earlier were signed with `2CFDEE6F36609094`. That key expired on 2026-05-27 and is retired;
don't sign with it again.

`ORG_GRADLE_PROJECT_SIGNINGKEY` and `ORG_GRADLE_PROJECT_SIGNINGPASSWORD` hold the new key's private half
and its passphrase. The workflow refuses to upload unless every signature was made by the fingerprint
above (`SIGNING_KEY_FINGERPRINT` in release.yml) and verifies against the public copy on
keyserver.ubuntu.com, one of the keyservers Maven Central checks. If you ever rotate the key, change
both release.yml and this section.

**Publishing the public key.** After you create a key, or change its expiry, upload the public half
again:

```sh
FPR=68D9DCB2AB93798113C29B8DA87EC316B9162471
gpg --keyserver keyserver.ubuntu.com --send-keys $FPR
gpg --export $FPR | curl -T - https://keys.openpgp.org
gpg --keyserver hkp://pgp.mit.edu --send-keys $FPR   # often times out; harmless if it does
```

keys.openpgp.org prints a link. Use it to send the verification email to ops@pinecone.io, then open the
link in that email. Until the address is verified, keys.openpgp.org serves the key without its user ID.
Wait about 24 hours after uploading before the real run, because Maven Central may not see the new copy
straight away.

**Extending the expiry.** This key has no expiry, so there is nothing to extend today. If you set one
later, extend it with `gpg --quick-set-expire $FPR 2y`; this key has no subkeys, so that one command
covers it. On a key with subkeys, don't rely on `gpg --quick-set-expire $FPR 2y '*'`: it skips a subkey
that has already expired. Name each subkey's fingerprint instead. Then publish the public key again as
above. The Actions secrets do not change.

**Secrets.** These repository secrets must exist on public (Settings > Secrets and variables >
Actions). The workflow passes them to Gradle under the same names.

| Secret | Holds |
|---|---|
| `ORG_GRADLE_PROJECT_SIGNINGKEY` | ASCII-armored PGP private key |
| `ORG_GRADLE_PROJECT_SIGNINGPASSWORD` | Its passphrase |
| `ORG_GRADLE_PROJECT_SIGNINGKEYID` | Leave it deleted (see below) |
| `ORG_GRADLE_PROJECT_SONATYPEUSERNAME` | Central Portal user token, username part |
| `ORG_GRADLE_PROJECT_SONATYPEPASSWORD` | Central Portal user token, password part |

The token comes from central.sonatype.com > your account > Generate User Token, by someone with
publish rights on the `io.pinecone` namespace. It is not the Portal login.

`ORG_GRADLE_PROJECT_SIGNINGKEYID` is deleted on public, so Gradle signs with the primary key, which is
what this key needs. If you ever set it, it must be the 8-hex-digit short form (such as `B9162471`).
Gradle rejects the 16-digit ID and the fingerprint, and the workflow fails on any other form. To go
back to the primary key, delete the secret. GitHub won't save an empty secret, so you can't clear it.

Keep these as repository secrets (not environment secrets). The dry run does not use the `release`
environment, so it cannot read secrets stored only there.

**The `release` environment (required).** The `publish` job runs in an environment named `release`,
and a reviewer on it approves each upload. If it did not exist, GitHub would create it on the first
real run with no protection, so preflight refuses a real run unless it exists with a required reviewer
(a dry run only warns). Create it once, before this workflow is first synced to public, under
Settings > Environments > New environment, name `release`:

- Required reviewers: add the people who may approve a publish. If you turn on "Prevent self-review",
  you need a second person to approve your own runs.
- Deployment branches and tags: Selected branches, `main`.

## Each release

The examples use 6.3.0. `PUB=pinecone-io/pinecone-java-client`.

### 1. Prep PR (this repo)

Open "chore: prepare to release v6.3.0" and merge it. It changes:

- `gradle.properties`: `pineconeClientVersion = 6.3.0`
- `src/main/java/io/pinecone/commons/Constants.java`: `"v6.3.0"` (the User-Agent, with the `v`)
- `CHANGELOG.md`: a `### 6.3.0` entry under `### Unreleased version`
- `README.md` (the `[pc:VERSION_LATEST_RELEASE]` spots) and `examples/*/pom.xml`

The workflow fails before uploading if the first three disagree. It does not check the README or the examples.

To inspect the artifacts locally without credentials:

```sh
./gradlew publishToMavenLocal -Dmaven.repo.local=/tmp/m2
ls /tmp/m2/io/pinecone/pinecone-client/6.3.0/   # jar, -all, -sources, -javadoc, .pom, .module
```

### 2. Sync to public

```sh
./scripts/sync_to_public.sh main          # dry run: lists the commits to push
./scripts/sync_to_public.sh main --push
```

Note the SHA of the prep commit: `git ls-remote https://github.com/$PUB refs/heads/main`.

### 3. Dry run

Public repo > Actions > Release > Run workflow. Use workflow from `main`, `ref` = the prep SHA (it has no
default, and it must be a commit on `main`), leave **dryRun** checked. Or:

```sh
gh workflow run release.yml -R $PUB -f ref=<SHA> -f dryRun=true
```

It uploads nothing. The run summary lists each check:

- **Preflight:** the SHA is on `main`, the User-Agent and CHANGELOG match the version, tag `v6.3.0` does
  not exist, 6.3.0 is not on Maven Central, the `release` environment has a required reviewer (a
  warning in a dry run), and the unit tests pass.
- **Dry run:** the secrets are set, every artifact signs with the stored key and passphrase, the
  signatures were made by the release key and verify against its copy on keyserver.ubuntu.com, and the
  Central Portal token authenticates.

Fix anything red before the real run.

### 4. Real run

Run it again with the same `ref` and **dryRun unchecked**:

```sh
gh workflow run release.yml -R $PUB -f ref=<SHA> -f dryRun=false
```

1. `preflight` repeats the checks and the tests, and fails if the `release` environment has no
   required reviewer.
2. `publish` waits for a reviewer to approve it from the run page ("Review deployments"). It then signs
   and verifies the signatures as the dry run does, uploads to the Central Portal, and releases the
   deployment.
3. `wait-for-central` polls repo1.maven.org for the POM, up to 30 minutes. 6.2.0 took about 2.
4. `github-release` creates a **draft** release `v6.3.0`, titled "v6.3.0 Release", targeting the SHA.
   A draft has no tag: `v6.3.0` does not exist until you publish it in step 5.

### 5. Publish the GitHub release

Open the draft from the run summary or the public Releases page and replace the placeholder with the
notes (house style: a summary, `## Features` or `## Fixes` with examples, `## Breaking Changes` for a
major, then a "Full Changelog" compare link). If the signing key changed since the previous release
(6.3.0: yes), add a "Signing key changed" note with the full fingerprint
`68D9 DCB2 AB93 7981 13C2 9B8D A87E C316 B916 2471`, and say that 6.2.0 and earlier were signed by
`2CFDEE6F36609094`. Then press Publish. That creates the `v6.3.0` tag at the SHA. Until you publish,
the release is on Maven Central with no tag, and step 6 has nothing to pull back.

```sh
git ls-remote https://github.com/$PUB refs/tags/v6.3.0   # must print the SHA
```

### 6. Pull the tag back

Only after the draft is published in step 5:

```sh
./scripts/sync_from_public.sh main          # expect: 1 new tag v6.3.0
./scripts/sync_from_public.sh main --push
```

## When something fails

| Where | What it means | What to do |
|---|---|---|
| Preflight check | The prep PR is incomplete or the version is already released | Fix it in a PR here, sync again, rerun. Never reuse a version that is on Central |
| Preflight, "not on main" | `ref` is a branch or a commit that never merged | Release only a commit on public `main`; pass its SHA |
| Preflight, `release` environment | It is missing or has no required reviewer | Create it (One-time setup), then rerun |
| Release secrets | A secret is missing, or `SIGNINGKEYID` is not an 8-hex-digit short ID | Add the missing secret, or delete `ORG_GRADLE_PROJECT_SIGNINGKEYID` |
| Signing | Wrong key or passphrase | Check the two signing secrets |
| Signature verify, "expired" | The key's public copy expired | See One-time setup |
| Signature verify, "not the release key" | The signing secret holds a different key | Put the release key in `ORG_GRADLE_PROJECT_SIGNINGKEY` |
| Central Portal token, HTTP 401 | Wrong or revoked token | Generate a new user token and update both Sonatype secrets |
| `publish`, before the upload step | Secrets, signing or verify failed; nothing was uploaded | Fix it as in the rows above, then rerun the whole workflow |
| `publish` upload step, deployment FAILED or no deployment | Nothing reached Central | Read the Gradle error. If central.sonatype.com > Publish > Deployments shows the deployment, drop it there, then rerun the whole workflow |
| `publish` upload step, deployment VALIDATED | It uploaded and passed validation but was not released | **Don't rerun.** Press Publish on the deployment. When the POM loads, create the draft by hand (the `github-release` row) |
| `publish` upload step, deployment PUBLISHING or PUBLISHED | The release is on its way to Central | **Don't rerun:** Central would reject the same version uploaded again. When the POM loads, create the draft by hand (the `github-release` row) |
| `wait-for-central` timeout | The upload worked but is not live | At central.sonatype.com > Publish > Deployments, if the deployment says VALIDATED, press Publish. When the POM loads, use "Re-run failed jobs". This does not upload again |
| `github-release` | Central has the release; GitHub does not | "Re-run failed jobs", or by hand: `gh release create v6.3.0 -R $PUB --draft --target <SHA> --title "v6.3.0 Release"` |

A bad release that already reached Central is fixed by releasing the next patch version. Never delete or
move a `v*` tag, and never force-push either repo's main.
