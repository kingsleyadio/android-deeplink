package com.kingsleyadio.deeplink

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeepLinkParserTest {

    // DeepLinkParser is immutable, so the same instance can be shared across tests
    private val parser = DeepLinkParser.of<String>(EmptyEnvironment)
        .addRoute(RecipeRoute)
        .addRoute(SubscriptionRoute)
        .addFallbackAction(FallbackAction)
        .build()

    @Test
    fun newParser_NoFallback_ThrowsException() {
        assertFailsWith<IllegalStateException> {
            DeepLinkParser.of<String>(EmptyEnvironment)
                .addRoute(RecipeRoute)
                .build()
        }
    }

    @Test
    fun parseSimple() {
        assertEquals("RecipeRoute", parser.parse(DeepLinkUri.parse("http://world.com/recipes")))
    }

    @Test
    fun parseWithParam() {
        assertEquals("1234", parser.parse(DeepLinkUri.parse("http://world.com/recipe/1234")))
    }

    @Test
    fun parseWithNextRouter() {
        assertEquals("SubscriptionRoute", parser.parse(DeepLinkUri.parse("example://host/subscription")))
    }

    @Test
    fun parseWithConflictingRouter() {
        // RecipeRoute registered first
        var parserWithConflict = DeepLinkParser.of<String>(EmptyEnvironment)
            .addRoute(RecipeRoute)
            .addRoute(ConflictingRecipeRoute)
            .addFallbackAction(FallbackAction)
            .build()
        assertEquals("RecipeRoute", parserWithConflict.parse(DeepLinkUri.parse("http://world.com/recipes")))

        // ConflictingRoute registered first
        parserWithConflict = DeepLinkParser.of<String>(EmptyEnvironment)
            .addRoute(ConflictingRecipeRoute)
            .addRoute(RecipeRoute)
            .addFallbackAction(FallbackAction)
            .build()
        assertEquals("Conflict", parserWithConflict.parse(DeepLinkUri.parse("http://world.com/recipes")))
    }

    @Test
    fun parseFallback() {
        assertEquals("Fallback", parser.parse(DeepLinkUri.parse("http://world.com/unknown")))
    }
}
