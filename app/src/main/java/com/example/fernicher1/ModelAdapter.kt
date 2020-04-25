package com.example.fernicher1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_model.view.*

class ModelAdapter (val models:List<Model>):RecyclerView.Adapter<ModelAdapter.ModelViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val inflator=LayoutInflater.from(parent.context)
        val view=inflator.inflate(R.layout.item_model,parent,false)
        return ModelViewHolder(view)
    }

    override fun getItemCount()=models.size

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
       holder.view.apply {
           ivThmbait.setImageResource(models[position].imageResourceId)
           tvTitle.text=models[position].title
       }
    }
    class ModelViewHolder(val view: View):RecyclerView.ViewHolder(view)
}