package com.example.conceal.concealtest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static com.example.conceal.concealtest.R.id.txtSmall;


public class MainActivity extends AppCompatActivity {

    private Crypto crypto;
    private Entity password;
    private File file;
    TextView txtSmall;
    TextView txtBig;
    TextView txtHuge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSmall = (TextView) findViewById(R.id.txtSmall);
        txtBig = (TextView) findViewById(R.id.txtBig);
        txtHuge = (TextView) findViewById(R.id.txtHuge);

        password = Entity.create("Password");
        file = new File(getFilesDir(), "test.txt");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e("MainActivity", "can not create a file", e);
            }
        }
    }

    public void onEncryptSmall(View view) {
        Log.i(MainActivity.class.getSimpleName(), "onEncryptSmall");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long start = System.currentTimeMillis();
                    encrypt(MainActivity.this, "this is test string".getBytes());
                    decrypt(MainActivity.this);
                    final long stop = System.currentTimeMillis() - start;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtSmall.setText(" Duration (ms) " + stop);
                        }
                    });
                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "can not read file", e);
                }
            }
        }).start();
    }

    public void onEncryptBig(View view) {
        Log.i(MainActivity.class.getSimpleName(), "onEncryptBig");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int size = 10 * 1024 * 1024;

                    byte[] data = new byte[size];

                    for (int i = 0; i < size; i++) {
                        data[i] = (byte) i;
                    }

                    long start = System.currentTimeMillis();
                    Log.i(MainActivity.class.getSimpleName(), "onEncryptBig start encryption");
                    encrypt(MainActivity.this, data);
                    Log.i(MainActivity.class.getSimpleName(), "onEncryptBig start decryption");
                    decrypt(MainActivity.this);
                    final long stop = System.currentTimeMillis() - start;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtBig.setText(" Duration (ms) " + stop);
                        }
                    });
                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "can not read file", e);
                }
            }
        }).start();
    }

    public void onEncryptHuge(View view) {
        Log.i(MainActivity.class.getSimpleName(), "onEncryptHuge");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int size = 50 * 1024 * 1024;

                    byte[] data = new byte[size];

                    for (int i = 0; i < size; i++) {
                        data[i] = (byte) i;
                    }

                    long start = System.currentTimeMillis();
                    Log.i(MainActivity.class.getSimpleName(), "onEncryptHuge start encryption");
                    encrypt(MainActivity.this, data);
                    Log.i(MainActivity.class.getSimpleName(), "onEncryptHuge start decryption");
                    decrypt(MainActivity.this);
                    final long stop = System.currentTimeMillis() - start;

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtHuge.setText(" Duration (ms) " + stop);
                        }
                    });
                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "can not read file", e);
                }
            }
        }).start();
    }

    private static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    private void encrypt(Context context, byte[] source) throws IOException, CryptoInitializationException, KeyChainException {
// Creates a new Crypto object with default implementations of
// a key chain as well as native library.
        crypto = new Crypto(
                new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256),
                new SystemNativeCryptoLibrary(), CryptoConfig.KEY_256);

// Check for whether the crypto functionality is available
// This might fail if android does not load libaries correctly.
        if (!crypto.isAvailable()) {
            return;
        }

        OutputStream fileStream = new FileOutputStream(file);

// Creates an output stream which encrypts the data as
// it is written to it and writes it out to the file.
        OutputStream outputStream = crypto.getCipherOutputStream(
                fileStream,
                password);

        // Write plaintext to it.
        outputStream.write(source);
        outputStream.close();
    }

    private void decrypt(Context context) throws IOException, CryptoInitializationException, KeyChainException {
// Get the file to which ciphertext has been written.
        FileInputStream fileStream = new FileInputStream(file);

// Creates an input stream which decrypts the data as
// it is read from it.
        InputStream inputStream = crypto.getCipherInputStream(
                fileStream,
                password);

// Read into a byte array.
        int read;
        byte[] buffer = new byte[1024];

        OutputStream out = new ByteArrayOutputStream(inputStream.available());

// You must read the entire stream to completion.
// The verification is done at the end of the stream.
// Thus not reading till the end of the stream will cause
// a security bug.
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        inputStream.close();
//        String result = out.toString();
        out.close();

    }
}
