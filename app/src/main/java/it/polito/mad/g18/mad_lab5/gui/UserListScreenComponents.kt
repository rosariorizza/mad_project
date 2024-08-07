package it.polito.mad.g18.mad_lab5.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.viewModels.DMUserListViewModel
import it.polito.mad.g18.mad_lab5.ui.theme.ChatActions
import it.polito.mad.g18.mad_lab5.ui.theme.TeamActions
import it.polito.mad.g18.mad_lab5.viewModels.AddTeamUserListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    teamId: String,
    actions: TeamActions,
    vm: AddTeamUserListViewModel = hiltViewModel()
) {
    val currentMembers by vm.currentMembers(teamId).collectAsState(initial = emptyList())

    val searchValue = vm.searchKey

    val allUsers by vm.allUsers.collectAsState(initial = emptyList())

    val foundUsers by vm.foundUsers.collectAsState()

    val newMembers = vm.newMembers

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        vm.setSearchKeyValue("")
                    }
                )
            },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row {
                        // back button
                        IconButton(
                            onClick = { actions.back(); vm.clear() }
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        UserListSearchBar(allUsers, vm::filterUsers, searchValue, vm::setSearchKeyValue)
                    }
                },
                actions = if (searchValue.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { vm.setSearchKeyValue("") }
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "back")
                        }
                    }
                } else {
                    {
                        IconButton(
                            onClick = { vm.save(teamId, actions.back) }
                        ) {
                            Icon(imageVector = Icons.Filled.Save, contentDescription = "save")
                        }
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            //lista membri aggiunti
            AddedUsersList(
                addedUsers = newMembers,
                removeNewMember = vm::removeNewMember
            )
            if (newMembers.isNotEmpty()) {
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
            }

            FoundUserList(
                currentMembers = currentMembers,
                foundUsers = foundUsers,
                addNewMember = vm::addNewMember
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListDMScreen(
    actions: ChatActions,
    vm: DMUserListViewModel = hiltViewModel()
) {

    val sarchValue = vm.searchKey
    val allUsers by vm.allUsers.collectAsState(initial = emptyList())
    val foundUsers by vm.foundUsers.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                        vm.setSearchKeyValue("")
                    }
                )
            },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Row {
                        // back button
                        IconButton(
                            onClick = { actions.back(); vm.clear() }
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        UserListSearchBar(allUsers, vm::filterUsers, sarchValue, vm::setSearchKeyValue)
                    }
                },
                actions = if (sarchValue.isNotBlank()) {
                    {
                        IconButton(
                            onClick = { vm.setSearchKeyValue("") }
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "back")
                        }
                    }
                } else { {} }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            //lista membri trovati
            FoundUserListDM(
                foundUsers = foundUsers,
                showChat = actions.showChat,
                newChat = vm::startNewChat
            )
        }
    }
}

//region Components
@Composable
fun UserListSearchBar(
    users: List<UserData>,
    filterUsers:(String, List<UserData>) -> Unit,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
    ) {


        TextField(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            value = value,
            onValueChange = { value ->
                onValueChange(value)
                filterUsers(value,users)
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
            placeholder = { Text("Search...") }
        )
    }
}


@Composable
fun FoundUserListDM(
    modifier: Modifier = Modifier,
    foundUsers: List<UserData>,
    showChat: (String) -> Unit,
    newChat: (UserData, ((String )-> Unit)) -> Unit


) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(foundUsers) {
            FoundUserItemDM(u = it, showChat, newChat)
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Composable
fun FoundUserItemDM(
    u: UserData,
    showChat: (String) -> Unit,
    newChat: (UserData, ((String )-> Unit)) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { newChat(u, showChat) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfilePicture(
                photo = u.profilePicture,
                isTeamChat = false,
                chatTitle = "${u.name} ${u.surname}",
                isTaskChat = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "${u.name} ${u.surname}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "@${u.userName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun FoundUserList(
    modifier: Modifier = Modifier,
    currentMembers: List<UserTeamData>,
    foundUsers: List<UserData>,
    addNewMember: (u: UserData) -> Unit
) {

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(foundUsers) {
            if(!currentMembers.map { x -> x.id }.contains(it.id)) {
                FoundUserItem(u = it, addNewMember)
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FoundUserItem(
    u: UserData,
    addNewMember: (u: UserData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { addNewMember(u) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfilePicture(
                photo = u.profilePicture,
                isTeamChat = false,
                chatTitle = "${u.name}${u.surname}",
                isTaskChat = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "${u.name} ${u.surname}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "@${u.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        IconButton(onClick = { addNewMember(u) }) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "add new member")
        }
    }
}

@Composable
fun AddedUsersList(
    modifier: Modifier = Modifier,
    addedUsers: List<UserData>,
    removeNewMember: (u: UserData) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(addedUsers) {
            AddedUserItem(u = it, removeNewMember)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AddedUserItem(
    u: UserData,
    removeNewMember: (u: UserData) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfilePicture(
                photo = u.profilePicture,
                isTeamChat = false,
                chatTitle = "${u.name}${u.surname}",
                isTaskChat = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "${u.name} ${u.surname}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "@${u.userName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        IconButton(onClick = { removeNewMember(u) }) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = "add new member")
        }
    }
}
//endregion