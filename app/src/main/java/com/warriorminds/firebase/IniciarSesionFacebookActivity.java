package com.warriorminds.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

/**
 * Clase que utiliza Facebook y Firebase para iniciar sesión.
 * Para que funcione, debem de agregar su ID de aplicación en strings.xml (facebook_app_id).
 *
 * @author warriorminds
 */
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
    private CallbackManager manejadorDeLlamadasFacebook;

    // Vistas
    private TextView textViewUsuario;
    // Botón de inicio de sesión de Facebook
    private LoginButton botonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Deben inicializar el SDK de Facebook antes del setContentView()
         */
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_iniciar_sesion_facebook);

        inicializarVistas();
        inicializarFacebook();
        inicializarAutenticacion();
    }

    /**
     * Método para inicializar las vistas utilizadas.
     */
    private void inicializarVistas() {
        textViewUsuario = (TextView) findViewById(R.id.usuario);
        botonLogin = (LoginButton) findViewById(R.id.boton_login_facebook);
    }

    /**
     * Método donde se inicializan los objetos de Facebook.
     */
    private void inicializarFacebook() {
        /**
         * Creamos el manejador de llamadas de Facebook.
         */
        manejadorDeLlamadasFacebook = CallbackManager.Factory.create();

        /**
         * Agregamos los permisos que queremos pedir al usuario. Al menos debemos de pedir
         * el permiso de email y public_profile.
         */
        botonLogin.setReadPermissions("email", "public_profile");

        /**
         * Necesitamos agregar el manejador de llamadas al botón de inicio de sesión. Creamos
         * un FacebookCallback<LoginResult> el cual tiene métodos que se ejecutarán cuando el usuario
         * haya iniciado sesión, cuando haya cancelado el inicio de sesión, o cuando hubo algún error.
         */
        botonLogin.registerCallback(manejadorDeLlamadasFacebook, new FacebookCallback<LoginResult>() {
            /**
             * Si fue exitoso el inicio de sesión, iniciamos sesión con Firebase. Necesitamos el
             * Access Token que obtenemos de Facebook para esto.
             * @param resultadoInicioSesionFacebook
             */
            @Override
            public void onSuccess(LoginResult resultadoInicioSesionFacebook) {
                iniciarSesionFirebaseConFacebook(resultadoInicioSesionFacebook.getAccessToken());
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

        /**
         * El AccessTokenTracker nos sirve para saber cuando hubo algún cambio en el estado de la sesión.
         * Si el tokenDeAccesoActual es null, quiere decir que se cerró sesión de Facebook y procedemos
         * a cerrar sesión de Firebase.
         */
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken tokenDeAccesoAnterior,
                    AccessToken tokenDeAccesoActual) {

                if (tokenDeAccesoActual == null){
                    cerrarSesionFirebase();
                }
            }
        };
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
                    textViewUsuario.setText(usuario.getEmail());
                    Toast.makeText(IniciarSesionFacebookActivity.this, "Usuario: " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                } else {
                    /**
                     * El usuario aún no ha iniciado sesión.
                     */
                    Toast.makeText(IniciarSesionFacebookActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Método para cerrar sesión con Firebase.
     */
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

    /**
     * Debemos mandar llamar al método onActivityResult() de nuestro manejador de llamadas facebook.
     *
     * @param codigoSolicitud
     * @param codigoResultado
     * @param datos
     */
    @Override
    protected void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datos) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos);
        manejadorDeLlamadasFacebook.onActivityResult(codigoSolicitud, codigoResultado, datos);
    }

    /**
     * Método para iniciar sesión con Firebase utilizando la sesión de Facebook.
     *
     * @param tokenDeAcceso
     */
    private void iniciarSesionFirebaseConFacebook(AccessToken tokenDeAcceso) {
        /**
         * Creamos la credencial utilizando el token de acceso que nos da Facebook.
         */
        AuthCredential credencial = FacebookAuthProvider.getCredential(tokenDeAcceso.getToken());
        /**
         * Iniciamos sesión con Firebase utilizando la credencial.
         */
        autenticacionFirebase.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            /**
                             * Hubo algún error al iniciar la sesión.
                             */
                            Toast.makeText(IniciarSesionFacebookActivity.this, "Hubo un error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
