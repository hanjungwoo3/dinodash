package com.ddonddo.dinodash

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), Runnable {
    private var thread: Thread? = null
    private var isPlaying: Boolean = false
    private val surfaceHolder: SurfaceHolder = holder
    private val gameContext: Context = context
    
    private lateinit var player: Player
    private val obstacles = mutableListOf<Obstacle>()
    private val backgroundElements = mutableListOf<BackgroundElement>()
    
    private var score: Int = 0
    private var highScore: Int = 0
    private var gameOver: Boolean = false
    private var gameStarted: Boolean = false
    private var jumpCount: Int = 0
    private var showAchievements: Boolean = false
    private var showCodeInput: Boolean = false
    private var secretCode: String = ""
    private var achievementScrollOffset: Float = 0f  // 업적창 스크롤 오프셋
    
    private var frameCount: Int = 0
    private val obstacleSpawnRate: Int = 90 // 프레임 수 (약 1.5초)
    private var backgroundSpawnCounter: Int = 0
    
    // 사운드 매니저
    private lateinit var soundManager: SoundManager
    
    // 업적 매니저
    private lateinit var achievementManager: AchievementManager
    private val newAchievements = mutableListOf<AchievementManager.Achievement>()
    private var achievementDisplayTime: Long = 0
    
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 60f
        textAlign = Paint.Align.CENTER
    }
    
    private val smallTextPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    
    private val groundPaint = Paint().apply {
        color = Color.parseColor("#808080")
        strokeWidth = 3f
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
    
    init {
        loadHighScore()
        soundManager = SoundManager(context)
        achievementManager = AchievementManager(context)
        
        // 시간대 업적 체크
        achievementManager.checkNightTime()
        achievementManager.checkMorningTime()
    }
    
    override fun run() {
        while (isPlaying) {
            update()
            draw()
            sleep()
        }
    }
    
    private fun update() {
        if (!gameStarted || gameOver) return
        
        // 플레이어 업데이트
        player.update()
        
        // 배경 요소 업데이트 및 최적화 (최대 15개까지만 유지)
        backgroundElements.forEach { it.update() }
        backgroundElements.removeAll { it.isOffScreen() }
        
        // 배경 요소 생성 (최대 개수 제한)
        backgroundSpawnCounter++
        if (backgroundSpawnCounter >= 90 && backgroundElements.size < 15) {  // 1.5초마다, 최대 15개
            spawnBackgroundElement()
            backgroundSpawnCounter = 0
        }
        
        // 장애물 업데이트
        obstacles.forEach { it.update() }
        
        // 화면 밖으로 나간 장애물 제거 및 점수 증가
        obstacles.removeAll { obstacle ->
            if (obstacle.isOffScreen()) {
                score++
                // 점수 업적 체크
                val achievements = achievementManager.checkAchievements(score, jumpCount)
                if (achievements.isNotEmpty()) {
                    newAchievements.addAll(achievements)
                    achievementDisplayTime = System.currentTimeMillis()
                }
                true
            } else {
                false
            }
        }
        
        // 새 장애물 생성
        frameCount++
        if (frameCount >= obstacleSpawnRate) {
            obstacles.add(Obstacle(width, height))
            frameCount = 0
        }
        
        // 충돌 감지
        checkCollision()
    }
    
    private fun spawnBackgroundElement() {
        val type = when ((0..20).random()) {
            in 0..3 -> BackgroundElement.ElementType.CLOUD_SMALL
            in 4..6 -> BackgroundElement.ElementType.CLOUD_LARGE
            in 7..8 -> BackgroundElement.ElementType.MOUNTAIN_SMALL
            in 9..10 -> BackgroundElement.ElementType.MOUNTAIN_LARGE
            in 11..15 -> BackgroundElement.ElementType.MOUNTAIN_FAR_SMALL  // 멀리 있는 작은 산 (더 많이)
            else -> BackgroundElement.ElementType.MOUNTAIN_FAR_LARGE  // 멀리 있는 큰 산 (더 많이)
        }
        backgroundElements.add(BackgroundElement(width, height, type))
    }
    
    private fun checkCollision() {
        val playerBounds = player.getBounds()
        
        for (obstacle in obstacles) {
            val obstacleBounds = obstacle.getBounds()
            if (playerBounds.intersect(obstacleBounds)) {
                gameOver = true
                soundManager.playGameOverSound() // 게임 오버 소리 재생
                
                // 게임 오버 업적 체크 및 알림
                val gameEndAchievements = achievementManager.recordGameEnd()
                if (gameEndAchievements.isNotEmpty()) {
                    newAchievements.addAll(gameEndAchievements)
                    achievementDisplayTime = System.currentTimeMillis()
                }
                
                updateHighScore()
                break
            }
        }
    }
    
    private fun draw() {
        if (surfaceHolder.surface.isValid) {
            val canvas = surfaceHolder.lockCanvas()
            
            // 배경 그리기
            canvas.drawColor(Color.WHITE)
            
            if (showCodeInput) {
                // 코드 입력창 그리기
                drawCodeInputScreen(canvas)
            } else if (showAchievements) {
                // 업적창 그리기
                drawAchievementScreen(canvas)
            } else if (!gameStarted) {
                // 시작 화면
                canvas.drawText(
                    context.getString(R.string.tap_to_start),
                    width / 2f,
                    height / 2f - 50f,
                    textPaint
                )
                
                // 업적 버튼
                drawAchievementButton(canvas)
                
                // 코드 입력 버튼
                drawCodeInputButton(canvas)
                
                // 최고 점수 표시
                val highScorePaint = Paint().apply {
                    color = Color.DKGRAY
                    textSize = 45f
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText(
                    "최고 점수: $highScore",
                    width / 2f,
                    height / 2f + 50f,
                    highScorePaint
                )
            } else {
                // 배경 요소 그리기 (레이어별로 정렬)
                val sortedBackground = backgroundElements.sortedBy { it.getLayer() }
                sortedBackground.forEach { it.draw(canvas) }
                
                // 지면 그리기 (더 굵고 명확하게)
                val groundY = height - 120f
                
                // 지면 선 (두껍게)
                val thickGroundPaint = Paint().apply {
                    color = Color.parseColor("#404040")
                    strokeWidth = 8f
                    isAntiAlias = false
                }
                canvas.drawLine(0f, groundY, width.toFloat(), groundY, thickGroundPaint)
                
                // 지면 아래 채우기 (배경과 구분)
                val groundFillPaint = Paint().apply {
                    color = Color.parseColor("#C8C8C8")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), groundFillPaint)
                
                // 장애물 그리기
                obstacles.forEach { it.draw(canvas) }
                
                // 플레이어 그리기 (맨 앞)
                player.draw(canvas)
                
                // 점수 표시
                canvas.drawText(
                    context.getString(R.string.score, score),
                    width / 2f,
                    100f,
                    smallTextPaint
                )
                
                // 업적 개수 표시
                val achievementText = "🏆 ${achievementManager.getUnlockedCount()}/${achievementManager.getTotalCount()}"
                val achievementPaint = Paint().apply {
                    color = Color.parseColor("#FFD700")
                    textSize = 35f
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(achievementText, width - 50f, 80f, achievementPaint)
                
                // 새 업적 알림
                if (newAchievements.isNotEmpty() && 
                    System.currentTimeMillis() - achievementDisplayTime < 3000) {
                    val achievement = newAchievements.first()
                    
                    // 반투명 배경 (골드 스킨 업적일 경우 금색)
                    val isGoldAchievement = achievement.id == "total_jumps_1000"
                    val bgPaint = Paint().apply {
                        color = if (isGoldAchievement) Color.parseColor("#CCFFD700") else Color.parseColor("#CC000000")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(
                        width / 2f - 250f, 150f,
                        width / 2f + 250f, 280f,
                        20f, 20f, bgPaint
                    )
                    
                    // 업적 텍스트
                    val achievementTitlePaint = Paint().apply {
                        color = if (isGoldAchievement) Color.parseColor("#8B4513") else Color.parseColor("#FFD700")
                        textSize = 45f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.drawText(
                        "${achievement.icon} ${achievement.title}",
                        width / 2f, 190f, achievementTitlePaint
                    )
                    
                    val achievementDescPaint = Paint().apply {
                        color = if (isGoldAchievement) Color.parseColor("#8B4513") else Color.WHITE
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.drawText(
                        achievement.description,
                        width / 2f, 230f, achievementDescPaint
                    )
                    
                    // 골드 스킨 업적일 경우 추가 메시지
                    if (isGoldAchievement) {
                        val goldMsgPaint = Paint().apply {
                            color = Color.parseColor("#FF6347")
                            textSize = 35f
                            textAlign = Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                        canvas.drawText(
                            "🌟 골드 스킨 획득! 🌟",
                            width / 2f, 265f, goldMsgPaint
                        )
                    }
                } else if (newAchievements.isNotEmpty() && 
                    System.currentTimeMillis() - achievementDisplayTime >= 3000) {
                    // 3초 지나면 다음 업적 표시
                    newAchievements.removeAt(0)
                    if (newAchievements.isNotEmpty()) {
                        achievementDisplayTime = System.currentTimeMillis()
                    }
                }
                
                // 게임 오버 화면
                if (gameOver) {
                    canvas.drawText(
                        context.getString(R.string.game_over),
                        width / 2f,
                        height / 2f - 100f,
                        textPaint
                    )
                    canvas.drawText(
                        context.getString(R.string.score, score),
                        width / 2f,
                        height / 2f,
                        smallTextPaint
                    )
                    canvas.drawText(
                        context.getString(R.string.high_score, highScore),
                        width / 2f,
                        height / 2f + 60f,
                        smallTextPaint
                    )
                    canvas.drawText(
                        context.getString(R.string.tap_to_restart),
                        width / 2f,
                        height / 2f + 150f,
                        smallTextPaint
                    )
                }
            }
            
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }
    
    private fun sleep() {
        try {
            Thread.sleep(16) // 약 60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    fun pause() {
        isPlaying = false
        try {
            thread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
    
    fun cleanup() {
        soundManager.release()
    }
    
    fun resume() {
        isPlaying = true
        thread = Thread(this)
        thread?.start()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                
                if (showCodeInput) {
                    // 코드 입력창에서 뒤로가기 버튼 클릭
                    if (isBackButtonClicked(x, y)) {
                        showCodeInput = false
                        return true
                    }
                    // 코드 입력 버튼들 처리
                    handleCodeInputTouch(x, y)
                } else if (showAchievements) {
                    // 업적창에서 뒤로가기 버튼 클릭
                    if (isBackButtonClicked(x, y)) {
                        showAchievements = false
                        achievementScrollOffset = 0f
                        return true
                    }
                } else if (!gameStarted) {
                    // 시작 화면에서 업적 버튼 클릭
                    if (isAchievementButtonClicked(x, y)) {
                        showAchievements = true
                        achievementScrollOffset = 0f
                        return true
                    }
                    // 코드 입력 버튼 클릭
                    if (isCodeInputButtonClicked(x, y)) {
                        showCodeInput = true
                        secretCode = ""
                        return true
                    }
                    // 게임 시작
                    startGame()
                } else if (gameOver) {
                    restartGame()
                } else {
                    player.jump()
                    jumpCount++
                    soundManager.playJumpSound() // 점프 소리 재생
                    
                    // 점프 업적 체크
                    val jumpAchievements = achievementManager.recordJump()
                    if (jumpAchievements.isNotEmpty()) {
                        newAchievements.addAll(jumpAchievements)
                        achievementDisplayTime = System.currentTimeMillis()
                    }
                    
                    // 첫 점프 및 점수 업적 체크
                    val achievements = achievementManager.checkAchievements(score, jumpCount)
                    if (achievements.isNotEmpty()) {
                        newAchievements.addAll(achievements)
                        achievementDisplayTime = System.currentTimeMillis()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // 업적창 스크롤 처리
                if (showAchievements && event.historySize > 0) {
                    val deltaY = event.y - event.getHistoricalY(event.historySize - 1)
                    achievementScrollOffset += deltaY
                    // 스크롤 범위 제한
                    val maxScroll = 0f
                    val minScroll = -(achievementManager.getTotalCount() * 90f - height + 500f)
                    achievementScrollOffset = achievementScrollOffset.coerceIn(minScroll, maxScroll)
                }
            }
        }
        return true
    }
    
    private fun isAchievementButtonClicked(x: Float, y: Float): Boolean {
        val buttonX = width / 2f
        val buttonY = height / 2f + 150f
        val buttonWidth = 300f
        val buttonHeight = 80f
        
        return x >= buttonX - buttonWidth / 2 && x <= buttonX + buttonWidth / 2 &&
               y >= buttonY - buttonHeight / 2 && y <= buttonY + buttonHeight / 2
    }
    
    private fun isBackButtonClicked(x: Float, y: Float): Boolean {
        val buttonX = 100f
        val buttonY = 100f
        val buttonSize = 100f
        
        return x >= buttonX - buttonSize / 2 && x <= buttonX + buttonSize / 2 &&
               y >= buttonY - buttonSize / 2 && y <= buttonY + buttonSize / 2
    }
    
    private fun startGame() {
        // 뷰 크기가 결정된 후에 플레이어 초기화
        post {
            player = Player(width, height)
            
            val prefs = gameContext.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
            
            // 💀 해골 스킨 (비밀 코드) - 최우선
            if (prefs.getBoolean("skull_skin_unlocked", false)) {
                player.setSkin(Player.SkinType.SKULL)
            }
            // 🌟 점프 1000번 업적 달성 시 골드 스킨 적용
            else if (achievementManager.isUnlocked("total_jumps_1000")) {
                player.setSkin(Player.SkinType.GOLD)
            }
            
            gameStarted = true
            
            // 게임 시작 시 배경 요소들을 미리 채워넣기 (끊김 없이)
            for (i in 0..10) {
                val xOffset = i * (width / 10f)
                val element = BackgroundElement(width, height, 
                    if (i % 2 == 0) BackgroundElement.ElementType.MOUNTAIN_FAR_LARGE 
                    else BackgroundElement.ElementType.MOUNTAIN_FAR_SMALL)
                element.x = xOffset
                backgroundElements.add(element)
            }
        }
    }
    
    private fun restartGame() {
        player.reset()
        
        val prefs = gameContext.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        
        // 💀 해골 스킨 (비밀 코드) - 최우선
        if (prefs.getBoolean("skull_skin_unlocked", false)) {
            player.setSkin(Player.SkinType.SKULL)
        }
        // 🌟 점프 1000번 업적 달성 시 골드 스킨 유지
        else if (achievementManager.isUnlocked("total_jumps_1000")) {
            player.setSkin(Player.SkinType.GOLD)
        }
        
        obstacles.clear()
        backgroundElements.clear()
        score = 0
        jumpCount = 0
        frameCount = 0
        backgroundSpawnCounter = 0
        gameOver = false
        newAchievements.clear()
        
        // 배경 요소 다시 채워넣기
        for (i in 0..10) {
            val xOffset = i * (width / 10f)
            val element = BackgroundElement(width, height, 
                if (i % 2 == 0) BackgroundElement.ElementType.MOUNTAIN_FAR_LARGE 
                else BackgroundElement.ElementType.MOUNTAIN_FAR_SMALL)
            element.x = xOffset
            backgroundElements.add(element)
        }
    }
    
    private fun loadHighScore() {
        highScore = sharedPreferences.getInt("high_score", 0)
    }
    
    private fun updateHighScore() {
        if (score > highScore) {
            highScore = score
            sharedPreferences.edit().putInt("high_score", highScore).apply()
        }
    }
    
    private fun drawAchievementButton(canvas: Canvas) {
        val buttonX = width / 2f
        val buttonY = height / 2f + 150f
        val buttonWidth = 300f
        val buttonHeight = 80f
        
        // 버튼 배경
        val buttonPaint = Paint().apply {
            color = Color.parseColor("#FFD700")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
            buttonX + buttonWidth / 2, buttonY + buttonHeight / 2,
            20f, 20f, buttonPaint
        )
        
        // 버튼 테두리
        val borderPaint = Paint().apply {
            color = Color.parseColor("#FFA500")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
            buttonX + buttonWidth / 2, buttonY + buttonHeight / 2,
            20f, 20f, borderPaint
        )
        
        // 버튼 텍스트
        val buttonTextPaint = Paint().apply {
            color = Color.parseColor("#8B4513")
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(
            "🏆 업적 (${achievementManager.getUnlockedCount()}/${achievementManager.getTotalCount()})",
            buttonX,
            buttonY + 15f,
            buttonTextPaint
        )
    }
    
    private fun drawAchievementScreen(canvas: Canvas) {
        // 뒤로가기 버튼
        val backButtonPaint = Paint().apply {
            color = Color.parseColor("#808080")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            50f, 50f, 150f, 150f,
            20f, 20f, backButtonPaint
        )
        
        val backTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("←", 100f, 115f, backTextPaint)
        
        // 제목
        val titlePaint = Paint().apply {
            color = Color.parseColor("#FFD700")
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(
            "🏆 업적 목록 🏆",
            width / 2f,
            100f,
            titlePaint
        )
        
        // 달성 현황
        val statusPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 35f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "달성: ${achievementManager.getUnlockedCount()} / ${achievementManager.getTotalCount()}",
            width / 2f,
            160f,
            statusPaint
        )
        
        // 업적 목록
        val achievements = achievementManager.getAllAchievements()
        val startY = 220f
        val itemHeight = 90f
        
        val achievementPaint = Paint().apply {
            textSize = 28f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        
        val iconPaint = Paint().apply {
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        var currentY = startY + achievementScrollOffset
        achievements.forEach { (achievement, unlocked) ->
            // 화면 밖이면 건너뛰기 (하지만 그리기는 계속)
            if (currentY < 180f || currentY > height - 50f) {
                currentY += itemHeight
                return@forEach
            }
            
            val isGoldAchievement = achievement.id == "total_jumps_1000"
            
            // 아이템 배경 (골드 업적은 특별하게)
            val bgPaint = Paint().apply {
                color = when {
                    unlocked && isGoldAchievement -> Color.parseColor("#FFF8DC")  // 골드 배경
                    unlocked -> Color.parseColor("#E8F5E9")
                    else -> Color.parseColor("#F5F5F5")
                }
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                50f, currentY - 40f,
                width - 50f, currentY + 40f,
                15f, 15f, bgPaint
            )
            
            // 테두리 (골드 업적은 금색 테두리)
            val borderPaint = Paint().apply {
                color = when {
                    unlocked && isGoldAchievement -> Color.parseColor("#FFD700")  // 금색 테두리
                    unlocked -> Color.parseColor("#4CAF50")
                    else -> Color.parseColor("#BDBDBD")
                }
                style = Paint.Style.STROKE
                strokeWidth = if (isGoldAchievement && unlocked) 5f else 3f
            }
            canvas.drawRoundRect(
                50f, currentY - 40f,
                width - 50f, currentY + 40f,
                15f, 15f, borderPaint
            )
            
            // 아이콘
            canvas.drawText(achievement.icon, 100f, currentY + 15f, iconPaint)
            
            // 제목
            achievementPaint.color = if (unlocked) Color.parseColor("#2E7D32") else Color.GRAY
            achievementPaint.isFakeBoldText = true
            canvas.drawText(achievement.title, 150f, currentY, achievementPaint)
            
            // 설명
            achievementPaint.textSize = 22f
            achievementPaint.isFakeBoldText = false
            achievementPaint.color = if (unlocked) Color.DKGRAY else Color.LTGRAY
            canvas.drawText(achievement.description, 150f, currentY + 28f, achievementPaint)
            achievementPaint.textSize = 28f
            
            // 잠금/달성 표시
            val statusIcon = if (unlocked) "✓" else "🔒"
            achievementPaint.textSize = 35f
            canvas.drawText(statusIcon, width - 100f, currentY + 10f, achievementPaint)
            achievementPaint.textSize = 28f
            
            currentY += itemHeight
        }
        
        // 통계
        val stats = achievementManager.getStats()
        val statsY = height - 150f
        
        val statsPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        // 통계 배경
        val statsBgPaint = Paint().apply {
            color = Color.parseColor("#FFF8E1")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            50f, statsY - 50f,
            width - 50f, statsY + 80f,
            15f, 15f, statsBgPaint
        )
        
        canvas.drawText("📊 통계", width / 2f, statsY - 10f, statsPaint)
        statsPaint.textSize = 24f
        canvas.drawText(
            "총 점프: ${stats["total_jumps"]}  |  총 플레이: ${stats["total_games"]}  |  총 죽음: ${stats["total_deaths"]}",
            width / 2f,
            statsY + 25f,
            statsPaint
        )
        canvas.drawText(
            "최고 점수: $highScore",
            width / 2f,
            statsY + 60f,
            statsPaint
        )
    }
    
    private fun isCodeInputButtonClicked(x: Float, y: Float): Boolean {
        val buttonX = width / 2f
        val buttonY = height / 2f + 250f
        val buttonWidth = 300f
        val buttonHeight = 80f
        
        return x >= buttonX - buttonWidth / 2 && x <= buttonX + buttonWidth / 2 &&
               y >= buttonY - buttonHeight / 2 && y <= buttonY + buttonHeight / 2
    }
    
    private fun drawCodeInputButton(canvas: Canvas) {
        val buttonX = width / 2f
        val buttonY = height / 2f + 250f
        val buttonWidth = 300f
        val buttonHeight = 80f
        
        // 버튼 배경
        val buttonPaint = Paint().apply {
            color = Color.parseColor("#9C27B0")  // 보라색
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
            buttonX + buttonWidth / 2, buttonY + buttonHeight / 2,
            20f, 20f, buttonPaint
        )
        
        // 버튼 테두리
        val borderPaint = Paint().apply {
            color = Color.parseColor("#7B1FA2")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
            buttonX + buttonWidth / 2, buttonY + buttonHeight / 2,
            20f, 20f, borderPaint
        )
        
        // 버튼 텍스트
        val buttonTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(
            "💀 비밀 코드",
            buttonX,
            buttonY + 15f,
            buttonTextPaint
        )
    }
    
    private fun drawCodeInputScreen(canvas: Canvas) {
        // 뒤로가기 버튼
        val backButtonPaint = Paint().apply {
            color = Color.parseColor("#808080")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            50f, 50f, 150f, 150f,
            20f, 20f, backButtonPaint
        )
        
        val backTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("←", 100f, 115f, backTextPaint)
        
        // 제목
        val titlePaint = Paint().apply {
            color = Color.parseColor("#9C27B0")
            textSize = 60f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(
            "💀 비밀 코드 입력",
            width / 2f,
            150f,
            titlePaint
        )
        
        // 코드 표시창
        val codeBoxPaint = Paint().apply {
            color = Color.parseColor("#F5F5F5")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            width / 2f - 200f, 220f,
            width / 2f + 200f, 320f,
            15f, 15f, codeBoxPaint
        )
        
        val codeBoxBorderPaint = Paint().apply {
            color = Color.parseColor("#9C27B0")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            width / 2f - 200f, 220f,
            width / 2f + 200f, 320f,
            15f, 15f, codeBoxBorderPaint
        )
        
        // 입력된 코드 표시
        val codePaint = Paint().apply {
            color = Color.BLACK
            textSize = 50f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            if (secretCode.isEmpty()) "..." else secretCode,
            width / 2f,
            285f,
            codePaint
        )
        
        // 키보드 (알파벳)
        val keyboardY = 400f
        val keySize = 70f
        val keySpacing = 10f
        
        val keys = listOf(
            listOf("d", "o", "n", "d", "o"),
            listOf("clear", "확인")
        )
        
        var currentY = keyboardY
        keys.forEach { row ->
            val rowWidth = row.size * (keySize + keySpacing) - keySpacing
            var currentX = width / 2f - rowWidth / 2f
            
            row.forEach { key ->
                val keyWidth = if (key.length > 1) keySize * 1.5f else keySize
                
                // 키 배경
                val keyPaint = Paint().apply {
                    color = when (key) {
                        "확인" -> Color.parseColor("#4CAF50")
                        "clear" -> Color.parseColor("#F44336")
                        else -> Color.parseColor("#9C27B0")
                    }
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    currentX, currentY,
                    currentX + keyWidth, currentY + keySize,
                    10f, 10f, keyPaint
                )
                
                // 키 텍스트
                val keyTextPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 35f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.drawText(
                    key,
                    currentX + keyWidth / 2f,
                    currentY + keySize / 2f + 12f,
                    keyTextPaint
                )
                
                currentX += keyWidth + keySpacing
            }
            currentY += keySize + keySpacing
        }
        
        // 힌트
        val hintPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 30f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "힌트: 이 게임의 이름은...?",
            width / 2f,
            height - 100f,
            hintPaint
        )
    }
    
    private fun handleCodeInputTouch(x: Float, y: Float) {
        val keyboardY = 400f
        val keySize = 70f
        val keySpacing = 10f
        
        val keys = listOf(
            listOf("d", "o", "n", "d", "o"),
            listOf("clear", "확인")
        )
        
        var currentY = keyboardY
        keys.forEach { row ->
            val rowWidth = row.size * (keySize + keySpacing) - keySpacing
            var currentX = width / 2f - rowWidth / 2f
            
            row.forEach { key ->
                val keyWidth = if (key.length > 1) keySize * 1.5f else keySize
                
                if (x >= currentX && x <= currentX + keyWidth &&
                    y >= currentY && y <= currentY + keySize) {
                    
                    when (key) {
                        "clear" -> secretCode = ""
                        "확인" -> {
                            if (secretCode.lowercase() == "ddonddo") {
                                // 해골 스킨 적용
                                val prefs = context.getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("skull_skin_unlocked", true).apply()
                                showCodeInput = false
                                // 성공 메시지는 게임 시작 시 표시
                            } else {
                                secretCode = ""
                            }
                        }
                        else -> {
                            if (secretCode.length < 10) {
                                secretCode += key
                            }
                        }
                    }
                    return
                }
                
                currentX += keyWidth + keySpacing
            }
            currentY += keySize + keySpacing
        }
    }
}

