package it.polito.mad.g18.mad_lab5.ui.theme

//import it.polito.mad.g18.mad_lab5.gui.UserListScreenComponents
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import it.polito.mad.g18.mad_lab5.gui.CameraScreen
import it.polito.mad.g18.mad_lab5.gui.ChatListScreen
import it.polito.mad.g18.mad_lab5.gui.ChatScreen
import it.polito.mad.g18.mad_lab5.gui.HomeScreen
import it.polito.mad.g18.mad_lab5.gui.Login
import it.polito.mad.g18.mad_lab5.gui.PublicUserProfileScreen
import it.polito.mad.g18.mad_lab5.gui.SettingsScreen
import it.polito.mad.g18.mad_lab5.gui.TaskDetailScreen
import it.polito.mad.g18.mad_lab5.gui.TaskEditScreen
import it.polito.mad.g18.mad_lab5.gui.TaskHistoryScreen
import it.polito.mad.g18.mad_lab5.gui.TaskListScreen
import it.polito.mad.g18.mad_lab5.gui.TeamAchievementsScreen
import it.polito.mad.g18.mad_lab5.gui.TeamDetailsScreen
import it.polito.mad.g18.mad_lab5.gui.TeamEditScreen
import it.polito.mad.g18.mad_lab5.gui.TeamListComponent
import it.polito.mad.g18.mad_lab5.gui.TeamPerformancesScreen
import it.polito.mad.g18.mad_lab5.gui.TeamRequestsScreen
import it.polito.mad.g18.mad_lab5.gui.TeamToJoinScreen
import it.polito.mad.g18.mad_lab5.gui.UserListDMScreen
//import it.polito.mad.g18.mad_lab5.gui.UserListScreen
import it.polito.mad.g18.mad_lab5.gui.UserProfileScreen
import it.polito.mad.g18.mad_lab5.gui.*

enum class NavigationLabels(
    val text: String,
    val iconSelected: ImageVector,
    val icon: ImageVector,
    val path: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    TASKS(
        "Tasks",
        Icons.AutoMirrored.Filled.StickyNote2,
        Icons.AutoMirrored.Outlined.StickyNote2,
        "tasks"
    ),
    TEAMS("Teams", Icons.Filled.Group, Icons.Outlined.Group, "teams"),
    CHATS("Chats", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "chats"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
}

@Composable
fun NavigationBarComponent(navCont: NavHostController, selected: NavigationLabels) {
    NavigationBar {
        NavigationLabels.entries.forEach {
            NavigationBarItem(
                selected = it == selected,
                onClick = {
                    navCont.navigate(it.path){
                        popUpTo(navCont.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                icon = {
                    if (selected == it) Icon(it.iconSelected, it.text)
                    else Icon(it.icon, it.text)
                },
                label = { Text(text = it.text) }
            )
        }
    }
}

@Composable
fun Navigation(deepLinkUri: Uri?) {

    val navController = rememberNavController()

    LaunchedEffect(deepLinkUri) {
        deepLinkUri?.lastPathSegment?.let { id ->
            navController.navigate("join/$id")
        }
    }

    val teamListsActions = remember(navController) {
        TeamListActions(navController)
    }
    val taskListsActions = remember(navController) {
        TaskListActions(navController)
    }
    val taskActions = remember(navController) {
        TaskActions(navController)
    }
    val teamActions = remember(navController) {
        TeamActions(navController)
    }
    val chatActions = remember(navController) {
        ChatActions(navController)
    }
    val settingsActions = remember(navController) {
        SettingsActions(navController)
    }

    val authActions = remember(navController) {
        AuthActions(navController)
    }


    var startingDestination = if (Firebase.auth.currentUser == null) {
        "login"
    } else {
        "home"
    }

    NavHost(
        navController = navController,
        startDestination = startingDestination,
    ) {

        //NICK

        // region HOME
        composable("home") {
            HomeScreen(taskAction = taskActions, navController = navController) {
                NavigationBarComponent(navCont = navController, NavigationLabels.HOME)
            }
        }
        //endregion

        // region TASKS
        composable("tasks") {
            TaskListScreen(teamId = null, actions = taskListsActions) {
                NavigationBarComponent(navCont = navController, NavigationLabels.TASKS)
            }
        }
        navigation(route = "tasks/{taskId}", startDestination = "details") {

            composable("details") { backStackEntry ->
                //val viewModel = backStackEntry.sharedViewModel<TaskViewModel>(navController = navController)
                val taskId = backStackEntry.arguments?.getString("taskId")

                TaskDetailScreen(taskId = taskId, actions = taskActions) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TASKS)
                }

            }
            composable("editTask/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                TaskEditScreen(taskId = taskId, teamId = null, actions = taskActions)
            }
            composable("history/{taskId}") { backStackEntry->
                val taskId = backStackEntry.arguments?.getString("taskId")
                TaskHistoryScreen(taskId = taskId, back = taskActions.back) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TASKS)
                }
            }

        }

        composable("teams/{teamId}/tasks") { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId")
            TaskListScreen(teamId = teamId, actions = taskListsActions) {
                NavigationBarComponent(navCont = navController, NavigationLabels.TASKS)
            }
        }
        composable("teams/{teamId}/addTask") { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId")
            TaskEditScreen(taskId = null, teamId = teamId, actions = taskActions)
        }
        //endregion

        //region TEAMS
        composable("teams") {
            TeamListComponent(actions = teamListsActions) {
                NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
            }
        }

        composable("teams/add") {
            TeamEditScreen(actions = teamActions, teamId = null)
        }


        navigation(route = "teams/{teamId}", startDestination = "detailsTeam") {

            composable("detailsTeam") { backStackEntry ->
                //val viewModel = backStackEntry.sharedViewModel<TaskViewModel>(navController = navController)
                val teamId = backStackEntry.arguments?.getString("teamId")
                TeamDetailsScreen(
                    teamId = teamId, teamActions = teamActions
                ) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
                }

            }
            composable("editTeam/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")

                TeamEditScreen(teamId = teamId, actions = teamActions)
            }

            composable("requestsTeam/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")
                TeamRequestsScreen(teamId = teamId, actions = teamActions) {

                    NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
                }
            }

            composable("achievementsTeam/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")

                TeamAchievementsScreen(teamId = teamId, actions = teamActions) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
                }
            }

            composable("tagsTeam/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")

                TeamTagsScreen(teamId = teamId, actions = teamActions) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
                }
            }

            composable("performancesTeam/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")

                TeamPerformancesScreen(teamId = teamId, actions = teamActions) {
                    NavigationBarComponent(navCont = navController, NavigationLabels.TEAMS)
                }
            }
            composable("addMembers/{teamId}") { backStackEntry ->
                val teamId = backStackEntry.arguments?.getString("teamId")
                UserListScreen(teamId = teamId ?: "", actions = teamActions)
            }
        }
        //endregion

        //region JOIN A TEAM

        composable(
            route = "join/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "begroup://join/id/{id}" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")

            TeamToJoinScreen(teamId = id, actions = teamActions)
        }

        //endregion

        //region CHATS
        composable("chats") {
            ChatListScreen(
                actions = chatActions,
                bottomBar = {
                    NavigationBarComponent(
                        navCont = navController,
                        selected = NavigationLabels.CHATS
                    )
                }
            )
        }
        composable("chats/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            ChatScreen(chatId ?: "", chatActions)
        }
        composable("chats/add") {
            UserListDMScreen(actions = chatActions)
        }
        //endregion

        //region SETTINGS
        composable("settings") { backStackEntry ->
            val nav: () -> Unit = { navController.navigate("settings/profile") }
            SettingsScreen(
                actions = settingsActions,

            ) {
                NavigationBarComponent(navCont = navController, NavigationLabels.SETTINGS)
            }
        }
        composable("settings/profile") {
            UserProfileScreen(actions = settingsActions)
        }
        //endregion

        //region CAMERA
        composable("teams/{teamId}/camera") { backStackEntry ->
            val nav: () -> Unit = { navController.popBackStack() }
            val teamId = backStackEntry.arguments?.getString("teamId")
            CameraScreen(id = teamId ?: "", isTeam = true, goBack = nav)
        }

        composable("settings/profile/camera") { backStackEntry ->
            val nav: () -> Unit = { navController.popBackStack() }
            CameraScreen(id = "", isTeam = false, goBack = nav)
        }
//endregion

        //region users
        composable("users/{userId}/{teamId}") { backStackEntry ->
            val nav: () -> Unit = { navController.popBackStack() }
            val userId = backStackEntry.arguments?.getString("userId")
            val teamId = backStackEntry.arguments?.getString("teamId")
            PublicUserProfileScreen(userId, teamId, nav)
        }
        composable("users/{userId}") { backStackEntry ->
            val nav: () -> Unit = { navController.popBackStack() }
            val userId = backStackEntry.arguments?.getString("userId")
            PublicUserProfileScreen(userId, null, nav)
        }

        //endregion

        //region auth
        composable("login") {
            Login(actions = authActions)
        }
        composable("register") {
            //todo
            Register(actions = authActions)
            //Text("register")
        }
        //endregion

    }


}


class TeamActions(navCont: NavHostController) {
    val login: () -> Unit = {
        navCont.navigate("login")
    }

    val showTeam: (String) -> Unit = { id ->
        navCont.navigate("teams/${id}")
    }

    val goHome: () -> Unit = {
        navCont.navigate("home")
    }

    val back: () -> Unit = {
        navCont.popBackStack()
    }

    val editTeam: (String) -> Unit = { id ->
        navCont.navigate("editTeam/${id}")
    }

    val deleteTeam: () -> Unit = {
        navCont.navigate("deleteTeam")
    }

    val showDetails: (String) -> Unit = { id ->
        back()
        navCont.navigate("teams/$id")
    }

    val showChat: (String) -> Unit = { id ->
        navCont.navigate("chats/$id")
    }

    val showUserChat: (String?) -> Unit = { id ->
        if (id != null) {
            navCont.navigate("chats/$id")
        }
    }
    val viewProfile: (String, String) -> Unit = { id, teamId ->
        navCont.navigate("users/$id/$teamId")
    }
    val showTasks: (String) -> Unit = { id ->
        navCont.navigate("teams/$id/tasks")
    }

    val showPerformances: (String) -> Unit = { id ->
        navCont.navigate("performancesTeam/$id")
    }
    val showAchievements: (String) -> Unit = { id ->
        navCont.navigate("achievementsTeam/$id")
    }
    val showTags: (String) -> Unit = {id ->
        navCont.navigate("tagsTeam/$id")
    }
    val showRequests: (String) -> Unit = { id ->
        navCont.navigate("requestsTeam/$id")
    }
    val addMembers: (String) -> Unit = { id ->
        navCont.navigate("addMembers/$id")
    }

    val takePhoto: (String) -> Unit = { id ->
        navCont.navigate("teams/$id/camera")
    }
}

class TeamListActions(navCont: NavHostController) {
    val showTeam: (String) -> Unit = { id ->
        navCont.navigate("teams/${id}")
    }
    val newTeam: () -> Unit = {
        navCont.navigate("teams/add")
        /*val id = 0
        navCont.navigate("teamToJoin?id=$id")*/
    }
    val showChat: (String) -> Unit = { id ->
        navCont.navigate("chats/$id")
    }
    val showTasks: (String) -> Unit = { id ->
        navCont.navigate("teams/$id/tasks")
    }

}

class ChatActions(navCont: NavHostController) {
    val back: () -> Unit = {
        navCont.popBackStack()
    }

    val showChat: (String) -> Unit = { id ->
        navCont.navigate("chats/$id")
    }

    val newChat: () -> Unit = {
        navCont.navigate("chats/add")
    }

    val showTeamDetails: (String) -> Unit = { teamId ->
        navCont.navigate("teams/$teamId")
    }

    val viewProfile: (String) -> Unit = { userId ->
        navCont.navigate("users/$userId")
    }
}

class TaskListActions(navCont: NavHostController) {
    val changeTeam: (String?) -> Unit = { id ->
        if (id == null) navCont.navigate("tasks")
        else navCont.navigate("teams/${id}/tasks")
    }

    val showTask: (String) -> Unit = { id ->
        navCont.navigate("tasks/${id}")
    }
    val newTask: (String) -> Unit = { id ->
        navCont.navigate("teams/$id/addTask")
    }
    val editTask: (String) -> Unit = { id ->
        navCont.navigate("chats/edit/$id")
    }
}

class TaskActions(navCont: NavHostController) {
    val back: () -> Unit = {
        navCont.popBackStack()
    }

    val showDetails: (String) -> Unit = { id ->
        back()
        navCont.navigate("tasks/$id")
    }

    val editTask: (String) -> Unit = { id ->
        navCont.navigate("editTask/$id")
    }

    val showAttachments: () -> Unit = {
        //navCont.navigate("attachments")
    }
    val showDiscussion: (String) -> Unit = { id ->
        navCont.navigate("chats/$id")
    }
    val showHistory: (String) -> Unit = { id ->
        navCont.navigate("history/$id")
    }
}

class SettingsActions(navCont: NavHostController) {
    val back: () -> Unit = {
        navCont.popBackStack()
    }
    val takePhoto: () -> Unit = {
        navCont.navigate("settings/profile/camera")
    }
    val showUser: () -> Unit = {
        navCont.navigate("settings/profile")
    }
    val logOut: () -> Unit = {
        Firebase.auth.signOut()
        //clearCredentialState()
        navCont.navigate("login")
    }
}

class AuthActions(navCont: NavHostController) {
    val back: () -> Unit = {
        navCont.popBackStack()
    }
    val goHome: () -> Unit = {
        //navCont.clearBackStack("home")
        navCont.navigate("home") {
            popUpTo(navCont.graph.startDestinationId) {
                inclusive = true
            }
        }
    }

    val register: () -> Unit = {
        navCont.navigate("register")
    }

    val takePhoto: () -> Unit = {
        navCont.navigate("camera")
    }
}

