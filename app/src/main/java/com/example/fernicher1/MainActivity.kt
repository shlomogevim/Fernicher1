package com.example.fernicher1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*

private const val PEEK = 50f

class MainActivity : AppCompatActivity() {
     private val models= mutableListOf(
         Model(R.drawable.chair,"Chair",R.raw.chair),
         Model(R.drawable.oven,"Oven",R.raw.oven),
         Model(R.drawable.piano,"Piano",R.raw.piano),
         Model(R.drawable.table,"Table",R.raw.table)
     )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtomSheet()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        rvModels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModels.adapter = ModelAdapter(models)
    }


    private fun setupButtomSheet() {
        val buttonSheetBehaviour = BottomSheetBehavior.from(bootomSheet)
        buttonSheetBehaviour.peekHeight =  //from dp to pixel
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, PEEK, resources.displayMetrics
            ).toInt()
    }
}
