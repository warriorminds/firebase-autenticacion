package com.warriorminds.firebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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

public class UsuariosAnonimosActivity extends AppCompatActivity {

    // Variables que utiliza Firebase.
    /**
     * FirebaseAuth es el objeto que contiene el listener que escucha los cambios en la cuenta
     * y con el que se puede crear una cuenta nueva con correo electrónico y contraseña.
     */
    private FirebaseAuth autenticacionFirebase;
    /**
     * El listener escucha cambios en la cuenta. Cuando se crea una cuenta de manera exitosa,
     * se ejecuta el método onAuthStateChanged y se obtiene un FirebaseUser != null. Si el FirebaseUser
     * es null, quiere decir que no se inició sesión con el nuevo usuario.
     */
    private FirebaseAuth.AuthStateListener listenerAutenticacion;

    // Variables de Facebook
    /**
     * Objeto de Facebook que se utiliza para manejar las llamadas desde la actividad.
     */
    private CallbackManager manejadorDeLlamadasFacebook;

    // Botón de inicio de sesión de Facebook
    private LoginButton botonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Deben inicializar el SDK de Facebook antes del setContentView()
         */
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_usuarios_anonimos);

        inicializarVistas();
        inicializarFacebook();
        inicializarAutenticacion();
        autenticarAnonimamente();
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


    private void inicializarVistas() {
        botonLogin = (LoginButton) findViewById(R.id.boton_login_facebook);
        Button cerrarSesionBoton = (Button) findViewById(R.id.boton_cerrar_sesion);
        cerrarSesionBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
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
                enlazarCuentas(resultadoInicioSesionFacebook.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(UsuariosAnonimosActivity.this, "Usuario canceló inicio de sesión con Facebook.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(UsuariosAnonimosActivity.this, "Hubo un error al iniciar sesión con Facebook.", Toast.LENGTH_SHORT).show();
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
                    cerrarSesion();
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

        /**
         * Crear el listener para escuchar los cambios en la cuenta.
         */
        listenerAutenticacion = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /**
                 * Obtener el usuario actual.
                 */
                FirebaseUser usuario = firebaseAuth.getCurrentUser();

                /**
                 * Si el usuario != null, quiere decir que se tiene una sesión iniciada. Si no,
                 * quiere decir que no se pudo iniciar sesión, o no se ha iniciado sesión aún.
                 */
                if (usuario != null) {
                    Toast.makeText(UsuariosAnonimosActivity.this, "Usuario: " + (usuario.isAnonymous() ? "Usuario anónimo" : usuario.getEmail()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UsuariosAnonimosActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void autenticarAnonimamente() {
        autenticacionFirebase.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(UsuariosAnonimosActivity.this, "Hubo un error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        botonLogin.setEnabled(false);
    }

    private void enlazarCuentas(AccessToken tokenDeAcceso) {
        AuthCredential credencial = FacebookAuthProvider.getCredential(tokenDeAcceso.getToken());

        autenticacionFirebase.getCurrentUser().linkWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(UsuariosAnonimosActivity.this, "Hubo un error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
