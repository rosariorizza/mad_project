package it.polito.mad.g18.mad_lab5.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImagePainter
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UserRegistrationViewModel @Inject constructor(
    private val model: MainModel,
    private val userRepository: UserRepository
) : ViewModel() {
    // Initial user data for registration
    var name by mutableStateOf("")
    var nameErr by mutableStateOf("")

    var surname by mutableStateOf("")
    var surnameErr by mutableStateOf("")

    var userName by mutableStateOf("")
    var userNameErr by mutableStateOf("")

    var email by mutableStateOf("")
    var emailErr by mutableStateOf("")

    var birthDate : MutableState<LocalDate?> =
        mutableStateOf(null)
        private set
    var birthDateErr by mutableStateOf("")

    var location by mutableStateOf("")
    var locationErr by mutableStateOf("")

    var phoneNumber by mutableStateOf("")
    var phoneNumberErr by mutableStateOf("")

    var description by mutableStateOf("")

    var photo by mutableStateOf<Bitmap?>(null)
    var photoErr by mutableStateOf("")

    // Utility methods
    fun validate(): Boolean {
        checkFirstName()
        checkLastName()
        //checkEmail()
        checkLocation()
        checkPhoneNumber()
        checkBirthDate()

        return nameErr.isBlank() && surnameErr.isBlank() && userNameErr.isBlank()
                && locationErr.isBlank() && phoneNumberErr.isBlank() &&
                birthDateErr.isBlank()
    }

    private fun checkFirstName() {
        nameErr = if (name.isBlank()) "First name cannot be blank" else ""
    }

    private fun checkLastName() {
        surnameErr = if (surname.isBlank()) "Last name cannot be blank" else ""
    }

    fun setUserNameError(v: String) {
        userNameErr = v
    }

    /*private fun checkUserName() {
        if (userName.isBlank()) {
            userNameErr = "Username cannot be blank"
        } else if (!isUsernameTakenPrivate(userName)) {
            userNameErr = "Username is already taken"
        } else userNameErr = ""
    }*/


    private fun checkEmail() {
        emailErr = if (email.isBlank()) {
            "Email cannot be blank"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email format"
        } else {
            ""
        }
    }

    private fun checkLocation() {
        locationErr = if (location.isBlank()) "Location cannot be blank" else ""
    }

    private fun checkPhoneNumber() {
        phoneNumberErr = if (phoneNumber.isBlank()) {
            "Phone number cannot be blank"
        } else if (!phoneNumber.matches(Regex("^\\+?[0-9\\-\\s]{9,15}\$"))) {
            "Invalid phone number format"
        } else {
            ""
        }
    }

    private fun checkBirthDate() {
        birthDateErr = if (birthDate == null) "Birth Date cannot be blank" else ""
    }

    val user = model.userDisplayed
    fun setPhotoValue(v: String?) {
        user.setProfilePicture(v)
        // TODO camera implementation
        //need to check how to implement better
        photo = null //v
    }

    var tempGalleryPhoto: AsyncImagePainter? by mutableStateOf(null)
    fun setTempGalleryPhotoValue(v: AsyncImagePainter) {
        tempGalleryPhoto = v
    }

    val tempPhoto = model.tempPhoto
    fun setTempPhoto(v: Bitmap?) { model.setTempPhoto(v) }

    fun setNameValue(v: String) {
        name = v
    }

    fun setSurnameValue(v: String) {
        surname = v
    }

    fun setUserNameValue(v: String) {
        userName = v
    }

    fun setEmailValue(v: String) {
        email = v
    }

    fun setBirthDateValue(v: LocalDate?) {
        birthDate.value = v
    }

    fun setLocationValue(v: String) {
        location = v
    }

    fun setPhoneNumberValue(v: String) {
        phoneNumber = v
    }

    fun setDescriptionValue(v: String) {
        description = v
    }

    fun setKpiValue(v: Double) {
        // In registration, KPI might not be relevant initially
    }


    fun checkIfUserRegistered(userId: String?, onTrue: () -> Unit, onFalse: () -> Unit) {
        if(userId != null)
            userRepository.checkIfUserRegistered(userId, onTrue, onFalse)
        else onFalse()
    }

    suspend fun isUsernameTaken(username: String) = userRepository.isUsernameTaken(username)
    suspend fun register(onSuccess: ()-> Unit){
        if(validate()) {
            userRepository.register(
                UserData(
                    name = name,
                    surname = surname,
                    userName = userName,
                    birthDate = birthDate.value,
                    location = location,
                    phoneNumber = phoneNumber,
                    description = description,
                )
            ).onSuccess { onSuccess() }
        }
    }

}