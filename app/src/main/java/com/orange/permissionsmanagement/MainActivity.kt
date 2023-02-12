package com.orange.permissionsmanagement

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.orange.permissionsmanagement.ui.theme.PermissionsManagementTheme


class MainActivity : ComponentActivity() {
    private val permissionsToRequest = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.RECORD_AUDIO

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionsManagementTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val viewModel = viewModel<MainViewModel>()
                    val dialogQueue = viewModel.visiblePermissionDialogQueue

                    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            viewModel.onPermissionResult(
                                permission = Manifest.permission.CAMERA,
                                isGranted = isGranted
                            )
                        }
                    )
                    val multiplePermissionResultLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions(),
                        onResult = { perms ->
                            permissionsToRequest.forEach { permission ->
                                viewModel.onPermissionResult(
                                    permission = permission,
                                    isGranted = perms[permission] == true
                                )
                            }
                        }
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            cameraPermissionResultLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }) {
                            Text(text = "Request one permission")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            multiplePermissionResultLauncher.launch(
                                permissionsToRequest
                            )
                        }) {
                            Text(text = "Request multiple permission")
                        }
                        dialogQueue
                            .reversed()
                            .forEach { permission ->
                                PermissionDialog(
                                    permissionTextProvider = when (permission) {
                                        Manifest.permission.CAMERA -> CameraPermissionTextProvider()
                                        Manifest.permission.RECORD_AUDIO -> RecordAudioPermissionTextProvider()
                                        Manifest.permission.CALL_PHONE -> CallPhonePermissionTextProvider()
                                        else -> return@forEach
                                    },
                                    isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                                        permission
                                    ),
                                    onDismiss = { viewModel.dismissDialog() },
                                    onOkClick = {
                                        viewModel.dismissDialog()
                                        multiplePermissionResultLauncher.launch(
                                            arrayOf(permission)
                                        )


                                    },
                                    onGoToAppSettingsClick = ::openAppSettings
                                )
                            }
                    }

                }
            }
        }
    }
}


fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}