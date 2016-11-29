package com.warriorminds.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    // Vistas para cerrar sesión y mostrar info de usuario.
    private Button botonCerrarSesion;
    private TextView textViewUsuario;

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
        /**
         * Necesitamos crear un TwitterAuthConfig con la API KEY y API SECRET de Twitter, para después
         * inicialicar Fabric con ese objeto. Debe hacerse antes del setContentView().
         */
        TwitterAuthConfig configuracionAutenticacionTwitter = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret));
        Fabric.with(this, new Twitter(configuracionAutenticacionTwitter));
        setContentView(R.layout.activity_iniciar_sesion_twitter);

        inicializarVistas();
        inicializarAutenticacion();
        inicializarTwitter();
    }

    /**
     * Agregado para que el botón de inicio de sesión de Twitter maneje el resultado de las actividades
     * que mandó llamar.
     *
     * @param codigoSolicitud
     * @param codigoResultado
     * @param datos
     */
    @Override
    protected void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datos) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos);
        botonInicioSesionTwitter.onActivityResult(codigoSolicitud, codigoResultado, datos);
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
     * Método para inicializar las vistas que se usan.
     */
    private void inicializarVistas() {
        botonCerrarSesion = (Button) findViewById(R.id.boton_cerrar_sesion);
        textViewUsuario = (TextView) findViewById(R.id.usuario);

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
    }

    /**
     * Método en el cual se inicializa Firebase.
     */
    private void inicializarAutenticacion() {
        /**
         * Obtener la instancia de FirebaseAuth.
         */
        autenticacionFirebase = FirebaseAuth.getInstance();

        listenerAutenticacion = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null) {
                    /**
                     * El usuario ha iniciado sesión correctamente.
                     */
                    textViewUsuario.setText("Usuario: " + usuario.getDisplayName());
                    botonInicioSesionTwitter.setEnabled(false);
                    botonCerrarSesion.setVisibility(View.VISIBLE);
                    Toast.makeText(IniciarSesionTwitterActivity.this, "Usuario: " + usuario.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    /**
                     * El usuario aún no ha iniciado sesión.
                     */
                    Toast.makeText(IniciarSesionTwitterActivity.this, "Usuario no ha iniciado sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Método donde se inicializa Twitter para poder iniciar sesión.
     */
    private void inicializarTwitter() {
        /**
         * Obtenemos el botón de inicio de sesión con Twitter.
         */
        botonInicioSesionTwitter = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        /**
         * Se agregan las llamadas de regreso, callbacks, para manejar cuando se haya iniciado sesión
         * correctamente con Twitter, o cuando hubo algún error al iniciar sesión.
         */
        botonInicioSesionTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> resultado) {
                /**
                 * Si el inicio de sesión con Twitter fue exitoso, iniciamos sesión con Firebase
                 * utilizando el resultado que nos provee Twitter.
                 */
                iniciarSesionConFirebase(resultado.data);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });
    }

    /**
     * Método para iniciar sesión con Firebase.
     *
     * @param sesionTwitter
     */
    private void iniciarSesionConFirebase(TwitterSession sesionTwitter) {
        /**
         * Se crea una credencial con TwitterAuthProvider y con el token y secret que nos proporciona Twitter.
         */
        AuthCredential credencial = TwitterAuthProvider.getCredential(sesionTwitter.getAuthToken().token, sesionTwitter.getAuthToken().secret);

        /**
         * Se inicia sesión con la credencial creada. Se agrega el OnCompleteListener para saber si
         * se pudo iniciar sesión en Firebase o si hubo algún error.
         */
        autenticacionFirebase.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(IniciarSesionTwitterActivity.this, "Hubo un error", Toast.LENGTH_SHORT).show();
                            cerrarSesion();
                        }
                    }
                });
    }

    /**
     * Método para cerrar sesión con Firebase y con Twitter.
     */
    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        Twitter.logOut();

        textViewUsuario.setText("");
        botonCerrarSesion.setVisibility(View.GONE);
        botonInicioSesionTwitter.setEnabled(true);
    }
}
