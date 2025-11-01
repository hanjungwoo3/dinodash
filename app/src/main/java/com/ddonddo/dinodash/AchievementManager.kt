package com.ddonddo.dinodash

import android.content.Context
import android.content.SharedPreferences

class AchievementManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("Achievements", Context.MODE_PRIVATE)
    
    // 업적 정의
    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val icon: String  // 이모지
    )
    
    private val achievements = listOf(
        Achievement("first_jump", "첫 점프", "첫 번째 점프를 했습니다", "🦘"),
        Achievement("first_death", "시작", "첫 번째 게임 오버", "😅"),
        Achievement("score_10", "새내기", "점수 10점 달성", "🌱"),
        Achievement("score_50", "숙련자", "점수 50점 달성", "⭐"),
        Achievement("score_100", "고수", "점수 100점 달성", "🏆"),
        Achievement("score_200", "마스터", "점수 200점 달성", "👑"),
        Achievement("score_500", "전설", "점수 500점 달성", "💎"),
        Achievement("death_10", "도전자", "10번 죽기", "💀"),
        Achievement("death_50", "불굴의 의지", "50번 죽기", "🔥"),
        Achievement("death_100", "끈기의 화신", "100번 죽기", "⚡"),
        Achievement("perfect_10", "완벽주의자", "연속 10번 완벽한 점프", "✨"),
        Achievement("play_10", "단골손님", "10회 플레이", "🎮"),
        Achievement("play_50", "열혈 플레이어", "50회 플레이", "🎯"),
        Achievement("play_100", "게임 중독", "100회 플레이", "🕹️"),
        Achievement("total_jumps_100", "점프왕", "총 100회 점프", "🚀"),
        Achievement("total_jumps_500", "점프마스터", "총 500회 점프", "💪"),
        Achievement("total_jumps_1000", "점프 레전드", "총 1000회 점프 - 골드 스킨 획득!", "🌟"),
        Achievement("night_player", "야행성", "밤에 게임 플레이", "🌙"),
        Achievement("morning_player", "아침형 인간", "아침에 게임 플레이", "☀️"),
        Achievement("speed_demon", "스피드 러너", "빠른 속도로 50점 달성", "⚡")
    )
    
    // 통계
    private var totalJumps: Int
        get() = prefs.getInt("total_jumps", 0)
        set(value) = prefs.edit().putInt("total_jumps", value).apply()
    
    private var totalGames: Int
        get() = prefs.getInt("total_games", 0)
        set(value) = prefs.edit().putInt("total_games", value).apply()
    
    private var totalDeaths: Int
        get() = prefs.getInt("total_deaths", 0)
        set(value) = prefs.edit().putInt("total_deaths", value).apply()
    
    private var perfectJumps: Int
        get() = prefs.getInt("perfect_jumps", 0)
        set(value) = prefs.edit().putInt("perfect_jumps", value).apply()
    
    init {
        // 첫 실행 시 업적 초기화
        if (!prefs.contains("initialized")) {
            achievements.forEach { achievement ->
                prefs.edit().putBoolean(achievement.id, false).apply()
            }
            prefs.edit().putBoolean("initialized", true).apply()
        }
    }
    
    fun isUnlocked(achievementId: String): Boolean {
        return prefs.getBoolean(achievementId, false)
    }
    
    fun unlock(achievementId: String): Boolean {
        if (!isUnlocked(achievementId)) {
            prefs.edit().putBoolean(achievementId, true).apply()
            return true  // 새로 달성
        }
        return false  // 이미 달성됨
    }
    
    fun checkAchievements(score: Int, jumps: Int): List<Achievement> {
        val newAchievements = mutableListOf<Achievement>()
        
        // 점프 기록
        if (jumps > 0 && unlock("first_jump")) {
            newAchievements.add(achievements.find { it.id == "first_jump" }!!)
        }
        
        // 점수 업적
        if (score >= 10 && unlock("score_10")) {
            newAchievements.add(achievements.find { it.id == "score_10" }!!)
        }
        if (score >= 50 && unlock("score_50")) {
            newAchievements.add(achievements.find { it.id == "score_50" }!!)
        }
        if (score >= 100 && unlock("score_100")) {
            newAchievements.add(achievements.find { it.id == "score_100" }!!)
        }
        if (score >= 200 && unlock("score_200")) {
            newAchievements.add(achievements.find { it.id == "score_200" }!!)
        }
        if (score >= 500 && unlock("score_500")) {
            newAchievements.add(achievements.find { it.id == "score_500" }!!)
        }
        
        return newAchievements
    }
    
    fun recordJump(): List<Achievement> {
        totalJumps++
        
        val newAchievements = mutableListOf<Achievement>()
        
        // 총 점프 업적 체크
        if (totalJumps >= 100 && unlock("total_jumps_100")) {
            newAchievements.add(achievements.find { it.id == "total_jumps_100" }!!)
        }
        if (totalJumps >= 500 && unlock("total_jumps_500")) {
            newAchievements.add(achievements.find { it.id == "total_jumps_500" }!!)
        }
        if (totalJumps >= 1000 && unlock("total_jumps_1000")) {
            newAchievements.add(achievements.find { it.id == "total_jumps_1000" }!!)
        }
        
        return newAchievements
    }
    
    fun recordGameEnd(): List<Achievement> {
        totalGames++
        totalDeaths++
        
        val newAchievements = mutableListOf<Achievement>()
        
        // 첫 죽음
        if (totalDeaths == 1 && unlock("first_death")) {
            newAchievements.add(achievements.find { it.id == "first_death" }!!)
        }
        
        // 죽음 업적
        if (totalDeaths >= 10 && unlock("death_10")) {
            newAchievements.add(achievements.find { it.id == "death_10" }!!)
        }
        if (totalDeaths >= 50 && unlock("death_50")) {
            newAchievements.add(achievements.find { it.id == "death_50" }!!)
        }
        if (totalDeaths >= 100 && unlock("death_100")) {
            newAchievements.add(achievements.find { it.id == "death_100" }!!)
        }
        
        // 플레이 횟수 업적
        if (totalGames >= 10 && unlock("play_10")) {
            newAchievements.add(achievements.find { it.id == "play_10" }!!)
        }
        if (totalGames >= 50 && unlock("play_50")) {
            newAchievements.add(achievements.find { it.id == "play_50" }!!)
        }
        if (totalGames >= 100 && unlock("play_100")) {
            newAchievements.add(achievements.find { it.id == "play_100" }!!)
        }
        
        return newAchievements
    }
    
    fun recordPerfectJump() {
        perfectJumps++
        
        if (perfectJumps >= 10) {
            unlock("perfect_10")
        }
    }
    
    fun checkNightTime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 22 || hour < 6) {
            return unlock("night_player")
        }
        return false
    }
    
    fun checkMorningTime(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 6 && hour < 9) {
            return unlock("morning_player")
        }
        return false
    }
    
    fun getAllAchievements(): List<Pair<Achievement, Boolean>> {
        return achievements.map { it to isUnlocked(it.id) }
    }
    
    fun getUnlockedCount(): Int {
        return achievements.count { isUnlocked(it.id) }
    }
    
    fun getTotalCount(): Int {
        return achievements.size
    }
    
    fun getStats(): Map<String, Int> {
        return mapOf(
            "total_jumps" to totalJumps,
            "total_games" to totalGames,
            "total_deaths" to totalDeaths,
            "perfect_jumps" to perfectJumps
        )
    }
    
    fun getHighScore(): Int {
        return prefs.getInt("high_score", 0)
    }
    
    fun setHighScore(score: Int) {
        if (score > getHighScore()) {
            prefs.edit().putInt("high_score", score).apply()
        }
    }
}

