package com.example.p10_music_app

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.p10_music_app.ui.theme.P10_music_appTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            P10_music_appTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicScreen()
                }
            }
        }
    }
}

val db = FirebaseFirestore.getInstance()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var musicList by remember { mutableStateOf<List<MusicInfo>>(emptyList()) }
    var refreshList by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = refreshList) {
        fetchMusicData { updatedMusicList ->
            musicList = updatedMusicList
            refreshList = !refreshList // Toggle the refresh key to trigger animation
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // App bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Blue,
            content = {
                TopAppBar(
                    title = { Text("Firebase - Music App", color = Color.Black) }, // Set the title color
                )
            }
        )

        // List of music cards
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(musicList) { musicInfo ->
                AnimatedVisibility(
                    visible = musicInfo in musicList,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    MusicCard(musicInfo = musicInfo)
                }
            }
        }

        // Plus button
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.End)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }

        // Modal dialog
        if (showDialog) {
            MusicDialog(
                onDismiss = { showDialog = false },
                onMusicInfoEntered = { info ->
                    // Save music info to Firestore
                    saveMusicInfo(info)
                    refreshList= !refreshList
                    showDialog = false
                }
            )
        }
    }
}


@Composable
fun MusicDialog(
    onDismiss: () -> Unit,
    onMusicInfoEntered: (MusicInfo) -> Unit
) {
    var musicName by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var movieName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        content = {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.White)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = musicName,
                        onValueChange = { musicName = it },
                        label = { Text("Music Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = genre,
                        onValueChange = { genre = it },
                        label = { Text("Genre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = movieName,
                        onValueChange = { movieName = it },
                        label = { Text("Movie Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onMusicInfoEntered(
                                    MusicInfo(
                                        musicName = musicName,
                                        genre = genre,
                                        movieName = movieName,
                                        duration = duration
                                    )
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun MusicCard(musicInfo: MusicInfo) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Music Name: ${musicInfo.musicName}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Genre: ${musicInfo.genre}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Movie Name: ${musicInfo.movieName}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Duration: ${musicInfo.duration}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {  }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { }, modifier = Modifier.background(Color(0xFFD22B2B)).clip(
                    RoundedCornerShape(16.dp)
                )) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }
}

data class MusicInfo(
    val musicName: String,
    val genre: String,
    val movieName: String,
    val duration: String
)

suspend fun fetchMusicData(callback: (List<MusicInfo>) -> Unit) {
    try {
        val querySnapshot = db.collection("Music").get().await()

        val musicList = mutableListOf<MusicInfo>()

        for (document in querySnapshot.documents) {
            val musicName = document.getString("musicName") ?: ""
            val genre = document.getString("genre") ?: ""
            val movieName = document.getString("movieName") ?: ""
            val duration = document.getString("duration") ?: ""
            musicList.add(MusicInfo(musicName, genre, movieName, duration))
        }

        // Update the musicList state variable
        callback(musicList)
    } catch (e: Exception) {
        Log.w(TAG, "Error getting documents.", e)
    }
}

fun saveMusicInfo(info: MusicInfo) {
    // Convert MusicInfo object to a map
    val musicInfoMap = mapOf(
        "musicName" to info.musicName,
        "genre" to info.genre,
        "movieName" to info.movieName,
        "duration" to info.duration
    )

    // Add the music info map to Firestore
    db.collection("Music")
        .add(musicInfoMap)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
        }
}
