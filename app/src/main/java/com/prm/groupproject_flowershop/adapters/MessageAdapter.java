package com.prm.groupproject_flowershop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.prm.groupproject_flowershop.R;
import com.prm.groupproject_flowershop.constants.AppConstants;
import com.prm.groupproject_flowershop.models.Customer;
import com.prm.groupproject_flowershop.models.MessageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    private Context context;
    private int layoutRight;
    private int layoutLeft;
    private ArrayList<MessageModel> messageList;
    private Customer receiver;

    public MessageAdapter(Context context, int layoutRight, int layoutLeft,
                          ArrayList<MessageModel> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.layoutRight = layoutRight;
        this.layoutLeft = layoutLeft;
    }

    public void setReceiver(Customer receiver) {
        this.receiver = receiver;
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }
    public String getItemStringId(int position) {
        return messageList.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        MessageModel message = (MessageModel) getItem(position);

        if (message.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            view = inflater.inflate(layoutRight, null);
        } else {
            view = inflater.inflate(layoutLeft, null);

            ImageView imgAvatar = view.findViewById(R.id.imgChatAvatar);
            TextView userName = view.findViewById(R.id.txtChatUsername);

            userName.setText(receiver.getCustomerName());
            if (!receiver.getAvatar().equals(AppConstants.DEFAULT_AVATAR)) {
                Picasso.get().load(receiver.getAvatar()).into(imgAvatar);
            }
        }
        TextView txtMessage = view.findViewById(R.id.txtChatMessage);
        txtMessage.setText(message.getMessage()+"");

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
