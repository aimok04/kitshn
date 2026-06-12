package de.kitshn.api.tandoor.model

import kotlinx.serialization.json.JsonArray
import kotlin.test.Test
import kotlin.test.assertEquals

class TandoorStepTemplatingTest {

    private fun createStep(instruction: String, ingredients: List<TandoorIngredient>): TandoorStep {
        return TandoorStep(
            id = 1,
            name = "Test Step",
            instruction = instruction,
            ingredientsRaw = JsonArray(emptyList()),
            time = 0,
            order = 1,
            show_as_header = false
        ).apply {
            this.ingredients = ingredients.toMutableList()
        }
    }

    private fun createIngredient(amount: Double): TandoorIngredient {
        return TandoorIngredient(
            id = 1,
            amount = amount,
            order = 1,
            is_header = false,
            no_amount = false
        )
    }

    @Test
    fun testBasicScaling() {
        val step = createStep("Scale 100: {{ scale(100) }}", emptyList())
        assertEquals("Scale 100: 100", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Scale 100: 200", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testExpressionScaling() {
        val step = createStep("Expr: {{ scale((10 + 20) * 2) }}", emptyList())
        assertEquals("Expr: 60", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Expr: 120", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testNumericAmountStandalone() {
        val ingredient = createIngredient(amount = 50.0)
        val step = createStep("Raw: {{ ingredients[0].numeric_amount }}", listOf(ingredient))
        assertEquals("Raw: 50.0", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Raw: 50.0", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testNumericAmountInScale() {
        val ingredient = createIngredient(amount = 100.0)
        val step = createStep("Half: {{ scale(ingredients[0].numeric_amount / 2) }}", listOf(ingredient))
        assertEquals("Half: 50", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Half: 100", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testMultipleTemplates() {
        val i1 = createIngredient(amount = 10.0)
        val i2 = createIngredient(amount = 20.0)
        val step = createStep("Sum: {{ scale(ingredients[0].numeric_amount + ingredients[1].numeric_amount) }}", listOf(i1, i2))
        assertEquals("Sum: 30", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Sum: 60", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testInvalidExpression() {
        val step = createStep("Invalid: {{ scale(10 + ) }}", emptyList())
        assertEquals("Invalid: Invalid scale template", step.applyTemplating(scale = 1.0, fractional = false))
    }

    @Test
    fun testFractionalScaling() {
        val step = createStep("Half: {{ scale(0.5) }}", emptyList())
        assertEquals("Half: ½", step.applyTemplating(scale = 1.0, fractional = true))
    }

    @Test
    fun testComplexExpressionsWithIngredients() {
        val i1 = createIngredient(amount = 100.0)
        val i2 = createIngredient(amount = 50.0)
        val step = createStep("Complex: {{ scale((ingredients[0].numeric_amount + ingredients[1].numeric_amount) / 3 * 2) }}", listOf(i1, i2))
        // (100 + 50) / 3 * 2 = 150 / 3 * 2 = 50 * 2 = 100
        assertEquals("Complex: 100", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Complex: 200", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testNegativeAndZero() {
        val step = createStep("Zero: {{ scale(0) }}, Neg: {{ scale(-10) }}", emptyList())
        assertEquals("Zero: 0, Neg: -10", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Zero: 0, Neg: -20", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testMixedScalingAndNumericAmount() {
        val i1 = createIngredient(amount = 12.5)
        val step = createStep("Value: {{ ingredients[0].numeric_amount }} vs {{ scale(ingredients[0].numeric_amount) }}", listOf(i1))
        assertEquals("Value: 12.5 vs 12.5", step.applyTemplating(scale = 1.0, fractional = false))
        assertEquals("Value: 12.5 vs 25", step.applyTemplating(scale = 2.0, fractional = false))
    }

    @Test
    fun testSpacingInExpressions() {
        val step = createStep("Space: {{scale( ( 10+20 ) *2 )}}", emptyList())
        assertEquals("Space: 60", step.applyTemplating(scale = 1.0, fractional = false))
    }

    @Test
    fun testVariedAmountsAndScales() {
        val i1 = createIngredient(amount = 0.5)
        val step = createStep("Amount: {{ scale(ingredients[0].numeric_amount) }}", listOf(i1))
        
        // Scale 1.0, fractional
        assertEquals("Amount: ½", step.applyTemplating(scale = 1.0, fractional = true))
        // Scale 1.0, non-fractional
        assertEquals("Amount: 0.5", step.applyTemplating(scale = 1.0, fractional = false))
        
        // Scale 0.5, fractional
        assertEquals("Amount: ¼", step.applyTemplating(scale = 0.5, fractional = true))
        // Scale 0.5, non-fractional
        assertEquals("Amount: 0.25", step.applyTemplating(scale = 0.5, fractional = false))

        // Scale 3.0, fractional
        assertEquals("Amount: 1 ½", step.applyTemplating(scale = 3.0, fractional = true))
        // Scale 3.0, non-fractional
        assertEquals("Amount: 1.5", step.applyTemplating(scale = 3.0, fractional = false))
    }

    @Test
    fun testMathCombinations() {
        val step = createStep("Math: {{ scale(1 + 2 * 3) }}", emptyList())
        // 1 + (2 * 3) = 7
        assertEquals("Math: 7", step.applyTemplating(scale = 1.0, fractional = false))
        
        val step2 = createStep("Math: {{ scale((1 + 2) * 3) }}", emptyList())
        // (1 + 2) * 3 = 9
        assertEquals("Math: 9", step2.applyTemplating(scale = 1.0, fractional = false))
    }
}
