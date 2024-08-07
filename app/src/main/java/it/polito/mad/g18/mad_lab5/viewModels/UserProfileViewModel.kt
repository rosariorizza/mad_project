package it.polito.mad.g18.mad_lab2

//NICK profilo dalle settings

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import it.polito.mad.g18.mad_lab5.viewModels.TaskFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

// ## VIEWMODEL
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    //private val model: MainModel,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {

    val user = userMe.value ?: UserData() // you should use this to get user values


    private val _userData = MutableLiveData<UserData?>(user)
    val userData: LiveData<UserData?> = _userData

    val oldUserName = mutableStateOf("")
    init {
        // Inizializza il flusso nel ViewModel
        viewModelScope.launch {
            userRepository.getUserMe().collect {
                _userData.value = it
                oldUserName.value = it?.userName.toString()
            }
        }
    }

    var isEditing by mutableStateOf(value = false)
        private set

    fun edit() {
        isEditing = true
    }


    private fun validate(): Boolean {
        checkFirstName()
        checkLastName()
        //checkUserName()
        //checkLocation()
        checkPhoneNumber()
        if (nameErr.isBlank() &&
            surnameErr.isBlank() &&
            //locationErr.isBlank() &&
            phoneNumberErr.isBlank() &&
            userNameErr.isBlank()
        ) {
            return true
        }
        return false
    }

    fun save() {
        if(validate()) {
            viewModelScope.launch {
                userData.value?.let {
                    userRepository.updateUser(it).onFailure { a-> Log.d("save failure", a.message.toString()) } }
            }
            isEditing = false
        }
    }

    // region PHOTO SECTION
    var tempGalleryPhoto: AsyncImagePainter? by mutableStateOf(null)
    fun setTempGalleryPhotoValue(v: AsyncImagePainter) {
        tempGalleryPhoto = v
    }
    fun updateProfilePicture(pfp: Bitmap?) {
        if(pfp != null) {
            viewModelScope.launch(Dispatchers.IO) {
                userRepository.setUserPfp(user.id, pfp)
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.deleteUserPfp(user.id)
        }
    }

    // endregion

    //var name = userData.value?.name
    //  private set

    /*val name: LiveData<String?> = _userData.map { userData ->
        userData?.name
    }*/
    val name: String
        get() = userData.value?.name.orEmpty()
    var nameErr by mutableStateOf(value = "")
        private set

    /*
    fun setName(v: String) {
        //user.setName(v)
        userData.value?.copy(name = v)
    }
    */


    fun setName(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(name = v)
            _userData.value = updatedUser
        }
    }

    private fun checkFirstName() {
        if (name.isBlank()) {
            nameErr = "First name cannot be blank"
        } else nameErr = ""
    }


    /*val surname: LiveData<String?> = _userData.map { userData ->
        userData?.surname
    }*/
    val surname: String
        get() = userData.value?.surname.orEmpty()
    /*var surname = user.surname
        private set*/
    var surnameErr by mutableStateOf(value = "")
        private set

    /*fun setSurname(v: String) {
        //setSurname(v)
    }*/

    fun setSurname(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(surname = v)
            _userData.value = updatedUser
        }
    }

    private fun checkLastName() {
        if (surname.isBlank()) {
            surnameErr = "Last name cannot be blank"
        } else surnameErr = ""
    }
    val userName: String
        get() = userData.value?.userName.orEmpty()
    var userNameErr by mutableStateOf(value = "")
        private set

    fun setUserName(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(userName = v)
            _userData.value = updatedUser
        }
    }

    fun setUserNameError(v: String) {
        userNameErr = v
    }

    suspend fun isUsernameTaken(username: String) : Boolean {
        if (username == oldUserName.value) {
            return false
        }
        return userRepository.isUsernameTaken(username)
    }

    val birthDate: LocalDate?
        get() = userData.value?.birthDate
    var birthDateErr by mutableStateOf(value = "")
        private set

    fun setBirthDateValue(v: LocalDate?) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(birthDate = v)
            _userData.value = updatedUser
        }
    }

    private fun checkBirthDate() {
        if (birthDate == null) {
            birthDateErr = "Birth Date cannot be blank"
        } else birthDateErr = ""
    }

    // Ottieni l'utente corrente
    val email: String
        get() = userData.value?.email.orEmpty()
    /*var emailErr by mutableStateOf(value = "")
        private set
    */
    fun setEmailValue(v: String) {
        //user.setEmail(v)
    }

    val location: String
        get() = userData.value?.location.orEmpty()
    var locationErr by mutableStateOf(value = "")
        private set

    /*fun setLocationValue(v: String) {
        //user.setLocation(v)
    }*/

    fun setLocationValue(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(location = v)
            _userData.value = updatedUser
        }
    }

    private fun checkLocation() {
        if (location.isBlank()) {
            locationErr = "Location cannot be blank"
        } else locationErr = ""
    }

    val phoneNumber: String
        get() = userData.value?.phoneNumber.orEmpty()

    var phoneNumberErr by mutableStateOf(value = "")
        private set
    fun setPhoneNumberValue(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(phoneNumber = v)
            _userData.value = updatedUser
        }
    }

    private fun checkPhoneNumber() {
        if (phoneNumber.isBlank()) {
            phoneNumberErr = "Phone number cannot be blank"
        } else if (!phoneNumber.matches(Regex("^\\+?[0-9\\-\\s]{9,15}\$"))) {
            phoneNumberErr = "Invalid phone number format"
        } else if (phoneNumber.replace(Regex("[^0-9]"), "").length != 10) {
            phoneNumberErr = "Phone number must have exactly 10 digits"
        } else {
            phoneNumberErr = ""
        }
    }

    val description: String
        get() = userData.value?.description.orEmpty()

    fun setDescriptionValue(v: String) {
        val currentUser = userData.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(description = v)
            _userData.value = updatedUser
        }
    }

    //var kpi = user.kpi
    //    private set
    var kpiErr by mutableStateOf(value = "")
        private set

    fun setKpiValue(v: Double) {
        //user.setKpi(v)
    }

    fun getDirectChat(userId: (String)): String?{
        //return model.getDirectChat(userId)
        return null
    }


    private val _filters = MutableStateFlow(TaskFilters())
    val filters: StateFlow<TaskFilters> get() = _filters

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<Map<String, List<TaskData>>> = _filters
        .flatMapLatest {
            withContext(Dispatchers.IO) {
                taskRepository.getFilteredTasks(TaskFilters(assignees = listOf(userMe.value!!.id)))
            }        }
        .stateIn(viewModelScope, SharingStarted.Lazily, sortedMapOf())


}
