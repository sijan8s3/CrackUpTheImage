package com.dsizon.crackuptheimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val uiState by bakingViewModel.uiState.collectAsState()
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        selectedImageBitmap = it
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(uri)
            selectedImageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.baking_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Upload a photo to crack yourself up with a funny joke! \uD83D\uDE02",
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,

        )

        selectedImageBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.CenterHorizontally)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary))
                    .padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Choose an image from:",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Button(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Camera")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Gallery")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedImageBitmap?.let { bitmap ->
                    bakingViewModel.sendPrompt(bitmap)
                }
            },
            enabled = selectedImageBitmap != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = stringResource(R.string.action_go))
        }

        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Success -> {
                Text(
                    text = (uiState as UiState.Success).outputText,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
            is UiState.Error -> {
                Text(
                    text = (uiState as UiState.Error).errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            else -> {}
        }
    }
}
