private const val APP_MODULE = ":app"
private const val BASELINE_PROFILE_MODULE = ":baselineprofile" // test module that targets :app by design
private const val FEATURE_PREFIX = ":feature:"
private const val DATA_PREFIX = ":core:data:"
private const val DOMAIN_MODULE = ":core:domain"
private val UI_MODULES = setOf(":core:design", ":core:ui-common")

/** Returns the reason why [self] may not depend on [target], or null when the dependency is allowed. */
internal fun architectureViolation(self: String, target: String): String? = when {
    target == self -> null // a module's own test classpaths
    target == APP_MODULE && self == BASELINE_PROFILE_MODULE -> null
    target == APP_MODULE -> "nothing may depend on :app"
    self.startsWith(FEATURE_PREFIX) && target.startsWith(DATA_PREFIX) ->
        "feature modules may not depend on data modules; depend on the interfaces in :core:domain " +
            "and let :app wire the implementation"
    self == DOMAIN_MODULE &&
        (target.startsWith(FEATURE_PREFIX) || target.startsWith(DATA_PREFIX) || target in UI_MODULES) ->
        "the domain module may not depend on feature, data or UI modules (:core:design, :core:ui-common); " +
            "they depend on domain, not the other way around"
    else -> null
}
