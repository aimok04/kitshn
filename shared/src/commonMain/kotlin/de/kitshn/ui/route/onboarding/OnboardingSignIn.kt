package de.kitshn.ui.route.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HighlightOff
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.eygraber.uri.Uri
import de.kitshn.Platforms
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorRequestsError
import de.kitshn.api.tandoor.TandoorCredentialsCustomHeader
import de.kitshn.api.tandoor.TandoorCredentialsToken
import de.kitshn.api.tandoor.TandoorRequestState
import de.kitshn.api.tandoor.TandoorRequestStateState
import de.kitshn.api.tandoor.rememberTandoorRequestState
import de.kitshn.crash.crashReportHandler
import de.kitshn.platformDetails
import de.kitshn.ui.component.HorizontalDividerWithLabel
import de.kitshn.utils.ClientCertificateData
import de.kitshn.utils.rememberClientCertificateSelector
import de.kitshn.ui.component.buttons.LoadingMediumExtendedFloatingActionButton
import de.kitshn.ui.route.RouteParameters
import de.kitshn.ui.state.ErrorLoadingSuccessState
import de.kitshn.ui.theme.Success
import kitshn.shared.generated.resources.Res
import kitshn.shared.generated.resources.action_abort
import kitshn.shared.generated.resources.action_add_custom_header
import kitshn.shared.generated.resources.action_add_mtls_certificate
import kitshn.shared.generated.resources.action_apply
import kitshn.shared.generated.resources.action_delete
import kitshn.shared.generated.resources.action_edit_custom_header
import kitshn.shared.generated.resources.action_remove
import kitshn.shared.generated.resources.action_select_certificate
import kitshn.shared.generated.resources.action_sign_in
import kitshn.shared.generated.resources.common_advanced_settings
import kitshn.shared.generated.resources.common_api_token
import kitshn.shared.generated.resources.common_custom_headers
import kitshn.shared.generated.resources.common_error_report
import kitshn.shared.generated.resources.common_field
import kitshn.shared.generated.resources.common_instance_url
import kitshn.shared.generated.resources.common_mtls
import kitshn.shared.generated.resources.common_not_reachable
import kitshn.shared.generated.resources.common_or_upper
import kitshn.shared.generated.resources.common_password
import kitshn.shared.generated.resources.common_reachable
import kitshn.shared.generated.resources.common_username
import kitshn.shared.generated.resources.common_value
import kitshn.shared.generated.resources.error_outdated_v1_instance
import kitshn.shared.generated.resources.onboarding_sign_in_custom_header_duplicate
import kitshn.shared.generated.resources.onboarding_sign_in_error_client_certificate_required
import kitshn.shared.generated.resources.onboarding_sign_in_error_instance_not_reachable
import kitshn.shared.generated.resources.onboarding_sign_in_error_instance_not_reachable_local_network_hint
import kitshn.shared.generated.resources.onboarding_sign_in_error_instance_not_reachable_sso_hint
import kitshn.shared.generated.resources.onboarding_sign_in_error_sign_in_failed
import kitshn.shared.generated.resources.onboarding_sign_in_hint_credentials_required
import org.jetbrains.compose.resources.getString
import kitshn.shared.generated.resources.onboarding_sign_in_mtls_required_dialog_description
import kitshn.shared.generated.resources.onboarding_sign_in_mtls_required_dialog_title
import kitshn.shared.generated.resources.onboarding_sign_in_title
import kitshn.shared.generated.resources.onboarding_sign_in_using_web_browser
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalEncodingApi::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun RouteOnboardingSignIn(
    p: RouteParameters
) {
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val crashReportHandler = crashReportHandler()

    val coroutineScope = rememberCoroutineScope()
    val certificateSelector = rememberClientCertificateSelector()

    var instanceUrlValue by rememberSaveable { mutableStateOf("https://app.tandoor.dev") }
    var instanceUrlDisplayValue by rememberSaveable { mutableStateOf("https://app.tandoor.dev") }
    var instanceUrlState by rememberSaveable { mutableStateOf(ErrorLoadingSuccessState.SUCCESS) }
    var instanceUrlV1Error by rememberSaveable { mutableStateOf(false) }
    var instanceUrlRequiresClientCert by rememberSaveable { mutableStateOf(false) }
    var instanceUrlRetryToken by rememberSaveable { mutableStateOf(0) }
    val instanceUrlFocusRequester = remember { FocusRequester() }

    var editingHeader by remember { mutableStateOf<TandoorCredentialsCustomHeader?>(null) }
    var isEditingHeader by remember { mutableStateOf(false) }
    val customHeaders = rememberSaveable { mutableStateListOf<TandoorCredentialsCustomHeader>() }
    var showMtlsRequiredDialog by remember { mutableStateOf(false) }
    var mtlsCertificateAlias by rememberSaveable { mutableStateOf<String?>(null) }
    var mtlsCertificateData by rememberSaveable { mutableStateOf<String?>(null) }
    var mtlsCertificatePassword by rememberSaveable { mutableStateOf<String?>(null) }

    val isMtlsConfigured = mtlsCertificateAlias != null || mtlsCertificateData != null
    var advancedSettingsExpanded by rememberSaveable {
        mutableStateOf(isMtlsConfigured || customHeaders.isNotEmpty())
    }

    fun applyMtlsCertData(data: ClientCertificateData?) {
        if (data != null) {
            mtlsCertificateAlias = data.alias
            mtlsCertificateData = data.pkcs12DataBase64
            mtlsCertificatePassword = data.pkcs12Password
            advancedSettingsExpanded = true
        }
    }

    LaunchedEffect(
        instanceUrlValue,
        editingHeader,
        mtlsCertificateAlias,
        mtlsCertificateData,
        instanceUrlRetryToken
    ) {
        instanceUrlState = ErrorLoadingSuccessState.LOADING
        delay(1000)

        val client = TandoorClient(
            TandoorCredentials(
                instanceUrl = instanceUrlValue,
                customHeaders = customHeaders,
                mtlsCertificateAlias = mtlsCertificateAlias,
                mtlsCertificateData = mtlsCertificateData,
                mtlsCertificatePassword = mtlsCertificatePassword,
            )
        )

        if (client.testConnection(ignoreAuth = true)) {
            try {
                client.serverSettings.current()
                instanceUrlState = ErrorLoadingSuccessState.SUCCESS
                instanceUrlRequiresClientCert = false

                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            } catch (e: Exception) {
                instanceUrlV1Error = true
                instanceUrlState = ErrorLoadingSuccessState.ERROR
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                instanceUrlRequiresClientCert = client.needsClientCertificate && !isMtlsConfigured
                showMtlsRequiredDialog = instanceUrlRequiresClientCert
            }
        } else {
            instanceUrlV1Error = false
            instanceUrlState = ErrorLoadingSuccessState.ERROR
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            instanceUrlRequiresClientCert = client.needsClientCertificate && !isMtlsConfigured
            showMtlsRequiredDialog = instanceUrlRequiresClientCert
        }
    }

    var usernameValue by rememberSaveable { mutableStateOf("") }
    var passwordValue by rememberSaveable { mutableStateOf("") }
    var tokenValue by rememberSaveable { mutableStateOf("") }

    val loginState = rememberTandoorRequestState()
    LaunchedEffect(usernameValue, passwordValue, tokenValue) { loginState.reset() }

    val snackbarHostState = remember { SnackbarHostState() }

    fun done() {
        if (instanceUrlState == ErrorLoadingSuccessState.ERROR) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            coroutineScope.launch { snackbarHostState.showSnackbar(getString(Res.string.onboarding_sign_in_error_instance_not_reachable)) }
            return
        }
        if (tokenValue.isBlank() && (usernameValue.isBlank() || passwordValue.isBlank())) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
            coroutineScope.launch { snackbarHostState.showSnackbar(getString(Res.string.onboarding_sign_in_hint_credentials_required)) }
            return
        }

        coroutineScope.launch {
            loginState.wrapRequest {
                delay(250)

                if (tokenValue.isNotBlank()) {
                    val credentials = TandoorCredentials(
                        instanceUrl = instanceUrlValue,
                        username = "",
                        password = "",
                        token = TandoorCredentialsToken(
                            token = tokenValue,
                            scope = "undefined",
                            expires = "undefined"
                        ),
                        customHeaders = customHeaders,
                        mtlsCertificateAlias = mtlsCertificateAlias,
                        mtlsCertificateData = mtlsCertificateData,
                        mtlsCertificatePassword = mtlsCertificatePassword,
                    )

                    p.vm.tandoorClient = TandoorClient(
                        credentials
                    )

                    val user = p.vm.tandoorClient!!.user.get()
                    if (user != null) {
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
                        customHeaders = customHeaders,
                        mtlsCertificateAlias = mtlsCertificateAlias,
                        mtlsCertificateData = mtlsCertificateData,
                        mtlsCertificatePassword = mtlsCertificatePassword,
                    )

                    val client = TandoorClient(credentials)
                    client.login()?.let {
                        credentials.token = it

                        TandoorRequestState().wrapRequest {
                            val user = p.vm.tandoorClient?.user?.get()
                            if (user != null) p.vm.uiState.userDisplayName = user.display_name
                        }

                        p.vm.signIn(client, credentials)

                        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                        return@wrapRequest
                    }

                    throw TandoorRequestsError(null, null, overrideMessage = "Login failed: server did not return a token")
                }
            }

            if (loginState.state == TandoorRequestStateState.ERROR)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    fun selectMtls(uri: Uri?) {
        val host = uri?.host
        val port = uri?.port ?: -1

        certificateSelector.selectCertificate(host, port) { data ->
            if (data != null) applyMtlsCertData(data)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.onboarding_sign_in_title)) },
                actions = {
                    if (crashReportHandler != null) IconButton(
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
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            InstanceUrlStatusIndicator(
                                state = instanceUrlState,
                                onRetry = { instanceUrlRetryToken++ })
                        },
                        isError = instanceUrlState == ErrorLoadingSuccessState.ERROR,
                        supportingText = {
                            if (instanceUrlState == ErrorLoadingSuccessState.ERROR) if (instanceUrlRequiresClientCert) {
                                Text(stringResource(Res.string.onboarding_sign_in_error_client_certificate_required))
                            } else if (instanceUrlV1Error) {
                                Text(stringResource(Res.string.error_outdated_v1_instance))
                            } else {
                                Text(
                                    buildString {
                                        append(stringResource(Res.string.onboarding_sign_in_error_instance_not_reachable))
                                        append("\n\n")
                                        append(stringResource(Res.string.onboarding_sign_in_error_instance_not_reachable_sso_hint))

                                        if (platformDetails.platform == Platforms.IOS) {
                                            append("\n\n")
                                            append(stringResource(Res.string.onboarding_sign_in_error_instance_not_reachable_local_network_hint))
                                        }
                                    }
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

                    Spacer(Modifier.height(4.dp))

                    val chevronRotation by animateFloatAsState(
                        targetValue = if (advancedSettingsExpanded) 180f else 0f,
                        label = "advancedSettingsChevron"
                    )

                    TextButton(
                        onClick = {
                            advancedSettingsExpanded = !advancedSettingsExpanded
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth()
                    ) {
                        Text(stringResource(Res.string.common_advanced_settings))
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.rotate(chevronRotation)
                        )
                    }

                    AnimatedVisibility(
                        visible = advancedSettingsExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            InputChip(
                                selected = isMtlsConfigured,
                                onClick = {
                                    if (isMtlsConfigured) {
                                        mtlsCertificateAlias = null
                                        mtlsCertificateData = null
                                        mtlsCertificatePassword = null
                                    } else {
                                        selectMtls(Uri.parseOrNull(instanceUrlValue))
                                    }
                                },
                                label = {
                                    if (isMtlsConfigured) {
                                        Text(
                                            mtlsCertificateAlias
                                                ?: stringResource(Res.string.common_mtls)
                                        )
                                    } else {
                                        Text(stringResource(Res.string.action_add_mtls_certificate))
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.VerifiedUser,
                                        stringResource(Res.string.common_mtls),
                                    )
                                },
                                trailingIcon = {
                                    if (isMtlsConfigured) Icon(
                                        Icons.Rounded.HighlightOff,
                                        contentDescription = stringResource(Res.string.action_remove),
                                    )
                                }
                            )

                            for (header in customHeaders) {
                                InputChip(
                                    selected = true,
                                    onClick = {
                                        editingHeader = header
                                        isEditingHeader = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Code,
                                            stringResource(Res.string.common_custom_headers)
                                        )
                                    },
                                    label = {
                                        Text("${header.field}: ${header.value}")
                                    }
                                )
                            }

                            AssistChip(
                                onClick = {
                                    editingHeader = TandoorCredentialsCustomHeader("", "")
                                    isEditingHeader = false
                                },
                                label = {
                                    Text(stringResource(Res.string.action_add_custom_header))
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.Code,
                                        stringResource(Res.string.common_custom_headers)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

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
                            if (loginState.state == TandoorRequestStateState.ERROR)
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
                            if (loginState.state == TandoorRequestStateState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_sign_in_failed))
                        },

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
                            if (loginState.state == TandoorRequestStateState.ERROR)
                                Text(stringResource(Res.string.onboarding_sign_in_error_sign_in_failed))
                        },

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
                            tokenValue = if (value.length > 10 && !value.startsWith("tda_")) {
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


    if (showMtlsRequiredDialog) AlertDialog(
        onDismissRequest = { showMtlsRequiredDialog = false },
        icon = { Icon(Icons.Rounded.VerifiedUser, null) },
        title = { Text(stringResource(Res.string.onboarding_sign_in_mtls_required_dialog_title)) },
        text = { Text(stringResource(Res.string.onboarding_sign_in_mtls_required_dialog_description)) },
        confirmButton = {
            Button(onClick = {
                showMtlsRequiredDialog = false
                selectMtls(Uri.parseOrNull(instanceUrlValue))
            }) { Text(stringResource(Res.string.action_select_certificate)) }
        },
        dismissButton = {
            TextButton(onClick = { showMtlsRequiredDialog = false }) {
                Text(stringResource(Res.string.action_abort))
            }
        }
    )

    editingHeader?.let { header ->
        var field by remember(header) { mutableStateOf(header.field) }
        var value by remember(header) { mutableStateOf(header.value) }
        val valueFocusRequester = remember { FocusRequester() }

        val isDuplicate =
            customHeaders.any { it.field.equals(field, ignoreCase = true) && it !== header }
        val canSubmit = field.isNotBlank() && value.isNotBlank() && !isDuplicate

        fun submit() {
            if (!canSubmit) return
            val new = TandoorCredentialsCustomHeader(field, value)
            if (isEditingHeader) {
                val index = customHeaders.indexOf(header)
                if (index != -1) customHeaders[index] = new
            } else {
                customHeaders.add(new)
            }
            editingHeader = null
        }

        AlertDialog(
            onDismissRequest = { editingHeader = null },
            icon = {
                Icon(Icons.Rounded.Code, null)
            },
            title = {
                Text(
                    stringResource(
                        if (isEditingHeader) Res.string.action_edit_custom_header
                        else Res.string.action_add_custom_header
                    )
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.common_field)) },
                        placeholder = { Text("X-Custom-Header") },
                        value = field,
                        onValueChange = { field = it },
                        singleLine = true,
                        isError = isDuplicate,
                        supportingText = {
                            if (isDuplicate) Text(stringResource(Res.string.onboarding_sign_in_custom_header_duplicate))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { valueFocusRequester.requestFocus() }
                        )
                    )

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(valueFocusRequester),
                        label = { Text(stringResource(Res.string.common_value)) },
                        value = value,
                        onValueChange = { value = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() })
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = canSubmit,
                    onClick = { submit() }
                ) {
                    Text(stringResource(Res.string.action_apply))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isEditingHeader) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            onClick = {
                                customHeaders.remove(header)
                                editingHeader = null
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.action_delete))
                        }
                    }

                    TextButton(onClick = { editingHeader = null }) {
                        Text(stringResource(Res.string.action_abort))
                    }
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        this.coroutineContext.job.invokeOnCompletion {
            try {
                instanceUrlFocusRequester.requestFocus()
            } catch (e: Exception) {
                Logger.e("OnboardingSignIn.kt", e)
            }
        }
    }
}


@Composable
private fun InstanceUrlStatusIndicator(
    state: ErrorLoadingSuccessState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            (fadeIn(tween(200, delayMillis = 40)) + scaleIn(
                tween(200, delayMillis = 40),
                initialScale = 0.8f
            ))
                .togetherWith(fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.8f))
        },
        label = "instanceUrlStatusTransition",
        modifier = modifier
    ) { targetState ->
        val containerColor = when (targetState) {
            ErrorLoadingSuccessState.LOADING -> MaterialTheme.colorScheme.surfaceContainerHighest
            ErrorLoadingSuccessState.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
            ErrorLoadingSuccessState.ERROR -> MaterialTheme.colorScheme.errorContainer
        }

        val contentColor = when (targetState) {
            ErrorLoadingSuccessState.LOADING -> MaterialTheme.colorScheme.primary
            ErrorLoadingSuccessState.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
            ErrorLoadingSuccessState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(containerColor, CircleShape)
                .then(
                    if (targetState == ErrorLoadingSuccessState.ERROR) {
                        Modifier.clickable(onClick = onRetry)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                ErrorLoadingSuccessState.LOADING -> LoadingIndicator(
                    modifier = Modifier.size(24.dp)
                )

                ErrorLoadingSuccessState.ERROR -> Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = stringResource(Res.string.common_not_reachable),
                    tint = contentColor
                )

                ErrorLoadingSuccessState.SUCCESS -> Icon(
                    Icons.Rounded.Check,
                    contentDescription = stringResource(Res.string.common_reachable),
                    tint = contentColor
                )
            }
        }
    }
}