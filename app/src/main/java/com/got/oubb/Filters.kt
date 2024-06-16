package com.got.oubb

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment




class Filters :  DialogFragment() {



    interface FiltroDialogListener {
        fun onFiltroSelected(filtro: String?)
    }

    private var filtroDialogListener: FiltroDialogListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filters, container, false)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnieci = view.findViewById<Button>(R.id.btnieci)
        val cpa = view.findViewById<Button>(R.id.CPA)
        val btnici = view.findViewById<Button>(R.id.btnici)
        val btnic = view.findViewById<Button>(R.id.btnic)


            val btnHombre = view.findViewById<Button>(R.id.btnHombre)
            val btnMujer = view.findViewById<Button>(R.id.btnMujer)
            val btnAll = view.findViewById<Button>(R.id.btnAll)

             btnieci?.setOnClickListener {
                filtroDialogListener?.onFiltroSelected("ieci")
                dismiss()
            }
            cpa?.setOnClickListener {
                    filtroDialogListener?.onFiltroSelected("cpa")
                    dismiss()
                }
            btnici?.setOnClickListener {
                    filtroDialogListener?.onFiltroSelected("ici")
                    dismiss()
                }
            btnic?.setOnClickListener {
                    filtroDialogListener?.onFiltroSelected("ic")
                    dismiss()
                }

            btnHombre?.setOnClickListener {
                filtroDialogListener?.onFiltroSelected("Hombre")
                dismiss()
            }

            btnMujer?.setOnClickListener {
                filtroDialogListener?.onFiltroSelected("Mujer")
                dismiss()
            }

        btnAll.setOnClickListener{
            filtroDialogListener?.onFiltroSelected(null)
            dismiss()
        }


        }


    fun setFiltroDialogListener(listener: MainActivity) {
        this.filtroDialogListener = listener
    }



    }








