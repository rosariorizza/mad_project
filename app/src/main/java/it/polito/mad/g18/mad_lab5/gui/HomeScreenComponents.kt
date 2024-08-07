package it.polito.mad.g18.mad_lab5.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.firebase.firestore.core.ComponentProvider.Configuration
import it.polito.mad.g18.mad_lab5.ChatData
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.UserChatData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.ui.theme.TaskActions
import it.polito.mad.g18.mad_lab5.viewModels.ChatListViewModel
import it.polito.mad.g18.mad_lab5.viewModels.TaskListViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    tasksVM: TaskListViewModel = hiltViewModel(),
    chatVM: ChatListViewModel = hiltViewModel(),
    taskAction: TaskActions,
    navController: NavHostController,
    bottomBar: @Composable () -> Unit,

    ) {
    val userMe by tasksVM.userMe.collectAsState()

    LaunchedEffect(userMe) {
        if(userMe!= null) {
            tasksVM.addAssigneeFilter(userMe!!.id)
        }
    }

    val tasks by tasksVM.tasks.collectAsState()

    val chats = if (userMe != null) chatVM.getChats(userMe!!.id).collectAsState(initial = emptyList()).value else emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hi, ${userMe?.name}!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when(LocalConfiguration.current.orientation) {
                android.content.res.Configuration.ORIENTATION_LANDSCAPE -> {
                    HomeScreenPaneLandScape(
                        userMe = chatVM.user,
                        tasks = tasks,
                        chats = chats,
                        taskActions = taskAction,
                        navController = navController
                    )
                }
                else -> {
                    HomeScreenPane(
                        userMe = chatVM.user,
                        tasks = tasks,
                        chats = chats,
                        taskActions = taskAction,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenPane(
    userMe: UserData,
    tasks: Map<String, List<TaskData>>,
    chats: List<UserChatData>,
    taskActions: TaskActions,
    navController: NavHostController,
) {
    // Filter tasks based on their status (excluding "completed")
    val filteredTasks = tasks.values.flatten().filterNot { it.status.displayed == "Completed" }
    val filteredChats = chats.filter { x -> x.unread }

    // Take a maximum of 3 tasks from the filtered list
    val limitedTasks = filteredTasks.take(3)
    val limitedChats = filteredChats.take(4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        // tasks
        Column (
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (limitedTasks.isEmpty()) "Nothing to do" else "Tasks to complete",
                style = MaterialTheme.typography.headlineMedium,
                //color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),  // Create a 2x2 grid
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(limitedTasks) { task ->
                    TaskCard(task = task, taskActions = taskActions)
                }

                // Check if there are more than 3 tasks
                if (filteredTasks.size > 3) {
                    item {
                        // Show "See all" button if there are more tasks
                        Button(
                            onClick = { navController.navigate("tasks") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "See all")
                        }
                    }
                }
            }
        }

        // chats
        Column (
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (limitedChats.isEmpty()) "No new messages" else "Unread messages",
                style = MaterialTheme.typography.headlineMedium,
                //color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            LazyColumn {
                items(limitedChats) { cdata ->
                    ChatItem(
                        cdata,
                        {id -> navController.navigate("chats/$id") },
                        cdata.unread,
                        userMe
                    )
                    if(cdata.chatId != limitedChats.last().chatId) HorizontalDivider()
                }
                if (filteredChats.size > 4) {
                    item {
                        Button(
                            onClick = { navController.navigate("chats") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "See all")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenPaneLandScape(
    userMe: UserData,
    tasks: Map<String, List<TaskData>>,
    chats: List<UserChatData>,
    taskActions: TaskActions,
    navController: NavHostController,
) {
    // Filter tasks based on their status (excluding "completed")
    val filteredTasks = tasks.values.flatten().filterNot { it.status.displayed == "Completed" }
    val filteredChats = chats.filter { x -> x.unread }

    // Take a maximum of 3 tasks from the filtered list
    val limitedTasks = filteredTasks.take(3)
    val limitedChats = filteredChats.take(4)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        // tasks
        Column (
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (limitedTasks.isEmpty()) "Nothing to do" else "Tasks to complete",
                style = MaterialTheme.typography.headlineMedium,
                //color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),  // Create a 2x2 grid
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(limitedTasks) { task ->
                    TaskCard(task = task, taskActions = taskActions)
                }

                // Check if there are more than 3 tasks
                if (filteredTasks.size > 3) {
                    item {
                        // Show "See all" button if there are more tasks
                        Button(
                            onClick = { navController.navigate("tasks") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "See all")
                        }
                    }
                }
            }
        }

        // chats
        Column (
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (limitedChats.isEmpty()) "No new messages" else "Unread messages",
                style = MaterialTheme.typography.headlineMedium,
                //color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            LazyColumn {
                items(limitedChats) { cdata ->
                    ChatItem(
                        cdata,
                        {id -> navController.navigate("chats/$id") },
                        cdata.unread,
                        userMe
                    )
                    if(cdata.chatId != limitedChats.last().chatId) HorizontalDivider()
                }
                if (filteredChats.size > 4) {
                    item {
                        Button(
                            onClick = { navController.navigate("chats") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(text = "See all")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskData, taskActions: TaskActions) {
    if (task.status.displayed != "Completed") {
        ElevatedCard(
            onClick = { taskActions.showDetails(task.id) },
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()  // Maintain full width for consistent card size
                .padding(vertical = 5.dp)
                .height(90.dp)
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
                    modifier = Modifier
                        .fillMaxWidth()  // Maintain full width for consistent card size
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.status.displayed,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

