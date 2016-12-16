package com.warriorminds.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.List;

import io.fabric.sdk.android.Fabric;

public class IniciarSesionActivity extends BaseFirebaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int CS_INICIAR_SESION = 9001;

    // Facebook
    private CallbackManager manejadorDeLlamadasFacebook;
    private LoginButton botonIniciarSesionFacebook;

    // Google
    private GoogleApiClient clienteApiGoogle;
    private SignInButton botonIniciarSesionGoogle;

    // Twitter
    private TwitterLoginButton botonIniciarSesionTwitter;

    private TextView tvErrorEnlazarCuentas;
    private View layoutDesenlazarCuentas;
    private Button botonDesenlazarFb;
    private Button botonDesenlazarGoogle;
    private Button botonDesenlazarTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        TwitterAuthConfig configuracionAutenticacionTwitter = new TwitterAuthConfig(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret));
        Fabric.with(this, new Twitter(configuracionAutenticacionTwitter));

        setContentView(R.layout.activity_iniciar_sesion);

        inicializarVistas();
        inicializarFacebook();
        inicializarGoogle();
        inicializarTwitter();
    }

    private void habilitarBotones() {
        if (autenticacionFirebase.getCurrentUser() != null) {
            List<String> proveedores = autenticacionFirebase.getCurrentUser().getProviders();
            layoutDesenlazarCuentas.setVisibility(proveedores.size() > 0 ? View.VISIBLE : View.GONE);

            for (String proveedor : proveedores) {
                switch (proveedor) {
                    case FACEBOOK_ID_PROVEEDOR:
                        botonIniciarSesionFacebook.setVisibility(View.GONE);
                        botonDesenlazarFb.setVisibility(View.VISIBLE);
                        break;
                    case GOOGLE_ID_PROVEEDOR:
                        botonIniciarSesionGoogle.setVisibility(View.GONE);
                        botonDesenlazarGoogle.setVisibility(View.VISIBLE);
                        break;
                    case TWITTER_ID_PROVEEDOR:
                        botonIniciarSesionTwitter.setVisibility(View.GONE);
                        botonDesenlazarTwitter.setVisibility(View.VISIBLE);
                        break;
                }
            }
            if (proveedores.size() == 3) {
                tvErrorEnlazarCuentas.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int codigoSolicitud, int codigoResultado, Intent datos) {
        super.onActivityResult(codigoSolicitud, codigoResultado, datos);
        manejadorDeLlamadasFacebook.onActivityResult(codigoSolicitud, codigoResultado, datos);
        botonIniciarSesionTwitter.onActivityResult(codigoSolicitud, codigoResultado, datos);

        if (codigoSolicitud == CS_INICIAR_SESION) {
            GoogleSignInResult resultado = Auth.GoogleSignInApi.getSignInResultFromIntent(datos);
            if (resultado.isSuccess()) {
                GoogleSignInAccount cuenta = resultado.getSignInAccount();
                iniciarSesionFirebaseConGoogle(cuenta);
            } else {
                Toast.makeText(this, R.string.hubo_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void inicializarVistas() {
        tvErrorEnlazarCuentas = (TextView) findViewById(R.id.tvErrorCuentasEnlazadas);
        layoutDesenlazarCuentas = findViewById(R.id.desenlazar_cuentas);
        botonDesenlazarFb = (Button) findViewById(R.id.btnDesenlazarFb);
        botonDesenlazarGoogle = (Button) findViewById(R.id.btnDesenlazarGoogle);
        botonDesenlazarTwitter = (Button) findViewById(R.id.btnDesenlazarTwitter);

        botonDesenlazarFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desenlazar(FACEBOOK_ID_PROVEEDOR);
            }
        });

        botonDesenlazarGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desenlazar(GOOGLE_ID_PROVEEDOR);
            }
        });

        botonDesenlazarTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desenlazar(TWITTER_ID_PROVEEDOR);
            }
        });

        botonIniciarSesionFacebook = (LoginButton) findViewById(R.id.boton_login_facebook);
        botonIniciarSesionGoogle = (SignInButton) findViewById(R.id.boton_sesion_google);
        botonIniciarSesionGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciarSesionConGoogle();
            }
        });
        botonIniciarSesionTwitter = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
    }

    private void desenlazar(final String idProveedor) {
        for (UserInfo info : autenticacionFirebase.getCurrentUser().getProviderData()) {
            if (TextUtils.equals(info.getProviderId(), idProveedor)) {
                autenticacionFirebase.getCurrentUser().unlink(idProveedor)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (TextUtils.equals(idProveedor, FACEBOOK_ID_PROVEEDOR)) {
                                        LoginManager.getInstance().logOut();
                                        botonDesenlazarFb.setVisibility(View.GONE);
                                        botonIniciarSesionFacebook.setVisibility(View.VISIBLE);
                                    } else if (TextUtils.equals(idProveedor, GOOGLE_ID_PROVEEDOR)) {
                                        botonDesenlazarGoogle.setVisibility(View.GONE);
                                        botonIniciarSesionGoogle.setVisibility(View.VISIBLE);
                                    } else if (TextUtils.equals(idProveedor, TWITTER_ID_PROVEEDOR)) {
                                        Twitter.logOut();
                                        botonDesenlazarTwitter.setVisibility(View.GONE);
                                        botonIniciarSesionTwitter.setVisibility(View.VISIBLE);
                                    }
                                    tvErrorEnlazarCuentas.setVisibility(View.GONE);
                                }
                            }
                        });
                break;
            }
        }
    }

    private void inicializarFacebook() {
        manejadorDeLlamadasFacebook = CallbackManager.Factory.create();
        botonIniciarSesionFacebook.setReadPermissions("email", "public_profile");
        botonIniciarSesionFacebook.registerCallback(manejadorDeLlamadasFacebook, new FacebookCallback<LoginResult>() {

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

    private void inicializarGoogle() {

        // TODO: agreguen su request token id.
        GoogleSignInOptions opcionesInicioSesionGoogle = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        clienteApiGoogle = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, opcionesInicioSesionGoogle)
                .build();
    }

    private void inicializarTwitter() {
        botonIniciarSesionTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> resultado) {
                iniciarSesionFirebaseConTwitter(resultado.data);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });
    }

    private void iniciarSesionConGoogle() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(clienteApiGoogle);
        startActivityForResult(intent, CS_INICIAR_SESION);
    }

    private void iniciarSesionFirebaseConFacebook(AccessToken tokenDeAcceso) {
        enlazarCuentas(FacebookAuthProvider.getCredential(tokenDeAcceso.getToken()));
    }

    private void iniciarSesionFirebaseConGoogle(GoogleSignInAccount cuentaGoogle) {
        enlazarCuentas(GoogleAuthProvider.getCredential(cuentaGoogle.getIdToken(), null));
    }

    private void iniciarSesionFirebaseConTwitter(TwitterSession sesionTwitter) {
        enlazarCuentas(TwitterAuthProvider.getCredential(sesionTwitter.getAuthToken().token, sesionTwitter.getAuthToken().secret));
    }

    @Override
    protected void sesionIniciada() {
        finish();
    }

    @Override
    protected void usuarioAutenticado() {
        habilitarBotones();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
