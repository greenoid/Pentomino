# GitHub Actions Workflows

This directory contains automated workflows for the Pentomino Game project.

## Available Workflows

### 1. CI Build (`ci.yml`) - Continuous Integration
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

### 2. Maven Release (`maven-release.yml`) - **RECOMMENDED**
**Triggers:** Manual workflow dispatch (Actions tab)

**Purpose:** Fully automated release using Maven Release Plugin

**Features:**
- ✓ Automatic version management (SNAPSHOT → Release → Next SNAPSHOT)
- ✓ Automatic Git commits and tagging
- ✓ Build and upload artifacts
- ✓ Create GitHub Release
- ✓ Optional version override
- ✓ Zero manual pom.xml editing

**Steps:**
- ✓ Configure Git user
- ✓ Extract current version
- ✓ Calculate release and next version
- ✓ Maven release:prepare (updates versions, commits, tags)
- ✓ Maven release:perform (builds from tag)
- ✓ Create GitHub Release with artifacts
- ✓ Upload build artifacts

**How to Use:**
1. Go to Actions tab → Maven Release
2. Click "Run workflow"
3. (Optional) Specify release/next versions or leave empty for automatic
4. Click green "Run workflow" button

**Badge:**
```markdown
![Maven Release](https://github.com/YOUR_USERNAME/YOUR_REPO/workflows/Maven%20Release/badge.svg)
```

### 3. Manual Release (`release.yml`) - Legacy
**Triggers:** Push of tags starting with `v` (e.g., `v1.0.0`)

**Purpose:** Manual release creation with build artifacts (for backward compatibility)

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

### Creating a Release (Recommended Method)

**Use the Maven Release Workflow:**

1. Go to your repository on GitHub
2. Click the **"Actions"** tab
3. Select **"Maven Release"** from the workflows list
4. Click **"Run workflow"** (top right)
5. Leave fields empty for automatic versioning, or specify:
   - Release version (e.g., `1.2.0`)
   - Next development version (e.g., `1.3.0`)
6. Click the green **"Run workflow"** button
7. Monitor progress in the workflow run

**What happens automatically:**
- Current `1.1.0-SNAPSHOT` → Release `1.1.0` (tagged as `v1.1.0`)
- Updates to next `1.2.0-SNAPSHOT`
- All changes committed and pushed
- GitHub Release created with JAR files

### Running CI Locally

To test what CI will do before pushing:

```bash
# Run the same commands as CI
mvn clean compile
mvn test
mvn package
```

### Creating a Manual Release (Legacy Method)

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

## Comparison: Maven Release vs Manual Release

| Feature | Maven Release | Manual Release |
|---------|--------------|----------------|
| Version Management | Automatic | Manual |
| Git Commits | Automatic | Manual |
| Git Tagging | Automatic | Manual |
| pom.xml Updates | Automatic | Manual |
| Error Prevention | High | Low |
| Ease of Use | Very Easy | Moderate |
| **Recommended** | ✅ Yes | ❌ Legacy |

## Generated Artifacts

### CI Build Artifacts
- **Name:** `pentomino-jar`
- **Contents:** `target/*.jar`
- **Retention:** 7 days
- **Access:** Available in workflow run page

### Maven Release Artifacts
- **Files Attached to Release:**
  - `Pentomino-{version}.jar` - Standard JAR
  - `Pentomino-{version}-jar-with-dependencies.jar` - Fat JAR with all dependencies
- **Location:** `target/checkout/target/*.jar`
- **Retention:** 30 days in workflow artifacts, permanent in release
- **Access:** GitHub Releases page

### Manual Release Artifacts
- **Files Attached to Release:**
  - `Pentomino-{version}.jar` - Standard JAR
  - `Pentomino-{version}-jar-with-dependencies.jar` - Fat JAR with all dependencies
- **Retention:** 30 days in workflow artifacts, permanent in release
- **Access:** GitHub Releases page
- **Note:** Legacy method, use Maven Release instead

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
[![Maven Release](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/maven-release.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/maven-release.yml)
```

For legacy manual release workflow:
```markdown
[![Manual Release](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/release.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions/workflows/release.yml)
```

Replace `YOUR_USERNAME` and `YOUR_REPO` with your actual GitHub username and repository name.