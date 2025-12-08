package com.vrsabu.markme.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vrsabu.markme.R
import com.vrsabu.markme.navigation.Screen

@Composable
fun LoginPage(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // Collect auth state to react to login success/failure
    val authState by viewModel.authState.collectAsState()

    // Navigate to Home when login succeeds
    LaunchedEffect(authState) {
        if (authState?.getOrNull() != null && viewModel.isLoggedIn()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    LoginScreenContent(
        onLogin = { email, pass -> viewModel.login(email, pass) },
        onSignUp = { /*navController.navigate(SignUp)*/ },
        onForgot = { /* TODO */ },
        authState = authState
    )
}

@Composable
fun LoginScreenContent(
    onLogin: (String, String) -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit,
    authState: Result<Unit>?
) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(40.dp))

        // Login Illustration
        Image(
            painter = painterResource(R.drawable._957136_mobile_login),
            contentDescription = "Login Illustration",
            modifier = Modifier.size(420.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Welcome",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Login to your account",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email address") },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1E3A8A),
                modifier = Modifier.clickable { onForgot() }
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E3A8A)
            )
        ) {
            Text("Login", color = Color.White, fontSize = 17.sp)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?")
            Spacer(Modifier.width(6.dp))
            Text(
                "Sign up",
                color = Color(0xFF1E3A8A),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSignUp() }
            )
        }

        Spacer(Modifier.height(20.dp))

        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(40.dp))
    }

    // Handle auth state
    authState?.let { result ->
        result.onSuccess {
            // Navigation handled at LoginPage level
        }
        // Optionally show failure messages
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Surface {
        LoginScreenContent(
            onLogin = { _, _ -> },
            onSignUp = {},
            onForgot = {},
            authState = null
        )
    }
}
