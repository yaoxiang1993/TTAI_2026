package com.example.ttai.bean

data class MembershipItem(
    val id: String,
    val vipName: String,
    val value: String,
    val tip: String,
    val isSelected: Boolean = false
) 