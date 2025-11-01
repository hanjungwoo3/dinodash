package com.ddonddo.dinodash

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.random.Random

class Obstacle(private val screenWidth: Int, private val screenHeight: Int) {
    var x: Float = screenWidth.toFloat()
    private val width: Float
    private val height: Float
    private val y: Float
    private val pixelWidth: Int
    private val pixelHeight: Int
    private val pixelSize: Float = 3f
    
    private val speed: Float = 12f
    
    // 픽셀 아트용 색상
    private val waterDarkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#4A90E2")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val waterLightPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#7FB3E8")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val waterHighlightPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#B3D9FF")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    init {
        // 물웅덩이 크기 랜덤 생성 (픽셀 단위 - 세로로 더 크게)
        val type = Random.nextInt(3)
        when (type) {
            0 -> { // 작은 물웅덩이
                pixelWidth = 12
                pixelHeight = 8
            }
            1 -> { // 중간 물웅덩이
                pixelWidth = 16
                pixelHeight = 10
            }
            else -> { // 큰 물웅덩이
                pixelWidth = 20
                pixelHeight = 12
            }
        }
        
        width = pixelWidth * pixelSize
        height = pixelHeight * pixelSize
        
        // 지면 위치 (물웅덩이는 땅 바로 위)
        y = screenHeight - 120f - 5f  // 지면에서 살짝만 위로
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
        x -= speed
    }
    
    fun draw(canvas: Canvas) {
        // 💧 픽셀 아트 물웅덩이
        // 타원형 물웅덩이 패턴 (가운데가 더 넓은 형태)
        
        val shine = (System.currentTimeMillis() / 500 % 2).toInt()
        
        for (py in 0 until pixelHeight) {
            for (px in 0 until pixelWidth) {
                // 타원형 모양 만들기
                val centerX = pixelWidth / 2f
                val centerY = pixelHeight / 2f
                val dx = (px - centerX) / centerX
                val dy = (py - centerY) / centerY
                val dist = dx * dx + dy * dy
                
                if (dist <= 1.0f) {
                    // 물웅덩이 안쪽
                    val paint = when {
                        py == 0 && px >= pixelWidth / 3 && px <= pixelWidth * 2 / 3 && shine == 0 -> 
                            waterHighlightPaint  // 반짝임
                        py < pixelHeight / 3 -> 
                            waterLightPaint  // 밝은 부분
                        else -> 
                            waterDarkPaint  // 어두운 부분
                    }
                    drawPixel(canvas, px, py, paint)
                }
            }
        }
    }
    
    fun isOffScreen(): Boolean {
        return x + width < 0
    }
    
    fun getBounds(): RectF {
        // 충돌 감지용 경계 박스 (물웅덩이 중심 부분만 - 게임을 조금 더 쉽게)
        return RectF(
            x + width * 0.1f, 
            y + height * 0.2f, 
            x + width * 0.9f, 
            y + height
        )
    }
}

