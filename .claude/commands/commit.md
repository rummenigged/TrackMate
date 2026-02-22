# Commit Changes

Create a well-structured git commit following Conventional Commits and best practices.

## Arguments
- Commit scope/context: $ARGUMENTS (optional - if not provided, infer from changed files)

## The Seven Rules of a Great Commit Message

1. **Separate subject from body with a blank line**
2. **Limit the subject line to 50 characters** (72 is the hard limit)
3. **Capitalize the subject line**
4. **Do not end the subject line with a period**
5. **Use the imperative mood** - Write as a command: "Add feature" not "Added feature"
6. **Wrap the body at 72 characters**
7. **Use the body to explain what and why, not how**

## Commit Message Format

```
type(scope): Subject line in imperative mood

Body explaining what changed and why (not how).
The code explains the how; the message explains the context.

Co-Authored-By: Claude <assistant_id>@anthropic.com
```

## Conventional Commit Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `refactor`: Code refactoring (no feature/fix)
- `test`: Adding or updating tests
- `ci`: CI/CD changes
- `chore`: Maintenance tasks

## Instructions

1. Run `git status` to see changed files (never use -uall flag)
2. Run `git diff` to see unstaged changes and `git diff --staged` to see staged changes
3. Run `git log --oneline -5` to check recent commit style
4. Analyze changes to determine:
   - **Type**: What kind of change is this?
   - **Scope**: Which module/feature is affected?
   - **Subject**: Concise imperative description (max 50 chars)
   - **Body**: Why was this change made? What problem does it solve?
5. Stage relevant files (prefer specific files over `git add -A`)
6. Create the commit using a HEREDOC for proper formatting:
   ```bash
   git commit -m "$(cat <<'EOF'
type(scope): Subject line here

Body explaining the change.

Co-Authored-By: Claude <assistant_id>@anthropic.com
EOF
)"
   ```
7. Run `git status` to verify the commit succeeded

## Subject Line Test

A good subject completes this sentence:
> "If applied, this commit will **[your subject line]**"

Examples:
- "If applied, this commit will **refactor user authentication flow**"
- "If applied, this commit will **fix memory leak in image processing**"

## Example

Changed files: `feature/home/src/.../HomeViewModel.kt`, `core/domain/src/.../GetTasksUseCase.kt`

```
feat(home): Add task filtering by priority

Implement priority-based filtering in the home screen to help users
focus on high-priority tasks. The filter persists across sessions
using DataStore preferences.

Closes #45

Co-Authored-By: Claude <assistant_id>@anthropic.com
```

## Reference

For edge cases not covered here, consult: https://cbea.ms/git-commit