package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.window.Dialog
import kotlin.concurrent.timer



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TimerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TimerApp() {
    var funny by remember { mutableStateOf( 0 ) }
    var timerNames = rememberSaveable { mutableMapOf<Int, String>() }
    var originalTimes = rememberSaveable { mutableMapOf<Int, Long>() }
    val elapsedTime = remember { mutableMapOf<Int, Long>() }
    var running by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var timerCount by remember { mutableStateOf(0) }
    var newTimer by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun updateTime(newTime: Long, timer: Int){
        elapsedTime[timer] = newTime
    }

    fun updateRunning(timer: Int){
        running = running.toMutableMap().apply {
            this[timer] = !this[timer]!!
        }
    }

    fun addTimer(length: Long, name: String){
        originalTimes[timerCount] = length
        elapsedTime[timerCount] = length
        running = running.toMutableMap().apply {
            this[timerCount] = true
        }
        timerNames[timerCount] = name
        timerCount++
        newTimer = false
    }

    fun removeTimer(key: Int){
        originalTimes.remove(key)
        elapsedTime.remove(key)
        running = running.toMutableMap().apply {
            this.remove(key)
        }
        timerCount--
    }


    if(newTimer){
        NewTimer(onDismissRequest = {newTimer = false}, onConfirmation = ::addTimer)
    }

    if(funny > 7) {
        Dialog(
            onDismissRequest = {funny = 0}
        ) {
            Card(
                modifier = Modifier.padding(10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Image(painter = painterResource(R.drawable.image), "A picture", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = {funny = 0}) {
                        Text("Ok")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(text = "Timer App", modifier = Modifier.clickable() { funny++ })
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(10.dp),
                onClick = { newTimer = true}) {
                Icon(painter = painterResource(R.drawable.more_time), contentDescription = "New Timer")
            }
        },

        floatingActionButtonPosition = FabPosition.End

    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(
                rememberScrollState()
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            originalTimes.forEach { (key, value) ->
                TimerCard(
                    key = key,
                    originalTime = value,
                    currentTime = elapsedTime[key]!!,
                    running = running[key]!!,
                    name = timerNames[key]!!,
                    updateTime = ::updateTime,
                    updateRunning = ::updateRunning,
                    deleteTimer = ::removeTimer
                )
            }
        }
    }



}

@Composable
fun TimerCard(key : Int, originalTime : Long, currentTime : Long, running : Boolean, name : String, updateTime : (Long, Int) -> Unit, updateRunning : (Int) -> Unit, deleteTimer : (Int) -> Unit) {
    var elapsedTime by remember { mutableStateOf(currentTime) }
    var isRunning by remember { mutableStateOf(running) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if(isRunning) {
                elapsedTime--
                updateTime(elapsedTime, key)
            }
        }
    }
    val color : CardColors = if(elapsedTime < 0) CardColors(
        containerColor = CardDefaults.cardColors().disabledContentColor,
        contentColor = CardDefaults.cardColors().contentColor,
        disabledContainerColor = CardDefaults.cardColors().disabledContainerColor,
        disabledContentColor = CardDefaults.cardColors().disabledContentColor
    )
    else CardDefaults.cardColors()
    Card(
        modifier = Modifier.padding(10.dp),
        colors = color,

        shape = RoundedCornerShape(20.dp),
        )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(name != "")
                Text(text = name, fontSize = 30.sp, modifier = Modifier.padding(top = 10.dp), textAlign = TextAlign.Center)
            Text(text = formatTime(elapsedTime), fontSize = 40.sp, modifier = Modifier.padding(16.dp))

            Row(
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Button(onClick = {
                    updateRunning(key)
                    isRunning = !isRunning
                }) {
                    if(running)
                        Text("Pause")
                    else
                        Text("Resume")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = {
                    elapsedTime = originalTime
                }) {
                    Text("Reset")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = {
                    deleteTimer(key)
                }) {
                    Text("Delete")
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun NewTimer(
    onDismissRequest: () -> Unit,
    onConfirmation: (Long, String) -> Unit
){
    var text by remember { mutableStateOf("") }
    var hour by remember { mutableLongStateOf(0L) }
    var minute by remember { mutableStateOf(0L) }
    var second by remember { mutableLongStateOf(0L) }
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ){
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                Text("New Timer", fontSize = 30.sp, modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp), textAlign = TextAlign.Center)
                OutlinedTextField(
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    value = text,
                    onValueChange = {text = it},
                    label = {Text("Label")}
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                hour++
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_up ), contentDescription = "up") }
                        )
                        Text(text = String.format("%02d", hour), fontSize = 40.sp, textAlign = TextAlign.Center)
                        IconButton(
                            onClick = {
                                if(hour > 0) hour--
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_down ), contentDescription = "up") }
                        )
                    }
                    Text(text = ":", fontSize = 40.sp, textAlign = TextAlign.Center)
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                minute++
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_up ), contentDescription = "up") }
                        )
                        Text(text = String.format("%02d", minute), fontSize = 40.sp, textAlign = TextAlign.Center)
                        IconButton(
                            onClick = {
                                if(minute > 0) minute--
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_down ), contentDescription = "up") }
                        )
                    }
                    Text(text = ":", fontSize = 40.sp, textAlign = TextAlign.Center)
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                second++
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_up ), contentDescription = "up") }
                        )
                        Text(text = String.format("%02d", second), fontSize = 40.sp, textAlign = TextAlign.Center)
                        IconButton(
                            onClick = {
                                if(second > 0) second--
                            },
                            content = { Icon(painter = painterResource( R.drawable.arrow_drop_down ), contentDescription = "up") }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = { onConfirmation(hour * 3600L + minute * 60L + second, text) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Create New")
                    }
                }
            }
        }
    }
}

fun formatTime(elapsedSeconds: Long): String {
    var time = elapsedSeconds
    var ret = ""
    if(elapsedSeconds < 0){
        time *= -1
        ret += "-"
    }

    return ret + String.format(
        "%02d:%02d:%02d",
        time / 3600,
        (time % 3600) / 60,
        time % 60
    )
}
