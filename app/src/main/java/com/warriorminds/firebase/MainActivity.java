package com.warriorminds.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseFirebaseActivity {

    private Button iniciarSesion;
    private Button cerrarSesion;
    private Button botonEnlazarCuentas;
    private View datos;
    private TextView tvCorreo;
    private TextView tvNombre;
    private TextView tvProveedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig configuracionAutenticacionTwitter = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret));
        Fabric.with(this, new Twitter(configuracionAutenticacionTwitter));
        setContentView(R.layout.activity_main);

        inicializarVistas();

        if (autenticacionFirebase.getCurrentUser() == null) {
            autenticarAnonimamente();
        } else {
            iniciarSesion.setVisibility(View.GONE);
            cerrarSesion.setVisibility(View.VISIBLE);
            botonEnlazarCuentas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser usuario = autenticacionFirebase.getCurrentUser();
        if (usuario != null && !usuario.isAnonymous()) {
            visibilidadCampos(true);
            actualizarDatos(usuario);
        } else {
            visibilidadCampos(false);
        }
    }

    @Override
    protected void sesionIniciada() {
        // no-op
    }

    @Override
    protected void usuarioAutenticado() {
        FirebaseUser usuario = autenticacionFirebase.getCurrentUser();
        if (usuario != null && !usuario.isAnonymous()) {
            visibilidadCampos(true);
            actualizarDatos(usuario);
        }
    }

    private void inicializarVistas() {
        datos = findViewById(R.id.datos);
        tvCorreo = (TextView) findViewById(R.id.tvCorreo);
        tvNombre = (TextView) findViewById(R.id.tvNombre);
        tvProveedor = (TextView) findViewById(R.id.tvProveedor);
        iniciarSesion = (Button) findViewById(R.id.btnIniciarSesion);
        iniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionActivity.class));
            }
        });

        cerrarSesion = (Button) findViewById(R.id.btnCerrarSesion);
        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });

        botonEnlazarCuentas = (Button) findViewById(R.id.boton_enlazar_cuentas);
        botonEnlazarCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IniciarSesionActivity.class));
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
        visibilidadCampos(false);
    }

    private void visibilidadCampos(boolean sesionIniciada) {
        iniciarSesion.setVisibility(sesionIniciada ? View.GONE : View.VISIBLE);
        cerrarSesion.setVisibility(sesionIniciada ? View.VISIBLE : View.GONE);
        datos.setVisibility(sesionIniciada ? View.VISIBLE : View.GONE);
        botonEnlazarCuentas.setVisibility(sesionIniciada ? View.VISIBLE : View.GONE);
    }

    private void actualizarDatos(FirebaseUser usuario) {

        String correo = usuario.getEmail();
        String nombre = usuario.getDisplayName();
        Uri foto = usuario.getPhotoUrl();

        List<? extends UserInfo> providerData = usuario.getProviderData();
        for (int i = 0; i < providerData.size(); i++) {
            UserInfo userInfo = providerData.get(i);
            if (TextUtils.isEmpty(correo)) {
                correo = userInfo.getEmail();
            }
            if (TextUtils.isEmpty(nombre)) {
                nombre = userInfo.getDisplayName();
            }

            if (foto == null) {
                foto = userInfo.getPhotoUrl();
            }
        }

        tvCorreo.setText(correo);
        tvNombre.setText(nombre);
        tvProveedor.setText(usuario.getProviderId());
        Picasso.with(this).load(foto).into(((ImageView)findViewById(R.id.imagen)));
    }
}
