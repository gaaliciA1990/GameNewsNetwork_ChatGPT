package com.gamenews.data

import com.gamenews.models.Admin
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

/**
 * Class for admin queries
 */
class AdminRepository(
    private val admin: CoroutineCollection<Admin>
) {

    /**
     * Query for an IP in the repo and return a boolean for
     * whether the IP address is an admin
     */
    suspend fun getAdminByIp(ip: String): Boolean {
        return admin.findOne(Admin::ip eq ip) != null
    }
}
