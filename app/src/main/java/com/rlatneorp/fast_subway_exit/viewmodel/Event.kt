package com.rlatneorp.fast_subway_exit.viewmodel

open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        if (hasBeenHandled) {
            return null
        }

        hasBeenHandled = true
        return content
    }
}