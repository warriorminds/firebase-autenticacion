package com.warriorminds.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class IniciarSesionTwitterActivity extends AppCompatActivity {

    // Boton de Twitter
    private TwitterLoginButton botonInicioSesionTwitter;

    // Variables que utiliza Firebase.
    /**
     * FirebaseAuth es el objeto que contiene el listener que escucha los cambios en la cuenta
     * y con el que se puede iniciar sesión con Google.
     */
    private FirebaseAuth autenticacionFirebase;
    /**
     * El listener escucha cambios en la cuenta. Cuando se inicia sesión de manera exitosa,
     * se ejecuta el método onAuthStateChanged y se obtiene un FirebaseUser != null. Si el FirebaseUser
     * es null, quiere decir que no se inició sesión.
     */
    private FirebaseAuth.AuthStateListener listenerAutenticacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret));
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_iniciar_sesion_twitter);

        inicializarAutenticacion();
        inicializarTwitter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        botonInicioSesionTwitter.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Se necesita agregar el listener de Firebase en onStart().
     */
    @Override
    public void onStart() {
        super.onStart();
        autenticacionFirebase.addAuthStateListener(listenerAutenticacion);
    }

    /**
     * Quitar el listener antes de salir de la actividad.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (listenerAutenticacion != null) {
            autenticacionFirebase.removeAuthStateListener(listenerAutenticacion);
        }
    }

    /**
     * Método en el cual se inicializa Firebase.
     */
    private void inicializarAutenticacion() {
        autenticacionFirebase = FirebaseAuth.getInstance();

        listenerAutenticacion = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null) {
                    Toast.makeText(IniciarSesionTwitterActivity.this, "Usuario: " + usuario.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(IniciarSesionTwitterActivity.this, "Usuario no ha iniciado sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void inicializarTwitter() {
        botonInicioSesionTwitter = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        botonInicioSesionTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> resultado) {
                iniciarSesionConFirebase(resultado.data);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });
    }

    private void iniciarSesionConFirebase(TwitterSession sesionTwitter) {
        AuthCredential credential = TwitterAuthProvider.getCredential(sesionTwitter.getAuthToken().token, sesionTwitter.getAuthToken().secret);

        autenticacionFirebase.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                           Toast.makeText(IniciarSesionTwitterActivity.this, "Hubo un error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
