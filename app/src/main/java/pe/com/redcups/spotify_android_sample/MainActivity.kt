package pe.com.redcups.spotify_android_sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val CLIENT_ID = "accf902e212f46dc9f8a157b18bef7ce"
    val REDIRECT_URI = "testschema://callback"
    var isPlaying = false
    private lateinit var mSpotifyAppRemote: SpotifyAppRemote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        play_button.setOnClickListener {
            if (::mSpotifyAppRemote.isInitialized){
                isPlaying = if(!isPlaying){
                    mSpotifyAppRemote.playerApi.resume()
                    !isPlaying
                }else{
                    mSpotifyAppRemote.playerApi.pause()
                    !isPlaying
                }
            }else{
                Toast.makeText(baseContext,"Not connected yet",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Set up connection
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(this, connectionParams,
            object : Connector.ConnectionListener {

                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Toast.makeText(baseContext,"Connected! Yay!",Toast.LENGTH_LONG).show()

                    // Now you can start interacting with App Remote
                    connected()

                }

                override fun onFailure(throwable: Throwable) {
                    Toast.makeText(baseContext,"Error!",Toast.LENGTH_LONG).show()
                    Log.e("MyActivity", throwable.message, throwable)
                    // Something went wrong when attempting to connect! Handle errors here
                }
            })
    }

    fun connected() {
        // We need the playlist ID

        //mSpotifyAppRemote.playerApi.play("spotify:playlist:2uFSXz1WeEUoZkP3jY5IHA")

        // Subscribe to Observer to get information about current song playing track
        // Can work well with Live Data or View Models
        mSpotifyAppRemote.playerApi
            .subscribeToPlayerState()
            .setEventCallback { playerState ->  
                val track = playerState.track
                track?.let {
                    Toast.makeText(
                        baseContext,
                        "${track.name} by ${track.artist.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    song_text.text = "${track.name} by ${track.artist.name}"
                    setArtwork(track.imageUri)

                    Log.d("MainActivity", "${track.name} by ${track.artist.name}")
                }
                isPlaying = !playerState.isPaused
            }


    }

    fun setArtwork(imageUri: ImageUri){
        mSpotifyAppRemote.imagesApi.getImage(imageUri).setResultCallback {
            track_art_work.setImageBitmap(it)
        }
    }

    override fun onStop() {
        super.onStop()
        // Disconnect when leaving the activity
        if (::mSpotifyAppRemote.isInitialized){
            SpotifyAppRemote.disconnect(mSpotifyAppRemote)
        }
    }
}
