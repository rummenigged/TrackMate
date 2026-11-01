import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ArchitectureRulesTest {
    private fun assertAllowed(self: String, target: String) =
        assertNull("$self -> $target should be allowed", architectureViolation(self, target))

    private fun assertForbidden(self: String, target: String) =
        assertNotNull("$self -> $target should be forbidden", architectureViolation(self, target))

    @Test
    fun `features may not depend on data modules`() {
        assertForbidden(":feature:home", ":core:data:database")
        assertForbidden(":feature:signIn", ":core:data:data-auth")
        assertForbidden(":feature:home", ":core:data:remote:nested")
    }

    @Test
    fun `features may depend on domain and shared core modules`() {
        assertAllowed(":feature:home", ":core:domain")
        assertAllowed(":feature:home", ":core:common")
        assertAllowed(":feature:home", ":core:design")
        assertAllowed(":feature:home", ":core:ui-common")
        assertAllowed(":feature:home", ":core:testing")
    }

    @Test
    fun `app may depend on data modules`() {
        assertAllowed(":app", ":core:data:data-entry")
        assertAllowed(":app", ":feature:home")
    }

    @Test
    fun `data modules may depend on each other and on domain`() {
        assertAllowed(":core:data:data-entry", ":core:data:database")
        assertAllowed(":core:data:data-entry", ":core:domain")
    }

    @Test
    fun `nothing may depend on app except baselineprofile`() {
        assertForbidden(":feature:home", ":app")
        assertForbidden(":core:domain", ":app")
        assertForbidden(":core:data:database", ":app")
        assertAllowed(":baselineprofile", ":app")
    }

    @Test
    fun `domain may not depend on features, data or ui modules`() {
        assertForbidden(":core:domain", ":feature:home")
        assertForbidden(":core:domain", ":core:data:database")
        assertForbidden(":core:domain", ":core:design")
        assertForbidden(":core:domain", ":core:ui-common")
    }

    @Test
    fun `domain may depend on other core modules and its own test fixtures`() {
        assertAllowed(":core:domain", ":core:testing")
        assertAllowed(":core:domain", ":core:common")
    }

    @Test
    fun `a module may depend on itself`() {
        assertAllowed(":feature:home", ":feature:home")
        assertAllowed(":core:domain", ":core:domain")
    }
}
