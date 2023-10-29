package com.rgd.anfive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rgd.anfive.ui.theme.AnFiveTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnFiveTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CircularDeterminateIndicator()
                }
            }
        }
    }
}

@Composable
fun CircularDeterminateIndicator() {
    var currentProgress by remember { mutableStateOf(0f) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Create a coroutine scope

    LaunchedEffect(key1 = true, block = {
        loading = true
        scope.launch {
            loadProgress(target = 5) { progress ->
                currentProgress = progress
            }
            loading = false // Reset loading when the coroutine finishes
        }
    })

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Timer(target = 5)
        CircularProgressIndicator(
            progress = currentProgress,
            modifier = Modifier
                .size(200.dp),
            strokeWidth = 10.dp
        )
    }
}

@Composable
fun Timer(target: Int) {
    var ticks by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        while (ticks < target) {
            delay(1000L)
            ticks++
        }
    }
    Text("$ticks")
}

/** Iterate the progress value */
suspend fun loadProgress(target: Int, updateProgress: (Float) -> Unit) {
    val inMs = target * 100
    for (i in 1..inMs) {
        updateProgress(i.toFloat() / inMs)
        delay(10L)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AnFiveTheme {
        CircularDeterminateIndicator()
    }
}