package com.shashi.blogmob.otheruser

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashi.blogmob.R
import com.shashi.blogmob.model.BlogPostModel
import de.hdodenhof.circleimageview.CircleImageView

class OpenAccount : AppCompatActivity() {

    private lateinit var blogItems: ArrayList<BlogPostModel>
    private lateinit var blogId: ArrayList<String>
    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: OtherAccountAdapter

    private lateinit var lastVisible: DocumentSnapshot

    //True only for the first time we load the data
    private var isFirestPageFirstLoad: Boolean = true

    private lateinit var firebaseFirestore: FirebaseFirestore
    private val COLLECTION_NAME_USERS = "users"
    private val COLLECTION_NAME_BLOGS = "blogs"

    private lateinit var profileImageHeader: CircleImageView
    private lateinit var textViewNameHeader: TextView

    lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user)

        userId = intent.getStringExtra("userId")!!

        firebaseFirestore = FirebaseFirestore.getInstance()

        initViews()

        updateUI(userId)
        getData()

    }

    private fun initViews() {
        profileImageHeader = findViewById(R.id.circle_image_view_header_other_user)
        textViewNameHeader = findViewById(R.id.text_view_name_header_other_user)

        blogItems = ArrayList()
        blogId = ArrayList()
        blogAdapter = OtherAccountAdapter()

        recyclerView = findViewById(R.id.recycler_view_other_user)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = blogAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val isReachedBottom: Boolean = !recyclerView.canScrollVertically(1)
                if (isReachedBottom) {
                    loadMorePost()
                }

            }
        })

    }

    private fun updateUI(userId: String) {

        firebaseFirestore
            .collection(COLLECTION_NAME_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot -> //Check if the document exists
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

        textViewNameHeader.text = userName

        Glide.with(this)
            .load(imageUrl)
            .into(profileImageHeader)

    }

    private fun getData() {

        val firstQuery: Query = firebaseFirestore
            .collection(COLLECTION_NAME_BLOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)

        firstQuery
            .addSnapshotListener { value, error ->

                if (isFirestPageFirstLoad) {
                    lastVisible = value!!.documents[value.size() - 1]
                }

                for (document in value!!.documentChanges) {
                    if (document.type == DocumentChange.Type.ADDED) {

                        val blogPostModel: BlogPostModel =
                            document.document.toObject(BlogPostModel::class.java)
                        val blogDocumentId: String = document.document.id

                        if (blogPostModel.user_id == userId) {

                            if (isFirestPageFirstLoad) {
                                blogItems.add(blogPostModel)
                                blogId.add(blogDocumentId)
                            } else {
                                blogItems.add(0, blogPostModel)
                                blogId.add(0, blogDocumentId)
                            }

                        }

                    }

                    blogAdapter.updateBlogList(blogItems, blogId)
                }

                isFirestPageFirstLoad = false
            }

    }

    private fun loadMorePost() {

        val newQuery: Query = firebaseFirestore
            .collection(COLLECTION_NAME_BLOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisible)
            .limit(3)

        newQuery
            .addSnapshotListener { value, error ->

                if (!value!!.isEmpty) {

                    lastVisible = value.documents[value.size() - 1]

                    for (document in value.documentChanges) {
                        if (document.type == DocumentChange.Type.ADDED) {
                            val blogPostModel: BlogPostModel =
                                document.document.toObject(BlogPostModel::class.java)
                            val blogDocumentId: String = document.document.id

                            if (blogPostModel.user_id == userId) {
                                blogItems.add(blogPostModel)
                                blogId.add(blogDocumentId)
                            }
                        }

                        blogAdapter.updateBlogList(blogItems, blogId)
                    }

                }
            }

    }

}