package de.kitshn.api.tandoor.model

import de.kitshn.api.tandoor.route.TandoorAIProvider
import de.kitshn.api.tandoor.route.TandoorUser
import kotlinx.serialization.Serializable

@Serializable
enum class TandoorSpaceTheme {
    BLANK,
    TANDOOR,
    BOOTSTRAP,
    DARKLY,
    FLATLY,
    SUPERHERO,
    TANDOOR_DARK,
}

@Serializable
enum class TandoorSpaceNavTextColor {
    BLANK,
    LIGHT,
    DARK,
}

@Serializable
data class TandoorSpaceFoodInheritField(
    val id: Int,
    val name: String? = null,
    val field: String? = null,
)

@Serializable
data class TandoorSpaceCustomTheme(
    val id: Int,
    val name: String,
    val nav_bg_color: String? = null,
    val nav_text_color: TandoorSpaceNavTextColor? = null,
)

@Serializable
data class PartialTandoorSpace(
    val name: String? = null,
    val message: String? = null,
    val food_inherit: List<TandoorSpaceFoodInheritField>? = null,
    val image: TandoorImage? = null,
    val nav_logo: TandoorImage? = null,
    val space_theme: TandoorSpaceTheme? = null,
    val custom_space_theme: TandoorSpaceCustomTheme? = null,
    val nav_bg_color: String? = null,
    val nav_text_color: TandoorSpaceNavTextColor? = null,
    val ai_default_provider: TandoorAIProvider? = null,
    val ai_image_recipe_provider: TandoorAIProvider? = null,
    val ai_credits_monthly: Int? = null,
    val ai_credits_balance: Double? = null,
    val ai_enabled: Boolean? = null,
    val space_setup_completed: Boolean? = null,
    val household_setup_completed: Boolean? = null,
)

@Serializable
data class TandoorSpace(
    val id: Int,
    val name: String,
    val created_by: TandoorUser? = null,
    val created_at: String? = null,
    val message: String = "",
    val max_recipes: Int = 0,
    val max_file_storage_mb: Int = 0,
    val max_users: Int = 0,
    val allow_sharing: Boolean = true,
    val demo: Boolean = false,
    val food_inherit: List<TandoorSpaceFoodInheritField> = emptyList(),
    val user_count: Int = 0,
    val recipe_count: Int = 0,
    val file_size_mb: Double = 0.0,
    val image: TandoorImage? = null,
    val nav_logo: TandoorImage? = null,
    val space_theme: TandoorSpaceTheme = TandoorSpaceTheme.BLANK,
    val custom_space_theme: TandoorSpaceCustomTheme? = null,
    val nav_bg_color: String = "",
    val nav_text_color: TandoorSpaceNavTextColor = TandoorSpaceNavTextColor.BLANK,
    val logo_color_32: TandoorImage? = null,
    val logo_color_128: TandoorImage? = null,
    val logo_color_144: TandoorImage? = null,
    val logo_color_180: TandoorImage? = null,
    val logo_color_192: TandoorImage? = null,
    val logo_color_512: TandoorImage? = null,
    val logo_color_svg: TandoorImage? = null,
    val ai_credits_monthly: Int = 0,
    val ai_credits_balance: Double = 0.0,
    val ai_monthly_credits_used: Int = 0,
    val ai_enabled: Boolean = false,
    val ai_default_provider: TandoorAIProvider? = null,
    val ai_image_recipe_provider: TandoorAIProvider? = null,
    val space_setup_completed: Boolean = false,
    val household_setup_completed: Boolean = false,
)
