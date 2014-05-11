package com.main.harriscam;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;
import java.util.Stack;


public class FeedbackActivity extends Activity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_feedback );

        getActionBar().setDisplayHomeAsUpEnabled( true );
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.feedback, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_send:
                String subject = ( ( EditText ) findViewById( R.id.etSubject ) ).getText().toString();
                String message = ( ( EditText ) findViewById( R.id.etMessage ) ).getText().toString();
                sendEmail( subject, message );
                finish();

                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask( this );

                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private void sendEmail( String subject, String message ) {
        Intent email = new Intent( Intent.ACTION_SEND );
        email.putExtra( Intent.EXTRA_EMAIL, new String[] { "kimdatagoon@gmail.com" } );
        email.putExtra( Intent.EXTRA_SUBJECT, subject );
        email.putExtra( Intent.EXTRA_TEXT, message );
        email.setType( "message/rfc822" );
        startActivity( createEmailOnlyChooserIntent( email, getString( R.string.msg_choose_email ) ) );
    }

    private Intent createEmailOnlyChooserIntent( Intent source, CharSequence chooserTitle ) {
        Stack< Intent > intents = new Stack< Intent >();
        Intent i = new Intent( Intent.ACTION_SENDTO, Uri.fromParts( "mailto", "kimdatagoon@gmail.com", null ) );
        List< ResolveInfo > activities = getPackageManager().queryIntentActivities( i, 0 );

        for ( ResolveInfo ri : activities ) {
            Intent target = new Intent( source );
            target.setPackage( ri.activityInfo.packageName );
            intents.add( target );
        }

        if ( !intents.isEmpty() ) {
            Intent chooserIntent = Intent.createChooser( intents.remove( 0 ), chooserTitle );
            chooserIntent.putExtra( Intent.EXTRA_INITIAL_INTENTS, intents.toArray( new Parcelable[ intents.size() ] ) );

            return chooserIntent;
        } else {
            return Intent.createChooser( source, chooserTitle );
        }
    }
}
