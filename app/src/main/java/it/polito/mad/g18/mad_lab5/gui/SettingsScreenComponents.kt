package it.polito.mad.g18.mad_lab5.gui

import android.content.res.Configuration
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import it.polito.mad.g18.mad_lab2.UserProfileViewModel
import it.polito.mad.g18.mad_lab5.ui.theme.SettingsActions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KSuspendFunction1
// Add this to your utils file
import android.content.Context
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.viewModels.SettingsViewModel
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


fun clearCredentialState(context: Context) {
    val credentialManager = CredentialManager.create(context)
    val clearCredentialStateRequest = ClearCredentialStateRequest()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            credentialManager.clearCredentialState(clearCredentialStateRequest)
            Log.d("AuthUtils", "Credential state cleared")
        } catch (e: Exception) {
            Log.e("AuthUtils", "Failed to clear credential state", e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = hiltViewModel(),
    actions: SettingsActions,

    bottomBar: @Composable () -> Unit
) {

    val context = LocalContext.current
    val darkMode by vm.darkMode.collectAsState()

    Scaffold(
        modifier = if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Modifier.padding(end = 41.dp) else Modifier,
        bottomBar = bottomBar
    ) { paddingValues ->
        paddingValues.toString()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineLarge
                )
            })
            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    actions.showUser()
                }
                .padding(vertical = 20.dp)

            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = 16.dp),
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "user profile"
                )
                Text(text = "User Profile", style = MaterialTheme.typography.headlineSmall)
            }

            HorizontalDivider()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "App Theme",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                ThemeSwitcher(
                    darkTheme = darkMode,
                    size = 50.dp,
                    padding = 5.dp,
                    onClick = vm::toggleTheme
                )
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { clearCredentialState(context); actions.logOut() }, modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(0.5f).align(Alignment.CenterHorizontally)) {
                Text(text = "Log Out")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    actions: SettingsActions,
    vm: UserProfileViewModel = hiltViewModel()
) {

    val userData by vm.userData.observeAsState()

    // Estrarre i dati direttamente
    val name = userData?.name.orEmpty()
    val surname = userData?.surname.orEmpty()
    val userName = userData?.userName.orEmpty()
    val email = userData?.email.orEmpty()
    val birthDate = userData?.birthDate
    val phoneNumber = userData?.phoneNumber.orEmpty()
    val description = userData?.description.orEmpty()
    val location = userData?.location.orEmpty()

    val photo = userData?.profilePicture.orEmpty()

    val tasks by vm.tasks.collectAsState()

    val kpi = 6.9

    //utility vars
    val appBarColor = MaterialTheme.colorScheme.background

    // orientation
    val configuration = LocalConfiguration.current

    val imageUri = rememberSaveable { mutableStateOf("") }
    val painter = rememberAsyncImagePainter(imageUri.value)
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                vm.updateProfilePicture(bitmap)
            }
        }

    val deleteImage = { vm.deleteProfilePicture() }

    BackHandler(
        onBack = {
            actions.back()
        },
        enabled = true,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // Apply background color
            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
        ) {


            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = appBarColor),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (vm.isEditing) {
                                vm.save()
                            } else {
                                actions.back()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                },
                actions = {
                    if (vm.isEditing)
                        IconButton(onClick = { vm.save() }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "save")
                        }
                    else
                        IconButton(onClick = { vm.edit() }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "edit")
                        }
                }
            )

            if (vm.isEditing) {
                BackHandler(
                    onBack = {
                        vm.save()
                    },
                    enabled = true,
                )
                EdiPane(
                    firstName = name,
                    firstNameErr = vm.nameErr,
                    setFirstName = vm::setName,

                    lastName = surname,
                    lastNameErr = vm.surnameErr,
                    setLastName = vm::setSurname,

                    userName = userName,
                    userNameErr = vm.userNameErr,
                    setUserName = vm::setUserName,

                    birthDate = birthDate,
                    birthDateErr = vm.birthDateErr,
                    setBirthDate = vm::setBirthDateValue,

                    email = email,
                    setEmail = vm::setEmailValue,

                    location = location,
                    locationErr = vm.locationErr,
                    setLocation = vm::setLocationValue,

                    description = description,
                    setDescription = vm::setDescriptionValue,

                    phoneNumber = phoneNumber,
                    setPhoneNumber = vm::setPhoneNumberValue,
                    phoneNumberErr = vm.phoneNumberErr,

                    kpi = kpi,
                    setKpi = vm::setKpiValue,
                    isUsernameTaken = vm::isUsernameTaken,
                    setUserNameError = vm::setUserNameError
                )

            } else {
                when (configuration.orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        InfoPaneHorizontal(
                            photo = photo,
                            painter = painter,
                            launcher = launcher,
                            imageUri = imageUri,
                            firstName = name,
                            lastName = surname,
                            userName = userName,
                            birthDate = birthDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                ?: "",
                            email = email,
                            location = location,
                            description = description,
                            kpi = kpi,
                            takePhoto = actions.takePhoto,
                            delete = deleteImage,
                            phoneNumber = phoneNumber,
                            tasks = tasks
                        )

                    }

                    else -> {
                        InfoPaneVertical(
                            photo = photo,
                            painter = painter,
                            launcher = launcher,
                            imageUri = imageUri,
                            firstName = name,
                            lastName = surname,
                            userName = userName,
                            birthDate = birthDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                                ?: "",
                            email = email,
                            location = location,
                            description = description,
                            kpi = kpi,
                            takePhoto = actions.takePhoto,
                            delete = deleteImage,
                            phoneNumber = phoneNumber,
                            tasks = tasks
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageComponent(
    modifier: Modifier = Modifier,
    photo: String,
    painter: AsyncImagePainter,
    launcher: ManagedActivityResultLauncher<String, Uri?>,
    imageUri: MutableState<String>,
    firstName: String,
    lastName: String,
    takePhoto: () -> Unit,
    delete: () -> Unit
) {
    var showMenu by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {

        val circleColor = MaterialTheme.colorScheme.primary

        Row(
            verticalAlignment = Alignment.Bottom
        ) {

            if (photo.isBlank()) {
                val char1 = if (firstName.isBlank()) "D" else firstName[0]
                val char2 = if (lastName.isBlank()) "U" else lastName[0]
                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${char1}${char2}",
                        style = TextStyle(color = Color.White, fontSize = 80.sp),
                    )
                }
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
                        model = photo,
                        contentDescription = "profile picture",
                        modifier = Modifier.scale(2f)
                    )
                }
            }

            // Positioned button with dropdown directly below
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

        ) {
            OutlinedIconButton(
                onClick = { showMenu = !showMenu },
                border = BorderStroke(1.dp, Color.DarkGray),
                shape = CircleShape,
                colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = Color.Black,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Edit Image"
                )
            }

                // DropdownMenu positioned to naturally flow below the button
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Take Picture") },
                        onClick = { takePhoto() }
                    )
                    DropdownMenuItem(
                        text = { Text("Select from Gallery") },
                        onClick = {
                            showMenu = false
                            launcher.launch("image/*")
                        }
                    )
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { delete() })
                }
            }
        }

    }
}

/*
        else if (photo == null && imageUri.value.isNotBlank()) {
            Image(
                painter = painter,
                contentScale = ContentScale.Crop,
                contentDescription = "profile picture",
                modifier = Modifier
                    .size(175.dp)
                    .clip(CircleShape)
            )
        }
*/

@Composable
fun Line(header: String, text: String) {
    Spacer(modifier = Modifier.height(11.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun InfoPaneVertical(
    photo: String,
    painter: AsyncImagePainter,
    launcher: ManagedActivityResultLauncher<String, Uri?>,
    imageUri: MutableState<String>,
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    email: String,
    location: String,
    description: String,
    kpi: Double,
    takePhoto: () -> Unit,
    delete: () -> Unit,
    phoneNumber: String,
    tasks: Map<String, List<TaskData>>
) {

    val scrollState = rememberScrollState()

    //profile picture 1/3
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {

            ImageComponent(
                Modifier,
                photo,
                painter,
                launcher,
                imageUri,
                firstName,
                lastName,
                takePhoto,
                delete
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(2f)
                .padding(horizontal = 16.dp)
                .verticalScroll(state = scrollState)

        ) {
            InfoComponent(
                firstName,
                lastName,
                userName,
                birthDate,
                email,
                location,
                description,
                kpi, phoneNumber,
                tasks
            )
        }
    }
}

@Composable
fun InfoPaneHorizontal(
    photo: String,
    painter: AsyncImagePainter,
    launcher: ManagedActivityResultLauncher<String, Uri?>,
    imageUri: MutableState<String>,
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    email: String,
    location: String,
    description: String,
    kpi: Double,
    takePhoto: () -> Unit,
    delete: () -> Unit,
    phoneNumber: String,
    tasks: Map<String, List<TaskData>>
) {

    val scrollState = rememberScrollState()
    //profile picture 1/3
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
                .weight(1f)
        ) {
            ImageComponent(
                Modifier,
                photo,
                painter,
                launcher,
                imageUri,
                firstName,
                lastName,
                takePhoto,
                delete
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .weight(2f)
                .verticalScroll(state = scrollState)
        ) {
            InfoComponent(
                firstName,
                lastName,
                userName,
                birthDate,
                email,
                location,
                description,
                kpi, phoneNumber, tasks
            )
        }
    }
}

@Composable
fun InfoComponent(
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    email: String,
    location: String,
    description: String,
    kpi: Double, phoneNumber: String,
    tasks: Map<String, List<TaskData>>

) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$firstName $lastName",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "@$userName",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (email.isNotBlank()) {
        Line(header = "Email", text = email)
    }

    if (birthDate.isNotBlank()) {
        Line(header = "Birth Date", text = birthDate)
    }

    if (phoneNumber.isNotBlank()) {
        Line(header = "Phone Number", text = phoneNumber)
    }

    if (location.isNotBlank()) {
        Line(header = "Location", text = location)
    }

    if (description.isNotBlank()) {
        Line(header = "Description", text = description)
    }



    Spacer(modifier = Modifier.height(32.dp))

    val tasksPending = tasks.values.flatten().count { it.status == TaskStatus.PENDING }
    val tasksInProgress = tasks.values.flatten().count { it.status == TaskStatus.IN_PROGRESS }
    val tasksOnHold = tasks.values.flatten().count { it.status == TaskStatus.ON_HOLD }
    val tasksCompleted = tasks.values.flatten().count { it.status == TaskStatus.COMPLETED }
    val tasksOverdue = tasks.values.flatten().count { it.status == TaskStatus.OVERDUE }

    val totalTasks = tasksPending + tasksInProgress + tasksOnHold + tasksCompleted + tasksOverdue


    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (totalTasks != 0) {

            Text(
                text = "Your tasks situations ",
                style = MaterialTheme.typography.titleMedium,
                //fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,

                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Spacer(modifier = Modifier.height(48.dp))

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
                        /*MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.surfaceTint,
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.colorScheme.scrim,
                        MaterialTheme.colorScheme.inverseOnSurface*/
                    )
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdiPane(
    firstName: String,
    firstNameErr: String,
    setFirstName: (String) -> Unit,

    lastName: String,
    lastNameErr: String,
    setLastName: (String) -> Unit,

    userName: String,
    userNameErr: String,
    setUserName: (String) -> Unit,

    phoneNumber: String,
    setPhoneNumber: (String) -> Unit,
    phoneNumberErr: String,

    description: String,
    setDescription: (String) -> Unit,

    birthDate: LocalDate?,
    setBirthDate: (LocalDate?) -> Unit,
    birthDateErr: String,

    email: String,
    setEmail: (String) -> Unit,

    location: String,
    locationErr: String,
    setLocation: (String) -> Unit,

    kpi: Double,
    setKpi: (Double) -> Unit,
    isUsernameTaken: KSuspendFunction1<String, Boolean>,
    setUserNameError: (String) -> Unit,

    ) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var isUsernameAvailable by remember { mutableStateOf(true) }
    var checkingUsername by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { focusManager.clearFocus() }
                )
            },
    ) {

        //profile info

        Text(
            text = "Profile Info",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
        )

        Spacer(modifier = Modifier.width(8.dp))

        //first name
        OutlinedTextField(
            value = firstName,
            onValueChange = setFirstName,
            label = { Text(text = "First Name", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PermIdentity, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = firstNameErr.isNotBlank(), // firstNameCheck() -> Boolean missing
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
        if (firstNameErr.isNotBlank()) {
            Text(text = firstNameErr, color = MaterialTheme.colorScheme.error)
        }

        //last name
        OutlinedTextField(
            value = lastName,
            onValueChange = setLastName,
            label = { Text(text = "Last Name", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PermIdentity, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = lastNameErr.isNotBlank(),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp, // Imposta la dimensione del testo
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            ),
        )
        if (lastNameErr.isNotBlank()) {
            Text(text = lastNameErr, color = MaterialTheme.colorScheme.error)
        }

        //phone number
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            value = phoneNumber,
            onValueChange = setPhoneNumber,
            label = { Text(text = "Phone Number", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid, // Icona da utilizzare
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
            ),
            isError = phoneNumberErr.isNotBlank()
        )
        if (phoneNumberErr.isNotBlank()) {
            Text(text = phoneNumberErr, color = MaterialTheme.colorScheme.error)
        }

        //location
        OutlinedTextField(
            value = location,
            onValueChange = setLocation,
            label = { Text(text = "Location", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = locationErr.isNotBlank(),
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 18.sp, // Imposta la dimensione del testo
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            )
        )
        if (locationErr.isNotBlank()) {
            Text(text = locationErr, color = MaterialTheme.colorScheme.error)
        }


        //description
        OutlinedTextField(
            value = description,
            onValueChange = setDescription,
            label = { Text(text = "About me", color = MaterialTheme.colorScheme.secondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            modifier = Modifier
                .height(100.dp)
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .heightIn(max = 500.dp), // Imposta l'altezza massima a 500.dp
            textStyle = TextStyle(
                fontSize = 18.sp, // Imposta la dimensione del testo
            ),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = Color.Black,
                containerColor = Color.Transparent
            )
        )

        //account info
        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = "Account Info",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
        )

        Spacer(modifier = Modifier.width(16.dp))

        //username
        OutlinedTextField(
            value = userName,
            onValueChange = {
                /*Log.d("USERNAME it", it)
                if(it.isBlank()) {
                    Log.d("USERNAME it", "E' vuoto")
                    setUserNameError("Username cannot be blank")
                } else setUserNameError("")*/
                setUserName(it)
                coroutineScope.launch {
                    checkingUsername = true
                    isUsernameAvailable = !isUsernameTaken(it)
                    if (it.isBlank()) setUserNameError("Username cannot be blank")
                    else if (!isUsernameAvailable) setUserNameError("User name is already taken")
                    else setUserNameError("")
                    checkingUsername = false
                }
            },
            label = { Text(text = "User Name", color = MaterialTheme.colorScheme.secondary) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isUsernameAvailable || userName.isBlank()
        )
        /*if (!isUsernameAvailable) {
            Text(text = "Username is already taken", color = MaterialTheme.colorScheme.error)
        } else{
            Text(text = "Username is available", color = MaterialTheme.colorScheme.primary)
        }*/
        if (checkingUsername) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        if (userNameErr.isNotBlank()) {
            Text(text = userNameErr, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


//region SELECT THEME

@Composable
fun ThemeSwitcher(
    darkTheme: Boolean = false,
    size: Dp = 50.dp,
    iconSize: Dp = size / 3,
    padding: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    parentShape: Shape = CircleShape,
    toggleShape: Shape = CircleShape,
    animationSpec: AnimationSpec<Dp> = tween(durationMillis = 300),
    onClick: () -> Unit
) {
    val offset by animateDpAsState(
        targetValue = if (darkTheme) 0.dp else size,
        animationSpec = animationSpec
    )

    Box(modifier = Modifier
        .width(size * 2)
        .height(size)
        .clip(shape = parentShape)
        .clickable { onClick() }
        .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .offset(x = offset)
                .padding(all = padding)
                .clip(shape = toggleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {}
        Row(
            modifier = Modifier
                .border(
                    border = BorderStroke(
                        width = borderWidth,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    shape = parentShape
                )
        ) {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = "Theme Icon",
                    tint = if (darkTheme) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Theme Icon",
                    tint = if (darkTheme) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}
//endregion
