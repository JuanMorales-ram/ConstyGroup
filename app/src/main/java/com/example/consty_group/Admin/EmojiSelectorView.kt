package com.example.consty_group.admin

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.consty_group.R

/**
 * EmojiSelectorView
 * ─────────────────
 * Selector horizontal de emojis para el compositor de notificaciones.
 */
class EmojiSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : HorizontalScrollView(context, attrs, defStyle) {

    private val emojis = listOf(
        "🔔", "🌅", "⏰", "🔥", "🎉",
        "💪", "⭐", "✅", "❤️", "🚀", "💡", "🎯"
    )

    private var selectedEmoji = "🔔"
    private var onEmojiSelected: ((String) -> Unit)? = null

    private val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        val pad = dp(4)
        setPadding(pad, pad, pad, pad)
    }

    init {
        isHorizontalScrollBarEnabled = false
        emojis.forEach { emoji -> row.addView(buildItem(emoji)) }
        addView(row)
    }

    /** Registra un callback que se llama al seleccionar un emoji. */
    fun setOnEmojiSelectedListener(listener: (String) -> Unit) {
        onEmojiSelected = listener
    }

    /** Cambia el emoji seleccionado desde código (p.ej. al aplicar una plantilla). */
    fun setSelectedEmoji(emoji: String) {
        selectedEmoji = emoji
        refreshHighlight()
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun buildItem(emoji: String): TextView {
        val size = dp(44)
        return TextView(context).apply {
            text = emoji
            textSize = 20f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = dp(6)
            }
            background = bgFor(emoji)
            setOnClickListener {
                selectedEmoji = emoji
                refreshHighlight()
                onEmojiSelected?.invoke(emoji)
            }
        }
    }

    private fun refreshHighlight() {
        for (i in 0 until row.childCount) {
            val child = row.getChildAt(i) as? TextView ?: continue
            child.background = bgFor(child.text.toString())
        }
    }

    private fun bgFor(emoji: String) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        val colorRes = if (emoji == selectedEmoji) R.color.todosUsuarios else R.color.progressBarVacia
        setColor(ContextCompat.getColor(context, colorRes))
    }

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()
}