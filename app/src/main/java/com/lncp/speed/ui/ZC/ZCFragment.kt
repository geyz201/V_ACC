package com.lncp.speed.ui.ZC

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import com.lncp.speed.R
import kotlinx.android.synthetic.main.fragment_zc.*

class ZCFragment : Fragment() {

    private val model: ZCViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_zc, container, false)

        model.Ending.observe(viewLifecycleOwner, Observer<Boolean> {
            if (it) {
                text_zc.visibility = View.INVISIBLE
                Btn_zc.visibility = View.VISIBLE
            }
        })

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Btn_zc.setOnClickListener {
            Btn_zc.visibility = View.INVISIBLE
            text_zc.visibility = View.VISIBLE
            model.ZeroCalibration()
        }
    }
}