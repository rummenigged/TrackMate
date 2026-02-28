# Create Pull Request

Create a pull request following the project template and Conventional Commits format.

## Arguments
- PR context/description: $ARGUMENTS (optional - if not provided, infer from commits)

## Pre-PR Verification

Before creating the PR, run these verification steps to catch issues early:

### 1. Build the Project
```bash
./gradlew assembleDebug
```

### 2. Lint Code
Run ktlint and Android Lint:
```bash
./gradlew ktlintCheck lintDebug
```

### 3. Run All Tests
```bash
./gradlew testDebugUnitTest
```

### Verification Failure Handling
- If any step fails, **stop and report the failure** to the user
- Do NOT proceed to create the PR until all checks pass
- Suggest fixes when possible (e.g., `./gradlew ktlintFormat` for lint issues)

---

## PR Title Format (Conventional Commits)

The PR title MUST follow semantic versioning format. Valid types:
- `build`: Build system changes
- `ci`: CI/CD changes
- `docs`: Documentation changes
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `style`: Code style changes (formatting, etc.)
- `perf`: Performance improvements

Format: `type(scope): Description`

Examples:
- `feat(home): Add task filtering by priority`
- `fix(auth): Resolve login timeout issue`
- `docs(project): Add comprehensive documentation`

## PR Body Template

```markdown
## Ticket
ISSUE - #<issue-number>

## Rationale
[Brief explanation of the purpose of this PR]

## Screenshots (UI PRs only)
[Add screenshots if UI changes were made, otherwise remove this section]
```

## Instructions

### Phase 1: Gather Information

1. Run these commands in parallel to understand the current state:
   ```bash
   git status
   git log develop..HEAD --oneline
   git diff develop...HEAD --stat
   ```

2. Check if there are uncommitted changes - if so, ask the user to commit first

3. Extract the issue number from the branch name (e.g., `docs/issue-121-...` → `121`)

### Phase 2: Run Verification (REQUIRED)

4. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```
   If build fails, stop and report the error.

5. **Lint code** (ktlint + Android Lint):
   ```bash
   ./gradlew ktlintCheck lintDebug
   ```
   If ktlint fails, suggest running `./gradlew ktlintFormat` to auto-fix.

6. **Run all unit tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
   If tests fail, stop and report which tests failed.

**IMPORTANT**: If ANY verification step fails, do NOT proceed to create the PR. Report the failure and help the user fix it.

### Phase 3: Create the PR

7. Analyze ALL commits in the branch (not just the latest) to determine:
   - **Type**: What kind of change is this overall?
   - **Scope**: Which module/feature is primarily affected?
   - **Title**: Concise description (under 70 chars total)
   - **Rationale**: Summary of what the PR accomplishes and why

8. Check if the branch is pushed to remote:
   ```bash
   git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null || echo "no-upstream"
   ```

9. Push to remote if needed:
   ```bash
   git push -u origin <branch-name>
   ```

10. Create the PR using `gh pr create` with HEREDOC for proper formatting:
    ```bash
    gh pr create --base develop --title "type(scope): Description" --body "$(cat <<'EOF'
    ## Ticket
    ISSUE - #<issue-number>

    ## Rationale
    <Brief explanation based on commit analysis>

    ## Screenshots (UI PRs only)
    N/A
    EOF
    )"
    ```

11. Return the PR URL to the user

## Example

Branch: `docs/issue-121-create-project-documentation-for-claude-code`
Commits:
- `chore(git): Ignore IDE auto-generated files`
- `docs(project): Add comprehensive project documentation`

Output:
```bash
gh pr create --base develop --title "docs(project): Add project documentation for Claude Code" --body "$(cat <<'EOF'
## Ticket
ISSUE - #121

## Rationale
Add comprehensive project documentation to support Claude Code integration
and improve developer onboarding. Includes README, architecture docs, ADRs,
guides, patterns, and module-level documentation.

## Screenshots (UI PRs only)
N/A
EOF
)"
```

## Notes

- Always analyze ALL commits, not just the most recent one
- The PR title type should reflect the overall nature of the changes
- If commits have mixed types, use the most significant one (feat > fix > refactor > docs > chore)
- Keep the rationale concise but informative (2-4 sentences)