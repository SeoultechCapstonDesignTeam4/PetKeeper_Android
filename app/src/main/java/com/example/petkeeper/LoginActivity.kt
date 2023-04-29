package com.example.petkeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.petkeeper.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mGoogleSignInClient : GoogleSignInClient? = null
    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>

    override fun onStart() {
        super.onStart()
        googleLoginLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result ->
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            initLogin()
        }

        binding.naverLoginButton.setOnClickListener {
            initnNaverLogin()
        }

        binding.kakaoLoginButton.setOnClickListener {
            initKakaoLogin()
        }

        binding.googleLoginButton.setOnClickListener {
            initGoogleLogin()
        }

        binding.signUpButton.setOnClickListener {
            initSignUpButton()
        }
    }

    //일반 로그인
    private fun initLogin(){
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)

        RetrofitBuilder.api.getUserInfo("apiTest").enqueue(object : Callback<UserInfo> {
            override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
                val userInfo = response.body()
                if(userInfo != null){
                    Log.d("API test", userInfo.id)
                    Log.d("API test", userInfo.password)
                }
            }

            override fun onFailure(call: Call<UserInfo>, t: Throwable) {
                Log.d("API test", "fail")
            }
        })
    }

    //네이버 로그인
    private fun initnNaverLogin(){
        NaverIdLoginSDK.initialize(applicationContext,
            resources.getString(R.string.naver_client_id),
            resources.getString(R.string.naver_client_secret),
            resources.getString(R.string.app_name))

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val user = response.profile
                val userId = user!!.id.toString()
                val email = user.mobile.toString()
                val birth = user.birthYear.toString()
                val name = user.name.toString()
                val gender = user.gender.toString()

                Log.d("Naver Login", "네이버 로그인 성공")
                Log.d("Naver Login", userId)
                Log.d("Naver Login", email)
                Log.d("Naver Login", birth)
                Log.d("Naver Login", name)
                Log.d("Naver Login", gender)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d("Naver Login", "네이버 로그인 실패")
            }
            override fun onError(errorCode: Int, message: String) {
                Log.d("Naver Login", "네이버 로그인 실패")
                onFailure(errorCode, message)
            }
        }

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                NidOAuthLogin().callProfileApi(profileCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d("Naver Login", errorCode)
                Log.d("Naver Login",  errorDescription!!)

            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(this@LoginActivity, oauthLoginCallback)
    }

    //카카오 로그인
    private fun initKakaoLogin(){
        KakaoSdk.init(this@LoginActivity, getString(R.string.kakao_native_key))

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("Kakao Login", "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i("Kakao Login", "카카오계정으로 로그인 성공 ${token.accessToken}")
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
            UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { token, error ->
                if (error != null) {
                    Log.e("Kakao Login", "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback = callback)
                } else if (token != null) {
                    Log.i("Kakao Login", "카카오 로그인 성공 ${token.accessToken}")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    finish()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback = callback)
        }
    }

    //구글 로그인
    private fun initGoogleLogin(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signIntent: Intent = mGoogleSignInClient!!.signInIntent
        googleLoginLauncher.launch(signIntent)
    }

    //구글 로그인 계정 정보 받아오기
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val email = account?.email.toString()
            val googletoken = account?.idToken.toString()
            val googletokenAuth = account?.serverAuthCode.toString()

            Log.d("Google Login", account.id!!)
            Log.d("Google Login", account.familyName!!)
            Log.d("Google Login", account.givenName!!)
            Log.d("Google Login", account.email!!)
            Log.d("Google Login", account.photoUrl.toString())
        } catch (e: ApiException){
            Log.e("Google Login","signInResult:failed Code = " + e.statusCode)
        }
    }

    private fun initSignUpButton(){
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}