package it.polito.mad.g18.mad_lab5

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.g18.mad_lab5.viewModels.GroupBy
import it.polito.mad.g18.mad_lab5.viewModels.Order
import it.polito.mad.g18.mad_lab5.viewModels.OrderField
import it.polito.mad.g18.mad_lab5.viewModels.Repetition
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.SortedMap
import javax.inject.Inject

enum class UserRole(val displayed: String, val description: String) {
    ADMIN(
        "Admin",
        "An admin has all the functionalities:" +
                "when a user creates a team, they will be admins"
    ),
    EDITOR(
        "Editor",
        "An editor has all the admin functionalities, " +
                "but they cannot add or remove the team members " +
                "(or manage the members approval requests), " +
                "edit the team details " +
                "or delete the team"
    ),
    VIEWER(
        "Viewer",
        "A viewer has all the editor's functionalities, " +
                "but they cannot edit or delete tasks they are not assigned to, " +
                "or create new ones"
    )
}

//## MAIN MODEL

class MainModel @Inject constructor(private val db: FirebaseFirestore) {

    //region USER SECTION


    //region USER SECTION

    private val _users = MutableStateFlow(
        mapOf(
            "0" to UserData(
                id = "0",
                name = "Mario",
                surname = "Rossi",
                userName = "mariorossi",
                email = "mariorossi@gmail.com",
                birthDate = LocalDate.parse("1970-04-05"),
                phoneNumber = "1234567890",
                location = "Fantamondo",
                kpi = 6.8,
                description = "smart fella",
                chats = listOf(
                    UserChatData()
                ).toMutableList(),
                teams = mutableListOf("0", "1")
            ),
            "1" to UserData(
                id = "1",
                name = "Luigi",
                surname = "Bianchi",
                userName = "luigibianchi",
                email = "luigibianchi@gmail.com",
                birthDate = LocalDate.parse("1980-06-15"),
                phoneNumber = "0987654321",
                chats = listOf(
                    UserChatData(),
                ).toMutableList(),
                teams = mutableListOf("0", "1", "2")
            ),
            "2" to UserData(
                id = "2",
                name = "Anna",
                surname = "Verdi",
                userName = "annaverdi",
                email = "annaverdi@gmail.com",
                birthDate = LocalDate.parse("1990-08-25"),
                phoneNumber = "1122334455",
                chats = listOf(
                    UserChatData()
                ).toMutableList(),
                teams = mutableListOf("1", "2")
            ),
            "3" to UserData(
                id = "3",
                name = "Paolo",
                surname = "Neri",
                userName = "paoloneri",
                email = "paoloneri@gmail.com",
                birthDate = LocalDate.parse("1985-11-30"),
                phoneNumber = "6677889900",
                chats = listOf(
                    UserChatData(),
                ).toMutableList(),
                teams = mutableListOf("1", "2")
            ),
            "4" to UserData(
                id = "4",
                name = "Giulia",
                surname = "Gialli",
                userName = "giuliagialli",
                email = "giuliagialli@gmail.com",
                birthDate = LocalDate.parse("1995-12-10"),
                phoneNumber = "9988776655",
                chats = listOf(
                    UserChatData()
                ).toMutableList(),
                teams = mutableListOf("0", "1", "2")
            )
        ).toSortedMap()
    )

    private val _filteredUsers = MutableStateFlow<List<UserData>>( emptyList())
    val filteredUsers: StateFlow<List<UserData>> = _filteredUsers

    fun setFilteredUsers(searchTerm: String) {
        _filteredUsers.value = _users.value.values.filter {
            searchTerm.isNotBlank() &&
            it.userName.contains(searchTerm.lowercase())
        }
    }

    private val _userMe = MutableStateFlow(_users.value["0"]!!)
    val userMe: StateFlow<UserData> = _userMe


    val users: StateFlow<SortedMap<String, UserData>> = _users

    fun editUser() {
        /*
        _users.value[userDisplayed.id.value] = userDisplayed.toUserData()
        if(userMe.value.id == userDisplayed.id.value)
            _userMe.value = userDisplayed.toUserData()
         */
    }

    val userDisplayed = UserModel()
    fun setUserDisplayed(id: String?) {
        val user = if (id != null) _users.value[id] ?: UserData()
        else _userMe.value
        userDisplayed.setFromUserData(user)

    }

    //endregion

    //region TASKS SECTION
    private val _tasks = MutableStateFlow(
        mutableMapOf(
            Pair(
                "0", TaskData(
                    "0",
                    "Implement RESTful Endpoints",
                    "0",
                    listOf("0", "1"),
                    LocalDate.parse("2024-05-12"),
                    Repetition.DAILY,
                    LocalDate.parse("2024-07-10"),
                    listOf("Backend", "LowPriority"),
                    TaskStatus.COMPLETED,
                    "API",
                    "Implement RESTful Endpoints",
                    chatId = "4",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-12T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-11T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            ),
            Pair(
                "1", TaskData(
                    "1",
                    "Redesign Website",
                    "0",
                    listOf("1", "4"),
                    LocalDate.parse("2024-05-25"),
                    Repetition.NONE,
                    LocalDate.parse("2024-07-16"),
                    listOf("UI", "MediumPriority"),
                    TaskStatus.PENDING,
                    "Frontend",
                    "Buttons",
                    chatId = "5",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-25T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-26T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            ),
            Pair(
                "2", TaskData(
                    "2",
                    "Optimize Database Queries",
                    "0",
                    listOf("0", "1", "4"),
                    LocalDate.parse("2024-05-15"),
                    Repetition.DAILY,
                    LocalDate.parse("2024-07-16"),
                    listOf("Backend", "HighPriority"),
                    TaskStatus.IN_PROGRESS,
                    "Database",
                    "Optimize Queries",
                    chatId = "6",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-15T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-16T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            ),
            Pair(
                "3", TaskData(
                    "3",
                    "Setup CI/CD Pipeline",
                    "1",
                    listOf("3", "4"),
                    LocalDate.parse("2024-05-10"),
                    Repetition.WEEKLY,
                    LocalDate.parse("2024-07-16"),
                    listOf("DevOps", "LowPriority"),
                    TaskStatus.COMPLETED,
                    "Infrastructure",
                    "Setup CI/CD Pipeline",
                    chatId = "7",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-10T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-11T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            ),
            Pair(
                "4", TaskData(
                    "4",
                    "Write Test Cases",
                    "1",
                    listOf("0", "1", "2"),
                    LocalDate.parse("2024-05-01"),
                    Repetition.MONTHLY,
                    LocalDate.parse("2024-07-05"),
                    listOf("QA", "MediumPriority"),
                    TaskStatus.PENDING,
                    "Testing",
                    "Write Test Cases",
                    chatId = "8",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-01T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-11T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            ),
            Pair(
                "5", TaskData(
                    "5",
                    "Design User Interface",
                    "1",
                    listOf("2", "3", "4"),
                    LocalDate.parse("2024-05-20"),
                    Repetition.NONE,
                    LocalDate.parse("2024-07-10"),
                    listOf("Frontend", "HighPriority"),
                    TaskStatus.IN_PROGRESS,
                    "UI/UX",
                    "Design User Interface",
                    chatId = "9",
                    history = listOf(
                        Message(
                            "0",
                            LocalDateTime.parse("2024-05-20T13:30:00"),
                            "Task Created"
                        ),
                        Message(
                            "4",
                            LocalDateTime.parse("2024-05-21T11:30:00"),
                            "Task Updated"
                        )
                    )
                )
            )
        ).toSortedMap()
    )
    val tasks: StateFlow<SortedMap<String, TaskData>> = _tasks

    fun addTask(): String {
        val newId = _tasks.value.lastKey() + 1

        _taskDisplayed.value.setChatId(
            "suca"
        )
        val newTask = _taskDisplayed.value.toData()
        newTask.id = newId

        val historyRecord = Message("userMe.value.id", LocalDateTime.now(), "Task created")
        newTask.history += historyRecord
        _taskDisplayed.value.setHistory(_taskDisplayed.value.history.value + historyRecord)

        _tasks.value[newId] = (newTask)
        _filteredTasks.value = applyFilter(_tasks.value.values)
        return newId
    }

    fun editTask() {
        val editedTask = _taskDisplayed.value.toData()
        //val originalTask = _tasks.value[_taskDisplayed.value.id.value]!!
        val historyRecord = Message(
            "userMe.value.id",
            LocalDateTime.now(),
            "Task Edited"
            //getDifferences(originalTask, editedTask)
        )
        editedTask.history += historyRecord
        _taskDisplayed.value.setHistory(_taskDisplayed.value.history.value + historyRecord)

        _tasks.value[_taskDisplayed.value.id.value] = editedTask
        _filteredTasks.value = applyFilter(_tasks.value.values)
    }

    fun deleteTask(id: String) {
        _tasks.value.remove(id)
        //_navigation.value = Navigation.LIST
        _filteredTasks.value = applyFilter(_tasks.value.values)
    }

    private fun <T : Any> getDifferences(first: T, second: T): String {
        return first::class.java.declaredFields
            .filter { it.get(first) != it.get(second) }
            .map {
                val name =
                    it.name.replaceFirstChar { n -> if (n.isLowerCase()) n.titlecase(Locale.getDefault()) else it.toString() }
                val before = it.get(first)
                val after = it.get(second)

                when (before) {
                    is List<*> -> {
                        "$name changed from ${before.joinToString { elem -> elem.toString() }}" +
                                " to ${(after as List<*>).joinToString { elem -> elem.toString() }}"
                    }

                    is TaskStatus -> "$name changed from ${before.displayed} to ${(after as TaskStatus).displayed}"
                    is Repetition -> "$name changed from ${before.displayed} to ${(after as Repetition).displayed}"
                    else -> "$name changed from $before to $after"
                }
            }.joinToString(separator = "\n") { it }
    }


    private val _filteredTasks = MutableStateFlow(
        applyFilter(
            _tasks.value.values
        )
    )
    val filteredTasks: StateFlow<Map<String, List<TaskData>>> = _filteredTasks

    fun setFilter(
        searchTerm: String = "",
        groupBy: GroupBy = GroupBy.DATE,
        order: Order = Order.ASC,
        teams: List<String> = emptyList(),
        assignees: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        statuses: List<TaskStatus> = emptyList(),
        categories: List<String> = emptyList(),
        startDate: LocalDate? = LocalDate.now(),
        endDate: LocalDate? = null
    ) {
        _filteredTasks.value = applyFilter(
            _tasks.value.values,
            searchTerm,
            groupBy,
            order,
            teams,
            assignees,
            tags,
            statuses,
            categories,
            startDate,
            endDate
        )
    }

    private fun applyFilter(
        list: MutableCollection<TaskData>,
        searchTerm: String = "",
        groupBy: GroupBy = GroupBy.DATE,
        order: Order = Order.ASC,
        teams: List<String> = emptyList(),
        assignees: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        statuses: List<TaskStatus> = emptyList(),
        categories: List<String> = emptyList(),
        startDate: LocalDate? = LocalDate.now(),
        endDate: LocalDate? = null
    ): Map<String, List<TaskData>> {

        var res = list
            /*            .filter {
                        searchTerm.isBlank() ||
                                (it.team.lowercase().contains(searchTerm) ||
                                        it.assignees.any { a ->
                                            a.lowercase().contains(searchTerm)
                                        } ||
                                        it.tags.any { a ->
                                            a.lowercase().contains(searchTerm)
                                        } ||
                                        it.status.displayed.lowercase().contains(searchTerm) ||
                                        it.category.lowercase().contains(searchTerm) ||
                                        it.description.lowercase().contains(searchTerm) ||
                                        it.discussion.any { a ->
                                            a.msgContent.toString().lowercase()
                                                .contains(searchTerm) //CAMBIATO QUI PERCHÉ IL FORMATO ORA È DIVERSO
                                        }
                                        )
                    }*/
            .filter {
                (teams.isEmpty() || teams.all { teamId -> teamId == it.teamId }) &&
                        (assignees.isEmpty() || it.assignees.any { a -> assignees.contains(a) }) &&
                        (tags.isEmpty() || it.tags.any { a -> tags.contains(a) }) &&
                        (statuses.isEmpty() || statuses.contains(it.status)) &&
                        (categories.isEmpty() || categories.contains((it.category))) &&
                        it.dueDate!! >= (startDate ?: LocalDate.MIN) && it.dueDate <= (endDate
                    ?: LocalDate.MAX)
            }.sortedBy {
                when (groupBy) {
                    GroupBy.DATE -> it.dueDate
                    GroupBy.STATUS -> it.status.ordinal
                    GroupBy.CATEGORY -> it.category
                }.toString()
            }


        if (order == Order.DESC) {
            res = res.reversed()
        }

        return res.groupBy {
            when (groupBy) {
                GroupBy.DATE -> it.dueDate.run {
                    val date = this!!
                    if (date.year == LocalDate.now().year && date.dayOfYear == LocalDate.now().dayOfYear) {
                        date.format(DateTimeFormatter.ofPattern("EEE dd MMM")) + " (Today)"
                    }
                    date.format(DateTimeFormatter.ofPattern("EEE dd MMM"))
                }

                GroupBy.STATUS -> it.status.displayed
                GroupBy.CATEGORY -> it.category
            }.toString()
        }
    }

    private val _tags = MutableStateFlow(
        listOf(
            "Backend", "LowPriority",
            "UI", "MediumPriority",
            "Backend", "HighPriority",
            "DevOps", "LowPriority",
            "QA", "MediumPriority",
            "Frontend", "HighPriority"
        )
    )
    val tags: StateFlow<List<String>> = _tags

    private val _taskCategories = MutableStateFlow(
        listOf(
            "API", "Frontend", "Database",
            "Infrastructure", "Testing",
            "UI/UX"
        )
    )
    val taskCategories: StateFlow<List<String>> = _taskCategories

    private val _taskDisplayed = MutableStateFlow(
        TaskModel()
    )
    val taskDisplayed: StateFlow<TaskModel> = _taskDisplayed
    fun setTaskDisplayed(id: String?, teamId: String? = null) {
        val task = if (id != null) {
            _tasks.value[id] ?: TaskData()
        } else {
            TaskData()
        }

        val team = if (task.teamId != "") {
            _teams.value[task.teamId] ?: TeamData()
        } else if (teamId != null) {
            _teams.value[teamId] ?: TeamData()
        } else TeamData()

        val assegnees = if (task.assignees.isNotEmpty()) {
            task.assignees.mapNotNull { team.members.find { a -> a.id == it } }
        } else emptyList()


        _taskDisplayed.value.setFromData(task, team, assegnees)
    }


    fun taskDisplayedChanged(): Boolean {
        if (_taskDisplayed.value.id.value == "") {
            return _taskDisplayed.value.toData() != TaskData()
        }
        return _taskDisplayed.value.toData() != _tasks.value[_taskDisplayed.value.id.value]
    }

    //endregion SECTION

    //region TEAM SECTION
    private val _teams = MutableStateFlow(
        mutableMapOf(
            Pair(
                "0", TeamData(
                    id = "0",
                    name = "Google",
                    profilePicture = "",
                    description = "Description of team 1",
                    creationDate = LocalDate.parse("2023-04-05"),
                    category = "Development",
                    chatId = "1",
                    members = listOf(
                        UserTeamData(
                            id = "0",
                            name = "Mario",
                            surname = "Rossi",
                            userName = "mariorossi",
                            role = UserRole.ADMIN,
                            joinDate = LocalDate.parse("2023-04-05"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                0
                            )
                        ),
                        UserTeamData(
                            id = "1",
                            name = "Luigi",
                            surname = "Bianchi",
                            userName = "luigibianchi",
                            role = UserRole.EDITOR,
                            joinDate = LocalDate.parse("2023-04-06"),
                            performance = PerformanceData(
                                2,
                                3,
                                0,
                                0
                            )
                        ),
                        UserTeamData(
                            id = "4",
                            name = "Giulia",
                            surname = "Gialli",
                            userName = "giuliagialli",
                            role = UserRole.ADMIN,
                            joinDate = LocalDate.parse("2023-04-09"),
                            performance = PerformanceData(
                                5,
                                3,
                                3,
                                0
                            )
                        )
                    ),
                    requests = listOf(
                        UserTeamRequestData(
                            id = "2",
                            name = "Anna",
                            surname = "Verdi",
                            userName = "annaverdi",
                            requestDate = LocalDateTime.parse("2023-04-07T10:14")
                        ),
                        UserTeamRequestData(
                            id = "3",
                            name = "Paolo",
                            surname = "Neri",
                            userName = "paoloneri",
                            requestDate = LocalDateTime.parse("2023-04-08T12:11")
                        ),
                    ),
                    achievements = listOf(0, 2, 4)
                )
            ),
            Pair(
                "1", TeamData(
                    id = "1",
                    name = "Microsoft",
                    profilePicture = "",
                    description = "Description of team 2",
                    creationDate = LocalDate.parse("2023-05-10"),
                    category = "Development",
                    chatId = "2",
                    members = listOf(
                        UserTeamData(
                            id = "3",
                            name = "Paolo",
                            surname = "Neri",
                            userName = "paoloneri",
                            role = UserRole.ADMIN,
                            joinDate = LocalDate.parse("2023-05-11"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "4",
                            name = "Giulia",
                            surname = "Gialli",
                            userName = "giuliagialli",
                            role = UserRole.EDITOR,
                            joinDate = LocalDate.parse("2023-05-12"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "0",
                            name = "Mario",
                            surname = "Rossi",
                            userName = "mariorossi",
                            role = UserRole.VIEWER,
                            joinDate = LocalDate.parse("2023-05-13"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "1",
                            name = "Luigi",
                            surname = "Bianchi",
                            userName = "luigibianchi",
                            role = UserRole.EDITOR,
                            joinDate = LocalDate.parse("2023-05-14"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "2",
                            name = "Anna",
                            surname = "Verdi",
                            userName = "annaverdi",
                            role = UserRole.VIEWER,
                            joinDate = LocalDate.parse("2023-05-15"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        )
                    ),
                    achievements = listOf(1, 2, 3, 5)
                )
            ),
            Pair(
                "2", TeamData(
                    id = "2",
                    name = "Fiat",
                    profilePicture = "",
                    description = "Description of team 3",
                    creationDate = LocalDate.parse("2023-06-20"),
                    category = "Automotive",
                    chatId = "3",
                    members = listOf(
                        UserTeamData(
                            id = "1",
                            name = "Luigi",
                            surname = "Bianchi",
                            userName = "luigibianchi",
                            role = UserRole.ADMIN,
                            joinDate = LocalDate.parse("2023-06-21"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "2",
                            name = "Anna",
                            surname = "Verdi",
                            userName = "annaverdi",
                            role = UserRole.EDITOR,
                            joinDate = LocalDate.parse("2023-06-22"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "3",
                            name = "Paolo",
                            surname = "Neri",
                            userName = "paoloneri",
                            role = UserRole.VIEWER,
                            joinDate = LocalDate.parse("2023-06-23"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        ),
                        UserTeamData(
                            id = "4",
                            name = "Giulia",
                            surname = "Gialli",
                            userName = "giuliagialli",
                            role = UserRole.ADMIN,
                            joinDate = LocalDate.parse("2023-06-24"),
                            performance = PerformanceData(
                                2,
                                3,
                                1,
                                1
                            )
                        )
                    ),
                    achievements = listOf(1, 2)
                )
            )
        ).toSortedMap()
    )
    val teams: StateFlow<SortedMap<String, TeamData>> = _teams

    private val _userTeams =
        MutableStateFlow(userMe.value.teams.associateWith { _teams.value[it] ?: TeamData() }
            .toSortedMap())
    val userTeams: StateFlow<SortedMap<String, TeamData>> = _userTeams


    fun addTeam(): String {
        val newId = _teams.value.lastKey() + 1
        _teamDisplayed.value.setMembers(
            listOf(
                UserTeamData(
                    id = _userMe.value.id,
                    name = _userMe.value.name,
                    surname = _userMe.value.surname,
                    userName = _userMe.value.userName,
                    role = UserRole.ADMIN,
                    joinDate = LocalDate.now(),
                    profilePicture = _userMe.value.profilePicture
                )
            )
        )
        _teamDisplayed.value.setChatId(
            "suca"
        )

        val newTeam = _teamDisplayed.value.toData()
        newTeam.id = newId

        _teams.value[newId] = newTeam

        _userMe.value.teams.add(newId)
        setUserTeams()

        _filteredTeams.value = applyFilter(_userTeams.value.values)
        return newId
    }

    fun editTeam() {
        val editedTeam = _teamDisplayed.value.toData()

        _teams.value[_teamDisplayed.value.id.value] = editedTeam
        _filteredTeams.value = applyFilter(_userTeams.value.values)
    }

    fun deleteTeam(id: String) {

        val team = _teams.value.remove(id)?:return

        team.members.forEach{
            _users.value[it.id]?.teams?.removeAll{a->
                a == team.id
            }
            _users.value[it.id]?.chats?.removeAll{a ->
                a.chatId == null
            }

        }
        _tasks.value = _tasks.value.filter {
            it.value.teamId != team.id
        }.toSortedMap()

        _userMe.value.teams.remove(team.id)
        setUserTeams()

        _filteredTeams.value = applyFilter(_userTeams.value.values)
    }


    private val _teamDisplayed = MutableStateFlow(TeamModel())
    val teamDisplayed: StateFlow<TeamModel> = _teamDisplayed

    fun setTeamDisplayed(id: String?) {
        _teamDisplayed.value.setFromData(
            if (id != null) {
                _teams.value[id] ?: TeamData()
            } else {
                TeamData()
            }
        )
    }

    fun teamDisplayedChanged(): Boolean {
        if (_teamDisplayed.value.id.value == "") {
            return _teamDisplayed.value.toData() != TeamData()
        }
        return _teamDisplayed.value.toData() != _teams.value[_teamDisplayed.value.id.value]
    }

    private val _filteredTeams = MutableStateFlow(
        applyFilter(
            _userTeams.value.values
        )
    )
    val filteredTeams: StateFlow<List<TeamData>> = _filteredTeams

    /*
    * applyFilter modifies the value of filtered task, and it's not accessible
    * from outside
    * setFilter is the wrapper that allows the vm to call applyFilter
    */
    private fun applyFilter(
        list: MutableCollection<TeamData>,
        searchTerm: String = "",
        orderField: OrderField = OrderField.NAME,
        order: Order = Order.ASC,
        category: List<String> = emptyList(),
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<TeamData> {
        var res = list.filter {
            searchTerm.isBlank() ||
                    (it.name.lowercase().contains(searchTerm) ||
                            it.members.any { a ->
                                a.name.lowercase().contains(searchTerm) ||
                                        a.surname.lowercase().contains(searchTerm) ||
                                        a.userName.lowercase().contains(searchTerm)
                            } ||
                            it.category.lowercase().contains(searchTerm) ||
                            it.description.lowercase().contains(searchTerm)
                            )
        }
            .filter {
                (category.isEmpty() || category.contains(it.category)) &&
                        it.creationDate >= (startDate
                    ?: LocalDate.MIN) && it.creationDate <= (endDate
                    ?: LocalDate.MAX)
            }.sortedBy {
                when (orderField) {
                    OrderField.NAME -> it.name
                    OrderField.DATE -> it.creationDate
                }.toString()
            }


        if (order == Order.DESC) {
            res = res.reversed()
        }
        return res
    }

    fun setUserTeams(){
        _userTeams.value = userMe.value.teams.associateWith { _teams.value[it] ?: TeamData() }
            .toSortedMap()
    }

    fun setFilter(
        searchTerm: String = "",
        orderField: OrderField = OrderField.NAME,
        order: Order = Order.ASC,
        category: List<String> = emptyList(),
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ) {
        _filteredTeams.value = applyFilter(
            _userTeams.value.values,
            searchTerm,
            orderField,
            order,
            category,
            startDate,
            endDate
        )
    }

    private val _categories = MutableStateFlow(
        listOf("Development", "Marketing", "Sales")
    )
    val categories: StateFlow<List<String>> = _categories

    fun addRequestMember(u: UserTeamRequestData, role: UserRole) {
        /*
        teamDisplayed.value.removeRequest(u)
        _teamDisplayed.value.addMember(
            UserTeamData(
                u.id,
                u.name,
                u.surname,
                u.userName,
                role, LocalDate.now(),
                _users.value[u.id]?.profilePicture,

                )
        )

        _teams.value[_teamDisplayed.value.id.value] = _teamDisplayed.value.toData()
        setTeamDisplayed(_teamDisplayed.value.id.value)

        // adapting userdata
        //val me = userMe.value
        _users.value[u.id]?.chats?.add(UserChatData(_teamDisplayed.value.chatId.value))
        _users.value[u.id]?.teams?.add(_teamDisplayed.value.id.value)

        //_users.value[_userMe.value.id] = me
        //_userMe.value = me
        */

    }

    fun addMember(u: UserData,) {
        /*
        _teamDisplayed.value.addMember(
            UserTeamData(
                u.id,
                u.name,
                u.surname,
                u.userName,
                UserRole.VIEWER,
                LocalDate.now(),
                _users.value[u.id]?.profilePicture,

                )
        )

        _teams.value[_teamDisplayed.value.id.value] = _teamDisplayed.value.toData()
        setTeamDisplayed(_teamDisplayed.value.id.value)

        // adapting userdata
        //val me = userMe.value
        _users.value[u.id]?.chats?.add(UserChatData(_teamDisplayed.value.chatId.value))
        _users.value[u.id]?.teams?.add(_teamDisplayed.value.id.value)

        //_users.value[_userMe.value.id] = me
        //_userMe.value = me
        */

    }

    fun removeMember(u: UserTeamData) {
        /*
        _teamDisplayed.value.removeMember(u)

        _teams.value[_teamDisplayed.value.id.value] = _teamDisplayed.value.toData()

        // adapting userdata
        //val me = userMe.value
        //val user = _users.value[u.id]
        _users.value[u.id]?.chats?.removeAll { it.chatId == _teamDisplayed.value.chatId.value }
        _users.value[u.id]?.teams?.removeAll { it == _teamDisplayed.value.id.value }

        //_users.value[_userMe.value.id] = me
        //_userMe.value = me
         */
    }

    fun leaveTeam(){
        val me = teamDisplayed.value.members.value.find {
            it.id == _userMe.value.id
        }
        if(me != null)
            removeMember(me)

        _userMe.value = _users.value[_userMe.value.id]?: UserData()
        setUserTeams()
        _filteredTeams.value = applyFilter(_userTeams.value.values)
    }

    fun rejectRequestMember(u: UserTeamRequestData) {
        _teamDisplayed.value.removeRequest(u)

        _teams.value[_teamDisplayed.value.id.value] = _teamDisplayed.value.toData()

    }


    //endregion

    //region CAMERA SECTION
    private val _tempPhoto = MutableStateFlow<Bitmap?>(null)
    val tempPhoto: StateFlow<Bitmap?> = _tempPhoto

    fun setTempPhoto(pfp: Bitmap?) {
        _tempPhoto.value = pfp
    }

    // CAMERA PERMISSIONS
    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA // to resolve this value you should import "android.Manifest" not "my.app.package.Manifest"
        )
    }

    private fun hasRequiredCameraPermissions(ctx: Context): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                ctx,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    /*
    TODO: CONTROLLO DA METTERE DOVE NECESSARIO PRIMA DI USARE LA SCHERMATA DELLA CAMERA
    if(!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                CAMERAX_PERMISSIONS, //array di permessi
                0
            )
        }
     */

    // CAMERA CONTROLS
    fun takePhoto(
        context: Context,
        controller: LifecycleCameraController,
        onPhotoTaken: (Bitmap) -> Unit
    ) {
        controller.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    val matrix = Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())

                        // se si vuole evitare l'immagine specchiata si può usare l'istruzione
                        //postScale(-1f, 1f)
                        // ma bisogna controllare che sia stata utilizzata la camera frontale
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true
                    )
                    onPhotoTaken(rotatedBitmap)
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Couldn't take photo")
                }
            }
        )
    }

    //endregion
}

//region TEAM DATA SECTION
data class UserTeamRequestData(
    var id: String = "",
    val name: String = "",
    val surname: String = "",
    val userName: String = "",
    val requestDate: LocalDateTime = LocalDateTime.MIN,
)

data class UserTeamData(
    var id: String = "",
    val name: String = "",
    val surname: String = "",
    val userName: String = "",
    val role: UserRole = UserRole.VIEWER,
    val joinDate: LocalDate = LocalDate.MIN,
    val profilePicture: String = "",
    val performance: PerformanceData = PerformanceData()
)

data class PerformanceData(
    val tasksDoneOnTime: Int = 0, //completati prima della scadenza
    val tasksToDo: Int = 0, //da fare
    val tasksOverdue: Int = 0, //competati in ritardo
    val tasksLate: Int = 0 //in ritardo non completati
)

class PerformanceModel(
    tasksDoneOnTime: Int = 0,
    tasksToDo: Int = 0,
    tasksOverdue: Int = 0,
    tasksLate: Int = 0
)
{
    fun setFromData(data: PerformanceData) {

        setTasksDoneOnTime(data.tasksDoneOnTime)
        setTasksToDo(data.tasksToDo)
        setTasksOverdue(data.tasksOverdue)
        setTasksLate(data.tasksLate)
    }

    fun toData(): PerformanceData {
        return PerformanceData(
            tasksDoneOnTime.value,
            tasksToDo.value,
            tasksOverdue.value,
            tasksLate.value
        )
    }

    //### PERFORMANCE DATA

    //tasks done in time

    private val _tasksDoneOnTime = MutableStateFlow(tasksDoneOnTime)
    val tasksDoneOnTime: StateFlow<Int> = _tasksDoneOnTime

    fun setTasksDoneOnTime(value: Int) {
        _tasksDoneOnTime.value = value
    }

    //tasks done overdue

    private val _tasksOverdue = MutableStateFlow(tasksOverdue)
    val tasksOverdue: StateFlow<Int> = _tasksOverdue

    fun setTasksOverdue(value: Int) {
        _tasksOverdue.value = value
    }

    //tasks to do

    private val _tasksToDo = MutableStateFlow(tasksToDo)
    val tasksToDo: StateFlow<Int> = _tasksToDo

    fun setTasksToDo(value: Int) {
        _tasksToDo.value = value
    }

    //tasks late

    private val _tasksLate = MutableStateFlow(tasksLate)
    val tasksLate: StateFlow<Int> = _tasksLate

    fun setTasksLate(value: Int) {
        _tasksLate.value = value
    }

}


data class TeamData(
    var id: String = "",
    val name: String = "",
    val profilePicture: String = "",
    val description: String = "",
    val creationDate: LocalDate = LocalDate.now(),
    val category: String = "",
    val members: List<UserTeamData> = emptyList(),
    val requests: List<UserTeamRequestData> = emptyList(),
    val chatId: String = "",
    val achievements: List<Int> = emptyList(),
    val tags: List<String> = emptyList()
)

class TeamModel(
    id: String = "",
    profilePicture: String ="",
    name: String = "",
    description: String = "",
    creationDate: LocalDate = LocalDate.now(),
    category: String = "",
    members: List<UserTeamData> = emptyList(),
    requests: List<UserTeamRequestData> = emptyList(),
    chatId: String = "",
    achievements: List<Int> = emptyList()
)
{
    fun setFromData(data: TeamData) {
        setId(data.id)
        setName(data.name)
        setDescription(data.description)
        setCreationDate(data.creationDate)
        setProfilePicture(data.profilePicture)
        setMembers(data.members)
        setChatId(data.chatId)
        setRequests(data.requests)
        setAchievements(data.achievements)
        setCategory(data.category)
    }

    fun toData(): TeamData {
        return TeamData(
            id.value,
            name.value,
            profilePicture.value,
            description.value,
            creationDate.value,
            category.value,
            members.value,
            requests.value,
            chatId.value,
            achievements.value
        )
    }

    //### TEAM DATA

    //team id
    private val _id = MutableStateFlow(id)
    val id: StateFlow<String> = _id
    fun setId(value: String) {
        _id.value = value
    }

    //team name
    private val _name = MutableStateFlow(name)
    val name: StateFlow<String> = _name
    fun setName(v: String) {
        _name.value = v
    }

    // team pfp
    private val _profilePicture = MutableStateFlow(profilePicture)
    val profilePicture: StateFlow<String> = _profilePicture
    fun setProfilePicture(pfp: String) {
        _profilePicture.value = pfp
    }

    //team description
    private val _description = MutableStateFlow(description)
    val description: StateFlow<String> = _description
    fun setDescription(v: String) {
        _description.value = v
    }

    //team creation date
    private val _creationDate = MutableStateFlow(creationDate)
    val creationDate: StateFlow<LocalDate> = _creationDate
    fun setCreationDate(v: LocalDate) {
        _creationDate.value = v
    }

    //team creation date
    private val _category = MutableStateFlow(category)
    val category: StateFlow<String> = _category
    fun setCategory(v: String) {
        _category.value = v
    }

    //team's members: array<Strings>

    private val _members = MutableStateFlow(members.toMutableList())
    val members: StateFlow<List<UserTeamData>> = _members
    fun addMember(t: UserTeamData) {
        setMembers(_members.value + t)
    }

    fun setMembers(v: List<UserTeamData>) {
        _members.value = v.toMutableList()
    }

    fun removeMember(t: UserTeamData) {
        setMembers(_members.value - t)
    }


    private val _requests = MutableStateFlow(requests.toMutableList())
    val requests: StateFlow<List<UserTeamRequestData>> = _requests
    fun removeRequest(t: UserTeamRequestData) {
        setRequests(_requests.value - t)
    }

    fun setRequests(v: List<UserTeamRequestData>) {
        _requests.value = v.toMutableList()
    }


    //team chatId
    private val _chatId = MutableStateFlow(chatId)
    val chatId: StateFlow<String> = _chatId
    fun setChatId(value: String) {
        _chatId.value = value
    }

    private val _achievements = MutableStateFlow(achievements.toMutableList())

    val achievements: StateFlow<List<Int>> = _achievements

    fun setAchievements(v: List<Int>) {
        _achievements.value = v.toMutableList()
    }

    fun addAchievement(t: Int) {
        _achievements.value.add(t)
    }

    fun removeAchievement(t: Int) {
        _achievements.value.remove(t)
    }

}
//endregion

//region USER DATA SECTION
data class UserData(
    var id: String = "",
    val name: String = "",
    val surname: String = "",
    val userName: String = "",
    val email: String = "",
    val birthDate: LocalDate? = LocalDate.now(),
    val phoneNumber: String = "",
    val profilePicture: String = "",
    val description: String = "",
    val kpi: Double = 0.0,
    val location: String = "",
    var chats: MutableList<UserChatData> = mutableListOf(),
    val teams: MutableList<String> = mutableListOf()
)

data class UserChatData(
    val chatId: String = "",
    val userId: String = "",
    val teamId: String = "",
    val taskId: String = "",
    val pfp: String = "",
    val title: String = "",
    val lastMessage: Message = Message(),
    val unread: Boolean = false
)

class UserModel(
    id: String = "",
    name: String = "",
    surname: String = "",
    userName: String = "",
    email: String = "",
    birthDay: LocalDate? = LocalDate.now(),
    phoneNumber: String = "",
    profilePicture: Bitmap? = null,
    role: UserRole = UserRole.VIEWER,
    joinDate: LocalDate = LocalDate.MIN,
    description: String = "",
    kpi: Double = 0.0,
    location: String = "",
    chats: List<UserChatData> = emptyList(),
    teams: List<String> = emptyList()
)
{
    fun setFromUserData(data: UserData) {
        setId(data.id)
        setName(data.name)
        setSurname(data.surname)
        setUserName(data.userName)
        setEmail(data.email)
        setBirthDay(data.birthDate)
        setPhoneNumber(data.phoneNumber)
        setProfilePicture(data.profilePicture)
        setLocation(data.location)
        setDescription(data.description)
        setKpi(data.kpi)
        setChats(data.chats)
        setTeams(data.teams)
    }

    fun setFromUserTeamData(data: UserTeamData) {
        setId(data.id)
        setName(data.name)
        setSurname(data.surname)
        setUserName(data.userName)
        setRole(data.role)
        setJoinDate(data.joinDate)
    }

    fun toUserData(): UserData {
        return UserData(
            id.value,
            name.value,
            surname.value,
            userName.value,
            email.value,
            birthDay.value,
            phoneNumber.value,
            profilePicture.value.toString(),
            description.value,
            kpi.value,
            location.value,
            chats.value,
            teams.value
        )
    }

    fun toUserTeamData(): UserTeamData {
        return UserTeamData(
            id.value,
            name.value,
            surname.value,
            userName.value,
            role.value,
            joinDate.value
        )
    }

    //### User DATA

    //user id
    private val _id = MutableStateFlow(id)
    val id: StateFlow<String> = _id
    fun setId(value: String) {
        _id.value = value
    }

    //user name
    private val _name = MutableStateFlow(name)
    val name: StateFlow<String> = _name
    fun setName(v: String) {
        _name.value = v
    }

    //user surname
    private val _surname = MutableStateFlow(surname)
    val surname: StateFlow<String> = _surname
    fun setSurname(v: String) {
        _surname.value = v
    }

    //user userName
    private val _userName = MutableStateFlow(userName)
    val userName: StateFlow<String> = _userName
    fun setUserName(v: String) {
        _userName.value = v
    }

    //user email
    private val _email = MutableStateFlow(email)
    val email: StateFlow<String> = _email
    fun setEmail(v: String) {
        _email.value = v
    }

    //user birthDate
    private val _birthDay = MutableStateFlow(birthDay)
    val birthDay: StateFlow<LocalDate?> = _birthDay
    fun setBirthDay(v: LocalDate?) {
        _birthDay.value = v
    }

    //user phoneNumber
    private val _phoneNumber = MutableStateFlow(phoneNumber)
    val phoneNumber: StateFlow<String> = _phoneNumber
    fun setPhoneNumber(v: String) {
        _phoneNumber.value = v
    }

    // profile picture
    private val _profilePicture = MutableStateFlow(profilePicture)
    val profilePicture: StateFlow<Bitmap?> = _profilePicture

    fun setProfilePicture(pfp: String?) {}

    private val _joinDate = MutableStateFlow(joinDate)
    val joinDate: StateFlow<LocalDate> = _joinDate
    fun setJoinDate(v: LocalDate) {
        _joinDate.value = v
    }

    private val _role = MutableStateFlow(role)
    val role: StateFlow<UserRole> = _role
    fun setRole(v: UserRole) {
        _role.value = v
    }

    private val _location = MutableStateFlow(location)
    val location: StateFlow<String> = _location
    fun setLocation(v: String) {
        _location.value = v
    }

    private val _description = MutableStateFlow(description)
    val description: StateFlow<String> = _description
    fun setDescription(v: String) {
        _description.value = v
    }

    private val _kpi = MutableStateFlow(kpi)
    val kpi: StateFlow<Double> = _kpi
    fun setKpi(v: Double) {
        _kpi.value = v
    }

    private val _chats = MutableStateFlow(chats.toMutableList())
    val chats: StateFlow<MutableList<UserChatData>> = _chats
    fun setChats(v: MutableList<UserChatData>) {
        _chats.value = v
    }

    private val _teams = MutableStateFlow(teams.toMutableList())
    val teams: StateFlow<MutableList<String>> = _teams
    fun setTeams(v: MutableList<String>) {
        _teams.value = v
    }

}
//endregion

//region CHAT DATA SECTION
data class Message(
    val userId: String = "",
    val timeStamp: LocalDateTime = LocalDateTime.MIN,
    val msgContent: String = "" //todo: definirlo come contenuto che può essere un file o una stringa
)

//todo: definire un enum di tipi di file consentiti come allegati, tipo png, pdf, pptx ecc..
//      ed associare ad ogni tipo di file un'icona diversa tipo un icona rossa per i .pdf, un bianca per i .txt
class Attachment(
    val refId: Int,
    val fileName: String,
    val timeStamp: LocalDateTime = LocalDateTime.now(),
    val dimension: Float,
    val fileType: String
)

data class ChatTeamData(
    val teamId: String,
    val taskId: String? = null
)

data class ChatData(
    var id: String = "",
    val userIds: Pair<String, String>? = null,
    val teamIds: ChatTeamData? = null,
    val messages: List<Message> = emptyList(),
    val pfp: String = "",
    val title: String = ""
)
//endregion

//region TASKS DATA SECTION
data class TaskData(
    var id: String = "",
    val title: String = "",
    val teamId: String = "",
    val assignees: List<String> = emptyList(),
    val creationDate: LocalDate = LocalDate.now(),
    val repeat: Repetition = Repetition.NONE,
    val dueDate: LocalDate? = null,
    val tags: List<String> = emptyList(),
    val status: TaskStatus = TaskStatus.PENDING,
    val category: String = "",
    val description: String = "",
    val chatId: String = "",
    var history: List<Message> = emptyList(),
)

class TaskModel(
    id: String = "",
    title: String = "",
    team: TeamData = TeamData(),
    assignees: List<UserTeamData> = emptyList(),
    creationDate: LocalDate = LocalDate.now(),
    repeat: Repetition = Repetition.NONE,
    dueDate: LocalDate? = null,
    tags: List<String> = emptyList(),
    status: TaskStatus = TaskStatus.PENDING,
    category: String = "",
    description: String = "",
    chatId: String = "",
    history: List<Message> = emptyList(),
)
{

    fun setFromData(data: TaskData, team: TeamData, assegnees: List<UserTeamData>) {
        setId(data.id)
        setTeam(team)
        setTitle(data.title)
        setTags(data.tags)
        setAssignees(assegnees)
        setDueDate(data.dueDate)
        setCategory(data.category)
        setRepetition(data.repeat)
        setStartDate(data.creationDate)
        setDescription(data.description)
        setStatus(data.status)
        setChatId(data.chatId)
        setHistory(data.history)
    }

    fun toData(): TaskData {
        return TaskData(
            id.value,
            title.value,
            team.value.id,
            assignees.value.map { it.id },
            creationDate.value,
            repeat.value,
            dueDate.value,
            tags.value,
            status.value,
            category.value,
            description.value,
            chatId.value,
            history.value
        )
    }

    //### TASK DATA ###
    private val _id = MutableStateFlow(id)
    val id: StateFlow<String> = _id

    fun setId(value: String) {
        _id.value = value
    }


    //title
    private val _title = MutableStateFlow(title)
    val title: StateFlow<String> = _title
    fun setTitle(v: String) {
        _title.value = v
    }


    //team
    private val _team = MutableStateFlow(team)
    val team: StateFlow<TeamData> = _team
    fun setTeam(v: TeamData) {
        _team.value = v
    }

    //assignees: array<usernames>
    private val _assignees = MutableStateFlow(assignees.toMutableList())
    val assignees: StateFlow<List<UserTeamData>> = _assignees
    private fun setAssignees(v: List<UserTeamData>) {
        _assignees.value = v.toMutableList()
    }

    fun addAssignee(a: UserTeamData) {
        setAssignees(_assignees.value + a)
    }

    fun removeAssignee(a: UserTeamData) {
        setAssignees(_assignees.value - a)
    }

    //creation date
    private val _creationDate = MutableStateFlow(creationDate)
    val creationDate: StateFlow<LocalDate> = _creationDate
    fun setStartDate(v: LocalDate) {
        _creationDate.value = v
    }

    //repeat
    private val _repeat = MutableStateFlow(repeat)
    val repeat: StateFlow<Repetition> = _repeat
    fun setRepetition(v: Repetition) {
        _repeat.value = v
    }

    //due date
    private val _dueDate = MutableStateFlow(dueDate)
    val dueDate: StateFlow<LocalDate?> = _dueDate
    fun setDueDate(v: LocalDate?) {
        _dueDate.value = v
    }

    //tags: array<Strings>
    private val _tags = MutableStateFlow(tags.toMutableList())
    val tags: StateFlow<List<String>> = _tags
    fun setTags(v: List<String>) {
        _tags.value = v.toMutableList()
    }

    fun addTag(t: String) {
        setTags(_tags.value + t)
    }

    fun removeTag(t: String) {
        setTags(_tags.value - t)
    }

    //status
    private val _status = MutableStateFlow(status)
    val status: StateFlow<TaskStatus> = _status
    fun setStatus(v: TaskStatus) {
        _status.value = v
    }

    //category
    private val _category = MutableStateFlow(category)
    val category: StateFlow<String> = _category
    fun setCategory(v: String) {
        _category.value = v
    }

    //description
    private val _description = MutableStateFlow(description)
    val description: StateFlow<String> = _description
    fun setDescription(v: String) {
        _description.value = v
    }


    //TODO IMPORTANTE: aggiungere il controllo sull'author(ancora da definire)
    //discussion[]
    private val _chatId = MutableStateFlow(chatId)
    val chatId: StateFlow<String> = _chatId
    fun setChatId(v: String) {
        _chatId.value = v
    }

    //history
    private val _history = MutableStateFlow(history.toMutableList())
    val history: StateFlow<List<Message>> = _history
    fun updateHistory(m: Message) {
        _history.value.add(m)
    }

    fun setHistory(v: List<Message>) {
        _history.value = v.toMutableList()
    }
}
//endregion