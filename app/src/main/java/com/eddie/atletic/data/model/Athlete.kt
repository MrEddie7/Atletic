package com.eddie.atletic.data.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Athlete(
    var id: String = "",
    var name: String = "",
    var registrationNumber: String = "",
    var cpf: String = "",
    var birthDate: String = "",
    var academy: String = "",
    var modality: String = "Jiu-Jitsu",
    var beltName: String = "WHITE",
    var degrees: Int = 0,
    var weightCategory: String = "Médio",
    var weightKg: Double = 82.3,
    var gender: String = "Masculino",
    var registrationDate: String = "",
    var renewalDate: String = "",
    var phone: String = "",
    var email: String = "",
    var notes: String = "",
    var avatarColorIndex: Int = 0,
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
) {
    @get:Exclude
    val belt: Belt
        get() = Belt.fromString(beltName)

    @get:Exclude
    val renewalInfo: RenewalInfo
        get() = RenewalCalculator.calculate(renewalDate)

    @get:Exclude
    val graduationTitle: String
        get() = belt.getFullGraduationTitle(degrees)

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "registrationNumber" to registrationNumber,
            "cpf" to cpf,
            "birthDate" to birthDate,
            "academy" to academy,
            "modality" to modality,
            "beltName" to beltName,
            "degrees" to degrees,
            "weightCategory" to weightCategory,
            "weightKg" to weightKg,
            "gender" to gender,
            "registrationDate" to registrationDate,
            "renewalDate" to renewalDate,
            "phone" to phone,
            "email" to email,
            "notes" to notes,
            "avatarColorIndex" to avatarColorIndex,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<*, *>): Athlete {
            return Athlete(
                id = id,
                name = map["name"] as? String ?: "",
                registrationNumber = map["registrationNumber"] as? String ?: "",
                cpf = map["cpf"] as? String ?: "",
                birthDate = map["birthDate"] as? String ?: "",
                academy = map["academy"] as? String ?: "",
                modality = map["modality"] as? String ?: "Jiu-Jitsu",
                beltName = map["beltName"] as? String ?: "WHITE",
                degrees = (map["degrees"] as? Number)?.toInt() ?: 0,
                weightCategory = map["weightCategory"] as? String ?: "Médio",
                weightKg = (map["weightKg"] as? Number)?.toDouble() ?: 0.0,
                gender = map["gender"] as? String ?: "Masculino",
                registrationDate = map["registrationDate"] as? String ?: "",
                renewalDate = map["renewalDate"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                email = map["email"] as? String ?: "",
                notes = map["notes"] as? String ?: "",
                avatarColorIndex = (map["avatarColorIndex"] as? Number)?.toInt() ?: 0,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

