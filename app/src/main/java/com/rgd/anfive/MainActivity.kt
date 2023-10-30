package com.rgd.anfive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rgd.anfive.ui.theme.AnFiveTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnFiveTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Container()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Container() {
    val controller = rememberNavController()

    Scaffold(
        bottomBar = { NavBar(controller = controller) },
        content = {padding ->
            Column(
                modifier = Modifier.padding(padding)
            ) {
                val intervalState = rememberPickerState()
                NavHost(navController = controller, startDestination = "home") {
                    composable("home") { HomeScreen(
                        state = intervalState,
                        onStateChange = { intervalState.selectedItem = it }
                    ) }
                    composable("timer") { ReadyGo(state = intervalState) }
                }
            }
        }
    )
}


sealed class NavigationItem(var route: String, val icon: ImageVector?, var title: String) {
    object Home : NavigationItem("home", Icons.Rounded.Home, "Home")
    object Timer : NavigationItem("timer", Icons.Rounded.PlayArrow, "Timer")
}

@Composable
fun HomeScreen(state: PickerState, onStateChange: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val values = remember { (5..59).map { it.toString() } }
//        val valuesPickerState = rememberPickerState()
        Text(text = "Example Picker", modifier = Modifier.padding(top = 16.dp))
        Picker(
//            state = valuesPickerState,
            onStateChange = onStateChange,
            items = values,
            visibleItemsCount = 3,
            modifier = Modifier.weight(0.3f),
            textModifier = Modifier.padding(8.dp),
            textStyle = TextStyle(fontSize = 32.sp)
        )
        Text(
            text = """
                You have chosen an interval of: ${state.selectedItem}.
                When you're ready to Take Five, 
                tap on the "Timer" tab!
                """,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun rememberPickerState() = remember { PickerState() }

class PickerState {
    var selectedItem by mutableStateOf("")
}

@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Picker(
    modifier: Modifier = Modifier,
    items: List<String>,
//    state: PickerState = rememberPickerState(),
    onStateChange: (String) -> Unit,
    startIndex: Int = 0,
    visibleItemsCount: Int = 3,
    textModifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    dividerColor: Color = LocalContentColor.current,
) {

    val visibleItemsMiddle = visibleItemsCount / 2
    val listScrollCount = Integer.MAX_VALUE
    val listScrollMiddle = listScrollCount / 2
    val listStartIndex = listScrollMiddle - listScrollMiddle % items.size - visibleItemsMiddle + startIndex

    fun getItem(index: Int) = items[index % items.size]

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val itemHeightPixels = remember { mutableIntStateOf(0) }
    val itemHeightDp = pixelsToDp(itemHeightPixels.intValue)

    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> getItem(index + visibleItemsMiddle) }
            .distinctUntilChanged()
            .collect { item -> onStateChange(item) }
    }

    Box(modifier = modifier) {

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp * visibleItemsCount)
                .fadingEdge(fadingEdgeGradient)
        ) {
            items(listScrollCount) { index ->
                Text(
                    text = getItem(index),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                    modifier = Modifier
                        .onSizeChanged { size -> itemHeightPixels.intValue = size.height }
                        .then(textModifier)
                )
            }
        }

        Divider(
            color = dividerColor,
            modifier = Modifier.offset(y = itemHeightDp * visibleItemsMiddle)
        )

        Divider(
            color = dividerColor,
            modifier = Modifier.offset(y = itemHeightDp * (visibleItemsMiddle + 1))
        )

    }

}

@Composable
fun NavBar(controller: NavController) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        NavigationItem.Home,
        NavigationItem.Timer
    )
    var currentRoute by remember { mutableStateOf(NavigationItem.Home.route) }

    tabs.forEachIndexed { index, navigationItem ->
        if (navigationItem.route == currentRoute) {
            selectedItem = index
        }
    }

    NavigationBar {
        tabs.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                label = {
                        Text(item.title)
                },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    currentRoute = item.route
                    controller.navigate(currentRoute)
                },
                icon = {
                    Icon(item.icon!!, contentDescription = item.title)
                })
        }
    }
}

@Composable
fun ReadyGo(state: PickerState) {
    var ready by remember { mutableStateOf(false) }
    val start = state.selectedItem.toInt()

    if(ready) {
        ShowTimer(start = start)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { ready = true },
            modifier = Modifier.alpha(
                if (ready) 0f else 1f
            )
        ) {
            Text("Ready?")
        }
    }
}

@Composable
fun ShowTimer(start: Int) {
    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(key1 = true) {
        // need get the state from the picker somehow
        loadProgress(target = start) { progress ->
            currentProgress = progress
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Timer(target = start)
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
    var ticks by remember { mutableIntStateOf(target) }

    LaunchedEffect(key1 = true) {
        while (ticks > 0) {
            delay(1000L)
            ticks--
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

    }
}