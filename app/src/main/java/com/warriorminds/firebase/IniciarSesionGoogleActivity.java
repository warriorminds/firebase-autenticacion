package com.warriorminds.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/***
 * Esta clase implementa el inicio de sesión con Google, utilizando Firebase.
 * Para que funcione, deben de agregar su requestIdToken en el método inicializarGoogle().
 *
 * @author warriorminds
 */
public class IniciarSesionGoogleActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /**
     * Código de solicitud utilizado para iniciar la Actividad de Inicio de Sesión de Google.
     */
    private static final int CS_INICIAR_SESION = 9001;

    /**
     * Objeto Cliente API de Google.
     */
    private GoogleApiClient clienteApiGoogle;

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

    /**
     * Vistas utilizadas: TextView muestra el usuario, Button es para cerrar sesión,
     * SignInButton es el botón de Google para iniciar sesión.
     */
    private TextView textViewUsuario;
    private Button botonCerrarSesion;
    private SignInButton botonIniciarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion_google);

        inicializarVistas();
        inicializarGoogle();
        inicializarAutenticacion();
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
     * Este método se llama, en este ejemplo, después de seleccionar la cuenta con la que iniciarás
     * sesión. Se tienen los datos de la cuenta con la que el usuario inició sesión en Google.
     *
     * @param codigoSolicitud
     * @param codigoResultado
     * @param datos
     */
    @Override
    public void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datos) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos);
        /**
         * Revisamos que sea nuestro código de solicitud.
         */
        if (codigoSolicitud == CS_INICIAR_SESION) {
            /**
             * Obtenemos el resultado de la acción, en este caso el resultado del inicio de sesión.
             */
            GoogleSignInResult resultado = Auth.GoogleSignInApi.getSignInResultFromIntent(datos);
            if (resultado.isSuccess()) {
                /**
                 * Obtenemos el objeto cuenta que trae información del usuario.
                 */
                GoogleSignInAccount cuenta = resultado.getSignInAccount();
                /**
                 * Se inicia sesión con Firebase utilizando la cuenta de Google seleccionada.
                 */
                inicioSesionFirebaseConCuentaGoogle(cuenta);
            } else {
                /**
                 * Notificar que hubo algún error.
                 */
                Toast.makeText(this, R.string.hubo_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método para inicializar nuestras vistas.
     */
    private void inicializarVistas() {
        botonIniciarSesion = (SignInButton) findViewById(R.id.boton_sesion_google);
        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesionConGoogle();
            }
        });
        botonCerrarSesion = (Button) findViewById(R.id.boton_cerrar_sesion);
        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
        textViewUsuario = (TextView) findViewById(R.id.usuario);
    }

    /**
     * Método para inicializar Google.
     */
    private void inicializarGoogle() {
        /**
         * Este objeto son las opciones que Google nos proporcionará al momento de hacer login.
         * En este ejemplo, utilizamos la configuración por defecto.
         *
         * Se debe de asignar el requestIdToken que nos da Firebase en la consola, a la hora de habilitar
         * el inicio de sesión con Google.
         *
         * TODO: Agreguen su requestIdToken aquí, para que funcione.
         */
        GoogleSignInOptions opcionesInicioSesionGoogle = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /**
         * Objeto que se utiliza para iniciar la actividad para iniciar sesión. Se configura con el listener
         * para escuchar los cambios en la conexión con Google. Se le agrega el API que vamos a utilizar,
         * que es el GOOGLE_SIGN_IN_API. También se le agregan las opciones de GoogleSignInOptions que
         * se definieron antes.
         */
        clienteApiGoogle = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, opcionesInicioSesionGoogle)
                .build();
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
                    botonCerrarSesion.setVisibility(View.VISIBLE);
                    botonIniciarSesion.setEnabled(false);

                    textViewUsuario.setText(usuario.getEmail());
                    Toast.makeText(IniciarSesionGoogleActivity.this, "Usuario: " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                } else {
                    /**
                     * El usuario aún no ha iniciado sesión.
                     */
                    Toast.makeText(IniciarSesionGoogleActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Este método inicia la actividad para iniciar sesión con Google. Esta actividad es donde
     * puedes elegir con qué correo iniciar sesión. Se requiere pasar el CS_INICIAR_SESION
     * para obtener el resultado de esa actividad una vez que el usuario termina.
     */
    private void iniciarSesionConGoogle() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(clienteApiGoogle);
        startActivityForResult(intent, CS_INICIAR_SESION);
    }

    /**
     * Método que se utiliza para cerrar sesión.
     */
    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        botonCerrarSesion.setVisibility(View.GONE);
        botonIniciarSesion.setEnabled(true);
        textViewUsuario.setText("");
    }

    /**
     * Una vez que se inició sesión con Google y se obtuvo la cuenta, se debe iniciar sesión
     * utilizando Firebase.
     * @param cuentaGoogle
     */
    private void inicioSesionFirebaseConCuentaGoogle(GoogleSignInAccount cuentaGoogle) {
        /**
         * Debemos obtener esta credencial de Firebase utilizando el Token ID de nuestra cuenta Google.
         */
        AuthCredential credencial = GoogleAuthProvider.getCredential(cuentaGoogle.getIdToken(), null);
        /**
         * Iniciar sesión con Firebase, utilizando la credencial obtenida.
         */
        autenticacionFirebase.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            /**
                             * Hubo un error al iniciar la sesión con Firebase.
                             */
                            Toast.makeText(IniciarSesionGoogleActivity.this, "Falló la autenticación.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
