package app.wordgame


class WordGameApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        _root_ide_package_.app.wordgame.di.AppContainer.initialize(this)
    }
}