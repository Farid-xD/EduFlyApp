package com.example.proj.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.databinding.ItemAssetBinding
import com.example.proj.models.Asset

class SimpleAssetsAdapter(
    private var assets: List<Asset>,
    private val onItemClick: (Asset) -> Unit,
    private val onDownloadClick: (Asset) -> Unit
) : RecyclerView.Adapter<SimpleAssetsAdapter.AssetViewHolder>() {

    inner class AssetViewHolder(private val binding: ItemAssetBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(asset: Asset) {
            binding.apply {
                // Match the correct IDs from XML
                assetName.text = asset.title
                assetType.text = asset.type
                assetSize.text = asset.fileSize?.let { "${it / 1024} KB" } ?: "Unknown"
                uploadDate.text = "Uploaded: ${formatDate(asset.createdAt)}"

                // Set download count if you have it (or remove this line if not available)
                downloadCount.text = "0" // You can add downloadCount to Asset model if needed

                downloadButton.setOnClickListener {
                    onDownloadClick(asset)
                }

                viewButton.setOnClickListener {
                    onItemClick(asset)
                }

                root.setOnClickListener {
                    onItemClick(asset)
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            val days = diff / (1000 * 60 * 60 * 24)

            return when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days < 7 -> "$days days ago"
                else -> {
                    val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    date.format(java.util.Date(timestamp))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(assets[position])
    }

    override fun getItemCount(): Int = assets.size

    fun updateAssets(newAssets: List<Asset>) {
        assets = newAssets
        notifyDataSetChanged()
    }
}