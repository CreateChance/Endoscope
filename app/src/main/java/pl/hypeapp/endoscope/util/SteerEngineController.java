package pl.hypeapp.endoscope.util;

import android.util.Log;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Gao Chao
 * @date 6/23/17
 */

public class SteerEngineController {

    private static final String TAG = "SteerEngineController";

    private int mCurrentRotation = 0;

    private static ExecutorService mCacheThreadExecutor = null;

    private int ACTION_TURN_RIGHT = 100;
    private int ACTION_TURN_LEFT = 101;

    private List<byte[]> mRotationList = new ArrayList<>();

    public SteerEngineController() {
        mCacheThreadExecutor = Executors.newCachedThreadPool();

        mRotationList.add(new byte[]{
                0x00,
                0x00
        });
        mRotationList.add(new byte[] {
                0x01,
                0x00
        });
        mRotationList.add(new byte[] {
                0x02,
                0x00
        });
        mRotationList.add(new byte[] {
                0x03,
                0x00
        });
        mRotationList.add(new byte[] {
                0x04,
                0x00
        });
        mRotationList.add(new byte[] {
                0x05,
                0x00
        });
        mRotationList.add(new byte[] {
                0x06,
                0x00
        });
        mRotationList.add(new byte[] {
                0x07,
                0x00
        });
        mRotationList.add(new byte[] {
                0x09,
                0x00
        });
        mRotationList.add(new byte[] {
                0x09,
                0x09
        });
    }

    public void doTurnRight() {
        ExecutorServiceThread(ACTION_TURN_RIGHT);
    }

    public void doTurnLeft() {
        ExecutorServiceThread(ACTION_TURN_LEFT);
    }

    private void ExecutorServiceThread(int action) {
        if (action == ACTION_TURN_RIGHT) {
            mCacheThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("192.168.100.15", 1025);
                        OutputStream os = socket.getOutputStream();
                        mCurrentRotation++;
                        if (mCurrentRotation > mRotationList.size() - 1) {
                            mCurrentRotation = mRotationList.size() - 1;
                        }
                        os.write(mRotationList.get(mCurrentRotation));
                        Log.d(TAG, "Turn right command send complete!");
                        os.close();
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } else if (action == ACTION_TURN_LEFT) {
            mCacheThreadExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket("192.168.100.15", 1025);
                        OutputStream os = socket.getOutputStream();
                        mCurrentRotation--;
                        if (mCurrentRotation < 0) {
                            mCurrentRotation = 0;
                        }
                        os.write(mRotationList.get(mCurrentRotation));
                        Log.d(TAG, "Turn left command send complete!");
                        os.close();
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
}
