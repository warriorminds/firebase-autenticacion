package com.warriorminds.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FacebookAuthProvider;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class IniciarSesionActivity extends BaseFirebaseActivity {

    // Facebook
    private CallbackManager manejadorDeLlamadasFacebook;
    private LoginButton botonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig configuracionAutenticacionTwitter = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret));
        Fabric.with(this, new Twitter(configuracionAutenticacionTwitter));

        setContentView(R.layout.activity_iniciar_sesion);

        inicializarVistas();
        inicializarFacebook();
    }

    @Override
    protected void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datos) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos);
        manejadorDeLlamadasFacebook.onActivityResult(codigoSolicitud, codigoResultado, datos);
    }

    private void inicializarVistas() {
        botonLogin = (LoginButton) findViewById(R.id.boton_login_facebook);
    }

    private void inicializarFacebook() {
        manejadorDeLlamadasFacebook = CallbackManager.Factory.create();
        botonLogin.setReadPermissions("email", "public_profile");
        botonLogin.registerCallback(manejadorDeLlamadasFacebook, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult resultadoInicioSesionFacebook) {
                iniciarSesionFirebaseConFacebook(resultadoInicioSesionFacebook.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(IniciarSesionActivity.this, "Usuario canceló inicio de sesión con Facebook.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(IniciarSesionActivity.this, "Hubo un error al iniciar sesión con Facebook.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarSesionFirebaseConFacebook(AccessToken tokenDeAcceso) {
        enlazarCuentas(FacebookAuthProvider.getCredential(tokenDeAcceso.getToken()));
    }

    @Override
    protected void sesionIniciada() {
        setResult(RESULT_OK);
        finish();
    }
}
