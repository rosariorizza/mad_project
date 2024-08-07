package it.polito.mad.g18.mad_lab5.gui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.polito.mad.g18.mad_lab5.viewModels.Order
import it.polito.mad.g18.mad_lab5.viewModels.OrderField
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.viewModels.TeamListViewModel
import it.polito.mad.g18.mad_lab5.ui.theme.TeamListActions
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListComponent(
    vm: TeamListViewModel = hiltViewModel(),
    actions: TeamListActions,
    bottomBar: @Composable () -> Unit,
) {


    val teams by vm.teams.collectAsState()
    val userMe by vm.userMe.collectAsState()


    var isSearchBarOpen by rememberSaveable {
        mutableStateOf(false)
    }
    var isBottomSheetOpened by rememberSaveable { mutableStateOf(false) }
    val closeBottomSheet = {
        isBottomSheetOpened = false
    }

    //val me by vm.getUserMe.collectAsState(initial = UserData())


/*    val temp = vm.getUserMe.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()*/
    var result by remember { mutableStateOf<Result<String>?>(null) }

    Scaffold(
        modifier = if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.padding(end = 41.dp) else Modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text( result?.getOrNull()?:"Your Teams", style = MaterialTheme.typography.headlineLarge)
                },
                actions = {
/*                    IconButton(onClick = { isSearchBarOpen = !isSearchBarOpen }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "search team"
                        )
                    }*/
                    IconButton(onClick = { isBottomSheetOpened = !isBottomSheetOpened }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "filter team"
                        )
                    }
                }
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    actions.newTeam()
                },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            LazyColumn {
                items(teams.toList()) {
                    TeamItem(
                        it,
                        actions.showTeam,
                        actions.showChat,
                        actions.showTasks,
                        userMe?.chats?.find { a-> a.teamId == it.id && a.taskId.isBlank() }?.unread?:false
                    )
                    HorizontalDivider()
                }
            }

        }


    }
    if (isBottomSheetOpened) {
        ModalBottomSheetFilter(closeBottomSheet, vm)
    }
}

@Composable
fun TeamItem(
    team: TeamData,
    showTeam: (String) -> Unit,
    showChat: (String) -> Unit,
    showTasks: (String) -> Unit,
    showBadge: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showTeam(team.id) }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            ProfilePicture(photo = team.profilePicture, isTeamChat = true, isTaskChat = false)
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 5.dp)
            ) {
                Text(text = team.name, style = MaterialTheme.typography.titleMedium)
                Text(text = team.category, style = MaterialTheme.typography.titleSmall)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterVertically)
        ) {
            Column(
                modifier = Modifier
                    .width(40.dp)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        showTasks(team.id)
                    }) {
                Icon(
                    Icons.AutoMirrored.Outlined.StickyNote2,
                    "tasks",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier
                .width(40.dp)
                .align(Alignment.CenterVertically)
                .clickable {
                    showChat(team.chatId)
                }) {
                BadgedBox(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    badge = {
                        if(showBadge) Badge()
                    }) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        "group chat",
                        )
                }
                Text(
                    text = "Chat",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetFilter(closeBottomSheet: () -> Unit, vm: TeamListViewModel) {
    val skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val stateScroll = rememberScrollState()
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
                    vm.resetFilters()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "task history"
                    )
                }
            }

            Text(
                text = "Sort By",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                MultipleChoiceOrderField(
                    filters.orderField,
                    vm::setOrderField
                )
            }
            Text(
                text = "Order",
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

            FilterLine(
                categories,
                filters.categories,
                vm::addCategory,
                vm::removeCategory,
                "Categories"
            )
            FilterDate(filters.startDate, vm::setStartDate, filters.endDate, vm::setEndDate)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceOrderField(
    checked: OrderField,
    setOrderField: (OrderField) -> Unit
) {

    val options = OrderField.entries

    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = {
                    setOrderField(label)
                },
                checked = checked == label
            ) {
                Text(label.displayed)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceOrder(checked: Order, setOrder: (Order) -> Unit) {

    val options = Order.entries

    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = {
                    setOrder(label)
                },
                checked = checked == label
            ) {
                Text(label.displayed)
            }
        }
    }
}

// completamente uguali al filtro dei task, da rimuovere

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterLine(
    list: List<String>,
    filterList: List<String>,
    addListFilter: (String) -> Unit,
    removeListFilter: (String) -> Unit,
    name: String
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
                    label = { Text(it) },
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
                            text = { Text(text = it) },
                            onClick = {
                                if (!filterList.contains(it)) addListFilter(it)
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
fun FilterDate(
    startDate: LocalDate?, startDateSetter: (LocalDate?) -> Unit,
    endDate: LocalDate?, endDateSetter: (LocalDate?) -> Unit
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

@Composable
private fun DateFilterChip(
    date: LocalDate?,
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
        if (date == null) {
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
                label = { Text("$prefix ${date.format(DateTimeFormatter.ofPattern("dd-MM-yyy"))}") },
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

@OptIn(ExperimentalMaterial3Api::class)
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

}
