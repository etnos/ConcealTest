package com.example.conceal.concealtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void enc() {
        // Creates a new Crypto object with default implementations of
// a key chain as well as native library.
        Crypto crypto = new Crypto(
                new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());

// Check for whether the crypto functionality is available
// This might fail if Android does not load libraries correctly.
        if (!crypto.isAvailable()) {
            return;
        }

        OutputStream fileStream = new BufferedOutputStream(
                new FileOutputStream(file));

// Creates an output stream which encrypts the data as
// it is written to it and writes it out to the file.
        OutputStream outputStream = crypto.getCipherOutputStream(
                fileStream,
                entity);

// Write plaintext to it.
        outputStream.write(plainText);
        outputStream.close();
    }
}
