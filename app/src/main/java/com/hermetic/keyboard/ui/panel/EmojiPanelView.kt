package com.hermetic.keyboard.ui.panel

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.hermetic.keyboard.R
import com.hermetic.keyboard.ime.HermeticIME

/**
 * Emoji panel with common emoji categories.
 */
class EmojiPanelView(
    context: Context,
    private val onEmojiSelected: (String) -> Unit
) : LinearLayout(context) {

    private lateinit var emojiGrid: RecyclerView
    private lateinit var categoryBar: LinearLayout
    private var currentCategory = 0

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.background))
        setupPanel()
    }

    private fun setupPanel() {
        addView(createCategoryBar())
        addView(createEmojiGrid())
        addView(createBottomBar())
        selectCategory(0)
    }

    private fun createCategoryBar(): HorizontalScrollView {
        val scroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }
        categoryBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6))
        }

        EMOJI_CATEGORIES.forEachIndexed { index, cat ->
            categoryBar.addView(TextView(context).apply {
                text = cat.first
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
                setBackgroundColor(ContextCompat.getColor(context,
                    if (index == 0) R.color.category_selected else R.color.category_unselected))
                layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = dpToPx(4)
                }
                tag = index
                setOnClickListener { selectCategory(index) }
            })
        }

        scroll.addView(categoryBar)
        return scroll
    }

    private fun createEmojiGrid(): RecyclerView {
        emojiGrid = RecyclerView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            val cols = ((context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density) / 44).toInt().coerceIn(7, 10)
            layoutManager = GridLayoutManager(context, cols)
        }
        return emojiGrid
    }

    private fun createBottomBar(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(44))
            setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

            addView(makeNavBtn("ABC") { (context as? HermeticIME)?.switchToMainKeyboard() })
            addView(makeNavBtn("🔮") { (context as? HermeticIME)?.switchToHermeticPanel() })
            addView(makeNavBtn("⌫") { (context as? HermeticIME)?.deleteBackward() })
        }
    }

    private fun makeNavBtn(label: String, action: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.on_background))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setBackgroundResource(R.drawable.key_background)
            setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6))
            layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                marginEnd = dpToPx(8)
            }
            setOnClickListener { action() }
        }
    }

    private fun selectCategory(index: Int) {
        currentCategory = index
        val emojis = EMOJI_CATEGORIES[index].second
        emojiGrid.adapter = EmojiAdapter(emojis, onEmojiSelected)

        for (i in 0 until categoryBar.childCount) {
            val tab = categoryBar.getChildAt(i) as? TextView ?: continue
            tab.setBackgroundColor(ContextCompat.getColor(context,
                if (i == index) R.color.category_selected else R.color.category_unselected))
        }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        val EMOJI_CATEGORIES = listOf(
            "Smileys" to listOf("😀","😃","😄","😁","😆","😅","🤣","😂","🙂","😉","😊","😇","🥰","😍","🤩","😘","😗","😚","😋","😛","😜","🤪","😝","🤑","🤗","🤭","🤫","🤔","🤐","🤨","😐","😑","😶","😏","😒","🙄","😬","😮‍💨","🤥","😌","😔","😪","🤤","😴","😷","🤒","🤕","🤢","🤮","🥵","🥶","🥴","😵","🤯","🤠","🥳","🥸","😎","🤓","🧐"),
            "Gestos" to listOf("👋","🤚","🖐","✋","🖖","👌","🤌","🤏","✌","🤞","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝","👍","👎","✊","👊","🤛","🤜","👏","🙌","👐","🤲","🤝","🙏","✍","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁","👅","👄"),
            "Pessoas" to listOf("👶","🧒","👦","👧","🧑","👱","👨","🧔","👩","🧓","👴","👵","🙍","🙎","🙅","🙆","💁","🙋","🧏","🙇","🤦","🤷","👮","🕵","💂","🥷","👷","🤴","👸","👳","🧕","🤵","👰","🤰","🫃","🤱","👼","🎅","🤶","🦸","🦹","🧙","🧚","🧛","🧜","🧝","🧞","🧟","💆","💇","🚶","🧍","🧎","🏃","💃","🕺"),
            "Natureza" to listOf("🌵","🎄","🌲","🌳","🌴","🪵","🌱","🌿","☘","🍀","🎍","🪴","🎋","🍃","🍂","🍁","🪻","🪷","🌾","🌺","🌻","🌹","🥀","🌷","🪹","🌼","🌸","💐","🍄","🌰","🐚","🪸","🪨","🌎","🌍","🌏","🌕","🌖","🌗","🌘","🌑","🌒","🌓","🌔","🌚","🌝","🌛","🌜","☀","🌤","⛅","🌥","🌦","🌧","⛈","🌩","🌨","❄","☃","⛄","🌬","💨","🌪","🌫","🌈","☔"),
            "Comida" to listOf("🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶","🫑","🌽","🥕","🫒","🧄","🧅","🥔","🍠","🥐","🍞","🥖","🥨","🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍖","🌭","🍔","🍟","🍕","🫓","🥪","🌮","🌯","🫔","🥙","🧆","🥗","🍝","🍜","🍲","🍛","🍣","🍱","🥟","🦪","🍤"),
            "Objetos" to listOf("⌚","📱","💻","⌨","🖥","🖨","🖱","🖲","🕹","🗜","💽","💾","💿","📀","📼","📷","📸","📹","🎥","📽","🎬","📺","📻","🎙","🎚","🎛","🧭","⏱","⏲","⏰","🕰","⌛","📡","🔋","🔌","💡","🔦","🕯","🧯","🛢","💸","💵","💴","💶","💷","🪙","💰","💳","💎","⚖","🪜","🧰","🪛","🔧","🔨","⚒","🛠","⛏","🪚","🔩","⚙","🪤","🧱","⛓","🧲","🔫","💣","🧨","🪓","🔪","🗡","⚔","🛡","🚬","⚰","🪦","⚱","🏺","🔮","📿","🧿","🪬","💈","⚗","🔭","🔬","🕳","🩹","🩺","🩻","🩼","💊","💉","🩸","🧬","🦠","🧫","🧪","🌡","🧹","🪠","🧺","🧻","🚽"),
            "Símbolos" to listOf("❤","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣","💕","💞","💓","💗","💖","💘","💝","💟","☮","✝","☪","🕉","☸","✡","🔯","🕎","☯","☦","🛐","⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛","🉑","☢","☣","📴","📳","🈶","🈚","🈸","🈺","🈷","✴","🆚","💮","🉐","㊙","㊗","🈴","🈵","🈹","🈲","🅰","🅱","🆎","🆑","🅾","🆘","❌","⭕","🛑","⛔","📛","🚫","💯","💢","♨","🚷","🚯","🚳","🚱","🔞","📵","🚭","❗","❕","❓","❔","‼","⁉","🔅","🔆","〽","⚠","🚸","🔱","⚜","🔰","♻","✅","🈯","💹","❇","✳","❎","🌐","💠","Ⓜ","🌀","💤","🏧","🚾","♿","🅿","🛗","🈳","🈂","🛂","🛃","🛄","🛅","🚹","🚺","🚻","🚼","🚮","🎦","📶","🈁","🔣","ℹ","🔤","🔡","🔠","🆖","🆗","🆙","🆒","🆕","🆓","0⃣","1⃣","2⃣","3⃣","4⃣","5⃣","6⃣","7⃣","8⃣","9⃣","🔟","🔢","#⃣","*⃣","⏏","▶","⏸","⏯","⏹","⏺","⏭","⏮","⏩","⏪","⏫","⏬","◀","🔼","🔽","➡","⬅","⬆","⬇","↗","↘","↙","↖","↕","↔","↩","↪","⤴","⤵","🔀","🔁","🔂","🔄","🔃","🎵","🎶","➕","➖","➗","✖","♾","💲","💱","™","©","®","👁‍🗨","🔚","🔙","🔛","🔝","🔜","〰","➰","➿","✔","☑","🔘","🔴","🟠","🟡","🟢","🔵","🟣","⚫","⚪","🟤","🔺","🔻","🔸","🔹","🔶","🔷","🔳","🔲","▪","▫","◾","◽","◼","◻","🟥","🟧","🟨","🟩","🟦","🟪","⬛","⬜","🟫","🔈","🔇","🔉","🔊","🔔","🔕","📣","📢","💬","💭","🗯","♠","♣","♥","♦","🃏","🎴","🀄")
        )
    }
}

class EmojiAdapter(
    private val emojis: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.VH>() {

    class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, parent.context.resources.displayMetrics).toInt()
        val tv = TextView(parent.context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            layoutParams = ViewGroup.LayoutParams(size, size)
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = emojis[position]
        holder.tv.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onSelect(emojis[position])
        }
    }

    override fun getItemCount() = emojis.size
}
