rootProject.name = "slack-team-happiness"

include("app")

gradle.settingsEvaluated {
    if (JavaVersion.current() != JavaVersion.VERSION_17) {
        throw GradleException("This build requires JDK 17.")
    }
}