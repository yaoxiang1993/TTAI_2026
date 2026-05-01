package com.example.ttai.bean

data class SynthesizeItem(
    val id: String,
    val name: String,
    val content: String,
    val imageUrl: String,
    val tag: String,
    val isLiked: Boolean = false
)

//{
//    "_id": "角色ID",
//    "name": "角色名称",
//    "description": "角色描述",
//    "personality_tags": ["温柔", "古风"],
//    "avatar_url": "头像URL",
//    "popularity": 100
//}