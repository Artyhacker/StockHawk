package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.udacity.stockhawk.R;

/**
 * Created by dh on 17-2-5.
 */

public class StockDetailActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("symbol");
        TextView tvSymbol = (TextView) findViewById(R.id.detail_symbol);
        tvSymbol.setText(symbol);
    }
}
