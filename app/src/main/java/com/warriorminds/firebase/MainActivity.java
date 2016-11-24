package com.warriorminds.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botonCrearCuenta = (Button) findViewById(R.id.btnCrearEmail);
        botonCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CrearCuentaActivity.class));
            }
        });

        Button botonIniciarSesion = (Button) findViewById(R.id.btnIniciarSesion);
        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionCorreoContrasenaActivity.class));
            }
        });

        Button botonIniciarSesionGoogle = (Button) findViewById(R.id.btnIniciarSesionGoogle);
        botonIniciarSesionGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionGoogleActivity.class));
            }
        });

        Button botonIniciarSesionFacebook = (Button) findViewById(R.id.btnIniciarSesionFacebook);
        botonIniciarSesionFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionFacebookActivity.class));
            }
        });

        Button botonIniciarSesionTwitter = (Button) findViewById(R.id.btnIniciarSesionTwitter);
        botonIniciarSesionTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionTwitterActivity.class));
            }
        });
    }
}
