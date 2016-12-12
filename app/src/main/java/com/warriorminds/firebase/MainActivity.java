package com.warriorminds.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseFirebaseActivity {

    private static int CODIGO_INICIO_SESION = 8000;

    private Button iniciarSesion;
    private Button cerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        inicializarVistas();

        if (autenticacionFirebase.getCurrentUser() == null) {
            autenticarAnonimamente();
        } else {
            iniciarSesion.setVisibility(View.GONE);
            cerrarSesion.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void sesionIniciada() {
        // no-op
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_INICIO_SESION) {
            if (resultCode == RESULT_OK) {
                // TODO: obtener datos del usuario correspondiente.
                FirebaseUser usuario = autenticacionFirebase.getCurrentUser();

                if (usuario != null) {
                    Toast.makeText(this, "Usuario: " + usuario.getUid(), Toast.LENGTH_SHORT).show();
                }

                iniciarSesion.setVisibility(View.GONE);
                cerrarSesion.setVisibility(View.VISIBLE);
            } else {
                cerrarSesion();
            }
        }
    }

    private void inicializarVistas() {
        iniciarSesion = (Button) findViewById(R.id.btnIniciarSesion);
        iniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, IniciarSesionActivity.class), CODIGO_INICIO_SESION);
            }
        });

        cerrarSesion = (Button) findViewById(R.id.btnCerrarSesion);
        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    private void autenticarAnonimamente() {
        autenticacionFirebase.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Hubo un error.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Bienvenido usuario anónimo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void cerrarSesion() {
        super.cerrarSesion();
        iniciarSesion.setVisibility(View.VISIBLE);
        cerrarSesion.setVisibility(View.GONE);
    }
}
