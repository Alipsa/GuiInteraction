# Publishing Guide

This guide explains how to publish GuiInteraction to Maven Central.

## Prerequisites

### 1. Sonatype Account

You need a Sonatype OSSRH (OSS Repository Hosting) account:

1. Create an account at https://issues.sonatype.org
2. Create a New Project ticket requesting access to the `se.alipsa` group ID
3. Wait for approval (usually 1-2 business days)

### 2. GPG Key Setup

All artifacts must be signed with a GPG key:

```bash
# Generate a new GPG key (if you don't have one)
gpg --gen-key

# List your keys to find the key ID (last 8 characters of the key fingerprint)
gpg --list-keys --keyid-format SHORT

# Example output:
# pub   rsa3072/ABCD1234 2024-01-01 [SC]
#       1234567890ABCDEF1234567890ABCDABCD1234
# uid   [ultimate] Your Name <your.email@example.com>
# sub   rsa3072/EFGH5678 2024-01-01 [E]

# Upload your public key to a key server
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234

# Export your secret key ring (for Gradle signing)
gpg --export-secret-keys ABCD1234 > ~/.gnupg/secring.gpg
```

### 3. Configure Credentials

Add credentials to your `~/.gradle/gradle.properties` (NOT the project's `gradle.properties`):

```properties
# Sonatype OSSRH credentials
sonatypeUsername=your-sonatype-username
sonatypePassword=your-sonatype-password

# GPG signing configuration
signing.keyId=ABCD1234
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
```

**Important Security Notes:**
- Never commit credentials to version control
- Use environment variables in CI/CD instead of files
- Consider using GPG agent for passphrase management

### Alternative: Environment Variables

For CI/CD environments, use environment variables:

```bash
export ORG_GRADLE_PROJECT_sonatypeUsername=your-username
export ORG_GRADLE_PROJECT_sonatypePassword=your-password
export ORG_GRADLE_PROJECT_signingKeyId=ABCD1234
export ORG_GRADLE_PROJECT_signingPassword=your-passphrase
export ORG_GRADLE_PROJECT_signingSecretKeyRingFile=/path/to/secring.gpg
```

## Publishing Process

### Using the Release Script

The easiest way to publish is using the release script:

```bash
# Dry run (shows what would happen)
./release.sh --dry-run

# Release current version
./release.sh

# Bump version and release
./release.sh --bump patch   # 0.2.0 -> 0.2.1
./release.sh --bump minor   # 0.2.0 -> 0.3.0
./release.sh --bump major   # 0.2.0 -> 1.0.0
```

### Manual Publishing

If you prefer manual control:

```bash
# 1. Run tests
./gradlew test

# 2. Publish each module
./gradlew :gi-common:clean :gi-common:build :gi-common:release
./gradlew :gi-console:clean :gi-console:build :gi-console:release
./gradlew :gi-fx:clean :gi-fx:build :gi-fx:release
./gradlew :gi-swing:clean :gi-swing:build :gi-swing:release

# 3. Create and push git tag
git tag -a v0.2.0 -m "Release 0.2.0"
git push origin v0.2.0
```

### Publishing to Staging Only

To upload without releasing (for verification):

```bash
./gradlew :gi-common:publishToSonatype
```

Then manually release from https://s01.oss.sonatype.org/

## Release Checklist

Before releasing:

- [ ] All tests pass: `./gradlew test`
- [ ] Version is not SNAPSHOT: check `build.gradle`
- [ ] Changelog is updated: check `CHANGELOG.md`
- [ ] Documentation is current
- [ ] No uncommitted changes: `git status`

After releasing:

- [ ] Create git tag: `git tag -a vX.Y.Z -m "Release X.Y.Z"`
- [ ] Push tag: `git push origin vX.Y.Z`
- [ ] Create GitHub release: https://github.com/Alipsa/GuiInteraction/releases/new
- [ ] Verify on Maven Central (may take 10-30 minutes): https://central.sonatype.com/search?q=se.alipsa.gi
- [ ] Bump version to next SNAPSHOT for development

## Troubleshooting

### "signing.keyId is not defined"

The build skips signing if credentials aren't found. This is normal for local development but required for publishing.

### "401 Unauthorized" on publish

Check that:
1. Credentials in `~/.gradle/gradle.properties` are correct
2. Your Sonatype account has permission for `se.alipsa` group
3. You're not using 2FA (use token-based auth instead)

### "Could not sign" errors

Verify GPG setup:
```bash
# Test signing
echo "test" | gpg --clearsign

# Check key is available
gpg --list-secret-keys
```

### Artifacts not appearing on Maven Central

After publishing to Sonatype:
1. Log in to https://s01.oss.sonatype.org/
2. Go to "Staging Repositories"
3. Find your repository and click "Close"
4. After validation passes, click "Release"
5. Wait 10-30 minutes for sync to Maven Central

## CI/CD Setup (GitHub Actions)

For automated releases, add secrets to your GitHub repository:

1. Go to Settings > Secrets and variables > Actions
2. Add these repository secrets:
   - `SONATYPE_USERNAME`
   - `SONATYPE_PASSWORD`
   - `GPG_SIGNING_KEY` (base64-encoded secret key)
   - `GPG_SIGNING_PASSWORD`

Example workflow step:
```yaml
- name: Publish to Maven Central
  env:
    ORG_GRADLE_PROJECT_sonatypeUsername: ${{ secrets.SONATYPE_USERNAME }}
    ORG_GRADLE_PROJECT_sonatypePassword: ${{ secrets.SONATYPE_PASSWORD }}
    ORG_GRADLE_PROJECT_signingKeyId: ${{ secrets.GPG_KEY_ID }}
    ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_SIGNING_PASSWORD }}
    ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_SIGNING_KEY }}
  run: ./gradlew publish
```
