package com.ddonddo.dinodash

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.random.Random

// 배경 요소 (구름, 산)
class BackgroundElement(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val type: ElementType
) {
    var x: Float = screenWidth.toFloat()
    private var y: Float
    private val speed: Float
    private val pixelSize: Float = 3f
    
    enum class ElementType {
        CLOUD_SMALL,
        CLOUD_LARGE,
        MOUNTAIN_SMALL,
        MOUNTAIN_LARGE,
        MOUNTAIN_FAR_SMALL,    // 멀리 있는 작은 산 (가장 뒤)
        MOUNTAIN_FAR_LARGE     // 멀리 있는 큰 산 (가장 뒤)
    }
    
    private val whitePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E0E0E0")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val grayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#A0A0A0")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    private val darkGrayPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#707070")
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    // 멀리 있는 산 색상 (초록색)
    private val farMountainDarkPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2D5016")  // 진한 초록
        style = Paint.Style.FILL
        isAntiAlias = false
    }

    private val farMountainLightPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#5A9216")  // 밝은 초록
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    
    init {
        when (type) {
            ElementType.CLOUD_SMALL -> {
                y = Random.nextFloat() * (screenHeight * 0.3f) + 50f
                speed = 2f
            }
            ElementType.CLOUD_LARGE -> {
                y = Random.nextFloat() * (screenHeight * 0.3f) + 80f
                speed = 1.5f
            }
            ElementType.MOUNTAIN_SMALL -> {
                y = screenHeight - 200f
                speed = 4f
            }
            ElementType.MOUNTAIN_LARGE -> {
                y = screenHeight - 250f
                speed = 3f
            }
            ElementType.MOUNTAIN_FAR_SMALL -> {
                y = screenHeight - 400f  // 적당한 크기
                speed = 0.5f  // 매우 느리게 (멀리 있는 효과)
            }
            ElementType.MOUNTAIN_FAR_LARGE -> {
                y = screenHeight - 500f  // 적당한 크기
                speed = 0.3f  // 매우 느리게
            }
        }
    }
    
    fun update() {
        x -= speed
    }
    
    fun draw(canvas: Canvas) {
        when (type) {
            ElementType.CLOUD_SMALL -> drawSmallCloud(canvas)
            ElementType.CLOUD_LARGE -> drawLargeCloud(canvas)
            ElementType.MOUNTAIN_SMALL -> drawSmallMountain(canvas)
            ElementType.MOUNTAIN_LARGE -> drawLargeMountain(canvas)
            ElementType.MOUNTAIN_FAR_SMALL -> drawFarSmallMountain(canvas)
            ElementType.MOUNTAIN_FAR_LARGE -> drawFarLargeMountain(canvas)
        }
    }
    
    fun getLayer(): Int {
        return when (type) {
            ElementType.MOUNTAIN_FAR_SMALL, ElementType.MOUNTAIN_FAR_LARGE -> 0  // 가장 뒤
            ElementType.CLOUD_SMALL, ElementType.CLOUD_LARGE -> 1  // 중간
            ElementType.MOUNTAIN_SMALL, ElementType.MOUNTAIN_LARGE -> 2  // 앞
        }
    }
    
    private fun drawPixel(canvas: Canvas, px: Int, py: Int, paint: Paint) {
        canvas.drawRect(
            x + px * pixelSize,
            y + py * pixelSize,
            x + (px + 1) * pixelSize,
            y + (py + 1) * pixelSize,
            paint
        )
    }
    
    private fun drawSmallCloud(canvas: Canvas) {
        // 작은 구름 (8x4 픽셀)
        val pattern = arrayOf(
            intArrayOf(0,1,1,1,1,0,0,0),
            intArrayOf(1,1,1,1,1,1,1,0),
            intArrayOf(1,1,1,1,1,1,1,1),
            intArrayOf(0,1,1,1,1,1,1,0)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                if (pattern[py][px] == 1) {
                    drawPixel(canvas, px, py, whitePaint)
                }
            }
        }
    }
    
    private fun drawLargeCloud(canvas: Canvas) {
        // 큰 구름 (12x6 픽셀)
        val pattern = arrayOf(
            intArrayOf(0,0,1,1,1,1,1,0,0,0,0,0),
            intArrayOf(0,1,1,1,1,1,1,1,1,0,0,0),
            intArrayOf(1,1,1,1,1,1,1,1,1,1,1,0),
            intArrayOf(1,1,1,1,1,1,1,1,1,1,1,1),
            intArrayOf(1,1,1,1,1,1,1,1,1,1,1,0),
            intArrayOf(0,1,1,1,1,1,1,1,1,1,0,0)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                if (pattern[py][px] == 1) {
                    drawPixel(canvas, px, py, whitePaint)
                }
            }
        }
    }
    
    private fun drawSmallMountain(canvas: Canvas) {
        // 작은 산 (10x8 픽셀)
        val pattern = arrayOf(
            intArrayOf(0,0,0,0,2,2,0,0,0,0),
            intArrayOf(0,0,0,2,2,2,2,0,0,0),
            intArrayOf(0,0,2,2,3,3,2,2,0,0),
            intArrayOf(0,2,2,3,3,3,3,2,2,0),
            intArrayOf(0,2,3,3,3,3,3,3,2,0),
            intArrayOf(2,2,3,3,3,3,3,3,2,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,2)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                val paint = when (pattern[py][px]) {
                    2 -> darkGrayPaint
                    3 -> grayPaint
                    else -> continue
                }
                drawPixel(canvas, px, py, paint)
            }
        }
    }
    
    private fun drawLargeMountain(canvas: Canvas) {
        // 큰 산 (16x12 픽셀)
        val pattern = arrayOf(
            intArrayOf(0,0,0,0,0,2,2,2,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,2,2,2,2,2,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,2,2,2,3,3,2,2,0,0,0,0,0,0),
            intArrayOf(0,0,2,2,2,3,3,3,3,2,2,0,0,0,0,0),
            intArrayOf(0,2,2,2,3,3,3,3,3,3,2,2,0,0,0,0),
            intArrayOf(0,2,2,3,3,3,3,3,3,3,3,2,2,0,0,0),
            intArrayOf(2,2,3,3,3,3,3,3,3,3,3,3,2,2,0,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                val paint = when (pattern[py][px]) {
                    2 -> darkGrayPaint
                    3 -> grayPaint
                    else -> continue
                }
                drawPixel(canvas, px, py, paint)
            }
        }
    }
    
    fun isOffScreen(): Boolean {
        return x < -200f  // 충분히 왼쪽으로 나가면
    }
    
    private fun drawFarSmallMountain(canvas: Canvas) {
        // 멀리 있는 작은 산 (20x15 픽셀, 초록색) - 더욱 최적화
        val pattern = arrayOf(
            intArrayOf(0,0,0,0,0,0,0,2,2,2,2,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,2,2,3,3,2,2,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,2,2,3,3,3,3,2,2,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,2,2,3,3,3,3,3,3,2,2,0,0,0,0,0,0),
            intArrayOf(0,0,0,2,2,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0),
            intArrayOf(0,0,2,2,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0),
            intArrayOf(0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0),
            intArrayOf(0,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0,0,0),
            intArrayOf(2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                val paint = when (pattern[py][px]) {
                    2 -> farMountainDarkPaint
                    3 -> farMountainLightPaint
                    else -> continue
                }
                drawPixel(canvas, px, py, paint)
            }
        }
    }
    
    private fun drawFarLargeMountain(canvas: Canvas) {
        // 멀리 있는 큰 산 (30x20 픽셀, 초록색) - 더욱 최적화
        val pattern = arrayOf(
            intArrayOf(0,0,0,0,0,0,0,0,0,0,0,2,2,2,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0,0,0,2,2,2,3,3,2,2,2,0,0,0,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0,0,2,2,3,3,3,3,3,3,2,2,0,0,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,0,2,2,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,0,2,2,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0,0,0),
            intArrayOf(0,0,0,0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0,0),
            intArrayOf(0,0,0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0,0),
            intArrayOf(0,0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0,0),
            intArrayOf(0,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0,0),
            intArrayOf(0,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0,0,0),
            intArrayOf(2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,0),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2),
            intArrayOf(2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2)
        )
        
        for (py in pattern.indices) {
            for (px in pattern[py].indices) {
                val paint = when (pattern[py][px]) {
                    2 -> farMountainDarkPaint
                    3 -> farMountainLightPaint
                    else -> continue
                }
                drawPixel(canvas, px, py, paint)
            }
        }
    }
}
