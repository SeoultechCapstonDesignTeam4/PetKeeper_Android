package com.example.petkeeper.util.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.petkeeper.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.like.LikeButton

data class Data(
    val POST_ID: Int,
    val POSTED_USER_IMAGE: String,
    val POSTED_USER_EMAIL: String,
    val POST_CONTENT: String,
    val POST_IMAGE: String,
    var LikesCount: Int = 0,
    var CommentsCount: Int = 0,
    var IsLikedByCurrentUser: Boolean = false
)

class CommunityAdapter(private val context: Context, private val items: ArrayList<Data>): RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.community_item, parent, false)
        return ViewHolder(inflatedView)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        val POSTED_USER_EMAIL = view.findViewById<TextView>(R.id.userName)
        val POST_COMMENTS = view.findViewById<TextView>(R.id.comment)
        val POSTED_USER_IMAGE = view.findViewById<ImageView>(R.id.profileImage)
        val POST_IMAGE = view.findViewById<ImageView>(R.id.image)
//        val LIKE_COUNT = view.findViewById<TextView>(R.id.likeCount)
        val POST_CONTENT = view.findViewById<TextView>(R.id.content)
        val likeButton = view.findViewById<LikeButton>(R.id.likeButton)
        val commentButton = view.findViewById<TextView>(R.id.comment)

        init {
            commentButton.setOnClickListener {
                Log.d("testtt", "dialog")
                val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog, null)
                val bottomSheetDialog = BottomSheetDialog(context)

                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
////                    val data = dataList[position]
//
//                }
            }
        }

        fun bind(item: Data) {
            POSTED_USER_EMAIL.text = item.POSTED_USER_EMAIL
            POST_COMMENTS.text = "댓글 "+item.CommentsCount+"개 모두 보기"
//            LIKE_COUNT.text = item.LikesCount.toString()
            POST_CONTENT.text=  item.POST_CONTENT
            likeButton.isLiked = item.IsLikedByCurrentUser

            Glide.with(view)
                .load(item.POST_IMAGE)
                .into(POST_IMAGE)
            Glide.with(view)
                .load(item.POSTED_USER_IMAGE)
                .into(POSTED_USER_IMAGE)

            likeButton.setOnClickListener {
                likeButton.isLiked = !likeButton.isLiked
            }
        }
    }
}