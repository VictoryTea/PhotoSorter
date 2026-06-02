package com.example.photosorter.util

interface AudioPlayer {
    fun playKeepSound(themeId: String = "default")
    fun playTrashSound(themeId: String = "default")
    fun playAlbumSound(themeId: String = "default")
    fun playSkipSound(themeId: String = "default")
    fun playThemeSelectSound(themeId: String)
    fun setMuted(muted: Boolean)
    fun isMuted(): Boolean
    fun release()
}

class DummyAudioPlayer : AudioPlayer {
    override fun playKeepSound(themeId: String) {}
    override fun playTrashSound(themeId: String) {}
    override fun playAlbumSound(themeId: String) {}
    override fun playSkipSound(themeId: String) {}
    override fun playThemeSelectSound(themeId: String) {}
    override fun setMuted(muted: Boolean) {}
    override fun isMuted(): Boolean = false
    override fun release() {}
}
