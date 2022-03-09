package gcore.videocalls.demo.call

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.peer_item.view.*
import gcore.videocalls.demo.databinding.PeerItemBinding
import gcore.videocalls.meet.GCoreMeet
import org.protoojs.droid.Peer
import timber.log.Timber

class PeerDataBinding {
    var audioEnabled: ObservableField<Boolean> = ObservableField()
    var videoVisible: ObservableField<Boolean?> = ObservableField()
    var isModer: ObservableField<Boolean> = ObservableField()
}

class PeersRecyclerViewAdapter(
    val context: Context,
    val owner: LifecycleOwner,
    private val moderOptionsClicked: (gcore.videocalls.meet.model.Peer) -> Unit
) :
    RecyclerView.Adapter<PeersRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(v: View?) : RecyclerView.ViewHolder(
        v!!
    ) {
        var binding: PeerItemBinding? = DataBindingUtil.bind(v!!)
    }

    var measuredHeight: Int = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: PeerItemBinding = PeerItemBinding.inflate(inflater, viewGroup, false)

        measuredHeight = viewGroup.measuredHeight

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")
        with(viewHolder.itemView) {
            val peer = GCoreMeet.instance.getPeers().value!!.allPeers[position]
            Timber.d("peerId:" + peer.id)

            peer_video_view.connect(peer.id)

            viewHolder.binding?.peerData =
                PeerDataBinding().apply {
                    audioEnabled = peer_video_view.getAudioState()
                    videoVisible = peer_video_view.getVideoState()
                    isModer.set(GCoreMeet.instance.roomManager.isModer)
                }

            display_name.text = peer.displayName

            moderator_options.setOnClickListener {
                moderOptionsClicked.invoke(peer)
            }
        }

        val lp: GridLayoutManager.LayoutParams =
            viewHolder.itemView.layoutParams as GridLayoutManager.LayoutParams
        lp.height = (measuredHeight / if (itemCount > 1) 2.0 else 1.1).toInt()
        viewHolder.itemView.layoutParams = lp
    }

    override fun getItemCount() = GCoreMeet.instance.getPeers().value?.allPeers?.size ?: 0

    companion object {
        private const val TAG = "PeersRecyclerViewAdapter"
    }
}
