package com.hermetic.keyboard.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hermetic.keyboard.R

/**
 * Context-aware prediction bar that suggests next words based on:
 * 1. N-gram model (previous 1-2 words → likely next words)
 * 2. Prefix matching when user starts typing
 * 3. User learning: tracks word frequency from usage
 *
 * Suggestions appear even before the user types, based on context.
 */
class SuggestionBarView(
    context: Context,
    private val onSuggestionSelected: (String) -> Unit
) : LinearLayout(context) {

    private val suggestionViews = mutableListOf<TextView>()
    private val wordHistory = mutableListOf<String>() // last N words typed
    private val prefs: SharedPreferences = context.getSharedPreferences("hermetic_predictions", Context.MODE_PRIVATE)

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
                    marginStart = dpToPx(2); marginEnd = dpToPx(2)
                }
                isClickable = true; isFocusable = false
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

    /**
     * Called when user is typing. Shows predictions filtered by prefix.
     */
    fun updateSuggestions(currentPrefix: String) {
        val suggestions = if (currentPrefix.isEmpty()) {
            getNextWordPredictions()
        } else {
            getPrefixSuggestions(currentPrefix)
        }
        displaySuggestions(suggestions)
    }

    /**
     * Called when a word is completed (space/punctuation pressed).
     * Triggers learning and shows next-word predictions.
     */
    fun onWordCompleted(word: String) {
        val clean = word.lowercase().trim()
        if (clean.length >= 2) {
            wordHistory.add(clean)
            if (wordHistory.size > 5) wordHistory.removeAt(0)
            learnWord(clean)
            learnBigram()
        }
        // Show next-word predictions immediately
        displaySuggestions(getNextWordPredictions())
    }

    fun clear() {
        suggestionViews.forEach { it.text = "" }
    }

    /**
     * Predictions when user hasn't typed yet (predict next whole word).
     */
    private fun getNextWordPredictions(): List<String> {
        val lastWord = wordHistory.lastOrNull() ?: return emptyList()
        val lastTwo = if (wordHistory.size >= 2) "${wordHistory[wordHistory.size - 2]} $lastWord" else null

        // Try bigram first (2 previous words)
        val bigramResults = lastTwo?.let { TRIGRAM_MODEL[it] } ?: emptyList()
        // Then single previous word
        val unigramResults = BIGRAM_MODEL[lastWord] ?: emptyList()
        // Merge user-learned bigrams
        val learnedResults = getLearnedNextWords(lastWord)

        return (learnedResults + bigramResults + unigramResults).distinct().take(3)
    }

    /**
     * Suggestions filtered by what the user is currently typing.
     */
    private fun getPrefixSuggestions(prefix: String): List<String> {
        val normalized = prefix.lowercase()
        val lastWord = wordHistory.lastOrNull()

        // Contextual: filter predicted words by prefix
        val contextual = if (lastWord != null) {
            val predicted = (BIGRAM_MODEL[lastWord] ?: emptyList()) +
                    (getLearnedNextWords(lastWord))
            predicted.filter { it.startsWith(normalized) }.take(2)
        } else emptyList()

        // Dictionary matches sorted by user frequency
        val dictMatches = WORDS.filter { it.startsWith(normalized) && it != normalized && it !in contextual }
        val scored = dictMatches.map { it to getWordFrequency(it) }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3 - contextual.size)

        return (contextual + scored).distinct().take(3)
    }

    // --- User learning ---

    private fun learnWord(word: String) {
        val key = "freq_$word"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    private fun learnBigram() {
        if (wordHistory.size >= 2) {
            val prev = wordHistory[wordHistory.size - 2]
            val curr = wordHistory.last()
            val key = "bigram_${prev}_$curr"
            val count = prefs.getInt(key, 0)
            prefs.edit().putInt(key, count + 1).apply()
        }
    }

    private fun getWordFrequency(word: String): Int = prefs.getInt("freq_$word", 0)

    private fun getLearnedNextWords(prevWord: String): List<String> {
        val allKeys = prefs.all.keys.filter { it.startsWith("bigram_${prevWord}_") }
        return allKeys.map { it.removePrefix("bigram_${prevWord}_") to prefs.getInt(it, 0) }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3)
    }

    private fun displaySuggestions(suggestions: List<String>) {
        suggestionViews.forEachIndexed { i, tv -> tv.text = suggestions.getOrNull(i) ?: "" }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    companion object {
        /** Bigram model: word → likely next words */
        private val BIGRAM_MODEL = mapOf(
            "a" to listOf("gente", "vida", "pessoa", "empresa"),
            "o" to listOf("que", "mundo", "problema", "governo"),
            "é" to listOf("que", "muito", "importante", "necessário", "preciso"),
            "está" to listOf("bem", "certo", "pronto", "aqui", "no"),
            "não" to listOf("sei", "posso", "tenho", "quero", "consigo", "é"),
            "eu" to listOf("quero", "preciso", "acho", "sei", "tenho", "vou", "estou"),
            "você" to listOf("pode", "quer", "sabe", "precisa", "está", "tem"),
            "vou" to listOf("fazer", "tentar", "verificar", "enviar", "mandar", "te"),
            "como" to listOf("está", "vai", "fazer", "funciona", "você"),
            "qual" to listOf("é", "seria", "foi", "problema", "motivo"),
            "obrigado" to listOf("pela", "por", "mesmo", "demais"),
            "muito" to listOf("obrigado", "obrigada", "bem", "bom", "importante"),
            "tudo" to listOf("bem", "certo", "pronto", "ok"),
            "por" to listOf("favor", "exemplo", "isso", "causa", "enquanto", "aqui"),
            "de" to listOf("acordo", "novo", "repente", "fato", "qualquer", "forma"),
            "com" to listOf("certeza", "licença", "razão", "todo", "você"),
            "bom" to listOf("dia", "trabalho", "momento", "resultado"),
            "boa" to listOf("noite", "tarde", "sorte", "viagem", "ideia"),
            "pode" to listOf("ser", "fazer", "ajudar", "me", "enviar"),
            "preciso" to listOf("de", "fazer", "saber", "ver", "que"),
            "quero" to listOf("saber", "fazer", "que", "ver", "ir"),
            "advogado" to listOf("justiça", "jurídico", "criminal", "trabalhista"),
            "política" to listOf("é", "está", "pública", "nacional", "social"),
            "direito" to listOf("penal", "civil", "constitucional", "trabalhista"),
            "processo" to listOf("judicial", "penal", "civil", "seletivo"),
            "lei" to listOf("federal", "estadual", "municipal", "complementar"),
            "tribunal" to listOf("de", "federal", "regional", "superior"),
            "energia" to listOf("vital", "cósmica", "solar", "lunar", "divina"),
            "ritual" to listOf("sagrado", "mágico", "diário", "lunar", "solar"),
            "meditação" to listOf("guiada", "profunda", "diária", "matinal"),
            "alquimia" to listOf("espiritual", "interna", "hermética", "transformação"),
            "símbolo" to listOf("sagrado", "alquímico", "astrológico", "hermético"),
            "planeta" to listOf("regente", "mercúrio", "vênus", "marte"),
            "me" to listOf("ajudar", "dizer", "falar", "enviar", "mandar"),
            "te" to listOf("amo", "ajudar", "mandar", "enviar", "agradeço"),
            "aqui" to listOf("está", "tem", "no", "na", "perto"),
            "fazer" to listOf("isso", "algo", "um", "o", "amanhã"),
            "hoje" to listOf("é", "está", "tem", "vou", "foi"),
            "amanhã" to listOf("eu", "vou", "tem", "será", "pode"),
            "porque" to listOf("eu", "não", "ele", "ela", "isso"),
            "quando" to listOf("eu", "você", "ele", "ela", "for"),
            "se" to listOf("você", "eu", "ele", "puder", "for", "quiser"),
            "mas" to listOf("eu", "não", "também", "acho", "preciso"),
            "também" to listOf("é", "tem", "quero", "preciso", "acho"),
            "ainda" to listOf("não", "é", "tem", "está", "bem"),
            "já" to listOf("é", "foi", "está", "sei", "fiz"),
            "depois" to listOf("de", "eu", "vou", "que", "disso"),
            "sempre" to listOf("que", "foi", "é", "será", "quis")
        )

        /** Trigram model: "word1 word2" → likely next words */
        private val TRIGRAM_MODEL = mapOf(
            "a política" to listOf("é", "está", "pública", "brasileira"),
            "a política está" to listOf("no", "em", "crescendo", "mudando"),
            "eu não" to listOf("sei", "posso", "quero", "consigo", "tenho"),
            "eu vou" to listOf("fazer", "tentar", "ver", "te", "enviar"),
            "você pode" to listOf("me", "fazer", "ajudar", "enviar"),
            "tudo bem" to listOf("com", "por", "obrigado", "aqui"),
            "por favor" to listOf("me", "envie", "faça", "verifique"),
            "bom dia" to listOf("como", "tudo", "você", "pessoal"),
            "boa noite" to listOf("como", "tudo", "pessoal"),
            "de acordo" to listOf("com", "vamos", "perfeito"),
            "com certeza" to listOf("vou", "pode", "sim"),
            "o que" to listOf("é", "você", "aconteceu", "houve", "fazer"),
            "por que" to listOf("você", "não", "isso", "ele"),
            "eu acho" to listOf("que", "isso", "interessante"),
            "eu preciso" to listOf("de", "fazer", "saber", "ir"),
            "não sei" to listOf("se", "como", "quando", "porque", "o"),
            "pode ser" to listOf("que", "amanhã", "depois", "sim"),
            "vai ser" to listOf("bom", "difícil", "fácil", "legal"),
            "tem que" to listOf("ser", "fazer", "ter", "ir")
        )

        /** Base dictionary for prefix matching */
        private val WORDS = listOf(
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
            "informação", "informações", "implementar", "incluir",
            "início", "internet", "investigação", "investimento",
            "política", "público", "pública", "nacional", "federal", "estadual",
            "municipal", "tecnologia", "digital", "sistema", "projeto", "programa",
            "resultado", "situação", "condição", "solução", "decisão", "questão"
        ).sorted()
    }
}
