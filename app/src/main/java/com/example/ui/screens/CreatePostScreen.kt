package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ARTMUSEUM_TOPICS
import com.example.data.model.ArtmuseumThemePalette
import com.example.data.model.PRESET_ARTWORKS
import com.example.ui.components.ArtmuseumArtworkImage
import com.example.ui.components.ArtmuseumThemeSwatch
import com.example.ui.components.ArtmuseumTopicChip
import com.example.ui.theme.ArtmuseumShapeTokens
import com.example.ui.theme.LocalArtmuseumColors
import com.example.ui.theme.LocalArtmuseumDimensions
import com.example.ui.theme.OlivePillActive
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.WarmPaper
import com.example.ui.viewmodel.ArtmuseumViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialTheme: ArtmuseumThemePalette,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onPublish: (
        text: String,
        imageURL: String,
        imageCaption: String,
        topics: List<String>,
        themePalette: ArtmuseumThemePalette,
        aspectRatio: String
    ) -> Unit,
    onThemeChanged: (ArtmuseumThemePalette) -> Unit,
    onShowMessage: (String) -> Unit
) {
    BackHandler(onBack = onBack)

    val colors = LocalArtmuseumColors.current
    val dimens = LocalArtmuseumDimensions.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageSource by remember { mutableStateOf("preset:alpine_valley") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var selectedImageSize by remember { mutableStateOf("standard") }
    var captionText by remember { mutableStateOf("") }
    var selectedTopics by remember { mutableStateOf(listOf("Life")) }
    var availableTopics by remember { mutableStateOf(ARTMUSEUM_TOPICS) }
    var selectedPalette by remember { mutableStateOf(initialTheme) }
    var isEncodingImage by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }
    var showPresetGallerySheet by remember { mutableStateOf(false) }

    // Android Photo Picker (Zero storage permissions required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isEncodingImage = true
            scope.launch {
                val encoded = ArtmuseumViewModel.encodeUriToCompactDataUri(context, uri)
                isEncodingImage = false
                if (encoded != null) {
                    selectedImageSource = encoded
                } else {
                    onShowMessage("Selected photo could not be processed; using gallery canvas.")
                }
            }
        }
    }

    // Camera capture preview launcher
    val cameraTakePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isEncodingImage = true
            scope.launch {
                val encoded = ArtmuseumViewModel.encodeBitmapToCompactDataUri(bitmap)
                isEncodingImage = false
                if (encoded != null) {
                    selectedImageSource = encoded
                }
            }
        }
    }

    // Contextual CAMERA runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraTakePictureLauncher.launch(null)
        } else {
            onShowMessage("Camera permission was declined. You can still select artwork from Gallery.")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Top Bar (Matches Screen 4 of mockup: < back icon, "Create Post" serif title, olive "Post" pill button)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = colors.inkPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack)
                        .padding(4.dp)
                        .testTag("create_back_button")
                )

                Text(
                    text = "Create Post",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = colors.inkPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )

                Button(
                    onClick = {
                        onPublish(
                            captionText.trim(),
                            selectedImageSource,
                            "",
                            selectedTopics,
                            selectedPalette,
                            if (selectedImageSize == "standard") selectedAspectRatio else "${selectedAspectRatio}_${selectedImageSize}"
                        )
                    },
                    enabled = !isSubmitting && !isEncodingImage,
                    shape = ArtmuseumShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.pillSelectedFill,
                        contentColor = colors.pillSelectedText
                    ),
                    contentPadding = PaddingValues(horizontal = if (dimens.isCompact) 16.dp else 22.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(if (dimens.isCompact) 34.dp else 38.dp)
                        .testTag("publish_post_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.pillSelectedText,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Post",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (dimens.isCompact) 13.sp else 14.sp
                            ),
                            color = colors.pillSelectedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (dimens.isCompact) 10.dp else 16.dp))

            // Rounded Artwork Preview Card with circular X button at top-right
            if (selectedImageSource.isNotBlank()) {
                val ratioFloat = when (selectedAspectRatio) {
                    "4:5" -> 4f / 5f
                    "16:9" -> 16f / 9f
                    "3:4" -> 3f / 4f
                    else -> 1f
                }
                val previewMaxHeight = when (selectedImageSize) {
                    "compact" -> 175.dp
                    "large" -> 320.dp
                    else -> if (dimens.isCompact) 230.dp else 260.dp
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = previewMaxHeight)
                        .aspectRatio(ratioFloat)
                        .clip(ArtmuseumShapeTokens.Card)
                ) {
                    ArtmuseumArtworkImage(
                        imageSource = selectedImageSource,
                        contentDescription = "Selected artwork preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showPresetGallerySheet = true },
                        contentScale = ContentScale.Crop
                    )

                    if (isEncodingImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = WarmPaper)
                        }
                    }

                    // Circular X button in top right corner
                    Surface(
                        onClick = { selectedImageSource = "" },
                        shape = CircleShape,
                        color = Color(0xFF2B2723).copy(alpha = 0.62f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(28.dp)
                            .testTag("remove_artwork_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove image",
                                tint = WarmPaper,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Aspect Ratio Selector Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("1:1" to "Square 1:1", "4:5" to "Portrait 4:5", "16:9" to "Cinema 16:9", "3:4" to "Classic 3:4").forEach { (key, label) ->
                        val isSelected = selectedAspectRatio == key
                        Surface(
                            onClick = { selectedAspectRatio = key },
                            shape = ArtmuseumShapeTokens.Pill,
                            color = if (isSelected) colors.pillSelectedFill else colors.subtleSurface,
                            border = BorderStroke(0.8.dp, if (isSelected) colors.pillSelectedFill else colors.border),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colors.pillSelectedText else colors.inkPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Image Display Size Selector Chips
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Size:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.mutedText
                    )
                    listOf("compact" to "Compact", "standard" to "Standard", "large" to "Full Frame").forEach { (key, label) ->
                        val isSelected = selectedImageSize == key
                        Surface(
                            onClick = { selectedImageSize = key },
                            shape = ArtmuseumShapeTokens.Pill,
                            color = if (isSelected) colors.pillSelectedFill else colors.subtleSurface,
                            border = BorderStroke(0.8.dp, if (isSelected) colors.pillSelectedFill else colors.border),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) colors.pillSelectedText else colors.inkPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    onClick = { showPresetGallerySheet = true },
                    shape = ArtmuseumShapeTokens.Card,
                    color = colors.subtleSurface,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = colors.mutedText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Text-only exhibit · Tap to attach museum artwork",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Pill Buttons: [🖼️ Gallery] and [📷 Camera] (Matches Screen 4 of mockup)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = ArtmuseumShapeTokens.Pill,
                    color = colors.subtleSurface,
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("create_gallery_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Gallery",
                            tint = colors.inkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gallery",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.inkPrimary
                        )
                    }
                }

                Surface(
                    onClick = {
                        val hasCamPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCamPerm) {
                            cameraTakePictureLauncher.launch(null)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = ArtmuseumShapeTokens.Pill,
                    color = colors.subtleSurface,
                    border = BorderStroke(0.8.dp, colors.border),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("create_camera_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Camera",
                            tint = colors.inkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Camera",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colors.inkPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rounded Rectangle Caption / Thought Editor ("Add a caption..." + "0/2000" counter)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.subtleSurface,
                border = BorderStroke(0.8.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    if (captionText.isEmpty()) {
                        Text(
                            text = "Add a caption...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.mutedText
                        )
                    }
                    BasicTextField(
                        value = captionText,
                        onValueChange = { if (it.length <= 2000) captionText = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.inkPrimary,
                            fontSize = 14.5.sp
                        ),
                        cursorBrush = SolidColor(colors.pillSelectedFill),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("create_caption_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${captionText.length}/2000",
                style = MaterialTheme.typography.labelSmall,
                color = colors.mutedText,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add Tags (optional)
            Text(
                text = "Add Tags (optional)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp
                ),
                color = colors.inkPrimary.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableTopics) { topic ->
                    val isSelected = topic in selectedTopics
                    ArtmuseumTopicChip(
                        label = topic,
                        selected = isSelected,
                        onClick = {
                            selectedTopics = if (isSelected) {
                                selectedTopics - topic
                            } else {
                                (selectedTopics + topic).take(10)
                            }
                        },
                        testTag = "topic_tag_${topic.lowercase(Locale.ROOT)}"
                    )
                }

                item {
                    Surface(
                        onClick = { showAddTagDialog = true },
                        shape = CircleShape,
                        color = colors.subtleSurface,
                        border = BorderStroke(0.8.dp, colors.border),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("add_custom_tag_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add custom tag",
                                tint = colors.inkPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Choose Theme Panel (Matches Screen 4 bottom panel of mockup)
            Surface(
                shape = ArtmuseumShapeTokens.Card,
                color = colors.cardSurface,
                border = BorderStroke(0.8.dp, colors.border.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPresetGallerySheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = colors.inkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose Theme",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.5.sp
                            ),
                            color = colors.inkPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Curated paintings",
                            tint = colors.mutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ArtmuseumThemePalette.entries) { palette ->
                            ArtmuseumThemeSwatch(
                                palette = palette,
                                selected = palette == selectedPalette,
                                onClick = {
                                    selectedPalette = palette
                                    onThemeChanged(palette)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            containerColor = colors.cardSurface,
            title = { Text("Add Topic Tag") },
            text = {
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it.take(24) },
                    label = { Text("Topic name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleaned = customTagInput.trim()
                        if (cleaned.isNotEmpty()) {
                            if (cleaned !in availableTopics) {
                                availableTopics = availableTopics + cleaned
                            }
                            if (cleaned !in selectedTopics) {
                                selectedTopics = (selectedTopics + cleaned).take(10)
                            }
                        }
                        customTagInput = ""
                        showAddTagDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPresetGallerySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetGallerySheet = false },
            containerColor = colors.cardSurface,
            shape = ArtmuseumShapeTokens.LargeSheetTop
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Curated Museum Paintings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.inkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose from the Artmuseum oil collection or pick from your device gallery.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedText
                )
                Spacer(modifier = Modifier.height(14.dp))
                PRESET_ARTWORKS.forEach { preset ->
                    Surface(
                        onClick = {
                            selectedImageSource = preset.key
                            if (captionText.isBlank()) {
                                captionText = preset.defaultCaption
                            }
                            showPresetGallerySheet = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = colors.subtleSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            ArtmuseumArtworkImage(
                                imageSource = preset.key,
                                contentDescription = preset.title,
                                modifier = Modifier
                                    .size(64.dp, 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.inkPrimary
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.mutedText
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
