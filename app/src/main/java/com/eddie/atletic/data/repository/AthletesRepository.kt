package com.eddie.atletic.data.repository

import com.eddie.atletic.data.model.Athlete
import com.eddie.atletic.data.model.Belt
import com.eddie.atletic.data.model.RenewalCalculator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AthletesRepository(
    private val database: FirebaseDatabase = getDatabaseInstance()
) {
    private val athletesRef: DatabaseReference = database.reference.child("athletes")

    companion object {
        private var persistenceInitialized = false

        private fun getDatabaseInstance(): FirebaseDatabase {
            val db = FirebaseDatabase.getInstance()
            if (!persistenceInitialized) {
                try {
                    db.setPersistenceEnabled(true)
                    persistenceInitialized = true
                } catch (e: Exception) {
                    // Persistence already enabled or not supported in this run
                }
            }
            return db
        }
    }

    /**
     * Observa a lista de atletas em tempo real do Firebase Realtime Database
     */
    fun getAthletesFlow(): Flow<Result<List<Athlete>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val athletes = mutableListOf<Athlete>()
                for (child in snapshot.children) {
                    try {
                        val athleteId = child.key ?: ""
                        val map = child.value as? Map<*, *>
                        if (map != null) {
                            athletes.add(Athlete.fromMap(athleteId, map))
                        } else {
                            val athlete = child.getValue(Athlete::class.java)
                            if (athlete != null) {
                                athlete.id = athleteId
                                athletes.add(athlete)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // Ordena por data de atualização mais recente ou nome
                val sortedAthletes = athletes.sortedByDescending { it.updatedAt }
                trySend(Result.success(sortedAthletes))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }

        athletesRef.addValueEventListener(listener)
        awaitClose { athletesRef.removeEventListener(listener) }
    }

    /**
     * Busca um atleta específico em tempo real
     */
    fun getAthleteByIdFlow(athleteId: String): Flow<Athlete?> = callbackFlow {
        val targetRef = athletesRef.child(athleteId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val map = snapshot.value as? Map<*, *>
                    if (map != null) {
                        trySend(Athlete.fromMap(athleteId, map))
                    } else {
                        val athlete = snapshot.getValue(Athlete::class.java)?.apply { id = athleteId }
                        trySend(athlete)
                    }
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        targetRef.addValueEventListener(listener)
        awaitClose { targetRef.removeEventListener(listener) }
    }

    /**
     * Salva ou atualiza um atleta no Firebase Realtime Database (Create & Update)
     */
    suspend fun saveAthlete(athlete: Athlete): Result<String> {
        return try {
            val now = System.currentTimeMillis()
            val athleteId = if (athlete.id.isBlank()) {
                athletesRef.push().key ?: UUID.randomUUID().toString()
            } else {
                athlete.id
            }

            val registrationNumber = if (athlete.registrationNumber.isBlank()) {
                generateRegistrationNumber()
            } else {
                athlete.registrationNumber
            }

            val athleteToSave = athlete.copy(
                id = athleteId,
                registrationNumber = registrationNumber,
                createdAt = if (athlete.createdAt == 0L) now else athlete.createdAt,
                updatedAt = now
            )

            athletesRef.child(athleteId).setValue(athleteToSave.toMap()).await()
            Result.success(athleteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Exclui um atleta do Firebase Realtime Database (Delete)
     */
    suspend fun deleteAthlete(athleteId: String): Result<Unit> {
        return try {
            athletesRef.child(athleteId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Renova a anuidade/matrícula do atleta por +1 ano
     */
    suspend fun renewAthleteMembership(athleteId: String): Result<Unit> {
        return try {
            val snapshot = athletesRef.child(athleteId).get().await()
            if (snapshot.exists()) {
                val currentMap = snapshot.value as? Map<*, *>
                val currentAthlete = if (currentMap != null) {
                    Athlete.fromMap(athleteId, currentMap)
                } else {
                    snapshot.getValue(Athlete::class.java)
                }

                if (currentAthlete != null) {
                    val newRenewalDate = RenewalCalculator.addOneYear(currentAthlete.renewalDate)
                    val updates = mapOf<String, Any>(
                        "renewalDate" to newRenewalDate,
                        "updatedAt" to System.currentTimeMillis()
                    )
                    athletesRef.child(athleteId).updateChildren(updates).await()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Atleta não encontrado para renovação"))
                }
            } else {
                Result.failure(Exception("Atleta não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gera um número único de matrícula federativa
     */
    fun generateRegistrationNumber(): String {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val randomDigits = (1000..9999).random()
        return "FED-$currentYear-$randomDigits"
    }

    /**
     * Popula dados iniciais de demonstração (Seed)
     */
    suspend fun seedSampleAthletes(): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val sampleAthletes = listOf(
                Athlete(
                    name = "Rodrigo 'Caçador' Silva",
                    registrationNumber = "FED-2026-1042",
                    cpf = "123.456.789-00",
                    birthDate = "1994-05-14",
                    academy = "Alliance Jiu-Jitsu",
                    modality = "Jiu-Jitsu",
                    beltName = Belt.BLACK.name,
                    degrees = 3,
                    weightCategory = "Médio",
                    weightKg = 82.3,
                    gender = "Masculino",
                    registrationDate = "2018-02-10",
                    renewalDate = "2027-02-10", // Em dia
                    phone = "(11) 98765-4321",
                    email = "rodrigo.silva@alliance.com",
                    notes = "Campeão Estadual 2024 e 2025. Professor responsável pelo CT Central.",
                    avatarColorIndex = 0,
                    createdAt = now,
                    updatedAt = now
                ),
                Athlete(
                    name = "Beatriz 'Tigresa' Menezes",
                    registrationNumber = "FED-2026-2189",
                    cpf = "234.567.890-11",
                    birthDate = "1998-11-20",
                    academy = "Gracie Barra",
                    modality = "Jiu-Jitsu",
                    beltName = Belt.BROWN.name,
                    degrees = 2,
                    weightCategory = "Leve",
                    weightKg = 64.0,
                    gender = "Feminino",
                    registrationDate = "2020-03-15",
                    renewalDate = RenewalCalculator.todayIso(), // Vence hoje
                    phone = "(21) 99887-1122",
                    email = "beatriz.menezes@gb.com",
                    notes = "Vice-campeã Brasileira 2025. Concorrendo para graduação de faixa preta.",
                    avatarColorIndex = 1,
                    createdAt = now,
                    updatedAt = now
                ),
                Athlete(
                    name = "Lucas 'Samurai' Takahashi",
                    registrationNumber = "FED-2026-3401",
                    cpf = "345.678.901-22",
                    birthDate = "2001-08-03",
                    academy = "Nova União",
                    modality = "Judô",
                    beltName = Belt.PURPLE.name,
                    degrees = 4,
                    weightCategory = "Meio-Pesado",
                    weightKg = 88.5,
                    gender = "Masculino",
                    registrationDate = "2022-09-01",
                    renewalDate = "2026-09-10", // A vencer em breve
                    phone = "(31) 97654-3210",
                    email = "lucas.samurai@novauniao.com",
                    notes = "Especialista em quedas e passagens de guarda.",
                    avatarColorIndex = 2,
                    createdAt = now,
                    updatedAt = now
                ),
                Athlete(
                    name = "Camila 'Fênix' Albuquerque",
                    registrationNumber = "FED-2026-4552",
                    cpf = "456.789.012-33",
                    birthDate = "2003-02-18",
                    academy = "Checkmat",
                    modality = "Jiu-Jitsu",
                    beltName = Belt.BLUE.name,
                    degrees = 2,
                    weightCategory = "Pena",
                    weightKg = 58.5,
                    gender = "Feminino",
                    registrationDate = "2023-01-20",
                    renewalDate = "2026-01-20", // Vencida
                    phone = "(41) 98456-7890",
                    email = "camila.fenix@checkmat.com",
                    notes = "Atleta promessa da categoria adulto pena.",
                    avatarColorIndex = 3,
                    createdAt = now,
                    updatedAt = now
                ),
                Athlete(
                    name = "Gabriel 'Gladiador' Ramos",
                    registrationNumber = "FED-2026-5620",
                    cpf = "567.890.123-44",
                    birthDate = "2006-07-29",
                    academy = "Atos Jiu-Jitsu",
                    modality = "Jiu-Jitsu",
                    beltName = Belt.WHITE.name,
                    degrees = 3,
                    weightCategory = "Pesado",
                    weightKg = 94.2,
                    gender = "Masculino",
                    registrationDate = "2025-05-10",
                    renewalDate = "2026-11-15", // Em dia
                    phone = "(19) 99123-4567",
                    email = "gabriel.ramos@atos.com",
                    notes = "Estreante no circuito regional 2026.",
                    avatarColorIndex = 4,
                    createdAt = now,
                    updatedAt = now
                ),
                Athlete(
                    name = "Mestre Osvaldo 'Leão' Carneiro",
                    registrationNumber = "FED-2026-0008",
                    cpf = "678.901.234-55",
                    birthDate = "1960-03-12",
                    academy = "Associação Tradicional de Lutas",
                    modality = "Jiu-Jitsu",
                    beltName = Belt.CORAL.name,
                    degrees = 7,
                    weightCategory = "Super Pesado",
                    weightKg = 100.5,
                    gender = "Masculino",
                    registrationDate = "1985-01-01",
                    renewalDate = "2027-01-01", // Em dia
                    phone = "(11) 99999-8888",
                    email = "mestre.osvaldo@federacao.org",
                    notes = "Membro fundado da Federação. Mais de 40 anos dedicados à arte marcial.",
                    avatarColorIndex = 5,
                    createdAt = now,
                    updatedAt = now
                )
            )

            for (sample in sampleAthletes) {
                saveAthlete(sample)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

