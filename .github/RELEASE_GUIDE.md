# GitHub Release Pipeline Guide

This document explains how to use the GitHub release pipeline for the Pentomino Game project.

## Overview

The project includes two GitHub Actions workflows:
1. **CI Build** (`.github/workflows/ci.yml`) - Runs on every push and pull request
2. **Release** (`.github/workflows/release.yml`) - Creates releases when tags are pushed

## Automatic CI Build

The CI workflow automatically runs when:
- Code is pushed to `main`, `develop`, or `master` branches
- A pull request is opened targeting `main` or `master`

### What it does:
- Checks out the code
- Sets up Java 11
- Compiles the project
- Runs all tests
- Packages the application
- Uploads build artifacts (available for 7 days)

## Creating a Release

### Step 1: Update Version

First, update the version in `pom.xml`:

```xml
<version>1.0.0</version>
```

Change from `1.0-SNAPSHOT` to your desired release version (e.g., `1.0.0`, `1.1.0`, `2.0.0`).

### Step 2: Commit Changes

```bash
git add pom.xml
git commit -m "Prepare release 1.0.0"
git push origin main
```

### Step 3: Create and Push Tag

```bash
# Create an annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push the tag to GitHub
git push origin v1.0.0
```

**Important:** The tag must start with `v` (e.g., `v1.0.0`, `v2.1.3`)

### Step 4: Automatic Release Process

Once the tag is pushed, GitHub Actions will automatically:
1. Checkout the code
2. Update pom.xml version to match the tag
3. Build the project (skips tests for faster release)
4. Create two JAR files:
   - `Pentomino-{version}.jar` - Standard JAR
   - `Pentomino-{version}-jar-with-dependencies.jar` - Fat JAR with all dependencies
5. Create a GitHub Release with:
   - Release notes (auto-generated from commits)
   - Downloadable JAR files
   - Installation instructions
6. Upload artifacts for 30 days retention

### Step 5: Verify Release

1. Go to your GitHub repository
2. Click on "Releases" in the right sidebar
3. Verify the new release is created with JAR files attached

## Semantic Versioning

Follow [Semantic Versioning](https://semver.org/) (SemVer):

**Format:** MAJOR.MINOR.PATCH (e.g., 1.2.3)

- **MAJOR** (1.x.x): Breaking changes, incompatible API changes
- **MINOR** (x.1.x): New features, backwards-compatible
- **PATCH** (x.x.1): Bug fixes, backwards-compatible

### Examples:
- `1.0.0` - Initial release
- `1.1.0` - Added new AI strategy
- `1.1.1` - Fixed bug in MinMax algorithm
- `2.0.0` - Changed game board API (breaking change)

## Development Workflow

### For Feature Development:

```bash
# Create feature branch
git checkout -b feature/new-strategy

# Make changes and commit
git add .
git commit -m "Add new computer strategy"

# Push and create pull request
git push origin feature/new-strategy
```

The CI workflow will automatically run tests on your pull request.

### For Bug Fixes:

```bash
# Create fix branch
git checkout -b fix/board-rendering

# Make changes and commit
git add .
git commit -m "Fix board rendering issue"

# Push and create pull request
git push origin fix/board-rendering
```

## Release Checklist

Before creating a release, ensure:

- [ ] All tests pass locally: `mvn test`
- [ ] Code builds successfully: `mvn clean package`
- [ ] Version is updated in `pom.xml`
- [ ] CHANGELOG is updated (if you maintain one)
- [ ] Documentation is up-to-date
- [ ] All changes are committed and pushed
- [ ] CI build passes on main branch

## Testing the Release Locally

Before creating a tag, test the build locally:

```bash
# Clean build
mvn clean package

# Test the standard JAR
java -jar target/Pentomino-1.0-SNAPSHOT.jar

# Test the fat JAR (with dependencies)
java -jar target/Pentomino-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Troubleshooting

### Release workflow didn't trigger
- Ensure tag starts with `v` (e.g., `v1.0.0`)
- Check GitHub Actions tab for errors
- Verify you have permissions to create releases

### Build failed
- Check the Actions tab for detailed error logs
- Ensure all dependencies are available
- Verify Java version compatibility (requires Java 11)

### JAR files not attached
- Check if the package phase completed successfully
- Verify the `target/*.jar` files exist
- Review workflow permissions (needs `contents: write`)

## Manual Release (Without GitHub Actions)

If you prefer manual releases:

```bash
# Build the project
mvn clean package

# Create the release manually in GitHub UI
# Upload the JAR files from target/ directory
```

## Rollback a Release

If you need to rollback:

```bash
# Delete the tag locally
git tag -d v1.0.0

# Delete the tag remotely
git push origin :refs/tags/v1.0.0

# Delete the GitHub release in the UI if already created
```

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/)
- [Semantic Versioning](https://semver.org/)

## Support

For issues with the release pipeline, check:
1. GitHub Actions logs in the "Actions" tab
2. Maven build output
3. Project issue tracker