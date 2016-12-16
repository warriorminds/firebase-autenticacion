package com.warriorminds.firebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twitter.sdk.android.Twitter;

public abstract class BaseFirebaseActivity extends AppCompatActivity {
    public static final String FACEBOOK_ID_PROVEEDOR = "facebook.com";
    public static final String GOOGLE_ID_PROVEEDOR = "google.com";
    public static final String TWITTER_ID_PROVEEDOR = "twitter.com";

    protected FirebaseAuth autenticacionFirebase;
    protected FirebaseAuth.AuthStateListener listenerAutenticacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inicializarAutenticacion();
    }

    @Override
    public void onStart() {
        super.onStart();
        autenticacionFirebase.addAuthStateListener(listenerAutenticacion);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerAutenticacion != null) {
            autenticacionFirebase.removeAuthStateListener(listenerAutenticacion);
        }
    }

    protected abstract void sesionIniciada();

    protected abstract void usuarioAutenticado();

    private void inicializarAutenticacion() {
        autenticacionFirebase = FirebaseAuth.getInstance();

        listenerAutenticacion = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null && !usuario.isAnonymous()) {
                    usuarioAutenticado();
                }
            }
        };
    }

    protected void iniciarSesionFirebase(AuthCredential credencial) {
        autenticacionFirebase.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(BaseFirebaseActivity.this, "Hubo un error al conectar con Firebase.", Toast.LENGTH_SHORT).show();
                            cerrarSesion();
                        } else {
                            sesionIniciada();
                        }
                    }
                });
    }

    protected void enlazarCuentas(final AuthCredential credencial) {
        FirebaseUser usuario = autenticacionFirebase.getCurrentUser();
        if (usuario != null) {
            usuario.linkWithCredential(credencial)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                iniciarSesionFirebase(credencial);
                            } else {
                                sesionIniciada();
                            }
                        }
                    });
        } else {
            iniciarSesionFirebase(credencial);
        }
    }

    protected void cerrarSesion() {
        autenticacionFirebase.signOut();
        LoginManager.getInstance().logOut();
        Twitter.logOut();
    }
}
