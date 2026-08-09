import org.gradle.api.Plugin
import org.gradle.api.Project

class DummyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Dummy plugin to suppress the "No valid plugin descriptors" warning
        // that Gradle throws when java-gradle-plugin is applied without plugins.
    }
}
