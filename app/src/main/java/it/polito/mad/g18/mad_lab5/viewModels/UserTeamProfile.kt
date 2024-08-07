package it.polito.mad.g18.mad_lab5.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class UserTeamProfile @Inject constructor(
    val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>
): ViewModel() {

    private val _userTeamId = MutableStateFlow<String?>(null)
    fun setUserTeamId(id: String?) {
        _userTeamId.value = id
    }

    private val _teamDetailId = MutableStateFlow<String?>(null)
    fun setTeamDetail(id: String?) {
        _teamDetailId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val user: StateFlow<UserData?> = _userTeamId
        .flatMapLatest { id ->
            if (id != null) {
                withContext(Dispatchers.IO) {
                    userRepository.getUser(id)
                }
            }
            else flowOf(UserData())
        }.stateIn(viewModelScope, SharingStarted.Lazily, UserData())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<Map<String, List<TaskData>>> = _teamDetailId
        .flatMapLatest { teamId ->
            if (teamId != null) {
                withContext(Dispatchers.IO) {
                    taskRepository.getFilteredTasks(TaskFilters(teams = listOf(teamId)))
                }
            } else {
                flowOf(emptyMap())
            }
        }
        .map { taskMap ->
            taskMap.mapValues { (_, tasks) ->
                tasks.filter { task -> _userTeamId.value?.let { task.assignees.contains(it) } == true }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

}
