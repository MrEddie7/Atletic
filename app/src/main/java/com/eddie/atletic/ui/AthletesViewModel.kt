package com.eddie.atletic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eddie.atletic.data.model.Athlete
import com.eddie.atletic.data.model.Belt
import com.eddie.atletic.data.model.RenewalState
import com.eddie.atletic.data.repository.AthletesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AthletesUiState(
    val athletes: List<Athlete> = emptyList(),
    val filteredAthletes: List<Athlete> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedRenewalFilter: RenewalState? = null,
    val selectedBeltFilter: Belt? = null,
    val athleteToDelete: Athlete? = null,
    val isSaving: Boolean = false,
    val userMessage: String? = null
) {
    val totalAthletesCount: Int get() = athletes.size
    val activeRenewalsCount: Int get() = athletes.count { it.renewalInfo.state == RenewalState.ACTIVE }
    val expiringRenewalsCount: Int get() = athletes.count { it.renewalInfo.state == RenewalState.EXPIRING_SOON }
    val expiredRenewalsCount: Int get() = athletes.count { it.renewalInfo.state == RenewalState.EXPIRED }
}

class AthletesViewModel(
    private val repository: AthletesRepository = AthletesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AthletesUiState())
    val uiState: StateFlow<AthletesUiState> = _uiState.asStateFlow()

    init {
        observeAthletes()
    }

    private fun observeAthletes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAthletesFlow().collect { result ->
                result.fold(
                    onSuccess = { athletesList ->
                        _uiState.update { state ->
                            state.copy(
                                athletes = athletesList,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        applyFilters()
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                errorMessage = "Erro ao conectar com Firebase: ${error.localizedMessage ?: "Erro desconhecido"}"
                            )
                        }
                    }
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onRenewalFilterChange(filter: RenewalState?) {
        _uiState.update { it.copy(selectedRenewalFilter = filter) }
        applyFilters()
    }

    fun onBeltFilterChange(belt: Belt?) {
        _uiState.update { it.copy(selectedBeltFilter = belt) }
        applyFilters()
    }

    private fun applyFilters() {
        _uiState.update { state ->
            val query = state.searchQuery.trim().lowercase()
            val renewalFilter = state.selectedRenewalFilter
            val beltFilter = state.selectedBeltFilter

            val filtered = state.athletes.filter { athlete ->
                val matchesQuery = query.isEmpty() ||
                        athlete.name.lowercase().contains(query) ||
                        athlete.registrationNumber.lowercase().contains(query) ||
                        athlete.academy.lowercase().contains(query) ||
                        athlete.modality.lowercase().contains(query) ||
                        athlete.cpf.contains(query)

                val matchesRenewal = renewalFilter == null || athlete.renewalInfo.state == renewalFilter
                val matchesBelt = beltFilter == null || athlete.belt == beltFilter

                matchesQuery && matchesRenewal && matchesBelt
            }

            state.copy(filteredAthletes = filtered)
        }
    }

    fun saveAthlete(athlete: Athlete, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = repository.saveAthlete(athlete)
            _uiState.update { it.copy(isSaving = false) }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(userMessage = if (athlete.id.isBlank()) "Atleta cadastrado com sucesso!" else "Atleta atualizado com sucesso!")
                    }
                    onComplete(true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = "Erro ao salvar atleta: ${error.localizedMessage}")
                    }
                    onComplete(false)
                }
            )
        }
    }

    fun setAthleteToDelete(athlete: Athlete?) {
        _uiState.update { it.copy(athleteToDelete = athlete) }
    }

    fun confirmDeleteAthlete() {
        val athlete = _uiState.value.athleteToDelete ?: return
        viewModelScope.launch {
            val result = repository.deleteAthlete(athlete.id)
            _uiState.update { it.copy(athleteToDelete = null) }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(userMessage = "Atleta '${athlete.name}' removido com sucesso!") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(userMessage = "Erro ao remover atleta: ${error.localizedMessage}") }
                }
            )
        }
    }

    fun renewAthlete(athleteId: String) {
        viewModelScope.launch {
            val result = repository.renewAthleteMembership(athleteId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(userMessage = "Matrícula anual renovada por +1 ano com sucesso!") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(userMessage = "Erro ao renovar matrícula: ${error.localizedMessage}") }
                }
            )
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.seedSampleAthletes()
            _uiState.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(userMessage = "Dados de exemplo carregados no Firebase com sucesso!") }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(userMessage = "Erro ao carregar exemplos: ${error.localizedMessage}") }
                }
            )
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun getAthleteById(id: String): Athlete? {
        return _uiState.value.athletes.firstOrNull { it.id == id }
    }
}

