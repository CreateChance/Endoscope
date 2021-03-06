package pl.hypeapp.endoscope.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import net.grandcentrix.thirtyinch.TiActivity;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.hypeapp.endoscope.R;
import pl.hypeapp.endoscope.presenter.PlayStreamPresenter;
import pl.hypeapp.endoscope.util.SettingsPreferencesUtil;
import pl.hypeapp.endoscope.util.SteerEngineController;
import pl.hypeapp.endoscope.view.PlayStreamView;

public class PlayStreamActivity extends TiActivity<PlayStreamPresenter, PlayStreamView>
        implements PlayStreamView, SurfaceHolder.Callback, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    private static final String TAG = "PlayStreamActivity";

    public static final String INTENT_EXTRA_IP_CONNECT = "ip_connect";
    private MediaPlayer mediaPlayer;
    @BindView(R.id.surface_play) SurfaceView surfaceView;

    private SteerEngineController mSteerEngineController;

    @NonNull
    @Override
    public PlayStreamPresenter providePresenter() {
        SettingsPreferencesUtil settingsPreferencesUtil = new SettingsPreferencesUtil(PreferenceManager.getDefaultSharedPreferences(this));
        String ipAddress = getIntent().getStringExtra(INTENT_EXTRA_IP_CONNECT);
        return new PlayStreamPresenter(ipAddress, settingsPreferencesUtil);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_stream);
        ButterKnife.bind(this);
        surfaceView.getHolder().addCallback(this);

        mSteerEngineController = new SteerEngineController();
    }

    @Override
    public void setFullscreenWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getPresenter().onSurfaceCreated();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        getPresenter().onSurfaceDestroyed();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        getPresenter().onMediaPlayerPrepared(mp);
    }

    @Override
    public void configureMediaPlayer(Uri videoUri) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setDisplay(surfaceView.getHolder());
        mediaPlayer.setOnPreparedListener(this);
        try {
            mediaPlayer.setDataSource(this, videoUri);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void releaseMediaPlayer() {
        surfaceView.getHolder().removeCallback(this);
        mediaPlayer.release();
    }

    @Override
    public void logError(String msg) {
        String error = (msg == null) ? "Error unknown" : msg;
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_dialog_title)
                .setCancelable(false)
                .setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(PlayStreamActivity.this, ConnectToStreamActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    Intent intent = new Intent(PlayStreamActivity.this, ConnectToStreamActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return false;
            }
        }).create().show();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
            logError("MEDIA ERROR");
        } else if (extra == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            logError("SERVER DIED ERROR");
        } else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
            logError("MEDIA UNSUPPORTED");
        } else if (extra == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
            logError("MEDIA ERROR UNKNOWN");
        } else if (extra == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
            logError("NOT VALID PROGRESSIVE PLAYBACK");
        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
            logError("MEDIA ERROR TIMED OUT");
        } else {
            logError("ERROR UNKNOWN (" + what + ")" + "(" + extra + ")");
        }
        return false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void OnClick(View view) {
        switch (view.getId()) {
            case R.id.turn_left:
                Log.d(TAG, "turn left here.");
                mSteerEngineController.doTurnLeft();
                break;
            case R.id.turn_right:
                Log.d(TAG, "turn right here.");
                mSteerEngineController.doTurnRight();
                break;
        }
    }
}
