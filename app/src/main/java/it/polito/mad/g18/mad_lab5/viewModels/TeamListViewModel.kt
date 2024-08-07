package it.polito.mad.g18.mad_lab5.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

enum class OrderField(val displayed: String) {
    NAME("Name"),
    DATE("Date")
}

enum class Order(val displayed: String) {
    ASC("Ascendant"),
    DESC("Descendant")
}

@HiltViewModel
class TeamListViewModel @Inject constructor(
    private val model: MainModel,
    private val teamRepository: TeamRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>,
    private val taskRepository: TaskRepository
) : ViewModel() {


    private val _filters = MutableStateFlow(TeamFilters())
    val filters: StateFlow<TeamFilters> get() = _filters

    @OptIn(ExperimentalCoroutinesApi::class)
    val teams: StateFlow<List<TeamData>> = _filters
        .flatMapLatest { params ->
            withContext(Dispatchers.IO) {
                teamRepository.getFilteredTeams(params)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = teamRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchTerm(searchTerm: String) {
        _filters.value = _filters.value.copy(searchTerm = searchTerm)
    }

    fun setOrderField(orderField: OrderField) {
        _filters.value = _filters.value.copy(orderField = orderField)
    }

    fun setOrder(order: Order) {
        _filters.value = _filters.value.copy(order = order)
    }

    fun addCategory(category: String) {
        _filters.value = _filters.value.copy(categories = _filters.value.categories + category)
    }

    fun removeCategory(category: String) {
        _filters.value = _filters.value.copy(categories = _filters.value.categories - category)
    }

    fun setStartDate(startDate: LocalDate?) {
        _filters.value = _filters.value.copy(startDate = startDate)
    }

    fun setEndDate(endDate: LocalDate?) {
        _filters.value = _filters.value.copy(endDate = endDate)
    }

    fun resetFilters() {
        _filters.value = TeamFilters()
    }
}

data class TeamFilters(
    val searchTerm: String = "",
    val orderField: OrderField = OrderField.NAME,
    val order: Order = Order.ASC,
    val categories: List<String> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
/*        val getUserMe = userRepository.getUserMe()
        suspend fun createTeam(): Result<String>{

            return teamRepository.createTeam(TeamData())
        }*/

