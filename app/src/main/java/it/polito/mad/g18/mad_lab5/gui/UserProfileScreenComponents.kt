package it.polito.mad.g18.mad_lab5.gui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import it.polito.mad.g18.mad_lab5.viewModels.UserTeamProfile
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicUserProfileScreen(
    userId: String? = null,
    teamId: String? = null,
    back: () -> Unit,
    vm: UserTeamProfile = hiltViewModel()
) {

    LaunchedEffect(userId, teamId) {
        vm.setUserTeamId(userId)
        vm.setTeamDetail(teamId)
    }

    val user by vm.user.collectAsState()

    //val userData by vm.userData.observeAsState()

    // Estrarre i dati direttamente
    val name = user?.name.orEmpty()
    val surname = user?.surname.orEmpty()
    val userName = user?.userName.orEmpty()
    val birthDate = user?.birthDate
    val description = user?.description.orEmpty()
    val location = user?.location.orEmpty()
    //val email = user?.email.orEmpty()
    //val phoneNumber = user?.phoneNumber.orEmpty()
    val photo = user?.profilePicture.orEmpty()

    val tasks by vm.tasks.collectAsState()

    val kpi = 8.0

    //utility vars
    val appBarColor = MaterialTheme.colorScheme.background
    // orientation
    val configuration = LocalConfiguration.current

    //ui
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // Apply background color
            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = appBarColor),
                navigationIcon = {
                    IconButton(onClick = { back() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back"
                        )
                    }
                }
            )
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    PublicInfoPaneHorizontal(
                        photo = photo,
                        firstName = name,
                        lastName = surname,
                        userName = userName,
                        birthDate = birthDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            ?: "",
                        location = location,
                        description = description,
                        kpi = kpi,
                        tasks = tasks
                        //email = email,
                        //phoneNumber = phoneNumber
                    )
                }

                else -> {
                    PublicInfoPaneVertical(
                        photo = photo,
                        firstName = name,
                        lastName = surname,
                        userName = userName,
                        birthDate = birthDate?.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            ?: "",
                        location = location,
                        description = description,
                        kpi = kpi,
                        tasks = tasks
                        //email = email,
                        //phoneNumber = phoneNumber
                    )
                }
            }
        }
    }
}


@Composable
fun PublicImageComponent(
    photo: String,
    firstName: String,
    lastName: String,

    ) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {

        val circleColor = MaterialTheme.colorScheme.primary

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
    }

}


@Composable
fun PublicInfoPaneVertical(
    photo: String,
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    location: String,
    description: String,
    kpi: Double,
    tasks: Map<String, List<TaskData>>,
//    email: String,
//    phoneNumber: String,
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

            PublicImageComponent(
                photo,
                firstName,
                lastName,
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
            PublicInfoComponent(
                firstName,
                lastName,
                userName,
                birthDate,
                location,
                description,
                kpi,
                tasks = tasks,
//                email = email,
//                phoneNumber = phoneNumber
            )
        }
    }
}

@Composable
fun PublicInfoPaneHorizontal(
    photo: String,
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    location: String,
    description: String,
    kpi: Double,
    tasks: Map<String, List<TaskData>>,
//    email: String,
//    phoneNumber: String,
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
            PublicImageComponent(
                photo,
                firstName,
                lastName
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .weight(2f)
                .verticalScroll(state = scrollState)
        ) {
            PublicInfoComponent(
                firstName,
                lastName,
                userName,
                birthDate,
                location,
                description,
                kpi,
                tasks,
//                email,
//                phoneNumber
            )
        }
    }
}

@Composable
fun PublicInfoComponent(
    firstName: String,
    lastName: String,
    userName: String,
    birthDate: String,
    location: String,
    description: String,
    kpi: Double,
    tasks: Map<String, List<TaskData>>,
//    email: String,
//    phoneNumber: String
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

    /*Card(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Email: ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )



            Text(
                text = email,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
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
                text = "Birthdate: ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )

            Text(
                text = birthDate,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
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
                text = "Phone number ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )

            Text(
                text = phoneNumber ,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )


        }

        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Location ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )

            Text(
                text = location ,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )


        }

        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Descriprion ",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )

            Text(
                text = description ,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Default,
                    color = Color.DarkGray
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Per far sì che il testo occupi tutto lo spazio disponibile
            )


        }

    }*/

    if (birthDate.isNotBlank())
        Line(header = "Birth Date", text = birthDate)

    if (location.isNotBlank())
        Line(header = "Location", text = location)

    //Line(header = "Score", text = "$kpi/10")

    if (description.isNotBlank())
        Line(header = "Description", text = description)

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
                text = "@$userName tasks situations ",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 25.sp,
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
                    )
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

}
