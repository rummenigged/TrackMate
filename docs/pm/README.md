# Project Management Documentation

This directory contains project management artifacts for TrackMate.

## Structure

```
docs/pm/
├── README.md              # This file
├── PROJECT_STATUS.md      # Current project status
├── sprints/               # Sprint reports (Sprint-YYYY-WW.md)
├── use-cases/             # Use case documents (UC-XXX.md)
└── templates/             # Templates for PM documents
    ├── SPRINT_TEMPLATE.md
    └── USE_CASE_TEMPLATE.md
```

## Using the PM Agent

Invoke the PM agent with `/pm` command.

The PM agent can:
- Create use cases from requirements
- Break work into epics, tasks, subtasks
- Plan sprints with dependencies
- Create GitHub issues with proper labels
- Document sprint outcomes

## GitHub Labels

| Label | Purpose |
|-------|---------|
| `epic` | Large feature grouping |
| `task` | Individual work item |
| `subtask` | Subdivision of a task |
| `use-case` | Linked to a use case |
| `priority:critical` | Must be done immediately |
| `priority:high` | Important, do soon |
| `priority:medium` | Standard priority |
| `priority:low` | Nice to have |
| `sprint:current` | In current sprint |
| `blocked` | Waiting on dependency |

## Sprint Milestones

Format: `Sprint-YYYY-WW` (e.g., `Sprint-2026-09`)

## Confluence Integration

For dense documentation (detailed use cases, design docs), the PM agent will:
1. Generate content in markdown format
2. Output it for you to paste into Confluence
3. Reference the Confluence page in GitHub issues

To enable direct Confluence integration, configure an MCP server for Confluence.

## Documentation Locations

| Document Type | Location |
|---------------|----------|
| Sprint reports | `docs/pm/sprints/` |
| Use cases | `docs/pm/use-cases/` OR Confluence |
| Project status | `docs/pm/PROJECT_STATUS.md` |
| Technical ADRs | `docs/decisions/` |
| Architecture | `ARCHITECTURE.md`, `docs/architecture/` |
