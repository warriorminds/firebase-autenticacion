package com.warriorminds.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IniciarSesionFacebookActivity extends AppCompatActivity {

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

    // Variables de Facebook
    /**
     * Objeto de Facebook que se utiliza para manejar las llamadas desde la actividad.
     */
    private CallbackManager manejadorDeLlamadas;

    // Vistas
    private TextView textViewUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_iniciar_sesion_facebook);

        textViewUsuario = (TextView) findViewById(R.id.usuario);
        manejadorDeLlamadas = CallbackManager.Factory.create();

        LoginButton botonLogin = (LoginButton) findViewById(R.id.boton_login_facebook);
        botonLogin.setReadPermissions("email", "public_profile");
        botonLogin.registerCallback(manejadorDeLlamadas, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                iniciarSesionFirebaseConFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(IniciarSesionFacebookActivity.this, "Usuario canceló inicio de sesión con Facebook.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(IniciarSesionFacebookActivity.this, "Hubo un error al iniciar sesión con Facebook.", Toast.LENGTH_SHORT).show();
            }
        });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    cerrarSesionFirebase();
                }
            }
        };

        inicializarAutenticacion();
    }

    private void cerrarSesionFirebase() {
        autenticacionFirebase.signOut();
        textViewUsuario.setText("");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        manejadorDeLlamadas.onActivityResult(requestCode, resultCode, data);
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
                    textViewUsuario.setText(usuario.getEmail());
                    Toast.makeText(IniciarSesionFacebookActivity.this, "Usuario: " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(IniciarSesionFacebookActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void iniciarSesionFirebaseConFacebook(AccessToken tokenDeAcceso) {
        AuthCredential credencial = FacebookAuthProvider.getCredential(tokenDeAcceso.getToken());
        autenticacionFirebase.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(IniciarSesionFacebookActivity.this, "Hubo un error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
