import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class ArchitectureGuardPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyArchitectureGuard()
    }
}

internal fun Project.applyArchitectureGuard() {
    val self = path
    configurations.configureEach {
        val configurationName = name
        dependencies.withType(ProjectDependency::class.java).configureEach {
            val target = path
            val violation = architectureViolation(self, target)
            if (violation != null) {
                throw GradleException(
                    "Architecture rule broken in $self ($configurationName -> $target): $violation.",
                )
            }
        }
    }
}
