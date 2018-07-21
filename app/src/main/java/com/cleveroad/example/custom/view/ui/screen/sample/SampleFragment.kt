package com.cleveroad.example.custom.view.ui.screen.sample

import android.os.Bundle
import com.cleveroad.example.custom.view.R
import com.cleveroad.example.custom.view.ui.base.BaseFragment


class SampleFragment : BaseFragment() {

    companion object {
        fun newInstance() = SampleFragment().apply {
            arguments = Bundle()
        }
    }

    override val layoutId = R.layout.fragment_sample

}
