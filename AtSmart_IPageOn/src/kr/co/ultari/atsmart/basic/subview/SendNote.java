package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.SendMessage;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SendNote extends MessengerActivity implements OnClickListener {
        private TextView tvCount, tvUserlist, fileList;
        private EditText etTitle, etContent;
        private ImageButton btnClose, btnAddFile, btnAddUser;
        private Button btnSend, btnCancel;
        private String mUserId, mUserName;

        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.sub_note_popup );
                tvCount = ( TextView ) findViewById( R.id.note_count );
                tvUserlist = ( TextView ) findViewById( R.id.note_userlist );
                etTitle = ( EditText ) findViewById( R.id.note_title );
                etContent = ( EditText ) findViewById( R.id.note_content );
                btnClose = ( ImageButton ) findViewById( R.id.note_close );
                btnClose.setOnClickListener( this );
                btnAddFile = ( ImageButton ) findViewById( R.id.note_addfile );
                btnAddFile.setOnClickListener( this );
                btnAddUser = ( ImageButton ) findViewById( R.id.note_adduser );
                btnAddUser.setOnClickListener( this );
                btnSend = ( Button ) findViewById( R.id.note_send );
                btnSend.setOnClickListener( this );
                btnCancel = ( Button ) findViewById( R.id.note_cancel );
                btnCancel.setOnClickListener( this );
                fileList = ( TextView ) findViewById( R.id.note_filelist );
                mUserId = getIntent().getStringExtra( "USERID" );
                mUserName = getIntent().getStringExtra( "USERNAME" );
                String[] parse = mUserName.split( "," );
                tvCount.setText( getString( R.string.note_counttitle ) + parse.length + getString( R.string.people ) + ")" );
                tvUserlist.setText( mUserName );
                tvUserlist.setMovementMethod( new ScrollingMovementMethod() );
        }

        @Override
        public void onClick( View v )
        {
                if ( v == btnClose )
                {
                        hideKeyboard();
                        finish();
                }
                else if ( v == btnAddFile )
                {
                        // 파일첨부 탐색기
                }
                else if ( v == btnAddUser )
                {
                        // 사용자 추가
                }
                else if ( v == btnSend )
                {
                        sendNoteMessage();
                }
                else if ( v == btnCancel )
                {
                        hideKeyboard();
                        finish();
                }
        }

        private void sendNoteMessage()
        {
                if ( etContent.getText().toString().equals( "" ) ) return;
                try
                {
                        String[] parseIds = mUserId.split( "," );
                        for ( int i = 0; i < parseIds.length; i++ )
                                new SendMessage( getApplicationContext(), parseIds[i], Define.getMyId(), Define.getMyName(), etTitle.getText().toString()
                                                .trim(), etContent.getText().toString().trim() );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                text.setText( getString( R.string.send ) );
                text.setTypeface( Define.tfRegular );
                Toast toast = new Toast( getApplicationContext() );
                toast.setGravity( Gravity.CENTER, 0, 0 );
                toast.setDuration( Toast.LENGTH_SHORT );
                toast.setView( layout );
                toast.show();
                etTitle.setText( "" );
                etContent.setText( "" );
        }

        private void hideKeyboard()
        {
                if ( etTitle.isFocused() )
                {
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( Context.INPUT_METHOD_SERVICE );
                        imm.hideSoftInputFromWindow( etTitle.getWindowToken(), 0 );
                }
                if ( etContent.isFocused() )
                {
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( Context.INPUT_METHOD_SERVICE );
                        imm.hideSoftInputFromWindow( etContent.getWindowToken(), 0 );
                }
        }
}
