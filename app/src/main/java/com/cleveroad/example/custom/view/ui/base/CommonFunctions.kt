package com.cleveroad.example.custom.view.ui.base


inline fun <reified T> bindInterfaceOrThrow(vararg objects: Any?): T = objects.find { it is T }
        ?.let { it as T }
        ?: throw NotImplementedInterfaceException(T::class.java)