package com.lncp.speed.ui.ZC

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lncp.speed.R
import kotlinx.android.synthetic.main.fragment_zc.*
import threeDvector.Vec3D

class ZCFragment : Fragment() {

    private lateinit var ZCViewModel: ZCViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        ZCViewModel =
                ViewModelProviders.of(this).get(ZCViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_zc, container, false)
        text_zc.visibility=View.INVISIBLE
        ZCViewModel.app=requireActivity()
        ZCViewModel.Ending.observe(viewLifecycleOwner,Observer<Boolean>{
            if(it){
                text_zc.visibility=View.INVISIBLE
                Btn_zc.visibility=View.VISIBLE
            }
        })
        Btn_zc.setOnClickListener{
            text_zc.visibility=View.VISIBLE
            Btn_zc.visibility=View.INVISIBLE
            ZCViewModel.ZeroCalibration()
        }
        return root
    }
}