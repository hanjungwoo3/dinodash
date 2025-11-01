package com.ddonddo.dinodash

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

class SoundManager(context: Context) {
    private var soundPool: SoundPool? = null
    private var jumpSoundId: Int = 0
    private var gameOverSoundId: Int = 0
    private var isSoundEnabled: Boolean = true
    
    init {
        // SoundPool 설정 (효과음용)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(3) // 동시에 재생할 수 있는 최대 스트림 수
            .setAudioAttributes(audioAttributes)
            .build()
        
        // 사운드 파일 로드
        try {
            // 리소스 ID를 동적으로 가져오기 (파일이 없으면 0)
            val jumpResId = context.resources.getIdentifier("jump_sound", "raw", context.packageName)
            val gameOverResId = context.resources.getIdentifier("game_over_sound", "raw", context.packageName)
            
            if (jumpResId != 0) {
                jumpSoundId = soundPool?.load(context, jumpResId, 1) ?: 0
            }
            if (gameOverResId != 0) {
                gameOverSoundId = soundPool?.load(context, gameOverResId, 1) ?: 0
            }
        } catch (e: Exception) {
            // 사운드 파일이 없을 경우 에러 처리
            e.printStackTrace()
        }
    }
    
    fun playJumpSound() {
        if (isSoundEnabled && jumpSoundId != 0) {
            soundPool?.play(
                jumpSoundId,
                1.0f,  // 왼쪽 볼륨
                1.0f,  // 오른쪽 볼륨
                1,     // 우선순위
                0,     // 반복 (0 = 반복 안 함)
                1.0f   // 재생 속도
            )
        }
    }
    
    fun playGameOverSound() {
        if (isSoundEnabled && gameOverSoundId != 0) {
            soundPool?.play(
                gameOverSoundId,
                1.0f,
                1.0f,
                1,
                0,
                1.0f
            )
        }
    }
    
    fun enableSound() {
        isSoundEnabled = true
    }
    
    fun disableSound() {
        isSoundEnabled = false
    }
    
    fun toggleSound(): Boolean {
        isSoundEnabled = !isSoundEnabled
        return isSoundEnabled
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}


