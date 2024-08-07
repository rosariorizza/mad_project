package it.polito.mad.g18.mad_lab5.gui

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.viewModels.Repetition
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import it.polito.mad.g18.mad_lab5.viewModels.TaskViewModel
import it.polito.mad.g18.mad_lab5.ui.theme.TaskActions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//### TASK DETAILS ###


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    vm: TaskViewModel = hiltViewModel(),
    taskId: String? = null,
    actions: TaskActions,
    bottomBar: @Composable () -> Unit,
) {
    LaunchedEffect(taskId) {
        vm.setTaskDetail(taskId)
    }

    val task by vm.task.collectAsState()

    val userMe by vm.userMe.collectAsState()
    val team by vm.team.collectAsState()
    val teamMembers by vm.teamMembers.collectAsState()

    var isDeletingTask by remember {
        mutableStateOf(false)
    }
    val role by vm.role.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { actions.back() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close task page"
                        )
                    }
                },
                actions = {
                    var dropdownExpanded by remember { mutableStateOf(false) }

                    //chat
                    IconButton(onClick = { actions.showDiscussion(task.chatId) }) {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = "task discussion"
                        )
                    }
                    if (role != UserRole.VIEWER || task.assignees.map { it }.contains(userMe?.id?:"")) {

                        IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "task discussion"
                            )
                        }


                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = { actions.showHistory(taskId!!) },
                                text = { Text(text = "Task History") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = "task history"
                                    )
                                })
                            DropdownMenuItem(
                                onClick = { actions.editTask(taskId!!) },
                                text = { Text(text = "Edit Task") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "task history"
                                    )
                                })
                            DropdownMenuItem(
                                onClick = { isDeletingTask = true },
                                text = { Text(text = "Delete Task") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "task history"
                                    )
                                })
                        }
                    } else {
                        IconButton(onClick = { actions.showHistory(taskId!!) }) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "task history"
                            )
                        }
                    }
                },
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        paddingValues.toString()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues) //non so come togliere sta cosa
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isDeletingTask) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    },
                    onDismissRequest = { isDeletingTask = false },
                    title = { Text(text = "Do you want to delete this task?") },
                    text = { Text(text = "The operation cannot be undone.") },
                    dismissButton = {
                        Button(onClick = { isDeletingTask = false }) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { vm.deleteTask(taskId, { isDeletingTask=false; actions.back() }) }) {
                            Text("OK")
                        }
                    }
                )
            }

            ShowTaskDetailsPane(
                userMe = userMe?:UserData(),
                title = task.title,
                team = team,
                assignees = teamMembers.filter { task.assignees.contains(it.id) },
                dueDate = task.dueDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyy")) ?: "",
                repeat = task.repeat,
                status = task.status,
                category = task.category,
                details = task.description,
                tags = task.tags
            )
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    vm: TaskViewModel = hiltViewModel(),
    taskId: String? = null,
    teamId: String? = null,
    actions: TaskActions,
) {
    LaunchedEffect(teamId, taskId) {
        if (taskId!=null && teamId==null) {
            // edit task scenario
            vm.setEditTask(taskId)
        }else if(taskId==null && teamId!=null){
            // new task scenario
            vm.setNewTask(teamId)
        }
    }

    val userMe by vm.userMe.collectAsState()
    val team by vm.team.collectAsState()
    val teamMembers by vm.teamMembers.collectAsState()


    val title by vm.title
    val assignees by vm.assignees
    val dueDate by vm.dueDate
    val repeat by vm.repetition
    val numRepetitions by vm.numRepetitions
    val status by vm.status
    val category by vm.category
    val description by vm.description
    val tags by vm.tags


    val assigneesError by vm.assigneesError
    val titleError by vm.titleError
    val dueDateError by vm.dueDateError
    val categoryError by vm.categoryError

    val focusManager = LocalFocusManager.current


    BackHandler(
        onBack = {
            vm.back(actions.back)
        },
        enabled = true,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "${team.name}'s New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = { vm.back(actions.back) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close task page"
                        )
                    }
                },
                actions = {
                    Button(onClick = {
                        if (taskId == null) vm.saveNew (actions.showDetails)
                        else vm.saveEdit (actions.back)
                    }) {
                        Text("Save")
                    }
                }
            )
        }

    ) { paddingValues ->
        paddingValues.toString()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues) //non so come togliere sta cosa
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { focusManager.clearFocus() }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (vm.isClosingWithoutSaving) {
                AlertDialog(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning"
                        )
                    },
                    onDismissRequest = { vm.setIsClosingWithoutSaving(false) },
                    title = { Text(text = "Do you want to discard changes?") },
                    dismissButton = {
                        Button(onClick = { vm.setIsClosingWithoutSaving(false) }) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        Button(onClick = { vm.setIsClosingWithoutSaving(false); actions.back() }) {
                            Text("OK")
                        }
                    }
                )
            }
            ShowTaskEditPane(
                userMe = userMe?:UserData(),
                title = title,
                setTitle = vm::setTitle,
                titleError = titleError,
                assignees = teamMembers.filter { assignees.contains(it.id) },
                allAssignees = teamMembers,
                addAssignee = vm::addAssignee,
                removeAssignee = vm::removeAssignee,
                assigneesError = assigneesError,
                dueDate = dueDate,
                setDueDate = vm::setDueDate,
                dueDateError = dueDateError,
                repeat = repeat,
                setRepetition = vm::setRepetition,
                status = status,
                setStatus = vm::setStatus,
                category = category,
                setCategory = vm::setCategory,
                categoryError = categoryError,
                description = description,
                setDescription = vm::setDescription,
                tags = tags,
                allTags = team.tags,
                addTag = vm::addTag,
                removeTag = vm::removeTag,
                numRepetitions = numRepetitions,
                setNumRepetitions = vm::setNumRepetitions
            )
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHistoryScreen(
    taskId: String?,
    vm: TaskViewModel = hiltViewModel(),
    back: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    LaunchedEffect(taskId) {
        vm.setTaskDetail(taskId)
    }

    val history by vm.history.collectAsState()
    val team by vm.team.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task History") },
                navigationIcon = {
                    IconButton(onClick = {
                        back()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "close history task"
                        )
                    }
                }
            )

        },
        bottomBar = bottomBar

    ) { paddingValues ->
        paddingValues.toString()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues) //non so come togliere sta cosa
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShowTaskHistoryPane(history = history, users = team.members)
        }
    }


}


//## Panes

@Composable
fun ShowTaskDetailsPane(
    userMe: UserData,
    title: String,
    team: TeamData,
    assignees: List<UserTeamData>,
    tags: List<String>,
    dueDate: String,
    repeat: Repetition,
    status: TaskStatus,
    category: String,
    details: String,
) {

    //title
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
    Spacer(modifier = Modifier.height(16.dp))

    //team
    ShowTaskLine(label = "Team", value = team.name) {}

    //assignees
    ShowTaskLine(label = "Assignees") {
        val youAndAssegnees =
            assignees.filter { it.id != userMe.id }.map { "${it.name} ${it.surname}" }
                .toMutableList()

        if (youAndAssegnees.size < assignees.size) {
            youAndAssegnees.add(0, "You")
        }
        LabelIconList(list = youAndAssegnees) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "task discussion"
            )
        }
    }

    //due date
    ShowTaskLine(label = "Due date", value = dueDate) {}

    //repeat
    ShowTaskLine(label = "Repetition", value = repeat.toString()) {}

    //tags
    ShowTaskLine(label = "Tags") {
        LabelIconList(list = tags, clipShape = RoundedCornerShape(4.dp)) {}
    }

    //status
    ShowTaskLine(label = "Status", value = status.toString()) {}

    //category
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Category:\t\t\t",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Column(modifier = Modifier.weight(3f)) {
            LabelItemElement(e = category, clipShape = RoundedCornerShape(4.dp)) {}
        }
    }
    Spacer(modifier = Modifier.height(16.dp))

    //details
    ShowTaskLine(label = "Details", value = details) {}
}

@Composable
fun ShowTaskLine(
    label: String,
    value: String = "",
    content: @Composable (ColumnScope.() -> Unit)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start //.spacedBy(16.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(3f)
            )
        }
        Column(
            modifier = if (value.isEmpty()) {
                Modifier.weight(3f)
            } else {
                Modifier
            }
        ) {
            content()
        }

    }
    Spacer(modifier = Modifier.height(16.dp))
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowTaskEditPane(
    userMe: UserData,
    allAssignees: List<UserTeamData>,
    title: String,
    setTitle: (String) -> Unit,
    assignees: List<UserTeamData>,
    addAssignee: (String) -> Unit,
    removeAssignee: (String) -> Unit,
    tags: List<String>,
    addTag: (String) -> Unit,
    removeTag: (String) -> Unit,
    dueDate: LocalDate?,
    setDueDate: (LocalDate?) -> Unit,
    repeat: Repetition,
    setRepetition: (Repetition) -> Unit,
    status: TaskStatus,
    setStatus: (TaskStatus) -> Unit,
    category: String,
    setCategory: (String) -> Unit,
    description: String,
    setDescription: (String) -> Unit,
    allTags: List<String>,
    titleError: String,
    assigneesError: String,
    categoryError: String,
    dueDateError: String,
    numRepetitions: Int?,
    setNumRepetitions: (Int?)->Unit
) {


    //title
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = title,
            onValueChange = setTitle,
            placeholder = {
                Text(
                    text = "Add new title...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.Black,
                selectionColors = LocalTextSelectionColors.current,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = Color.Transparent,
                unfocusedTrailingIconColor = Color.Transparent,
                disabledTrailingIconColor = Color.Transparent
            ),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        if (titleError.isNotBlank())
            Text(
                text = titleError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            )
    }

    Spacer(modifier = Modifier.height(16.dp))

    /*    //team
        if(){
            ShowTaskLine(label = "Team", value = team) {}
        }
        else TeamInput(allTeams = allTeams, team = team, setTeam)
        if (teamNameError.isNotBlank())
            Text(
                text = teamNameError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            )*/

    AssigneeLabelIconList(
        userMe = userMe,
        assignees = allAssignees,
        selectedAssegnees = assignees,
        addTeamAssignee = addAssignee,
        removeTeamAssignee = removeAssignee
    )
    if (assigneesError.isNotBlank())
        Text(
            text = assigneesError,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )


    //due date
    //ShowTaskLine(label = "Due date", value = dueDate, setDueDate) {}
    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Date:"
            )
        }
        FlowRow {
            DateInput(dueDate, setDueDate)
        }
    }
    if (dueDateError.isNotBlank())
        Text(
            text = dueDateError,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )
    //repeat
    //ShowTaskLine(label = "Repetition", value = repeat.displayed, setRepetition) {}
    RepetitionInput(
        repetition = repeat,
        setRepetition = setRepetition,
    )
    if(repeat != Repetition.NONE) {
        NumRepetitionsTaskLine(
            value = numRepetitions?.toString() ?: "",
            onValueChange = setNumRepetitions
        )
    }
    Spacer(modifier = Modifier.height(16.dp))


    //tags
    /*
    ShowTaskLine(label = "Tags","", setTeam) {
        LabelIconList(list = tags, clipShape = RoundedCornerShape(4.dp)) {}
    }*/
    TagsLabelIconList(
        allTags = allTags,
        selectedTags = tags,
        addTag = addTag,
        removeTag = removeTag
    )

    //status
    //ShowTaskLine(label = "Status", value = status.displayed,setStatus) {}
    StatusInput(
        status = status,
        addStatusFilter = setStatus,
    )
    Spacer(modifier = Modifier.height(16.dp))

    //category
    EditTaskLine(
        label = "Category",
        value = category,
        "Add a category...",
        onValueChange = setCategory
    ) {}
    if (categoryError.isNotBlank())
        Text(
            text = categoryError,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .fillMaxWidth()
        )

    //details
    EditTaskLine(label = "Details", value = description, "Add some details...", setDescription) {}

}

@Composable
private fun DateInput(
    date: LocalDate?,
    dateSetter: (LocalDate?) -> Unit
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }
    val hideDatePicker = {
        showDatePicker = false
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (showDatePicker) DatePickerComponent(hideDatePicker, dateSetter)
        if (date == null) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                Modifier.padding(horizontal = 2.dp)
            ) {
                Row {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                    Text("Add date", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        } else {
            InputChip(
                modifier = Modifier.padding(2.dp),
                onClick = {
                    dateSetter(null)
                },
                label = { Text(date.format(DateTimeFormatter.ofPattern("dd-MM-yyy"))) },
                selected = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Localized description",
                        Modifier.size(InputChipDefaults.AvatarSize)
                    )
                },
            )
        }

    }
}

@Composable
fun ShowTaskHistoryPane(history: List<Message>, users: List<UserTeamData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(history) {
            val user = users.find { a -> it.userId == a.id } ?: UserTeamData()
            val userNameDisplayed = "${user.name} ${user.surname} (@${user.userName})"
            Column(
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                //Text(text = userNameDisplayed, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = it.msgContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = it.timeStamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


//## Other components

@Composable
fun EditTaskLine(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start //.spacedBy(16.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.size(width = 85.dp, height = 20.dp),
        )

        TextField(
            modifier = Modifier.align(Alignment.CenterVertically),
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.Black,
                selectionColors = LocalTextSelectionColors.current,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = Color.Transparent,
                unfocusedTrailingIconColor = Color.Transparent,
                disabledTrailingIconColor = Color.Transparent
            ),
        )
        /*Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(3f)
        )*/

        Column {
            content()
        }

    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun NumRepetitionsTaskLine(
    value: String,
    onValueChange: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start //.spacedBy(16.dp)
    ) {
        Text(
            text = "Times:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.size(width = 85.dp, height = 20.dp),
        )

        TextField(
            modifier = Modifier.align(Alignment.CenterVertically),
            value = value,
            onValueChange = { a-> onValueChange(a.toIntOrNull()) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            placeholder = {
                Text(
                    text = "Add task num of repetitions",
                    color = MaterialTheme.colorScheme.secondary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.Black,
                selectionColors = LocalTextSelectionColors.current,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = Color.Transparent,
                unfocusedTrailingIconColor = Color.Transparent,
                disabledTrailingIconColor = Color.Transparent
            ),
        )


    }
    Spacer(modifier = Modifier.height(16.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelIconList(
    list: List<String>,
    clipShape: Shape = CircleShape,
    content: @Composable (RowScope.() -> Unit)
) {

    FlowRow(
        modifier = Modifier
            .offset(y = (-4).dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        list.map {
            LabelItemElement(e = it, clipShape = clipShape) {
                content()
            }
        }
    }
}

@Composable
fun LabelItemElement(
    e: String,
    clipShape: Shape = CircleShape,
    content: @Composable (RowScope.() -> Unit)
) {
    Column {
        Row(
            modifier = Modifier
                .clip(clipShape)
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .padding(4.dp, 4.dp, 6.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
            Text(
                text = if (e.length < 4) {
                    "  $e  "
                } else {
                    " $e "
                }
            )
        }
    }
}

//### TODO: REVIEW THIS ONES
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssigneeLabelIconList(
    userMe: UserData,
    assignees: List<UserTeamData>, //all
    selectedAssegnees: List<UserTeamData>, //assignees from the task
    addTeamAssignee: (String) -> Unit,
    removeTeamAssignee: (String) -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Assignees:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //chips
            selectedAssegnees.forEach {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = {
                        removeTeamAssignee(it.id)
                    },
                    label = {
                        Text(
                            if (it.id == userMe.id) {
                                "You"
                            } else {
                                it.userName
                            }
                        )
                    },
                    selected = true,
                    avatar = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )
            }
            //adding components
            Box {
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    scrollState = scrollState,
                    modifier = Modifier.height(260.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("You")
                        },
                        onClick = {
                            val userMeTeam = selectedAssegnees.find { it.id == userMe.id }
                            if (userMeTeam == null) addTeamAssignee(userMe.id)
                            showDropdown = false
                            coroutineScope.launch {
                                scrollState.scrollTo(0)
                            }
                        })

                    assignees.map { assignee ->
                        if (assignee.id != userMe.id) {
                            DropdownMenuItem(
                                text = {
                                    Text(assignee.userName)
                                },
                                onClick = {
                                    if (!selectedAssegnees.contains(assignee)) addTeamAssignee(
                                        assignee.id
                                    )
                                    showDropdown = false
                                    coroutineScope.launch {
                                        scrollState.scrollTo(0)
                                    }
                                })
                        }
                    }

                }
                OutlinedButton(
                    onClick = { showDropdown = true },
                    Modifier.padding(horizontal = 2.dp)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                        Text("Add", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }


            }
        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsLabelIconList(
    allTags: List<String>, //all
    selectedTags: List<String>, //assignees from the task
    addTag: (String) -> Unit,
    removeTag: (String) -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tags:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //chips
            selectedTags.forEach {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = {
                        removeTag(it)
                    },
                    label = {
                        Text(it)
                    },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )
            }
            //adding components
            Box {
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    scrollState = scrollState,
                    modifier = Modifier.height(260.dp)
                ) {
                    allTags.forEach { assignee ->
                        DropdownMenuItem(
                            text = {
                                Text(assignee)
                            },
                            onClick = {
                                if (!selectedTags.contains(assignee)) addTag(
                                    assignee
                                )
                                showDropdown = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            })

                    }

                }
                OutlinedButton(
                    onClick = { showDropdown = true },
                    Modifier.padding(horizontal = 2.dp)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                        Text("Add", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }


            }
        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusInput(
    status: TaskStatus,
    addStatusFilter: (TaskStatus) -> Unit,
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Status:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {


            Box {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = { showDropdown = true },
                    label = { Text(status.displayed) },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    modifier = Modifier.height(260.dp),
                ) {
                    TaskStatus.entries.map {
                        DropdownMenuItem(
                            text = { Text(text = it.displayed) },
                            onClick = {
                                if (status != it) addStatusFilter(it)
                                showDropdown = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            })
                    }

                }
            }


        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamInput(
    allTeams: List<String>,
    team: String,
    setTeam: (String) -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Team:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {


            Box {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = { showDropdown = true },
                    label = { Text(team.ifBlank { "Select team..." }) },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    modifier = Modifier.height(260.dp),
                ) {
                    allTeams.map {
                        DropdownMenuItem(
                            text = { Text(text = it) },
                            onClick = {
                                if (team != it) setTeam(it)
                                showDropdown = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            })
                    }

                }
            }


        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepetitionInput(
    repetition: Repetition,
    setRepetition: (Repetition) -> Unit,
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 85.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Repetition:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {


            Box {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = { showDropdown = true },
                    label = { Text(repetition.displayed) },
                    selected = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Localized description",
                            Modifier.size(InputChipDefaults.AvatarSize)
                        )
                    },
                )

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    modifier = Modifier.height(260.dp),
                ) {
                    Repetition.entries.map {
                        DropdownMenuItem(
                            text = { Text(text = it.displayed) },
                            onClick = {
                                if (repetition != it) setRepetition(it)
                                showDropdown = false
                                coroutineScope.launch {
                                    scrollState.scrollTo(0)
                                }
                            })
                    }

                }
            }

        }
    }
}
