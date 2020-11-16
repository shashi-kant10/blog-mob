package com.shashi.blogmob.model

import com.google.firebase.Timestamp

data class BlogPostModel(
    var image_url: String = "",
    var desc: String = "",
    var user_id: String = "",
    var timestamp: Timestamp = Timestamp(0, 0)
)