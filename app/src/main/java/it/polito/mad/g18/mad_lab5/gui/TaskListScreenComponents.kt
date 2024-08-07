package it.polito.mad.g18.mad_lab5.gui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.viewModels.GroupBy
import it.polito.mad.g18.mad_lab5.viewModels.Order
import it.polito.mad.g18.mad_lab5.viewModels.TaskListViewModel
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import it.polito.mad.g18.mad_lab5.ui.theme.TaskListActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    vm: TaskListViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TaskListActions,
    bottomBar: @Composable () -> Unit
) {


    val userMe by vm.userMe.collectAsState()
    val members by remember(teamId) {
        vm.getMembers(teamId)
    }.collectAsState(initial = emptyList())
    val filters by vm.filters.collectAsState()

    LaunchedEffect(teamId, userMe) {
        if (teamId != null) {
            vm.addTeamFilter(teamId)
            vm.setTeamId(teamId)
        } else if (userMe != null) vm.addAssigneeFilter(userMe!!.id)
    }

    val tasks by vm.tasks.collectAsState()

    var isSearchBarOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isBottomSheetOpened by rememberSaveable { mutableStateOf(false) }
    val closeBottomSheet = {
        isBottomSheetOpened = false
    }

    var showDropdown by remember {
        mutableStateOf(false)
    }

    val teams by vm.teams.collectAsState(initial = emptyList())
    val role by vm.role.collectAsState()
    val teamId by vm.teamId.collectAsState()

    Scaffold(
        modifier = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.padding(
            end = 34.dp
        ) else Modifier,
        bottomBar = bottomBar,
        topBar = {
            TopAppBar(
                title = {
                    Row(Modifier.clickable { showDropdown = !showDropdown }) {
                        Text(
                            text = if (teamId == null) {
                                "Your Tasks"
                            } else {
                                "${teams.find { it.id == teamId }?.name}'s Tasks"
                            }, style = MaterialTheme.typography.headlineLarge
                        )
                        Icon(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "show team"
                        )

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }) {
                            if (teamId != null) {
                                DropdownMenuItem(
                                    text = { Text(text = "Your Tasks") },
                                    onClick = { actions.changeTeam(null) })
                            }
                            teams.forEach {
                                if (it.id != teamId) {
                                    DropdownMenuItem(
                                        text = { Text(text = it.name) },
                                        onClick = { actions.changeTeam(it.id) })
                                }
                            }
                        }
                    }
                },
                actions = {
/*                    IconButton(onClick = { isSearchBarOpen = !isSearchBarOpen }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search task"
                        )
                    }*/
                    IconButton(onClick = { isBottomSheetOpened = !isBottomSheetOpened }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "filter task"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (teamId != null && role != UserRole.VIEWER) {
                FloatingActionButton(
                    onClick = { vm.resetFilters(teamId); actions.newTask(teamId!!) },
                ) {
                    Icon(Icons.Filled.Add, "Floating action button.")
                }
            }
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (filters.groupBy == GroupBy.DATE && filters.order == Order.ASC && filters.startDate != null) {
                            Text(
                                "Load Previous Tasks",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { vm.loadPreviousTasks() }
                                    .align(Alignment.Center)
                            )
                        }
                    }

                }
                items(tasks.toList()) {
                    TaskGroup(it, actions.showTask, teams)
                }
            }

        }

        if (isBottomSheetOpened) {
            ModalBottomSheetSample(closeBottomSheet, vm, teamId, members, userMe ?: UserData())
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetSample(
    closeBottomSheet: () -> Unit,
    vm: TaskListViewModel,
    teamId: String?,
    members: List<UserTeamData>,
    userMe: UserData
) {
    val skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val stateScroll = rememberScrollState()

    val teams by vm.teams.collectAsState(initial = emptyList())
    val filters by vm.filters.collectAsState()
    val categories by vm.categories.collectAsState()

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        onDismissRequest = { closeBottomSheet() },
        sheetState = bottomSheetState,
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .verticalScroll(stateScroll),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                IconButton(onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            closeBottomSheet()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "task history"
                    )
                }
                IconButton(onClick = {
                    vm.resetFilters(teamId)
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "task history"
                    )
                }
            }

            Text(
                text = "Group By",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                MultipleChoiceGroupBy(
                    filters.groupBy,
                    vm::setGroupBy
                )
            }
            Text(
                text = "Sort",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                MultipleChoiceOrder(
                    filters.order,
                    vm::setOrder
                )
            }
            Text(
                text = "Filter",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (teamId == null) {
                FilterTeamLine(
                    teams,
                    filters.teams,
                    vm::addTeamFilter,
                    vm::removeTeamFilter,
                    "Teams"
                )
            } else {
                FilterAssigneeLine(
                    userMe,
                    members,
                    filters.assignees,
                    vm::addAssigneeFilter,
                    vm::removeAssigneeFilter
                )

                val tags = teams.find { it.id == teamId }?.tags
                if (tags != null) {
                    FilterLine(tags, filters.tags, vm::addTagFilter, vm::removeTagFilter, "Tags")
                }
            }



            FilterLine(
                categories,
                filters.categories,
                vm::addCategoryFilter,
                vm::removeCategoryFilter,
                "Categories"
            )
            FilterStatusLine(filters.statuses, vm::addStatusFilter, vm::removeStatusFilter)

            FilterDate(filters.startDate, vm::setStartDate, filters.endDate, vm::setEndDate)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/*
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDate(
    startDate: MutableState<LocalDate?>, startDateSetter: (LocalDate?) -> Unit,
    endDate: MutableState<LocalDate?>, endDateSetter: (LocalDate?) -> Unit
) {

    Row {
        Column(
            modifier = Modifier.size(width = 95.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Date:"
            )
        }
        FlowRow {
            DateFilterChip(startDate, startDateSetter, "From ", "Add Start Date")
            DateFilterChip(endDate, endDateSetter, "To ", "Add End Date")
        }
    }

}
*/

/*
@Composable
private fun DateFilterChip(
    date: MutableState<LocalDate?>,
    dateSetter: (LocalDate?) -> Unit,
    prefix: String,
    buttonText: String,
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
        if (date.value == null) {
            OutlinedButton(
                onClick = { showDatePicker = true },
                Modifier.padding(horizontal = 2.dp)
            ) {
                Row {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                    Text(buttonText, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        } else {
            InputChip(
                modifier = Modifier.padding(2.dp),
                onClick = {
                    dateSetter(null)
                },
                label = { Text("$prefix ${date.value!!.format(DateTimeFormatter.ofPattern("dd-MM-yyy"))}") },
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
*/

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerComponent(hideDatePicker: () -> Unit, dateSetter: (LocalDate?) -> Unit) {

    val datePickerState = rememberDatePickerState()
    val confirmEnabled = remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }
    DatePickerDialog(
        onDismissRequest = {
            hideDatePicker()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    hideDatePicker()
                    if (datePickerState.selectedDateMillis != null) {
                        val instant = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                        val localDate = instant.atZone(ZoneOffset.UTC).toLocalDate()
                        dateSetter(localDate)
                    }
                },
                enabled = confirmEnabled.value
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    hideDatePicker()
                }
            ) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }

}*/

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterTeamLine(
    list: List<TeamData>,
    filterList: List<String>,
    addListFilter: (String) -> Unit,
    removeListFilter: (String) -> Unit,
    name: String
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }
    //val teams by list.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 95.dp, height = 50.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$name:"
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
        ) {


            filterList.map {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = {
                        removeListFilter(it)
                    },
                    label = { Text(list.find { a -> it == a.id }?.name ?: "") },
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

            Box() {
                OutlinedButton(
                    onClick = { showDropdown = true },
                    Modifier.padding(horizontal = 2.dp)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                        Text("Add", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = {
                        showDropdown = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                        }
                    },
                    modifier = Modifier.height(260.dp)
                ) {
                    list.map {
                        DropdownMenuItem(
                            text = { Text(text = it.name) },
                            onClick = {
                                if (!filterList.contains(it.id)) addListFilter(it.id)
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
fun FilterAssigneeLine(
    userMe: UserData?,
    teamMembers: List<UserTeamData>,
    filterAssignee: List<String>,
    addTeamAssignee: (String) -> Unit,
    removeTeamAssignee: (String) -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }
    val me = userMe ?: UserData()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 95.dp, height = 50.dp),
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
            filterAssignee.map {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = {
                        removeTeamAssignee(it)
                    },
                    label = {
                        Text(
                            if (it == me.id) {
                                "You"
                            } else {
                                val assignee =
                                    teamMembers.find { tm -> tm.id == it } ?: UserTeamData()
                                "${assignee.name} ${assignee.surname}"
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
                            if (!filterAssignee.contains(me.id))
                                addTeamAssignee(me.id)
                            showDropdown = false
                            coroutineScope.launch {
                                scrollState.scrollTo(0)
                            }
                        })

                    teamMembers.map { teamMember ->
                        if (teamMember.id != me.id) {
                            DropdownMenuItem(
                                text = {
                                    Text("${teamMember.name} ${teamMember.surname}")
                                },
                                onClick = {
                                    if (!filterAssignee.contains(teamMember.id)) addTeamAssignee(
                                        teamMember.id
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
fun FilterStatusLine(
    filterStatuses: List<TaskStatus>,
    addStatusFilter: (TaskStatus) -> Unit,
    removeStatusFilter: (TaskStatus) -> Unit
) {

    var showDropdown by remember {
        mutableStateOf(false)
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Row {
        Column(
            modifier = Modifier.size(width = 95.dp, height = 50.dp),
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


            filterStatuses.map {
                InputChip(
                    modifier = Modifier.padding(2.dp),
                    onClick = {
                        removeStatusFilter(it)
                    },
                    label = { Text(it.displayed) },
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

            Box {
                OutlinedButton(
                    onClick = { showDropdown = true },
                    Modifier.padding(horizontal = 2.dp)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add")
                        Text("Add", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                }
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
                                if (!filterStatuses.contains(it)) addStatusFilter(it)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceGroupBy(checked: GroupBy, setGroupBy: (GroupBy) -> Unit) {

    val options = GroupBy.entries

    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = {
                    setGroupBy(label)
                },
                checked = checked == label
            ) {
                Text(label.displayed)
            }
        }
    }
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceOrder(vm: TaskListViewModel) {
    val checked by remember {
        vm.filterOrder
    }
    val options = Order.entries

    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = {
                    vm.setOrder(label)
                },
                checked = checked == label
            ) {
                Text(label.displayed)
            }
        }
    }
}*/


@Composable
fun TaskGroup(
    task: Pair<String, List<TaskData>>,
    showTask: (String) -> Unit,
    teams: List<TeamData>
) {
    var open by remember {
        mutableStateOf(true)
    }


    Row(modifier = Modifier
        .clickable { open = !open }
        .fillMaxWidth()
        .padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {

        Text(
            text = task.first,
            style = MaterialTheme.typography.headlineSmall
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            if (open) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "compress group"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "expand group"
                )
            }
        }
    }

    if (open) {
        Column {
            task.second.forEach { task ->
                TaskCard(task = task, showTask, teams)
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskData, showTask: (String) -> Unit, teams: List<TeamData>) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { showTask(task.id) }
    ) {
        Column {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.title,
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.category,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,

                    )

                Row {
                    task.tags.map {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .align(Alignment.CenterVertically),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        ) {
                            Text(
                                text = if (it.length < 4) {
                                    " " + it + " "
                                } else {
                                    it
                                },
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = teams.find { it.id == task.teamId }?.name ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = task.status.displayed,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

    }

}
