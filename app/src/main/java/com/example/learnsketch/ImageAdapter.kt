import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.learnsketch.DrawActivity
import com.example.learnsketch.HomeScreen
import com.example.learnsketch.ImageItem
import com.example.learnsketch.ProSketchActivity
import com.example.learnsketch.ProSketchCanvas
import com.example.learnsketch.R
import com.example.learnsketch.SaveSketch
import java.lang.Exception


class ImageAdapter(private val imageList: List<ImageItem>) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var prosketchmode: Boolean = false
    private var imageTitle: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recentsketch_itemview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageItem = imageList[position]

        // Load the image into the ImageView using Picasso
        var imagePath = imageItem.imagePath
        imageTitle = imagePath.substringAfterLast('/').substringBeforeLast('.')
        if (imageTitle.contains("@pro")) {
            imageTitle = imageTitle.removeSuffix("@pro")
        }
        var refimagePath = "${
            imagePath.substringBeforeLast('/').substringBeforeLast('/')
        }/refImages/${imageTitle}.png"
        holder.imageView.setImageURI(imagePath.toUri())
        holder.imageTitle_TV.text = imageTitle
//        Picasso.get()
//            .load(imageItem.imagePath.toUri())
//            .into(holder.imageView)
//        Picasso.get().setLoggingEnabled(true)

        try {

            holder.itemView.setOnClickListener {

                if (imagePath.contains("@pro")) {
                    HomeScreen.isSavedSketchClicked = true
                    val context = holder.itemView.context
                    val intent = Intent(context, ProSketchActivity::class.java)
                    intent.putExtra("classFrom", ImageAdapter::class.java)
                    intent.putExtra("imagepath", imageItem.imagePath)
                    context.startActivity(intent)
                } else {

                    HomeScreen.isSavedSketchClicked = true
                    val context = holder.itemView.context
                    // Create an intent to start the next activity
                    val intent = Intent(context, DrawActivity::class.java)
                    // Pass the image path as an extra to the intent
                    intent.putExtra("classFrom", ImageAdapter::class.java.toString())
                    intent.putExtra("imagepath", imageItem.imagePath)
                    intent.putExtra("saved_refimagepath", refimagePath)
//                intent.putExtra("imagepath", imageItem.imagePath).putExtra("Hi", "hi")
//                Log.d("DrawActivitySavedSketch", imageItem.imagePath)
//                Log.d("PulledRefImagepath", DrawActivity.pulled_refimgpath)
                    // Start the activity
                    context.startActivity(intent)
                }

            }
        } catch (e: Exception) {
            Log.d("ErrorImageAdapter", e.toString())
        }

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_IV)
        val imageTitle_TV: TextView = itemView.findViewById(R.id.imageTitle_TV)
    }
}
