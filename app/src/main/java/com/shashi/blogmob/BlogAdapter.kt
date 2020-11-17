package com.shashi.blogmob

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.shashi.blogmob.model.BlogPostModel
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class BlogAdapter : RecyclerView.Adapter<BlogViewHolder>() {

    private val blogItems: ArrayList<BlogPostModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.blog_list_item, parent, false)
        val viewHolder = BlogViewHolder(view)

        return viewHolder
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val description = blogItems.get(position).desc
        val image_url = blogItems.get(position).image_url
        val userId = blogItems.get(position).user_id

        try {
            val timestamp: Long = blogItems.get(position).timestamp!!.seconds
            holder.blogTime.text = getDate(timestamp)
        } catch (e: Exception) {
            holder.blogTime.text = ""
        }

        holder.blogDescription.text = description
        Glide.with(holder.itemView.context).load(image_url).into(holder.blogImageView)

        holder.showUserName(userId)
    }

    override fun getItemCount(): Int {
        return blogItems.size
    }

    fun updateBlogList(updatedNews: ArrayList<BlogPostModel>) {
        blogItems.clear()
        blogItems.addAll(updatedNews)

        notifyDataSetChanged()
    }

    private fun getDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy", calendar).toString()
    }
}

class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val userProfileImage: CircleImageView = itemView.findViewById(R.id.circle_image_view_blog_item)
    val userNameTextView: TextView = itemView.findViewById(R.id.text_View_username_blog_item)
    val blogTime: TextView = itemView.findViewById(R.id.text_View_time_blog_item)
    val blogImageView: ImageView = itemView.findViewById(R.id.image_view_blog_item)
    val blogDescription: TextView = itemView.findViewById(R.id.text_View_description_blog_item)

    fun showUserName(userId: String) {

        val firebaseFirestore = FirebaseFirestore.getInstance()

        firebaseFirestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    var userName = documentSnapshot.getString("name")
                    var imageUrl = documentSnapshot.getString("image")

                    if (userName!!.isEmpty()) {
                        userName = ""
                    }
                    if (imageUrl!!.isEmpty()) {
                        imageUrl = ""
                    }

                    showData(userName, imageUrl)
                }
            }

    }

    private fun showData(userName: String, imageUrl: String) {

        userNameTextView.text = userName

        val placeolderRequest = RequestOptions()
        placeolderRequest.placeholder(R.drawable.image_placeholder_new_post)

        Glide.with(itemView.context)
            .setDefaultRequestOptions(placeolderRequest)
            .load(imageUrl)
            .into(userProfileImage)
    }

}
