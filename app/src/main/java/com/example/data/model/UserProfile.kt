package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val fullName: String = "",
    val age: String = "",
    val dob: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val isPinSet: Boolean = false,
    val pinCode: String = "",
    val isDarkMode: Boolean? = null,
    val isGridView: Boolean = false,
    val viewMode: String = "BARS", // "BARS", "BOXES", "HORIZONTAL_CARDS"
    val customBgColor: String? = null,
    val customSelectionColor: String? = null,
    val customFileColor: String? = null
)

