package pl.droidsonroids.bootcamp.yo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import pl.droidsonroids.bootcamp.yo.Constants;
import pl.droidsonroids.bootcamp.yo.R;
import pl.droidsonroids.bootcamp.yo.api.ApiService;
import pl.droidsonroids.bootcamp.yo.model.User;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class UserListAdapter extends RecyclerView.Adapter<UserItemViewVolder> {

    private List<User> userList = Collections.emptyList();

    @Override
    public UserItemViewVolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return new UserItemViewVolder(view);
    }

    @Override
    public void onBindViewHolder(UserItemViewVolder userItemViewVolder, final int i) {
        User user = userList.get(i);
        userItemViewVolder.bindData(user);
        if(user.isSentNotification()) {
            userItemViewVolder.itemView.setBackgroundColor(Color.CYAN);
        } else {
            userItemViewVolder.itemView.setBackgroundColor(Color.WHITE);
        }
        userItemViewVolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.dialog_title))
                        .setPositiveButton(context.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                final int selfId = sharedPreferences.getInt(Constants.KEY_USER_ID, 0);
                                if (selfId == 0) {
                                    Toast.makeText(context, "registration not complete", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                ApiService.API_SERVICE.postYo(userList.get(i).getId(), selfId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Void>() {
                                    @Override
                                    public void call(Void aVoid) {
                                        Toast.makeText(context, "message sent", Toast.LENGTH_SHORT).show();
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(context.getText(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void refreshUserList(List<User> userList) {
        this.userList = userList;
        Collections.sort(this.userList);
        notifyDataSetChanged();
    }
}
