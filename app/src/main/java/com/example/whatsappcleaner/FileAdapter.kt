package com.example.whatsappcleaner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(private val fileList: List<FileItem>) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = fileList[position]
        holder.tvFileName.text = item.file.name
        
        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = item.isSelected

        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
        }
    }

    override fun getItemCount(): Int = fileList.size
}
