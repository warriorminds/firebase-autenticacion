package com.warriorminds.firebase;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class IniciarSesionCorreoContrasenaActivity extends AppCompatActivity {

    // Variables que utiliza Firebase.
    /**
     * FirebaseAuth es el objeto que contiene el listener que escucha los cambios en la cuenta
     * y con el que se puede iniciar sesión con correo electrónico y contraseña.
     */
    private FirebaseAuth autenticacionFirebase;
    /**
     * El listener escucha cambios en la cuenta. Cuando se inicia sesión de manera exitosa,
     * se ejecuta el método onAuthStateChanged y se obtiene un FirebaseUser != null. Si el FirebaseUser
     * es null, quiere decir que no se inició sesión.
     */
    private FirebaseAuth.AuthStateListener listenerAutenticacion;

    // Vistas
    private AutoCompleteTextView correo;
    private EditText contraseña;
    private View cargando;
    private View formaDeLogin;
    private Button botonIniciarSesion;
    private Button botonCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion_email_password);

        inicializarVistas();
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
                    Toast.makeText(IniciarSesionCorreoContrasenaActivity.this, "Usuario: " + usuario.getEmail(), Toast.LENGTH_SHORT).show();
                    botonCerrarSesion.setVisibility(View.VISIBLE);
                    deshabilitarActivarCampos(false);
                    asignarUsuario(usuario);
                } else {
                    Toast.makeText(IniciarSesionCorreoContrasenaActivity.this, "Usuario sin sesión", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    /**
     * Se inicializan todas las vistas de la actividad.
     */
    private void inicializarVistas() {
        configurarActionBar();

        botonIniciarSesion = (Button) findViewById(R.id.boton_iniciar_sesion);
        botonCerrarSesion = (Button) findViewById(R.id.boton_cerrar_sesion);

        correo = (AutoCompleteTextView) findViewById(R.id.email);
        correo.setAdapter(agregarEmailsAutocompletar());

        contraseña = (EditText) findViewById(R.id.password);

        /**
         * Este Observador nos ayuda a saber si el usuario puso un correo y un contraseña válido.
         *
         * Si se cumplen los requisitos, se activa el botón para iniciar sesión.
         */
        TextWatcher observadorTexto = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (esFormaValida(correo.getText().toString(), contraseña.getText().toString())) {
                    botonIniciarSesion.setEnabled(true);
                } else {
                    botonIniciarSesion.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        contraseña.addTextChangedListener(observadorTexto);
        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarSesion(correo.getText().toString(), contraseña.getText().toString());
            }
        });

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrarSesion();
            }
        });
        formaDeLogin = findViewById(R.id.forma_login);
        cargando = findViewById(R.id.progreso_login);
    }

    /**
     * Agrega el botón para regresar desde la Action Bar.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void configurarActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Este método nos indica si los valores de los campos son válidos.
     * @param email
     * @param password
     * @return true si la forma es válida.
     */
    private boolean esFormaValida(String email, String password) {
        if (esEmailValido(email) && esPasswordValido(password)) {
            return true;
        }
        return false;
    }

    /**
     * Este método nos indica si el correo es válido. Por ahora solamente checa si contiene @.
     * Se podría utilizar una expresión regular.
     *
     * @param email
     * @return true si el correo tiene un formato válido.
     */
    private boolean esEmailValido(String email) {
        return email.contains("@");
    }

    /**
     * Método que checa si el contraseña es válido. Por ahora solamente checa que sea mayor a 5 caracteres.
     * Firebase pide que al menos sean 6 caracteres en el contraseña.
     *
     * @param password
     * @return true si el contraseña es válido.
     */
    private boolean esPasswordValido(String password) {
        return password.length() > 5;
    }

    /**
     * Este método esconde todos los campos para mostrar un círculo de progreso. De igual manera,
     * esconde el círculo de progreso y muestra de vuelta los campos.
     *
     * @param mostrar true si se muestra el círculo de progreso y se esconden los campos.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void mostrarProgreso(final boolean mostrar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
            formaDeLogin.animate().setDuration(shortAnimTime).alpha(
                    mostrar ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
                }
            });

            cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            cargando.animate().setDuration(shortAnimTime).alpha(
                    mostrar ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            cargando.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            formaDeLogin.setVisibility(mostrar ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Método que obtiene los emails de las cuentas asociadas al teléfono. El usuario debe de haber
     * aceptado el permiso de Cuentas.
     *
     * @return adapter con las cuentas de correo disponibles en el teléfono.
     */
    private ArrayAdapter<String> agregarEmailsAutocompletar() {
        /**
         * Si no se ha aceptado el permiso, regresar un adapter sin cuentas.
         */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        }

        /**
         * Obtener cuentas.
         */
        Account[] cuentas = AccountManager.get(this).getAccounts();
        List<String> emails = new ArrayList<>();

        if (cuentas != null && cuentas.length > 0) {
            for (Account account : cuentas) {
                if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                    emails.add(account.name);
                }
            }
        }

        return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emails);
    }

    /**
     * Crear una cuenta utilizando correo y contraseña utilizando Firebase.
     * @param email
     * @param password
     */
    private void iniciarSesion(String email, String password) {
        // Mostrar círculo de progreso y esconder los campos.
        mostrarProgreso(true);
        /**
         * Este método se utiliza para iniciar sesión con correo y contraseña. Se agrega un
         * onCompleteListener que nos indica si se pudo iniciar sesión.
         *
         * Si se pudo iniciar sesión, se manda llamar el listener, en donde se puede
         * obtener el usuario.
         */
        autenticacionFirebase.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mostrarProgreso(false);

                        if (!task.isSuccessful()) {
                            Toast.makeText(IniciarSesionCorreoContrasenaActivity.this, R.string.hubo_error, Toast.LENGTH_SHORT).show();
                        } else {
                            // Deshabilita los campos si el inicio de sesión fue exitoso.
                            botonCerrarSesion.setVisibility(View.VISIBLE);
                            deshabilitarActivarCampos(false);
                        }
                    }
                });
    }


    /**
     * Método que se utiliza para cerrar sesión.
     */
    private void cerrarSesion() {
        autenticacionFirebase.signOut();
        botonCerrarSesion.setVisibility(View.GONE);
        TextView usuario = (TextView) findViewById(R.id.usuario);
        usuario.setText("");
        deshabilitarActivarCampos(true);
    }

    /**
     * Método que deshabilita todos los campos.
     */
    private void deshabilitarActivarCampos(boolean activar) {
        correo.setEnabled(activar);
        contraseña.setEnabled(activar);
        botonIniciarSesion.setEnabled(activar);
    }

    /**
     * Método que obtiene el correo del usuario y lo asigna a una text view.
     * @param usuarioFirebase
     */
    private void asignarUsuario(FirebaseUser usuarioFirebase) {
        TextView usuario = (TextView) findViewById(R.id.usuario);
        usuario.setText("Usuario: " +usuarioFirebase.getEmail());
    }
}
