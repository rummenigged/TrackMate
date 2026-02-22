# Commit and Push

Create a well-structured git commit and push to remote in one step.

## Arguments
- Commit scope/context: $ARGUMENTS (optional - if not provided, infer from changed files)

## Instructions

1. **Create the commit** following the `/commit` command rules:

   ### The Seven Rules of a Great Commit Message
   1. Separate subject from body with a blank line
   2. Limit the subject line to 50 characters (72 is the hard limit)
   3. Capitalize the subject line
   4. Do not end the subject line with a period
   5. Use the imperative mood - "Add feature" not "Added feature"
   6. Wrap the body at 72 characters
   7. Use the body to explain what and why, not how

   ### Commit Message Format
   ```
   type(scope): Subject line in imperative mood

   Body explaining what changed and why.

   Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
   ```

   ### Conventional Commit Types
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation changes
   - `refactor`: Code refactoring
   - `test`: Adding or updating tests
   - `ci`: CI/CD changes
   - `chore`: Maintenance tasks

   ### Steps
   1. Run `git status` to see changed files
   2. Run `git diff` to see changes
   3. Run `git log --oneline -5` to check commit style
   4. Analyze and determine type, scope, subject, body
   5. Stage relevant files
   6. Create the commit using HEREDOC

2. **Push to remote**:
   ```bash
   git push
   ```

   If no upstream is set:
   ```bash
   git push -u origin <branch-name>
   ```

3. **Verify** by running `git status` and confirming push succeeded

## Example

```bash
# After committing
git push -u origin feature/issue-45-add-filtering
```

Output:
```
Commit created: feat(home): Add task filtering
Pushed to origin/feature/issue-45-add-filtering
```

## Reference

For commit message edge cases: https://cbea.ms/git-commit