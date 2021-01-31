package com.syrex.developerslife

// made by Makar Shevchenko (C)
// makarsevchenko@gmail.com

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifImageView
import com.bumptech.glide.request.target.Target
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var mGifView: GifImageView
    private lateinit var mCommentView: TextView
    private lateinit var mErrorView: TextView
    private lateinit var mErrorButton: Button
    private lateinit var mPrevButton: Button
    private var prevPost: Array<Post> = arrayOf()
    private var curPostInd: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // my code
        // place for gif
        mGifView = findViewById(R.id.gifImageView)
        // place for comment
        mCommentView = findViewById(R.id.Comment)
        // error text
        mErrorView = findViewById(R.id.ErrorText)
        // error button
        mErrorButton = findViewById(R.id.ErrorButton)
        // prev button
        mPrevButton = findViewById(R.id.Previous)
        // load gif by default
        loadRandomPost(null)
        // set inactive
        updatePrevButton(null)
    }

    // load gif and show progress circle before
    fun loadGif(url: String)
    {
        // progress circle
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        // load gif and set
        Glide
            .with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(circularProgressDrawable)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>?, p3: Boolean): Boolean {
                    showError(null)
                    Log.e("Glide","Error while loading gif")
                    return true//To change body of created functions use File | Settings | File Templates.
                }
                override fun onResourceReady(p0: Drawable?, p1: Any?, p2: Target<Drawable>?, p3: DataSource?, p4: Boolean): Boolean {
                    //do something when picture already loaded
                    hideError(null)
                    return false
                }
            })
            .into(mGifView)
    }

    // select random post
    data class Post(val description: String, val gifURL: String)
    fun loadRandomPost(view: View?)
    {
        hideError(null)
        curPostInd += 1
        if(curPostInd >= prevPost.size) // new post
        {
            val url = "https://developerslife.ru/random?json=true"

            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this)

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                Response.Listener<String> { response ->
                    try {
                        val json = Klaxon()
                            .parse<Post>(response)
                        if (json != null) {
                            // set gif and text
                            loadGif(json.gifURL)
                            mCommentView.text = json.description
                            prevPost += arrayOf(json)
                        }
                        else // error
                        {
                            curPostInd -= 1
                            showError(null)
                        }
                    } catch (e: Exception) // problem while parsing
                    {
                        curPostInd -= 1
                        showError(null);
                    };
                },
                Response.ErrorListener {
                    curPostInd -= 1
                    showError(null) }) // error

            // Add the request to the RequestQueue.
            queue.add(stringRequest)
        }
        else    // post already in cache
        {
            loadGif(prevPost[curPostInd].gifURL)
            mCommentView.text = prevPost[curPostInd].description
        }
        updatePrevButton(null)
    }

    // load previous post from cache
    fun loadPrevPost(view: View?)
    {
        hideError(null)
        if(curPostInd > 0 && curPostInd <= prevPost.size)
        {
            curPostInd -= 1
            loadGif(prevPost[curPostInd].gifURL)
            mCommentView.text = prevPost[curPostInd].description
        }
        updatePrevButton(null)
    }

    // show text and button of error
    fun showError(view: View?)
    {
        mGifView.setImageBitmap(null)
        ErrorText.visibility = View.VISIBLE;
        ErrorButton.visibility = View.VISIBLE;
    }

    // hide error button and text
    fun hideError(view: View?)
    {
        ErrorText.visibility = View.INVISIBLE;
        ErrorButton.visibility = View.INVISIBLE;
    }

    // retry to load post
    fun retry(view: View?)
    {
        hideError(null)
        if(curPostInd >= 0) // we downloaded post before
        {
            loadGif(prevPost[curPostInd].gifURL)
            mCommentView.text = prevPost[curPostInd].description
        }
        else // we have no loaded posts
        {
            loadRandomPost(null)
        }
        updatePrevButton(null)
    }

    fun updatePrevButton(view: View?)
    {
        if(curPostInd < 1) {
            mPrevButton.setEnabled(false)
        }
        else
        {
            mPrevButton.setEnabled(true)
        }
    }
}