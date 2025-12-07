package com.vrsabu.markme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.coffee
import com.vrsabu.markme.ui.theme.MarkMeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Hold the list of coffee in Compose state
    val coffeesState = remember { mutableStateOf<List<coffee>>(emptyList()) }

    // Launch a coroutine in the composition to fetch data once
    LaunchedEffect(Unit) {
        try {
            val list = withContext(Dispatchers.IO) {
                // ApiService.getCoffeeList() is a synchronous call in this project
                RetrofitInstance.api.getCoffeeList()
            }
            coffeesState.value = list
        } catch (e: Exception) {
            // keep it simple for now: print the stacktrace and leave list empty
            e.printStackTrace()
        }
    }

    // Header + content
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Hello $name")

        if (coffeesState.value.isEmpty()) {
            Text(
                text = "Loading...",
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(coffeesState.value) { item ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(text = item.title)
                        Text(text = item.description)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MarkMeTheme {
        Greeting("Android")
    }
}