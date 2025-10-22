# GitHub Actions Workflows

This directory contains automated workflows for the Pentomino Game project.

## Available Workflows

### 1. CI Build (`ci.yml`)
**Triggers:** Push to main/develop/master branches, Pull Requests to main/master

**Purpose:** Continuous Integration - validates code quality on every change

**Steps:**
- ✓ Checkout code
- ✓ Set up Java 11
- ✓ Build project
- ✓ Run tests
- ✓ Package application
- ✓ Upload artifacts (7-day retention)

**Badge:**
```markdown
![CI Build](https://github.com/YOUR_USERNAME/YOUR_REPO/workflows/CI%20Build/badge.svg)
```

### 2. Release (`release.yml`)
**Triggers:** Push of tags starting with `v` (e.g., `v1.0.0`)

**Purpose:** Automated release creation with build artifacts

**Steps:**
- ✓ Checkout code
- ✓ Set up Java 11
- ✓ Extract version from tag
- ✓ Update pom.xml version
- ✓ Build release artifacts
- ✓ Create GitHub Release
- ✓ Upload JAR files
- ✓ Generate release notes

**Badge:**
```markdown
![Release](https://github.com/YOUR_USERNAME/YOUR_REPO/workflows/Release/badge.svg)
```

## Quick Start

### Running CI Locally

To test what CI will do before pushing:

```bash
# Run the same commands as CI
mvn clean compile
mvn test
mvn package
```

### Creating a Release

```bash
# 1. Update version in pom.xml (e.g., 1.0.0)
# 2. Commit changes
git add pom.xml
git commit -m "Prepare release 1.0.0"
git push

# 3. Create and push tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

The release workflow will automatically create a GitHub release with JAR files.

## Workflow Requirements

### Permissions
Both workflows require these repository permissions:
- `contents: read` - For checking out code
- `contents: write` - For creating releases (release workflow only)

### Secrets
No additional secrets required! The workflows use the built-in `GITHUB_TOKEN`.

## Generated Artifacts

### CI Build Artifacts
- **Name:** `pentomino-jar`
- **Contents:** `target/*.jar`
- **Retention:** 7 days
- **Access:** Available in workflow run page

### Release Artifacts
- **Files Attached to Release:**
  - `Pentomino-{version}.jar` - Standard JAR
  - `Pentomino-{version}-jar-with-dependencies.jar` - Fat JAR with all dependencies
- **Retention:** 30 days in workflow artifacts, permanent in release
- **Access:** GitHub Releases page

## Monitoring Workflows

### View Workflow Runs
1. Go to the "Actions" tab in your repository
2. Select a workflow from the left sidebar
3. Click on a specific run to see details

### Troubleshooting Failed Workflows
1. Click on the failed workflow run
2. Expand the failed step to see error details
3. Check build logs for compilation or test errors
4. Verify Java version and Maven configuration

## Modifying Workflows

When editing workflow files:
1. Make changes to `.yml` files in this directory
2. Test locally if possible
3. Commit and push changes
4. Monitor the Actions tab for results

## Additional Resources

- [Full Release Guide](../RELEASE_GUIDE.md)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Documentation](https://maven.apache.org/)

## Status Badges

Add these to your README.md:

```markdown
[![CI Build](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/ci.yml)
[![Release](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/release.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/release.yml)
```

Replace `YOUR_USERNAME` and `YOUR_REPO` with your actual GitHub username and repository name.