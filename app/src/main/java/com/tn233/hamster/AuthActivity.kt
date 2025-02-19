package com.tn233.hamster

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.tn233.hamster.ui.theme.HamsterNewsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthActivity : ComponentActivity() {

    val coroutineScope = CoroutineScope(Dispatchers.Default) // Coroutine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HamsterNewsTheme {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }) { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
                        Row(horizontalArrangement = Arrangement.Center){
                            Image(painterResource(R.drawable.logo_hamster),"hamster_news_logo",modifier = Modifier.size(100.dp))
                        }
                        Text("仓鼠新闻 - 登录",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SourceHanSerif,
                            lineHeight = 1.25.em)
//                        Spacer(modifier = Modifier.height(10.dp))

                        var username by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("用户名/邮箱") }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            if(username.isNullOrBlank() || password.isNullOrBlank()){
                                scope.launch { snackbarHostState.showSnackbar("请检查用户名和密码是否输入后重试") }
                                return@Button;
                            }
                            coroutineScope.launch {
                                if(!Login(username, password)){
                                    scope.launch {
                                        snackbarHostState.showSnackbar("用户名或密码错误，请重试")
                                    }
                                }
                            }
                        }, modifier = Modifier.height(45.dp).width(95.dp)) {
                            Text("登录")
                        }
                        Spacer(modifier = Modifier.height(150.dp))
                        TextButton(onClick = {

                        }) {
                            Text("没有账户？注册")
                        }
                    }
                }
            }
        }
    }

    fun Login(username: String, password: String): Boolean{
        val body_construct = "{\"email\": \"${username}\",\"password\": \"${password}\"}"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://113.45.206.216:3000/api/users/login")
            .post(body_construct.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            val res_obj = JSONObject(response.body?.string())
            val is_success = res_obj.getBoolean("success")
            Log.i("res_obj", res_obj.toString())
            Log.i("is_success", is_success.toString())
            return is_success
        }catch (e: Exception){
            Log.e("GET_NEWS_ERR", e.toString())
            return false
        }
    }

}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HamsterNewsTheme {
        Greeting("Android")
    }
}