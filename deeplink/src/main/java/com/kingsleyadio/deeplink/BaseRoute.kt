package com.kingsleyadio.deeplink

import java.util.regex.Pattern

abstract class BaseRoute<out T>(private vararg val routes: String) : Action<T> {

    internal fun matchWith(uri: DeepLinkUri): MatchResult {
        val (_, inputParts) = retrieveHostAndPathSegments(uri)
        routes.forEach { route ->
            val params = hashMapOf<String, String>()

            val parts = route.split("/").dropLastWhile { it.isEmpty() }
            if (inputParts.size != parts.size) {
                return@forEach
            }
            inputParts.zip(parts) { inPart, routePart ->
                when {
                    routePart.startsWith(":") -> {
                        val (key, value) = resolveParameterizedPath(routePart, inPart) ?: return@forEach
                        key.ifEmpty { return@zip }
                        if (key in params && params[key] != value) return@forEach
                        else params[key] = value
                    }
                    routePart == "*" -> return@zip
                    routePart != inPart -> return@forEach
                }
            }
            return MatchResult(true, params)
        }
        return MatchResult(false)
    }

    private fun retrieveHostAndPathSegments(uri: DeepLinkUri): Pair<String, List<String>> {
        val trimmedPathSegments = uri.pathSegments().dropLastWhile { it.isEmpty() }
        if (treatHostAsPath(uri)) {
            val pathSegments = ArrayList<String>(uri.pathSize() + 1)
            pathSegments.add(uri.host())
            pathSegments.addAll(trimmedPathSegments)
            return "" to pathSegments
        }
        return uri.host() to trimmedPathSegments
    }

    private fun resolveParameterizedPath(routePart: String, inPart: String): Pair<String, String>? {
        val partialMatcher = PARAMETERIZED_PATH_PATTERN.matcher(routePart)
        if (!partialMatcher.matches()) return null

        val key = partialMatcher.group(1).orEmpty()
        val userPattern = partialMatcher.group(3) ?: return Pair(key, inPart)
        val inputMatcher = Pattern.compile(userPattern).matcher(inPart)
        if (!inputMatcher.matches()) return null

        return Pair(key, inPart)
    }

    /**
     * Returns whether or not to treat the [uri] host as part of the path segments.
     *
     * This is useful for URIs with custom schemes that do not have an explicit
     * host, but rather uses the scheme as the deeplink identifier. In such cases,
     * one might prefer to treat the host itself as a path segment.
     */
    protected open fun treatHostAsPath(uri: DeepLinkUri): Boolean {
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseRoute<*>) return false

        return routes.contentEquals(other.routes)
    }

    override fun hashCode(): Int {
        return routes.contentHashCode()
    }

    companion object {

        /**
         * Regex pattern that will be used to validate and retrieve path values from a specified input.
         *
         * This pattern is made up of 3 groups.
         * Group 1: An optional parameter name that will be used to reference the actual path value
         * Group 2: The entirety of the (optional) regex pattern for the path
         * Group 3: The actual regex pattern that will be supplied by the user
         */
        private val PARAMETERIZED_PATH_PATTERN = Pattern.compile("^:(\\w*)(\\((.*)\\))?$")
    }

}
