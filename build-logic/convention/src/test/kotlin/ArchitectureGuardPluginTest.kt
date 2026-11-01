import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureGuardPluginTest {
    private fun buildProjects(vararg paths: String): Map<String, Project> {
        val root = ProjectBuilder.builder().withName("root").build()
        val projects = mutableMapOf(":" to root)
        paths.forEach { path ->
            var parent = root
            var current = ""
            path.removePrefix(":").split(":").forEach { segment ->
                current += ":$segment"
                parent = projects.getOrPut(current) { ProjectBuilder.builder().withName(segment).withParent(parent).build() }
            }
        }
        projects.values.forEach { it.pluginManager.apply("trackmate.architecture-guard") }
        return projects
    }

    private fun Project.dependOn(target: Project, configuration: String = "implementation") {
        configurations.maybeCreate(configuration)
        dependencies.add(configuration, dependencies.project(mapOf("path" to target.path)))
    }

    private fun Throwable.messages(): String = generateSequence(this) { it.cause }.joinToString("\n") { it.message.orEmpty() }

    @Test
    fun `adding a forbidden project dependency fails with a descriptive message`() {
        val projects = buildProjects(":feature:home", ":core:data:database")

        val error = runCatching { projects.getValue(":feature:home").dependOn(projects.getValue(":core:data:database")) }
            .exceptionOrNull()

        val messages = error?.messages().orEmpty()
        assertTrue(messages, "Architecture rule broken in :feature:home (implementation -> :core:data:database)" in messages)
    }

    @Test
    fun `adding an allowed project dependency succeeds`() {
        val projects = buildProjects(":feature:home", ":core:domain")

        projects.getValue(":feature:home").dependOn(projects.getValue(":core:domain"))
    }

    @Test
    fun `the guard applies to test configurations as well`() {
        val projects = buildProjects(":core:domain", ":core:design")

        val error = runCatching {
            projects.getValue(":core:domain").dependOn(projects.getValue(":core:design"), "testImplementation")
        }.exceptionOrNull()

        assertTrue("(testImplementation -> :core:design)" in error?.messages().orEmpty())
    }
}
