package it.polito.mad.g18.mad_lab5.gui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PeopleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import it.polito.mad.g18.mad_lab5.R
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.UserTeamRequestData
import it.polito.mad.g18.mad_lab5.viewModels.TeamViewModel
import it.polito.mad.g18.mad_lab5.ui.theme.TeamActions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.ui.theme.md_theme_light_primary
import it.polito.mad.g18.mad_lab5.ui.theme.md_theme_light_secondary
import it.polito.mad.g18.mad_lab5.ui.theme.md_theme_light_tertiary
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus

//region SCREENS

@Composable
fun TeamDetailsScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    teamActions: TeamActions,
    bottomBar: @Composable () -> Unit,
) {

    LaunchedEffect(teamId) {
        vm.setTeamDetail(teamId)
    }

    val team by vm.team.collectAsState()
    val userMe by vm.userMe.collectAsState()
    val members by vm.members.collectAsState()
    val requests by vm.requests.collectAsState()
    val role by vm.role.collectAsState()

    Scaffold(
        bottomBar = bottomBar,
        topBar = {
            TeamDetailsTopAppBar(
                teamActions,
                userMe ?: UserData(),
                members,
                vm.isDeleteTeam,
                vm::setIsDeleteTeam,
                vm::deleteTeam,
                team.id,
                vm.isLeavingTeam,
                vm::setIsLeavingTeam,
                vm::leaveTeam,
                role = role, title = team.name
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {


            ShowTeamDetailsPane(
                id = team.id,
                name = team.name,
                description = team.description,
                category = team.category,
                members = members,
                requestToJoin = requests,
                creationDate = team.creationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    ?: "",
                profilePicture = team.profilePicture,
                actions = teamActions,
                chatId = team.chatId,
                removeMember = vm::removeMember,
                userMe = userMe ?: UserData(),
                showBadge = userMe?.chats?.find { it.teamId == teamId && it.taskId.isBlank() }?.unread
                    ?: false,
                memberToRemove = vm.memberToRemove,
                setMemberToRemove = vm::removeMemberAfterOk,
                getDirectChat = vm::getDirectChat,
                tags = team.tags,
                changeRole = vm::changeRole,
                role = role
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
) {
    LaunchedEffect(teamId) {
        //if (teamId == null) vm.setTeamDisplayed(null)
        if (teamId != null)
            vm.setEditTeam(teamId)

    }

    //val userMe by vm.userMe.collectAsState()
    //val team by vm.team.collectAsState()

    val pfp by vm.profilePicture
    val name by vm.name
    val description by vm.description
    val category by vm.category
    val nameError by vm.nameError
    val categoryError by vm.categoryError

    BackHandler(
        onBack = {
            vm.back(actions.back)
        },
        enabled = true,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (teamId == null) "New Team" else "Edit Team") },
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
                        if (teamId == null) vm.saveNew(actions.showDetails)
                        else vm.saveEdit(actions.back)
                    }) {
                        Text("Save")
                    }
                }
            )

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
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
            EditTeamPane(

                teamId = teamId ?: "",

                profilePicture = pfp,
                setProfilePicture = vm::setProfilePicture,

                name = name,
                nameError = nameError,
                setName = vm::setName,

                description = description,
                setDescription = vm::setDescription,

                category = category,
                categoryError = categoryError,
                setCategory = vm::setCategory,
                takePhoto = actions.takePhoto,
                deletePfp = vm::deleteProfilePicture
            )


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRequestsScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
    bottomBar: @Composable () -> Unit,
) {

    // team attributes
    LaunchedEffect(teamId) {
        vm.setTeamDetail(teamId)
    }

    val team by vm.team.collectAsState()
    val requests by vm.requests.collectAsState()





    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Requests") },
                navigationIcon = {
                    IconButton(onClick = { vm.back(actions.back) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close requests page"
                        )
                    }
                }
            )

        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (teamId != null) {
                JoinRequestPane(
                    teamId = teamId,
                    name = team.name,
                    requestToJoin = requests,
                    profilePicture = team.profilePicture, //team.profilePicture
                    acceptRequest = vm::acceptRequest,
                    rejectRequest = vm::rejectRequest,
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPerformancesScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
    bottomBar: @Composable () -> Unit,
) {

    // team attributes
    LaunchedEffect(teamId) {
        vm.setTeamDetail(teamId)
    }

    val team by vm.team.collectAsState()

    val tasks by vm.tasks.collectAsState()



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance") },
                navigationIcon = {
                    IconButton(onClick = { vm.back(actions.back) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close requests page"
                        )
                    }
                }
            )

        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TeamPerformancePane(
                name = team.name,
                profilePicture = team.profilePicture,
                tasks = tasks
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAchievementsScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
    bottomBar: @Composable () -> Unit,
) {

    // team attributes

    LaunchedEffect(teamId) {
        vm.setTeamDetail(teamId)
    }

    val team by vm.team.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = { vm.back(actions.back) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close requests page"
                        )
                    }
                }
            )

        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AchievementsPane(
                name = team.name,
                profilePicture = null, //team.profilePicture
                achievementsIds = team.achievements
            )
        }
    }
}

//chen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTagsScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
    bottomBar: @Composable () -> Unit,
) {

    // team attributes

    LaunchedEffect(teamId) {
        //vm.setTags(listOf("Prova"))
        vm.setTeamDetail(teamId)
        //vm.setTags(vm.team.value.tags)
    }

    val team by vm.team.collectAsState()
    val tags = team.tags
    //var newTag by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
                navigationIcon = {
                    IconButton(onClick = { actions.back() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close requests page"
                        )
                    }
                }
            )

        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TagsPane(
                name = team.name,
                profilePicture = null, //team.profilePicture
                tags = tags,
                addTag = vm::addTag,
                removeTag = vm::removeTag,
                teamId = teamId
                //achievementsIds = team.achievements
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamToJoinScreen(
    vm: TeamViewModel = hiltViewModel(),
    teamId: String? = null,
    actions: TeamActions,
) {
    val userMe by vm.userMe.collectAsState()
    LaunchedEffect(userMe, actions) {
        if (userMe == null || userMe!!.id.isBlank()) {
            actions.login()
        } else if (teamId == null) {
            actions.goHome()

        } else if (userMe!!.teams.contains(teamId)) {
            actions.showTeam(teamId)
        } else {
            vm.setTeamDetail(teamId)
        }
    }

    val team by vm.team.collectAsState()
    val members by vm.members.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join us") },
                navigationIcon = {
                    IconButton(onClick = { actions.back }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "close requests page"
                        )
                    }
                }
            )

        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TeamToJoinPane(
                teamId = teamId,
                userMe = userMe ?: UserData(),
                name = team.name,
                profilePicture = team.profilePicture,
                members = members,
                description = team.description,
                creationDate = team.creationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    ?: "",
                category = team.category,
                actions = actions,
                onAccept = vm::addTeamRequest
            )

        }
    }
}

//endregion

//region TeamDetailsTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsTopAppBar(
    teamActions: TeamActions,
    userMe: UserData,
    members: List<UserTeamData>,
    isDeleteTeam: Boolean,
    setIsDeleteTeam: (Boolean) -> Unit,
    myDeleteTeam: (String) -> Unit,
    teamId: String,
    isLeavingTeam: Boolean,
    setIsLeavingTeam: (Boolean) -> Unit,
    leaveTeam: (String, String) -> Unit,
    role: UserRole, title: String
) {
    var expanded by remember { mutableStateOf(false) }


    if (isDeleteTeam) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            onDismissRequest = { setIsDeleteTeam(false) },
            title = { Text(text = "Do you want to delete this group?") },
            dismissButton = {
                Button(onClick = { setIsDeleteTeam(false) }) {
                    Text("Cancel")
                }
            },
            confirmButton = { /*nick da aggiungere la removemember*/
                Button(onClick = { setIsDeleteTeam(false); myDeleteTeam(teamId); teamActions.back() }) {
                    Text("OK")
                }
            }
        )
    }

    if (isLeavingTeam) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            onDismissRequest = { setIsLeavingTeam(false) },
            title = { Text(text = "Do you want to leave this group?") },
            dismissButton = {
                Button(onClick = { setIsLeavingTeam(false) }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                Button(onClick = {
                    setIsLeavingTeam(false); leaveTeam(
                    teamId,
                    userMe.id
                ); teamActions.back()
                }) {
                    Text("OK")
                }
            }
        )
    }

    TopAppBar(title = { Text(text = title)},
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = { teamActions.back() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "go back"
                )
            }
        },
        actions = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }


                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.padding(end = 30.dp)
                ) {
                    if(role == UserRole.ADMIN) {
                        DropdownMenuItem(
                            text = { Text(text = "Edit Team") },
                            onClick = { teamActions.editTeam(teamId) })
                    }
                    DropdownMenuItem(
                        text = { Text(text = "Leave Team") },
                        onClick = { setIsLeavingTeam(true) })
                }


            }
        })
}

//endregion

//region DETAILS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTeamDetailsPane(
    id: String,
    name: String,
    description: String,
    category: String,
    members: List<UserTeamData>,
    requestToJoin: List<UserTeamRequestData>,
    creationDate: String,
    profilePicture: String,
    actions: TeamActions,
    chatId: String,
    removeMember: (String, String) -> Unit,
    userMe: UserData,
    showBadge: Boolean,
    memberToRemove: UserTeamData?,
    setMemberToRemove: (UserTeamData?) -> Unit,
    getDirectChat: (String) -> String?,
    tags: List<String>,
    changeRole: (String, String, UserRole) -> Unit,
    role: UserRole
) {
    val scrollState = rememberScrollState()
    var showAllMembers by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var filteredMembers by remember { mutableStateOf(members) }
    var userNotFound by remember { mutableStateOf(false) }
    var isBottomSheetOpened by rememberSaveable { mutableStateOf(false) }
    val closeBottomSheet = {
        isBottomSheetOpened = false
    }

    if (memberToRemove != null) {
        AlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning"
                )
            },
            onDismissRequest = { setMemberToRemove(null) },
            title = { Text(text = "Do you want to remove this member ${memberToRemove.userName}?") },
            dismissButton = {
                Button(onClick = { setMemberToRemove(null) }) {
                    Text("Cancel")
                }
            },
            confirmButton = { /*nick da aggiungere la removemember*/
                Button(
                    onClick = {
                        setMemberToRemove(null); removeMember(id, memberToRemove.id)
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



        //Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            item{
                // Profile picture at the top center
                val circleColor = MaterialTheme.colorScheme.primary

                if (profilePicture.isNotBlank()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = profilePicture,
                                contentDescription = "team profile picture",
                                modifier = Modifier.scale(2f)
                            )
                        }
                    }

                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

                        Icon(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .padding(21.dp),
                            imageVector = Icons.Filled.Group,
                            contentDescription = "team picture",
                        )
                    }
                }


                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = name,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                //Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily.Default,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category: ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )



                        Text(
                            text = category,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creation Date: ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                        Text(
                            text = creationDate,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )


                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

            }

            item {
                Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                        IconButton(onClick = { actions.showTasks(id) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.StickyNote2,
                                contentDescription = "go to tasks"
                            )

                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chat",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                        IconButton(
                            onClick = { actions.showChat(chatId) },
                            modifier = Modifier.size(45.dp)
                        ) {
                            BadgedBox(
                                badge = { if (showBadge) Badge() }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "go to chat"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    /*
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Notification",
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = FontFamily.Default,
                                                    color = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Toggle notifications with remembered state
                                            var notificationEnabled by remember { mutableStateOf(true) }
                                            Switch(
                                                modifier = Modifier.scale(0.7f),
                                                checked = notificationEnabled,
                                                onCheckedChange = { notificationEnabled = it },
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Media and links",
                                                style = TextStyle(
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    fontFamily = FontFamily.Default,
                                                    color = Color.DarkGray
                                                ),
                                                modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                                            )

                                            IconButton(onClick = { /*TODO*/ }) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                                    contentDescription = "go to media"
                                                )
                                            }
                                        }
                    */
                    Spacer(modifier = Modifier.height(6.dp))

                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Performance",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                        IconButton(onClick = { actions.showPerformances(id) }) {
                            Icon(
                                imageVector = Icons.Outlined.BarChart,
                                contentDescription = "go to performance"
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )

                        IconButton(onClick = { actions.showAchievements(id) }) {

                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.baseline_go_to_achivements),
                                contentDescription = "go to achievements"
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                        )
                        IconButton(onClick = { actions.showTags(id) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Label,
                                contentDescription = "go to tags"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                if(role == UserRole.ADMIN) {
                    Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        var showQRDialog by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add new members",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default,
                                modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                            )

                            IconButton(onClick = { actions.addMembers(id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.AddCircleOutline,
                                    contentDescription = "add members"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Invite new members through link",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default,
                                modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                            )

                            IconButton(onClick = { showQRDialog = true }) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode2,
                                    contentDescription = "qr code"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val num = requestToJoin.size
                            Text(
                                text = "$num users want to join ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default,
                                modifier = Modifier.weight(1f) // Per far sì che il testo occupi tutto lo spazio disponibile
                            )

                            IconButton(onClick = { actions.showRequests(id) }) {

                                Icon(Icons.Default.ExitToApp, contentDescription = "join requests")
                            }

                        }

                        if (showQRDialog) {
                            CustomDialog(
                                onDismissRequest = { showQRDialog = false },
                                link = "begroup://join/id/${id}"
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {}
            }

            item {
                Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Column {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val num = members.size
                            Text(
                                text = "$num members",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 10.dp)
                            )

                            /*IconButton(onClick = {
                                /*showSearch = !showSearch
                                if (!showSearch) {
                                    searchQuery = ""
                                    filteredMembers = members
                                    userNotFound = false
                                }*/
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "search member")
                            }*/
                        }

                        /*if (showSearch) {
                            Spacer(modifier = Modifier.height(10.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    filteredMembers = if (searchQuery.isEmpty()) {
                                        members
                                    } else {
                                        members.filter { member ->
                                            member.userName.contains(
                                                searchQuery,
                                                ignoreCase = true
                                            )
                                        }.also {
                                            userNotFound = it.isEmpty()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                placeholder = { Text("Search members...") }
                            )
                        }*/
                    }
                }
                //Spacer(modifier = Modifier.height(8.dp))
            }

            if (userNotFound) {
                item {
                    Text(
                        text = "No users found",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            color = Color.DarkGray
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {

                item {
                    UserLineMe(members, userMe.id, actions, id)
                }
                items(
                    items = members
                ) { member ->
                    if (member.id != userMe.id) UserLine(
                        member,
                        actions,
                        setMemberToRemove,
                        getDirectChat,
                        id,
                        role = role,
                        changeRole = changeRole
                    )
                }

                /*if (members.size > 3) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(text = if (showAllMembers) "See less" else "See all",
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable { showAllMembers = !showAllMembers })

                        }
                        Spacer(modifier = Modifier.height(25.dp))

                    }
                }*/
            }
        }
    }
}

@Composable
private fun UserLine(
    member: UserTeamData,
    actions: TeamActions,
    setMemberToRemove: (UserTeamData?) -> Unit,
    getDirectChat: (String) -> String?,
    teamId: String,
    role: UserRole,
    changeRole: (String, String, UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person, contentDescription = "Member Icon",
                modifier = Modifier.padding(end = 16.dp) // Padding tra icona e testo
            )

            Column(
                modifier = Modifier
            ) {
                Text(
                    member.userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                )

                //Spacer(modifier = Modifier.height(5.dp))
/*                Text(
                    member.role.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    fontFamily = FontFamily.Default,

                )*/
                RoleWithTooltip(member.role)
            }
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreVert, contentDescription = "More Options"
                )
            }
            DropdownMenu(expanded = expanded,
                onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text(text = "View Profile") },
                    onClick = { actions.viewProfile(member.id, teamId) })
                DropdownMenuItem(text = { Text(text = "Chat") },
                    onClick = { actions.showUserChat(getDirectChat(member.id)) })

                if(role == UserRole.ADMIN){
                    when(member.role){
                        UserRole.ADMIN-> {}

                        UserRole.EDITOR -> {
                            DropdownMenuItem(text = { Text(text = "Remove Member") },
                                onClick = {
                                    setMemberToRemove(member)
                                }
                            )
                            DropdownMenuItem(text = { Text(text = "Promote to Admin") },
                                onClick = {
                                    changeRole(teamId, member.id, UserRole.ADMIN)
                                }
                            )
                            DropdownMenuItem(text = { Text(text = "Demote to Viewer") },
                                onClick = {
                                    changeRole(teamId, member.id, UserRole.VIEWER)

                                }
                            )

                        }
                        UserRole.VIEWER -> {
                            DropdownMenuItem(text = { Text(text = "Remove Member") },
                                onClick = {
                                    setMemberToRemove(member)
                                }
                            )
                            DropdownMenuItem(text = { Text(text = "Promote to Editor") },
                                onClick = {
                                    changeRole(teamId, member.id, UserRole.EDITOR)
                                }
                            )
                        }
                    }
                }


            }
        }


    }
    HorizontalDivider()

    //Spacer(modifier = Modifier.height(10.dp))


}

@Composable
fun RoleWithTooltip(role: UserRole) {
    var showTooltip by remember { mutableStateOf(false) }

    Box {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = role.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 15.sp,
                fontWeight = FontWeight.Thin,
                fontFamily = FontFamily.Default,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "role info",
                modifier = Modifier.clickable { showTooltip = !showTooltip })
        }
        if (showTooltip) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, -20),
                properties = PopupProperties(
                    focusable = true,
                    dismissOnClickOutside = true,
                ),
                onDismissRequest = { showTooltip = false }

            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = role.description,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
@Composable
private fun UserLineMe(
    members: List<UserTeamData>,
    userMeId: String,
    actions: TeamActions,
    teamId: String,

    ) {
    val member by remember {
        mutableStateOf(members.find { it.id == userMeId } ?: UserTeamData())
    }
    var expanded by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person, contentDescription = "Member Icon",
                modifier = Modifier.padding(end = 16.dp) // Padding tra icona e testo
            )

            Column(
                modifier = Modifier
            ) {
                Text(
                    "${member.userName} (You)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,

                    )
                //Spacer(modifier = Modifier.height(5.dp))
/*                Text(
                    member.role.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Thin,
                        fontFamily = FontFamily.Default,
                )*/
                RoleWithTooltip(member.role)
            }

        }
        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreVert, contentDescription = "More Options"
                )
            }
            DropdownMenu(expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(text = { Text(text = "View Profile") },
                    onClick = { actions.viewProfile(member.id, teamId) })
            }
        }

    }
    HorizontalDivider()

    //Spacer(modifier = Modifier.height(10.dp))


}


@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CustomDialog(onDismissRequest: () -> Unit, link: String) {


    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            link
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current


    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .size(350.dp)
                .background(Color.White, shape = RectangleShape)
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "Scan me",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                val qr = generateQrCode(link)?.asImageBitmap()

                if (qr != null) {
                    Image(bitmap = qr, contentDescription = "qr code")
                } else {
                    Log.e("QrCode", "QR code generation failed")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Text(
                        text = "Share with link",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black

                    )
                }

                Row {
                    IconButton(onClick = {
                        GlobalScope.launch(Dispatchers.Main) { context.startActivity(shareIntent) }
                    }) {
                        Icon(
                            Icons.Default.Share, contentDescription = "share"
                        )
                    }
                }


            }
        }
    }
}

//generate QR Code

fun generateQrCode(link: String): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = BarcodeEncoder().encode(link, BarcodeFormat.QR_CODE, 300, 300)
        BarcodeEncoder().createBitmap(bitMatrix)

    } catch (e: WriterException) {
        Log.e("QrCode", "Error generating QR code", e)
        null
    }
}


//endregion

//region EDIT TEAM
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTeamPane(
    teamId: String,

    profilePicture: String,
    setProfilePicture: (String, Bitmap) -> Unit,

    name: String,
    nameError: String,
    setName: (String) -> Unit,

    description: String,
    setDescription: (String) -> Unit,

    category: String,
    categoryError: String,
    setCategory: (String) -> Unit,

    takePhoto: (String) -> Unit,
    deletePfp: (String) -> Unit

) {
    var showMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val imageUri = rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                imageUri.value = it.toString()
                setProfilePicture(teamId, bitmap)
            }
        }
    val circleColor = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusManager.clearFocus() }
                )
            },
    ) {

        //photo
        if(teamId.isNotBlank()) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (profilePicture.isBlank()) {
                    Icon(
                        modifier = Modifier
                            .size(175.dp)
                            .clip(CircleShape)
                            .background(circleColor)
                            .padding(21.dp),
                        imageVector = Icons.Filled.Group,
                        contentDescription = "team picture",
                        tint = Color.White
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .clip(CircleShape)
                            .background(circleColor)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = profilePicture,
                            contentDescription = "team profile picture",
                            modifier = Modifier.scale(2f)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center

                ) {
                    OutlinedIconButton(
                        onClick = { showMenu = true },
                        border = BorderStroke(1.dp, Color.DarkGray),
                        shape = CircleShape,
                        colors = IconButtonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = Color.Black,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .offset(x = 0.dp) // NICK OFFSET QUI
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Edit Image"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Take a photo") },
                            onClick = {
                                takePhoto(teamId)
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = "Select photo") },
                            onClick = {
                                launcher.launch("image/*")
                                showMenu = false
                            })

                        if (profilePicture.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text(text = "Delete photo") },
                                onClick = { deletePfp(teamId); showMenu = false })
                        }
                    }
                }

            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        //name

        OutlinedTextField(
            value = name,
            onValueChange = setName,
            label = {
                Text(
                    text = "Team name",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PeopleOutline, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = nameError.isNotBlank(),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            )
        )
        if (nameError.isNotBlank()) {
            Text(text = nameError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        //description

        OutlinedTextField(
            value = description,
            onValueChange = setDescription,
            label = {
                Text(
                    text = "$name team description",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        //name

        OutlinedTextField(
            value = category,
            onValueChange = setCategory,
            label = {
                Text(
                    text = "$name team category",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Category, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = categoryError.isNotBlank(),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp,
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            )
        )
        if (categoryError.isNotBlank()) {
            Text(text = categoryError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}

//endregion

//region ACHIEVEMENTS

// gli achievements sono FISSI, per cui un vettore di numeri interi indicherà quali sono stati raggiunti
@Composable
fun AchievementsPane(
    name: String,
    profilePicture: Bitmap?,
    achievementsIds: List<Int>
) {
    val scrollState = rememberScrollState()
    val predefinedAchievements = listOf(
        "Completed 100 tasks",
        "Reached 100% team satisfaction",
        "Won 'Team of the Year' award",
        "Implemented CI/CD pipeline",
        "Achieved 1000 commits",
        "Hosted 10 team-building events",
        "Zero downtime for 1 year"
    )

    // Filter the achievements to display based on the provided IDs
    val achievementsToDisplay = achievementsIds.mapNotNull { id ->
        predefinedAchievements.getOrNull(id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center, // Center horizontally
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            if (profilePicture != null) {
                Image(
                    bitmap = profilePicture.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                )
            } else {
                Icon(
                    Icons.Outlined.Group,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$name's Achievements",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {

            items(achievementsToDisplay) { achievement ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.baseline_go_to_achivements),
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "go to achievements"
                        )
                        Text(
                            text = achievement,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
//endregion

//region TAGS
@Composable
fun TagsPane(
    name: String,
    profilePicture: Bitmap?,
    tags: List<String>,
    addTag: (String, String) -> Unit,
    removeTag: (String, String) -> Unit,
    teamId: String?
) {
    val scrollState = rememberScrollState()
    var newTagText by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center, // Center horizontally
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            if (profilePicture != null) {
                Image(
                    bitmap = profilePicture.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                )
            } else {
                Icon(
                    Icons.Outlined.Group,
                    contentDescription = "Profile Picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$name's Tags",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Campo di input per aggiungere un nuovo tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                placeholder = { Text("Enter a new tag") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            IconButton(
                onClick = {
                    val newTag = newTagText.trim()
                    if (newTag.isNotEmpty()) {
                        if (teamId != null) {
                            addTag(teamId, newTag)
                        }
                        newTagText = ""
                    }
                },
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add tag",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(tags) { tag ->
                if (tag != "") {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween // Allinea gli elementi alla fine della riga
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = "tag image",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.headlineSmall,
                                    //fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (teamId != null) {
                                        removeTag(teamId, tag)
                                    }
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "delete tag",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

            }
        }
    }
}
//endregion

//region JOIN REQUEST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRequestPane(
    teamId: String,
    name: String,
    requestToJoin: List<UserTeamRequestData>,
    profilePicture: String?,
    acceptRequest: (String, UserTeamRequestData, UserRole) -> Unit,
    rejectRequest: (String, String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        if (requestToJoin.isEmpty()) {
            Text(
                text = "There are not join request",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth() // Center the text
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(requestToJoin) { request ->

                    var selectedRole by remember {
                        mutableStateOf(
                            UserRole.VIEWER
                        )
                    }

                    var expanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly// Modificato per distribuire uniformemente lo spazio su entrambi i lati
                    ) {

                        // Prima parte: Username

                        Box(Modifier.weight(1f)) {
                            Text(
                                text = request.userName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Default,
                                color = MaterialTheme.colorScheme.secondary,

                                )
                        }

                        // Seconda parte: Box con il ruolo e menu a discesa
                        Box(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedRole.displayed,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default,
                                    color = MaterialTheme.colorScheme.secondary,

                                    )
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Role"
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                UserRole.entries.forEach { role ->
                                    DropdownMenuItem(text = { Text(text = role.displayed) },
                                        onClick = {
                                            selectedRole = role
                                            expanded = false
                                        })
                                }
                            }
                        }


                        // Terza parte: Icone per accettare e rifiutare
                        Box(Modifier.weight(1f)) {
                            Row {
                                IconButton(
                                    onClick = {
                                        acceptRequest(
                                            teamId,
                                            request,
                                            selectedRole
                                        );
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Accept",
                                        tint = Color.Green
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        rejectRequest(teamId, request.id);

                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Reject",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }

                }
            }


        }
    }
}


//endregion

//region TEAM PERFORMANCE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPerformancePane(
    name: String,
    profilePicture: String?,
    tasks: Map<String, List<TaskData>>
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(state = scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            if (profilePicture != null) {
                /*AsyncImage(
                    model = profilePicture?: "",
                    description = "profile picture",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp, bottom = 8.dp)
                )*/

            } else {
                Icon(
                    Icons.Outlined.Group,
                    contentDescription = "Profile Picture",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
            )
        }


        // Aggregare i task per stato

        val tasksPending = tasks.values.flatten().count { it.status == TaskStatus.PENDING }
        val tasksInProgress = tasks.values.flatten().count { it.status == TaskStatus.IN_PROGRESS }
        val tasksOnHold = tasks.values.flatten().count { it.status == TaskStatus.ON_HOLD }
        val tasksCompleted = tasks.values.flatten().count { it.status == TaskStatus.COMPLETED }
        val tasksOverdue = tasks.values.flatten().count { it.status == TaskStatus.OVERDUE }


        val totalTasks =
            tasksPending + tasksInProgress + tasksOnHold + tasksCompleted + tasksOverdue

        if (totalTasks == 0) {
            Spacer(modifier = Modifier.height(32.dp))

            Row {
                Text(
                    text = "Nothing to see here! ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Total Pending tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = tasksPending.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = if (tasksPending > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "Total In Progress tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = tasksInProgress.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "Total On Hold tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = tasksOnHold.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "Total Completed tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = tasksCompleted.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "Total Overdue tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = tasksOverdue.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(
                    text = "Total tasks: ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Text(
                    text = totalTasks.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            // Grafico a barre
            BarChart(
                data = listOf(
                    tasksPending.toFloat(),
                    tasksInProgress.toFloat(),
                    tasksOnHold.toFloat(),
                    tasksCompleted.toFloat(),
                    tasksOverdue.toFloat()
                ),
                labels = listOf(
                    "Pending",
                    "In Progress",
                    "On Hold",
                    "Completed",
                    "Overdue"
                ),
                colors = listOf(
                    Color.Yellow, Color.Green, Color.Cyan, Color.Red, Color.Blue
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

// Grafico a torta
            PieChart(
                data = listOf(
                    tasksPending.toFloat(),
                    tasksInProgress.toFloat(),
                    tasksOnHold.toFloat(),
                    tasksCompleted.toFloat(),
                    tasksOverdue.toFloat()
                ),
                colors = listOf(
                    Color.Yellow, Color.Green, Color.Cyan, Color.Red, Color.Blue
                )
            )

        }



        Spacer(modifier = Modifier.height(32.dp))
    }
}


@Composable
fun BarChart(data: List<Float>, labels: List<String>, colors: List<Color>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val maxData = data.maxOrNull() ?: 1f
        val barWidth = size.width / (data.size * 2)

        data.forEachIndexed { index, value ->

            val barHeight = (value / maxData) * size.height
            val xOffset = index * 2 * barWidth + barWidth / 2
            drawRect(
                color = colors.getOrElse(index) { Color.Gray },
                topLeft = androidx.compose.ui.geometry.Offset(xOffset, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            )
            drawRect(
                style = Stroke(5f),
                color = Color.Black,
                topLeft = androidx.compose.ui.geometry.Offset(xOffset, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
            )
        }

    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f)
            ) {

                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .align(alignment = Alignment.Center)
                )

            }
        }
    }
}

@Composable
fun PieChart(data: List<Float>, colors: List<Color>) {
    val total = data.sum()
    val angles = data.map { 360 * (it / total) }

    Canvas(
        modifier = Modifier
            .size(200.dp)
    ) {
        var startAngle = 0f

        angles.forEachIndexed { index, sweepAngle ->
            drawArc(
                color = colors.getOrElse(index) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            drawArc(
                style = Stroke(5f),
                color = Color.Black,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}

//endregion

//region TEAM TO JOIN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamToJoinPane(
    teamId: String?,
    userMe: UserData,
    name: String,
    profilePicture: String,
    members: List<UserTeamData>,
    description: String,
    creationDate: String,
    category: String,
    actions: TeamActions,
    onAccept: (String?, UserTeamRequestData, () -> Unit) -> Unit //NICK QUA
) {
    var showAllMembers by remember { mutableStateOf(false) }

    LazyColumn {

        // Picture and name
        item {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp)
                ) {
                    if (profilePicture.isNotBlank()) {
                        Text("I need to figure out the cloud storage")
                        /*
                        Image(
                            bitmap = profilePicture.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                        */
                    } else {
                        Icon(
                            Icons.Outlined.Group,
                            contentDescription = "Profile Picture",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                    )

                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }

        // Description, creation date and category
        item {
            Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Description: ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )

                        Text(
                            text = description,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category: ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )

                        Text(
                            text = category,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)

                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creation Date: ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)

                        )

                        Text(
                            text = creationDate,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }
        }

        // si / no
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Do you wanna join $name team?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            onAccept(
                                teamId, UserTeamRequestData(
                                    id = userMe.id,
                                    name = userMe.name,
                                    surname = userMe.surname,
                                    userName = userMe.userName,
                                ),
                                actions.goHome
                            )

                        }) {
                            Text("Yes")
                        }

                        Button(onClick = { actions.back() }) {
                            Text("No")
                        }
                    }
                }
            }
        }

        // Members part
        item {
            Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val num = members.size
                        Text(
                            text = "$num members",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Default,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp)
                        )

                        if (members.size > 0) {
                            Text(
                                text = if (showAllMembers) "Hide members" else "Show members",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .clickable { showAllMembers = !showAllMembers }
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Members list
        items(if (showAllMembers) members else members.take(0)) { member ->

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (member.profilePicture.isNotBlank()) {
                    Text("I need to figure out the cloud storage")
                    /*
                    Image(
                        bitmap = member.profilePicture.asImageBitmap(),
                        contentDescription = "Member Picture",
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape)
                    )*/
                } else {
                    Icon(
                        Icons.Outlined.Group,
                        contentDescription = "Profile Picture",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(25.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = member.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,

                    )
                Spacer(modifier = Modifier.height(8.dp))
            }

        }


    }
}


//endregion


