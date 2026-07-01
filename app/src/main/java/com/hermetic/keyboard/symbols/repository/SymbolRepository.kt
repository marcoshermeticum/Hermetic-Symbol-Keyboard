package com.hermetic.keyboard.symbols.repository

import com.hermetic.keyboard.symbols.data.FavoriteDao
import com.hermetic.keyboard.symbols.data.FavoriteEntity
import com.hermetic.keyboard.symbols.data.RecentDao
import com.hermetic.keyboard.symbols.data.RecentEntity
import com.hermetic.keyboard.symbols.data.SymbolDataProvider
import com.hermetic.keyboard.symbols.model.Symbol
import com.hermetic.keyboard.symbols.model.SymbolCategory

/**
 * Repository pattern for accessing symbols, favorites, and recents.
 * Single source of truth for the UI layer.
 */
class SymbolRepository(
    private val dataProvider: SymbolDataProvider,
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao
) {

    fun getCategories(): List<SymbolCategory> = dataProvider.loadCategories()

    fun getAllSymbols(): List<Symbol> = dataProvider.getAllSymbols()

    fun getSymbolsByCategory(categoryId: String): List<Symbol> {
        return dataProvider.loadCategories()
            .find { it.id == categoryId }
            ?.symbols ?: emptyList()
    }

    // Favorites
    suspend fun getFavorites(): List<Symbol> {
        val favoriteIds = favoriteDao.getAllIds().toSet()
        return getAllSymbols().filter { it.id in favoriteIds }
    }

    suspend fun addFavorite(symbol: Symbol) {
        favoriteDao.insert(FavoriteEntity(symbolId = symbol.id))
    }

    suspend fun removeFavorite(symbol: Symbol) {
        favoriteDao.deleteById(symbol.id)
    }

    suspend fun isFavorite(symbolId: String): Boolean {
        return favoriteDao.isFavorite(symbolId)
    }

    // Recents
    suspend fun getRecents(): List<Symbol> {
        val recentEntities = recentDao.getRecent(30)
        val symbolMap = getAllSymbols().associateBy { it.id }
        return recentEntities.mapNotNull { symbolMap[it.symbolId] }
    }

    suspend fun addRecent(symbol: Symbol) {
        recentDao.insert(RecentEntity(symbolId = symbol.id))
        recentDao.trimToSize(30)
    }

    suspend fun clearRecents() {
        recentDao.clearAll()
    }
}
