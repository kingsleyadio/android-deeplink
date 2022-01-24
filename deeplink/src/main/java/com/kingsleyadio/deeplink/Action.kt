package com.kingsleyadio.deeplink

fun interface Action<out T> {

    fun run(uri: DeepLinkUri, params: Map<String, String>, env: Environment): T
}
