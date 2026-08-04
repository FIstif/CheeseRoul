sealed class Screen(val route: String) {
    object Setup : Screen("setup_screen")
    object Game : Screen("game_screen")
    object History : Screen("history_screen") // <- Новое
}