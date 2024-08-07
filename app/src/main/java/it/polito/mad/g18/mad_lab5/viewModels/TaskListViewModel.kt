package it.polito.mad.g18.mad_lab5.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named


enum class GroupBy(val displayed: String) {
    DATE("Date"),
    STATUS("Status"),
    CATEGORY("Category")
}
data class TaskFilters(
    var searchTerm: String = "",
    var groupBy: GroupBy = GroupBy.DATE,
    var order: Order = Order.ASC,
    var teams: List<String> = emptyList(),
    var assignees: List<String> = emptyList(),
    var tags: List<String> = emptyList(),
    var statuses: List<TaskStatus> = emptyList(),
    var categories: List<String> = emptyList(),
    var startDate: LocalDate? = LocalDate.now(),
    var endDate: LocalDate? = null
)



@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {



    private val _filters = MutableStateFlow(TaskFilters())
    val filters: StateFlow<TaskFilters> get() = _filters

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<Map<String, List<TaskData>>> = _filters
        .flatMapLatest { filters ->
            withContext(Dispatchers.IO) {
                taskRepository.getFilteredTasks(filters)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, sortedMapOf())


    val teams = teamRepository.getFilteredTeams()

    private val _teamId = MutableStateFlow<String?>(null)
    val teamId : StateFlow<String?> = _teamId
    fun setTeamId(id: String?) {
        _teamId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories =  _teamId.flatMapLatest { id->
        withContext(Dispatchers.IO){
            taskRepository.getCategories(id)
        }

    }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val role: StateFlow<UserRole> = _teamId
        .flatMapLatest { id ->
            if (id != null) {
                withContext(Dispatchers.IO) {
                    teamRepository.getUserMeRole(id)

                }
            } else flowOf(UserRole.VIEWER)
        }.stateIn(viewModelScope, SharingStarted.Lazily, UserRole.VIEWER)



    fun getMembers(teamId: String?): Flow<List<UserTeamData>> {
        if(teamId == null) return flowOf(emptyList())
        return teamRepository.getTeamMembers(teamId)
    }


    fun setGroupBy(field: GroupBy) {
        _filters.value = _filters.value.copy(groupBy = field)
    }


    fun setSearchTerm(term: String) {
        _filters.value = _filters.value.copy(searchTerm = term.lowercase())
    }

    fun setOrder(order: Order) {
        _filters.value = _filters.value.copy(order = order)
    }

    fun addTeamFilter(team: String) {
        _filters.value = _filters.value.copy(teams = _filters.value.teams + team)
    }

    fun removeTeamFilter(team: String) {
        _filters.value = _filters.value.copy(teams = _filters.value.teams - team)
    }

    fun addAssigneeFilter(assignee: String) {
        _filters.value = _filters.value.copy(assignees = _filters.value.assignees + assignee)
    }

    fun removeAssigneeFilter(assignee: String) {
        _filters.value = _filters.value.copy(assignees = _filters.value.assignees - assignee)
    }

    fun addTagFilter(tag: String) {
        _filters.value = _filters.value.copy(tags = _filters.value.tags + tag)
    }

    fun removeTagFilter(tag: String) {
        _filters.value = _filters.value.copy(tags = _filters.value.tags - tag)
    }

    fun addStatusFilter(status: TaskStatus) {
        _filters.value = _filters.value.copy(statuses = _filters.value.statuses + status)
    }

    fun removeStatusFilter(status: TaskStatus) {
        _filters.value = _filters.value.copy(statuses = _filters.value.statuses - status)
    }

    fun addCategoryFilter(category: String) {
        _filters.value = _filters.value.copy(categories = _filters.value.categories + category)
    }

    fun removeCategoryFilter(category: String) {
        _filters.value = _filters.value.copy(categories = _filters.value.categories - category)
    }


    fun setStartDate(startDate: LocalDate?) {
        _filters.value = _filters.value.copy(startDate = startDate)
    }
    fun setEndDate(endDate: LocalDate?) {
        _filters.value = _filters.value.copy(endDate = endDate)
    }

    fun loadPreviousTasks() {
        _filters.value = _filters.value.copy(startDate = _filters.value.startDate?.minusWeeks(1))
    }

    fun resetFilters(teamId: String?){
        _filters.value = TaskFilters()
    }


}