package de.kitshn.ui.route.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorCredentialsCustomHeader
import de.kitshn.api.tandoor.TandoorCredentialsToken
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.crash.crashReportHandler
import de.kitshn.ui.component.HorizontalDividerWithLabel
import de.kitshn.ui.component.buttons.LoadingMediumExtendedFloatingActionButton
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Success
import kitshn.composeapp.generated.resources.Res
import kitshn.composeapp.generated.resources.action_add
import kitshn.composeapp.generated.resources.action_apply
import kitshn.composeapp.generated.resources.action_delete
import kitshn.composeapp.generated.resources.action_sign_in
import kitshn.composeapp.generated.resources.common_api_token
import kitshn.composeapp.generated.resources.common_custom_headers
import kitshn.composeapp.generated.resources.common_error_report
import kitshn.composeapp.generated.resources.common_field
import kitshn.composeapp.generated.resources.common_instance_url
import kitshn.composeapp.generated.resources.common_loading_short
import kitshn.composeapp.generated.resources.common_not_reachable
import kitshn.composeapp.generated.resources.common_or_upper
import kitshn.composeapp.generated.resources.common_password
import kitshn.composeapp.generated.resources.common_reachable
import kitshn.composeapp.generated.resources.common_username
import kitshn.composeapp.generated.resources.common_value
import kitshn.composeapp.generated.resources.error_outdated_v1_instance
import kitshn.composeapp.generated.resources.onboarding_sign_in_error_instance_not_reachable
import kitshn.composeapp.generated.resources.onboarding_sign_in_error_instance_not_reachable_sso_hint
import kitshn.composeapp.generated.resources.onboarding_sign_in_error_sign_in_failed
import kitshn.composeapp.generated.resources.onboarding_sign_in_title
import kitshn.composeapp.generated.resources.onboarding_sign_in_using_web_browser
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
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
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val crashReportHandler = crashReportHandler()

    val coroutineScope = rememberCoroutineScope()

    var instanceUrlValue by rememberSaveable { mutableStateOf("https://app.tandoor.dev") }
    var instanceUrlDisplayValue by rememberSaveable { mutableStateOf("https://app.tandoor.dev") }
    var instanceUrlState by rememberSaveable { mutableStateOf(ErrorLoadingSuccessState.SUCCESS) }
    var instanceUrlV1Error by rememberSaveable { mutableStateOf(false) }
    val instanceUrlFocusRequester = remember { FocusRequester() }

    var showCustomHeadersDialog by remember { mutableStateOf(false) }
    val customHeaders = remember { mutableStateListOf<TandoorCredentialsCustomHeader>() }

    LaunchedEffect(instanceUrlValue, showCustomHeadersDialog) {
        instanceUrlState = ErrorLoadingSuccessState.LOADING
        delay(1000)

        val client = TandoorClient(
            TandoorCredentials(
                instanceUrl = instanceUrlValue,
                customHeaders = customHeaders
            )
        )

        if(client.testConnection(ignoreAuth = true)) {
            try {
                client.serverSettings.current()
                instanceUrlState = ErrorLoadingSuccessState.SUCCESS

                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            } catch(e: Exception) {
                instanceUrlV1Error = true
                instanceUrlState = ErrorLoadingSuccessState.ERROR

                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            }
        } else {
            instanceUrlV1Error = false
            instanceUrlState = ErrorLoadingSuccessState.ERROR

            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
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
                delay(250)

                if(tokenValue.isNotBlank()) {
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrlValue,
                        username = "",
                        password = "",
                        token = TandoorCredentialsToken(
                            token = tokenValue,
                            scope = "undefined",
                            expires = "undefined"
                        ),
                        customHeaders = customHeaders
                    )

                    p.vm.tandoorClient = TandoorClient(
                        credentials
                    )

                    val user = p.vm.tandoorClient!!.user.get()
                    if(user != null) {
                        credentials.username = user.username
                        p.vm.uiState.userDisplayName = user.display_name

                        p.vm.settings.saveTandoorCredentials(credentials)

                        p.vm.navHostController?.navigate("onboarding/welcome")

                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        return@wrapRequest
                    }

                    throw Error("UNDEFINED_USER")
                } else {
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrlValue,
                        username = usernameValue,
                        password = passwordValue,
                        customHeaders = customHeaders
                    )

                    val client = TandoorClient(credentials)
                    client.login()?.let {
                        credentials.token = it

                        TandoorRequestState().wrapRequest {
                            val user = p.vm.tandoorClient?.user?.get()
                            if(user != null) p.vm.uiState.userDisplayName = user.display_name
                        }

                        p.vm.signIn(client, credentials)

                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        return@wrapRequest
                    }

                    throw Error("UNDEFINED_TOKEN")
                }
            }

            if(loginState.state == TandoorRequestStateState.ERROR)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.onboarding_sign_in_title)) },
                actions = {
                    if(crashReportHandler != null) IconButton(
                        onClick = {
                            crashReportHandler(null)
                        }
                    ) {
                        Icon(
                            Icons.Rounded.BugReport,
                            stringResource(Res.string.common_error_report)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LoadingMediumExtendedFloatingActionButton(
                loading = loginState.state == TandoorRequestStateState.LOADING
                        || loginState.state == TandoorRequestStateState.SUCCESS,
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
                    Row {
                        TextField(
                            modifier = Modifier
                                .weight(1f, true)
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
                                if(instanceUrlState == ErrorLoadingSuccessState.ERROR) if(instanceUrlV1Error) {
                                    Text(stringResource(Res.string.error_outdated_v1_instance))
                                } else {
                                    Text(
                                        stringResource(Res.string.onboarding_sign_in_error_instance_not_reachable) + "\n\n" + stringResource(
                                            Res.string.onboarding_sign_in_error_instance_not_reachable_sso_hint
                                        )
                                    )
                                }
                            },

                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Next
                            ),

                            value = instanceUrlDisplayValue,
                            onValueChange = { value ->
                                instanceUrlDisplayValue = value
                                instanceUrlValue = value.trimEnd('/')
                            }
                        )

                        Box(
                            Modifier
                                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                                .size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    showCustomHeadersDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Code,
                                    stringResource(Res.string.common_custom_headers)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(16.dp))

                    TextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentType = androidx.compose.ui.autofill.ContentType.Username
                            },

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

                    TextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentType = androidx.compose.ui.autofill.ContentType.Password
                            },

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

                    TextField(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics {
                                contentType = androidx.compose.ui.autofill.ContentType.Password
                            },

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
                            tokenValue = if(value.length > 10 && !value.startsWith("tda_")) {
                                ""
                            } else {
                                value
                            }
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


    if(showCustomHeadersDialog) AlertDialog(
        onDismissRequest = {
            showCustomHeadersDialog = false
        },
        icon = {
            Icon(Icons.Rounded.Code, stringResource(Res.string.common_custom_headers))
        },
        title = {
            Text(stringResource(Res.string.common_custom_headers))
        },
        text = {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            customHeaders.add(
                                TandoorCredentialsCustomHeader(
                                    field = "",
                                    value = ""
                                )
                            )
                        }
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            stringResource(Res.string.action_add)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(stringResource(Res.string.action_add))
                    }
                }

                items(customHeaders.size) {
                    val header = customHeaders[it]

                    var field by remember { mutableStateOf(header.field) }
                    var value by remember { mutableStateOf(header.value) }

                    LaunchedEffect(field, value) {
                        header.field = field
                        header.value = value
                    }

                    Card {
                        Column(
                            Modifier
                                .padding(8.dp)
                        ) {
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(Res.string.common_field)) },
                                value = field,
                                onValueChange = { field = it }
                            )

                            Spacer(Modifier.height(8.dp))

                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(Res.string.common_value)) },
                                value = value,
                                onValueChange = { value = it }
                            )

                            Spacer(Modifier.height(8.dp))

                            FilledTonalButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    customHeaders.removeAt(it)
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    stringResource(Res.string.action_delete)
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(stringResource(Res.string.action_delete))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { showCustomHeadersDialog = false }
            ) {
                Text(stringResource(Res.string.action_apply))
            }
        }
    )

    LaunchedEffect(Unit) {
        this.coroutineContext.job.invokeOnCompletion {
            try {
                instanceUrlFocusRequester.requestFocus()
            } catch(e: Exception) {
                Logger.e("OnboardingSignIn.kt", e)
            }
        }
    }
}
