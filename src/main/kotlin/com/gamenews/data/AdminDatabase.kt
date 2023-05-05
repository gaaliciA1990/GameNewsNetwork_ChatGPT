package com.gamenews.data

import com.gamenews.models.Admin
import org.litote.kmongo.coroutine.CoroutineCollection

/**
 * Class for admin queries
 */
class AdminRepository(
    private val admin: CoroutineCollection<Admin>
) {

    /**
     * Query for an IP in the DB and return a boolean for
     * whether the IP address is an admin
     */
    suspend fun getAdminByIp(ip: String): Boolean {
        return admin.findOne(ip) != null
    }
}