package it.polito.mad.g18.mad_lab5.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.ToastManager
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

enum class TaskStatus(val displayed: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    COMPLETED("Completed"),
    OVERDUE("Overdue")
}

enum class Repetition(val displayed: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    val model: MainModel,
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository,
    private val toastManager: ToastManager,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {


    // id del task, proveniente dalla navigazione, usato per il task details pane
    private val _taskDetailId = MutableStateFlow<String?>(null)
    fun setTaskDetail(id: String?) {
        _taskDetailId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val task: StateFlow<TaskData> = _taskDetailId
        .flatMapLatest { id ->
            if (id != null) {
                withContext(Dispatchers.IO) {
                    taskRepository.getTask(id)
                }
            } else flowOf(TaskData())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, TaskData())


    // id del team del task, proveniente dalla navigazione, usato per il task details pane
    // e il task edit pane

    private val _teamId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val team: StateFlow<TeamData> =
        // in questo modo il team viene re-fetchato ogni volta che uno dei due cambia
        combine(task, _teamId) { task, teamId ->
            Pair(task, teamId)
        }.flatMapLatest { (task, teamId) ->
            if (!teamId.isNullOrBlank()) {
                withContext(Dispatchers.IO) {
                    teamRepository.getTeam(teamId)
                }
            } else if (task.teamId.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    teamRepository.getTeam(task.teamId)
                }
            } else flowOf(TeamData())
        }.stateIn(viewModelScope, SharingStarted.Lazily, TeamData())

    @OptIn(ExperimentalCoroutinesApi::class)
    val teamMembers: StateFlow<List<UserTeamData>> =
        combine(task, _teamId) { task, teamId ->
            Pair(task, teamId)
        }.flatMapLatest { (task, teamId) ->
            if (!teamId.isNullOrBlank()) {
                withContext(Dispatchers.IO) {
                    teamRepository.getTeamMembers(teamId)
                }
            } else if (task.teamId.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    teamRepository.getTeamMembers(task.teamId)
                }
            } else {
                flowOf(listOf())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf())


    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<Message>> = _taskDetailId
        .flatMapLatest { taskId ->
            if (!taskId.isNullOrBlank()) {
                withContext(Dispatchers.IO) {
                    taskRepository.getHistory(taskId)
                }
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val role: StateFlow<UserRole> = task
        .flatMapLatest { task ->
            if (task.teamId.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    teamRepository.getUserMeRole(task.teamId)

                }
            } else flowOf(UserRole.VIEWER)
        }.stateIn(viewModelScope, SharingStarted.Lazily, UserRole.VIEWER)


    var isClosingWithoutSaving by mutableStateOf(false)
        private set

    fun setIsClosingWithoutSaving(v: Boolean) {
        isClosingWithoutSaving = v
    }

    //### SINGLE TASK STATES con relativi errori

    var title = mutableStateOf(value = "")
        private set

    fun setTitle(v: String) {
        title.value = v
    }

    var titleError = mutableStateOf(value = "")
        private set

    var teamNameError = mutableStateOf(value = "")
        private set


    var assignees = mutableStateOf(value = listOf<String>())
        private set

    private fun setAssignees(a: List<String>) {
        assignees.value = a
    }

    fun addAssignee(a: String) {
        assignees.value += a
    }

    fun removeAssignee(a: String) {
        assignees.value -= a

    }

    var assigneesError = mutableStateOf(value = "")
        private set

    var tags = mutableStateOf(value = listOf<String>())
        private set

    fun setTags(t: List<String>) {
        tags.value = t
    }

    fun addTag(t: String) {
        tags.value += t
    }

    fun removeTag(t: String) {
        tags.value -= t
    }

    var tagsError = mutableStateOf(value = "")
        private set

    var category = mutableStateOf(value = "")
        private set

    fun setCategory(v: String) {
        category.value = v
    }

    var categoryError = mutableStateOf(value = "")
        private set

    var description = mutableStateOf(value = "")
        private set

    fun setDescription(v: String) {
        description.value = v
    }


    var startDate = mutableStateOf(value = LocalDate.now())
        private set

    fun setStartDate(v: LocalDate) {
        startDate.value = v
    }

    var startDateError = mutableStateOf(value = "")
        private set


    var dueDate = mutableStateOf(value = LocalDate.now().plusWeeks(1))
        private set

    fun setDueDate(v: LocalDate?) {
        dueDate.value = v

    }

    var dueDateError = mutableStateOf(value = "")
        private set


    var repetition = mutableStateOf(value = Repetition.NONE)
        private set

    fun setRepetition(v: Repetition) {
        repetition.value = v
    }

    var allTeams = teamRepository.getFilteredTeams(TeamFilters())
        private set

    var status = mutableStateOf(value = TaskStatus.PENDING)
        private set

    fun setStatus(v: TaskStatus) {
        status.value = v
    }

    //attachments
    /*
        var attachments = task.attachments
            private set

       fun attachFile(a: Attachment) {
            task.attachFile(a)
            updateHistory(
                Message(
                    refId = a.refId,
                    author = userMe.value,
                    timeStamp = a.timeStamp,
                    msgContent = "Attached the file: ${a.fileName}"
                )
            )
        }*/

    //history

    fun deleteTask(id: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (id != null) {
                val result = taskRepository.deleteTask(id)

                result.onSuccess {
                    onSuccess()
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    //### CONTROLLI SUI TASK STATES
    private fun validate(): Boolean {

        checkTitle()
        checkAssignees()
        checkCategory()
        checkDueDate()

        if (titleError.value.isBlank() &&
            teamNameError.value.isBlank() &&
            assigneesError.value.isBlank() &&
            categoryError.value.isBlank() &&
            dueDateError.value.isBlank()
        ) {
            return true
        }
        return false
    }

    private fun clear() {
        titleError.value = ""
        teamNameError.value = ""
        assigneesError.value = ""
        categoryError.value = ""
        dueDateError.value = ""
    }

    private fun checkTitle() {
        titleError.value = if (title.value.isBlank()) "Title cannot be blank" else ""
    }

    private fun checkAssignees() {
        assigneesError.value = if (assignees.value.isEmpty()) "Assignees cannot be empty" else ""
    }

    private fun checkCategory() {
        categoryError.value = if (category.value.isBlank()) "Category cannot be blank" else ""
    }

    private fun checkDueDate() {
        dueDateError.value = if (dueDate.value == null) {
            "Due date cannot be blank"
            /*        } else if (dueDate.value!!.isBefore(LocalDate.now())) {
                        "Due date must be after now"*/
        } else {
            ""
        }
    }

    fun saveEdit(navigate: () -> Unit) {
        if (validate()) {
            clear()
            viewModelScope.launch {
                val result = taskRepository.updateTask(
                        getTaskData(id = oldTask.value.id, chatId = oldTask.value.chatId)
                    )
                result.onSuccess {
                    navigate()
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    fun saveNew(navigate: (String) -> Unit) {
        if (validate()) {
            viewModelScope.launch {
                val result = taskRepository.createTask(
                        getTaskData(teamId = _teamId.value),
                        if (repetition.value == Repetition.NONE) 1
                        else numRepetitions.value ?: 1
                    )
                result.onSuccess {
                    clear()
                    navigate(it)
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }

            }
        }
    }

    private fun hasTaskChanged(): Boolean {
        if (oldTask.value.id.isBlank()) {
            return getTaskData() != TaskData(
                creationDate = LocalDate.now(),
                dueDate = LocalDate.now().plusWeeks(1)
            )
        }
        return getTaskData() != oldTask.value
    }

    fun back(navigate: () -> Unit) {
        clear()
        if (hasTaskChanged()) {
            isClosingWithoutSaving = true
        } else navigate()

    }

    // converti gli state del vm in un'istanza di TaskData
    private fun getTaskData(
        id: String? = null,
        title: String? = null,
        assignees: List<String>? = null,
        dueDate: LocalDate? = null,
        repeat: Repetition? = null,
        tags: List<String>? = null,
        status: TaskStatus? = null,
        category: String? = null,
        description: String? = null,
        teamId: String? = null,
        chatId: String? = null
    ): TaskData {
        return TaskData(
            id = id ?: oldTask.value.id,
            title = title ?: this.title.value,
            assignees = assignees ?: this.assignees.value,
            dueDate = dueDate ?: this.dueDate.value,
            repeat = repeat ?: this.repetition.value,
            tags = tags ?: this.tags.value,
            status = status ?: this.status.value,
            category = category ?: this.category.value,
            description = description ?: this.description.value,
            teamId = teamId ?: oldTask.value.teamId,
            chatId = chatId ?: oldTask.value.teamId
        )
    }

    fun setNewTask(teamId: String) {
        _teamId.value = teamId
    }

    fun setEditTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.getTask(taskId).collect { task ->
                setTitle(task.title)
                setAssignees(task.assignees)
                setDueDate(task.dueDate)
                setRepetition(task.repeat)
                setStatus(task.status)
                setCategory(task.category)
                setDescription(task.description)
                setTags(task.tags)
                _teamId.value = task.teamId
                setOldTask(task)

            }
        }
    }

    // stato necessario per controllare eventuali cambiamenti del task modificato
    private var oldTask = mutableStateOf(value = TaskData())
        private set

    private fun setOldTask(v: TaskData) {
        oldTask.value = v

    }

    var numRepetitions = mutableStateOf<Int?>(null)
        private set

    fun setNumRepetitions(v: Int?) {
        if (repetition.value != Repetition.NONE) {
            numRepetitions.value = v
        }
    }

}