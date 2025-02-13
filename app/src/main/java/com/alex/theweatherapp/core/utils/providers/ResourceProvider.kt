package com.alex.theweatherapp.core.utils.providers

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

open class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    open fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}