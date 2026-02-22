# Investigate Issue

Investigate a GitHub issue and create an implementation plan.

## Arguments
- Issue ID: $ARGUMENTS (required)

## Model Selection

Use **Sonnet** for this task - it's optimized for code investigation, searching, and planning. Reserve Opus for complex architectural decisions that emerge during investigation.

## Instructions

1. **Fetch the issue details**:
   ```bash
   gh issue view $ARGUMENTS --json title,body,labels,assignees
   ```

2. **Enter plan mode** to investigate and design a solution:
   - Use the `EnterPlanMode` tool to switch to planning mode
   - This ensures the user approves the approach before implementation

3. **In plan mode, investigate the codebase**:
   - Search for relevant files using Glob and Grep
   - Read key files to understand current implementation
   - Identify affected modules and dependencies
   - Note any existing patterns that should be followed

4. **Create an implementation plan** that includes:
   - **Summary**: Brief description of the issue and proposed solution
   - **Root Cause** (for bugs): What's causing the issue
   - **Affected Files**: List of files that need changes
   - **Implementation Steps**: Ordered list of changes to make
   - **Testing Strategy**: How to verify the fix works
   - **Risks/Considerations**: Potential side effects or edge cases

5. **Exit plan mode** with `ExitPlanMode` to get user approval

## Plan Format

Write the plan to the plan file in this format:

```markdown
# Issue #<id>: <title>

## Summary
<Brief description of the issue and proposed solution>

## Root Cause (if bug)
<What's causing the issue>

## Affected Files
- `path/to/file1.kt` - <what changes>
- `path/to/file2.kt` - <what changes>

## Implementation Steps
1. <First step with details>
2. <Second step with details>
3. ...

## Testing Strategy
- <How to test the changes>
- <Edge cases to verify>

## Risks & Considerations
- <Potential side effects>
- <Breaking changes>
- <Performance implications>
```

## Example

Input: `123`

```bash
gh issue view 123 --json title,body,labels,assignees
```

Output:
```json
{
  "title": "Task completion not syncing to Firestore",
  "body": "When marking a task as complete offline, the change doesn't sync when back online...",
  "labels": [{"name": "bug"}]
}
```

Then enter plan mode, investigate `core/data/`, `WorkManager` sync logic, and create a detailed fix plan.

## Notes

- Always use plan mode for investigation - this ensures user buy-in before coding
- Reference existing patterns in `docs/patterns/` when proposing solutions
- Check ADRs in `docs/decisions/` for architectural context
- For complex multi-module changes, consider recommending Opus for deeper analysis