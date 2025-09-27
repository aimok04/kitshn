@file:OptIn(ExperimentalTime::class)

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewModelScope
import de.kitshn.AppActivity
import de.kitshn.KitshnViewModel
import de.kitshn.TestTagRepository
import de.kitshn.api.tandoor.TandoorClient
import de.kitshn.api.tandoor.TandoorCredentials
import de.kitshn.api.tandoor.TandoorCredentialsToken
import de.kitshn.ui.theme.custom.AvailableColorSchemes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AutomatedScreenshotTest {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<AppActivity>()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun automatedScreenshotTest() {
        composeTestRule.waitUntil { composeTestRule.activity.vm != null }
        val vm = composeTestRule.activity.vm!!

        vm.tandoorClient = TandoorClient(
            credentials = TandoorCredentials(
                instanceUrl = kitshn.composeApp.BuildConfig.TEST_DEMO_URL,
                username = "demo",
                token = TandoorCredentialsToken(
                    token = "",
                    scope = "",
                    expires = ""
                )
            )
        )

        vm.isTest = true

        // set settings
        vm.settings.setLatestBetaVersionCheck("-1")

        vm.settings.setColorScheme(AvailableColorSchemes.DEFAULT.name)
        vm.settings.setEnableSystemTheme(false)

        vm.settings.setEnableDarkTheme(false)
        composeTestRule.waitForIdle()
        createScreenshotSeries(vm, "LIGHT")

        vm.settings.setEnableDarkTheme(true)
        composeTestRule.waitForIdle()
        createScreenshotSeries(vm, "DARK")
    }

    @OptIn(ExperimentalTestApi::class)
    private fun createScreenshotSeries(
        vm: KitshnViewModel,
        prefix: String
    ) {
        // create HOME screenshot
        vm.viewModelScope.launch {
            vm.navigateTo("main", "home")
        }
        waitUntilRoute("main")

        val latestBetaVersionCheck = runBlocking { vm.settings.getLatestBetaVersionCheck.first() }
        if(kitshn.composeApp.BuildConfig.PACKAGE_IS_BETA && latestBetaVersionCheck == "-1")
            composeTestRule.onNodeWithTag(TestTagRepository.ACTION_OKAY.name)
                .performClick()

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(
                TestTagRepository.CARD_RECIPE.active(
                    true
                )
            ), 10000
        )
        takeScreenshot(
            id = "${prefix}_01_HOME",
            delay = 5000
        )

        // create HOME_CREATE_RECIPE screenshot
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_ADD.name)
            .performClick()

        takeScreenshot("${prefix}_04_HOME_CREATE_RECIPE")

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TestTagRepository.ACTION_CLOSE_DIALOG.name))
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_CLOSE_DIALOG.name)
            .performClick()

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TestTagRepository.ACTION_CONTINUE.name))
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_CONTINUE.name)
            .performClick()

        // create HOME_RECIPE_VIEW screenshot
        composeTestRule.waitForIdle()
        vm.viewRecipe(434)

        takeScreenshot("${prefix}_02_HOME_RECIPE_VIEW")

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_CLOSE_RECIPE.name)
            .performClick()

        // create MEAL_PLAN screenshot
        composeTestRule.waitForIdle()
        composeTestRule.activity.vm!!.run {
            viewModelScope.launch {
                vm.mainSubNavHostController?.navigate("mealplan")
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(
                TestTagRepository.CARD_MEAL_PLAN_DAY.active(
                    true
                )
            ), 10000
        )
        takeScreenshot("${prefix}_05_MEAL_PLAN")

        // create MEAL_PLAN_CREATE screenshot
        composeTestRule.onAllNodesWithTag(TestTagRepository.ACTION_ADD.name)
            .onFirst()
            .performClick()

        takeScreenshot("${prefix}_06_MEAL_PLAN_CREATE")

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TestTagRepository.ACTION_CLOSE_DIALOG.name))
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_CLOSE_DIALOG.name)
            .performClick()

        // create SHOPPING screenshot
        composeTestRule.activity.vm!!.run {
            viewModelScope.launch {
                vm.mainSubNavHostController?.navigate("shopping")
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(
                TestTagRepository.SCAFFOLD_SHOPPING.active(
                    true
                )
            ), 10000
        )
        takeScreenshot("${prefix}_07_SHOPPING")

        // create BOOKS screenshot
        composeTestRule.waitForIdle()
        composeTestRule.activity.vm!!.run {
            viewModelScope.launch {
                vm.mainSubNavHostController?.navigate("books")
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(
                TestTagRepository.CARD_HORIZONTAL_RECIPE_BOOK.active(
                    true
                )
            ), 10000
        )
        takeScreenshot("${prefix}_09_BOOKS")

        // create BOOKS_DETAILS_VIEW screenshot
        composeTestRule.onAllNodesWithTag(TestTagRepository.CARD_HORIZONTAL_RECIPE_BOOK.active(true))
            .onFirst()
            .performClick()

        takeScreenshot("${prefix}_10_BOOKS_DETAILS_VIEW")

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTagRepository.ACTION_CLOSE_RECIPE_BOOK.name)
            .performClick()

        // create RECIPE_COOKING_MODE screenshot
        composeTestRule.waitForIdle()
        composeTestRule.activity.vm!!.run {
            viewModelScope.launch {
                navigateTo("recipe/434/cook/4")
            }
        }

        takeScreenshot("${prefix}_03_RECIPE_COOKING_MODE")

        // create SHOPPING_MODE screenshot
        composeTestRule.activity.vm!!.run {
            viewModelScope.launch {
                navigateTo("shopping/shoppingMode")
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntilAtLeastOneExists(
            hasTestTag(TestTagRepository.LIST_ITEM_SHOPPING_LIST_ENTRY.name),
            10000
        )
        takeScreenshot("${prefix}_08_SHOPPING_MODE")
    }

    private fun takeScreenshot(id: String, delay: Long = 1000) {
        composeTestRule.waitForIdle()
        timeout(delay)
        Screengrab.screenshot(id)
    }

    private fun timeout(millis: Long) {
        val time = Clock.System.now().toEpochMilliseconds()
        composeTestRule.waitUntil(10000) {
            (Clock.System.now().toEpochMilliseconds() - time) > millis
        }
    }

    fun waitUntilRoute(route: String) {
        composeTestRule.waitUntil(
            timeoutMillis = 10000
        ) {
            composeTestRule.activity.vm?.navHostController?.currentDestination?.route == route
        }
    }

}