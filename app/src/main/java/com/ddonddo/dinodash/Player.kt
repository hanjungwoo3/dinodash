package com.ddonddo.dinodash

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Player(private val screenWidth: Int, private val screenHeight: Int) {
    var x: Float = 150f
    var y: Float = 0f
    val width: Float = 48f  // 픽셀 단위 (16x3)
    val height: Float = 48f  // 픽셀 단위 (16x3)
    private val pixelSize: Float = 3f  // 각 픽셀의 크기
    
    private var velocityY: Float = 0f
    private val gravity: Float = 1.5f
    private val jumpForce: Float = -25f
    
    private val groundY: Float
    private var isJumping: Boolean = false
    
    // 스킨 타입
    enum class SkinType {
        NORMAL,  // 일반 삼색 고양이
        GOLD,    // 금색 고양이 (점프 1000번 업적)
        SKULL    // 해골 고양이 (비밀 코드: ddonddo)
    }
    
    private var currentSkin: SkinType = SkinType.NORMAL
    
    // 픽셀 아트용 색상 (일반)
    private val whitePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = false  // 픽셀 아트는 안티앨리어싱 끄기
    }
    
    private val blackPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val orangePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FF9966")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val pinkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FF6699")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val grayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#808080")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    // 골드 스킨용 색상
    private val goldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FFD700")  // 황금색
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val goldDarkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#DAA520")  // 진한 황금색
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val goldLightPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FFF8DC")  // 밝은 황금색
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    // 해골 스킨용 색상
    private val skullWhitePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F5F5F5")  // 뼈 색
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val skullGrayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#A0A0A0")  // 어두운 뼈
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val skullDarkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2C2C2C")  // 해골 윤곽
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val skullRedPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#FF0000")  // 빨간 눈
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    init {
        // 지면 위치 설정 (화면 하단에서 80픽셀 위)
        groundY = screenHeight - 120f
        y = groundY
    }
    
    // 픽셀 그리기 헬퍼 함수
    private fun drawPixel(canvas: Canvas, px: Int, py: Int, paint: Paint) {
        canvas.drawRect(
            x + px * pixelSize,
            y + py * pixelSize,
            x + (px + 1) * pixelSize,
            y + (py + 1) * pixelSize,
            paint
        )
    }
    
    fun update() {
        // 중력 적용
        velocityY += gravity
        y += velocityY
        
        // 지면에 닿으면 점프 상태 해제
        if (y >= groundY) {
            y = groundY
            velocityY = 0f
            isJumping = false
        }
    }
    
    fun jump() {
        // 지면에 있을 때만 점프 가능
        if (!isJumping) {
            velocityY = jumpForce
            isJumping = true
        }
    }
    
    fun setSkin(skinType: SkinType) {
        currentSkin = skinType
    }
    
    fun draw(canvas: Canvas) {
        // 🎮 16x16 픽셀 아트 고양이 (개선 버전)
        
        val frame = if (isJumping) 0 else (System.currentTimeMillis() / 200 % 2).toInt()
        
        // 픽셀 아트 데이터 (16x16)
        // 0=투명, 1=흰색, 2=검은색(윤곽선), 3=주황색, 4=분홍색
        val catPixels = arrayOf(
            // 점프 프레임
            arrayOf(
                intArrayOf(0,0,0,2,2,2,2,0,0,3,3,3,3,0,0,0),  // 귀
                intArrayOf(0,0,2,2,1,1,2,2,3,3,1,1,3,3,0,0),
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),  // 머리
                intArrayOf(0,0,2,1,2,1,2,1,1,2,1,2,1,3,0,0),  // 눈
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),
                intArrayOf(0,0,2,1,1,1,4,4,1,1,1,1,1,2,0,0),  // 코
                intArrayOf(0,0,2,2,1,4,1,4,1,1,1,1,2,2,0,0),  // 입
                intArrayOf(0,2,1,1,2,2,2,2,1,3,1,1,1,1,2,0),  // 몸통 시작
                intArrayOf(0,2,1,1,1,1,3,1,1,1,1,3,1,1,2,0),
                intArrayOf(0,2,1,1,3,1,1,1,2,1,1,1,1,1,2,0),  // 몸통
                intArrayOf(0,2,1,1,1,1,1,3,1,1,3,1,1,2,0,0),
                intArrayOf(0,0,2,1,1,2,1,1,1,1,1,1,2,0,0,0),
                intArrayOf(0,0,2,2,1,1,2,2,2,1,1,2,2,0,0,0),  // 다리 (모음)
                intArrayOf(0,0,0,2,1,1,2,0,2,1,1,2,0,0,0,0),
                intArrayOf(0,0,0,2,2,2,2,0,2,2,2,2,0,0,0,0),
                intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            ),
            // 달리기 프레임 1
            arrayOf(
                intArrayOf(0,0,0,2,2,2,2,0,0,3,3,3,3,0,0,0),
                intArrayOf(0,0,2,2,1,1,2,2,3,3,1,1,3,3,0,0),
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),
                intArrayOf(0,0,2,1,2,1,2,1,1,2,1,2,1,3,0,0),
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),
                intArrayOf(0,0,2,1,1,1,4,4,1,1,1,1,1,2,0,0),
                intArrayOf(0,0,2,2,1,4,1,4,1,1,1,1,2,2,0,0),
                intArrayOf(0,2,1,1,2,2,2,2,1,3,1,1,1,1,2,0),
                intArrayOf(0,2,1,1,1,1,3,1,1,1,1,3,1,1,2,0),
                intArrayOf(0,2,1,1,3,1,1,1,2,1,1,1,1,1,2,0),
                intArrayOf(0,2,1,1,1,1,1,3,1,1,3,1,1,2,0,0),
                intArrayOf(0,0,2,1,1,2,1,1,1,1,1,1,2,0,0,0),
                intArrayOf(0,0,2,2,1,1,2,0,0,2,2,0,0,0,0,0),  // 왼쪽 다리 앞
                intArrayOf(0,0,0,2,1,1,2,0,0,0,2,2,1,1,2,0),  // 오른쪽 다리 뒤
                intArrayOf(0,0,0,2,2,2,2,0,0,0,2,2,2,2,2,0),
                intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            ),
            // 달리기 프레임 2
            arrayOf(
                intArrayOf(0,0,0,2,2,2,2,0,0,3,3,3,3,0,0,0),
                intArrayOf(0,0,2,2,1,1,2,2,3,3,1,1,3,3,0,0),
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),
                intArrayOf(0,0,2,1,2,1,2,1,1,2,1,2,1,3,0,0),
                intArrayOf(0,0,2,1,1,1,1,1,1,1,1,1,1,3,0,0),
                intArrayOf(0,0,2,1,1,1,4,4,1,1,1,1,1,2,0,0),
                intArrayOf(0,0,2,2,1,4,1,4,1,1,1,1,2,2,0,0),
                intArrayOf(0,2,1,1,2,2,2,2,1,3,1,1,1,1,2,0),
                intArrayOf(0,2,1,1,1,1,3,1,1,1,1,3,1,1,2,0),
                intArrayOf(0,2,1,1,3,1,1,1,2,1,1,1,1,1,2,0),
                intArrayOf(0,2,1,1,1,1,1,3,1,1,3,1,1,2,0,0),
                intArrayOf(0,0,2,1,1,2,1,1,1,1,1,1,2,0,0,0),
                intArrayOf(0,0,0,2,2,0,0,2,2,1,1,2,0,0,0,0),  // 오른쪽 다리 앞
                intArrayOf(0,0,2,2,1,1,2,0,2,1,1,2,0,0,0,0),  // 왼쪽 다리 뒤
                intArrayOf(0,0,2,2,2,2,2,0,2,2,2,2,0,0,0,0),
                intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)
            )
        )
        
        val currentFrame = if (isJumping) catPixels[0] else catPixels[frame + 1]
        
        // 픽셀 아트 그리기 (스킨에 따라 색상 변경)
        for (py in 0 until 16) {
            for (px in 0 until 16) {
                val color = currentFrame[py][px]
                val paint = when (currentSkin) {
                    SkinType.GOLD -> {
                        // 골드 스킨
                        when (color) {
                            1 -> goldLightPaint  // 흰색 -> 밝은 금색
                            2 -> goldDarkPaint   // 검은색 -> 진한 금색
                            3 -> goldPaint       // 주황색 -> 금색
                            4 -> goldPaint       // 분홍색 -> 금색
                            else -> continue
                        }
                    }
                    SkinType.SKULL -> {
                        // 해골 스킨
                        when (color) {
                            1 -> skullWhitePaint  // 흰색 -> 뼈 색
                            2 -> skullDarkPaint   // 검은색 -> 해골 윤곽
                            3 -> skullGrayPaint   // 주황색 -> 어두운 뼈
                            4 -> skullRedPaint    // 분홍색 -> 빨간 눈
                            else -> continue
                        }
                    }
                    else -> {
                        // 일반 스킨
                        when (color) {
                            1 -> whitePaint
                            2 -> blackPaint
                            3 -> orangePaint
                            4 -> pinkPaint
                            else -> continue  // 0은 건너뛰기
                        }
                    }
                }
                drawPixel(canvas, px, py, paint)
            }
        }
    }
    
    fun getBounds(): RectF {
        // 충돌 감지용 경계 박스 (약간 작게 설정하여 게임을 더 쉽게)
        return RectF(
            x + 5f,
            y + 5f,
            x + width - 5f,
            y + height - 5f
        )
    }
    
    fun reset() {
        y = groundY
        velocityY = 0f
        isJumping = false
    }
}

