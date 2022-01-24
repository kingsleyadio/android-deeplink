package com.kingsleyadio.deeplink.extension

import android.net.Uri
import com.kingsleyadio.deeplink.DeepLinkUri

fun DeepLinkUri.Companion.get(uri: Uri): DeepLinkUri {
    return parse(uri.toString())
}

fun DeepLinkUri.toAndroidUri(): Uri {
    return Uri.parse(toString())
}
