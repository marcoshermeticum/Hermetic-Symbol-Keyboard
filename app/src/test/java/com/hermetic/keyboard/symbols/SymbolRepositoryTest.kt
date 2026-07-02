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
        id = "planets_0", categoryId = "planets", name = "Sun",
        symbol = "☉", unicode = "U+2609",
        keywords = listOf("sun", "gold"), meaning = "Source of life."
    )

    private val testSymbol2 = Symbol(
        id = "planets_1", categoryId = "planets", name = "Moon",
        symbol = "☽", unicode = "U+263D",
        keywords = listOf("moon", "silver"), meaning = "Intuition."
    )

    private val testCategories = listOf(
        SymbolCategory(id = "planets", name = "Planetary", icon = "☉",
            symbols = listOf(testSymbol, testSymbol2)),
        SymbolCategory(id = "hebrew", name = "Hebrew", icon = "א",
            symbols = listOf(
                Symbol("hebrew_0", "hebrew", "Aleph", "א", "U+05D0",
                    keywords = listOf("aleph"), meaning = "Spirit", gematriaValue = 1)
            ))
    )

    @Before
    fun setup() {
        dataProvider = mockk()
        favoriteDao = mockk()
        recentDao = mockk()
        every { dataProvider.loadCategories() } returns testCategories
        every { dataProvider.getAllSymbols() } returns testCategories.flatMap { it.symbols }
        repository = SymbolRepository(dataProvider, favoriteDao, recentDao)
    }

    @Test
    fun `getCategories returns all categories`() {
        val cats = repository.getCategories()
        assertEquals(2, cats.size)
        assertEquals("planets", cats[0].id)
        assertEquals("hebrew", cats[1].id)
    }

    @Test
    fun `getSymbolsByCategory returns correct symbols`() {
        val symbols = repository.getSymbolsByCategory("planets")
        assertEquals(2, symbols.size)
        assertEquals("Sun", symbols[0].name)
        assertEquals("Moon", symbols[1].name)
    }

    @Test
    fun `getSymbolsByCategory returns empty for unknown`() {
        assertTrue(repository.getSymbolsByCategory("nonexistent").isEmpty())
    }

    @Test
    fun `getAllSymbols returns symbols from all categories`() {
        val all = repository.getAllSymbols()
        assertEquals(3, all.size)
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
        assertTrue(repository.isFavorite("planets_0"))
    }

    @Test
    fun `isFavorite returns false for non-favorite`() = runTest {
        coEvery { favoriteDao.isFavorite("unknown") } returns false
        assertFalse(repository.isFavorite("unknown"))
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
    fun `getRecents maps entities to symbols`() = runTest {
        coEvery { recentDao.getRecent(30) } returns listOf(
            RecentEntity("planets_0"), RecentEntity("planets_1")
        )
        val recents = repository.getRecents()
        assertEquals(2, recents.size)
        assertEquals("Sun", recents[0].name)
        assertEquals("Moon", recents[1].name)
    }

    @Test
    fun `getRecents skips symbols not in data`() = runTest {
        coEvery { recentDao.getRecent(30) } returns listOf(
            RecentEntity("planets_0"), RecentEntity("deleted_symbol")
        )
        val recents = repository.getRecents()
        assertEquals(1, recents.size)
        assertEquals("Sun", recents[0].name)
    }

    @Test
    fun `clearRecents calls DAO`() = runTest {
        coEvery { recentDao.clearAll() } just Runs
        repository.clearRecents()
        coVerify { recentDao.clearAll() }
    }

    @Test
    fun `getFavorites returns symbols matching favorite IDs`() = runTest {
        coEvery { favoriteDao.getAllIds() } returns listOf("planets_0", "hebrew_0")
        val favorites = repository.getFavorites()
        assertEquals(2, favorites.size)
        assertTrue(favorites.any { it.name == "Sun" })
        assertTrue(favorites.any { it.name == "Aleph" })
    }

    @Test
    fun `getFavorites returns empty when no favorites`() = runTest {
        coEvery { favoriteDao.getAllIds() } returns emptyList()
        val favorites = repository.getFavorites()
        assertTrue(favorites.isEmpty())
    }
}
