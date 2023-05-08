package com.gamenews.models

import org.bson.codecs.pojo.annotations.BsonId

/**
 * Model for tracking approve IP addresses to access CRUD features
 */
data class Admin (
    @BsonId
    val id: String,
    var ip: String
)