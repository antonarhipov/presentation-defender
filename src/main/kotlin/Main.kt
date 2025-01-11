import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.TimerApp
import viewmodel.TimerViewModel

fun main() = application {

    val viewModel = remember { TimerViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Timer Application"
    ) {
        TimerApp(viewModel)
    }
}
