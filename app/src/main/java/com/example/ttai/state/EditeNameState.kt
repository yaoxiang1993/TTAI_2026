package com.example.ttai.state

import com.example.ttai.base.MviState

data class EditeNameState(
    val isLoading: Boolean = false,
    val currentName: String = "",
    val nameLength: Int = 0,
    val isNameValid: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : MviState