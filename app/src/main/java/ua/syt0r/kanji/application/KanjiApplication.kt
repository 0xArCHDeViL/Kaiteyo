package ua.syt0r.kanji.application

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.di.appComponentsModule
import ua.syt0r.kanji.di.appModules
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ua.syt0r.kanji.flavorModule

class KanjiApplication : Application(), KoinComponent {

    companion object {
        private val modules = appModules + flavorModule + appComponentsModule
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@KanjiApplication)
            loadKoinModules(modules)
        }
        
        // Initialize CrashAnalyticEngine
        val appPreferences: ua.syt0r.kanji.core.user_data.preferences.PreferencesContract.AppPreferences by inject()
        CrashAnalyticEngine(this, appPreferences)
    }

}