package com.cleveroad.example.custom.view.ui.screen

import android.os.Bundle
import com.cleveroad.example.custom.view.R
import com.cleveroad.example.custom.view.ui.base.BaseActivity
import com.cleveroad.example.custom.view.ui.screen.sample.SampleFragment


class MainActivity : BaseActivity(){

    override val containerId = R.id.container
    override val layoutId = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) showChooseScreen()
    }

    private fun showChooseScreen() {
        replaceFragment(SampleFragment.newInstance(), false)
    }
}
