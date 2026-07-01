package com.hermetic.keyboard.symbols

import com.hermetic.keyboard.symbols.data.FavoriteDao
import com.hermetic.keyboard.symbols.data.FavoriteEntity
import com.hermetic.keyboard.symbols.data.RecentDao
import com.hermetic.keyboard.symbols.data.RecentEntity
import com.hermetic.keyboard.symbols.data.SymbolDataProvider
import com.hermetic.keyboard.symbols.model.Symbol
import com.hermetic.keyboard.symbols.model.SymbolCategory
import com.hermetic.keyboard.symbols.repository.SymbolRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SymbolRepositoryTest {

    private lateinit var repository: SymbolRepository
    private lateinit var dataProvider: SymbolDataProvider
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var recentDao: RecentDao

    private val testSymbol = Symbol(
        id = "planets_0",
        categoryId = "planets",
        name = "Sun",
        symbol = "☉",
        unicode = "U+2609",
        keywords = listOf("sun", "gold"),
        meaning = "Source of life."
    )

    private val testCategories = listOf(
        SymbolCategory(
            id = "planets",
            name = "Planetary Symbols",
            icon = "☉",
            symbols = listOf(testSymbol)
        )
    )

    @Before
    fun setup() {
        dataProvider = mockk()
        favoriteDao = mockk()
        recentDao = mockk()

        every { dataProvider.loadCategories() } returns testCategories
        every { dataProvider.getAllSymbols() } returns listOf(testSymbol)

        repository = SymbolRepository(dataProvider, favoriteDao, recentDao)
    }

    @Test
    fun `getCategories returns all categories`() {
        val categories = repository.getCategories()
        assertEquals(1, categories.size)
        assertEquals("planets", categories.first().id)
    }

    @Test
    fun `getSymbolsByCategory returns correct symbols`() {
        val symbols = repository.getSymbolsByCategory("planets")
        assertEquals(1, symbols.size)
        assertEquals("Sun", symbols.first().name)
    }

    @Test
    fun `getSymbolsByCategory returns empty for unknown category`() {
        val symbols = repository.getSymbolsByCategory("nonexistent")
        assertTrue(symbols.isEmpty())
    }

    @Test
    fun `addFavorite inserts into DAO`() = runTest {
        coEvery { favoriteDao.insert(any()) } just Runs

        repository.addFavorite(testSymbol)

        coVerify { favoriteDao.insert(match { it.symbolId == "planets_0" }) }
    }

    @Test
    fun `removeFavorite deletes from DAO`() = runTest {
        coEvery { favoriteDao.deleteById("planets_0") } just Runs

        repository.removeFavorite(testSymbol)

        coVerify { favoriteDao.deleteById("planets_0") }
    }

    @Test
    fun `isFavorite delegates to DAO`() = runTest {
        coEvery { favoriteDao.isFavorite("planets_0") } returns true

        val result = repository.isFavorite("planets_0")

        assertTrue(result)
    }

    @Test
    fun `addRecent inserts and trims`() = runTest {
        coEvery { recentDao.insert(any()) } just Runs
        coEvery { recentDao.trimToSize(30) } just Runs

        repository.addRecent(testSymbol)

        coVerify { recentDao.insert(match { it.symbolId == "planets_0" }) }
        coVerify { recentDao.trimToSize(30) }
    }

    @Test
    fun `getRecents maps entities back to symbols`() = runTest {
        coEvery { recentDao.getRecent(30) } returns listOf(
            RecentEntity("planets_0", System.currentTimeMillis())
        )

        val recents = repository.getRecents()

        assertEquals(1, recents.size)
        assertEquals("Sun", recents.first().name)
    }

    @Test
    fun `clearRecents delegates to DAO`() = runTest {
        coEvery { recentDao.clearAll() } just Runs

        repository.clearRecents()

        coVerify { recentDao.clearAll() }
    }
}
