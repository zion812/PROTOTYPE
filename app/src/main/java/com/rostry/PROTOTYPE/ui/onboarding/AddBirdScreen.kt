package com.rostry.prototype.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rostry.prototype.R

private val BIRD_TYPES = listOf("Broiler", "Layer", "Country")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirdScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var birdTypeExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.updatePhotoUri(it) }
    }

    LaunchedEffect(state.birdSaved) {
        if (state.birdSaved) {
            snackbarHostState.showSnackbar("Bird saved offline \u2713")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                text = "Add Bird",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Register a new bird to your farm",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.birdName,
                onValueChange = viewModel::updateBirdName,
                label = { Text("Bird Name / Batch Label") },
                singleLine = true,
                isError = state.birdNameError != null,
                supportingText = state.birdNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.breed,
                onValueChange = viewModel::updateBreed,
                label = { Text("Breed") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = birdTypeExpanded,
                onExpandedChange = { birdTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.birdType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bird Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = birdTypeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = MaterialTheme.shapes.small
                )
                ExposedDropdownMenu(
                    expanded = birdTypeExpanded,
                    onDismissRequest = { birdTypeExpanded = false }
                ) {
                    BIRD_TYPES.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.updateBirdType(type)
                                birdTypeExpanded = false
                            },
                            trailingIcon = if (state.birdType == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Photo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.photoUri != null && state.photoUri != Uri.EMPTY) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = state.photoUri,
                        contentDescription = "Bird photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_bird_placeholder)
                    )

                    IconButton(
                        onClick = { viewModel.updatePhotoUri(Uri.EMPTY) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove photo",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Photo")
                }
            } else {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap to add photo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.saveBird {} },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "Save Bird",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (state.birdSaved) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateToDashboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        "Go to Dashboard",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.resetBirdSaved()
                        viewModel.updateBirdName("")
                        viewModel.updateBreed("")
                        viewModel.updateBirdType("")
                        viewModel.updatePhotoUri(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Add Another Bird")
                }
            }

            if (state.saveError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.saveError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
