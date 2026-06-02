package com.example.photosorter.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.photosorter.R
import kotlin.random.Random

class AndroidAudioPlayer(context: Context) : AudioPlayer {
    private val soundPool: SoundPool
    private val prefs = context.getSharedPreferences("audio_prefs", Context.MODE_PRIVATE)
    
    private val keepSoundId: Int
    private val trashSoundId: Int
    private val albumSoundId: Int
    private val skipSoundId: Int

    private val cowboySounds = mutableListOf<Int>()
    private val cowboyThemeSelectSoundId: Int
    
    private val meepSounds = mutableListOf<Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        keepSoundId = soundPool.load(context, R.raw.keep, 1)
        trashSoundId = soundPool.load(context, R.raw.trash, 1)
        albumSoundId = soundPool.load(context, R.raw.album, 1)
        skipSoundId = soundPool.load(context, R.raw.skip, 1)

        cowboySounds.add(soundPool.load(context, R.raw.cowboy_revolver2, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_revolver, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_ricochet3, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_ricochet2, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_neigh, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_tincan, 1))
        cowboySounds.add(soundPool.load(context, R.raw.cowboy_ricochet, 1))
        cowboyThemeSelectSoundId = soundPool.load(context, R.raw.cowboy_theme_select, 1)

        meepSounds.add(soundPool.load(context, R.raw.meep1, 1))
        meepSounds.add(soundPool.load(context, R.raw.meep2, 1))
        meepSounds.add(soundPool.load(context, R.raw.meep3, 1))
    }

    override fun setMuted(muted: Boolean) {
        prefs.edit().putBoolean("is_muted", muted).apply()
    }

    override fun isMuted(): Boolean {
        return prefs.getBoolean("is_muted", false)
    }

    private fun playSound(id: Int) {
        if (isMuted()) return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    private fun playRandom(sounds: List<Int>) {
        if (sounds.isEmpty()) return
        playSound(sounds[Random.nextInt(sounds.size)])
    }

    override fun playKeepSound(themeId: String) {
        when (themeId) {
            "cowboy" -> playRandom(cowboySounds)
            "meep" -> playRandom(meepSounds)
            else -> playSound(keepSoundId)
        }
    }

    override fun playTrashSound(themeId: String) {
        when (themeId) {
            "cowboy" -> playRandom(cowboySounds)
            "meep" -> playRandom(meepSounds)
            else -> playSound(trashSoundId)
        }
    }

    override fun playAlbumSound(themeId: String) {
        when (themeId) {
            "cowboy" -> playRandom(cowboySounds)
            "meep" -> playRandom(meepSounds)
            else -> playSound(albumSoundId)
        }
    }

    override fun playSkipSound(themeId: String) {
        when (themeId) {
            "cowboy" -> playRandom(cowboySounds)
            "meep" -> playRandom(meepSounds)
            else -> playSound(skipSoundId)
        }
    }
    
    override fun playThemeSelectSound(themeId: String) {
        when (themeId) {
            "cowboy" -> playSound(cowboyThemeSelectSoundId)
        }
    }

    override fun release() {
        soundPool.release()
    }
}
