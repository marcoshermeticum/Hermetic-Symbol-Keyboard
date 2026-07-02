package com.hermetic.keyboard.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R

/**
 * Context-aware suggestion bar for Portuguese.
 * Uses the previous word to provide contextual predictions.
 */
class SuggestionBarView(
    context: Context,
    private val onSuggestionSelected: (String) -> Unit
) : LinearLayout(context) {

    private val suggestionViews = mutableListOf<TextView>()
    private var previousWord: String = ""

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.suggestion_bar))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(38))
        setPadding(dpToPx(4), 0, dpToPx(4), 0)

        repeat(3) { _ ->
            val tv = TextView(context).apply {
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.on_background))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                    marginStart = dpToPx(2)
                    marginEnd = dpToPx(2)
                }
                isClickable = true
                isFocusable = false
                setBackgroundResource(R.drawable.key_background)
                setOnClickListener {
                    val word = text?.toString() ?: ""
                    if (word.isNotEmpty()) onSuggestionSelected(word)
                }
            }
            suggestionViews.add(tv)
            addView(tv)
        }
    }

    fun updateSuggestions(currentWord: String) {
        val suggestions = getContextualSuggestions(currentWord, previousWord)
        suggestionViews.forEachIndexed { i, tv ->
            tv.text = suggestions.getOrNull(i) ?: ""
        }
    }

    fun onWordCompleted(word: String) {
        previousWord = word.lowercase()
    }

    fun clear() {
        suggestionViews.forEach { it.text = "" }
    }

    /**
     * Context-aware suggestions: uses previous word + current prefix.
     */
    private fun getContextualSuggestions(input: String, prevWord: String): List<String> {
        if (input.length < 2) return emptyList()
        val prefix = input.lowercase()

        // First try contextual pairs (previous word → likely next words)
        val contextual = WORD_PAIRS[prevWord]
            ?.filter { it.startsWith(prefix) }
            ?.take(2) ?: emptyList()

        // Then fill with dictionary matches
        val dictMatches = PORTUGUESE_WORDS
            .filter { it.startsWith(prefix) && it != prefix && it !in contextual }
            .take(3 - contextual.size)

        return (contextual + dictMatches).take(3)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    companion object {
        /** Contextual word pairs: previousWord → likely next words */
        private val WORD_PAIRS = mapOf(
            "advogado" to listOf("justiça", "jurídico", "jurisprudência", "judicial", "juri", "direito", "processo"),
            "bom" to listOf("dia", "trabalho", "momento", "resultado"),
            "boa" to listOf("noite", "tarde", "sorte", "viagem"),
            "muito" to listOf("obrigado", "obrigada", "bem", "bom", "importante"),
            "tudo" to listOf("bem", "certo", "pronto", "ok"),
            "por" to listOf("favor", "exemplo", "isso", "causa", "enquanto"),
            "de" to listOf("acordo", "novo", "repente", "fato", "qualquer"),
            "com" to listOf("certeza", "licença", "razão", "todo"),
            "está" to listOf("bem", "certo", "pronto", "aqui"),
            "pode" to listOf("ser", "fazer", "ajudar", "me"),
            "não" to listOf("sei", "posso", "tenho", "quero", "consigo"),
            "eu" to listOf("quero", "preciso", "acho", "sei", "tenho", "vou"),
            "você" to listOf("pode", "quer", "sabe", "precisa", "está"),
            "vou" to listOf("fazer", "tentar", "verificar", "enviar", "mandar"),
            "como" to listOf("está", "vai", "fazer", "funciona"),
            "qual" to listOf("é", "seria", "foi", "problema"),
            "obrigado" to listOf("pela", "por", "mesmo", "demais"),
            "direito" to listOf("penal", "civil", "constitucional", "trabalhista", "administrativo"),
            "processo" to listOf("judicial", "penal", "civil", "administrativo", "seletivo"),
            "lei" to listOf("federal", "estadual", "municipal", "complementar", "ordinária"),
            "tribunal" to listOf("justiça", "federal", "regional", "superior"),
            "energia" to listOf("vital", "cósmica", "solar", "lunar", "divina"),
            "ritual" to listOf("sagrado", "mágico", "diário", "lunar", "solar"),
            "meditação" to listOf("guiada", "profunda", "diária", "matinal"),
            "alquimia" to listOf("espiritual", "interna", "hermética", "transformação"),
            "símbolo" to listOf("sagrado", "alquímico", "astrológico", "hermético"),
            "planeta" to listOf("regente", "mercúrio", "vênus", "marte", "júpiter", "saturno")
        )

        private val PORTUGUESE_WORDS = listOf(
            "que", "não", "para", "com", "uma", "por", "mais", "como", "mas", "foi",
            "quando", "muito", "depois", "sobre", "também", "entre", "desde", "então",
            "porque", "ainda", "antes", "onde", "apenas", "algum", "alguma", "alguém",
            "ser", "estar", "ter", "fazer", "poder", "dizer", "dar", "ver", "saber",
            "querer", "chegar", "passar", "dever", "ficar", "deixar", "começar",
            "parecer", "viver", "achar", "pensar", "trabalhar", "conhecer", "falar",
            "olhar", "sentir", "ouvir", "pedir", "lembrar", "precisar", "gostar",
            "acreditar", "acontecer", "escrever", "perder", "encontrar", "receber",
            "entender", "seguir", "conseguir", "criar", "ajudar", "tentar",
            "tempo", "vida", "mundo", "casa", "coisa", "homem", "mulher", "dia",
            "vez", "ano", "governo", "pessoa", "parte", "cidade", "trabalho",
            "momento", "forma", "problema", "grupo", "país", "lugar", "caso",
            "ponto", "estado", "história", "exemplo", "família", "empresa",
            "água", "nome", "número", "noite", "verdade", "razão", "mão",
            "olho", "corpo", "palavra", "cabeça", "filho", "criança", "porta",
            "grande", "novo", "bom", "mesmo", "último", "longo", "certo",
            "melhor", "pouco", "primeiro", "próprio", "possível", "político",
            "pequeno", "brasileiro", "importante", "diferente", "social",
            "bonito", "forte", "feliz", "triste", "rápido", "lento",
            "obrigado", "obrigada", "desculpa", "perfeito", "combinado",
            "mensagem", "agora", "amanhã", "ontem", "hoje", "sempre", "nunca",
            "talvez", "aqui", "ali", "isso", "isto", "aquilo",
            "justiça", "juri", "jurisprudência", "judicial", "jurídico", "juiz",
            "advogado", "processo", "sentença", "recurso", "petição", "audiência",
            "direito", "constitucional", "penal", "civil", "trabalhista",
            "alquimia", "astrologia", "hermetismo", "kabbalah", "gematria",
            "zodíaco", "elemento", "planeta", "símbolo", "ritual", "meditação",
            "consciência", "espírito", "energia", "transformação", "iluminação",
            "sabedoria", "mistério", "sagrado", "divino", "cósmico", "celestial",
            "informação", "informações", "importante", "implementar", "incluir",
            "início", "internet", "investigação", "investimento"
        ).sorted()
    }
}
