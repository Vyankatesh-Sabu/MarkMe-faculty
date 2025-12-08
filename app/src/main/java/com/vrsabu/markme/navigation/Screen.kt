package com.vrsabu.markme.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")

    object Profile : Screen("profile")
    class Attendance(courseId : Long) : Screen("attendance/$courseId")
}

