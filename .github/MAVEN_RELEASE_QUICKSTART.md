# Maven Release Quick Start Guide

## TL;DR - Creating a Release in 3 Steps

1. **Go to GitHub Actions tab** in your repository
2. **Click "Maven Release" → "Run workflow"**
3. **Click green "Run workflow" button** (leave fields empty for automatic versioning)

That's it! The entire release process is automated.

---

## What Happens Automatically

When you run the Maven Release workflow:

### Phase 1: Preparation (2-3 minutes)
```
Current version: 1.1.0-SNAPSHOT
    ↓
Release version: 1.1.0
    ↓
Create tag: v1.1.0
    ↓
Next version: 1.2.0-SNAPSHOT
```

### Phase 2: Execution
1. ✓ Updates `pom.xml` from `1.1.0-SNAPSHOT` to `1.1.0`
2. ✓ Commits: `[maven-release-plugin] prepare release v1.1.0`
3. ✓ Creates and pushes tag: `v1.1.0`
4. ✓ Updates `pom.xml` to `1.2.0-SNAPSHOT`
5. ✓ Commits: `[maven-release-plugin] prepare for next development iteration`
6. ✓ Builds release artifacts from the tag
7. ✓ Creates GitHub Release with:
   - `Pentomino-1.1.0.jar`
   - `Pentomino-1.1.0-jar-with-dependencies.jar`
8. ✓ Generates release notes automatically

## When to Use Custom Versions

### Automatic Versioning (Recommended)
Leave both fields empty. The workflow will:
- **Release version:** Remove `-SNAPSHOT` from current version
- **Next version:** Increment minor version + add `-SNAPSHOT`

**Example:**
```
1.1.0-SNAPSHOT → 1.1.0 → 1.2.0-SNAPSHOT
```

### Custom Versioning

Specify versions when you need:

#### Major Release (Breaking Changes)
```
Release version: 2.0.0
Next version: 2.1.0
```
Current: `1.9.0-SNAPSHOT` → Release: `2.0.0` → Next: `2.1.0-SNAPSHOT`

#### Patch Release (Bug Fix)
```
Release version: 1.1.1
Next version: 1.1.2
```
Current: `1.1.1-SNAPSHOT` → Release: `1.1.1` → Next: `1.1.2-SNAPSHOT`

#### Minor Release (New Features)
```
Release version: 1.2.0
Next version: 1.3.0
```
Current: `1.2.0-SNAPSHOT` → Release: `1.2.0` → Next: `1.3.0-SNAPSHOT`

## Visual Guide

### Step 1: Navigate to Actions
```
GitHub Repository → Actions tab (top menu)
```

### Step 2: Select Maven Release
```
Left sidebar: Maven Release workflow
```

### Step 3: Run Workflow
```
[Run workflow ▼] button (top right)
```

### Step 4: Configure (Optional)
```
┌─────────────────────────────────────────────┐
│ Use workflow from: Branch: main         ▼  │
│                                             │
│ Release version (e.g., 1.0.0)              │
│ [                                    ]     │
│ Leave empty for automatic                  │
│                                             │
│ Next development version (e.g., 1.1.0)     │
│ [                                    ]     │
│ Leave empty for automatic                  │
│                                             │
│          [Run workflow]                     │
└─────────────────────────────────────────────┘
```

### Step 5: Monitor Progress
```
Maven Release
├─ Checkout code ✓
├─ Set up JDK 11 ✓
├─ Configure Git ✓
├─ Extract current version ✓
├─ Determine versions ✓
├─ Maven Release Prepare ✓
│  ├─ Update pom.xml to 1.1.0
│  ├─ Commit and tag v1.1.0
│  └─ Update pom.xml to 1.2.0-SNAPSHOT
├─ Maven Release Perform ✓
│  └─ Build from tag v1.1.0
├─ Create GitHub Release ✓
└─ Upload artifacts ✓
```

## Semantic Versioning Reference

**Format:** MAJOR.MINOR.PATCH

| Type | When | Example |
|------|------|---------|
| **MAJOR** | Breaking changes | 1.9.0 → 2.0.0 |
| **MINOR** | New features (compatible) | 1.1.0 → 1.2.0 |
| **PATCH** | Bug fixes | 1.1.0 → 1.1.1 |

## Common Scenarios

### Scenario 1: Regular Feature Release
**Current:** `1.5.0-SNAPSHOT`
**Action:** Click "Run workflow" with empty fields
**Result:** 
- Release: `1.5.0`
- Next: `1.6.0-SNAPSHOT`

### Scenario 2: Hotfix/Patch Release
**Current:** `1.5.1-SNAPSHOT`
**Action:** Specify versions:
- Release: `1.5.1`
- Next: `1.5.2`

**Result:**
- Release: `1.5.1`
- Next: `1.5.2-SNAPSHOT`

### Scenario 3: Major Version Release
**Current:** `1.9.0-SNAPSHOT`
**Action:** Specify versions:
- Release: `2.0.0`
- Next: `2.1.0`

**Result:**
- Release: `2.0.0`
- Next: `2.1.0-SNAPSHOT`

## Troubleshooting

### Workflow Failed
1. Check the workflow logs in Actions tab
2. Most common issues:
   - Merge conflicts (pull latest changes first)
   - Test failures (fix tests before releasing)
   - Invalid version format

### Need to Cancel a Release
If workflow is still running:
1. Go to Actions tab
2. Click on the running workflow
3. Click "Cancel workflow" button (top right)

### Released Wrong Version
1. Delete the release in GitHub UI
2. Delete the tag:
   ```bash
   git tag -d v1.1.0
   git push origin :refs/tags/v1.1.0
   ```
3. Reset pom.xml if needed
4. Run the workflow again

## Pre-Release Checklist

Before running Maven Release:

- [ ] All changes committed and pushed
- [ ] CI build passing (green)
- [ ] Tests passing locally: `mvn test`
- [ ] Code reviewed and approved
- [ ] CHANGELOG updated (if you maintain one)
- [ ] Documentation up to date
- [ ] Ready to merge to main/master

## Post-Release Verification

After workflow completes:

1. **Check Releases page**
   - Go to: Repository → Releases
   - Verify new release exists
   - Verify JAR files are attached

2. **Test the release**
   ```bash
   # Download JAR from releases page
   java -jar Pentomino-1.1.0-jar-with-dependencies.jar
   ```

3. **Verify next development version**
   ```bash
   git pull
   # Check pom.xml shows 1.2.0-SNAPSHOT
   ```

## Benefits Over Manual Process

| Aspect | Maven Release | Manual |
|--------|---------------|--------|
| Version updates | Automatic | Manual |
| Git operations | Automatic | Manual |
| Error prone | Low | High |
| Time required | 3 minutes | 10-15 minutes |
| Rollback | Easy | Complex |
| Audit trail | Complete | Incomplete |

## Advanced: Command Line Usage

For developers who prefer CLI:

```bash
# Make sure you're on main and up to date
git checkout main
git pull

# Run Maven release
mvn release:prepare release:perform

# Plugin will prompt for versions
# Press Enter to accept defaults or type custom versions
```

## Need Help?

- **Full Documentation:** [RELEASE_GUIDE.md](RELEASE_GUIDE.md)
- **Workflow Details:** [workflows/README.md](workflows/README.md)
- **Issues:** Check GitHub Actions logs for detailed error messages

---

**Remember:** The Maven Release workflow is the recommended method for all releases. It's automated, safe, and prevents common mistakes.