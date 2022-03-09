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
import gcore.videocalls.demo.databinding.WaitingPeerItemBinding
import gcore.videocalls.meet.GCoreMeet
import timber.log.Timber

class WaitingPeerDataBinding {
    //    var audioEnabled: ObservableField<Boolean> = ObservableField()
//    var videoVisible: ObservableField<Boolean?> = ObservableField()
    var name: ObservableField<String> = ObservableField()
}

class WaitingPeersRecyclerViewAdapter(val context: Context, val owner: LifecycleOwner) :
    RecyclerView.Adapter<WaitingPeersRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(v: View?) : RecyclerView.ViewHolder(
        v!!
    ) {
        var binding: WaitingPeerItemBinding? = DataBindingUtil.bind(v!!)
    }

//    var measuredHeight: Int = 0

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding: WaitingPeerItemBinding =
            WaitingPeerItemBinding.inflate(inflater, viewGroup, false)

//        measuredHeight = viewGroup.measuredHeight

        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Timber.d("onBindViewHolder")
        with(viewHolder.itemView) {
            val peer =
                GCoreMeet.instance.roomManager.roomProvider.waitingPeers.value!!.allPeers[position]
            Timber.d("peerId:" + peer.id)

            peer_video_view.connect(peer.id)



            display_name.text = peer.displayName
        }

//        val lp: GridLayoutManager.LayoutParams =
//            viewHolder.itemView.layoutParams as GridLayoutManager.LayoutParams
//        lp.height = (measuredHeight / if (itemCount > 1) 2.0 else 1.1).toInt()
//        viewHolder.itemView.layoutParams = lp
    }

    override fun getItemCount() =
        GCoreMeet.instance.roomManager.roomProvider.waitingPeers.value?.allPeers?.size ?: 0

    companion object {
        private const val TAG = "PeersRecyclerViewAdapter"
    }
}
