import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.hbch.traewelling.databinding.StandardListItemBinding

class StandardListItemAdapter<T>(
    val list: List<T>,
    private val completeBinding: (T, StandardListItemBinding) -> Unit,
    private val onItemClick: (T) -> Unit
) : RecyclerView.Adapter<StandardListItemAdapter.StandardListItemViewHolder<T>>() {

    private lateinit var binding: StandardListItemBinding

    class StandardListItemViewHolder<T>(val binding: StandardListItemBinding, private val completeBinding: (T, StandardListItemBinding) -> Unit) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: T) {
            completeBinding(item, binding)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandardListItemViewHolder<T> {
        val inflater = LayoutInflater.from(parent.context)
        binding = StandardListItemBinding.inflate(inflater, parent, false)
        return StandardListItemViewHolder<T>(binding, completeBinding)
    }

    override fun onBindViewHolder(holder: StandardListItemViewHolder<T>, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            onItemClick(list[position])
        }
    }

    override fun getItemCount() = list.size
}