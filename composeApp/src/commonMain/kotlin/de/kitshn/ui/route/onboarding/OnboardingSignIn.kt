package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorCredentialsToken
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.ui.component.HorizontalDividerWithLabel
import de.kitshn.ui.component.buttons.LoadingExtendedFloatingActionButton
import de.kitshn.ui.modifier.autofill
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Success
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_sign_in
import kitshn.composeapp.generated.resources.common_api_token
import kitshn.composeapp.generated.resources.common_instance_url
import kitshn.composeapp.generated.resources.common_loading_short
import kitshn.composeapp.generated.resources.common_not_reachable
import kitshn.composeapp.generated.resources.common_or_upper
import kitshn.composeapp.generated.resources.common_password
import kitshn.composeapp.generated.resources.common_reachable
import kitshn.composeapp.generated.resources.common_username
import kitshn.composeapp.generated.resources.onboarding_sign_in_error_instance_not_reachable
import kitshn.composeapp.generated.resources.onboarding_sign_in_error_sign_in_failed
import kitshn.composeapp.generated.resources.onboarding_sign_in_title
import kitshn.composeapp.generated.resources.onboarding_sign_in_using_web_browser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalEncodingApi::class
)
@Composable
fun RouteOnboardingSignIn(
    p: RouteParameters
) {
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()

    var instanceUrlValue by rememberSaveable { mutableStateOf("https://app.tandoor.dev") }
    var instanceUrlState by rememberSaveable { mutableStateOf(ErrorLoadingSuccessState.SUCCESS) }
    val instanceUrlFocusRequester = remember { FocusRequester() }

    LaunchedEffect(instanceUrlValue) {
        instanceUrlState = ErrorLoadingSuccessState.LOADING
        delay(1000)

        instanceUrlState = if(TandoorClient(TandoorCredentials(instanceUrlValue))
                .testConnection(ignoreAuth = true)
        ) {
            ErrorLoadingSuccessState.SUCCESS
        } else {
            ErrorLoadingSuccessState.ERROR
        }
    }

    var usernameValue by rememberSaveable { mutableStateOf("") }
    var passwordValue by rememberSaveable { mutableStateOf("") }
    var tokenValue by rememberSaveable { mutableStateOf("") }

    val loginState = rememberTandoorRequestState()
    LaunchedEffect(usernameValue, passwordValue, tokenValue) { loginState.reset() }

    fun done() {
        coroutineScope.launch {
            loginState.wrapRequest {
                if(tokenValue.isNotBlank()) {
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrlValue,
                        username = "",
                        password = "",
                        token = TandoorCredentialsToken(
                            token = tokenValue,
                            scope = "undefined",
                            expires = "undefined"
                        )
                    )

                    p.vm.tandoorClient = TandoorClient(
                        credentials
                    )

                    val user = p.vm.tandoorClient!!.user.get()
                    if(user != null) {
                        credentials.username = user.display_name
                        p.vm.settings.saveTandoorCredentials(credentials)

                        p.vm.navHostController?.navigate("onboarding/welcome")
                        return@wrapRequest
                    }

                    throw Error("UNDEFINED_USER")
                } else {
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrlValue,
                        username = usernameValue,
                        password = passwordValue
                    )

                    p.vm.tandoorClient = TandoorClient(
                        credentials
                    )

                    val token = p.vm.tandoorClient!!.login()
                    if(token != null) {
                        credentials.token = token
                        p.vm.settings.saveTandoorCredentials(credentials)

                        p.vm.navHostController?.navigate("onboarding/welcome")
                        return@wrapRequest
                    }

                    throw Error("UNDEFINED_TOKEN")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.onboarding_sign_in_title)) }
            )
        },
        floatingActionButton = {
            LoadingExtendedFloatingActionButton(
                loading = loginState.state == TandoorRequestStateState.LOADING,
                text = { Text(text = stringResource(Res.string.action_sign_in)) },
                icon = {
                    Icon(
                        Icons.AutoMirrored.Rounded.Login,
                        stringResource(Res.string.action_sign_in)
                    )
                },
                onClick = { done() }
            )
        }
    ) {
        LazyColumn(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            item {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(instanceUrlFocusRequester),

                        label = { Text(stringResource(Res.string.common_instance_url)) },
                        placeholder = { Text("https://app.tandoor.dev") },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Web,
                                stringResource(Res.string.common_instance_url)
                            )
                        },

                        trailingIcon = {
                            when(instanceUrlState) {
                                ErrorLoadingSuccessState.ERROR -> Icon(
                                    Icons.Rounded.ErrorOutline,
                                    stringResource(
                                        Res.string.common_not_reachable
                                    ), Modifier, MaterialTheme.colorScheme.error
                                )

                                ErrorLoadingSuccessState.LOADING -> Icon(
                                    Icons.Rounded.Refresh,
                                    stringResource(
                                        Res.string.common_loading_short
                                    )
                                )

                                ErrorLoadingSuccessState.SUCCESS -> Icon(
                                    Icons.Rounded.Check,
                                    stringResource(
                                        Res.string.common_reachable
                                    ), Modifier, Success
                                )
                            }
                        },
                        isError = instanceUrlState == ErrorLoadingSuccessState.ERROR,
                        supportingText = {
                            if(instanceUrlState == ErrorLoadingSuccessState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_instance_not_reachable))
                        },

                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),

                        value = instanceUrlValue,
                        onValueChange = { value ->
                            instanceUrlValue = value
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .autofill(
                                autofillTypes = listOf(AutofillType.Username),
                                onFill = { v -> usernameValue = v }
                            ),

                        enabled = instanceUrlState == ErrorLoadingSuccessState.SUCCESS,
                        label = { Text(stringResource(Res.string.common_username)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                stringResource(Res.string.common_username)
                            )
                        },

                        isError = loginState.state == TandoorRequestStateState.ERROR,
                        supportingText = {
                            if(loginState.state == TandoorRequestStateState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_sign_in_failed))
                        },

                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),

                        value = usernameValue,
                        onValueChange = { value ->
                            usernameValue = value
                        }
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .autofill(
                                autofillTypes = listOf(AutofillType.Password),
                                onFill = { v ->
                                    if(!v.startsWith("tda_"))
                                        passwordValue = v
                                }
                            ),

                        enabled = instanceUrlState == ErrorLoadingSuccessState.SUCCESS,
                        label = { Text(stringResource(Res.string.common_password)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Password,
                                stringResource(Res.string.common_password)
                            )
                        },

                        isError = loginState.state == TandoorRequestStateState.ERROR,
                        supportingText = {
                            if(loginState.state == TandoorRequestStateState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_sign_in_failed))
                        },

                        visualTransformation = PasswordVisualTransformation(),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                done()
                                focusManager.clearFocus()
                            }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),

                        value = passwordValue,
                        onValueChange = { value ->
                            passwordValue = value
                        }
                    )

                    HorizontalDividerWithLabel(text = stringResource(Res.string.common_or_upper))

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .autofill(
                                autofillTypes = listOf(AutofillType.Password),
                                onFill = { v ->
                                    if(v.startsWith("tda_"))
                                        tokenValue = v
                                }
                            ),

                        enabled = instanceUrlState == ErrorLoadingSuccessState.SUCCESS,
                        label = { Text(stringResource(Res.string.common_api_token)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Key,
                                stringResource(Res.string.common_api_token)
                            )
                        },

                        isError = loginState.state == TandoorRequestStateState.ERROR,
                        supportingText = {
                            if(loginState.state == TandoorRequestStateState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_sign_in_failed))
                        },

                        visualTransformation = PasswordVisualTransformation(),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                done()
                                focusManager.clearFocus()
                            }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),

                        value = tokenValue,
                        onValueChange = { value ->
                            tokenValue = value
                        }
                    )

                    HorizontalDividerWithLabel(text = stringResource(Res.string.common_or_upper))

                    OutlinedButton(
                        onClick = {
                            p.vm.navHostController?.navigate(
                                "onboarding/signIn/browser/${
                                    kotlin.io.encoding.Base64.encode(
                                        instanceUrlValue.encodeToByteArray()
                                    )
                                }"
                            )
                        },
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            Icons.Rounded.Web,
                            stringResource(Res.string.onboarding_sign_in_using_web_browser)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(text = stringResource(Res.string.onboarding_sign_in_using_web_browser))
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        instanceUrlFocusRequester.requestFocus()
    }
}