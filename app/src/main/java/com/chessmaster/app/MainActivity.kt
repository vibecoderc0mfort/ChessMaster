
package com.chessmaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.view.View
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var board: ChessBoard
    private lateinit var status: TextView
    private var whiteSide = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.rgb(25, 30, 38))
        }

        val title = TextView(this).apply {
            text = "♟ ChessMaster"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        status = TextView(this).apply {
            text = "Вы играете белыми"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(8, 16, 8, 16)
        }
        root.addView(status)

        board = ChessBoard(this)
        root.addView(
            board,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        val flip = Button(this).apply {
            text = "🔄 Перевернуть доску"
            setOnClickListener { board.flipped = !board.flipped; board.invalidate() }
        }
        root.addView(flip)

        val side = Button(this).apply {
            text = "⚪⚫ Сменить сторону"
            setOnClickListener {
                whiteSide = !whiteSide
                board.flipped = !whiteSide
                board.invalidate()
                status.text = if (whiteSide)
                    "Вы играете белыми" else "Вы играете чёрными"
            }
        }
        root.addView(side)

        val best = Button(this).apply {
            text = "🎯 Лучший ход"
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "Следующий шаг — подключить шахматный движок Stockfish",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        root.addView(best)

        setContentView(root)
    }
}

class ChessBoard(context: android.content.Context) : View(context) {
    var flipped = false
    private var selected: Pair<Int, Int>? = null

    private val squares = arrayOf(
        charArrayOf('r','n','b','q','k','b','n','r'),
        CharArray(8) { 'p' },
        CharArray(8) { ' ' },
        CharArray(8) { ' ' },
        CharArray(8) { ' ' },
        CharArray(8) { ' ' },
        CharArray(8) { 'P' },
        charArrayOf('R','N','B','Q','K','B','N','R')
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height) / 8f
        val left = (width - size * 8) / 2f
        val top = (height - size * 8) / 2f

        for (y in 0..7) for (x in 0..7) {
            val sx = if (flipped) 7 - x else x
            val sy = if (flipped) 7 - y else y
            val x0 = left + x * size
            val y0 = top + y * size

            paint.color = if ((x + y) % 2 == 0)
                Color.rgb(238, 238, 210)
            else Color.rgb(118, 150, 86)
            canvas.drawRect(x0, y0, x0 + size, y0 + size, paint)

            if (selected == Pair(sy, sx)) {
                paint.color = Color.argb(120, 255, 215, 0)
                canvas.drawRect(x0, y0, x0 + size, y0 + size, paint)
            }

            val piece = squares[sy][sx]
            if (piece != ' ') {
                paint.color = if (piece.isUpperCase())
                    Color.WHITE else Color.BLACK
                paint.textSize = size * 0.78f
                paint.textAlign = Paint.Align.CENTER
                paint.style = Paint.Style.FILL
                val glyph = when (piece.lowercaseChar()) {
                    'k' -> "♚"
                    'q' -> "♛"
                    'r' -> "♜"
                    'b' -> "♝"
                    'n' -> "♞"
                    else -> "♟"
                }
                canvas.drawText(
                    glyph, x0 + size / 2,
                    y0 + size * 0.78f, paint
                )
            }
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action != android.view.MotionEvent.ACTION_UP)
            return true

        val size = minOf(width, height) / 8f
        val left = (width - size * 8) / 2f
        val top = (height - size * 8) / 2f
        val x = ((event.x - left) / size).toInt()
        val y = ((event.y - top) / size).toInt()

        if (x !in 0..7 || y !in 0..7) return true

        val file = if (flipped) 7 - x else x
        val rank = if (flipped) 7 - y else y
        val current = selected

        if (current == null) {
            if (squares[rank][file] != ' ')
                selected = Pair(rank, file)
        } else {
            squares[rank][file] = squares[current.first][current.second]
            squares[current.first][current.second] = ' '
            selected = null
        }
        invalidate()
        return true
    }
}
