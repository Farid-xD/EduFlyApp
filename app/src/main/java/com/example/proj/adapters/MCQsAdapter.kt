package com.example.proj.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.models.MCQQuestion



class MCQsAdapter(
    private val mcqs: List<MCQQuestion>,
    private val onMCQClick: (MCQQuestion) -> Unit
) : RecyclerView.Adapter<MCQsAdapter.MCQViewHolder>() {

    class MCQViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestionNumber: TextView = view.findViewById(R.id.tvQuestionNumber)
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvOptionsCount: TextView = view.findViewById(R.id.tvOptionsCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MCQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mcq, parent, false)
        return MCQViewHolder(view)
    }

    override fun onBindViewHolder(holder: MCQViewHolder, position: Int) {
        val mcq = mcqs[position]

        holder.tvQuestionNumber.text = "Question ${position + 1}"
        holder.tvQuestion.text = mcq.question
        holder.tvOptionsCount.text = "${mcq.options.size} options"

        holder.itemView.setOnClickListener {
            onMCQClick(mcq)
        }
    }

    override fun getItemCount() = mcqs.size
}
