package it.polito.mad.g18.mad_lab5.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CameraViewModel @Inject constructor(
    val model: MainModel,
    val userRepository: UserRepository,
    val teamRepository: TeamRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {

    val userMeId = userMe.value?.id ?: "ERROR"

    var tempPhoto: MutableState<Bitmap?> = mutableStateOf(null)

    var pictureTaken by mutableStateOf(false)

    private fun setPictureTakenValue(b: Boolean) { pictureTaken = b }

    fun takePhoto(
        ctx: Context,
        controller: LifecycleCameraController
    ) {
        model.takePhoto(ctx, controller, ::setTempPhotoValue)
    }

    private fun setTempPhotoValue(v: Bitmap?) {
        tempPhoto.value = v//model.setTempPhoto(v)
        setPictureTakenValue(true)
    }

    fun goBackAndCancel() {
        model.setTempPhoto(null)
        setPictureTakenValue(false)
    }

    fun saveUserPfp(pfp: Bitmap?, teamId: String, isTeam: Boolean, goBack: () -> Unit) {
        if (pfp != null) {
            if (!isTeam) {
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.setUserPfp(userMeId, pfp)
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    teamRepository.setTeamPfp(teamId, pfp)
                }
            }
            goBack()
        }
    }
}
