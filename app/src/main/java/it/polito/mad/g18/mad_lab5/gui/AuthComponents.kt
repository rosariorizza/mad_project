package it.polito.mad.g18.mad_lab5.gui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PermIdentity
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import it.polito.mad.g18.mad_lab5.R
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.ui.theme.AuthActions
import it.polito.mad.g18.mad_lab5.viewModels.UserRegistrationViewModel
import kotlin.Result
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


@SuppressLint("SuspiciousIndentation")
@Composable
fun Login(vm: UserRegistrationViewModel = hiltViewModel(), actions: AuthActions) {

    LaunchedEffect(actions) {
        vm.checkIfUserRegistered(
            Firebase.auth.currentUser?.uid,
            actions.goHome,
            {})
    }

    val context = LocalContext.current
    val launcher = rememberFirebaseAuthLauncher(
        onAuthComplete = { result ->
            vm.checkIfUserRegistered(
                result.user?.uid,
                onTrue = actions.goHome,
                onFalse = actions.register
            )
        },
        onAuthError = {
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    )

    val token = stringResource(id = R.string.web_client_id)


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /*Image(
        painter = painterResource(id = android.R.drawable.sym_def_app_icon),
        contentDescription = "App Image",
        modifier = Modifier
            .size(150.dp)
            .padding(16.dp)
        )*/

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Not logged in",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {

            val gso =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(token)
                    .requestEmail()
                    .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            launcher.launch(googleSignInClient.signInIntent)
        }) {
            Text("Sign in via Google")
        }
        Text(
            text = "Please register if you don't have an account registered with Google.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Blue,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("GoogleAuth", "account $account")
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            Log.d("GoogleAuth", e.toString())
            onAuthError(e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Register(vm: UserRegistrationViewModel = hiltViewModel(), actions: AuthActions) {

    var isUsernameAvailable by remember { mutableStateOf(true) }
    var checkingUsername by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val imageUri = rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->

            // BRODER LE IMMAGINI DAL CLOUD SI PRENDONO IN UN MODO MOLTO PIù SEMPLICE

            /*
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                imageUri.value = it.toString()
                vm.setPhotoValue(bitmap)
            }
            */
        }
    var showMenu by remember { mutableStateOf(false) }

    // Do I have to put the same email that I gave to Google???
    //val firebaseUserUid = Firebase.auth.currentUser?.uid //?: return Result.failure(Exception("User not logged in"))


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "Welcome to BeGroup!", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))



        // TextFields for user input
        OutlinedTextField(
            value = vm.name,
            onValueChange = { vm.setNameValue(it) },
            label = { Text(text = "First Name", color = Color.Blue) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PermIdentity, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = vm.nameErr.isNotBlank(),
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
        if (vm.nameErr.isNotBlank()) {
            Text(text = vm.nameErr, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = vm.surname,
            onValueChange = { vm.setSurnameValue(it) },
            label = { Text(text = "Last Name", color = Color.Blue) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.PermIdentity, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = vm.surnameErr.isNotBlank(),
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
        if (vm.surnameErr.isNotBlank()) {
            Text(text = vm.surnameErr, color = MaterialTheme.colorScheme.error)
        }

        // TODO disable button register if userName is already taken
        OutlinedTextField(
            value = vm.userName,
            onValueChange = {
                vm.setUserNameValue(it)
                coroutineScope.launch {
                    checkingUsername = true
                    isUsernameAvailable = !vm.isUsernameTaken(it)
                    if (it.isBlank()) vm.setUserNameError("Username cannot be blank")
                    else if (!isUsernameAvailable) vm.setUserNameError("User name is already taken")
                    else vm.setUserNameError("")
                    checkingUsername = false
                }
            },
            label = { Text(text = "User Name", color = Color.Blue) },
            modifier = Modifier.fillMaxWidth(),
            isError = vm.userNameErr.isNotBlank()
        )
        /*if (!isUsernameAvailable) {
            Text(text = "Username is already taken", color = MaterialTheme.colorScheme.error)
        } else{
            Text(text = "Username is available", color = MaterialTheme.colorScheme.primary)
        }*/
        if (checkingUsername) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
        if (vm.userNameErr.isNotBlank()) {
            Text(text = vm.userNameErr, color = MaterialTheme.colorScheme.error)
        }

        /*OutlinedTextField(
            value = vm.email,
            onValueChange = { /*vm.setEmailValue(it) */},
            label = { Text("Email Not Working") },
            modifier = Modifier.fillMaxWidth()
        )*/

        // Birth Date picker
        Row {
            Column(
                modifier = Modifier.size(width = 80.dp, height = 50.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Date:"
                )
            }
            FlowRow {
                DateFilterChip(vm.birthDate, vm::setBirthDateValue, "BirthDate ", "Add Birth Date")
            }
        }
        if (vm.birthDateErr.isNotBlank()) {
            Text(text = vm.birthDateErr, color = MaterialTheme.colorScheme.error)
        }



        OutlinedTextField(
            value = vm.location,
            onValueChange = { vm.setLocationValue(it) },
            label = { Text(text = "Location", color = Color.Blue) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocationOn, // Icona da utilizzare
                    contentDescription = null, // Content description opzionale
                    modifier = Modifier.size(18.dp) // Imposta la dimensione dell'icona
                )
            },
            isError = vm.locationErr.isNotBlank(),
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
        if (vm.locationErr.isNotBlank()) {
            Text(text = vm.locationErr, color = MaterialTheme.colorScheme.error)
        }

        //phone number
        OutlinedTextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone
            ),
            value = vm.phoneNumber,
            onValueChange = { vm.setPhoneNumberValue(it) },
            label = { Text(text = "Phone Number", color = Color.Blue) },
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
            isError = vm.phoneNumber.isNotBlank()
        )
        if (vm.phoneNumberErr.isNotBlank()) {
            Text(text = vm.phoneNumberErr, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = vm.description,
            onValueChange = { vm.setDescriptionValue(it) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // Button to perform registration
        Button(
            onClick = {
                // Call registration function in ViewModel
                coroutineScope.launch {
                    vm.register { actions.goHome() }

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(text = "Register")
        }
    }
}

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

/*
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

}*/



