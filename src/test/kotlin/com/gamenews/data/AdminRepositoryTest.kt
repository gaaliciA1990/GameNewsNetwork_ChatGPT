package com.gamenews.data

import com.gamenews.models.Admin
import com.mongodb.client.result.UpdateResult
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import kotlin.test.*

/**
 * Unit tests for the ArticlesDB class
 */
class AdminRepositoryTest {
    lateinit var mockCollection: CoroutineCollection<Admin>
    lateinit var adminRepo: AdminRepository
    lateinit var publisher: CoroutineFindPublisher<Admin>

    @BeforeTest
    fun beforeEach() {
        mockCollection = mockk()
        adminRepo = AdminRepository(mockCollection)
        publisher = mockk()
    }

    @AfterTest
    fun aftereach() {
        clearAllMocks()
        unmockkAll()
    }

    /**
     * Testing getAdminIP returns valid Admin
     */
    @Test
    fun `getAdminByIp returns true with a valid admin IP from the repository`() = runTest {
        // SET UP
        val admin = Admin("12euh09rr1oi2h3", "134.123.5.432")

        coEvery { mockCollection.findOne(any<Bson>()) } returns admin

        // DO
        val isAdmin = adminRepo.getAdminByIp(admin.ip)

        // ASSERT
        assertTrue(isAdmin)
    }

    /**
     * Testing getAdminIp returns false
     */
    @Test
    fun `getAdminByIp returns false with an invalid admin IP from the repository`() = runTest {
        // SET UP
        val basicIp = "193.783.4.12"
        coEvery { mockCollection.findOne(any<Bson>()) } returns null

        // DO

        val isAdmin = adminRepo.getAdminByIp(basicIp)

        // ASSERT
        assertFalse(isAdmin)
    }
}
