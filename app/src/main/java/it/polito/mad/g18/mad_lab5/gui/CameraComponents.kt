package it.polito.mad.g18.mad_lab5.gui

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import it.polito.mad.g18.mad_lab5.viewModels.CameraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    vm: CameraViewModel = hiltViewModel(),
    id: String,
    isTeam: Boolean,
    goBack: () -> Unit
) {

    //states
    val tempPhoto by vm.tempPhoto
    val pictureTaken = vm.pictureTaken

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = { 
            TopAppBar(
                title = {},
                navigationIcon = {
                    if (pictureTaken) {
                        IconButton(
                            onClick = { vm.goBackAndCancel() }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back")
                        }
                    }
                },
                actions = {
                    if (pictureTaken) {
                        IconButton(
                            onClick = { vm.saveUserPfp(tempPhoto, id, isTeam, goBack)  }
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = "save")
                        }
                    }
                }
            ) 
        }

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (!pictureTaken) {
                CameraPane(
                    takePhoto = vm::takePhoto,
                    goBack = goBack
                )
            } else {
                SavePicturePane(tempPhoto, teamId = id, isTeam = isTeam, vm::saveUserPfp, goBack)
            }
        }
    }
}

@Composable
fun CameraPane(
    takePhoto: (Context, LifecycleCameraController) -> Unit,
    goBack: () -> Unit
) {

    val ctx = LocalContext.current
    val controller = remember {
        LifecycleCameraController(ctx).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }
    Box (
        modifier = Modifier
            .fillMaxSize()
    ) {


        CameraPreview(
            controller = controller,
            modifier = Modifier.fillMaxSize()
        )

        // go back
        CameraPreviewButton(
            modifier = Modifier
                .offset(16.dp, 16.dp),
            imageVector = Icons.Default.Close,
            contentDescription = "go back",
            onClick = { goBack() }
        )

        // switch cameras
        CameraPreviewButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(-16.dp, 16.dp),
            imageVector = Icons.Default.ChangeCircle,
            contentDescription = "switch camera",
            onClick = {
                controller.cameraSelector =
                    if(controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    else
                        CameraSelector.DEFAULT_BACK_CAMERA
            }
        )

        // take picture
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .windowInsetsPadding(BottomAppBarDefaults.windowInsets)
            .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center
        ) {

            //take picture button
            CameraPreviewButton(
                modifier = Modifier,
                imageVector = Icons.Default.Camera,
                contentDescription = "take photo",
                onClick =  {
                    takePhoto(ctx ,controller)
                }
            )
        }
    }
}

@Composable
fun SavePicturePane(
    tempPhoto: Bitmap?,
    teamId: String,
    isTeam: Boolean,
    savePicture: (Bitmap?, String, Boolean,(() -> Unit)) -> Unit,
    goBack: () -> Unit
) {
    val blankImage = ImageBitmap(
        width = 20,
        height = 20,
        config = ImageBitmapConfig.Argb8888,
        hasAlpha = true,
        colorSpace = ColorSpaces.Srgb
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            bitmap = tempPhoto?.asImageBitmap() ?: blankImage,
            contentDescription = "taken picture preview"
        )
        Row(
            modifier = Modifier
                .weight(0.25f)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.77f)
                    .fillMaxHeight(0.45f),
                onClick = { savePicture(tempPhoto, teamId, isTeam, goBack) }
            ) {
                Text(text = "Save picture")
            }
        }

    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    //serve alla previewView per poter effettuare la composizione della Camera in maniera corretta
    val lifeCycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifeCycleOwner)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CameraPreviewButton(
    modifier: Modifier,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val backGroundColor = Color.LightGray//MaterialTheme.colorScheme.background
    IconButton(
        onClick = onClick,
        modifier = modifier
            .scale(1.5f)
            .drawBehind {
                drawCircle(
                    color = backGroundColor,
                    radius = this.size.height / 4.0f
                )
            }
    ) {
        Icon(imageVector = imageVector, contentDescription = contentDescription)
    }
}