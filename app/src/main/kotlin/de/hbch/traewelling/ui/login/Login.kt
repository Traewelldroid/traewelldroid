package de.hbch.traewelling.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.hbch.traewelling.R
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.theme.Traewelldroid
import de.hbch.traewelling.ui.composables.ButtonWithIconAndText
import de.hbch.traewelling.ui.composables.OutlinedButtonWithIconAndText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loginAction: () -> Unit = { },
    informationAction: () -> Unit = { }
) {
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.setSystemBarsColor(Traewelldroid, darkIcons = false)
        systemUiController.setNavigationBarColor(Traewelldroid, darkIcons = false)

        onDispose {  }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        containerColor = Traewelldroid
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            val horizontalPadding = Modifier.padding(horizontal = 16.dp)
            Text(
                text = stringResource(id = R.string.welcome),
                fontSize = 40.sp,
                color = Color.White,
                modifier = horizontalPadding.padding(top = 64.dp)
            )
            Text(
                text = stringResource(id = R.string.account_notice),
                color = Color.White,
                modifier = horizontalPadding.padding(top = 32.dp)
            )
            ButtonWithIconAndText(
                stringId = R.string.login_oauth,
                drawableId = R.drawable.ic_popup,
                onClick = loginAction,
                modifier = horizontalPadding.padding(top = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC72730),
                    contentColor = Color.White
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                OutlinedButtonWithIconAndText(
                    stringId = R.string.information,
                    modifier = horizontalPadding.padding(bottom = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    onClick = informationAction
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLoginScreen() {
    MainTheme {
        LoginScreen()
    }
}
