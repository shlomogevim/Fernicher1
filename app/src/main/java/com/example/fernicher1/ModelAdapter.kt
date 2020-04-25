package com.example.fernicher1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_model.view.*

const val SELECTED_MODEL_COLOR = Color.YELLOW
const val UNSELECTED_MODEL_COLOR = Color.LTGRAY

class ModelAdapter(val models: List<Model>) : RecyclerView.Adapter<ModelAdapter.ModelViewHolder>() {


    var selectedModl = MutableLiveData<Model>()
    private var selectedModelIndex = 0
    private var modelViewHolders = mutableListOf<ModelViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val view = inflator.inflate(R.layout.item_model, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount() = models.size

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        if (!modelViewHolders.contains(holder)) {
            modelViewHolders.add(holder)
        }
        holder.view.apply {
            ivThmbait.setImageResource(models[position].imageResourceId)
            tvTitle.text = models[position].title
            if (selectedModelIndex==position){
                setBackgroundColor(SELECTED_MODEL_COLOR)
                selectedModl.value=models[position]
            }
            setOnClickListener {
                selectModelPosition(position)
            }
        }
    }

    private fun selectModelPosition(position: Int) {
        modelViewHolders[selectedModelIndex].view.setBackgroundColor(UNSELECTED_MODEL_COLOR)
        selectedModelIndex = position
        modelViewHolders[selectedModelIndex].view.setBackgroundColor(SELECTED_MODEL_COLOR)
        selectedModl.value = models[selectedModelIndex]
    }

    class ModelViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}