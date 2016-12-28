package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.view.ChatWindow;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
//2016-03-31
public class CustomDeleteRoomDialog extends Dialog implements android.view.View.OnClickListener 
{
        public Dialog d;
        public Button yes, no;
        public TextView tvTitle, tvContent;
        public Button btnYes, btnNo;
        private Context c;
        
        public CustomDeleteRoomDialog( Context context )
        {
                super( context );
                this.c = context;
        }

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                
                setContentView( R.layout.chat_dialog_view );
                
                tvTitle = ( TextView ) findViewById( R.id.chat_dialog_title );
                tvContent = ( TextView ) findViewById( R.id.chat_dialog_content );
                btnYes = ( Button ) findViewById( R.id.chat_dialog_yes );
                btnNo = ( Button ) findViewById( R.id.chat_dialog_no );
                btnYes.setOnClickListener( this );
                btnNo.setOnClickListener( this );
                
                tvTitle.setTypeface( Define.tfRegular ); 
                tvContent.setTypeface( Define.tfRegular );
                btnYes.setTypeface( Define.tfRegular );
                btnNo.setTypeface( Define.tfRegular );
        }

        @Override
        public void onClick( View v )
        {
                if(v == btnYes)
                {
                        if(MainActivity.cw != null)
                        {
                                Message m = MainActivity.cw.alertHandler.obtainMessage( Define.AM_CONFIRM_YES, null );
                                MainActivity.cw.alertHandler.sendMessage( m );
                                dismiss();
                        }
                }
                else if(v == btnNo)
                {
                        dismiss();
                }
        }
}
