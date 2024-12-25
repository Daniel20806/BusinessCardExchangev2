package com.example.businesscardexchange

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.entity.ContentLengthStrategy
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.protocol.RequestContent

class CardListAdapter(private val cardDataList: List<CardData>, private val fragment: CardListFragment) :
    RecyclerView.Adapter<CardListAdapter.ViewHolder>() {

        /*ViewHolder*/
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: ImageView = itemView.findViewById(R.id.photoImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val companyTextView: TextView = itemView.findViewById(R.id.companyTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardData = cardDataList[position]
        holder.nameTextView.text = cardData.name
        holder.companyTextView.text = cardData.company
        holder.phoneTextView.text = cardData.phone
        holder.emailTextView.text = cardData.email

        // 解码 Base64 图片数据并显示在 ImageView 上
        if (cardData.photo.isNotEmpty()) {
            val decodedBytes = Base64.decode(cardData.photo, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            holder.photoImageView.setImageBitmap(bitmap) // 将 Bitmap 设置到 ImageView 上
        }


        /*設定點擊事件*/
        holder.itemView.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
                .setTitle("選擇操作")
                .setItems(arrayOf("修改", "刪除")) { _, which ->
                    when (which) {
                        0 -> fragment.editCardData(position) // 修改操作
                        1 -> fragment.removeCardData(position) // 删除操作
                    }
                }

            builder.create().show()
        }
    }

    override fun getItemCount(): Int {
        return cardDataList.size
    }

}