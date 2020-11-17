package com.shashi.blogmob.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shashi.blogmob.BlogAdapter
import com.shashi.blogmob.R
import com.shashi.blogmob.model.BlogPostModel

class HomeFragment : Fragment() {

    private lateinit var blogItems: ArrayList<BlogPostModel>
    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter

    private lateinit var progressBar: ProgressBar

    private lateinit var firebaseFirestore: FirebaseFirestore
    private val COLLECTION_NAME = "blogs"
    private lateinit var lastVisible: DocumentSnapshot

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseFirestore = FirebaseFirestore.getInstance()

        initViews(view)
        getData()

        return view

    }

    private fun initViews(view: View) {

        progressBar = view.findViewById(R.id.progress_bar_home)

        blogItems = ArrayList()
        blogAdapter = BlogAdapter()

        recyclerView = view.findViewById(R.id.recycler_view_home)
        recyclerView.layoutManager = LinearLayoutManager(activity)
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

    private fun getData() {

        progressBar.visibility = View.VISIBLE

        val firstQuery: Query = firebaseFirestore
            .collection(COLLECTION_NAME)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)

        firstQuery
            .addSnapshotListener { value, error ->

                lastVisible = value!!.documents[value.size() - 1]

                for (document in value.documentChanges) {
                    if (document.type == DocumentChange.Type.ADDED) {
                        val blogPostModel: BlogPostModel =
                            document.document.toObject(BlogPostModel::class.java)

                        blogItems.add(blogPostModel)
                    }

                    blogAdapter.updateBlogList(blogItems)
                }

                progressBar.visibility = View.GONE

            }

        progressBar.visibility = View.GONE

    }

    private fun loadMorePost() {

        val firstQuery: Query = firebaseFirestore
            .collection(COLLECTION_NAME)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .startAfter(lastVisible)
            .limit(3)

        firstQuery
            .addSnapshotListener { value, error ->

                if (!value!!.isEmpty) {

                    lastVisible = value.documents[value.size() - 1]

                    for (document in value.documentChanges) {
                        if (document.type == DocumentChange.Type.ADDED) {
                            val blogPostModel: BlogPostModel =
                                document.document.toObject(BlogPostModel::class.java)

                            blogItems.add(blogPostModel)
                        }

                        blogAdapter.updateBlogList(blogItems)
                    }

                }
            }

    }

}