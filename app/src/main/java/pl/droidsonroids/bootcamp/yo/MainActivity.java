/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.droidsonroids.bootcamp.yo;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.bootcamp.yo.api.ApiService;
import pl.droidsonroids.bootcamp.yo.model.User;
import pl.droidsonroids.bootcamp.yo.services.RegistrationIntentService;
import pl.droidsonroids.bootcamp.yo.ui.UserListAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Bind(R.id.users_recycler)
    RecyclerView usersRecycler;
    @Bind(R.id.name_edit_text)
    EditText nameEditText;
    @Bind(R.id.register_button)
    Button registerButton;

    final UserListAdapter userListAdapter = new UserListAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        usersRecycler.setLayoutManager(new LinearLayoutManager(this));
        usersRecycler.setAdapter(userListAdapter);
        onRefreshButtonClick();

        changeUIValuesWhenRegistered();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @OnClick(R.id.refresh_button)
    public void onRefreshButtonClick() {
        ApiService.API_SERVICE.getUsers().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<User>>() {
            @Override
            public void call(List<User> users) {
                if (getIntent().getAction() != null) {
                    for (User user : users) {
                        if (user.getName().equals(getIntent().getAction())) {
                            user.setSentNotification(true);
                        }
                    }
                }
                userListAdapter.refreshUserList(users);
                changeUIValuesWhenRegistered();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @OnClick(R.id.register_button)
    public void onRegisterButtonClick() {
        if (checkPlayServices()) {
            if(nameEditText.getText().toString().length() != 0) {
                Intent intent = new Intent(this, RegistrationIntentService.class);
                intent.setAction(nameEditText.getText().toString());
                startService(intent);
            } else {
                Toast.makeText(this, R.string.none_name, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void changeUIValuesWhenRegistered() {
        String name = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.KEY_USER_NAME, null);
        if(name != null) {
            nameEditText.setText(name);
            registerButton.setText(R.string.change_name);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                GoogleApiAvailability.getInstance().showErrorDialogFragment(this, resultCode, 0);
                finish();
            }
            return false;
        }
        return true;
    }

}
