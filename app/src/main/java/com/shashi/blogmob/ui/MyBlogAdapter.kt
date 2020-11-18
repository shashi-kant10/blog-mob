package com.shashi.blogmob.ui

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shashi.blogmob.R
import com.shashi.blogmob.model.BlogPostModel
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class MyBlogAdapter : RecyclerView.Adapter<MyBlogViewHolder>() {

    private val blogItems: ArrayList<BlogPostModel> = ArrayList()
    private val blogId: ArrayList<String> = ArrayList()

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUserId: String

    private lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBlogViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.my_blog_list_item, parent, false)
        val viewHolder = MyBlogViewHolder(view)

        mContext = parent.context
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        currentUserId = firebaseAuth.currentUser!!.uid

        return viewHolder
    }

    override fun onBindViewHolder(holder: MyBlogViewHolder, position: Int) {
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

        val currentBlogId = blogId.get(position)

        //Get likes count
        firebaseFirestore
            .collection("blogs/" + currentBlogId + "/likes")
            .addSnapshotListener { snapshot, e ->
                if (!snapshot!!.isEmpty) {
                    val count: Int = snapshot.size()
                    holder.updateLikeCount(count)
                } else
                    holder.updateLikeCount(0)
            }

        //Get like by user
        firebaseFirestore
            .collection("blogs/" + currentBlogId + "/likes")
            .document(currentUserId)
            .addSnapshotListener { snapshot, e ->

                if (snapshot!!.exists())
                    holder.blogLike.setImageDrawable(mContext.getDrawable(R.drawable.icon_like_red))
                else
                    holder.blogLike.setImageDrawable(mContext.getDrawable(R.drawable.icon_like_grey))

            }

        //Click to like and dislike
        holder.blogLike.setOnClickListener {

            firebaseFirestore
                .collection("blogs/" + currentBlogId + "/likes")
                .document(currentUserId)
                .get()
                .addOnCompleteListener {

                    if (!it.result!!.exists()) {

                        //If like does not exist, then like
                        val mapLike: MutableMap<String, Any> = HashMap()
                        mapLike["timestamp"] = FieldValue.serverTimestamp()

                        firebaseFirestore
                            .collection("blogs/" + currentBlogId + "/likes")
                            .document(currentUserId)
                            .set(mapLike)

                    } else {

                        //If like exists, then dislike
                        firebaseFirestore
                            .collection("blogs/" + currentBlogId + "/likes")
                            .document(currentUserId)
                            .delete()

                    }

                }
        }

    }

    override fun getItemCount(): Int {
        return blogItems.size
    }

    fun updateBlogList(updatedBlogList: ArrayList<BlogPostModel>, updatedId: ArrayList<String>) {
        blogItems.clear()
        blogItems.addAll(updatedBlogList)

        blogId.clear()
        blogId.addAll(updatedId)

        notifyDataSetChanged()
    }

    private fun getDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy", calendar).toString()
    }
}

class MyBlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val userProfileImage: CircleImageView = itemView.findViewById(R.id.circle_image_view_my_blog)
    val userNameTextView: TextView = itemView.findViewById(R.id.text_View_username_my_blog)
    val blogTime: TextView = itemView.findViewById(R.id.text_View_time_my_blog)
    val blogImageView: ImageView = itemView.findViewById(R.id.image_view_my_blog)
    val blogDescription: TextView = itemView.findViewById(R.id.text_View_description_my_blog)
    val blogLike: ImageView = itemView.findViewById(R.id.image_view_like_my_blog)
    val blogLikeCount: TextView = itemView.findViewById(R.id.text_view_like_count_my_blog)

    fun showUserName(userId: String) {

        val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    fun updateLikeCount(count: Int) {

        val countText: String

        if (count == 0 || count == 1)
            countText = "$count like"
        else
            countText = "$count likes"

        blogLikeCount.text = countText
    }

}
