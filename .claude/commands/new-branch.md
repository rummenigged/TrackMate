# New Branch from GitHub Issue

Create a git branch from a GitHub issue following the naming convention: `branchType/issue-ID-issue-title`

## Arguments
- Issue ID: $ARGUMENTS (required)

## Instructions

1. Fetch the GitHub issue details using `gh issue view $ARGUMENTS --json title,labels`

2. Determine the branch type based on issue labels:
   - If label contains "feature" or "enhancement" → `feature`
   - If label contains "bug" → `fix`
   - If label contains "documentation" → `docs`
   - If label contains "refactor" → `refactor`
   - If no matching label found, ask the user to choose: feature, fix, docs, refactor

3. Format the branch name:
   - Take the issue id
   - Take the issue title
   - Convert to lowercase
   - Replace spaces and special characters with hyphens
   - Remove consecutive hyphens
   - Trim hyphens from start/end
   - Prefix with branch type, slash, and `issue-ID-`

4. Create and checkout the branch from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b branchType/issue-ID-formatted-issue-title
   ```

5. Confirm the branch was created successfully

## Example

Input: `121`
Issue title: "Create project documentation for Claude Code"
Label: "documentation"

Output branch: `docs/issue-121-create-project-documentation-for-claude-code`