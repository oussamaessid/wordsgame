package app.wordgame


class WordGameApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        app.wordgame.di.AppContainer.initialize(this)
    }
}