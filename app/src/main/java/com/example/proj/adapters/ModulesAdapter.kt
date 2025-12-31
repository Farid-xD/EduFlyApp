package com.example.proj.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.models.CourseModule

class ModulesAdapter(
    private val modules: List<CourseModule>,
    private val onModuleClick: (CourseModule) -> Unit
) : RecyclerView.Adapter<ModulesAdapter.ModuleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_module, parent, false)
        return ModuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        val module = modules[position]
        holder.bind(module)
    }

    override fun getItemCount() = modules.size

    inner class ModuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvModuleNumber: TextView = itemView.findViewById(R.id.tvModuleNumber)
        private val tvModuleTitle: TextView = itemView.findViewById(R.id.tvModuleTitle)
        private val tvModuleDescription: TextView = itemView.findViewById(R.id.tvModuleDescription)
        private val ivCompleted: ImageView = itemView.findViewById(R.id.ivCompleted)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val module = modules[position]
                    onModuleClick(module)
                }
            }
        }

        fun bind(module: CourseModule) {
            tvModuleNumber.text = "Module ${module.moduleNumber}"
            tvModuleTitle.text = module.title
            tvModuleDescription.text = module.description

            // Show completion status
            if (module.isCompleted) {
                ivCompleted.visibility = View.VISIBLE
                itemView.alpha = 0.8f
            } else {
                ivCompleted.visibility = View.GONE
                itemView.alpha = 1.0f
            }
        }
    }
}