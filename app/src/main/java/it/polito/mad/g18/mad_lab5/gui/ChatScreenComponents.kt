package it.polito.mad.g18.mad_lab5.gui

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.UserChatData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.ui.theme.ChatActions
import it.polito.mad.g18.mad_lab5.viewModels.ChatListViewModel
import it.polito.mad.g18.mad_lab5.viewModels.ChatViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//region ## MAIN SCREEN

@Composable
fun ChatScreen(
    chatId: String = "",
    actions: ChatActions,
    vm: ChatViewModel = hiltViewModel()
) {

    // vm attributes
    val userMe = vm.user
    val c by vm.currentChat(userMe.id, chatId).collectAsState(initial = UserChatData())
    val msgList by vm.messageList(chatId).collectAsState(initial = emptyList())
    val isTeamChat = c.teamId.isNotBlank()

    val profileId = if (isTeamChat) {
        c.teamId
    } else {
        c.userId
    }

    val memberList by vm.memberList(c.teamId).collectAsState(initial = emptyList())

    val msgDraft = vm.msgDraft

    LaunchedEffect(msgList) {
        if (!msgList.isNullOrEmpty() && c.chatId.isNotBlank())
            vm.setLastMessageRead(c.chatId, msgList?.last())
    }

    Scaffold(
        bottomBar = {
            ChatBottomAppBar(
                chatId = chatId,
                msgDraft = msgDraft,
                setMsgDraftValue = vm::setMsgDraftValue,
                sendMessage = vm::sendMessage,
                memberList = memberList,
                userMeId = userMe.id,
                userId = c.userId,
                teamId = c.teamId,
                taskId = c.taskId
            )
        },
        topBar = {
            ChatTopAppBar(
                c.title,
                c.pfp,
                isTeamChat,
                actions.back,
                profileId = profileId.toString(),
                navigateToProfile = if (isTeamChat) actions.showTeamDetails else actions.viewProfile,
                isTaskChat = c.taskId.isNotBlank()
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ChatPane(userMe, msgList, memberList, isTeamChat, actions.viewProfile)
        }
    }
}


@Composable
fun ChatListScreen(
    actions: ChatActions,
    bottomBar: @Composable () -> Unit,
    vm: ChatListViewModel = hiltViewModel()
) {

    // vm vars
    val chats by vm.chats.collectAsState(initial = emptyList())
    val userMe by vm.userMe.collectAsState()

    // view vars
    var isSearchBarOpen by rememberSaveable { mutableStateOf(false) }
    var isBottomSheetOpen by rememberSaveable { mutableStateOf(false) }
    val closeBottomSheet = { isBottomSheetOpen = false }

    Scaffold(
        modifier = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier
            .fillMaxSize()
            .padding(end = 41.dp) else Modifier.fillMaxSize(),
        topBar = { ChatTopAppBar() },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    actions.newChat()
                }
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            LazyColumn {
                items(chats) { cdata ->
                    ChatItem(
                        cdata,
                        actions.showChat,
                        cdata.unread,
                        userMe
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
//endregion

//region ## PANES
@Composable
fun ChatPane(
    currentUser: UserData,
    messageList: List<Message>?,
    memberList: List<UserTeamData>,
    isTeamChat: Boolean,
    viewProfile: (String) -> Unit
) {

    val listState = rememberLazyListState()
    LaunchedEffect(messageList?.size) {
        if (messageList != null) {
            listState.animateScrollToItem(messageList.size)
        }
    }
    LazyColumn(
        state = listState
    ) {
        items(messageList ?: emptyList()) {
            if (it.userId == currentUser.id) {
                ChatBalloon(
                    isUserMe = true,
                    isTeamChat = isTeamChat,
                    author = "${currentUser.name} ${currentUser.surname}",
                    msgContent = it.msgContent,
                    memberList = memberList,
                    time = it.timeStamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    viewProfile = viewProfile
                )
            } else {
                val member = memberList.find { a -> a.id == it.userId }
                val author =
                    if (member != null) "${member.name} ${member.surname}" else "User Removed"
                ChatBalloon(
                    isUserMe = false,
                    isTeamChat = isTeamChat,
                    author = author,
                    msgContent = it.msgContent,
                    memberList = memberList,
                    time = it.timeStamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    viewProfile = viewProfile
                )
            }

        }
    }
}
//endregion

//region ## COMPONENTS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
) {
    TopAppBar(
        title = {
            Text("Your Chats", style = MaterialTheme.typography.headlineLarge)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    chatTitle: String,
    pfpImage: String,
    isTeamChat: Boolean,
    back: () -> Unit,
    profileId: String,
    navigateToProfile: (String) -> Unit,
    isTaskChat: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {},
        colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // back button
                IconButton(
                    onClick = { back() }
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                }

                // team icon + name
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    Button(
                        modifier = Modifier.height(IntrinsicSize.Max),
                        onClick = { navigateToProfile(profileId) },
                        shape = RectangleShape,
                        colors = ButtonColors(
                            contentColor = Color.Black,
                            containerColor = Color.Transparent,
                            disabledContentColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                    ) {
                        ProfilePicture(
                            photo = pfpImage,
                            isTeamChat = isTeamChat,
                            chatTitle = chatTitle,
                            isTaskChat = isTaskChat
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = chatTitle, style = MaterialTheme.typography.headlineSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
                    DropdownMenuItem(
                        text = { Text(text = if (isTeamChat) "Team Details" else "Profile details") },
                        onClick = { navigateToProfile(profileId) })
                    /*                    DropdownMenuItem(
                                            text = { Text(text = if (isTeamChat) "Team Media" else "Profile media") },
                                            onClick = { *//*TODO*//* })*/
                }
            }
        }
    )
}

@Composable
fun ChatBottomAppBar(
    chatId: String,
    msgDraft: String,
    setMsgDraftValue: (s: String) -> Unit,
    sendMessage: (String, Message, String, String, String) -> Unit,
    memberList: List<UserTeamData>,
    userMeId: String,
    userId: String,
    teamId: String,
    taskId: String
) {

    // tagging handler
    var isFocused by remember { mutableStateOf(false) }
    val switchFocus = { x: Boolean -> isFocused = x }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = msgDraft
            )
        )
    }
    var tagSearch by remember {
        mutableStateOf("")
    }
    var startTagSearch by remember {
        mutableStateOf(-1)
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
            .padding(4.dp),
    ) {
        // group member search part
        Row {
            if (startTagSearch > -1 && isFocused) {
                Column(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    val list = memberList.filter {
                        it.userName.contains(tagSearch) ||
                                it.name.contains(tagSearch) ||
                                it.surname.contains(tagSearch)
                    }
                    if (list.isNotEmpty())
                        list.map {
                            DropdownMenuItem(

                                text = {
                                    Column {
                                        Text(
                                            text = "${it.name} ${it.surname}",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "@${it.userName}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                },
                                onClick = {
                                    val newTextValue = "${
                                        msgDraft.substring(
                                            0,
                                            startTagSearch + 1
                                        )
                                    }${it.userName} "
                                    startTagSearch = -1
                                    tagSearch = ""
                                    setMsgDraftValue(newTextValue)
                                    val newTextFieldValue = TextFieldValue(
                                        text = newTextValue,
                                        selection = TextRange(newTextValue.length)
                                    )
                                    textFieldValue = newTextFieldValue
                                }
                            )
                            if (it != memberList.last()) {
                                HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
                            }
                        }
                }
            }
        }

        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            //# TEXT INPUT FIELD
            Column(
                modifier = Modifier.weight(5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                TextField(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .onFocusEvent { if (!it.isFocused) switchFocus(false) else switchFocus(true) },
                    value = textFieldValue,
                    onValueChange = {
                        if (it.text.isNotEmpty() && it.text.last() == '@') {
                            startTagSearch = it.text.length - 1
                        }
                        if (startTagSearch > -1) {
                            tagSearch = if (it.text.length > startTagSearch)
                                if (it.text.last() != ' ') {
                                    it.text.substring(startTagSearch + 1)
                                } else {
                                    startTagSearch = -1
                                    ""
                                }
                            else {
                                startTagSearch = -1
                                ""
                            }
                        }

                        setMsgDraftValue(it.text)
                        textFieldValue = it
                    },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        lineBreak = LineBreak.Heading
                    ),
                    maxLines = 4,
                    minLines = 1,
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
                    placeholder = { Text("Message...") }
                )
            }

            //# SEND MESSAGE
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //send message
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.primaryContainer),
                    onClick = {
                        sendMessage(
                            chatId,
                            Message(
                                userId = userMeId,
                                timeStamp = LocalDateTime.now(),
                                msgContent = msgDraft.trim(),
                            ),
                            userId, teamId, taskId
                        )
                        val newTextFieldValue = TextFieldValue("")
                        textFieldValue = newTextFieldValue
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "send message"
                    )
                }
            }
        }
    }


}

private val ChatBubbleShapeLft = RoundedCornerShape(2.dp, 10.dp, 10.dp, 10.dp)
private val ChatBubbleShapeRht = RoundedCornerShape(10.dp, 2.dp, 10.dp, 10.dp)

@Composable
fun ChatBalloon(
    isUserMe: Boolean,
    isTeamChat: Boolean = false,
    author: String,
    msgContent: String,
    memberList: List<UserTeamData>,
    time: String,
    viewProfile: (String) -> Unit
) {

    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val bubbleShape = if (isUserMe) {
        ChatBubbleShapeRht
    } else {
        ChatBubbleShapeLft
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        clipboardManager.setText(AnnotatedString(msgContent))
                    }
                )
            },
        horizontalArrangement = if (isUserMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.60f)
                .clip(bubbleShape)
                .background(color = backgroundBubbleColor)
                .padding(8.dp)
        ) {
            if (isTeamChat) {
                Text(
                    text = if (isUserMe) "You" else author,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (memberList.isNotEmpty()) {

                val annotatedMsg = buildAnnotatedString {

                    msgContent.split(" ").map { msgWord ->

                        // user tag is found
                        if (msgWord.isNotBlank() && memberList.map { it.userName }
                                .contains(msgWord.substring(1))) {
                            pushStringAnnotation(
                                tag = "USER", annotation = msgWord.substring(1)
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("$msgWord ")
                            }
                            pop()

                            // user tag is not found
                        } else {
                            append("$msgWord ")
                        }
                    }
                }
                annotatedMsg.text.trim(' ')
                ClickableText(
                    text = annotatedMsg,
                    onClick = { offset ->
                        annotatedMsg
                            .getStringAnnotations(
                                tag = "USER",
                                start = offset,
                                end = offset
                            ).firstOrNull()
                            ?.let { annotation ->
                                val userId =
                                    memberList.find { x -> x.userName == annotation.item }?.id
                                if (userId != null) {
                                    viewProfile(userId)
                                }
                            }
                    }
                )

            } else {
                Text(text = msgContent)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(text = time + "", color = Color.Gray)
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ProfilePicture(
    photo: String,
    isTeamChat: Boolean,
    chatTitle: String = "",
    isTaskChat: Boolean
) {
    val circleColor = MaterialTheme.colorScheme.primary
    val circleSize = 50.dp

    // default pfp
    if (photo.isBlank()) {

        // default team pfp
        if (isTeamChat) {
            Icon(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(circleColor)
                    .padding(8.dp),
                imageVector = if (!isTaskChat) Icons.Filled.Group else Icons.AutoMirrored.Outlined.StickyNote2,
                contentDescription = "team picture",
                tint = Color.White
            )
        } else {
            // default person pfp
            // if this is a person pfp I can safely assume that the chat title will be: "firstName lastName"
            val names = if (chatTitle.isNotEmpty()) chatTitle.split(" ") else listOf("D", "U")
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .clip(CircleShape)
                    .background(circleColor)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${names.first().first()}${names.last().first()}",
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }

        // image pfp
    } else {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(circleColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo,
                contentDescription = "profile picture",
                modifier = Modifier.scale(2f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatItem(
    chatData: UserChatData,
    showChat: (String) -> Unit,
    showBadge: Boolean,
    userMe: UserData?
) {
    val isTeamChat = chatData.teamId.isNotBlank()
    val fontWeight = if (showBadge) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showChat(chatData.chatId) }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            ProfilePicture(
                photo = chatData.pfp,
                isTeamChat = isTeamChat,
                isTaskChat = chatData.taskId.isNotBlank(),
                chatTitle = chatData.title
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 5.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = chatData.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = fontWeight),
                        modifier = Modifier.weight(1f)
                    )
                    if (showBadge) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .size(10.dp)
                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val details =
                        if (chatData.lastMessage.userId == userMe?.id && chatData.lastMessage.msgContent.isNotBlank()) {
                            "You: ${chatData.lastMessage.msgContent}"
                        } else chatData.lastMessage.msgContent
                    Text(
                        text = details,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = fontWeight),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    ChatTimestampText(chatData)
                }
            }
        }
    }
}

@Composable
fun ChatTimestampText(chatData: UserChatData) {
    val dateTime = chatData.lastMessage.timeStamp
    val now = LocalDate.now()
    val isToday = dateTime.toLocalDate().isEqual(now)
    val formatter = if (isToday) {
        DateTimeFormatter.ofPattern("HH:mm")
    } else {
        DateTimeFormatter.ofPattern("dd/MM")
    }
    Text(
        text = dateTime.format(formatter),
        color = Color.DarkGray
    )
}
//endregion