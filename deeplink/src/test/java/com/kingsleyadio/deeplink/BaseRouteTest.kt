package com.kingsleyadio.deeplink

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BaseRouteTest {

    @Test
    fun matchWith_pathVariations() {
        var uri = DeepLinkUri.parse("http://www.example.com/recipes")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/x")
        assertFalse(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe/1234")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe/")
        assertFalse(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://host/recipes")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://host/recipes/")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://host/recipes/x")
        assertFalse(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://host/recipe/1234")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://host/recipe/")
        assertFalse(TestRoute.matchWith(uri).isMatch)
    }

    @Test
    fun matchWith_pathVariationsWithOverride() {
        var uri = DeepLinkUri.parse("example://recipes")
        assertTrue(PathOverrideRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://recipes/")
        assertTrue(PathOverrideRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://recipes/x")
        assertFalse(PathOverrideRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://recipe/1234")
        assertTrue(PathOverrideRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("example://recipe/")
        assertFalse(PathOverrideRoute.matchWith(uri).isMatch)
    }

    @Test
    fun matchWith_pathVariationsWithNamelessParameter() {
        var uri = DeepLinkUri.parse("http://www.example.com/recipes/x/1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes//1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/")
        assertFalse(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes//")
        assertFalse(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe/x")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe/1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe/")
        assertFalse(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipe")
        assertFalse(NamelessPathRoute.matchWith(uri).isMatch)
    }

    @Test
    fun matchWith_InputWithOnlyPathParams_ReturnsPathData() {
        val uri = DeepLinkUri.parse("http://www.example.com/recipe/1234")
        val params = TestRoute.matchWith(uri).params

        assertEquals(1, params.size)
        assertEquals("1234", params["id"])
    }

    @Test
    fun matchWith_DefaultPathResolution() {
        val uri = DeepLinkUri.parse("http://www.example.com/recipe/1234")
        assertTrue(TestRoute.matchWith(uri).isMatch)

        val customUriWithHost = DeepLinkUri.parse("example://host/recipe/1234")
        assertTrue(TestRoute.matchWith(customUriWithHost).isMatch)

        val customUriNoHost = DeepLinkUri.parse("example://recipe/1234")
        assertFalse(TestRoute.matchWith(customUriNoHost).isMatch)
    }

    @Test
    fun matchWith_OverridePathResolution() {
        val uri = DeepLinkUri.parse("http://www.example.com/recipe/1234")
        assertTrue(PathOverrideRoute.matchWith(uri).isMatch)

        val customUriWithHost = DeepLinkUri.parse("example://host/recipe/1234")
        assertFalse(PathOverrideRoute.matchWith(customUriWithHost).isMatch)

        val customUriNoHost = DeepLinkUri.parse("example://recipe/1234")
        assertTrue(PathOverrideRoute.matchWith(customUriNoHost).isMatch)
    }

    @Test
    fun matchWith_namelessPathResolution() {
        var uri = DeepLinkUri.parse("http://www.example.com/recipes/me/1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/customer-key/1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/anything/1234")
        assertTrue(NamelessPathRoute.matchWith(uri).isMatch)
    }

    @Test
    fun matchWith_regexPathResolution() {
        var uri = DeepLinkUri.parse("http://www.example.com/recipes/detail/abc-1234")
        var res = RegexPathRoute.matchWith(uri)
        assertTrue(res.isMatch)
        assertEquals("detail", res.params["action"])
        assertEquals("abc-1234", res.params["id"])

        uri = DeepLinkUri.parse("http://www.example.com/recipes/info/abc-1234")
        res = RegexPathRoute.matchWith(uri)
        assertTrue(res.isMatch)
        assertEquals("info", res.params["action"])
        assertEquals("abc-1234", res.params["id"])

        // action does not match
        uri = DeepLinkUri.parse("http://www.example.com/recipes/invalid/abc-1234")
        assertFalse(RegexPathRoute.matchWith(uri).isMatch)

        // id does not match
        uri = DeepLinkUri.parse("http://www.example.com/recipes/detail/1234")
        assertFalse(RegexPathRoute.matchWith(uri).isMatch)
    }

    @Test
    fun matchWith_regexPathResolution_equalNamedPaths() {
        var uri = DeepLinkUri.parse("http://www.example.com/recipes/1234/1234")
        var res = EqualNamedPathRoute.matchWith(uri)
        assertTrue(res.isMatch)
        assertEquals(1, res.params.size)

        uri = DeepLinkUri.parse("http://www.example.com/recipes/1234/5678")
        res = EqualNamedPathRoute.matchWith(uri)
        assertFalse(res.isMatch)
    }

    @Test
    fun matchWith_regexPathResolutionUnnamed() {
        val uri = DeepLinkUri.parse("http://www.example.com/recipe/abc-1234")
        val res = UnnamedRegexPathRoute.matchWith(uri)
        assertTrue(res.isMatch)
        assertTrue(res.params.isEmpty())
    }

    object TestRoute : BaseRoute<Unit>("recipes", "recipe/:id") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit
    }

    object PathOverrideRoute : BaseRoute<Unit>("recipes", "recipe/:id") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit

        override fun treatHostAsPath(uri: DeepLinkUri): Boolean {
            return uri.scheme() == "example"
        }
    }

    object NamelessPathRoute : BaseRoute<Unit>("recipe/*", "recipes/*/:id") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit
    }

    object RegexPathRoute : BaseRoute<Unit>("recipes/:action(detail|info)/:id(.*-\\w+)") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit
    }

    object UnnamedRegexPathRoute : BaseRoute<Unit>("recipe/:(.*-\\w+)") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit
    }

    object EqualNamedPathRoute : BaseRoute<Unit>("recipes/:id(\\w{4})/:id") {

        override fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment) = Unit
    }
}
