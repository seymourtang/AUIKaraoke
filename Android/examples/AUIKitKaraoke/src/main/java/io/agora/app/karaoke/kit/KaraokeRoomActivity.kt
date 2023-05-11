package io.agora.app.karaoke.kit

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.karaoke.databinding.KaraokeRoomActivityBinding
import io.agora.asceneskit.karaoke.IKaraokeRoomService
import io.agora.scene.show.utils.PermissionHelp

class KaraokeRoomActivity : AppCompatActivity(), IKaraokeRoomService.KaraokeRoomRoomRespDelegate {

    companion object {
        private var roomService: IKaraokeRoomService? = null
        private var onRoomDestroy: ((Boolean) -> Unit)? = null
        private var onRoomCreated: (() -> Unit)? = null
        private var themeId: Int = View.NO_ID

        fun launch(
            context: Context,
            themeId: Int,
            roomService: IKaraokeRoomService,
            onRoomCreated: () -> Unit,
            onRoomDestroy: (Boolean) -> Unit
        ) {
            Companion.roomService = roomService
            Companion.onRoomCreated = onRoomCreated
            Companion.onRoomDestroy = onRoomDestroy
            Companion.themeId = themeId

            val intent = Intent(context, KaraokeRoomActivity::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private val mViewBinding by lazy { KaraokeRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)

    override fun onDestroy() {
        super.onDestroy()
        roomService?.unbindRespDelegate(this)
        roomService = null
        onRoomDestroy?.invoke(false)
        onRoomDestroy = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (themeId != View.NO_ID) {
            setTheme(themeId)
        }
        setContentView(mViewBinding.root)

        roomService?.bindRespDelegate(this)
        mPermissionHelp.checkMicPerm(
            {
                mViewBinding.karaokeRoomView.bindService(roomService!!)
                onRoomCreated?.invoke()
                onRoomCreated = null
            },
            {
                onRoomDestroy?.invoke(true)
                onRoomDestroy = null
                finish()
            },
            true
        )
    }

    override fun onRoomExitedOrDestroyed() {
        super.onRoomExitedOrDestroyed()
        finish()
    }

    override fun onBackPressed() {
        mViewBinding.karaokeRoomView.showExitDialog()
    }

}